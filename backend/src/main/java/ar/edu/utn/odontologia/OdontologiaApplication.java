package ar.edu.utn.odontologia;

import java.util.List;
import java.util.Properties;
import java.time.Clock;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.postgresql.Driver;
import ar.edu.utn.odontologia.agenda.Agenda;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class OdontologiaApplication {
    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
    }
    public static void main(String[] args) {
        new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public Agenda agenda() { return new Agenda(); }

    @Bean
    public Clock relojSistema() { return Clock.systemUTC(); }

    @Bean
    static BeanFactoryPostProcessor configuracionObligatoria(Environment environment) {
        return factory -> {
            for (String key : List.of("DB_URL", "DB_USER", "DB_PASSWORD", "DB_MIGRATION_USER", "DB_MIGRATION_PASSWORD")) {
                String value = environment.getProperty(key);
                if (value == null || value.isBlank()) {
                    throw new IllegalStateException("Falta la configuración obligatoria " + key);
                }
            }
            validarUrlSinCredenciales(environment.getRequiredProperty("spring.datasource.url"), "spring.datasource.url");
            validarUrlSinCredenciales(environment.getRequiredProperty("spring.flyway.url"), "spring.flyway.url");
            if (!"odonto_app".equals(environment.getRequiredProperty("spring.datasource.username"))) {
                throw new IllegalStateException("spring.datasource.username debe ser odonto_app, sin permisos de migración");
            }
            if (!"odonto_migrator".equals(environment.getRequiredProperty("spring.flyway.user"))) {
                throw new IllegalStateException("spring.flyway.user debe ser odonto_migrator, sin privilegios de administrador");
            }
        };
    }

    private static void validarUrlSinCredenciales(String url, String key) {
        String error = key + " no admite credenciales en la URL; configurarlas por separado";
        try {
            // Detectar antes de invocar el driver evita que un diagnóstico de URL malformada revele secretos.
            if (!url.startsWith("jdbc:postgresql:")) throw new IllegalArgumentException();
            if (url.startsWith("jdbc:postgresql://") && URI.create(url.substring(5)).getRawUserInfo() != null) {
                throw new IllegalArgumentException();
            }
            int query = url.indexOf('?');
            if (query >= 0) {
                for (String parameter : url.substring(query + 1).split("&")) {
                    String name = URLDecoder.decode(parameter.split("=", 2)[0], StandardCharsets.UTF_8);
                    if (name.equalsIgnoreCase("user") || name.equalsIgnoreCase("password") || name.equalsIgnoreCase("service")) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            Properties parsed = Driver.parseURL(url, new Properties());
            if (parsed == null || parsed.containsKey("user") || parsed.containsKey("password") || parsed.containsKey("service")) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException failure) {
            // No incluir la URL ni la excepción del parser en un diagnóstico que pueda contener contraseña.
            throw new IllegalStateException(error);
        }
    }
}
