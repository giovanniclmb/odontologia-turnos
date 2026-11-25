package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.comunicaciones.ClaveIdempotencia;
import ar.edu.utn.odontologia.comunicaciones.ConfiguracionEmail;
import ar.edu.utn.odontologia.comunicaciones.CorreoSalida;
import ar.edu.utn.odontologia.comunicaciones.SolicitudEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * Test operativo manual. Su nombre evita la selección automática de Surefire/Failsafe;
 * solo puede ejecutarse por selección explícita y opt-in de entorno.
 * No volver a ejecutar ante timeout o resultado incierto hasta revisar el panel de Resend.
 */
@EnabledIfEnvironmentVariable(named = "C17_REAL_EMAIL_CONFIRM", matches = "PREFLIGHT_ONLY|SEND_ONE_C17_TEST")
class CorreoRealManual {
    private static final String ID_ENVIO = "C17_REAL_TEST";
    private static final UUID TURNO_TEST = UUID.fromString("3c17a117-0000-4000-8000-000000000001");
    private static final String ASUNTO = "Prueba real de transporte C-17";
    private static final String CUERPO = "Mensaje sintético para verificar el transporte C-17. "
            + "No contiene datos personales ni información clínica.";

    @Test
    void enviaUnMensajeSinteticoSoloAlDestinatarioAprobado() {
        String confirmacion = System.getenv("C17_REAL_EMAIL_CONFIRM");
        if ("PREFLIGHT_ONLY".equals(confirmacion)) {
            verificarGuardasSinteticas();
            return;
        }

        String destinatario = requerida("C17_REAL_EMAIL_TO");
        String dominioVerificado = System.getenv("C17_REAL_EMAIL_FROM_VERIFIED");
        validarConfirmacion(confirmacion, dominioVerificado);

        try (var db = PostgresFixture.start(); var context = abrir(db)) {
            ConfiguracionEmail config = context.getBean(ConfiguracionEmail.class);
            validarPreflight(destinatario, config, confirmacion, dominioVerificado);

            var solicitud = new SolicitudEnvio(ID_ENVIO, TURNO_TEST, destinatario, ASUNTO, CUERPO);
            String key = ClaveIdempotencia.generar(ID_ENVIO, TURNO_TEST, destinatario);
            var resultado = context.getBean(CorreoSalida.class).enviar(solicitud);

            assertTrue(resultado.exitoso(), "Resend no aceptó el mensaje; revisa el error antes de cualquier reintento");
            assertEquals(1, resultado.intentos());
            assertTrue(key.equals(resultado.idempotencyKey()), "la respuesta no coincide con la clave local de prueba");
            var registro = context.getBean(EmailsEnviados.class).findByIdempotencyKey(key);
            assertNotNull(registro);
            assertEquals("sent", registro.estado());
            assertEquals(1, registro.intentos());
        }
    }

    private static String requerida(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("falta la variable local " + nombre);
        }
        return valor.trim();
    }

    static void validarConfirmacion(String confirmacion, String dominioVerificado) {
        if (!"SEND_ONE_C17_TEST".equals(confirmacion) || !"YES".equals(dominioVerificado)) {
            throw new IllegalStateException("faltan las confirmaciones locales obligatorias para el envío manual");
        }
    }

    static void validarAllowlist(String destinatario, List<String> allowlist) {
        String esperado = normalizar(destinatario);
        List<String> normalizada = allowlist == null ? List.of()
                : allowlist.stream().map(CorreoRealManual::normalizar).toList();
        if (normalizada.size() != 1 || !normalizada.get(0).equals(esperado)) {
            throw new IllegalStateException("EMAIL_TEST_ALLOWLIST debe contener solo el destinatario aprobado");
        }
    }

    static void validarPreflight(String destinatario, ConfiguracionEmail config,
                                 String confirmacion, String dominioVerificado) {
        validarConfirmacion(confirmacion, dominioVerificado);
        if (config == null || config.resendApiKey() == null || config.resendApiKey().isBlank()) {
            throw new IllegalStateException("falta RESEND_API_KEY en la configuración local");
        }
        if (config.from() == null || config.from().isBlank()) {
            throw new IllegalStateException("falta EMAIL_FROM en la configuración local");
        }
        if (config.maxRetries() != 0) {
            throw new IllegalStateException("EMAIL_MAX_RETRIES debe ser 0 para esta prueba");
        }
        validarAllowlist(destinatario, config.testAllowlist());
    }

    private static void verificarGuardasSinteticas() {
        var configSinCredenciales = new ConfiguracionEmail();
        assertThrows(IllegalStateException.class,
                () -> validarPreflight("destinatario.invalid", configSinCredenciales,
                        "SEND_ONE_C17_TEST", "YES"));
        assertDoesNotThrow(() -> validarAllowlist("destinatario.invalid", List.of("destinatario.invalid")));
        assertThrows(IllegalStateException.class,
                () -> validarAllowlist("destinatario.invalid", List.of("otro.invalid")));
        assertThrows(IllegalStateException.class,
                () -> validarConfirmacion("SEND_ONE_C17_TEST", "NO"));
    }

    private static String normalizar(String email) {
        return Normalizer.normalize(email.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    private static ConfigurableApplicationContext abrir(PostgresFixture db) {
        var env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("postgresTemporal", db.properties()));
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE)
                .environment(env).run("--spring.config.location=classpath:/application.yaml");
    }
}
