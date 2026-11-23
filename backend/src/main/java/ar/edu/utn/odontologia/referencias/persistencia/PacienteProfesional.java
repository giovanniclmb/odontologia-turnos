package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "paciente_profesional", schema = "odontologia")
public class PacienteProfesional {
    @EmbeddedId
    private PacienteProfesionalId id;

    @Column(nullable = false)
    private boolean activo;

    protected PacienteProfesional() {}

    public PacienteProfesional(PacienteProfesionalId id, boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    public PacienteProfesionalId getId() { return id; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
