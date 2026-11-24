package ar.edu.utn.odontologia.comunicaciones;

import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorreoSalidaConfig {

    @Bean
    CorreoSalida correoSalida(ConfiguracionEmail configuracion, EmailsEnviados repositorio) {
        return new AdaptadorResend(configuracion, repositorio);
    }
}