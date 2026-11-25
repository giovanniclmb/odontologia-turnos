package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.comunicaciones.AdaptadorResend;
import ar.edu.utn.odontologia.comunicaciones.AllowlistEmail;
import ar.edu.utn.odontologia.comunicaciones.ConfiguracionEmail;
import ar.edu.utn.odontologia.comunicaciones.CorreoSalida;
import ar.edu.utn.odontologia.comunicaciones.RecordatorioTurnoPorEmail;
import ar.edu.utn.odontologia.comunicaciones.ResultadoEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Prueba operativa manual de C-20: prepara un turno ficticio en una base temporal y arranca la aplicación con el
 * recordatorio activado para que lo envíe sola. Su nombre evita la selección automática; exige selección explícita
 * y opt-in de entorno. Nunca imprime la dirección ni la credencial. Ante un resultado incierto, no repetir sin
 * revisar el panel de Resend.
 *
 * <p>{@code ENSAYO} usa un servidor local; {@code PREFLIGHT_ONLY} solo valida la configuración;
 * {@code SEND_ONE_C20_TEST} envía exactamente un correo real.
 */
@EnabledIfEnvironmentVariable(named = "C20_REAL_EMAIL_CONFIRM",
        matches = "ENSAYO|PREFLIGHT_ONLY|SEND_ONE_C20_TEST")
class RecordatorioRealManual {
    private static final String ENSAYO = "ENSAYO";
    private static final String DESTINO_ENSAYO = "ensayo@example.invalid";
    private static final String ENVIOS = """
            select estado || ':' || intentos from odontologia.email_envio
            where turno_id = ? and tipo like 'turno-recordatorio-%'
            """;

    @Test
    void laAplicacionActivadaEnviaUnUnicoRecordatorioDeUnTurnoFicticio() throws Exception {
        String modo = System.getenv("C20_REAL_EMAIL_CONFIRM");
        if (ENSAYO.equals(modo)) {
            ensayar();
            return;
        }
        String destinatario = validarConfiguracionReal();
        if ("PREFLIGHT_ONLY".equals(modo)) {
            return;
        }
        recorrer(destinatario, Map.of(), null);
    }

    /** Mismo recorrido que el envío real, con un servidor local en lugar del proveedor. */
    private static void ensayar() throws Exception {
        var recibidos = new CopyOnWriteArrayList<String>();
        HttpServer proveedor = servidorLocal(recibidos);
        String endpoint = "http://127.0.0.1:" + proveedor.getAddress().getPort() + "/";
        Map<String, Object> ficticia = Map.of("email.resend-api-key", "clave-ficticia",
                "email.from", "consultorio@example.invalid", "email.test-allowlist", DESTINO_ENSAYO,
                "email.max-retries", "0");
        try {
            recorrer(DESTINO_ENSAYO, ficticia, endpoint);
            assertEquals(1, recibidos.size());
            assertTrue(recibidos.getFirst().contains("Recordatorio de tu turno"));
            assertFalse(recibidos.getFirst().contains("http"), "El recordatorio no lleva enlaces");
        } finally {
            proveedor.stop(0);
        }
    }

    private static void recorrer(String destinatario, Map<String, Object> adicionales, String endpointLocal)
            throws Exception {
        try (var db = PostgresFixture.start()) {
            var turno = prepararTurnoFicticio(db, destinatario);
            var activada = new HashMap<>(adicionales);
            activada.put("email.recordatorio.activo", "true");
            activada.put("server.port", "0");
            try (var contexto = abrir(db, activada, aplicacion -> {
                if (endpointLocal != null) {
                    aplicacion.registerBean("correoEnsayo", CorreoSalida.class, () -> {
                        var configuracion = aplicacion.getBean(ConfiguracionEmail.class);
                        return new AdaptadorResend(configuracion,
                                new AllowlistEmail(configuracion.testAllowlist()),
                                aplicacion.getBean(EmailsEnviados.class), HttpClient.newHttpClient(), endpointLocal);
                    }, bean -> bean.setPrimary(true));
                }
            })) {
                var jdbc = contexto.getBean(JdbcTemplate.class);
                esperarResultado(jdbc, turno);
                // Una revisión posterior no debe repetir el envío.
                contexto.getBean(RecordatorioTurnoPorEmail.class).enviarPendientes();
                assertEquals(List.of("sent:1"), jdbc.queryForList(ENVIOS, String.class, turno),
                        "No volver a ejecutar sin revisar el panel de Resend");
            }
        }
    }

