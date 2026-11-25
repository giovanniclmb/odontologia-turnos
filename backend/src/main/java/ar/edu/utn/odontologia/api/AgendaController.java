package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.agenda.ConsultaAgenda;
import ar.edu.utn.odontologia.agenda.EstadoTurno;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agenda")
public class AgendaController {
    public record CrearTurnoRequest(UUID pacienteId, UUID profesionalId, UUID sillonId, UUID prestacionId,
                                    String inicio, BigDecimal duracionAjustadaMinutos) {}
    public record TurnoResponse(UUID id, UUID profesionalId, UUID sillonId, UUID prestacionId, Instant inicio,
                                Instant fin, int duracionMinutos, EstadoTurno estado, String version) {}
    public record TurnoAgendaResponse(UUID id, String pacienteNombre, String pacienteApellido, UUID profesionalId,
                                      UUID sillonId, UUID prestacionId, Instant inicio, Instant fin,
                                      int duracionMinutos, EstadoTurno estado, String version) {}

    private final ServicioAgenda agenda;
    private final ConsultaAgenda consulta;

    public AgendaController(ServicioAgenda agenda, ConsultaAgenda consulta) {
        this.agenda = agenda;
        this.consulta = consulta;
    }

    @GetMapping("/sillones/{sillonId}/turnos")
    public List<TurnoAgendaResponse> consultar(@PathVariable UUID sillonId, @RequestParam String desde,
                                              @RequestParam String hasta, HttpServletRequest request) {
        var identidad = Solicitudes.identidad(request);
        var periodo = Solicitudes.periodoDeConsulta(desde, hasta);
        return consulta.consultar(identidad, sillonId, periodo.desde(), periodo.hasta())
                .stream().map(AgendaController::mapear).toList();
    }

    @PostMapping("/turnos")
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse crear(@RequestBody CrearTurnoRequest cuerpo, HttpServletRequest request) {
        var identidad = Solicitudes.identidad(request);
        var solicitud = cuerpo == null ? null : new ServicioAgenda.SolicitudAlta(
                cuerpo.pacienteId(), cuerpo.profesionalId(), cuerpo.sillonId(), cuerpo.prestacionId(),
                Solicitudes.instante(cuerpo.inicio()), Solicitudes.entero(cuerpo.duracionAjustadaMinutos()));
        var turno = agenda.crear(identidad, solicitud);
        return new TurnoResponse(turno.id(), turno.profesionalId(), turno.sillonId(), turno.prestacionId(),
                turno.inicio(), turno.fin(), turno.duracionMinutos(), turno.estado(), turno.version());
    }

    private static TurnoAgendaResponse mapear(ConsultaAgenda.TurnoAgenda vista) {
        var turno = vista.turno();
        return new TurnoAgendaResponse(turno.id(), vista.pacienteNombre(), vista.pacienteApellido(),
                turno.profesionalId(), turno.sillonId(), turno.prestacionId(), turno.inicio(), turno.fin(),
                turno.duracionMinutos(), turno.estado(), turno.version());
    }
}
