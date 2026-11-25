package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sede/intervalo")
public class SedeController {
    public record IntervaloRequest(BigDecimal minutos) {}
    /** {@code minutos} es nulo mientras la sede no configuró el intervalo. */
    public record IntervaloResponse(Integer minutos) {}

    private final ServicioConfiguracionSede sede;

    public SedeController(ServicioConfiguracionSede sede) {
        this.sede = sede;
    }

    @GetMapping
    public IntervaloResponse consultar(HttpServletRequest request) {
        return new IntervaloResponse(sede.consultarIntervalo(Solicitudes.identidad(request)).orElse(null));
    }

    @PutMapping
    public IntervaloResponse establecer(@RequestBody IntervaloRequest cuerpo, HttpServletRequest request) {
        var identidad = Solicitudes.identidad(request);
        var minutos = cuerpo == null ? null : Solicitudes.entero(cuerpo.minutos());
        sede.establecerIntervalo(identidad, minutos);
        return new IntervaloResponse(minutos);
    }
}
