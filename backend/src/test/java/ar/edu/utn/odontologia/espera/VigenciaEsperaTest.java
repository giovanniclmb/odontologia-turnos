package ar.edu.utn.odontologia.espera;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class VigenciaEsperaTest {
    private final LocalDate fin = LocalDate.of(2026, 11, 25);

    @Test
    void venceAlComenzarElDiaSiguienteEnBuenosAires() {
        assertEquals(VigenciaEspera.VIGENTE, VigenciaEspera.calcular(fin, null,
                VigenciaEspera.fechaLocal(reloj("2026-11-26T02:59:59Z").instant())));
        assertEquals(VigenciaEspera.VENCIDA, VigenciaEspera.calcular(fin, null,
                VigenciaEspera.fechaLocal(reloj("2026-11-26T03:00:00Z").instant())));
    }

    @Test
    void laBajaConservaSuEstadoDespuesDelFinDelRango() {
        var baja = reloj("2026-11-25T15:00:00Z").instant();
        assertEquals(VigenciaEspera.BAJA, VigenciaEspera.calcular(fin, baja,
                VigenciaEspera.fechaLocal(baja)));
        assertEquals(VigenciaEspera.BAJA, VigenciaEspera.calcular(fin, baja, fin.plusDays(1)));
    }

    private Clock reloj(String instante) {
        return Clock.fixed(Instant.parse(instante), ZoneOffset.UTC);
    }
}
