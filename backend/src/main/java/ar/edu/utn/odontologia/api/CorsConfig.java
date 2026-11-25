package ar.edu.utn.odontologia.api;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    private static final Duration VIGENCIA_PREFLIGHT = Duration.ofMinutes(30);

    /** Precede a los demás filtros: sus rechazos, como el 401 por token, no llegan a Spring MVC. */
    @Bean
    FilterRegistrationBean<CorsFilter> filtroCors(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        var politica = new CorsConfiguration();
        politica.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        politica.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        politica.setAllowedHeaders(List.of(CorsConfiguration.ALL));
        politica.setAllowCredentials(false);
        politica.setMaxAge(VIGENCIA_PREFLIGHT);
        var rutas = new UrlBasedCorsConfigurationSource();
        rutas.registerCorsConfiguration("/**", politica);
        var registro = new FilterRegistrationBean<>(new CorsFilter(rutas));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
