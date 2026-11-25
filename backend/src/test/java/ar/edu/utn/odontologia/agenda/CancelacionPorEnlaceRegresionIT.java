package ar.edu.utn.odontologia.agenda;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudReprogramacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.ServicioBloqueos;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Regresiones de la revisión del PR #44: confirmación, orden de locks y plazo tras las esperas. */
class CancelacionPorEnlaceRegresionIT {
    private static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    private static final Instant INICIO = BASE.plus(Duration.ofDays(5));
    private static final Instant LIMITE = INICIO.minus(Duration.ofHours(48));
    private static final AtomicReference<Instant> AHORA = new AtomicReference<>(BASE);
    private static final Identidad RECEPCION =
            new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);

    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ServicioCancelacionPorEnlace cancelacion;
    private static ServicioBloqueos bloqueos;
    private static JdbcTemplate jdbc;
    private static TransactionTemplate transacciones;

    private ExecutorService hilos;
    private Datos datos;
    private TurnoRegistrado turno;
    private String enlace;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojRegresionPruebas", Clock.class,
                    () -> reloj(ZoneOffset.UTC), bean -> bean.setPrimary(true));
        }).run("--spring.datasource.hikari.maximum-pool-size=6");
        agenda = contexto.getBean(ServicioAgenda.class);
        cancelacion = contexto.getBean(ServicioCancelacionPorEnlace.class);
        bloqueos = contexto.getBean(ServicioBloqueos.class);
        jdbc = contexto.getBean(JdbcTemplate.class);
        transacciones = new TransactionTemplate(contexto.getBean(PlatformTransactionManager.class));
        contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(RECEPCION, 10);
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
    void preparar() {
        AHORA.set(BASE);
        hilos = Executors.newFixedThreadPool(3);
        datos = nuevosDatos();
        turno = alta(INICIO);
        enlace = cancelacion.emitir(turno.id(), 0, turno.inicio(), BASE);
    }

    @AfterEach
    void liberarHilos() {
        hilos.shutdownNow();
    }

    /* ==================== Vigencia ante la confirmacion ==================== */

    @Test
    void elEnlaceEmitidoAntesDeConfirmarSigueCancelandoUnaSolaVez() {
        confirmar();

        assertTrue(cancelacion.esValido(enlace, AHORA.get()));
        var cancelado = agenda.cancelarPorToken(enlace);

        assertEquals(EstadoTurno.CANCELADO, cancelado.estado());
        assertEquals("2", cancelado.version());
        assertTrue(consumido());
        assertThrows(TokenCancelacionInvalidoException.class, () -> agenda.cancelarPorToken(enlace));
        assertEquals("2", consultar().version());
        assertDoesNotThrow(() -> alta(INICIO));
    }

    @Test
    void laCancelacionQueEsperaUnaConfirmacionTerminaCancelando() throws Exception {
        var pendiente = transacciones.execute(transaccion -> {
            confirmar();
            var tarea = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            esperarBloqueados(1, tarea);
            return tarea;
        });

        assertEquals(EstadoTurno.CANCELADO, resultado(pendiente).estado());
    }

    @Test
    void laValidacionQueEsperaUnaConfirmacionInformaElEnlaceDisponible() throws Exception {
        var pendiente = transacciones.execute(transaccion -> {
            confirmar();
            var tarea = hilos.submit(() -> cancelacion.esValido(enlace, AHORA.get()));
            esperarBloqueados(1, tarea);
            return tarea;
        });

        assertTrue(resultado(pendiente));
    }

    @Test
    void reprogramarUnTurnoConfirmadoInvalidaElEnlace() {
        var confirmado = confirmar();

        mover(confirmado, INICIO.plus(Duration.ofHours(2)));

        assertThrows(TokenCancelacionInvalidoException.class, () -> agenda.cancelarPorToken(enlace));
    }

    @Test
    void unaConfirmacionRevertidaConservaElEnlaceEnLaVersionAnterior() {
        transacciones.executeWithoutResult(transaccion -> {
            confirmar();
            transaccion.setRollbackOnly();
        });

        assertEquals(0L, versionDelEnlace());
        assertEquals(EstadoTurno.CANCELADO, agenda.cancelarPorToken(enlace).estado());
    }

    @Test
    void confirmarNoRecuperaUnEnlaceDeUnaVersionAnterior() {
        var desactualizado = cancelacion.emitir(turno.id(), 7, turno.inicio(), BASE);

        confirmar();

        assertEquals(7L, versionDelEnlace());
        assertThrows(TokenCancelacionInvalidoException.class, () -> agenda.cancelarPorToken(desactualizado));
    }

    /* ==================== Orden de locks ==================== */

    @Test
    void reprogramacionPrimeroYEnlaceDespuesTerminanSinDeadlock() throws Exception {
        Future<TurnoRegistrado> movimiento;
        Future<TurnoRegistrado> porEnlace;
        try (var retenido = retener(Fila.PROFESIONAL)) {
            movimiento = hilos.submit(() -> mover(turno, INICIO.plus(Duration.ofHours(2))));
            esperarBloqueados(1, movimiento);
            porEnlace = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            esperarBloqueados(2, porEnlace);
            retenido.rollback();
        }

        var movido = resultado(movimiento);
        var rechazo = falla(porEnlace);
        assertInstanceOf(TokenCancelacionInvalidoException.class, rechazo, () -> causaRaiz(rechazo));
        assertEquals(EstadoTurno.PENDIENTE, movido.estado());
        assertEquals(movido, consultar(movido));
    }

    @Test
    void enlacePrimeroYReprogramacionDespuesDejanElTurnoCancelado() throws Exception {
        Future<TurnoRegistrado> porEnlace;
        Future<TurnoRegistrado> movimiento;
        try (var retenido = retener(Fila.PROFESIONAL)) {
            porEnlace = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            esperarBloqueados(1, porEnlace);
            movimiento = hilos.submit(() -> mover(turno, INICIO.plus(Duration.ofHours(2))));
            esperarBloqueados(2, movimiento);
            retenido.rollback();
        }

        var cancelado = resultado(porEnlace);
        assertInstanceOf(VersionTurnoDesactualizadaException.class, falla(movimiento));
        assertEquals(cancelado, consultar());
        assertTrue(consumido());
    }

    @Test
    void laCancelacionInternaPrimeroDejaElEnlaceSinEfecto() throws Exception {
        Future<TurnoRegistrado> interna;
        Future<TurnoRegistrado> porEnlace;
        try (var retenido = retener(Fila.PROFESIONAL)) {
            interna = hilos.submit(() -> agenda.cancelar(RECEPCION,
                    new SolicitudCancelacion(turno.id(), turno.version())));
            esperarBloqueados(1, interna);
            porEnlace = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            esperarBloqueados(2, porEnlace);
            retenido.rollback();
        }

        assertEquals("1", resultado(interna).version());
        assertInstanceOf(TokenCancelacionInvalidoException.class, falla(porEnlace));
        assertFalse(consumido());
    }

    @Test
    void altaYBloqueoEnEsperaTerminanDespuesDeLaCancelacionPorEnlace() throws Exception {
        var otroSillon = recursos().altaSillon(RECEPCION, codigo(), true).id();
        Future<TurnoRegistrado> porEnlace;
        Future<TurnoRegistrado> alta;
        Future<?> bloqueo;
        try (var retenido = retener(Fila.SILLON)) {
            porEnlace = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            esperarBloqueados(1, porEnlace);
            alta = hilos.submit(() -> agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(),
                    datos.profesional(), otroSillon, datos.prestacion(), INICIO, 30)));
            esperarBloqueados(2, alta);
            bloqueo = hilos.submit(() -> bloqueos.crear(RECEPCION, new RecursosBloqueo(null, datos.sillon()),
                    new PeriodoBloqueo(INICIO, turno.fin())));
            esperarBloqueados(3, bloqueo);
            retenido.rollback();
        }

        assertEquals(EstadoTurno.CANCELADO, resultado(porEnlace).estado());
        assertEquals(EstadoTurno.PENDIENTE, resultado(alta).estado());
        assertNotNull(resultado(bloqueo));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void unEnlaceSinEfectoSeRechazaSinEsperarLosRecursos(boolean usado) throws Exception {
        if (usado) {
            agenda.cancelarPorToken(enlace);
        } else {
            agenda.cancelar(RECEPCION, new SolicitudCancelacion(turno.id(), turno.version()));
        }

        try (var retenido = retener(Fila.PROFESIONAL)) {
            var porEnlace = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            assertInstanceOf(TokenCancelacionInvalidoException.class, falla(porEnlace));
            retenido.rollback();
        }
    }

    /* ==================== Plazo despues de las esperas ==================== */

    @ParameterizedTest
    @EnumSource(Fila.class)
    void rechazaSiElPlazoVenceMientrasEsperaUnLock(Fila fila) throws Exception {
        AHORA.set(LIMITE.minusSeconds(1));
        var porEnlace = cancelarEsperando(fila, LIMITE.plus(1, ChronoUnit.MICROS));

        assertInstanceOf(TokenCancelacionInvalidoException.class, falla(porEnlace));
        assertEquals(turno, consultar());
        assertFalse(consumido());
        assertThrows(ConflictoAgendaException.class, () -> alta(INICIO));
    }

    @ParameterizedTest
    @EnumSource(Fila.class)
    void aceptaElBordeInclusivoAlcanzadoMientrasEsperaUnLock(Fila fila) throws Exception {
        AHORA.set(LIMITE.minusSeconds(1));
        var porEnlace = cancelarEsperando(fila, LIMITE);

        assertEquals(EstadoTurno.CANCELADO, resultado(porEnlace).estado());
        assertEquals(LIMITE, jdbc.queryForObject(
                "select consumido_en from odontologia.token_cancelacion where turno_id = ?",
                java.time.OffsetDateTime.class, turno.id()).toInstant());
    }

    /* ==================== Apoyo ==================== */

    private Future<TurnoRegistrado> cancelarEsperando(Fila fila, Instant alLiberar) throws SQLException {
        try (var retenido = retener(fila)) {
            var porEnlace = hilos.submit(() -> agenda.cancelarPorToken(enlace));
            esperarBloqueados(1, porEnlace);
            AHORA.set(alLiberar);
            retenido.rollback();
            return porEnlace;
        }
    }

    /** Retiene una fila desde una conexión ajena hasta que se revierte o cierra. */
    private Connection retener(Fila fila) throws SQLException {
        var conexion = db.adminConnection();
        conexion.setAutoCommit(false);
        var id = switch (fila) {
            case TURNO, TOKEN -> turno.id();
            case PROFESIONAL -> datos.profesional();
            case SILLON -> datos.sillon();
        };
        try (var consulta = conexion.prepareStatement(fila.bloqueo)) {
            consulta.setObject(1, id);
            try (var filas = consulta.executeQuery()) {
                assertTrue(filas.next());
            }
        }
        return conexion;
    }

    private static void esperarBloqueados(int cantidad, Future<?> ultima) {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try (var conexion = db.adminConnection(); var consulta = conexion.prepareStatement("""
                select count(*) from pg_stat_activity
                where datname = current_database() and cardinality(pg_blocking_pids(pid)) > 0
                """)) {
            while (!ultima.isDone() && System.nanoTime() < limite) {
                try (var filas = consulta.executeQuery()) {
                    filas.next();
                    if (filas.getInt(1) >= cantidad) return;
                }
                Thread.sleep(10);
            }
        } catch (SQLException error) {
            throw new IllegalStateException(error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error);
        }
        fail("La operación no quedó esperando un lock");
    }

    private static <T> T resultado(Future<T> tarea) throws Exception {
        return tarea.get(15, TimeUnit.SECONDS);
    }

    private static Throwable falla(Future<?> tarea) throws Exception {
        try {
            tarea.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException error) {
            return error.getCause();
        }
        return fail("Se esperaba que la operación fuera rechazada");
    }

    private static String causaRaiz(Throwable error) {
        var causa = error;
        while (causa.getCause() != null) causa = causa.getCause();
        return causa instanceof SQLException sql ? sql.getSQLState() + " " + sql.getMessage() : causa.toString();
    }

    private TurnoRegistrado confirmar() {
        return agenda.confirmar(RECEPCION, turno.id(), turno.version());
    }

    private static TurnoRegistrado mover(TurnoRegistrado actual, Instant inicio) {
        return agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                actual.id(), actual.version(), inicio, actual.profesionalId(), actual.sillonId()));
    }

    private TurnoRegistrado consultar() {
        return consultar(turno);
    }

    private static TurnoRegistrado consultar(TurnoRegistrado buscado) {
        return agenda.consultarAgendaSillon(RECEPCION, buscado.sillonId(), buscado.inicio(), buscado.fin())
                .stream().filter(vista -> vista.id().equals(buscado.id())).findFirst().orElseThrow();
    }

    private boolean consumido() {
        return jdbc.queryForObject("select consumido from odontologia.token_cancelacion where turno_id = ?",
                Boolean.class, turno.id());
    }

    private long versionDelEnlace() {
        return jdbc.queryForObject("select version_turno from odontologia.token_cancelacion where turno_id = ?",
                Long.class, turno.id());
    }

    private TurnoRegistrado alta(Instant inicio) {
        return agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(),
                datos.prestacion(), inicio, 30));
    }

    private static ServicioRecursosAgenda recursos() {
        return contexto.getBean(ServicioRecursosAgenda.class);
    }

    private static Datos nuevosDatos() {
        var paciente = contexto.getBean(ServicioPacientes.class).crear(RECEPCION,
                new CrearPaciente(codigo(), "Nombre ficticio", "Apellido ficticio", "turno@example.invalid", null)).id();
        return new Datos(paciente, recursos().altaProfesional(RECEPCION, codigo(), true).id(),
                recursos().altaSillon(RECEPCION, codigo(), true).id(),
                recursos().altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

    private static Clock reloj(ZoneId zona) {
        return new Clock() {
            @Override public ZoneId getZone() { return zona; }
            @Override public Clock withZone(ZoneId otra) { return reloj(otra); }
            @Override public Instant instant() { return AHORA.get(); }
        };
    }

    private enum Fila {
        TURNO("select id from odontologia.turno where id = ? for no key update"),
        PROFESIONAL("select id from odontologia.profesional where id = ? for no key update"),
        SILLON("select id from odontologia.sillon where id = ? for no key update"),
        TOKEN("select id from odontologia.token_cancelacion where turno_id = ? for update");

        private final String bloqueo;

        Fila(String bloqueo) {
            this.bloqueo = bloqueo;
        }
    }

    private record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {}
}
