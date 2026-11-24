package ar.edu.utn.odontologia.acceso.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentidadCuentaPacienteTest {
    @Test
    void identidadPendienteNoIncluyePaciente() {
        var identidad = new IdentidadCuentaPaciente(UUID.randomUUID(), "pendiente@example.invalid",
                null, EstadoVinculoCuentaPaciente.PENDIENTE);

        assertEquals(EstadoVinculoCuentaPaciente.PENDIENTE, identidad.estadoVinculo());
        assertNull(identidad.pacienteId());
    }

    @Test
    void identidadVinculadaIncluyePaciente() {
        UUID pacienteId = UUID.randomUUID();
        var identidad = new IdentidadCuentaPaciente(UUID.randomUUID(), "vinculada@example.invalid",
                pacienteId, EstadoVinculoCuentaPaciente.VINCULADA);

        assertEquals(EstadoVinculoCuentaPaciente.VINCULADA, identidad.estadoVinculo());
        assertEquals(pacienteId, identidad.pacienteId());
    }

    @Test
    void estadoYReferenciaDebenSerCoherentes() {
        assertThrows(IllegalArgumentException.class, () -> new IdentidadCuentaPaciente(
                UUID.randomUUID(), "pendiente@example.invalid", UUID.randomUUID(),
                EstadoVinculoCuentaPaciente.PENDIENTE));
        assertThrows(IllegalArgumentException.class, () -> new IdentidadCuentaPaciente(
                UUID.randomUUID(), "vinculada@example.invalid", null,
                EstadoVinculoCuentaPaciente.VINCULADA));
    }

    @Test
    void identidadPacienteQuedaFueraDeRolesInternos() {
        assertEquals(List.of(Rol.RECEPCION, Rol.ODONTOLOGO), List.of(Rol.values()));
        assertNotEquals(Identidad.class, IdentidadCuentaPaciente.class);
    }
}
