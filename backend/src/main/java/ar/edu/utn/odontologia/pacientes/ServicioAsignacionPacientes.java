package ar.edu.utn.odontologia.pacientes;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesional;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionalId;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencias;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioAsignacionPacientes {
    private final PacienteReferencias pacientes;
    private final ProfesionalReferencias profesionales;
    private final PacienteProfesionales asignaciones;
    private final ServicioAutorizacion autorizacion;

    public ServicioAsignacionPacientes(PacienteReferencias pacientes, ProfesionalReferencias profesionales,
                                      PacienteProfesionales asignaciones, ServicioAutorizacion autorizacion) {
        this.pacientes = pacientes;
        this.profesionales = profesionales;
        this.asignaciones = asignaciones;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public AsignacionPaciente asignar(Identidad solicitante, UUID pacienteId, UUID profesionalId) {
        exigirRecepcion(solicitante);
        validarReferencias(pacienteId, profesionalId);
        var id = new PacienteProfesionalId(pacienteId, profesionalId);
        PacienteProfesional asignacion = asignaciones.findById(id)
                .orElseGet(() -> new PacienteProfesional(id, true));
        asignacion.setActivo(true);
        return vista(asignaciones.save(asignacion));
    }

    @Transactional
    public AsignacionPaciente desasignar(Identidad solicitante, UUID pacienteId, UUID profesionalId) {
        exigirRecepcion(solicitante);
        validarReferencias(pacienteId, profesionalId);
        var id = new PacienteProfesionalId(pacienteId, profesionalId);
        PacienteProfesional asignacion = asignaciones.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La asignación paciente-profesional no existe"));
        asignacion.setActivo(false);
        return vista(asignaciones.save(asignacion));
    }

    private void validarReferencias(UUID pacienteId, UUID profesionalId) {
        if (pacienteId == null || profesionalId == null) {
            throw new IllegalArgumentException("El paciente y el profesional son obligatorios");
        }
        pacientes.findById(pacienteId).orElseThrow(() -> new IllegalArgumentException("El paciente no existe"));
        profesionales.findById(profesionalId).orElseThrow(() -> new IllegalArgumentException("El profesional no existe"));
    }

    private void exigirRecepcion(Identidad solicitante) {
        autorizacion.exigir(solicitante, Permiso.PACIENTE_DATOS_ADMIN);
        if (solicitante.rol() != Rol.RECEPCION) {
            throw new AutorizacionDenegadaException();
        }
    }

    private static AsignacionPaciente vista(PacienteProfesional asignacion) {
        return new AsignacionPaciente(asignacion.getId().getPacienteId(),
                asignacion.getId().getProfesionalId(), asignacion.isActivo());
    }

    public record AsignacionPaciente(UUID pacienteId, UUID profesionalId, boolean activa) {}
}
