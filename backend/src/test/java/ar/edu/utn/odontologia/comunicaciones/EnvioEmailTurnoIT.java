package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudReprogramacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.agenda.TurnoProgramado;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Recorrido desde la agenda hasta el proveedor, con PostgreSQL real, el adaptador real y un servidor local en
 * lugar de Resend: no sale ningún correo.
 */
@ExtendWith(OutputCaptureExtension.class)
class EnvioEmailTurnoIT {
    private static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    private static final Instant INICIO = Instant.parse("2040-04-10T10:00:00Z");
    private static final String URL_BASE = "https://turnos.example.invalid/cancelar";
    private static final String ANA = "ana@example.invalid";
    private static final String BEA = "bea@example.invalid";
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Identidad RECEPCION =
            new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);

    private static final List<JsonNode> enviados = new CopyOnWriteArrayList<>();
    private static final AtomicInteger estadoProveedor = new AtomicInteger(200);
    private static final AtomicBoolean transporteRoto = new AtomicBoolean();

    private static HttpServer proveedor;
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ConfiguracionEmail configuracion;
    private static ThreadPoolTaskExecutor ejecutor;
    private static JdbcTemplate jdbc;
    private static TransactionTemplate transacciones;

    @BeforeAll
    static void iniciar() throws Exception {
        proveedor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proveedor.createContext("/", intercambio -> {
            var cuerpo = intercambio.getRequestBody().readAllBytes();
            enviados.add(JSON.readTree(new String(cuerpo, StandardCharsets.UTF_8)));
            // El rechazo repite el pedido, como podría hacerlo un proveedor.
            intercambio.sendResponseHeaders(estadoProveedor.get(), cuerpo.length);
            try (var salida = intercambio.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        proveedor.start();
        var endpoint = "http://127.0.0.1:" + proveedor.getAddress().getPort() + "/";
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            var fuentes = ctx.getEnvironment().getPropertySources();
            fuentes.addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            fuentes.addFirst(new MapPropertySource("email-ficticio", Map.of(
                    "email.resend-api-key", "clave-ficticia", "email.from", "consultorio@example.invalid",
                    "email.test-allowlist", ANA + "," + BEA, "email.cancelacion-url-base", URL_BASE,
                    "email.max-retries", "1")));
            var generico = (GenericApplicationContext) ctx;
            generico.registerBean("relojEnvioPruebas", Clock.class,
                    () -> Clock.fixed(BASE, ZoneOffset.UTC), bean -> bean.setPrimary(true));
            generico.registerBean("correoPruebas", CorreoSalida.class, () -> {
                var propiedades = generico.getBean(ConfiguracionEmail.class);
                var adaptador = new AdaptadorResend(propiedades, new AllowlistEmail(propiedades.testAllowlist()),
                        generico.getBean(EmailsEnviados.class), HttpClient.newHttpClient(), endpoint);
                return solicitud -> {
                    if (transporteRoto.get()) {
                        throw new IllegalStateException("falla " + solicitud.destinatario() + solicitud.cuerpo());
                    }
                    return adaptador.enviar(solicitud);
                };
            }, bean -> bean.setPrimary(true));
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        configuracion = contexto.getBean(ConfiguracionEmail.class);
        ejecutor = contexto.getBean(EnvioEmailAsincronoConfig.EJECUTOR, ThreadPoolTaskExecutor.class);
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
            if (proveedor != null) proveedor.stop(0);
        }
    }

    @BeforeEach
    void preparar() throws Exception {
        esperarEnvios();
        enviados.clear();
        estadoProveedor.set(200);
        transporteRoto.set(false);
        configuracion.setCancelacionUrlBase(URL_BASE);
    }

    @Test
    void elAltaEnviaAlPacienteUnEnlaceQueCancelaSuTurno() throws Exception {
        var turno = alta(nuevosDatos("Ana", ANA), INICIO);
        esperarEnvios();

        assertEquals(1, enviados.size());
        var email = enviados.getFirst();
        assertEquals(ANA, email.get("to").get(0).asString());
        assertEquals("Tu turno en el consultorio", email.get("subject").asString());
        var texto = email.get("text").asString();
        assertTrue(texto.contains("Hola, Ana:"));
        assertTrue(texto.contains("10/04/2040 a las 07:00"));
        assertFalse(texto.contains("Apellido"), "Solo se informa el nombre");
        assertFalse(texto.contains(turno.id().toString()));
        assertFalse(texto.contains(turno.profesionalId().toString()));
        assertEquals(turno.id(), agenda.cancelarPorToken(token(texto)).id());
        assertEquals(List.of("turno-alta:sent"), envios(turno));
    }

    @Test
    void cadaPacienteRecibeSoloSusDatosYSuEnlace() throws Exception {
        var deAna = alta(nuevosDatos("Ana", ANA), INICIO);
        var deBea = alta(nuevosDatos("Bea", BEA), INICIO);
        esperarEnvios();

        assertEquals(2, enviados.size());
        var paraAna = textoPara(ANA);
        var paraBea = textoPara(BEA);
        assertTrue(paraAna.startsWith("Hola, Ana:"));
        assertTrue(paraBea.startsWith("Hola, Bea:"));
        assertFalse(paraAna.contains("Hola, Bea"));
        assertFalse(paraBea.contains("Hola, Ana"));
        assertEquals(deAna.id(), agenda.cancelarPorToken(token(paraAna)).id());
        assertEquals(deBea.id(), agenda.cancelarPorToken(token(paraBea)).id());
    }

    @Test
    void cadaReprogramacionEnviaSuPropioEmailConUnEnlaceNuevo() throws Exception {
        var turno = alta(nuevosDatos("Ana", ANA), INICIO);
        esperarEnvios();
        var primero = mover(turno, INICIO.plus(Duration.ofHours(2)));
        esperarEnvios();
        mover(primero, INICIO.plus(Duration.ofHours(4)));
        esperarEnvios();

        assertEquals(3, enviados.size());
        var ultimo = enviados.getLast();
        assertEquals("Tu turno fue reprogramado", ultimo.get("subject").asString());
        assertTrue(ultimo.get("text").asString().contains("10/04/2040 a las 11:00"));
        assertNotEquals(token(enviados.get(1).get("text").asString()), token(ultimo.get("text").asString()));
        assertEquals(List.of("turno-alta:sent", "turno-reprogramacion-v1:sent", "turno-reprogramacion-v2:sent"),
                envios(turno));
    }

    @Test
    void conMenosDe48HorasEnviaSinEnlace() throws Exception {
        configuracion.setCancelacionUrlBase("");

        alta(nuevosDatos("Ana", ANA), BASE.plus(Duration.ofHours(30)));
        esperarEnvios();

        assertEquals(1, enviados.size());
        var texto = enviados.getFirst().get("text").asString();
        assertTrue(texto.contains("ya no puede cancelarse por enlace"));
        assertFalse(texto.contains("#"));
    }

    @Test
    void noEnviaSiLaTransaccionRevierteNiAlConfirmar() throws Exception {
        var datos = nuevosDatos("Ana", ANA);
        transacciones.executeWithoutResult(transaccion -> {
            alta(datos, INICIO);
            transaccion.setRollbackOnly();
        });
        var turno = alta(datos, INICIO);
        esperarEnvios();
        assertEquals(1, enviados.size(), "El alta revertida no debe enviar nada");
        enviados.clear();

        agenda.confirmar(RECEPCION, turno.id(), turno.version());
        esperarEnvios();

        assertTrue(enviados.isEmpty());
        assertEquals(List.of("turno-alta:sent"), envios(turno));
    }

    @Test
    void descartaElMensajeDeUnTurnoCanceladoAntesDeEnviar() throws Exception {
        var datos = nuevosDatos("Ana", ANA);
        TurnoRegistrado turno;
        try (var retenido = retener()) {
            turno = alta(datos, INICIO);
            agenda.cancelar(RECEPCION, new SolicitudCancelacion(turno.id(), turno.version()));
        }
        esperarEnvios();

        assertTrue(enviados.isEmpty());
        assertTrue(envios(turno).isEmpty());
    }

    @Test
    void trasDosCambiosSinEnviarSoloSaleElUltimo() throws Exception {
        TurnoRegistrado turno;
        try (var retenido = retener()) {
            turno = alta(nuevosDatos("Ana", ANA), INICIO);
            mover(mover(turno, INICIO.plus(Duration.ofHours(2))), INICIO.plus(Duration.ofHours(4)));
        }
        esperarEnvios();

        assertEquals(1, enviados.size());
        assertEquals(List.of("turno-reprogramacion-v2:sent"), envios(turno));
    }

    @Test
    void confirmarAntesDeEnviarNoDescartaElMensaje() throws Exception {
        try (var retenido = retener()) {
            var turno = alta(nuevosDatos("Ana", ANA), INICIO);
            agenda.confirmar(RECEPCION, turno.id(), turno.version());
        }
        esperarEnvios();

        assertEquals(1, enviados.size());
    }

    @Test
    void elMismoAvisoProcesadoDosVecesSeEnviaUnaSolaVez() throws Exception {
        var turno = alta(nuevosDatos("Ana", ANA), INICIO);
        esperarEnvios();
        var token = token(enviados.getFirst().get("text").asString());

        transacciones.executeWithoutResult(transaccion -> contexto.publishEvent(new TurnoProgramado(
                turno.id(), 0, turno.inicio(), TurnoProgramado.Motivo.ALTA, token)));
        esperarEnvios();

        assertEquals(1, enviados.size());
        assertEquals(List.of("turno-alta:sent"), envios(turno));
    }

    @Test
    void unProveedorQueRechazaDejaElFalloSinTocarElTurnoNiExponerDatos(CapturedOutput salida) throws Exception {
        estadoProveedor.set(500);

        var turno = alta(nuevosDatos("Ana", ANA), INICIO);
        esperarEnvios();

        assertEquals(2, enviados.size(), "Un intento y un reintento");
        assertEquals(List.of("turno-alta:failed"), envios(turno));
        assertEquals("HTTP 500", jdbc.queryForObject(
                "select error from odontologia.email_envio where turno_id = ?", String.class, turno.id()));
        assertEquals("PENDIENTE", jdbc.queryForObject(
                "select estado from odontologia.turno where id = ?", String.class, turno.id()));
        assertTrue(salida.getAll().contains("Envío fallido del turno " + turno.id() + ": HTTP 500"));
        assertSinDatos(salida, token(enviados.getFirst().get("text").asString()));
    }

    @Test
    void unaFallaInesperadaDelTransporteNoAfectaElAltaNiRegistraDatos(CapturedOutput salida) throws Exception {
        transporteRoto.set(true);

        var turno = assertDoesNotThrow(() -> alta(nuevosDatos("Ana", ANA), INICIO));
        esperarEnvios();

        assertNotNull(turno);
        assertTrue(salida.getAll().contains("No se pudo enviar el email del turno " + turno.id()));
        assertSinDatos(salida, "turnos.example.invalid");
    }

    @Test
    void sinUrlBaseNoEnviaElMensajeQueNecesitaEnlace(CapturedOutput salida) throws Exception {
        configuracion.setCancelacionUrlBase("");

        var turno = alta(nuevosDatos("Ana", ANA), INICIO);
        esperarEnvios();

        assertTrue(enviados.isEmpty());
        assertTrue(envios(turno).isEmpty());
        assertTrue(salida.getAll().contains("EMAIL_CANCELACION_URL_BASE"));
        assertSinDatos(salida, "Hola, Ana");
    }

    @Test
    void unDestinatarioFueraDeLaListaQuedaRechazadoSinContactarAlProveedor() throws Exception {
        var turno = alta(nuevosDatos("Carla", "carla@example.invalid"), INICIO);
        esperarEnvios();

        assertTrue(enviados.isEmpty());
        assertEquals(List.of("turno-alta:failed"), envios(turno));
    }

    private static void assertSinDatos(CapturedOutput salida, String secreto) {
        assertFalse(salida.getAll().contains(ANA), "El registro no debe incluir la dirección");
        assertFalse(salida.getAll().contains("Hola, Ana"), "El registro no debe incluir el mensaje");
        assertFalse(salida.getAll().contains(secreto), "El registro no debe incluir el enlace ni el token");
    }

    /** Ocupa el único hilo de envío hasta cerrar, para modificar el turno antes de que salga su aviso. */
    private static AutoCloseable retener() {
        var liberar = new CountDownLatch(1);
        ejecutor.execute(() -> {
            try {
                liberar.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        });
        return liberar::countDown;
    }

    /** El ejecutor tiene un hilo y respeta el orden: una tarea vacía termina después de los envíos previos. */
    private static void esperarEnvios() throws Exception {
        ejecutor.submit(() -> {}).get(20, TimeUnit.SECONDS);
    }

    private static String token(String texto) {
        var encontrado = Pattern.compile(Pattern.quote(URL_BASE) + "#([A-Za-z0-9_-]{43})").matcher(texto);
        assertTrue(encontrado.find(), "El mensaje debe incluir el enlace");
        return encontrado.group(1);
    }

    private static String textoPara(String direccion) {
        return enviados.stream().filter(email -> email.get("to").get(0).asString().equals(direccion))
                .findFirst().orElseThrow().get("text").asString();
    }

    private static List<String> envios(TurnoRegistrado turno) {
        return jdbc.queryForList("""
                select tipo || ':' || estado from odontologia.email_envio where turno_id = ? order by tipo
                """, String.class, turno.id());
    }

    private static TurnoRegistrado alta(Datos datos, Instant inicio) {
        return agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(),
                datos.prestacion(), inicio, 30));
    }

    private static TurnoRegistrado mover(TurnoRegistrado turno, Instant inicio) {
        return agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                turno.id(), turno.version(), inicio, turno.profesionalId(), turno.sillonId()));
    }

    private static Datos nuevosDatos(String nombre, String email) {
        var recursos = contexto.getBean(ServicioRecursosAgenda.class);
        var paciente = contexto.getBean(ServicioPacientes.class).crear(RECEPCION,
                new CrearPaciente(codigo(), nombre, "Apellido ficticio", email, null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, codigo(), true).id(),
                recursos.altaSillon(RECEPCION, codigo(), true).id(),
                recursos.altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

    private record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {}
}
