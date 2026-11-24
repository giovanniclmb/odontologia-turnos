package ar.edu.utn.odontologia.agenda.persistencia;

import ar.edu.utn.odontologia.agenda.Agenda;
import ar.edu.utn.odontologia.agenda.EstadoTurno;
import java.time.Instant;
import java.util.UUID;

public record TurnoLeido(UUID id, UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                        Instant inicio, Instant fin, int duracionMinutos, EstadoTurno estado, long version) {
    public Agenda.Reserva reserva() {
        return new Agenda.Reserva(profesionalId.toString(), sillonId.toString(), inicio, fin);
    }
}
