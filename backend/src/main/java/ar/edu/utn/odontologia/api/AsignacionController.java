package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
public class AsignacionController {
    public record AsignarRequest(UUID profesionalId) {}
    public record AsignacionResponse(UUID pacienteId, UUID profesionalId, boolean activa) {}

    private final ServicioAsignacionPacientes asignaciones;

    public AsignacionController(ServicioAsignacionPacientes asignaciones) {
        this.asignaciones = asignaciones;
    }

    private Identidad identidad(HttpServletRequest request) {
        return (Identidad) request.getAttribute(TokenAutenticacionFilter.ATRIBUTO_IDENTIDAD);
    }

    @PostMapping("/{pacienteId}/profesionales")
    public AsignacionResponse asignar(@PathVariable UUID pacienteId, @RequestBody AsignarRequest request,
                                     HttpServletRequest http) {
        var resultado = asignaciones.asignar(identidad(http), pacienteId,
                request == null ? null : request.profesionalId());
        return new AsignacionResponse(resultado.pacienteId(), resultado.profesionalId(), resultado.activa());
    }

    @DeleteMapping("/{pacienteId}/profesionales/{profesionalId}")
    public AsignacionResponse desasignar(@PathVariable UUID pacienteId, @PathVariable UUID profesionalId,
                                         HttpServletRequest http) {
        var resultado = asignaciones.desasignar(identidad(http), pacienteId, profesionalId);
        return new AsignacionResponse(resultado.pacienteId(), resultado.profesionalId(), resultado.activa());
    }
}