package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClaveIdempotenciaTest {
    @Test
    void deterministaYDistintaPorDestinatario() {
        UUID t = UUID.randomUUID();
        String a = ClaveIdempotencia.generar("ALTA", t, "a@test.com");
        String b = ClaveIdempotencia.generar("ALTA", t, "a@test.com");
        String c = ClaveIdempotencia.generar("ALTA", t, "b@test.com");
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(64, a.length());
    }
}