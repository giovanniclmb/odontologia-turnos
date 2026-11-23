package ar.edu.utn.odontologia.pacientes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesional;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionalId;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencias;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioAsignacionPacientesTest {
    private final PacienteReferencias pacientes = mock(PacienteReferencias.class);
    private final ProfesionalReferencias profesionales = mock(ProfesionalReferencias.class);
    private final PacienteProfesionales asignaciones = mock(PacienteProfesionales.class);
    private ServicioAsignacionPacientes servicio;
    private final UUID pacienteId = UUID.randomUUID();
    private final UUID profesionalId = UUID.randomUUID();
    private final Identidad recepcion = new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
    private final Identidad odontologo = new Identidad(UUID.randomUUID(), "odontologo-ficticio", Rol.ODONTOLOGO, profesionalId);

    @BeforeEach
    void iniciar() {
        servicio = new ServicioAsignacionPacientes(pacientes, profesionales, asignaciones, new ServicioAutorizacion());
        when(pacientes.findById(pacienteId)).thenReturn(Optional.of(
                new PacienteReferencia("PAC-TEST", "Ana", "Ejemplo", "ana@example.invalid", null)));
        when(profesionales.findById(profesionalId)).thenReturn(Optional.of(new ProfesionalReferencia("PRO-TEST", true)));
    }

    @Test
    void recepcionAsignaProfesionalExistente() {
        when(asignaciones.findById(any(PacienteProfesionalId.class))).thenReturn(Optional.empty());
        when(asignaciones.save(any(PacienteProfesional.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var asignada = servicio.asignar(recepcion, pacienteId, profesionalId);

        assertEquals(pacienteId, asignada.pacienteId());
        assertEquals(profesionalId, asignada.profesionalId());
        assertTrue(asignada.activa());
        verify(asignaciones).save(any(PacienteProfesional.class));
    }

    @Test
    void odontologiaNoAdministraAsignaciones() {
        assertThrows(AutorizacionDenegadaException.class,
                () -> servicio.asignar(odontologo, pacienteId, profesionalId));
        assertThrows(AutorizacionDenegadaException.class,
                () -> servicio.desasignar(odontologo, pacienteId, profesionalId));
        verifyNoInteractions(asignaciones);
    }

    @Test
    void profesionalInexistenteNoCambiaAsignaciones() {
        when(profesionales.findById(profesionalId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.asignar(recepcion, pacienteId, profesionalId));

        verify(asignaciones, never()).save(any());
    }

    @Test
    void desasignacionDesactivaYUnaAsignacionDesactivadaPuedeReactivarse() {
        var id = new PacienteProfesionalId(pacienteId, profesionalId);
        var existente = new PacienteProfesional(id, true);
        when(asignaciones.findById(id)).thenReturn(Optional.of(existente));
        when(asignaciones.save(existente)).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertFalse(servicio.desasignar(recepcion, pacienteId, profesionalId).activa());
        when(asignaciones.findById(id)).thenReturn(Optional.of(existente));
        assertTrue(servicio.asignar(recepcion, pacienteId, profesionalId).activa());
    }
}
