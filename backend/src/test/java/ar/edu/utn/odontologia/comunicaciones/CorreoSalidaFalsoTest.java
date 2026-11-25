package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CorreoSalidaFalsoTest {
    private SolicitudEnvio solicitud(String dest) {
        return new SolicitudEnvio("ALTA", UUID.randomUUID(), dest, "asunto", "cuerpo");
    }

    @Test
    void forzarExitoYFallo() {
        var fake = new CorreoSalidaFalso();
        var r1 = fake.enviar(solicitud("a@test.com"));
        assertTrue(r1.exitoso());
        assertEquals(1, r1.intentos());

        fake.forzarFallo("boom");
        var r2 = fake.enviar(solicitud("b@test.com"));
        assertFalse(r2.exitoso());
        assertEquals("boom", r2.error());
    }

    @Test
    void noPropagaExcepcion() {
        var fake = new CorreoSalidaFalso();
        var r = fake.enviar(null);
        assertFalse(r.exitoso());
    }

    @Test
    void idempotenciaEnMemoriaNoAplicaSinRepoPeroRegistra() {
        var fake = new CorreoSalidaFalso();
        var s = solicitud("a@test.com");
        fake.enviar(s);
        assertEquals(1, fake.registros().size());
    }
}