package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.pacientes.ServicioFichaClinica;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
public class FichaController {
    public record ActualizarAntecedentesRequest(String antecedentes) {}
    public record CrearNotaRequest(String texto) {}
    public record EditarNotaRequest(String texto) {}
    public record NotaResponse(UUID id, UUID autorId, java.time.Instant creadaEn, String texto) {}
    public record FichaResponse(UUID pacienteId, String antecedentes, List<NotaResponse> notas) {}

    private final ServicioFichaClinica ficha;

    public FichaController(ServicioFichaClinica ficha) {
        this.ficha = ficha;
    }

    private Identidad identidad(HttpServletRequest request) {
        return (Identidad) request.getAttribute(TokenAutenticacionFilter.ATRIBUTO_IDENTIDAD);
    }

    @GetMapping("/{pacienteId}/ficha")
    public FichaResponse consultar(@PathVariable UUID pacienteId, HttpServletRequest request) {
        var vista = ficha.consultar(identidad(request), pacienteId);
        return new FichaResponse(vista.pacienteId(), vista.antecedentes(),
                vista.notas().stream().map(n -> new NotaResponse(n.id(), n.autorId(), n.creadaEn(), n.texto())).toList());
    }

    @PutMapping("/{pacienteId}/ficha/antecedentes")
    public FichaResponse actualizarAntecedentes(@PathVariable UUID pacienteId,
                                               @RequestBody ActualizarAntecedentesRequest request,
                                               HttpServletRequest http) {
        var vista = ficha.actualizarAntecedentes(identidad(http), pacienteId,
                request == null ? null : request.antecedentes());
        return new FichaResponse(vista.pacienteId(), vista.antecedentes(),
                vista.notas().stream().map(n -> new NotaResponse(n.id(), n.autorId(), n.creadaEn(), n.texto())).toList());
    }

    @PostMapping("/{pacienteId}/ficha/notas")
    public NotaResponse agregarNota(@PathVariable UUID pacienteId, @RequestBody CrearNotaRequest request,
                                    HttpServletRequest http) {
        var nota = ficha.agregarNota(identidad(http), pacienteId,
                new ServicioFichaClinica.CrearNotaClinica(request == null ? null : request.texto()));
        return new NotaResponse(nota.id(), nota.autorId(), nota.creadaEn(), nota.texto());
    }

    @PutMapping("/{pacienteId}/ficha/notas/{notaId}")
    public NotaResponse editarNota(@PathVariable UUID pacienteId, @PathVariable UUID notaId,
                                   @RequestBody EditarNotaRequest request, HttpServletRequest http) {
        var nota = ficha.editarNota(identidad(http), pacienteId, notaId,
                new ServicioFichaClinica.EditarNotaClinica(request == null ? null : request.texto()));
        return new NotaResponse(nota.id(), nota.autorId(), nota.creadaEn(), nota.texto());
    }
}