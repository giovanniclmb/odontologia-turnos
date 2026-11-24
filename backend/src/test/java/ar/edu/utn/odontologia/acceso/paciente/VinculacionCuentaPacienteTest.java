package ar.edu.utn.odontologia.acceso.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioCredenciales;
import ar.edu.utn.odontologia.acceso.persistencia.CuentaPaciente;
import ar.edu.utn.odontologia.acceso.persistencia.CuentasPacientes;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VinculacionCuentaPacienteTest {
    private final CuentasPacientes cuentas = mock(CuentasPacientes.class);
    private final PacienteReferencias pacientes = mock(PacienteReferencias.class);
    private final ServicioCuentasPaciente servicio = new ServicioCuentasPaciente(
            cuentas, new ServicioCredenciales(), pacientes);

    @Test
    void recepcionVinculaCuentaPendienteConFichaExistente() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        CuentaPaciente cuenta = cuentaPendiente(cuentaId);
        when(cuentas.buscarParaVincular(cuentaId)).thenReturn(Optional.of(cuenta));
        when(pacientes.findById(pacienteId)).thenReturn(Optional.of(mock(PacienteReferencia.class)));
        when(cuentas.findByPacienteRef(pacienteId)).thenReturn(Optional.empty());
        when(cuentas.save(cuenta)).thenReturn(cuenta);

        IdentidadCuentaPaciente identidad = servicio.confirmarVinculo(
                identidad(Rol.RECEPCION), new VinculacionCuentaPaciente(cuentaId, pacienteId));

        assertEquals(pacienteId, identidad.pacienteId());
        assertEquals(EstadoVinculoCuentaPaciente.VINCULADA, identidad.estadoVinculo());
    }

    @Test
    void odontologoNoPuedeVincularCuenta() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        assertThrows(AutorizacionDenegadaException.class, () -> servicio.confirmarVinculo(
                identidad(Rol.ODONTOLOGO), new VinculacionCuentaPaciente(cuentaId, UUID.randomUUID())));
    }

    @Test
    void rechazaFichaYaVinculadaAOtraCuenta() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        when(cuentas.buscarParaVincular(cuentaId)).thenReturn(Optional.of(cuentaPendiente(cuentaId)));
        when(pacientes.findById(pacienteId)).thenReturn(Optional.of(mock(PacienteReferencia.class)));
        when(cuentas.findByPacienteRef(pacienteId)).thenReturn(Optional.of(cuentaPendiente(UUID.randomUUID())));

        assertThrows(IllegalArgumentException.class, () -> servicio.confirmarVinculo(
                identidad(Rol.RECEPCION), new VinculacionCuentaPaciente(cuentaId, pacienteId)));
    }

    @Test
    void rechazaFichaInexistente() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        when(cuentas.buscarParaVincular(cuentaId)).thenReturn(Optional.of(cuentaPendiente(cuentaId)));
        when(pacientes.findById(pacienteId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.confirmarVinculo(
                identidad(Rol.RECEPCION), new VinculacionCuentaPaciente(cuentaId, pacienteId)));
    }

    @Test
    void rechazaCuentaNoPendiente() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        UUID pacienteActual = UUID.randomUUID();
        CuentaPaciente cuenta = new CuentaPaciente("vinculada@example.invalid", "hash", pacienteActual);
        asignarId(cuenta, cuentaId);
        when(cuentas.buscarParaVincular(cuentaId)).thenReturn(Optional.of(cuenta));

        assertThrows(IllegalArgumentException.class, () -> servicio.confirmarVinculo(
                identidad(Rol.RECEPCION), new VinculacionCuentaPaciente(cuentaId, UUID.randomUUID())));
    }

    private static CuentaPaciente cuentaPendiente(UUID id) throws Exception {
        CuentaPaciente cuenta = new CuentaPaciente("cuenta-" + id + "@example.invalid", "hash");
        asignarId(cuenta, id);
        return cuenta;
    }

    private static void asignarId(CuentaPaciente cuenta, UUID id) throws Exception {
        Field campo = CuentaPaciente.class.getDeclaredField("id");
        campo.setAccessible(true);
        campo.set(cuenta, id);
    }

    private static Identidad identidad(Rol rol) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio", rol, null);
    }
}
