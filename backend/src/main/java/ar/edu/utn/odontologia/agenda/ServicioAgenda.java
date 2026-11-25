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
import ar.edu.utn.odontologia.agenda.persistencia.EdicionesTurnoPersistidas;
import ar.edu.utn.odontologia.agenda.persistencia.TurnoLeido;
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
import java.util.Optional;
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
    private final EdicionesTurnoPersistidas ediciones;
    private final Agenda agenda;
    private final Clock reloj;
    private final ServicioCancelacionPorEnlace tokensCancelacion;

    public ServicioAgenda(ServicioAutorizacion autorizacion, PacienteReferencias pacientes,
                          PacienteProfesionales asignaciones, PrestacionReferencias prestaciones,
                          ServicioRecursosAgenda recursos, LocksRecursosAgenda locks, ConsultaBloqueos bloqueos,
                          TurnosPersistidos turnos, EdicionesTurnoPersistidas ediciones,
                          Agenda agenda, Clock reloj, ServicioCancelacionPorEnlace tokensCancelacion) {
        this.autorizacion = autorizacion;
        this.pacientes = pacientes;
        this.asignaciones = asignaciones;
        this.prestaciones = prestaciones;
        this.recursos = recursos;
        this.locks = locks;
        this.bloqueos = bloqueos;
        this.turnos = turnos;
        this.ediciones = ediciones;
        this.agenda = agenda;
        this.reloj = reloj;
        this.tokensCancelacion = tokensCancelacion;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado crear(Identidad solicitante, SolicitudAlta solicitud) {
        autorizacion.exigir(solicitante, Permiso.TURNO_CREAR);
        validarSolicitud(solicitud);
        exigirAlcanceOdontologia(solicitante, solicitud.pacienteId(), solicitud.profesionalId());
        if (solicitante.rol() == Rol.RECEPCION && pacientes.findById(solicitud.pacienteId()).isEmpty()) {
            throw new IllegalArgumentException("El paciente no existe");
        }

        var seleccion = new RecursosBloqueo(solicitud.profesionalId(), solicitud.sillonId());
        locks.adquirir(List.of(seleccion));
        // La asignación puede haber cambiado mientras el alta esperaba los recursos.
        exigirAlcanceOdontologia(solicitante, solicitud.pacienteId(), solicitud.profesionalId());
        var inicio = normalizarInicio(solicitud.inicio());
        if (!inicio.isAfter(reloj.instant())) {
            throw new IllegalArgumentException("El inicio del turno debe ser futuro");
        }
        var intervalo = intervaloVigente(solicitante, seleccion);
        var referencia = duracionPrestacion(solicitud.prestacionId());
        var ajuste = solicitud.duracionAjustadaMinutos() == null
                ? null : Duration.ofMinutes(solicitud.duracionAjustadaMinutos());
        var dominio = solicitudDominio(solicitud.pacienteId(), solicitud.prestacionId(), seleccion,
                inicio, referencia, ajuste);
        var turno = validarDestino(seleccion, dominio, intervalo, null);
        var persistido = turnos.save(new TurnoPersistido(turno));
        tokensCancelacion.emitir(persistido.id(), persistido.datos().version(), persistido.inicio(), reloj.instant());
        return vista(persistido.datos());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado reprogramar(Identidad solicitante, SolicitudReprogramacion solicitud) {
        return reprogramar(solicitante, solicitud, MAXIMO_PERSISTIBLE);
    }

    /** Permite al llamador acotar el fin de un movimiento efectivo sin cambiar el rango del servicio. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado reprogramar(Identidad solicitante, SolicitudReprogramacion solicitud, Instant finMaximo) {
        autorizacion.exigir(solicitante, Permiso.TURNO_REPROGRAMAR);
        validarReprogramacion(solicitud);
        if (solicitante.rol() == Rol.ODONTOLOGO && !solicitud.profesionalId().equals(solicitante.profesionalRef())) {
            throw new AutorizacionDenegadaException();
        }

        var actual = bloquearTurno(solicitante, solicitud.turnoId());
        if (!versionOpaca(actual.version()).equals(solicitud.versionEsperada())) {
            throw new VersionTurnoDesactualizadaException();
        }

        var origen = new RecursosBloqueo(actual.profesionalId(), actual.sillonId());
        var destino = new RecursosBloqueo(solicitud.profesionalId(), solicitud.sillonId());
        locks.adquirir(List.of(origen, destino));
        exigirAlcanceOdontologia(solicitante, actual.pacienteId(), actual.profesionalId());
        var inicio = normalizarInicio(solicitud.inicio());
        var ahora = reloj.instant();
        if (!estadoEditable(solicitante, actual.estado()) || !actual.inicio().isAfter(ahora)) {
            throw new IllegalArgumentException("Solo se pueden reprogramar turnos elegibles que aún no comenzaron");
        }
        if (!inicio.isAfter(ahora)) {
            throw new IllegalArgumentException("El inicio de destino debe ser futuro");
        }
        if (origen.equals(destino) && actual.inicio().equals(inicio)) {
            return vista(actual);
        }

        var intervalo = intervaloVigente(solicitante, destino);
        var dominio = solicitudDominio(actual.pacienteId(), actual.prestacionId(), destino,
                inicio, Duration.ofMinutes(actual.duracionMinutos()), null);
        var movimiento = validarDestino(destino, dominio, intervalo, actual.id());
        if (movimiento.fin().isAfter(finMaximo)) {
            throw new IllegalArgumentException("La duración excede el rango de fechas admitido");
        }
        var movido = ediciones.mover(actual.id(), actual.version(), movimiento)
                .orElseThrow(VersionTurnoDesactualizadaException::new);
        // La reprogramacion invalida el token previo y emite uno nuevo solo si el nuevo inicio cumple la ventana.
        tokensCancelacion.emitir(movido.id(), movido.version(), movido.inicio(), ahora);
        return vista(movido);
    }

    /**
     * Cancela el turno y devuelve sus datos actualizados. Si el llamador abre una transacción exterior,
     * debe usar READ COMMITTED, entrar sin locks previos de agenda y respetar el orden fila → recursos
     * durante la transacción. El horario se libera al confirmar esa transacción.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado cancelar(Identidad solicitante, SolicitudCancelacion solicitud) {
        autorizacion.exigir(solicitante, Permiso.TURNO_CANCELAR);
        if (solicitud == null || solicitud.turnoId() == null || solicitud.versionEsperada() == null
                || solicitud.versionEsperada().isBlank()) {
            throw new IllegalArgumentException("El turno y su versión son obligatorios");
        }

        var actual = bloquearTurno(solicitante, solicitud.turnoId());
        if (!versionOpaca(actual.version()).equals(solicitud.versionEsperada())) {
            throw new VersionTurnoDesactualizadaException("cancelarlo");
        }

        locks.adquirir(List.of(new RecursosBloqueo(actual.profesionalId(), actual.sillonId())));
        exigirAlcanceOdontologia(solicitante, actual.pacienteId(), actual.profesionalId());
        if (actual.estado() == EstadoTurno.CANCELADO) {
            throw new IllegalArgumentException("El turno ya está cancelado");
        }
        if (!estadoEditable(solicitante, actual.estado()) || !actual.inicio().isAfter(reloj.instant())) {
            throw new IllegalArgumentException("Solo se pueden cancelar turnos elegibles que aún no comenzaron");
        }

        return vista(ediciones.cambiarEstado(actual.id(), actual.version(), actual.estado(), EstadoTurno.CANCELADO)
                .orElseThrow(() -> new VersionTurnoDesactualizadaException("cancelarlo")));
    }

    /**
     * Cancela un turno ya autorizado por un token de enlace del paciente. La autorizacion proviene unicamente
     * del token; no se requiere ni se acepta identidad interna, cookie de sesion ni identificador de turno
     * como credencial. Delegara en la misma logica transaccional de cancelacion interna y consume el token
     * en la misma transaccion.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado cancelarPorToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenCancelacionInvalidoException();
        }
        return tokensCancelacion.cancelar(token);
    }

    @Transactional(readOnly = true)
    public List<TurnoRegistrado> consultarPendientes(Identidad solicitante) {
        autorizacion.exigir(solicitante, Permiso.PENDIENTES_CONSULTAR);
        // Los inicios persistidos tienen precisión de microsegundos.
        var ahora = reloj.instant().truncatedTo(ChronoUnit.MICROS);
        if (ahora.isAfter(MAXIMO_PERSISTIBLE)) {
            return List.of();
        }
        return turnos.consultarPendientes(EstadoTurno.PENDIENTE, ahora)
                .stream().map(ServicioAgenda::vista).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TurnoRegistrado confirmar(Identidad solicitante, UUID turnoId, String versionEsperada) {
        autorizacion.exigir(solicitante, Permiso.CONFIRMACION_REGISTRAR);
        if (turnoId == null || versionEsperada == null || versionEsperada.isBlank()) {
            throw new IllegalArgumentException("El turno y su versión son obligatorios");
        }

        var actual = bloquearTurno(solicitante, turnoId);
        if (!versionOpaca(actual.version()).equals(versionEsperada)) {
            throw new VersionTurnoDesactualizadaException("confirmarlo");
        }
        switch (actual.estado()) {
            case CONFIRMADO -> throw new IllegalArgumentException("El turno ya está confirmado");
            case CANCELADO -> throw new IllegalArgumentException("El turno está cancelado");
            case PENDIENTE -> {
                if (!actual.inicio().isAfter(reloj.instant())) {
                    throw new IllegalArgumentException("El turno ya alcanzó su inicio");
                }
            }
        }
        var confirmado = ediciones.cambiarEstado(actual.id(), actual.version(), EstadoTurno.PENDIENTE,
                EstadoTurno.CONFIRMADO).orElseThrow(() -> new VersionTurnoDesactualizadaException("confirmarlo"));
        tokensCancelacion.conservarTrasConfirmacion(confirmado.id(), actual.version(), confirmado.version());
        return vista(confirmado);
    }

    private static boolean estadoEditable(Identidad solicitante, EstadoTurno estado) {
        return estado == EstadoTurno.PENDIENTE
                || (solicitante.rol() == Rol.RECEPCION && estado == EstadoTurno.CONFIRMADO);
    }

    private TurnoLeido bloquearTurno(Identidad solicitante, UUID turnoId) {
        boolean odontologo = solicitante.rol() == Rol.ODONTOLOGO;
        UUID profesionalPropio = odontologo ? solicitante.profesionalRef() : null;
        if (odontologo && profesionalPropio == null) {
            throw new AutorizacionDenegadaException();
        }

        var actual = ediciones.bloquear(turnoId, profesionalPropio)
                .orElseThrow(() -> odontologo ? new AutorizacionDenegadaException()
                        : new IllegalArgumentException("El turno no existe"));
        // La asignación puede haber cambiado mientras la solicitud esperaba la fila del turno.
        exigirAlcanceOdontologia(solicitante, actual.pacienteId(), actual.profesionalId());
        return actual;
    }

    private Agenda.Turno validarDestino(RecursosBloqueo seleccion, Agenda.Solicitud dominio,
                                       Duration intervalo, UUID turnoExcluido) {
        var duracion = dominio.duracionAjustada() == null ? dominio.prestacion().duracion() : dominio.duracionAjustada();
        var fin = dominio.inicio().plus(duracion);
        validarRango(fin);
        var desdeOcupacion = dominio.inicio().minus(intervalo);
        var hastaOcupacion = fin.plus(intervalo);
        var ocupantes = turnos.buscarOcupantes(seleccion.profesionalId(), seleccion.sillonId(),
                desdeOcupacion.isBefore(MINIMO_PERSISTIBLE) ? MINIMO_PERSISTIBLE : desdeOcupacion,
                hastaOcupacion.isAfter(MAXIMO_PERSISTIBLE) ? MAXIMO_PERSISTIBLE : hastaOcupacion,
                EstadoTurno.OCUPANTES, turnoExcluido).stream().map(TurnoLeido::reserva).toList();
        var turno = switch (agenda.crear(dominio, ocupantes, intervalo)) {
            case Agenda.Exito exito -> exito.turno();
            case Agenda.NoDisponible conflicto -> throw new ConflictoAgendaException();
            case Agenda.EntradaInvalida invalida -> throw new IllegalArgumentException(invalida.motivo());
        };
        if (!bloqueos.buscar(seleccion, new PeriodoBloqueo(turno.inicio(), turno.fin())).isEmpty()) {
            throw new ConflictoAgendaException();
        }
        return turno;
    }

    private static Agenda.Solicitud solicitudDominio(UUID paciente, UUID prestacion, RecursosBloqueo seleccion,
                                                     Instant inicio, Duration referencia, Duration ajuste) {
        return new Agenda.Solicitud(paciente.toString(), seleccion.profesionalId().toString(),
                seleccion.sillonId().toString(), new Agenda.Prestacion(prestacion.toString(), referencia), inicio, ajuste);
    }

    private Duration intervaloVigente(Identidad solicitante, RecursosBloqueo seleccion) {
        return Duration.ofMinutes(recursos.validarDisponibilidadMinima(
                solicitante, seleccion.profesionalId(), seleccion.sillonId()));
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

    private static void validarReprogramacion(SolicitudReprogramacion solicitud) {
        if (solicitud == null || solicitud.turnoId() == null || solicitud.versionEsperada() == null
                || solicitud.versionEsperada().isBlank() || solicitud.inicio() == null
                || solicitud.profesionalId() == null || solicitud.sillonId() == null) {
            throw new IllegalArgumentException("Turno, versión, inicio, profesional y sillón de destino son obligatorios");
        }
    }

    private void exigirAlcanceOdontologia(Identidad solicitante, UUID paciente, UUID profesional) {
        if (solicitante.rol() == Rol.ODONTOLOGO
                && (!profesional.equals(solicitante.profesionalRef())
                || !asignaciones.tieneAsignacionActiva(paciente, profesional))) {
            throw new AutorizacionDenegadaException();
        }
    }

    private Duration duracionPrestacion(UUID prestacion) {
        var existente = prestaciones.findById(prestacion)
                .orElseThrow(() -> new IllegalArgumentException("La prestación no existe"));
        return Optional.ofNullable(existente.getDuracionReferenciaMinutos())
                .filter(minutos -> minutos > 0)
                .map(Duration::ofMinutes)
                .orElseThrow(() -> new IllegalArgumentException("La prestación debe tener una duración configurada"));
    }

    private static Instant normalizarInicio(Instant inicio) {
        var normalizado = inicio.truncatedTo(ChronoUnit.MICROS);
        validarRango(normalizado);
        return normalizado;
    }

    private static void validarRango(Instant instante) {
        if (instante.isBefore(MINIMO_PERSISTIBLE) || instante.isAfter(MAXIMO_PERSISTIBLE)) {
            throw new IllegalArgumentException("El período excede el rango temporal de la agenda");
        }
    }

    private static TurnoRegistrado vista(TurnoLeido turno) {
        return new TurnoRegistrado(turno.id(), turno.pacienteId(), turno.profesionalId(), turno.sillonId(),
                turno.prestacionId(), turno.inicio(), turno.fin(), turno.duracionMinutos(), turno.estado(),
                versionOpaca(turno.version()));
    }

    private static String versionOpaca(long version) {
        return Long.toString(version);
    }

    public record SolicitudAlta(UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                                Instant inicio, Integer duracionAjustadaMinutos) {}

    public record SolicitudReprogramacion(UUID turnoId, String versionEsperada, Instant inicio,
                                          UUID profesionalId, UUID sillonId) {}

    public record SolicitudCancelacion(UUID turnoId, String versionEsperada) {}

    public record TurnoRegistrado(UUID id, UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                                 Instant inicio, Instant fin, int duracionMinutos, EstadoTurno estado, String version) {
        public TurnoRegistrado(UUID id, UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                               Instant inicio, Instant fin, int duracionMinutos, EstadoTurno estado) {
            this(id, pacienteId, profesionalId, sillonId, prestacionId, inicio, fin, duracionMinutos, estado, "0");
        }
    }
}
