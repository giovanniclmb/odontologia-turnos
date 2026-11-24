package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ConflictoAgendaException;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudReprogramacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.agenda.VersionTurnoDesactualizadaException;
import ar.edu.utn.odontologia.agenda.bloqueos.LocksRecursosAgenda;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.ServicioBloqueos;
import ar.edu.utn.odontologia.agenda.bloqueos.TurnoOcupanteException;
import ar.edu.utn.odontologia.agenda.persistencia.TurnoPersistido;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import jakarta.persistence.EntityManagerFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

class ReprogramacionPersistidaIT {
    private static final Instant INICIO = Instant.parse("2040-04-05T10:00:00Z");
    private static final Instant DESTINO = INICIO.plusSeconds(7200);
    private static final AtomicReference<Instant> AHORA = new AtomicReference<>();
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION, null);
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ServicioBloqueos bloqueos;
    private static ServicioPacientes pacientes;
    private static ServicioRecursosAgenda recursos;
    private static ServicioConfiguracionSede sede;
    private static ServicioAsignacionPacientes asignaciones;
    private static LocksRecursosAgenda locks;
    private static TransactionTemplate transacciones;
    private static TransactionTemplate nuevaTransaccion;
    private static JdbcTemplate jdbc;
    private Datos datos;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojReprogramacionPruebas", Clock.class,
                    () -> reloj(ZoneOffset.UTC), bean -> bean.setPrimary(true));
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        bloqueos = contexto.getBean(ServicioBloqueos.class);
        pacientes = contexto.getBean(ServicioPacientes.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
        sede = contexto.getBean(ServicioConfiguracionSede.class);
        asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        locks = contexto.getBean(LocksRecursosAgenda.class);
        transacciones = contexto.getBean(TransactionTemplate.class);
        nuevaTransaccion = new TransactionTemplate(transacciones.getTransactionManager());
        nuevaTransaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
    void preparar() {
        AHORA.set(INICIO.minusSeconds(3600));
        sede.establecerIntervalo(RECEPCION, 10);
        datos = nuevosDatos();
    }

    @Test
    void devuelveVersionRealTrasMoverAunSiLaTransaccionYaLeyoLaEntidad() {
        var original = alta(datos, INICIO, 45);
        var nuevo = nuevosDatos();
        assertNotNull(original.version());
        assertFalse(original.version().isBlank());
        assertEquals(original, consultar(original));
        var conConstructorAnterior = new TurnoRegistrado(original.id(), original.pacienteId(),
                original.profesionalId(), original.sillonId(), original.prestacionId(),
                original.inicio(), original.fin(), original.duracionMinutos(), original.estado());
        assertEquals(original.id(), conConstructorAnterior.id());
        recursos.ajustarDuracion(RECEPCION, datos.prestacion(), 20);

        var actualizado = transacciones.execute(estado -> {
            var entidades = EntityManagerFactoryUtils.getTransactionalEntityManager(contexto.getBean(EntityManagerFactory.class));
            assertNotNull(entidades);
            assertEquals(INICIO, entidades.find(TurnoPersistido.class, original.id()).inicio());
            var movido = mover(RECEPCION, original, nuevo.profesional(), nuevo.sillon(), DESTINO.plusNanos(123_456_999));
            assertEquals(movido, consultar(movido));
            return movido;
        });
        assertNotNull(actualizado);
        assertConservaCampos(original, actualizado);
        assertEquals(nuevo.profesional(), actualizado.profesionalId());
        assertEquals(nuevo.sillon(), actualizado.sillonId());
        assertEquals(DESTINO.plusNanos(123_456_000), actualizado.inicio());
        assertEquals(actualizado.inicio().plusSeconds(45 * 60), actualizado.fin());
        assertNotEquals(original.version(), actualizado.version());
        assertEquals(actualizado, consultar(actualizado));
        assertTrue(vista(RECEPCION, datos.sillon(), INICIO).isEmpty());
        assertNotNull(alta(datos, INICIO, null).id());
    }

    @Test
    void aplicaAlcancePorTurnoYAsignacionSinRevelarRegistrosAjenos() {
        var original = alta(datos, INICIO, null);
        var otros = nuevosDatos();
        var otroTurno = alta(otros, INICIO, null);
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        assertThrows(AutorizacionDenegadaException.class, () -> mover(null, original, datos.profesional(), otros.sillon(), DESTINO));
        assertThrows(AutorizacionDenegadaException.class, () -> mover(odontologo, original, datos.profesional(), otros.sillon(), DESTINO));
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        assertThrows(AutorizacionDenegadaException.class,
                () -> mover(odontologo, otroTurno, datos.profesional(), otros.sillon(), DESTINO));
        assertThrows(AutorizacionDenegadaException.class,
                () -> mover(odontologo, original, otros.profesional(), otros.sillon(), DESTINO));
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.reprogramar(odontologo,
                new SolicitudReprogramacion(UUID.randomUUID(), original.version(), DESTINO, datos.profesional(), otros.sillon())));
        assertEquals(original, consultar(original));
        var movido = mover(odontologo, original, datos.profesional(), otros.sillon(), DESTINO);
        assertEquals(datos.profesional(), movido.profesionalId());
        assertEquals(otros.sillon(), movido.sillonId());
    }

    @Test
    void unTurnoAjenoOcupaElSillonAunqueOdontologiaNoLoVea() {
        var original = alta(datos, INICIO, null);
        var otros = nuevosDatos();
        var ajeno = alta(otros, DESTINO, null);
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        assertTrue(vista(odontologo, otros.sillon(), DESTINO).isEmpty());
        var conflicto = assertThrows(ConflictoAgendaException.class,
                () -> mover(odontologo, original, datos.profesional(), otros.sillon(), DESTINO));
        assertEquals("El horario solicitado no está disponible", conflicto.getMessage());
        assertEquals(original, consultar(original));
        assertEquals(ajeno, consultar(ajeno));
    }

    @Test
    void rechazaVersionDesactualizadaInclusoTrasVolverYAdmiteDestinoIdentico() {
        var original = alta(datos, INICIO, null);
        var xminOriginal = jdbc.queryForObject("select xmin::text from odontologia.turno where id=?", String.class, original.id());
        var identico = mover(RECEPCION, original, datos.profesional(), datos.sillon(), INICIO.plusNanos(999));
        assertEquals(original, identico);
        assertEquals(xminOriginal, jdbc.queryForObject("select xmin::text from odontologia.turno where id=?",
                String.class, original.id()));
        var ida = mover(RECEPCION, original, datos.profesional(), datos.sillon(), INICIO.plusSeconds(5 * 60));
        var vuelta = mover(RECEPCION, ida, datos.profesional(), datos.sillon(), INICIO);
        assertNotEquals(original.version(), vuelta.version());
        var desactualizado = assertThrows(VersionTurnoDesactualizadaException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), INICIO));
        assertTrue(desactualizado.getMessage().contains("Volvé a consultar"));
        assertThrows(AutorizacionDenegadaException.class,
                () -> mover(null, vuelta, datos.profesional(), datos.sillon(), INICIO));
        AHORA.set(INICIO);
        assertThrows(IllegalArgumentException.class,
                () -> mover(RECEPCION, vuelta, datos.profesional(), datos.sillon(), INICIO));
        assertEquals(vuelta, consultar(vuelta));
    }

    @Test
    void identificaDatosYRecursosInvalidosYConservaLaReservaVigente() {
        var original = alta(datos, INICIO, null);
        var sillonInactivo = recursos.altaSillon(RECEPCION, codigo(), false).id();
        var profesionalInactivo = recursos.altaProfesional(RECEPCION, codigo(), false).id();
        assertThrows(IllegalArgumentException.class, () -> mover(RECEPCION, original, datos.profesional(), sillonInactivo, DESTINO));
        assertThrows(IllegalArgumentException.class, () -> mover(RECEPCION, original, profesionalInactivo, datos.sillon(), DESTINO));
        assertThrows(IllegalArgumentException.class, () -> mover(RECEPCION, original, datos.profesional(), UUID.randomUUID(), DESTINO));
        assertThrows(IllegalArgumentException.class, () -> agenda.reprogramar(RECEPCION,
                new SolicitudReprogramacion(original.id(), " ", DESTINO, datos.profesional(), datos.sillon())));
        assertThrows(IllegalArgumentException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), AHORA.get().plusNanos(999)));
        assertThrows(IllegalArgumentException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), AHORA.get().minusSeconds(1)));
        AHORA.set(INICIO.plusSeconds(1));
        assertThrows(IllegalArgumentException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), DESTINO));
        AHORA.set(INICIO.minusSeconds(3600));
        jdbc.update("update odontologia.configuracion_sede set intervalo_minutos = null");
        assertThrows(IllegalStateException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), DESTINO));
        assertEquals(original, consultar(original));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void conservaElMargenExactoAntesYDespuesPorCadaRecurso(boolean mismoProfesional) {
        var original = alta(datos, INICIO, 30);
        var otros = nuevosDatos();
        var vecino = new Datos(otros.paciente(), mismoProfesional ? datos.profesional() : otros.profesional(),
                mismoProfesional ? otros.sillon() : datos.sillon(), otros.prestacion());
        var ocupado = alta(vecino, DESTINO, 30);
        var antes = DESTINO.minusSeconds(40 * 60);
        var despues = ocupado.fin().plusSeconds(10 * 60);
        assertThrows(ConflictoAgendaException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), antes.plusNanos(1000)));
        assertThrows(ConflictoAgendaException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), despues.minusNanos(1000)));
        assertEquals(original, consultar(original));
        var movidoAntes = mover(RECEPCION, original, datos.profesional(), datos.sillon(), antes);
        assertEquals(antes, movidoAntes.inicio());
        assertEquals(despues, mover(RECEPCION, movidoAntes, datos.profesional(), datos.sillon(), despues).inicio());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void permiteBloqueosContiguosYRechazaUnMicrosegundoDeInterseccion(boolean profesional) {
        var original = alta(datos, INICIO, 30);
        var seleccion = profesional ? new RecursosBloqueo(datos.profesional(), null)
                : new RecursosBloqueo(null, datos.sillon());
        var periodo = new PeriodoBloqueo(DESTINO, DESTINO.plusSeconds(3600));
        bloqueos.crear(RECEPCION, seleccion, periodo);
        var antes = periodo.inicio().minusSeconds(30 * 60);
        var despues = periodo.fin();
        assertThrows(ConflictoAgendaException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), antes.plusNanos(1000)));
        assertThrows(ConflictoAgendaException.class,
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), despues.minusNanos(1000)));
        var movidoAntes = mover(RECEPCION, original, datos.profesional(), datos.sillon(), antes);
        assertEquals(periodo.inicio(), movidoAntes.fin());
        assertEquals(periodo.fin(), mover(RECEPCION, movidoAntes, datos.profesional(), datos.sillon(), despues).inicio());
    }

    @Test
    void revalidaLaAsignacionConfirmadaMientrasEspera() throws Exception {
        var original = alta(datos, INICIO, null);
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        var resultado = competir(() -> retenerRecursos(datos),
                () -> mover(odontologo, original, datos.profesional(), datos.sillon(), DESTINO), false,
                () -> nuevaTransaccion.executeWithoutResult(estado ->
                        asignaciones.desasignar(RECEPCION, datos.paciente(), datos.profesional())));
        assertInstanceOf(AutorizacionDenegadaException.class, resultado.error());
        assertEquals(original, consultar(original));
    }

    @Test
    void revalidaQueElOrigenSigaFuturoMientrasEspera() throws Exception {
        var original = alta(datos, INICIO, null);
        var resultado = competir(() -> retenerRecursos(datos),
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), DESTINO), false,
                () -> AHORA.set(INICIO));
        assertInstanceOf(IllegalArgumentException.class, resultado.error());
        assertEquals(original, consultar(original));
    }

    @Test
    void dosCambiosDelMismoTurnoSoloAceptanLaVersionActual() throws Exception {
        var original = alta(datos, INICIO, null);
        var resultado = competir(
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), DESTINO),
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), DESTINO.plusSeconds(3600)),
                false, () -> assertEquals(original, consultarFueraDeLaTransaccion(original)));
        assertInstanceOf(VersionTurnoDesactualizadaException.class, resultado.error());
        assertTrue(vista(RECEPCION, original.sillonId(), INICIO).isEmpty());
        assertEquals(resultado.primera(), vista(RECEPCION, original.sillonId(), DESTINO).getFirst());
    }

    @Test
    void priorizaAlcanceTrasEsperarLaFilaConVersionDesactualizada() throws Exception {
        var original = alta(datos, INICIO, null);
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        var destinoOdontologo = DESTINO.plusSeconds(3600);

        var resultado = competir(
                () -> mover(RECEPCION, original, datos.profesional(), datos.sillon(), DESTINO),
                () -> mover(odontologo, original, datos.profesional(), datos.sillon(), destinoOdontologo), false,
                () -> {
                    assertTrue(jdbc.queryForObject("""
                            select exists (
                                select 1 from pg_stat_activity a
                                where pg_backend_pid() = any(pg_blocking_pids(a.pid))
                                  and lower(a.query) like '%from odontologia.turno%'
                                  and lower(a.query) like '%for no key update%'
                            )
                            """, Boolean.class), "Odontología debe estar esperando el lock de la fila del turno");
                    nuevaTransaccion.executeWithoutResult(estado ->
                            asignaciones.desasignar(RECEPCION, datos.paciente(), datos.profesional()));
                });

        assertInstanceOf(AutorizacionDenegadaException.class, resultado.error());
        assertNull(resultado.segunda());
        var confirmado = assertInstanceOf(TurnoRegistrado.class, resultado.primera());
        assertNotEquals(original.version(), confirmado.version());
        assertEquals(confirmado, consultar(confirmado));
        assertTrue(vista(RECEPCION, original.sillonId(), destinoOdontologo).isEmpty());
    }

    @Test
    void movimientosDeDistintosTurnosConRecursosCruzadosSeSerializan() throws Exception {
        var otros = nuevosDatos();
        var primero = alta(datos, INICIO, null);
        var segundo = alta(otros, INICIO.plusSeconds(3600), null);
        var resultado = competir(
                () -> mover(RECEPCION, primero, otros.profesional(), otros.sillon(), DESTINO),
                () -> mover(RECEPCION, segundo, datos.profesional(), datos.sillon(), DESTINO.plusSeconds(3600)),
                false, () -> {});
        assertNull(resultado.error());
        assertEquals(resultado.primera(), vista(RECEPCION, otros.sillon(), DESTINO).getFirst());
        assertEquals(resultado.segunda(), vista(RECEPCION, datos.sillon(), DESTINO.plusSeconds(3600)).getFirst());
    }

    @ParameterizedTest
    @CsvSource({"ALTA,true,false", "ALTA,false,false", "ALTA,true,true", "ALTA,false,true",
            "BLOQUEO,true,false", "BLOQUEO,false,false", "BLOQUEO,true,true", "BLOQUEO,false,true"})
    void arbitraReprogramacionConOtrasEscriturasEnAmbosOrdenes(Operacion operacion, boolean muevePrimero,
                                                               boolean reviertePrimera) throws Exception {
        var original = alta(datos, INICIO, null);
        var destino = nuevosDatos();
        Supplier<?> movimiento = () -> mover(RECEPCION, original, destino.profesional(), destino.sillon(), DESTINO);
        Supplier<?> otraOperacion = operacion == Operacion.ALTA
                ? () -> alta(destino, DESTINO, null)
                : () -> bloqueos.crear(RECEPCION, new RecursosBloqueo(null, destino.sillon()),
                        new PeriodoBloqueo(DESTINO, DESTINO.plusSeconds(60)));
        var resultado = competir(muevePrimero ? movimiento : otraOperacion, muevePrimero ? otraOperacion : movimiento,
                reviertePrimera, () -> assertEquals(original, consultarFueraDeLaTransaccion(original)));
        if (reviertePrimera) {
            assertNull(resultado.error());
            assertNotNull(resultado.segunda());
        } else {
            assertNull(resultado.segunda());
            if (muevePrimero && operacion == Operacion.BLOQUEO) {
                assertInstanceOf(TurnoOcupanteException.class, resultado.error());
            } else {
                assertInstanceOf(ConflictoAgendaException.class, resultado.error());
            }
        }
        boolean movido = muevePrimero != reviertePrimera;
        if (movido) {
            assertTrue(vista(RECEPCION, original.sillonId(), INICIO).isEmpty());
            var enDestino = vista(RECEPCION, destino.sillon(), DESTINO).getFirst();
            assertConservaCampos(original, enDestino);
            assertNotEquals(original.version(), enDestino.version());
        } else {
            assertEquals(original, consultar(original));
        }
        assertEquals(movido || operacion == Operacion.ALTA ? 1 : 0, jdbc.queryForObject(
                "select count(*) from odontologia.turno where sillon_id=?", Integer.class, destino.sillon()));
        assertEquals(!movido && operacion == Operacion.BLOQUEO ? 1 : 0, jdbc.queryForObject(
                "select count(*) from odontologia.bloqueo_recurso where sillon_id=?", Integer.class, destino.sillon()));
    }

    private static Resultado competir(Supplier<?> primera, Supplier<?> segunda, boolean revertir,
                                      Runnable mientrasEspera) throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var pid = new CompletableFuture<Integer>();
            var pendiente = transacciones.execute(estado -> {
                Object primerResultado = primera.get();
                Future<?> tarea = executor.submit(() -> transacciones.execute(otroEstado -> {
                    pid.complete(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
                    return segunda.get();
                }));
                int pidSegundo = assertDoesNotThrow(() -> pid.get(5, TimeUnit.SECONDS));
                assertDoesNotThrow(() -> esperarBloqueo(tarea, pidSegundo));
                mientrasEspera.run();
                if (revertir) estado.setRollbackOnly();
                return new Pendiente(primerResultado, tarea);
            });
            assertNotNull(pendiente);
            try {
                return new Resultado(pendiente.primera(), pendiente.segunda().get(10, TimeUnit.SECONDS), null);
            } catch (ExecutionException error) {
                return new Resultado(pendiente.primera(), null, error.getCause());
            }
        }
    }

    private static void esperarBloqueo(Future<?> tarea, int pidSegundo) throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!tarea.isDone() && System.nanoTime() < limite) {
            if (Boolean.TRUE.equals(jdbc.queryForObject(
                    "select pg_backend_pid() = any(pg_blocking_pids(?))", Boolean.class, pidSegundo))) {
                return;
            }
            Thread.sleep(10);
        }
        fail("La segunda operación no esperó el lock de la primera");
    }

    private static RecursosBloqueo retenerRecursos(Datos datos) {
        var seleccion = new RecursosBloqueo(datos.profesional(), datos.sillon());
        locks.adquirir(List.of(seleccion));
        return seleccion;
    }

    private static void assertConservaCampos(TurnoRegistrado antes, TurnoRegistrado despues) {
        assertEquals(antes.id(), despues.id());
        assertEquals(antes.pacienteId(), despues.pacienteId());
        assertEquals(antes.prestacionId(), despues.prestacionId());
        assertEquals(antes.duracionMinutos(), despues.duracionMinutos());
        assertEquals(antes.estado(), despues.estado());
    }

    private static TurnoRegistrado mover(Identidad identidad, TurnoRegistrado turno, UUID profesional, UUID sillon,
                                         Instant inicio) {
        return agenda.reprogramar(identidad, new SolicitudReprogramacion(turno.id(), turno.version(), inicio, profesional, sillon));
    }

    private static TurnoRegistrado alta(Datos datos, Instant inicio, Integer duracion) {
        return agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(),
                datos.prestacion(), inicio, duracion));
    }

    private static TurnoRegistrado consultar(TurnoRegistrado turno) {
        return agenda.consultarAgendaSillon(RECEPCION, turno.sillonId(), turno.inicio(), turno.fin())
                .stream().filter(vista -> vista.id().equals(turno.id())).findFirst().orElseThrow();
    }

    private static TurnoRegistrado consultarFueraDeLaTransaccion(TurnoRegistrado turno) {
        return nuevaTransaccion.execute(estado -> consultar(turno));
    }

    private static List<TurnoRegistrado> vista(Identidad identidad, UUID sillon, Instant inicio) {
        return agenda.consultarAgendaSillon(identidad, sillon, inicio, inicio.plusSeconds(3600));
    }

    private static Datos nuevosDatos() {
        var paciente = pacientes.crear(RECEPCION,
                new CrearPaciente(codigo(), "Nombre ficticio", "Apellido ficticio", "turno@example.invalid", null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, codigo(), true).id(),
                recursos.altaSillon(RECEPCION, codigo(), true).id(),
                recursos.altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    private static String codigo() { return UUID.randomUUID().toString(); }

    private static Identidad identidad(Rol rol, UUID profesional) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio", rol, profesional);
    }

    private static Clock reloj(ZoneId zona) {
        return new Clock() {
            @Override public ZoneId getZone() { return zona; }
            @Override public Clock withZone(ZoneId otra) { return reloj(otra); }
            @Override public Instant instant() { return AHORA.get(); }
        };
    }

    private record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {}
    private record Pendiente(Object primera, Future<?> segunda) {}
    private record Resultado(Object primera, Object segunda, Throwable error) {}
    private enum Operacion { ALTA, BLOQUEO }
}
