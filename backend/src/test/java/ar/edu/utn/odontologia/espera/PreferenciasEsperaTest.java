package ar.edu.utn.odontologia.espera;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PreferenciasEsperaTest {
    private final LocalDate hoy = LocalDate.of(2026, 11, 25);
    private final UUID prestacion = UUID.randomUUID();
    private final List<FranjaEspera> franjas = List.of(
            new FranjaEspera(LocalTime.of(9, 0), LocalTime.of(10, 0)),
            new FranjaEspera(LocalTime.of(14, 0), LocalTime.of(16, 0)));

    @Test
    void aceptaUnDiaConVariasFranjasSinProfesional() {
        var preferencias = new PreferenciasEspera(prestacion, hoy, hoy, franjas, null);
        assertDoesNotThrow(() -> preferencias.validarFechaFinal(hoy));
        assertEquals(franjas, preferencias.franjas());
        assertNull(preferencias.profesionalId());
    }

    @Test
    void rechazaPreferenciasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new FranjaEspera(LocalTime.NOON, LocalTime.NOON));
        assertThrows(IllegalArgumentException.class, () -> new FranjaEspera(LocalTime.NOON, LocalTime.MIDNIGHT));
        assertThrows(IllegalArgumentException.class, () -> new PreferenciasEspera(null, hoy, hoy, franjas, null));
        assertThrows(IllegalArgumentException.class,
                () -> new PreferenciasEspera(prestacion, hoy.plusDays(1), hoy, franjas, null));
        assertThrows(IllegalArgumentException.class,
                () -> new PreferenciasEspera(prestacion, hoy, hoy, List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new PreferenciasEspera(prestacion, hoy, hoy, franjas, null).validarFechaFinal(hoy.plusDays(1)));
    }
}
