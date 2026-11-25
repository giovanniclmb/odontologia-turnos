package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.ServicioBloqueos;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agenda/bloqueos")
public class BloqueoController {
    public record CrearBloqueoRequest(UUID sillonId, UUID profesionalId, String inicio, String fin) {}
    public record BloqueoResponse(UUID id, Instant inicio, Instant fin, UUID sillonId, UUID profesionalId) {}

    private final ServicioBloqueos bloqueos;

    public BloqueoController(ServicioBloqueos bloqueos) {
        this.bloqueos = bloqueos;
    }

    @GetMapping
    public List<BloqueoResponse> consultar(@RequestParam UUID sillonId, @RequestParam String desde,
                                          @RequestParam String hasta, HttpServletRequest request) {
        var identidad = Solicitudes.identidad(request);
        var periodo = Solicitudes.periodoDeConsulta(desde, hasta);
        return bloqueos.consultarVisibles(identidad, sillonId, periodo.desde(), periodo.hasta()).stream()
                .map(b -> new BloqueoResponse(b.id(), b.inicio(), b.fin(), b.sillonId(), b.profesionalId()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueoResponse crear(@RequestBody CrearBloqueoRequest cuerpo, HttpServletRequest request) {
        var identidad = Solicitudes.identidad(request);
        var recursos = cuerpo == null ? null : new RecursosBloqueo(cuerpo.profesionalId(), cuerpo.sillonId());
        var periodo = cuerpo == null ? null
                : new PeriodoBloqueo(Solicitudes.instante(cuerpo.inicio()), Solicitudes.instante(cuerpo.fin()));
        var creado = bloqueos.crear(identidad, recursos, periodo);
        return new BloqueoResponse(creado.id(), creado.periodo().inicio(), creado.periodo().fin(),
                creado.recursos().sillonId(), creado.recursos().profesionalId());
    }
}
