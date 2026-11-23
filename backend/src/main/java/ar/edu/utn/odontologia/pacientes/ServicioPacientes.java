package ar.edu.utn.odontologia.pacientes;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioPacientes {
    private final PacienteReferencias pacientes;
    private final PacienteProfesionales asignaciones;
    private final ServicioAutorizacion autorizacion;

    public ServicioPacientes(PacienteReferencias pacientes, PacienteProfesionales asignaciones,
                             ServicioAutorizacion autorizacion) {
        this.pacientes = pacientes;
        this.asignaciones = asignaciones;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public PacienteDetalle crear(Identidad solicitante, CrearPaciente solicitud) {
        autorizacion.exigir(solicitante, Permiso.PACIENTE_DATOS_ADMIN);
        validarDatos(solicitud == null ? null : solicitud.codigo(),
                solicitud == null ? null : solicitud.nombre(),
                solicitud == null ? null : solicitud.apellido(),
                solicitud == null ? null : solicitud.email());
        PacienteReferencia paciente = new PacienteReferencia(solicitud.codigo(), solicitud.nombre(),
                solicitud.apellido(), solicitud.email(), solicitud.telefono());
        return detalle(pacientes.save(paciente));
    }

    @Transactional(readOnly = true)
    public PacienteDetalle consultar(Identidad solicitante, UUID pacienteId) {
        autorizacion.exigir(solicitante, Permiso.PACIENTE_DATOS_ADMIN);
        PacienteReferencia paciente = obtener(pacienteId);
        exigirAlcance(solicitante, paciente.getId());
        return detalle(paciente);
    }

    @Transactional
    public PacienteDetalle actualizarDatos(Identidad solicitante, UUID pacienteId, ActualizarDatosPaciente cambios) {
        autorizacion.exigir(solicitante, Permiso.PACIENTE_DATOS_ADMIN);
        validarDatos("CODIGO-EXISTENTE", cambios == null ? null : cambios.nombre(),
                cambios == null ? null : cambios.apellido(), cambios == null ? null : cambios.email());
        PacienteReferencia paciente = obtener(pacienteId);
        exigirAlcance(solicitante, paciente.getId());
        paciente.actualizarDatos(cambios.nombre(), cambios.apellido(), cambios.email(), cambios.telefono());
        return detalle(paciente);
    }

    @Transactional(readOnly = true)
    public List<ResumenPaciente> buscar(Identidad solicitante, FiltroBusquedaPaciente filtro) {
        autorizacion.exigir(solicitante, Permiso.PACIENTE_DATOS_ADMIN);
        if (filtro == null) {
            filtro = new FiltroBusquedaPaciente(null, null);
        }
        String codigo = filtroExacto(filtro.codigoExacto());
        String texto = limpiarFiltro(filtro.texto());
        UUID profesionalId = solicitante.rol() == Rol.RECEPCION ? null : solicitante.profesionalRef();
        if (solicitante.rol() == Rol.ODONTOLOGO && profesionalId == null) {
            return List.of();
        }
        return pacientes.buscarEnAlcance(codigo, texto, profesionalId).stream()
                .map(ServicioPacientes::resumen).toList();
    }

    private PacienteReferencia obtener(UUID pacienteId) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("La referencia del paciente es obligatoria");
        }
        return pacientes.findById(pacienteId).orElseThrow(() -> new IllegalArgumentException("El paciente no existe"));
    }

    private void exigirAlcance(Identidad solicitante, UUID pacienteId) {
        if (solicitante.rol() == Rol.ODONTOLOGO
                && (solicitante.profesionalRef() == null
                    || !asignaciones.tieneAsignacionActiva(pacienteId, solicitante.profesionalRef()))) {
            throw new AutorizacionDenegadaException();
        }
    }

    private static void validarDatos(String codigo, String nombre, String apellido, String email) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del paciente es obligatorio");
        }
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El nombre y apellido del paciente son obligatorios");
        }
        if (email == null || email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El email debe tener un formato válido");
        }
    }

    private static String limpiarFiltro(String filtro) {
        return filtro == null || filtro.isBlank() ? null : filtro.strip();
    }

    private static String filtroExacto(String filtro) {
        return filtro == null || filtro.isBlank() ? null : filtro;
    }

    private static ResumenPaciente resumen(PacienteReferencia paciente) {
        return new ResumenPaciente(paciente.getId(), paciente.getCodigo(), paciente.getNombre(),
                paciente.getApellido(), paciente.getEmail(), paciente.getTelefono());
    }

    private static PacienteDetalle detalle(PacienteReferencia paciente) {
        return new PacienteDetalle(paciente.getId(), paciente.getCodigo(), paciente.getNombre(),
                paciente.getApellido(), paciente.getEmail(), paciente.getTelefono());
    }

    public record CrearPaciente(String codigo, String nombre, String apellido, String email, String telefono) {}
    public record ActualizarDatosPaciente(String nombre, String apellido, String email, String telefono) {}
    public record FiltroBusquedaPaciente(String codigoExacto, String texto) {}
    public record ResumenPaciente(UUID id, String codigo, String nombre, String apellido, String email, String telefono) {}
    public record PacienteDetalle(UUID id, String codigo, String nombre, String apellido, String email, String telefono) {}
}
