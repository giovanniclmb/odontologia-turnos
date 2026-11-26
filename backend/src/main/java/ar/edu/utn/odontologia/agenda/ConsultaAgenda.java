package ar.edu.utn.odontologia.agenda;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.referencias.persistencia.NombrePaciente;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vista de agenda con la identificación mínima del paciente de cada turno ya autorizado. */
@Service
public class ConsultaAgenda {
    private final ServicioAgenda agenda;
    private final PacienteReferencias pacientes;

    public ConsultaAgenda(ServicioAgenda agenda, PacienteReferencias pacientes) {
        this.agenda = agenda;
        this.pacientes = pacientes;
    }

    @Transactional(readOnly = true)
    public List<TurnoAgenda> consultar(Identidad solicitante, UUID sillonId, Instant inicio, Instant fin) {
        // El alcance por rol se aplica primero: solo se resuelven nombres de turnos visibles.
        return proyectar(agenda.consultarAgendaSillon(solicitante, sillonId, inicio, fin));
    }

    @Transactional(readOnly = true)
    public List<TurnoAgenda> consultarPendientes(Identidad solicitante) {
        return proyectar(agenda.consultarPendientes(solicitante));
    }

    private List<TurnoAgenda> proyectar(List<ServicioAgenda.TurnoRegistrado> turnos) {
        if (turnos.isEmpty()) {
            return List.of();
        }
        var ids = turnos.stream().map(ServicioAgenda.TurnoRegistrado::pacienteId).distinct().toList();
        var nombres = pacientes.buscarNombres(ids).stream()
                .collect(Collectors.toMap(NombrePaciente::id, Function.identity()));
        return turnos.stream().map(turno -> {
            var paciente = nombres.get(turno.pacienteId());
            return new TurnoAgenda(turno, paciente.nombre(), paciente.apellido());
        }).toList();
    }

    public record TurnoAgenda(ServicioAgenda.TurnoRegistrado turno, String pacienteNombre, String pacienteApellido) {}
}
