package ar.edu.utn.odontologia.acceso.paciente;

import java.util.Objects;
import java.util.UUID;

public record IdentidadCuentaPaciente(
        UUID cuentaId,
        String email,
        UUID pacienteId,
        EstadoVinculoCuentaPaciente estadoVinculo) {

    public IdentidadCuentaPaciente {
        Objects.requireNonNull(cuentaId, "La cuenta es obligatoria");
        Objects.requireNonNull(email, "El email es obligatorio");
        Objects.requireNonNull(estadoVinculo, "El estado del vínculo es obligatorio");
        if (estadoVinculo == EstadoVinculoCuentaPaciente.PENDIENTE && pacienteId != null) {
            throw new IllegalArgumentException("Una cuenta pendiente no puede tener paciente vinculado");
        }
        if (estadoVinculo == EstadoVinculoCuentaPaciente.VINCULADA && pacienteId == null) {
            throw new IllegalArgumentException("Una cuenta vinculada requiere paciente");
        }
    }
}
