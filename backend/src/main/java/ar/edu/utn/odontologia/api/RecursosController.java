package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recursos")
public class RecursosController {
    public record ProfesionalResponse(UUID id, String codigo, boolean activo) {}
    public record SillonResponse(UUID id, String codigo, boolean activo) {}
    public record PrestacionResponse(UUID id, String codigo, Integer duracionReferenciaMinutos) {}

    private final ServicioRecursosAgenda recursos;

    public RecursosController(ServicioRecursosAgenda recursos) {
        this.recursos = recursos;
    }

    private Identidad identidad(HttpServletRequest request) {
        return (Identidad) request.getAttribute(TokenAutenticacionFilter.ATRIBUTO_IDENTIDAD);
    }

    @GetMapping("/profesionales")
    public List<ProfesionalResponse> profesionales(HttpServletRequest request) {
        return recursos.consultarProfesionales(identidad(request)).stream()
                .map(p -> new ProfesionalResponse(p.id(), p.codigo(), p.activo())).toList();
    }

    @GetMapping("/sillones")
    public List<SillonResponse> sillones(HttpServletRequest request) {
        return recursos.consultarSillones(Solicitudes.identidad(request)).stream()
                .map(s -> new SillonResponse(s.id(), s.codigo(), s.activo())).toList();
    }

    @GetMapping("/prestaciones")
    public List<PrestacionResponse> prestaciones(HttpServletRequest request) {
        return recursos.consultarPrestaciones(Solicitudes.identidad(request)).stream()
                .map(p -> new PrestacionResponse(p.id(), p.codigo(), p.duracionReferenciaMinutos())).toList();
    }
}