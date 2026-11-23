package ar.edu.utn.odontologia.agenda.bloqueos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record PeriodoBloqueo(Instant inicio, Instant fin) {
    public PeriodoBloqueo {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Ambos extremos del período son obligatorios");
        }
        inicio = inicio.truncatedTo(ChronoUnit.MICROS);
        fin = fin.truncatedTo(ChronoUnit.MICROS);
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("El fin debe ser posterior al inicio");
        }
    }
}