    /**
     * Da de alta, con el reloj dos días atrás, un turno que hoy está a menos de 24 horas. Esta aplicación auxiliar
     * no envía correos: así tampoco sale el email de alta.
     */
    private static UUID prepararTurnoFicticio(PostgresFixture db, String destinatario) {
        var ahora = Instant.now();
        try (var contexto = abrir(db, Map.of(), aplicacion -> {
            aplicacion.registerBean("relojDePreparacion", Clock.class,
                    () -> Clock.fixed(ahora.minus(Duration.ofDays(2)), ZoneOffset.UTC),
                    bean -> bean.setPrimary(true));
            aplicacion.registerBean("correoSinSalida", CorreoSalida.class,
                    () -> solicitud -> new ResultadoEnvio(true, 0, null, null), bean -> bean.setPrimary(true));
        }, "--spring.main.web-application-type=none")) {
            var recepcion = new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);
            contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(recepcion, 10);
            var recursos = contexto.getBean(ServicioRecursosAgenda.class);
            var paciente = contexto.getBean(ServicioPacientes.class).crear(recepcion, new CrearPaciente(
                    "PAC-DEMO-C20", "Paciente de prueba", "Ficticio", destinatario, null)).id();
            var inicio = ahora.plus(Duration.ofHours(20)).truncatedTo(ChronoUnit.HOURS);
            return contexto.getBean(ServicioAgenda.class).crear(recepcion, new SolicitudAlta(paciente,
                    recursos.altaProfesional(recepcion, "PRO-DEMO-C20", true).id(),
                    recursos.altaSillon(recepcion, "SIL-DEMO-C20", true).id(),
                    recursos.altaPrestacion(recepcion, "CONTROL-DEMO-C20", 30).id(), inicio, null)).id();
        }
    }

    private static void esperarResultado(JdbcTemplate jdbc, UUID turno) throws InterruptedException {
        var limite = Instant.now().plusSeconds(60);
        while (Instant.now().isBefore(limite)) {
            var envios = jdbc.queryForList(ENVIOS, String.class, turno);
            if (!envios.isEmpty() && !envios.getFirst().startsWith("pending")) {
                return;
            }
            Thread.sleep(200);
        }
        fail("La revisión no registró un resultado en 60 segundos; revisar el panel de Resend antes de repetir");
    }

    private static String validarConfiguracionReal() {
        String destinatario = requerida("C20_REAL_EMAIL_TO");
        requerida("RESEND_API_KEY");
        requerida("EMAIL_FROM");
        if (!"YES".equals(System.getenv("C20_REAL_EMAIL_FROM_VERIFIED"))) {
            throw new IllegalStateException("falta confirmar el remitente con C20_REAL_EMAIL_FROM_VERIFIED=YES");
        }
        if (!"0".equals(requerida("EMAIL_MAX_RETRIES"))) {
            throw new IllegalStateException("EMAIL_MAX_RETRIES debe ser 0 para esta prueba");
        }
        if (destinatario.contains(",") || !requerida("EMAIL_TEST_ALLOWLIST").toLowerCase(Locale.ROOT)
                .equals(destinatario.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("EMAIL_TEST_ALLOWLIST debe contener solo el destinatario aprobado");
        }
        return destinatario;
    }

    private static String requerida(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("falta la variable local " + nombre);
        }
        return valor.strip();
    }

    private static ConfigurableApplicationContext abrir(PostgresFixture db, Map<String, Object> adicionales,
            Consumer<GenericApplicationContext> beans, String... argumentos) {
        var entorno = new StandardEnvironment();
        var propiedades = new HashMap<String, Object>(db.properties());
        propiedades.putAll(adicionales);
        entorno.getPropertySources().addFirst(new MapPropertySource("c20Temporal", propiedades));
        var todos = new ArrayList<>(List.of(argumentos));
        todos.add("--spring.config.location=classpath:/application.yaml");
        return new SpringApplicationBuilder(OdontologiaApplication.class)
                .environment(entorno)
                .initializers(contexto -> beans.accept((GenericApplicationContext) contexto))
                .run(todos.toArray(String[]::new));
    }

    private static HttpServer servidorLocal(List<String> recibidos) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            recibidos.add(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            intercambio.sendResponseHeaders(200, 2);
            try (var salida = intercambio.getResponseBody()) {
                salida.write("{}".getBytes(StandardCharsets.UTF_8));
            }
        });
        servidor.start();
        return servidor;
    }
}
