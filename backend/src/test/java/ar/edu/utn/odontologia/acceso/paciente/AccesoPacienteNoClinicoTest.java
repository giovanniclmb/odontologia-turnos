package ar.edu.utn.odontologia.acceso.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioFichaClinica;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccesoPacienteNoClinicoTest {
    @Test
    void operacionesClinicasAdministrativasYDeAgendaExigenIdentidadInterna() throws Exception {
        assertPrimeraIdentidadInterna(ServicioFichaClinica.class.getMethod("consultar", Identidad.class, UUID.class));
        assertPrimeraIdentidadInterna(ServicioFichaClinica.class.getMethod(
                "actualizarAntecedentes", Identidad.class, UUID.class, String.class));
        assertPrimeraIdentidadInterna(ServicioPacientes.class.getMethod("consultar", Identidad.class, UUID.class));
        assertPrimeraIdentidadInterna(ServicioAsignacionPacientes.class.getMethod(
                "asignar", Identidad.class, UUID.class, UUID.class));
        assertPrimeraIdentidadInterna(ServicioAgenda.class.getMethod(
                "crear", Identidad.class, ServicioAgenda.SolicitudAlta.class));
        assertPrimeraIdentidadInterna(ServicioAgenda.class.getMethod(
                "consultarAgendaSillon", Identidad.class, UUID.class,
                java.time.Instant.class, java.time.Instant.class));
        assertPrimeraIdentidadInterna(ServicioAutorizacion.class.getMethod(
                "exigir", Identidad.class, ar.edu.utn.odontologia.acceso.Permiso.class));
    }

    private static void assertPrimeraIdentidadInterna(Method method) {
        assertEquals(Identidad.class, method.getParameterTypes()[0],
                () -> method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }
}
