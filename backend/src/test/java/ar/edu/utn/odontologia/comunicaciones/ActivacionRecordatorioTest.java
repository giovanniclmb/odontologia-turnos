package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

class ActivacionRecordatorioTest {
    /** Se lee sin resolver: las pruebas de integración fijan la variable y ocultarían otro valor por defecto. */
    @Test
    void laConfiguracionDeclaraElRecordatorioApagadoSalvoQueSeActive() throws IOException {
        var configuracion = new YamlPropertySourceLoader()
                .load("aplicacion", new ClassPathResource("application.yaml")).getFirst();

        assertEquals("${EMAIL_RECORDATORIO_ACTIVO:false}",
                String.valueOf(configuracion.getProperty("email.recordatorio.activo")));
    }
}
