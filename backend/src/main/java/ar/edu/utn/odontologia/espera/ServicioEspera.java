package ar.edu.utn.odontologia.espera;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.paciente.ContratoAutorizacionCuentaPaciente;
import ar.edu.utn.odontologia.acceso.paciente.IdentidadCuentaPaciente;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.PrestacionReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencias;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
class ServicioEspera implements ContratoServicioEspera {
    private final ContratoAutorizacionCuentaPaciente autorizacion;
    private final PacienteReferencias pacientes;
    private final PrestacionReferencias prestaciones;
    private final ProfesionalReferencias profesionales;
    private final InscripcionesEspera inscripciones;
    private final Clock reloj;

    ServicioEspera(ContratoAutorizacionCuentaPaciente autorizacion, PacienteReferencias pacientes,
                   PrestacionReferencias prestaciones, ProfesionalReferencias profesionales,
                   InscripcionesEspera inscripciones, Clock reloj) {
        this.autorizacion = autorizacion;
        this.pacientes = pacientes;
        this.prestaciones = prestaciones;
        this.profesionales = profesionales;
        this.inscripciones = inscripciones;
        this.reloj = reloj;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InscripcionEspera crear(IdentidadCuentaPaciente identidad, PreferenciasEspera preferencias) {
        UUID pacienteId = bloquearTitular(identidad);
        LocalDate hoy = VigenciaEspera.fechaLocal(reloj.instant());
        UUID id = UUID.randomUUID();
        validarPreferencias(pacienteId, id, preferencias, hoy);
        return vista(inscripciones.save(new InscripcionEsperaEntidad(id, pacienteId, preferencias)), hoy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionEspera> listar(IdentidadCuentaPaciente identidad) {
        UUID pacienteId = autorizacion.exigirPacienteVinculado(identidad);
        LocalDate hoy = VigenciaEspera.fechaLocal(reloj.instant());
        return inscripciones.findAllByPacienteIdOrderById(pacienteId).stream()
                .map(inscripcion -> vista(inscripcion, hoy))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InscripcionEspera> consultar(IdentidadCuentaPaciente identidad, UUID id) {
        UUID pacienteId = autorizacion.exigirPacienteVinculado(identidad);
        LocalDate hoy = VigenciaEspera.fechaLocal(reloj.instant());
        return inscripciones.findByIdAndPacienteId(id, pacienteId).map(inscripcion -> vista(inscripcion, hoy));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InscripcionEspera editar(IdentidadCuentaPaciente identidad, UUID id, PreferenciasEspera preferencias) {
        UUID pacienteId = bloquearTitular(identidad);
        LocalDate hoy = VigenciaEspera.fechaLocal(reloj.instant());
        var inscripcion = buscarPropia(id, pacienteId);
        if (inscripcion.vigencia(hoy) != VigenciaEspera.VIGENTE) {
            throw new IllegalStateException("La inscripción ya finalizó");
        }
        validarPreferencias(pacienteId, id, preferencias, hoy);
        inscripcion.actualizar(preferencias);
        return vista(inscripcion, hoy);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InscripcionEspera darDeBaja(IdentidadCuentaPaciente identidad, UUID id) {
        UUID pacienteId = bloquearTitular(identidad);
        var ahora = reloj.instant();
        LocalDate hoy = VigenciaEspera.fechaLocal(ahora);
        var inscripcion = buscarPropia(id, pacienteId);
        if (inscripcion.vigencia(hoy) == VigenciaEspera.VIGENTE) {
            inscripcion.marcarBaja(ahora.truncatedTo(ChronoUnit.MICROS));
        }
        return vista(inscripcion, hoy);
    }

    private UUID bloquearTitular(IdentidadCuentaPaciente identidad) {
        UUID pacienteId = autorizacion.exigirPacienteVinculado(identidad);
        pacientes.buscarParaModificarEspera(pacienteId).orElseThrow(AutorizacionDenegadaException::new);
        return pacienteId;
    }

    private InscripcionEsperaEntidad buscarPropia(UUID id, UUID pacienteId) {
        return inscripciones.findByIdAndPacienteId(id, pacienteId)
                .orElseThrow(() -> new NoSuchElementException("Inscripción no encontrada"));
    }

    private void validarPreferencias(UUID pacienteId, UUID id, PreferenciasEspera preferencias, LocalDate hoy) {
        if (preferencias == null) {
            throw new IllegalArgumentException("Se requieren preferencias");
        }
        preferencias.validarFechaFinal(hoy);
        if (prestaciones.findById(preferencias.prestacionId()).isEmpty()) {
            throw new IllegalArgumentException("Prestación inexistente");
        }
        if (preferencias.profesionalId() != null && profesionales.findById(preferencias.profesionalId()).isEmpty()) {
            throw new IllegalArgumentException("Profesional inexistente");
        }
        if (inscripciones.existsByPacienteIdAndPrestacionIdAndBajaEnIsNullAndHastaGreaterThanEqualAndIdNot(
                pacienteId, preferencias.prestacionId(), hoy, id)) {
            throw new IllegalStateException("Ya existe una inscripción vigente para la prestación");
        }
    }

    private InscripcionEspera vista(InscripcionEsperaEntidad inscripcion, LocalDate hoy) {
        return new InscripcionEspera(inscripcion.id(), inscripcion.preferencias(),
                inscripcion.vigencia(hoy), inscripcion.bajaEn());
    }
}
