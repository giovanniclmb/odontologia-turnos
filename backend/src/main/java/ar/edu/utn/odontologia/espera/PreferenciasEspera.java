package ar.edu.utn.odontologia.espera;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PreferenciasEspera(UUID prestacionId, LocalDate desde, LocalDate hasta,
                                 List<FranjaEspera> franjas, UUID profesionalId) {
    public PreferenciasEspera {
        if (prestacionId == null || desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("Se requieren una prestación y fechas ordenadas");
        }
        if (franjas == null || franjas.isEmpty() || franjas.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Se requiere al menos una franja válida");
        }
        franjas = List.copyOf(franjas);
    }

    void validarFechaFinal(LocalDate hoy) {
        if (hasta.isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha final ya pasó");
        }
    }
}
