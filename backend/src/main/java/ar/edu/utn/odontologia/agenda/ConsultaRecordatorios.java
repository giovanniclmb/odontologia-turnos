package ar.edu.utn.odontologia.agenda;

import ar.edu.utn.odontologia.agenda.persistencia.TurnoRecordable;
import ar.edu.utn.odontologia.agenda.persistencia.TurnosPersistidos;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lecturas sin locks para recordar turnos próximos. */
@Service
public class ConsultaRecordatorios {
    private static final Duration ANTICIPACION = Duration.ofHours(24);
    private static final List<EstadoTurno> ESTADOS = List.of(EstadoTurno.PENDIENTE, EstadoTurno.CONFIRMADO);

    private final TurnosPersistidos turnos;
    private final Clock reloj;

    public ConsultaRecordatorios(TurnosPersistidos turnos, Clock reloj) {
        this.turnos = turnos;
        this.reloj = reloj;
    }

    /** Turnos vigentes que comienzan dentro de 24 horas y cuyo horario se fijó con esa anticipación. */
    @Transactional(readOnly = true)
    public List<TurnoPorRecordar> porRecordar() {
        return recordables(null)
                .map(turno -> new TurnoPorRecordar(turno.turnoId(), turno.inicio()))
                .toList();
    }

    /** Devuelve el paciente si el horario todavía corresponde recordarlo en este momento. */
    @Transactional(readOnly = true)
    public Optional<UUID> pacienteSiSigueVigente(TurnoPorRecordar candidato) {
        return recordables(candidato.turnoId())
                .filter(turno -> turno.inicio().equals(candidato.inicio()))
                .map(TurnoRecordable::pacienteId)
                .findFirst();
    }

    private Stream<TurnoRecordable> recordables(UUID soloTurno) {
        // Los inicios persistidos tienen precisión de microsegundos.
        var ahora = reloj.instant().truncatedTo(ChronoUnit.MICROS);
        return turnos.consultarRecordables(ESTADOS, ahora, ahora.plus(ANTICIPACION), soloTurno).stream()
                .filter(turno -> turno.horarioFijadoEn() == null
                        || !turno.horarioFijadoEn().isAfter(turno.inicio().minus(ANTICIPACION)));
    }
}
