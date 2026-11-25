package ar.edu.utn.odontologia.acceso.paciente;

import java.util.UUID;

public record VinculacionCuentaPaciente(UUID cuentaId, UUID pacienteId) {
}
