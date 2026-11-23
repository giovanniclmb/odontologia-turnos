package ar.edu.utn.odontologia.agenda;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.agenda.bloqueos.ConsultaBloqueos;
import ar.edu.utn.odontologia.agenda.bloqueos.LocksRecursosAgenda;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import ar.edu.utn.odontologia.agenda.persistencia.TurnoPersistido;
import ar.edu.utn.odontologia.agenda.persistencia.TurnosPersistidos;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.PrestacionReferencias;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioAgenda {
    // Límites finitos de timestamptz, expresados en el calendario ISO de Java.
    private static final Instant MINIMO_PERSISTIBLE = Instant.parse("-4713-11-24T00:00:00Z");
    private static final Instant MAXIMO_PERSISTIBLE = Instant.parse("+294276-12-31T23:59:59.999999Z");

    private final ServicioAutorizacion autorizacion;
    private final PacienteReferencias pacientes;
    private final PacienteProfesionales asignaciones;
    private final PrestacionReferencias prestaciones;
    private final ServicioRecursosAgenda recursos;
    private final LocksRecursosAgenda locks;
    private final ConsultaBloqueos bloqueos;
    private final TurnosPersistidos turnos;
    private final Agenda agenda;
    private final Clock reloj;

    public ServicioAgenda(ServicioAutorizacion autorizacion, PacienteReferencias pacientes,
                          PacienteProfesionales asignaciones, PrestacionReferencias prestaciones,
                          ServicioRecursosAgenda recursos, LocksRecursosAgenda locks, ConsultaBloqueos bloqueos,
                          TurnosPersistidos turnos, Agenda agenda, Clock reloj) {
        this.autorizacion = autorizacion;
        this.pacientes = pacientes;
        this.asignaciones = asignaciones;
        this.prestaciones = prestaciones;
        this.recursos = recursos;
        this.locks = locks;
        this.bloqueos = bloqueos;
        this.turnos = turnos;
        this.agenda = agenda;
        this.reloj = reloj;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado crear(Identidad solicitante, SolicitudAlta solicitud) {
        autorizacion.exigir(solicitante, Permiso.TURNO_CREAR);
        validarSolicitud(solicitud);
        exigirAlcanceOdontologia(solicitante, solicitud);
        if (solicitante.rol() == Rol.RECEPCION && pacientes.findById(solicitud.pacienteId()).isEmpty()) {
            throw new IllegalArgumentException("El paciente no existe");
        }

        var seleccion = new RecursosBloqueo(solicitud.profesionalId(), solicitud.sillonId());
        locks.adquirir(List.of(seleccion));
        // La asignación puede haber cambiado mientras el alta esperaba los recursos.
        exigirAlcanceOdontologia(solicitante, solicitud);
        var inicio = solicitud.inicio().truncatedTo(ChronoUnit.MICROS);
        validarRango(inicio);
        if (!inicio.isAfter(reloj.instant())) {
            throw new IllegalArgumentException("El inicio del turno debe ser futuro");
        }
        var intervalo = Duration.ofMinutes(recursos.validarDisponibilidadMinima(
                solicitante, solicitud.profesionalId(), solicitud.sillonId()));
        var referencia = duracionPrestacion(solicitud.prestacionId());
        var ajuste = solicitud.duracionAjustadaMinutos() == null
                ? null : Duration.ofMinutes(solicitud.duracionAjustadaMinutos());
        var fin = inicio.plus(ajuste == null ? referencia : ajuste);
        validarRango(fin);

        var dominio = new Agenda.Solicitud(solicitud.pacienteId().toString(), solicitud.profesionalId().toString(),
                solicitud.sillonId().toString(), new Agenda.Prestacion(solicitud.prestacionId().toString(), referencia),
                inicio, ajuste);
        var desdeOcupacion = inicio.minus(intervalo);
        var hastaOcupacion = fin.plus(intervalo);
        var ocupantes = turnos.buscarOcupantes(seleccion.profesionalId(), seleccion.sillonId(),
                desdeOcupacion.isBefore(MINIMO_PERSISTIBLE) ? MINIMO_PERSISTIBLE : desdeOcupacion,
                hastaOcupacion.isAfter(MAXIMO_PERSISTIBLE) ? MAXIMO_PERSISTIBLE : hastaOcupacion,
                EstadoTurno.PENDIENTE).stream().map(TurnoPersistido::reserva).toList();
        var turno = switch (agenda.crear(dominio, ocupantes, intervalo)) {
            case Agenda.Exito exito -> exito.turno();
            case Agenda.NoDisponible conflicto -> throw new ConflictoAgendaException();
            case Agenda.EntradaInvalida invalida -> throw new IllegalArgumentException(invalida.motivo());
        };
        if (!bloqueos.buscar(seleccion, new PeriodoBloqueo(turno.inicio(), turno.fin())).isEmpty()) {
            throw new ConflictoAgendaException();
        }
        return vista(turnos.save(new TurnoPersistido(turno)));
    }

    @Transactional(readOnly = true)
    public List<TurnoRegistrado> consultarAgendaSillon(Identidad solicitante, UUID sillonId, Instant inicio, Instant fin) {
        autorizacion.exigir(solicitante, Permiso.AGENDA_SILLON_CONSULTAR);
        if (sillonId == null) {
            throw new IllegalArgumentException("La referencia del sillón es obligatoria");
        }
        var periodo = new PeriodoBloqueo(inicio, fin);
        validarRango(periodo.inicio());
        validarRango(periodo.fin());
        UUID profesional = solicitante.rol() == Rol.ODONTOLOGO ? solicitante.profesionalRef() : null;
        if (solicitante.rol() == Rol.ODONTOLOGO && profesional == null) {
            return List.of();
        }
        return turnos.consultarAgendaSillon(sillonId, profesional, periodo.inicio(), periodo.fin())
                .stream().map(ServicioAgenda::vista).toList();
    }

    private static void validarSolicitud(SolicitudAlta solicitud) {
        if (solicitud == null || solicitud.pacienteId() == null || solicitud.profesionalId() == null
                || solicitud.sillonId() == null || solicitud.prestacionId() == null || solicitud.inicio() == null) {
            throw new IllegalArgumentException("Paciente, profesional, sillón, prestación e inicio son obligatorios");
        }
        if (solicitud.duracionAjustadaMinutos() != null && solicitud.duracionAjustadaMinutos() <= 0) {
            throw new IllegalArgumentException("La duración ajustada debe ser positiva");
        }
    }

    private void exigirAlcanceOdontologia(Identidad solicitante, SolicitudAlta solicitud) {
        if (solicitante.rol() == Rol.ODONTOLOGO
                && (!solicitud.profesionalId().equals(solicitante.profesionalRef())
                || !asignaciones.tieneAsignacionActiva(solicitud.pacienteId(), solicitud.profesionalId()))) {
            throw new AutorizacionDenegadaException();
        }
    }

    private Duration duracionPrestacion(UUID prestacion) {
        return prestaciones.findById(prestacion)
                .map(referencia -> referencia.getDuracionReferenciaMinutos())
                .filter(minutos -> minutos > 0)
                .map(Duration::ofMinutes)
                .orElseThrow(() -> new IllegalArgumentException("La prestación debe existir y tener una duración configurada"));
    }

    private static void validarRango(Instant instante) {
        if (instante.isBefore(MINIMO_PERSISTIBLE) || instante.isAfter(MAXIMO_PERSISTIBLE)) {
            throw new IllegalArgumentException("El período excede el rango temporal de la agenda");
        }
    }

    private static TurnoRegistrado vista(TurnoPersistido turno) {
        return new TurnoRegistrado(turno.id(), turno.pacienteId(), turno.profesionalId(), turno.sillonId(),
                turno.prestacionId(), turno.inicio(), turno.fin(), turno.duracionMinutos(), turno.estado());
    }

    public record SolicitudAlta(UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                                Instant inicio, Integer duracionAjustadaMinutos) {}

    public record TurnoRegistrado(UUID id, UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                                 Instant inicio, Instant fin, int duracionMinutos, EstadoTurno estado) {}
}
