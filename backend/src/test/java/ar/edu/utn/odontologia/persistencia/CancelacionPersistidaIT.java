package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ConflictoAgendaException;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

class CancelacionPersistidaIT {
    private static final Instant INICIO = Instant.parse("2040-04-05T10:00:00.123456Z");
    private static final Instant DESTINO = INICIO.plusSeconds(7200);
    private static final AtomicReference<Instant> AHORA = new AtomicReference<>();
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION, null);

    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ServicioBloqueos bloqueos;
    private static ServicioPacientes pacientes;
    private static ServicioRecursosAgenda recursos;
    private static ServicioAsignacionPacientes asignaciones;
    private static LocksRecursosAgenda locks;
    private static TransactionTemplate transacciones;
    private static TransactionTemplate nuevaTransaccion;
    private static JdbcTemplate jdbc;

    private Datos datos;
    private TurnoRegistrado original;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojCancelacionPruebas", Clock.class,
                    () -> reloj(ZoneOffset.UTC), bean -> bean.setPrimary(true));
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        bloqueos = contexto.getBean(ServicioBloqueos.class);
        pacientes = contexto.getBean(ServicioPacientes.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
        asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        locks = contexto.getBean(LocksRecursosAgenda.class);
        jdbc = contexto.getBean(JdbcTemplate.class);
        var gestor = contexto.getBean(TransactionTemplate.class).getTransactionManager();
        transacciones = new TransactionTemplate(gestor);
        transacciones.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        nuevaTransaccion = new TransactionTemplate(gestor);
        nuevaTransaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
        AHORA.set(INICIO.minusSeconds(3600));
        datos = nuevosDatos();
        original = alta(datos, INICIO, 30);
    }

    @Test
    void conservaElTurnoYDevuelveElResultadoActualAunqueYaHayaUnaEntidadJpaCargada() {
        var actualizado = transacciones.execute(transaccion -> {
            var entidades = EntityManagerFactoryUtils.getTransactionalEntityManager(contexto.getBean(EntityManagerFactory.class));
            assertNotNull(entidades);
            assertEquals("PENDIENTE", entidades.find(TurnoPersistido.class, original.id()).estado().name());
            var cancelado = cancelar(RECEPCION, original);
            assertEquals(cancelado, consultar(original));
            return cancelado;
        });

        assertCancelado(actualizado);
        assertEquals(actualizado, consultar(original));
        assertEquals(1L, jdbc.queryForObject(
                "select version from odontologia.turno where id = ?", Long.class, original.id()));
    }

    @Test
    void permiteAOdontologiaCancelarTurnoPropioDePacienteAsignado() {
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());

        var cancelado = cancelar(odontologo, original);

        assertCancelado(cancelado);
        assertEquals(List.of(cancelado), agenda.consultarAgendaSillon(
                odontologo, datos.sillon(), original.inicio(), original.fin()));
    }

    @Test
    void deniegaSinIdentidadPermisoOVinculacion() {
        var solicitud = solicitud(original);
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.cancelar(null, solicitud));
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.cancelar(identidad(null, null), solicitud));
        assertThrows(AutorizacionDenegadaException.class,
                () -> agenda.cancelar(identidad(Rol.ODONTOLOGO, null), solicitud));
        assertEquals(original, consultar(original));
    }

    @Test
    void ocultaTurnosAjenosInexistentesYDePacientesNoAsignadosAOdontologia() {
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        var ajeno = alta(nuevosDatos(), INICIO, 30);
        var sinAsignar = assertThrows(AutorizacionDenegadaException.class, () -> cancelar(odontologo, original));
        assertEquals(sinAsignar.getMessage(), assertThrows(AutorizacionDenegadaException.class,
                () -> cancelar(odontologo, ajeno)).getMessage());
        assertEquals(sinAsignar.getMessage(), assertThrows(AutorizacionDenegadaException.class,
                () -> agenda.cancelar(odontologo, new SolicitudCancelacion(UUID.randomUUID(), original.version()))).getMessage());
        assertEquals(ajeno, consultar(ajeno));
        assertEquals(original, consultar(original));
    }

    @Test
    void rechazaLaSolicitudIncompletaYElInexistenteParaRecepcion() {
        assertThrows(IllegalArgumentException.class, () -> agenda.cancelar(RECEPCION, null));
        assertThrows(IllegalArgumentException.class,
                () -> agenda.cancelar(RECEPCION, new SolicitudCancelacion(null, original.version())));
        assertThrows(IllegalArgumentException.class,
                () -> agenda.cancelar(RECEPCION, new SolicitudCancelacion(original.id(), null)));
        assertThrows(IllegalArgumentException.class,
                () -> agenda.cancelar(RECEPCION, new SolicitudCancelacion(original.id(), " ")));

        var ausente = assertThrows(IllegalArgumentException.class, () ->
                agenda.cancelar(RECEPCION, new SolicitudCancelacion(UUID.randomUUID(), original.version())));
        assertTrue(ausente.getMessage().contains("no existe"));
        assertEquals(original, consultar(original));
    }

    @Test
    void diferenciaEdicionDesactualizadaDeRepeticionActual() {
        var movido = mover(original, DESTINO);
        var error = assertThrows(VersionTurnoDesactualizadaException.class, () -> cancelar(RECEPCION, original));
        assertTrue(error.getMessage().contains("consultar"));
        assertEquals(movido, consultar(movido));

        var cancelado = cancelar(RECEPCION, movido);
        assertThrows(VersionTurnoDesactualizadaException.class, () -> cancelar(RECEPCION, movido));
        AHORA.set(DESTINO);
        var repetida = assertThrows(IllegalArgumentException.class, () -> cancelar(RECEPCION, cancelado));
        assertTrue(repetida.getMessage().contains("ya está cancelado"));
        assertEquals(cancelado, consultar(cancelado));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1})
    void conservaElTurnoSiElInicioYaSeAlcanzo(long nanosegundosTranscurridos) {
        AHORA.set(INICIO.plusNanos(nanosegundosTranscurridos));

        assertThrows(IllegalArgumentException.class, () -> cancelar(RECEPCION, original));

        assertEquals(original, consultar(original));
    }

    @Test
    void conservaVisibleElCanceladoYPermiteReservarElHorarioRespetandoElIntervalo() {
        alta(datos, INICIO.plusSeconds(40 * 60), 30);
        var cancelado = cancelar(RECEPCION, original);
        assertThrows(ConflictoAgendaException.class, () -> alta(datos, INICIO, 31));

        var reemplazo = alta(datos, INICIO, 30);

        assertEquals(2, agenda.consultarAgendaSillon(RECEPCION, datos.sillon(), INICIO, original.fin()).size());
        assertEquals(cancelado, consultar(original));
        assertEquals(reemplazo, consultar(reemplazo));
        var ajeno = identidad(Rol.ODONTOLOGO, nuevosDatos().profesional());
        assertTrue(agenda.consultarAgendaSillon(ajeno, datos.sillon(), INICIO, original.fin()).isEmpty());
    }

    @Test
    void permiteMoverOtroTurnoAlHorarioLiberadoYRechazaMoverElCanceladoInclusoSinCambios() {
        var segundo = alta(datos, DESTINO, 30);
        var cancelado = cancelar(RECEPCION, original);

        assertThrows(IllegalArgumentException.class, () -> mover(cancelado, INICIO));
        var movido = mover(segundo, INICIO);

        assertEquals(INICIO, movido.inicio());
        assertEquals(cancelado, consultar(original));
        assertEquals(movido, consultar(movido));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void admiteBloquearCadaRecursoLiberadoYRechazaUnAltaIncompatible(boolean profesional) {
        cancelar(RECEPCION, original);
        var seleccion = profesional
                ? new RecursosBloqueo(datos.profesional(), null)
                : new RecursosBloqueo(null, datos.sillon());
        var bloqueo = bloqueos.crear(RECEPCION, seleccion, new PeriodoBloqueo(INICIO, original.fin()));

        assertNotNull(bloqueo.id());
        assertThrows(ConflictoAgendaException.class, () -> alta(datos, INICIO, 30));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void revalidaAsignacionRevocadaMientrasEsperaLaFilaOElRecurso(boolean esperaFila) throws Exception {
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        Supplier<?> primera = esperaFila ? () -> mover(original, DESTINO) : this::retenerRecursos;

        var carrera = competir(primera, () -> cancelar(odontologo, original), false, provisional -> {
            if (esperaFila) {
                assertTrue(jdbc.queryForObject("""
                        select exists (
                            select 1 from pg_stat_activity a
                            where pg_backend_pid() = any(pg_blocking_pids(a.pid))
                              and lower(a.query) like '%from odontologia.turno%'
                        )
                        """, Boolean.class));
            }
            nuevaTransaccion.executeWithoutResult(transaccion ->
                    asignaciones.desasignar(RECEPCION, datos.paciente(), datos.profesional()));
        });

        assertInstanceOf(AutorizacionDenegadaException.class, carrera.error());
        var turnoConfirmado = esperaFila ? (TurnoRegistrado) carrera.primera() : original;
        assertEquals(turnoConfirmado, consultar(turnoConfirmado));
    }

    @Test
    void rechazaSiElTurnoComienzaMientrasSeEsperanLosRecursos() throws Exception {
        var carrera = competir(this::retenerRecursos, () -> cancelar(RECEPCION, original), false,
                provisional -> AHORA.set(INICIO));

        assertInstanceOf(IllegalArgumentException.class, carrera.error());
        assertEquals(original, consultar(original));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void soloElPrimerCambioConLaVersionInicialConfirmaEntreCancelacionYReprogramacion(boolean cancelaPrimero)
            throws Exception {
        Supplier<TurnoRegistrado> cancelacion = () -> cancelar(RECEPCION, original);
        Supplier<TurnoRegistrado> movimiento = () -> mover(original, DESTINO);
        var carrera = competir(cancelaPrimero ? cancelacion : movimiento, cancelaPrimero ? movimiento : cancelacion,
                false, provisional -> assertOriginalConfirmado());

        assertInstanceOf(VersionTurnoDesactualizadaException.class, carrera.error());
        var confirmado = assertInstanceOf(TurnoRegistrado.class, carrera.primera());
        if (cancelaPrimero) {
            assertCancelado(confirmado);
        } else {
            assertEquals("PENDIENTE", confirmado.estado().name());
            assertEquals(DESTINO, confirmado.inicio());
        }
        assertEquals(confirmado, consultar(confirmado));
        assertEquals(1L, jdbc.queryForObject(
                "select version from odontologia.turno where id = ?", Long.class, original.id()));
    }

    @Test
    void dosCancelacionesConcurrentesSoloCambianLaVersionUnaVez() throws Exception {
        var carrera = competir(() -> cancelar(RECEPCION, original), () -> cancelar(RECEPCION, original),
                false, provisional -> assertOriginalConfirmado());

        assertInstanceOf(VersionTurnoDesactualizadaException.class, carrera.error());
        var cancelado = assertInstanceOf(TurnoRegistrado.class, carrera.primera());
        assertCancelado(cancelado);
        assertEquals(cancelado, consultar(original));
        assertEquals(1L, jdbc.queryForObject(
                "select version from odontologia.turno where id = ?", Long.class, original.id()));
    }

    @Test
    void reprogramaConLaVersionOriginalSiLaCancelacionSeRevierte() throws Exception {
        var carrera = competir(() -> cancelar(RECEPCION, original), () -> mover(original, DESTINO),
                true, provisional -> assertOriginalConfirmado());

        assertNull(carrera.error());
        var movido = assertInstanceOf(TurnoRegistrado.class, carrera.segunda());
        assertEquals("PENDIENTE", movido.estado().name());
        assertEquals(DESTINO, movido.inicio());
        assertEquals(movido, consultar(movido));
    }

    @ParameterizedTest
    @EnumSource(Operacion.class)
    void siLaOtraOperacionValidaPrimeroElTurnoAunOcupaElHorario(Operacion operacion) throws Exception {
        var carrera = competir(this::retenerRecursos, () -> cancelar(RECEPCION, original), true,
                provisional -> assertThrows(operacion.conflicto, () -> usarHorario(operacion)));

        assertNull(carrera.error());
        var cancelado = assertInstanceOf(TurnoRegistrado.class, carrera.segunda());
        assertCancelado(cancelado);
        assertEquals(cancelado, consultar(original));
    }

    @ParameterizedTest
    @CsvSource({"ALTA,true", "ALTA,false",
            "BLOQUEO_PROFESIONAL,true", "BLOQUEO_PROFESIONAL,false",
            "BLOQUEO_SILLON,true", "BLOQUEO_SILLON,false"})
    void elRetornoDeCancelarNoLiberaHastaConfirmarLaTransaccionExterior(Operacion operacion, boolean revertir)
            throws Exception {
        var carrera = competir(() -> cancelar(RECEPCION, original), () -> usarHorario(operacion), revertir,
                provisional -> {
                    assertCancelado(assertInstanceOf(TurnoRegistrado.class, provisional));
                    assertOriginalConfirmado();
                });

        if (revertir) {
            assertInstanceOf(operacion.conflicto, carrera.error());
            assertEquals(original, consultar(original));
        } else {
            assertNull(carrera.error());
            assertNotNull(carrera.segunda());
            assertEquals(carrera.primera(), consultar(original));
        }
    }

    @Test
    void confirmadoSigueOcupandoProfesionalYSillon() {
        jdbc.update("update odontologia.turno set estado = 'CONFIRMADO' where id = ?", original.id());
        var otros = nuevosDatos();
        for (var destino : List.of(
                new Datos(otros.paciente(), datos.profesional(), otros.sillon(), otros.prestacion()),
                new Datos(otros.paciente(), otros.profesional(), datos.sillon(), otros.prestacion()))) {
            assertThrows(ConflictoAgendaException.class, () -> alta(destino, INICIO, 30));
            assertThrows(TurnoOcupanteException.class, () -> bloqueos.crear(RECEPCION,
                    new RecursosBloqueo(destino.profesional(), destino.sillon()),
                    new PeriodoBloqueo(INICIO, original.fin())));
        }
        var otroTurno = alta(datos, original.fin().plusSeconds(600), 30);
        assertThrows(ConflictoAgendaException.class, () -> mover(otroTurno, INICIO));
    }

    @Test
    void registraYListaConfirmacionesSoloParaRecepcion() {
        var otro = alta(nuevosDatos(), DESTINO, 30);
        var simultaneo = alta(nuevosDatos(), INICIO, 30);
        var vencido = alta(nuevosDatos(), INICIO.minusSeconds(60), 30);
        AHORA.set(vencido.inicio());
        var controlados = List.of(original, simultaneo, otro, vencido);
        var esperados = new java.util.ArrayList<>(List.of(original, simultaneo));
        esperados.sort(java.util.Comparator.comparing(turno -> turno.id().toString()));
        esperados.add(otro);
        assertEquals(esperados, agenda.consultarPendientes(RECEPCION).stream()
                .filter(controlados::contains).toList());
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        assertThrows(AutorizacionDenegadaException.class, () -> agenda.consultarPendientes(odontologo));
        assertThrows(AutorizacionDenegadaException.class,
                () -> agenda.confirmar(odontologo, original.id(), original.version()));
        assertThrows(IllegalArgumentException.class, () -> confirmar(vencido));
        assertThrows(IllegalArgumentException.class, () -> agenda.confirmar(RECEPCION, UUID.randomUUID(), "0"));
        assertThrows(IllegalArgumentException.class, () -> agenda.confirmar(RECEPCION, original.id(), ""));

        var confirmado = confirmar(original);
        assertEquals("CONFIRMADO", confirmado.estado().name());
        assertEquals("1", confirmado.version());
        assertEquals(confirmado, consultar(original));
        assertTrue(agenda.consultarPendientes(RECEPCION).stream().noneMatch(t -> t.id().equals(original.id())));
        assertThrows(VersionTurnoDesactualizadaException.class, () -> confirmar(original));
        var repetido = assertThrows(IllegalArgumentException.class, () -> confirmar(confirmado));
        assertTrue(repetido.getMessage().contains("ya está confirmado"));
        assertEquals(confirmado, consultar(original));
        var cancelado = cancelar(RECEPCION, otro);
        assertThrows(IllegalArgumentException.class, () -> confirmar(cancelado));
        assertTrue(agenda.consultarPendientes(RECEPCION).stream().noneMatch(t -> t.id().equals(otro.id())));
    }

    @Test
    void recepcionPuedeMoverOCancelarUnConfirmado() {
        var confirmado = confirmar(original);
        var odontologo = identidad(Rol.ODONTOLOGO, datos.profesional());
        asignaciones.asignar(RECEPCION, datos.paciente(), datos.profesional());
        assertThrows(IllegalArgumentException.class, () -> cancelar(odontologo, confirmado));
        assertThrows(IllegalArgumentException.class, () -> agenda.reprogramar(odontologo,
                new SolicitudReprogramacion(confirmado.id(), confirmado.version(), DESTINO,
                        confirmado.profesionalId(), confirmado.sillonId())));
        assertEquals(confirmado, mover(confirmado, INICIO));
        var ocupado = alta(datos, DESTINO, 30);
        assertThrows(ConflictoAgendaException.class, () -> mover(confirmado, ocupado.inicio()));
        assertEquals(confirmado, consultar(original));
        var movido = mover(confirmado, DESTINO.plusSeconds(3600));
        assertEquals("PENDIENTE", movido.estado().name());
        assertEquals("2", movido.version());
        var cancelado = cancelar(RECEPCION, confirmar(movido));
        assertEquals("CANCELADO", cancelado.estado().name());
        assertEquals("4", cancelado.version());
        assertEquals(movido.inicio(), alta(datos, movido.inicio(), 30).inicio());
    }

    @ParameterizedTest
    @ValueSource(strings = {"confirmar", "cancelar", "mover"})
    void confirmacionCompitePorLaMismaVersion(String operacion) throws Exception {
        var carrera = competir(() -> confirmar(original), () -> switch (operacion) {
            case "cancelar" -> cancelar(RECEPCION, original);
            case "mover" -> mover(original, DESTINO);
            default -> confirmar(original);
        }, false, resultado -> {});
        assertInstanceOf(VersionTurnoDesactualizadaException.class, carrera.error());
        assertEquals(carrera.primera(), consultar(original));
    }

    @Test
    void confirmacionRevertidaPermiteReintentarConLaMismaVersion() throws Exception {
        var carrera = competir(() -> confirmar(original), () -> confirmar(original), true, resultado -> {});
        assertNull(carrera.error());
        assertEquals(carrera.segunda(), consultar(original));
    }

    @Test
    void confirmacionRevalidaElInicioTrasEsperarLaFila() throws Exception {
        var carrera = competir(() -> {
            jdbc.queryForObject("select id from odontologia.turno where id = ? for no key update",
                    UUID.class, original.id());
            return original;
        }, () -> confirmar(original), false, resultado -> AHORA.set(INICIO));
        assertInstanceOf(IllegalArgumentException.class, carrera.error());
        assertEquals(original, consultar(original));
    }

    private static TurnoRegistrado confirmar(TurnoRegistrado turno) {
        return agenda.confirmar(RECEPCION, turno.id(), turno.version());
    }

    private void assertCancelado(TurnoRegistrado cancelado) {
        assertNotNull(cancelado);
        assertEquals("CANCELADO", cancelado.estado().name());
        assertEquals(original.id(), cancelado.id());
        assertEquals(original.pacienteId(), cancelado.pacienteId());
        assertEquals(original.profesionalId(), cancelado.profesionalId());
        assertEquals(original.sillonId(), cancelado.sillonId());
        assertEquals(original.prestacionId(), cancelado.prestacionId());
        assertEquals(original.inicio(), cancelado.inicio());
        assertEquals(original.fin(), cancelado.fin());
        assertEquals(original.duracionMinutos(), cancelado.duracionMinutos());
        assertNotEquals(original.version(), cancelado.version());
    }

    private void assertOriginalConfirmado() {
        assertEquals(original, nuevaTransaccion.execute(transaccion -> consultar(original)));
    }

    private Object usarHorario(Operacion operacion) {
        return switch (operacion) {
            case ALTA -> alta(datos, INICIO, 30);
            case BLOQUEO_PROFESIONAL -> bloqueos.crear(RECEPCION, new RecursosBloqueo(datos.profesional(), null),
                    new PeriodoBloqueo(INICIO, original.fin()));
            case BLOQUEO_SILLON -> bloqueos.crear(RECEPCION, new RecursosBloqueo(null, datos.sillon()),
                    new PeriodoBloqueo(INICIO, original.fin()));
        };
    }

    private RecursosBloqueo retenerRecursos() {
        var seleccion = new RecursosBloqueo(datos.profesional(), datos.sillon());
        locks.adquirir(List.of(seleccion));
        return seleccion;
    }

    private static Carrera competir(Supplier<?> primera, Supplier<?> segunda, boolean revertir,
                                    Consumer<Object> mientrasEspera) throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var pid = new CompletableFuture<Integer>();
            var pendiente = transacciones.execute(transaccion -> {
                var resultado = primera.get();
                var tarea = executor.submit(() -> transacciones.execute(otraTransaccion -> {
                    pid.complete(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
                    return segunda.get();
                }));
                int pidSegundo = assertDoesNotThrow(() -> pid.get(5, TimeUnit.SECONDS));
                assertDoesNotThrow(() -> esperarBloqueo(tarea, pidSegundo));
                mientrasEspera.accept(resultado);
                if (revertir) transaccion.setRollbackOnly();
                return new Pendiente(resultado, tarea);
            });
            assertNotNull(pendiente);
            try {
                return new Carrera(pendiente.primera(), pendiente.segunda().get(10, TimeUnit.SECONDS), null);
            } catch (ExecutionException error) {
                return new Carrera(pendiente.primera(), null, error.getCause());
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

    private static TurnoRegistrado cancelar(Identidad identidad, TurnoRegistrado turno) {
        return agenda.cancelar(identidad, solicitud(turno));
    }

    private static SolicitudCancelacion solicitud(TurnoRegistrado turno) {
        return new SolicitudCancelacion(turno.id(), turno.version());
    }

    private static TurnoRegistrado mover(TurnoRegistrado turno, Instant inicio) {
        return agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                turno.id(), turno.version(), inicio, turno.profesionalId(), turno.sillonId()));
    }

    private static TurnoRegistrado consultar(TurnoRegistrado turno) {
        return agenda.consultarAgendaSillon(RECEPCION, turno.sillonId(), turno.inicio(), turno.fin())
                .stream().filter(vista -> vista.id().equals(turno.id())).findFirst().orElseThrow();
    }

    private static TurnoRegistrado alta(Datos datos, Instant inicio, Integer duracion) {
        return agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(),
                datos.prestacion(), inicio, duracion));
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
    private record Carrera(Object primera, Object segunda, Throwable error) {}

    private enum Operacion {
        ALTA(ConflictoAgendaException.class),
        BLOQUEO_PROFESIONAL(TurnoOcupanteException.class),
        BLOQUEO_SILLON(TurnoOcupanteException.class);

        private final Class<? extends RuntimeException> conflicto;

        Operacion(Class<? extends RuntimeException> conflicto) {
            this.conflicto = conflicto;
        }
    }
}
