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
}