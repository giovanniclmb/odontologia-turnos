package ar.edu.utn.odontologia.espera;

import java.time.LocalTime;

public record FranjaEspera(LocalTime inicio, LocalTime fin) {
    public FranjaEspera {
        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La franja requiere un inicio anterior al fin");
        }
    }
}
