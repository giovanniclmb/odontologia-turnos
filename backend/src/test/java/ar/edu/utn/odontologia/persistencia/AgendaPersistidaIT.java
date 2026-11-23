package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ConflictoAgendaException;
import ar.edu.utn.odontologia.agenda.EstadoTurno;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.bloqueos.ConsultaTurnosOcupantes;
import ar.edu.utn.odontologia.agenda.bloqueos.LocksRecursosAgenda;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.ServicioBloqueos;
import ar.edu.utn.odontologia.agenda.bloqueos.TurnoOcupanteException;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

class AgendaPersistidaIT {
    private static final Instant AHORA = Instant.parse("2032-03-03T03:00:00.123456500Z");
    private static final Instant INICIO = Instant.parse("2032-03-07T03:00:00Z");
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION, null);
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ServicioBloqueos bloqueos;
    private static ServicioPacientes pacientes;
    private static ServicioRecursosAgenda recursos;
    private static ServicioConfiguracionSede sede;
    private static ServicioAsignacionPacientes asignaciones;
    private static TransactionTemplate transacciones;
    private static JdbcTemplate jdbc;
    private Datos datos;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojAgendaPruebas", Clock.class,
                    () -> Clock.fixed(AHORA, ZoneOffset.UTC), bean -> bean.setPrimary(true));
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        bloqueos = contexto.getBean(ServicioBloqueos.class);
        pacientes = contexto.getBean(ServicioPacientes.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
        sede = contexto.getBean(ServicioConfiguracionSede.class);
        asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        transacciones = contexto.getBean(TransactionTemplate.class);
        jdbc = contexto.getBean(JdbcTemplate.class);
    }

    @AfterAll
    static void cerrar() {
        try {
            if (contexto != null) contexto.close();
        } finally {
            if (db != null) db.close();
        }
    }

    @BeforeEach
    void prepararTurno() {
        sede.establecerIntervalo(RECEPCION, 10);
        datos = nuevosDatos();
    }

    @Test
    void recepcionCreaConDuracionDeReferenciaYElCatalogoNoCambiaLoPersistido() {
        var solicitud = datos.solicitud(INICIO.plusNanos(123_456_999), null);
        var turno = agenda.crear(RECEPCION, solicitud);
        assertNotNull(turno.id());
        assertEquals(datos.paciente(), turno.pacienteId());
        assertEquals(datos.profesional(), turno.profesionalId());
        assertEquals(datos.sillon(), turno.sillonId());
        assertEquals(datos.prestacion(), turno.prestacionId());
        assertEquals(INICIO.plusNanos(123_456_000), turno.inicio());
        assertEquals(turno.inicio().plusSeconds(30 * 60), turno.fin());
        assertEquals(30, turno.duracionMinutos());
        assertEquals(EstadoTurno.PENDIENTE, turno.estado());
        recursos.ajustarDuracion(RECEPCION, datos.prestacion(), 45);
        assertEquals(List.of(turno), agenda.consultarAgendaSillon(RECEPCION, datos.sillon(), INICIO, INICIO.plusSeconds(3600)));
        assertEquals(1, cantidadTurnos(datos.sillon()));
    }

    @Test
    void validaElAlcanceDeAltaSinRevelarRegistrosAjenos() {
        var otros = nuevosDatos();
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        var solicitud = datos.solicitud(INICIO, 20);
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.crear(null, solicitud));
        assertThrows(AutorizacionDenegadaException.class,
                () -> agenda.crear(identidad(Rol.ODONTOLOGO, null), solicitud));
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.crear(odontologo, solicitud));
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.crear(odontologo,
                new SolicitudAlta(datos.paciente(), otros.profesional(), datos.sillon(), datos.prestacion(), INICIO, null)));
        var intentoAjeno = new SolicitudAlta(otros.paciente(), datos.profesional(), datos.sillon(), datos.prestacion(), INICIO, null);
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.crear(odontologo, intentoAjeno));
        assertEquals(0, cantidadTurnos(datos.sillon()));
        var turno = agenda.crear(odontologo, solicitud);
        assertEquals(20, turno.duracionMinutos());
        assertEquals(INICIO.plusSeconds(1200), turno.fin());
        asignaciones.desasignar(RECEPCION, datos.paciente(), datos.profesional());
        assertThrows(AutorizacionDenegadaException.class,
                () -> agenda.crear(odontologo, datos.solicitud(INICIO.plusSeconds(7200), 20)));
    }

    @Test
    void deniegaAltaSiRecepcionQuitaLaAsignacionMientrasEsperaLosLocks() throws Exception {
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        var seleccion = new RecursosBloqueo(datos.profesional(), datos.sillon());
        var locks = contexto.getBean(LocksRecursosAgenda.class);
        var revocacion = new TransactionTemplate(transacciones.getTransactionManager());
        revocacion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        var segunda = competir(() -> {
            locks.adquirir(List.of(seleccion));
            return seleccion;
        }, () -> agenda.crear(odontologo, datos.solicitud(INICIO, null)), false,
                () -> revocacion.executeWithoutResult(estado ->
                        asignaciones.desasignar(RECEPCION, datos.paciente(), datos.profesional())));

        assertInstanceOf(AutorizacionDenegadaException.class, segunda.error());
        assertEquals(0, cantidadTurnos(datos.sillon()));
    }

    @Test
    void rechazaReferenciasInvalidasRecursosInactivosIntervaloAusenteYFechasNoFuturas() throws Exception {
        var solicitud = datos.solicitud(INICIO, null);
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION, null));
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION,
                new SolicitudAlta(null, datos.profesional(), datos.sillon(), datos.prestacion(), INICIO, null)));
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION,
                new SolicitudAlta(UUID.randomUUID(), datos.profesional(), datos.sillon(), datos.prestacion(), INICIO, null)));
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION,
                new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(), UUID.randomUUID(), INICIO, null)));
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION,
                new SolicitudAlta(datos.paciente(), UUID.randomUUID(), datos.sillon(), datos.prestacion(), INICIO, null)));
        var profesionalInactivo = recursos.altaProfesional(RECEPCION, nuevoCodigo(), false).id();
        var sillonInactivo = recursos.altaSillon(RECEPCION, nuevoCodigo(), false).id();
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION,
                new SolicitudAlta(datos.paciente(), profesionalInactivo, datos.sillon(), datos.prestacion(), INICIO, null)));
        assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION,
                new SolicitudAlta(datos.paciente(), datos.profesional(), sillonInactivo, datos.prestacion(), INICIO, null)));
        assertThrows(IllegalArgumentException.class,
                () -> agenda.crear(RECEPCION, datos.solicitud(INICIO, 0)));
        for (var instante : List.of(AHORA.minusSeconds(1), AHORA, AHORA.plusNanos(1), Instant.MAX)) {
            assertThrows(IllegalArgumentException.class, () -> agenda.crear(RECEPCION, datos.solicitud(instante, null)));
        }
        try (var con = db.appConnection(); var sql = con.createStatement()) {
            sql.executeUpdate("update odontologia.configuracion_sede set intervalo_minutos = null");
        }
        assertThrows(IllegalStateException.class, () -> agenda.crear(RECEPCION, solicitud));
        assertEquals(0, cantidadTurnos(datos.sillon()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void protegeCadaRecursoYAdmiteMargenExactoAntesYDespues(boolean mismoProfesional) {
        var otros = nuevosDatos();
        var inicialmente = agenda.crear(RECEPCION, datos.solicitud(INICIO, 20));
        var segundo = comparteRecurso(otros, mismoProfesional);
        var bordeAnterior = INICIO.minusSeconds(30 * 60);
        var bordePosterior = inicialmente.fin().plusSeconds(10 * 60);
        assertThrows(ConflictoAgendaException.class,
                () -> agenda.crear(RECEPCION, segundo.solicitud(bordeAnterior.plusNanos(1000), 20)));
        assertThrows(ConflictoAgendaException.class,
                () -> agenda.crear(RECEPCION, segundo.solicitud(bordePosterior.minusNanos(1000), 20)));
        assertEquals(bordeAnterior, agenda.crear(RECEPCION, segundo.solicitud(bordeAnterior, 20)).inicio());
        assertEquals(bordePosterior, agenda.crear(RECEPCION, segundo.solicitud(bordePosterior, 20)).inicio());
    }

    @Test
    void consultaPorInterseccionOrdenYAlcanceSinOcultarOcupacionAlAlta() {
        var otros = nuevosDatos();
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        var tarde = agenda.crear(odontologo, datos.solicitud(INICIO.plusSeconds(7200), 20));
        var ajeno = agenda.crear(RECEPCION, comparteRecurso(otros, false).solicitud(INICIO, 20));
        var inicioConsulta = INICIO.plusSeconds(1);
        assertEquals(List.of(ajeno, tarde), agenda.consultarAgendaSillon(RECEPCION, datos.sillon(), inicioConsulta, tarde.fin()));
        assertEquals(List.of(tarde), agenda.consultarAgendaSillon(odontologo, datos.sillon(), inicioConsulta, tarde.fin()));
        assertEquals(List.of(), agenda.consultarAgendaSillon(RECEPCION, datos.sillon(), ajeno.fin(), tarde.inicio()));
        assertThrows(AutorizacionDenegadaException.class,
                () -> agenda.consultarAgendaSillon(null, datos.sillon(), INICIO, tarde.fin()));
        assertThrows(IllegalArgumentException.class, () -> agenda.consultarAgendaSillon(RECEPCION,
                datos.sillon(), INICIO, INICIO.plusNanos(999)));
        var conflicto = assertThrows(ConflictoAgendaException.class,
                () -> agenda.crear(odontologo, datos.solicitud(INICIO, 20)));
        assertEquals("El horario solicitado no está disponible", conflicto.getMessage());
        assertEquals(2, cantidadTurnos(datos.sillon()));
    }

    @ParameterizedTest
    @EnumSource(RecursoCompartido.class)
    void excluyeBloqueosDeCadaRecursoYAdmiteContiguidad(RecursoCompartido recurso) {
        assertEquals(1, contexto.getBeansOfType(ConsultaTurnosOcupantes.class).size());
        var seleccion = recurso.seleccionar(datos);
        var fin = INICIO.plusSeconds(1800);
        bloqueos.crear(RECEPCION, seleccion, new PeriodoBloqueo(INICIO, fin));
        assertThrows(ConflictoAgendaException.class,
                () -> agenda.crear(RECEPCION, datos.solicitud(fin.minusNanos(1000), 30)));
        assertThrows(ConflictoAgendaException.class,
                () -> agenda.crear(RECEPCION, datos.solicitud(INICIO.minusSeconds(1800).plusNanos(1000), 30)));
        var antes = agenda.crear(RECEPCION, datos.solicitud(INICIO.minusSeconds(1800), 30));
        var despues = agenda.crear(RECEPCION, datos.solicitud(fin, 30));
        assertEquals(INICIO, antes.fin());
        assertEquals(fin, despues.inicio());
        var bloqueoContiguo = bloqueos.crear(RECEPCION, seleccion, new PeriodoBloqueo(antes.fin(), despues.inicio()));
        assertNotNull(bloqueoContiguo.id());
        assertEquals(new PeriodoBloqueo(INICIO, fin), bloqueoContiguo.periodo());
        assertThrows(TurnoOcupanteException.class, () -> bloqueos.crear(RECEPCION,
                seleccion, new PeriodoBloqueo(despues.inicio().minusNanos(1000), despues.inicio().plusNanos(1000))));
        assertThrows(TurnoOcupanteException.class, () -> bloqueos.crear(RECEPCION,
                seleccion, new PeriodoBloqueo(antes.fin().minusNanos(1000), antes.fin().plusNanos(1000))));
        assertEquals(2, cantidadTurnos(datos.sillon()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void serializaDosAltasPorProfesionalOSillon(boolean mismoProfesional) throws Exception {
        var otros = comparteRecurso(nuevosDatos(), mismoProfesional);
        var segunda = competir(() -> agenda.crear(RECEPCION, datos.solicitud(INICIO, null)),
                () -> agenda.crear(RECEPCION, otros.solicitud(INICIO, null)), false);
        assertInstanceOf(ConflictoAgendaException.class, segunda.error());
        assertEquals(1, cantidadTurnos(datos.sillon()));
        assertEquals(mismoProfesional ? 0 : 1, cantidadTurnos(otros.sillon()));
    }

    @Test
    void dosAltasSinRecursosCompartidosPuedenCompletarEnParalelo() throws Exception {
        var otros = nuevosDatos();
        try (var executor = Executors.newSingleThreadExecutor()) {
            transacciones.executeWithoutResult(estado -> {
                agenda.crear(RECEPCION, datos.solicitud(INICIO, null));
                var segunda = executor.submit(() -> agenda.crear(RECEPCION, otros.solicitud(INICIO, null)));
                assertDoesNotThrow(() -> segunda.get(5, TimeUnit.SECONDS));
            });
        }
        assertEquals(1, cantidadTurnos(datos.sillon()));
        assertEquals(1, cantidadTurnos(otros.sillon()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void serializaLaCarreraTurnoBloqueoConAmbosOrdenesDeConfirmacion(boolean turnoPrimero) throws Exception {
        verificarCarreraTurnoBloqueo(turnoPrimero, false);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void revalidaLaCarreraCuandoLaPrimeraOperacionRevierte(boolean turnoPrimero) throws Exception {
        verificarCarreraTurnoBloqueo(turnoPrimero, true);
    }

    private void verificarCarreraTurnoBloqueo(boolean turnoPrimero, boolean revertirPrimera) throws Exception {
        Supplier<?> turno = () -> agenda.crear(RECEPCION, datos.solicitud(INICIO, null));
        var seleccion = new RecursosBloqueo(null, datos.sillon());
        var periodo = new PeriodoBloqueo(INICIO, INICIO.plusSeconds(60));
        Supplier<?> bloqueo = () -> bloqueos.crear(RECEPCION, seleccion, periodo);
        var segunda = competir(turnoPrimero ? turno : bloqueo, turnoPrimero ? bloqueo : turno, revertirPrimera);
        if (revertirPrimera) {
            assertNull(segunda.error());
            assertNotNull(segunda.resultado());
        } else {
            assertNull(segunda.resultado());
            assertNotNull(segunda.error());
            assertEquals(turnoPrimero ? TurnoOcupanteException.class : ConflictoAgendaException.class,
                    segunda.error().getClass());
        }
        boolean persisteTurno = turnoPrimero != revertirPrimera;
        assertEquals(persisteTurno ? 1 : 0, cantidadTurnos(datos.sillon()));
        assertEquals(persisteTurno ? 0 : 1, jdbc.queryForObject(
                "select count(*) from odontologia.bloqueo_recurso where sillon_id = ?", Integer.class, datos.sillon()));
    }

    private static ResultadoConcurrente competir(Supplier<?> primera, Supplier<?> segunda, boolean revertir) throws Exception {
        return competir(primera, segunda, revertir, () -> {});
    }

    private static ResultadoConcurrente competir(Supplier<?> primera, Supplier<?> segunda, boolean revertir,
                                                  Runnable mientrasEspera) throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var pid = new CompletableFuture<Integer>();
            Future<?> pendiente = transacciones.execute(estado -> {
                assertNotNull(primera.get());
                var tarea = executor.submit(() -> transacciones.execute(otroEstado -> {
                    pid.complete(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
                    return segunda.get();
                }));
                int pidSegundo = assertDoesNotThrow(() -> pid.get(5, TimeUnit.SECONDS));
                assertDoesNotThrow(() -> esperarBloqueo(tarea, pidSegundo));
                mientrasEspera.run();
                if (revertir) estado.setRollbackOnly();
                return tarea;
            });
            assertNotNull(pendiente);
            try {
                return new ResultadoConcurrente(pendiente.get(10, TimeUnit.SECONDS), null);
            } catch (ExecutionException e) {
                return new ResultadoConcurrente(null, e.getCause());
            }
        }
    }

    private static void esperarBloqueo(Future<?> tarea, int pidSegundo) throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!tarea.isDone() && System.nanoTime() < limite) {
            if (Boolean.TRUE.equals(jdbc.queryForObject(
                    "select cardinality(pg_blocking_pids(?)) > 0", Boolean.class, pidSegundo))) {
                return;
            }
            Thread.sleep(10);
        }
        fail("La segunda llamada productiva no esperó el lock de la primera");
    }

    private static int cantidadTurnos(UUID sillon) {
        return jdbc.queryForObject("select count(*) from odontologia.turno where sillon_id = ?", Integer.class, sillon);
    }

    private Datos comparteRecurso(Datos otros, boolean mismoProfesional) {
        return new Datos(otros.paciente(), mismoProfesional ? datos.profesional() : otros.profesional(),
                mismoProfesional ? otros.sillon() : datos.sillon(), otros.prestacion());
    }

    private static Datos nuevosDatos() {
        var paciente = pacientes.crear(RECEPCION,
                new CrearPaciente(nuevoCodigo(), "Nombre ficticio", "Apellido ficticio", "turno@example.invalid", null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, nuevoCodigo(), true).id(),
                recursos.altaSillon(RECEPCION, nuevoCodigo(), true).id(),
                recursos.altaPrestacion(RECEPCION, nuevoCodigo(), 30).id());
    }

    private static String nuevoCodigo() {
        return UUID.randomUUID().toString();
    }

    private static Identidad identidad(Rol rol, UUID profesional) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio", rol, profesional);
    }

    private record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {
        SolicitudAlta solicitud(Instant inicio, Integer minutos) {
            return new SolicitudAlta(paciente, profesional, sillon, prestacion, inicio, minutos);
        }
    }

    private record ResultadoConcurrente(Object resultado, Throwable error) {}

    private enum RecursoCompartido {
        PROFESIONAL, SILLON, AMBOS;

        RecursosBloqueo seleccionar(Datos turno) {
            return new RecursosBloqueo(this == SILLON ? null : turno.profesional(),
                    this == PROFESIONAL ? null : turno.sillon());
        }
    }
}
