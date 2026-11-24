package ar.edu.utn.odontologia.acceso.paciente;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AutorizadorCuentaPacienteTest {
    private final AutorizadorCuentaPaciente autorizador = new AutorizadorCuentaPaciente();

    @Test
    void cuentaVinculadaOperaSobreSuPaciente() {
        UUID pacienteId = UUID.randomUUID();
        IdentidadCuentaPaciente identidad = vinculada(pacienteId);

        assertEquals(pacienteId, autorizador.exigirPacienteVinculado(identidad));
        assertDoesNotThrow(() -> autorizador.exigirPropiedad(identidad, pacienteId));
    }

    @Test
    void cuentaPendienteNoPuedeOperarLista() {
        IdentidadCuentaPaciente identidad = new IdentidadCuentaPaciente(UUID.randomUUID(),
                "pendiente@example.invalid", null, EstadoVinculoCuentaPaciente.PENDIENTE);

        assertThrows(AutorizacionDenegadaException.class,
                () -> autorizador.exigirPacienteVinculado(identidad));
        assertThrows(AutorizacionDenegadaException.class,
                () -> autorizador.exigirPropiedad(identidad, UUID.randomUUID()));
    }

    @Test
    void cuentaVinculadaNoPuedeOperarInscripcionAjena() {
        IdentidadCuentaPaciente identidad = vinculada(UUID.randomUUID());

        assertThrows(AutorizacionDenegadaException.class,
                () -> autorizador.exigirPropiedad(identidad, UUID.randomUUID()));
    }

    @Test
    void identidadAusenteSeDeniega() {
        assertThrows(AutorizacionDenegadaException.class,
                () -> autorizador.exigirPacienteVinculado(null));
    }

    private static IdentidadCuentaPaciente vinculada(UUID pacienteId) {
        return new IdentidadCuentaPaciente(UUID.randomUUID(), "vinculada@example.invalid",
                pacienteId, EstadoVinculoCuentaPaciente.VINCULADA);
    }
}
