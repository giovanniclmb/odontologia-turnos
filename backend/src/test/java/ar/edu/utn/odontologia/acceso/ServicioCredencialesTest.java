package ar.edu.utn.odontologia.acceso;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ServicioCredencialesTest {
    private final ServicioCredenciales servicio = new ServicioCredenciales();

    @Test void hashDistintoDelTextoPlano() {
        String hash = servicio.hash("ClaveSegura123");
        assertNotEquals("ClaveSegura123", hash);
        assertTrue(servicio.verificar("ClaveSegura123", hash));
        assertFalse(servicio.verificar("otra", hash));
    }

    @Test void algoritmoYCostoBcrypt() {
        String hash = servicio.hash("ClaveSegura123");
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$") || hash.startsWith("$2y$12$"));
        // verify cost by BCryptPasswordEncoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        assertTrue(encoder.matches("ClaveSegura123", hash));
    }

    @Test void politicaRechazaCorta() {
        assertThrows(PoliticaContrasenaException.class, () -> servicio.validarPolitica("Corta1", new DatosPersonales("Juan", "juan", "j@x.com")));
    }

    @Test void politicaRechazaSinComplejidad() {
        assertThrows(PoliticaContrasenaException.class, () -> servicio.validarPolitica("aaaaaaaaaaaa", new DatosPersonales(null, null, null)));
        assertThrows(PoliticaContrasenaException.class, () -> servicio.validarPolitica("AAAAAAAAAAAA", new DatosPersonales(null, null, null)));
        assertThrows(PoliticaContrasenaException.class, () -> servicio.validarPolitica("111111111111", new DatosPersonales(null, null, null)));
    }

    @Test void politicaRechazaDatosPersonales() {
        assertThrows(PoliticaContrasenaException.class, () -> servicio.validarPolitica("JuanPerez12345", new DatosPersonales("Juan", "jperez", null)));
    }

    @Test void politicaRechazaReutilizacion() {
        String hash = servicio.hash("ClaveSegura123");
        assertThrows(PoliticaContrasenaException.class, () -> servicio.validarPolitica("ClaveSegura123", new DatosPersonales(null, null, null), List.of(hash), 5));
    }

    @Test void politicaAceptaValida() {
        assertDoesNotThrow(() -> servicio.validarPolitica("ClaveSegura123", new DatosPersonales("Ana", "ana", "a@b.com")));
    }
}
