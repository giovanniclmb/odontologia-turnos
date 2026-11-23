package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PacienteProfesionalId implements Serializable {
    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;

    protected PacienteProfesionalId() {}

    public PacienteProfesionalId(UUID pacienteId, UUID profesionalId) {
        this.pacienteId = Objects.requireNonNull(pacienteId);
        this.profesionalId = Objects.requireNonNull(profesionalId);
    }

    public UUID getPacienteId() { return pacienteId; }
    public UUID getProfesionalId() { return profesionalId; }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof PacienteProfesionalId id)) return false;
        return Objects.equals(pacienteId, id.pacienteId) && Objects.equals(profesionalId, id.profesionalId);
    }

    @Override
    public int hashCode() { return Objects.hash(pacienteId, profesionalId); }
}
