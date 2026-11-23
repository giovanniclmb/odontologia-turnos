package ar.edu.utn.odontologia.pacientes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioPacientesTest {
    private final PacienteReferencias pacientes = mock(PacienteReferencias.class);
    private final PacienteProfesionales asignaciones = mock(PacienteProfesionales.class);
    private final ServicioAutorizacion autorizacion = new ServicioAutorizacion();
    private ServicioPacientes servicio;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID profesionalId = UUID.randomUUID();
    private final Identidad recepcion = new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
    private final Identidad odontologo = new Identidad(UUID.randomUUID(), "odontologo-ficticio", Rol.ODONTOLOGO, profesionalId);

    @BeforeEach
    void iniciar() {
        servicio = new ServicioPacientes(pacientes, asignaciones, autorizacion);
    }

    @Test
    void ambosRolesPuedenCrearYElAltaOdontologicaQuedaSinAsignar() {
        when(pacientes.save(any(PacienteReferencia.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        var solicitud = new ServicioPacientes.CrearPaciente(
                "PAC-TEST", "Ana", "Ejemplo", "ana@example.invalid", null);

        var creado = servicio.crear(odontologo, solicitud);

        assertEquals("PAC-TEST", creado.codigo());
        assertEquals("Ana", creado.nombre());
        verify(asignaciones, never()).save(any());
        verify(pacientes).save(any(PacienteReferencia.class));
    }

    @Test
    void rechazaDatosAdministrativosInvalidosAntesDePersistir() {
        var invalido = new ServicioPacientes.CrearPaciente(
                "PAC-TEST", " ", "Ejemplo", "correo-invalido", null);

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(recepcion, invalido));

        verifyNoInteractions(pacientes);
    }

    @Test
    void permiteEmailsCompartidos() {
        when(pacientes.save(any(PacienteReferencia.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        var primero = new ServicioPacientes.CrearPaciente(
                "PAC-1", "Ana", "Uno", "compartido@example.invalid", null);
        var segundo = new ServicioPacientes.CrearPaciente(
                "PAC-2", "Bea", "Dos", "compartido@example.invalid", null);

        assertDoesNotThrow(() -> servicio.crear(recepcion, primero));
        assertDoesNotThrow(() -> servicio.crear(recepcion, segundo));

        verify(pacientes, times(2)).save(any(PacienteReferencia.class));
    }

    @Test
    void recepcionTieneAlcanceGlobalYOdontologiaRequiereAsignacionActiva() {
        var paciente = mock(PacienteReferencia.class);
        when(paciente.getId()).thenReturn(pacienteId);
        when(paciente.getCodigo()).thenReturn("PAC-TEST");
        when(paciente.getNombre()).thenReturn("Ana");
        when(paciente.getApellido()).thenReturn("Ejemplo");
        when(paciente.getEmail()).thenReturn("ana@example.invalid");
        when(pacientes.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(asignaciones.tieneAsignacionActiva(pacienteId, profesionalId)).thenReturn(true, false);

        assertDoesNotThrow(() -> servicio.consultar(recepcion, pacienteId));
        assertDoesNotThrow(() -> servicio.consultar(odontologo, pacienteId));
        assertThrows(AutorizacionDenegadaException.class, () -> servicio.consultar(odontologo, pacienteId));
    }

    @Test
    void resultadosAdministrativosNoExponenInformacionClinica() {
        var componentes = ServicioPacientes.ResumenPaciente.class.getRecordComponents();

        assertNotNull(componentes);
        assertTrue(List.of(componentes).stream().noneMatch(componente ->
                componente.getName().contains("antecedente") || componente.getName().contains("nota")));
    }

    @Test
    void busquedaAplicaAlcanceGlobalDeRecepcionYProfesionalDeOdontologia() {
        when(pacientes.buscarEnAlcance(null, "ana", null)).thenReturn(List.of());
        when(pacientes.buscarEnAlcance(null, "ana", profesionalId)).thenReturn(List.of());

        servicio.buscar(recepcion, new ServicioPacientes.FiltroBusquedaPaciente(null, "ana"));
        servicio.buscar(odontologo, new ServicioPacientes.FiltroBusquedaPaciente(null, "ana"));

        verify(pacientes).buscarEnAlcance(null, "ana", null);
        verify(pacientes).buscarEnAlcance(null, "ana", profesionalId);
    }

    @Test
    void odontologoSinVinculoNoPuedeBuscarPacientes() {
        Identidad sinVinculo = new Identidad(UUID.randomUUID(), "odontologo-sin-vinculo", Rol.ODONTOLOGO, null);

        assertTrue(servicio.buscar(sinVinculo, new ServicioPacientes.FiltroBusquedaPaciente(null, "ana")).isEmpty());

        verifyNoInteractions(pacientes);
    }
}
