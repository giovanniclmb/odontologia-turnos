package ar.edu.utn.odontologia.comunicaciones;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;

class ConfiguracionEmailTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(ConfiguracionEmailHabilitada.class)
            .withInitializer(context -> {
                context.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("testEnvironment", java.util.Map.of("EMAIL_TIMEOUT", "750ms")));
                try {
                    var yaml = new YamlPropertySourceLoader();
                    for (var propertySource : yaml.load("application", new ClassPathResource("application.yaml"))) {
                        context.getEnvironment().getPropertySources().addLast(propertySource);
                    }
                } catch (java.io.IOException e) {
                    throw new IllegalStateException(e);
                }
            });

    @Test
    void enlazaElTimeoutDocumentadoDesdeConfiguracion() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ConfiguracionEmail.class).timeout()).isEqualTo(Duration.ofMillis(750));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ConfiguracionEmail.class)
    static class ConfiguracionEmailHabilitada {}
}
