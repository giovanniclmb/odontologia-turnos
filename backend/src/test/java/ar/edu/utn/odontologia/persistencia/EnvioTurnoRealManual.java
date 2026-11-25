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
import ar.edu.utn.odontologia.comunicaciones.EnvioEmailAsincronoConfig;
import ar.edu.utn.odontologia.comunicaciones.MensajeTurno;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Prueba operativa manual de C-19: crea un turno ficticio en una base temporal y deja que la aplicación envíe su
 * email. Su nombre evita la selección automática; exige selección explícita y opt-in de entorno. Nunca imprime la
 * dirección, la credencial ni el enlace. Ante un resultado incierto, no repetir sin revisar el panel de Resend.
 *
 * <p>{@code ENSAYO} usa un servidor local; {@code PREFLIGHT_ONLY} solo valida la configuración;
 * {@code SEND_ONE_C19_TEST} envía exactamente un correo real.
 */
@EnabledIfEnvironmentVariable(named = "C19_REAL_EMAIL_CONFIRM",
        matches = "ENSAYO|PREFLIGHT_ONLY|SEND_ONE_C19_TEST")
class EnvioTurnoRealManual {
    private static final String ENSAYO = "ENSAYO";
    private static final String DESTINO_ENSAYO = "ensayo@example.invalid";
    private static final String URL_ENSAYO = "https://turnos.example.invalid/cancelar";

    @Test
    void elAltaDeUnTurnoFicticioEnviaUnUnicoEmail() throws Exception {
        String modo = System.getenv("C19_REAL_EMAIL_CONFIRM");
        if (ENSAYO.equals(modo)) {
            ensayar();
            return;
        }
        String destinatario = validarConfiguracionReal();
        if ("PREFLIGHT_ONLY".equals(modo)) {
            return;
        }
        try (var db = PostgresFixture.start(); var contexto = abrir(db, Map.of(), null)) {
            var turno = crearTurnoFicticio(contexto, destinatario);
            esperarEnvio(contexto);
            assertEquals(List.of("turno-alta:sent:1"), envios(contexto, turno),
                    "No volver a ejecutar sin revisar el panel de Resend");
        }
    }

    /** Mismo recorrido que el envío real, con un servidor local en lugar del proveedor. */
    private static void ensayar() throws Exception {
        var recibidos = new CopyOnWriteArrayList<String>();
        HttpServer proveedor = servidorLocal(recibidos);
        String endpoint = "http://127.0.0.1:" + proveedor.getAddress().getPort() + "/";
        Map<String, Object> ficticia = Map.of("email.resend-api-key", "clave-ficticia",
                "email.from", "consultorio@example.invalid", "email.test-allowlist", DESTINO_ENSAYO,
                "email.cancelacion-url-base", URL_ENSAYO, "email.max-retries", "0");
        try (var db = PostgresFixture.start(); var contexto = abrir(db, ficticia, endpoint)) {
            var turno = crearTurnoFicticio(contexto, DESTINO_ENSAYO);
            esperarEnvio(contexto);
            assertEquals(List.of("turno-alta:sent:1"), envios(contexto, turno));
            assertEquals(1, recibidos.size());
            assertTrue(recibidos.getFirst().contains(URL_ENSAYO + "#"), "El ensayo debe incluir un enlace");
        } finally {
            proveedor.stop(0);
        }
    }

    private static String validarConfiguracionReal() {
        String destinatario = requerida("C19_REAL_EMAIL_TO");
        requerida("RESEND_API_KEY");
        requerida("EMAIL_FROM");
        if (!"YES".equals(System.getenv("C19_REAL_EMAIL_FROM_VERIFIED"))) {
            throw new IllegalStateException("falta confirmar el remitente con C19_REAL_EMAIL_FROM_VERIFIED=YES");
        }
        if (!"0".equals(requerida("EMAIL_MAX_RETRIES"))) {
            throw new IllegalStateException("EMAIL_MAX_RETRIES debe ser 0 para esta prueba");
        }
        if (destinatario.contains(",") || !requerida("EMAIL_TEST_ALLOWLIST").toLowerCase(Locale.ROOT)
                .equals(destinatario.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("EMAIL_TEST_ALLOWLIST debe contener solo el destinatario aprobado");
        }
        if (MensajeTurno.enlace(requerida("EMAIL_CANCELACION_URL_BASE"), "x").isEmpty()) {
            throw new IllegalStateException("EMAIL_CANCELACION_URL_BASE no es una URL web válida");
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
                                                        String endpointLocal) {
        var entorno = new StandardEnvironment();
        var propiedades = new HashMap<String, Object>(db.properties());
        propiedades.putAll(adicionales);
        entorno.getPropertySources().addFirst(new MapPropertySource("c19Temporal", propiedades));
        var aplicacion = new SpringApplicationBuilder(OdontologiaApplication.class)
                .web(WebApplicationType.NONE).environment(entorno);
        if (endpointLocal != null) {
            aplicacion.initializers(ctx -> {
                var generico = (GenericApplicationContext) ctx;
                generico.registerBean("correoEnsayo", CorreoSalida.class, () -> {
                    var configuracion = generico.getBean(ConfiguracionEmail.class);
                    return new AdaptadorResend(configuracion, new AllowlistEmail(configuracion.testAllowlist()),
                            generico.getBean(EmailsEnviados.class), HttpClient.newHttpClient(), endpointLocal);
                }, bean -> bean.setPrimary(true));
            });
        }
        return aplicacion.run("--spring.config.location=classpath:/application.yaml");
    }

    private static UUID crearTurnoFicticio(ConfigurableApplicationContext contexto, String destinatario) {
        var recepcion = new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);
        contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(recepcion, 10);
        var recursos = contexto.getBean(ServicioRecursosAgenda.class);
        var paciente = contexto.getBean(ServicioPacientes.class).crear(recepcion, new CrearPaciente(
                "PAC-DEMO-C19", "Paciente de prueba", "Ficticio", destinatario, null)).id();
        var inicio = Instant.now().plus(Duration.ofDays(5)).truncatedTo(ChronoUnit.HOURS);
        return contexto.getBean(ServicioAgenda.class).crear(recepcion, new SolicitudAlta(paciente,
                recursos.altaProfesional(recepcion, "PRO-DEMO-C19", true).id(),
                recursos.altaSillon(recepcion, "SIL-DEMO-C19", true).id(),
                recursos.altaPrestacion(recepcion, "CONTROL-DEMO-C19", 30).id(), inicio, null)).id();
    }

    private static void esperarEnvio(ConfigurableApplicationContext contexto) throws Exception {
        contexto.getBean(EnvioEmailAsincronoConfig.EJECUTOR, ThreadPoolTaskExecutor.class)
                .submit(() -> {}).get(60, TimeUnit.SECONDS);
    }

    private static List<String> envios(ConfigurableApplicationContext contexto, UUID turno) {
        return contexto.getBean(JdbcTemplate.class).queryForList(
                "select tipo || ':' || estado || ':' || intentos from odontologia.email_envio where turno_id = ?",
                String.class, turno);
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
