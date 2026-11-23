package ar.edu.utn.odontologia.agenda.bloqueos;

import java.util.UUID;

public record RecursosBloqueo(UUID profesionalId, UUID sillonId) {
    public RecursosBloqueo {
        if (profesionalId == null && sillonId == null) {
            throw new IllegalArgumentException("Se requiere al menos un recurso");
        }
    }
}
