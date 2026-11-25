package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesional;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {
    public record CrearPacienteRequest(String codigo, String nombre, String apellido, String email, String telefono) {}
    public record ActualizarPacienteRequest(String nombre, String apellido, String email, String telefono) {}
    public record PacienteResponse(UUID id, String codigo, String nombre, String apellido, String email, String telefono,
                                  List<UUID> profesionalesAsignados) {}

    private final ServicioPacientes pacientes;
    private final PacienteProfesionales asignaciones;

    public PacienteController(ServicioPacientes pacientes, PacienteProfesionales asignaciones) {
        this.pacientes = pacientes;
        this.asignaciones = asignaciones;
    }

    private Identidad identidad(HttpServletRequest request) {
        return (Identidad) request.getAttribute(TokenAutenticacionFilter.ATRIBUTO_IDENTIDAD);
    }

    @GetMapping
    public List<PacienteResponse> listar(@RequestParam(required = false) String codigo,
                                         @RequestParam(required = false) String texto,
                                         HttpServletRequest request) {
        return pacientes.buscar(identidad(request), new ServicioPacientes.FiltroBusquedaPaciente(codigo, texto))
                .stream().map(this::mapear).toList();
    }

    @PostMapping
    public PacienteResponse crear(@RequestBody CrearPacienteRequest request, HttpServletRequest http) {
        var creado = pacientes.crear(identidad(http), new ServicioPacientes.CrearPaciente(
                request == null ? null : request.codigo(),
                request == null ? null : request.nombre(),
                request == null ? null : request.apellido(),
                request == null ? null : request.email(),
                request == null ? null : request.telefono()));
        return mapear(creado);
    }

    @GetMapping("/{id}")
    public PacienteResponse consultar(@PathVariable UUID id, HttpServletRequest request) {
        return mapear(pacientes.consultar(identidad(request), id));
    }

    @PutMapping("/{id}")
    public PacienteResponse actualizar(@PathVariable UUID id, @RequestBody ActualizarPacienteRequest request,
                                      HttpServletRequest http) {
        var actualizado = pacientes.actualizarDatos(identidad(http), id, new ServicioPacientes.ActualizarDatosPaciente(
                request == null ? null : request.nombre(),
                request == null ? null : request.apellido(),
                request == null ? null : request.email(),
                request == null ? null : request.telefono()));
        return mapear(actualizado);
    }

    private PacienteResponse mapear(ServicioPacientes.PacienteDetalle detalle) {
        List<UUID> profesionales = asignaciones.dePaciente(detalle.id()).stream()
                .filter(PacienteProfesional::isActivo).map(a -> a.getId().getProfesionalId()).toList();
        return new PacienteResponse(detalle.id(), detalle.codigo(), detalle.nombre(), detalle.apellido(),
                detalle.email(), detalle.telefono(), profesionales);
    }

    private PacienteResponse mapear(ServicioPacientes.ResumenPaciente resumen) {
        List<UUID> profesionales = asignaciones.dePaciente(resumen.id()).stream()
                .filter(PacienteProfesional::isActivo).map(a -> a.getId().getProfesionalId()).toList();
        return new PacienteResponse(resumen.id(), resumen.codigo(), resumen.nombre(), resumen.apellido(),
                resumen.email(), resumen.telefono(), profesionales);
    }
}