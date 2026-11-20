package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProcesoAisladoTest {
    @Test void soloPropagaLaAllowlistSinAlterarElJvm() {
        String anterior = System.getProperty("spring.flyway.url");
        try {
            System.setProperty("spring.flyway.url", "jdbc:postgresql://127.0.0.1:1/no_usar");
            var permitidas = Map.of("DB_URL", "jdbc:postgresql://127.0.0.1:2/fixture");
            var builder = ProcesoAislado.preparar(List.of("java", "-version"), permitidas);
            assertTrue(builder.environment().equals(permitidas), "El hijo solo debe recibir la allowlist (sin mostrar valores del entorno)");
            assertEquals("jdbc:postgresql://127.0.0.1:1/no_usar", System.getProperty("spring.flyway.url"));
        } finally {
            if (anterior == null) System.clearProperty("spring.flyway.url");
            else System.setProperty("spring.flyway.url", anterior);
        }
    }
}
