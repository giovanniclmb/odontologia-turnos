package ar.edu.utn.odontologia.pacientes;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.pacientes.persistencia.FichaClinica;
import ar.edu.utn.odontologia.pacientes.persistencia.FichasClinicas;
import ar.edu.utn.odontologia.pacientes.persistencia.NotaClinica;
import ar.edu.utn.odontologia.pacientes.persistencia.NotasClinicas;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioFichaClinica {
    private final PacienteReferencias pacientes;
    private final PacienteProfesionales asignaciones;
    private final FichasClinicas fichas;
    private final NotasClinicas notas;
    private final ServicioAutorizacion autorizacion;
    private final Clock reloj;

    public ServicioFichaClinica(PacienteReferencias pacientes, PacienteProfesionales asignaciones,
                                FichasClinicas fichas, NotasClinicas notas,
                                ServicioAutorizacion autorizacion, Clock reloj) {
        this.pacientes = pacientes;
        this.asignaciones = asignaciones;
        this.fichas = fichas;
        this.notas = notas;
        this.autorizacion = autorizacion;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public FichaClinicaVista consultar(Identidad solicitante, UUID pacienteId) {
        autorizacion.exigir(solicitante, Permiso.FICHA_LEER);
        exigirPaciente(pacienteId);
        exigirAlcance(solicitante, pacienteId);
        return fichas.findById(pacienteId).map(this::vista)
                .orElseGet(() -> new FichaClinicaVista(pacienteId, null, List.of()));
    }

    @Transactional
    public FichaClinicaVista actualizarAntecedentes(Identidad solicitante, UUID pacienteId, String texto) {
        autorizacion.exigir(solicitante, Permiso.FICHA_EDITAR);
        exigirPaciente(pacienteId);
        exigirAlcance(solicitante, pacienteId);
        if (texto == null) {
            throw new IllegalArgumentException("Los antecedentes deben ser texto");
        }
        FichaClinica ficha = obtenerOCrear(pacienteId);
        ficha.setAntecedentes(texto);
        fichas.save(ficha);
        return vista(ficha);
    }

    @Transactional
    public NotaClinicaVista agregarNota(Identidad solicitante, UUID pacienteId, CrearNotaClinica solicitud) {
        autorizacion.exigir(solicitante, Permiso.FICHA_EDITAR);
        exigirPaciente(pacienteId);
        exigirAlcance(solicitante, pacienteId);
        String texto = solicitud == null ? null : solicitud.texto();
        validarNota(texto);
        if (solicitante.usuarioId() == null) {
            throw new AutorizacionDenegadaException();
        }
        obtenerOCrear(pacienteId);
        Instant creadaEn = reloj.instant().truncatedTo(ChronoUnit.MICROS);
        NotaClinica nota = new NotaClinica(UUID.randomUUID(), pacienteId, solicitante.usuarioId(), texto, creadaEn);
        return vista(notas.save(nota));
    }

    @Transactional
    public NotaClinicaVista editarNota(Identidad solicitante, UUID pacienteId, UUID notaId,
                                      EditarNotaClinica cambios) {
        autorizacion.exigir(solicitante, Permiso.FICHA_EDITAR);
        exigirPaciente(pacienteId);
        exigirAlcance(solicitante, pacienteId);
        String texto = cambios == null ? null : cambios.texto();
        validarNota(texto);
        if (notaId == null) {
            throw new IllegalArgumentException("La referencia de la nota es obligatoria");
        }
        NotaClinica nota = notas.findById(notaId)
                .filter(existente -> existente.getPacienteId().equals(pacienteId))
                .orElseThrow(AutorizacionDenegadaException::new);
        if (solicitante.usuarioId() == null || !solicitante.usuarioId().equals(nota.getAutorUsuarioId())) {
            throw new AutorizacionDenegadaException();
        }
        nota.setTexto(texto);
        return vista(notas.save(nota));
    }

    private FichaClinica obtenerOCrear(UUID pacienteId) {
        return fichas.findById(pacienteId).orElseGet(() -> {
            fichas.crearSiNoExiste(pacienteId);
            return fichas.findById(pacienteId)
                    .orElseThrow(() -> new IllegalStateException("No se pudo crear la ficha clínica"));
        });
    }

    private void exigirPaciente(UUID pacienteId) {
        if (pacienteId == null || pacientes.findById(pacienteId).isEmpty()) {
            throw new IllegalArgumentException("El paciente no existe");
        }
    }

    private void exigirAlcance(Identidad solicitante, UUID pacienteId) {
        if (solicitante.rol() == Rol.ODONTOLOGO
                && (solicitante.profesionalRef() == null
                    || !asignaciones.tieneAsignacionActiva(pacienteId, solicitante.profesionalRef()))) {
            throw new AutorizacionDenegadaException();
        }
    }

    private FichaClinicaVista vista(FichaClinica ficha) {
        List<NotaClinicaVista> notasClinicas = notas.findAllByPacienteIdOrderByCreadaEnAscIdAsc(ficha.getPacienteId())
                .stream().map(ServicioFichaClinica::vista).toList();
        return new FichaClinicaVista(ficha.getPacienteId(), ficha.getAntecedentes(), notasClinicas);
    }

    private static NotaClinicaVista vista(NotaClinica nota) {
        return new NotaClinicaVista(nota.getId(), nota.getAutorUsuarioId(), nota.getCreadaEn(), nota.getTexto());
    }

    private static void validarNota(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El contenido de la nota es obligatorio");
        }
    }

    public record CrearNotaClinica(String texto) {}
    public record EditarNotaClinica(String texto) {}
    public record FichaClinicaVista(UUID pacienteId, String antecedentes, List<NotaClinicaVista> notas) {}
    public record NotaClinicaVista(UUID id, UUID autorId, java.time.Instant creadaEn, String texto) {}
}
