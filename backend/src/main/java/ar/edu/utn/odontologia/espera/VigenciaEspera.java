package ar.edu.utn.odontologia.espera;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public enum VigenciaEspera {
    VIGENTE, VENCIDA, BAJA;

    private static final ZoneId ZONA_SEDE = ZoneId.of("America/Argentina/Buenos_Aires");

    static LocalDate fechaLocal(Instant instante) {
        return LocalDate.ofInstant(instante, ZONA_SEDE);
    }

    static VigenciaEspera calcular(LocalDate hasta, Instant baja, LocalDate hoy) {
        if (baja != null) {
            return BAJA;
        }
        return hasta.isBefore(hoy) ? VENCIDA : VIGENTE;
    }
}
