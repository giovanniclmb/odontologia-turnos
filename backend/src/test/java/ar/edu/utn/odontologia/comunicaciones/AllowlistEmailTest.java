package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class AllowlistEmailTest {
    @Test
    void permiteSoloListados() {
        var a = new AllowlistEmail(List.of("Test@Example.com", "otro@test.com"));
        assertTrue(a.permite("test@example.com"));
        assertTrue(a.permite("  OTRO@test.com "));
        assertFalse(a.permite("nope@test.com"));
        assertFalse(a.permite(null));
        assertFalse(a.permite("   "));
    }

    @Test
    void allowlistVaciaRechazaTodo() {
        var a = new AllowlistEmail(List.of());
        assertFalse(a.permite("x@y.com"));
    }
}