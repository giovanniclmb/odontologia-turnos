package ar.edu.utn.odontologia.agenda;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudReprogramacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.agenda.persistencia.TokensCancelacionPersistidos;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Pruebas de integracion de C-18 contra PostgreSQL real (Testcontainers).
 * Cubre persistencia del token, emision, validacion, consumo atomico y concurrencia.
 */
class CancelacionPorEnlaceServiceIT {
    private static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    // El turno por defecto arranca cinco dias despues de ahora: dentro de la ventana para emitir token.
    private static final long CINCO_DIAS_SEGUNDOS = 5L * 24 * 3600;
    private static final long CUARENTAY_OCHO_HORAS_SEGUNDOS = 48L * 3600;
    private static final Instant INICIO = BASE.plusSeconds(CINCO_DIAS_SEGUNDOS);
    private static final AtomicInstant AHORA = new AtomicInstant(BASE);
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION, null);

    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ServicioCancelacionPorEnlace cancelacion;
    private static TokensCancelacionPersistidos tokens;
    private static ServicioPacientes pacientes;
    private static ServicioRecursosAgenda recursos;
    private static JdbcTemplate jdbc;

    private Datos datos;
    private TurnoRegistrado turno;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojCancelacionPruebas", Clock.class,
                    () -> reloj(ZoneOffset.UTC), bean -> bean.setPrimary(true));
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        cancelacion = contexto.getBean(ServicioCancelacionPorEnlace.class);
        tokens = contexto.getBean(TokensCancelacionPersistidos.class);
        pacientes = contexto.getBean(ServicioPacientes.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
        jdbc = contexto.getBean(JdbcTemplate.class);
        contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(RECEPCION, 10);
    }

    @AfterAll
    static void cerrar() {
        try {
            if (contexto != null) {
                contexto.close();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @BeforeEach
    void preparar() {
        AHORA.set(BASE);
        datos = nuevosDatos();
        turno = alta(datos, INICIO, 30);
    }

    /* ==================== Task 1: persistencia del token ==================== */

    @Test
    void migracionCreaTablaTokenYGuardaSoloHash() {
        var existe = jdbc.queryForObject(
                "select exists (select 1 from information_schema.tables "
                        + "where table_schema = 'odontologia' and table_name = 'token_cancelacion')",
                Boolean.class);
        assertTrue(existe, "La migracion V13 debe crear la tabla de tokens");

        var crudo = emitirPara(turno, AHORA.get());
        assertNotNull(crudo, "Debe emitirse token para turno lejano");

        var hashDb = jdbc.queryForObject(
                "select hash_token from odontologia.token_cancelacion where turno_id = ?",
                String.class, turno.id());
        assertEquals(hash(crudo), hashDb, "Debe persistirse solo el hash SHA-256");
        assertNotEquals(crudo, hashDb, "El token en claro no debe persistirse");
        var enClaro = jdbc.queryForObject(
                "select count(*) from odontologia.token_cancelacion where hash_token = ?",
                Long.class, crudo);
        assertEquals(0L, enClaro, "No debe almacenarse el token en claro");
    }

    @Test
    void accesoPorHashYPorTurnoConBloqueoSinDuplicar() {
        var crudo = emitirPara(turno, AHORA.get());
        var hash = hash(crudo);

        var encontrado = tokens.buscarPorHash(hash);
        assertTrue(encontrado.isPresent());
        assertEquals(turno.id(), encontrado.get().turnoId());
        assertFalse(encontrado.get().consumido());

        var bloqueado = tokens.bloquearPorHash(hash);
        assertTrue(bloqueado.isPresent());

        var porTurno = tokens.bloquearPorTurno(turno.id());
        assertTrue(porTurno.isPresent());
        assertEquals(hash, porTurno.get().hash());

        // Una segunda emisión para el mismo turno invalida la anterior: nunca hay dos filas activas.
        var segundo = emitirPara(turno, AHORA.get());
        assertNotNull(segundo);
        assertNotEquals(crudo, segundo, "La reemision genera un token distinto");
        var cantidad = jdbc.queryForObject(
                "select count(*) from odontologia.token_cancelacion where turno_id = ?",
                Long.class, turno.id());
        assertEquals(1L, cantidad, "No debe duplicarse el registro del turno");
        assertFalse(tokens.buscarPorHash(hash).isPresent(), "El token anterior queda invalidado");
        assertTrue(tokens.buscarPorHash(hash(segundo)).isPresent());
    }

    /* ==================== Task 2: emision del token ==================== */

    @Test
    void emiteTokenSoloCuandoElInicioEsEstrictamentePosteriorAhoraMas48h() {
        var otro = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);

        // Corte de EMISION: exactamente ahora + 48h no emite (se requiere estrictamente posterior).
        var justo = cancelacion.emitir(otro.id(), versionDe(otro), BASE.plusSeconds(CUARENTAY_OCHO_HORAS_SEGUNDOS), BASE);
        assertNull(justo, "No se emite cuando quedan exactamente 48 horas");

        var dentro = cancelacion.emitir(otro.id(), versionDe(otro),
                BASE.plusSeconds(CUARENTAY_OCHO_HORAS_SEGUNDOS + 1), BASE);
        assertNotNull(dentro, "Se emite cuando queda un segundo mas de 48 horas");

        var fuera = cancelacion.emitir(otro.id(), versionDe(otro), BASE.plusSeconds(3600), BASE);
        assertNull(fuera, "No se emite cuando quedan menos de 48 horas");
    }

    @Test
    void noEmiteTokenCuandoLaVentanaEstaCerrada() {
        var proximo = alta(nuevosDatos(), BASE.plusSeconds(3600), 30);
        var sinToken = cancelacion.emitir(proximo.id(), versionDe(proximo), proximo.inicio(), BASE);
        assertNull(sinToken, "No se emite token si el inicio esta dentro de las 48 horas");
    }

    @Test
    void reprogramacionInvalidaTokenPrevioYReemiteSiCumpleVentana() {
        var crudoAntiguo = emitirPara(turno, AHORA.get());
        assertNotNull(crudoAntiguo);

        var destino = INICIO.plusSeconds(3600);
        var movido = agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                turno.id(), turno.version(), destino, turno.profesionalId(), turno.sillonId()));

        assertNotNull(movido);
        assertEquals(destino, movido.inicio());

        // El token anterior ya no es valido (version desactualizada / eliminado).
        assertThrows(TokenCancelacionInvalidoException.class,
                () -> cancelacion.cancelar(crudoAntiguo));
        assertEquals(movido, consultar(movido));

        // Se reemite uno nuevo porque el nuevo inicio cumple la ventana.
        var nuevo = emitirPara(movido, AHORA.get());
        assertNotNull(nuevo);
        assertNotEquals(crudoAntiguo, nuevo);
    }

    @Test
    void reprogramacionAVentanaCerradaInvalidaYNoReemite() {
        var crudoAntiguo = emitirPara(turno, AHORA.get());
        assertNotNull(crudoAntiguo);

        var destino = BASE.plusSeconds(3600); // dentro de las 48 horas
        var movido = agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                turno.id(), turno.version(), destino, turno.profesionalId(), turno.sillonId()));

        assertNotNull(movido);
        var sinToken = cancelacion.emitir(movido.id(), versionDe(movido), movido.inicio(), BASE);
        assertNull(sinToken, "No se reemite si el turno reprogramado queda dentro de las 48 horas");
        assertThrows(TokenCancelacionInvalidoException.class, () -> cancelacion.cancelar(crudoAntiguo));
    }

    /* ==================== Task 3: validacion, consumo y cancelacion ==================== */

    @Test
    void validoRechazaCadaCondicionDeRechazo() {
        Instant ahora = BASE;

        // Token valido.
        var valido = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudoValido = cancelacion.emitir(valido.id(), versionDe(valido), valido.inicio(), ahora);
        assertTrue(cancelacion.esValido(crudoValido, ahora), "Un token valido debe ser valido");

        // Token desconocido.
        assertFalse(cancelacion.esValido("token-inexistente", ahora), "Token desconocido no es valido");

        // Token consumido.
        var consumible = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudoCons = cancelacion.emitir(consumible.id(), versionDe(consumible), consumible.inicio(), ahora);
        cancelarPorToken(crudoCons);
        assertFalse(cancelacion.esValido(crudoCons, ahora), "Token consumido no es valido");

        // Version desactualizada por reprogramacion.
        var conVersion = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudoVer = cancelacion.emitir(conVersion.id(), versionDe(conVersion), conVersion.inicio(), ahora);
        agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                conVersion.id(), conVersion.version(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS + 3600),
                conVersion.profesionalId(), conVersion.sillonId()));
        assertFalse(cancelacion.esValido(crudoVer, ahora), "Token con version desactualizada no es valido");

        // Turno cancelado.
        var cancelable = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudoCancel = cancelacion.emitir(cancelable.id(), versionDe(cancelable), cancelable.inicio(), ahora);
        cancelarPorToken(crudoCancel);
        assertFalse(cancelacion.esValido(crudoCancel, ahora), "Token de turno cancelado no es valido");

        // Corte de 48 horas no cumplido: ahora dentro de las 48 horas previas al inicio.
        var cercano = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudoCercano = cancelacion.emitir(cercano.id(), versionDe(cercano), cercano.inicio(), ahora);
        var fueraDePlazo = cercano.inicio().minusSeconds(100);
        assertFalse(cancelacion.esValido(crudoCercano, fueraDePlazo), "Token fuera de plazo no es valido");

        // Turno ya comenzo: ahora igual al inicio.
        var porEmpezar = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudoEmpezar = cancelacion.emitir(porEmpezar.id(), versionDe(porEmpezar), porEmpezar.inicio(), ahora);
        assertFalse(cancelacion.esValido(crudoEmpezar, porEmpezar.inicio()), "Token de turno ya comenzado no es valido");
    }

    @Test
    void cancelarPorTokenPasaTurnoACanceladoLiberaHorarioYConsumeToken() {
        var crudo = emitirPara(turno, AHORA.get());
        var antes = turno.version();

        var cancelado = cancelarPorToken(crudo);

        assertEquals("CANCELADO", cancelado.estado().name());
        assertEquals(turno.id(), cancelado.id());
        assertNotEquals(antes, cancelado.version(), "La version debe incrementarse");
        assertEquals(cancelado, consultar(cancelado));

        // El token se consumo.
        var registro = jdbc.queryForObject(
                "select consumido from odontologia.token_cancelacion where turno_id = ?",
                Boolean.class, turno.id());
        assertTrue(registro, "El token debe estar consumido");

        // El horario queda liberado: se puede crear otro turno en el mismo lugar y hora.
        var reemplazo = alta(datos, INICIO, 30);
        assertNotNull(reemplazo);
        assertEquals(2, agenda.consultarAgendaSillon(RECEPCION, datos.sillon(), INICIO, turno.fin()).size());
    }

    @Test
    void cancelarPorTokenFueraDePlazoNoMutNiConservaOcupacion() {
        var lejano = alta(nuevosDatos(), BASE.plusSeconds(CINCO_DIAS_SEGUNDOS), 30);
        var crudo = cancelacion.emitir(lejano.id(), versionDe(lejano), lejano.inicio(), BASE);
        assertNotNull(crudo);

        AHORA.set(lejano.inicio().minusSeconds(100)); // dentro de las 48 horas
        var error = assertThrows(TokenCancelacionInvalidoException.class,
                () -> cancelarPorToken(crudo));
        assertInstanceOf(TokenCancelacionInvalidoException.class, error);
        assertEquals("PENDIENTE", consultar(lejano).estado().name(), "El turno no se cancela fuera de plazo");
    }

    @Test
    void unSoloExitoPorTokenAntePeticionesConcurrentes() throws Exception {
        var crudo = emitirPara(turno, AHORA.get());
        var executor = Executors.newFixedThreadPool(2);
        try {
            var f1 = CompletableFuture.supplyAsync(() -> intento(crudo), executor);
            var f2 = CompletableFuture.supplyAsync(() -> intento(crudo), executor);
            CompletableFuture.allOf(f1, f2).join();
            Object r1 = f1.join();
            Object r2 = f2.join();

            int exitos = (r1 instanceof TurnoRegistrado ? 1 : 0) + (r2 instanceof TurnoRegistrado ? 1 : 0);
            assertEquals(1, exitos, "Solo una cancelacion puede tener exito");

            assertEquals(EstadoTurno.CANCELADO, consultar(turno).estado());
            assertEquals(1L, jdbc.queryForObject(
                    "select version from odontologia.turno where id = ?", Long.class, turno.id()),
            "La version se incrementa una sola vez");
        } finally {
            executor.shutdownNow();
        }
    }

    /* ==================== helpers ==================== */

    private TurnoRegistrado cancelarPorToken(String token) {
        return agenda.cancelarPorToken(token);
    }

    private Object intento(String token) {
        try {
            return cancelarPorToken(token);
        } catch (Throwable error) {
            return error;
        }
    }

    private String emitirPara(TurnoRegistrado t, Instant ahora) {
        long version = jdbc.queryForObject("select version from odontologia.turno where id = ?", Long.class, t.id());
        return cancelacion.emitir(t.id(), version, t.inicio(), ahora);
    }

    private long versionDe(TurnoRegistrado t) {
        return jdbc.queryForObject("select version from odontologia.turno where id = ?", Long.class, t.id());
    }

    private static String hash(String crudo) {
        return ServicioCancelacionPorEnlace.hash(crudo);
    }

    private static TurnoRegistrado consultar(TurnoRegistrado t) {
        return agenda.consultarAgendaSillon(RECEPCION, t.sillonId(), t.inicio(), t.fin())
                .stream().filter(vista -> vista.id().equals(t.id())).findFirst().orElseThrow();
    }

    private static TurnoRegistrado alta(Datos d, Instant inicio, Integer duracion) {
        return agenda.crear(RECEPCION, new SolicitudAlta(d.paciente(), d.profesional(), d.sillon(),
                d.prestacion(), inicio, duracion));
    }

    private static Datos nuevosDatos() {
        var paciente = pacientes.crear(RECEPCION,
                new CrearPaciente(codigo(), "Nombre ficticio", "Apellido ficticio", "turno@example.invalid", null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, codigo(), true).id(),
                recursos.altaSillon(RECEPCION, codigo(), true).id(),
                recursos.altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

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

    private static final class AtomicInstant {
        private final java.util.concurrent.atomic.AtomicReference<Instant> ref;
        AtomicInstant(Instant inicial) { this.ref = new java.util.concurrent.atomic.AtomicReference<>(inicial); }
        void set(Instant valor) { this.ref.set(valor); }
        Instant get() { return this.ref.get(); }
    }
}
