package ar.edu.utn.odontologia.pacientes.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ficha_clinica", schema = "odontologia")
public class FichaClinica {
    @Id
    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(columnDefinition = "text")
    private String antecedentes;

    protected FichaClinica() {}

    public FichaClinica(UUID pacienteId, String antecedentes) {
        this.pacienteId = pacienteId;
        this.antecedentes = antecedentes;
    }

    public UUID getPacienteId() { return pacienteId; }
    public String getAntecedentes() { return antecedentes; }
    public void setAntecedentes(String antecedentes) { this.antecedentes = antecedentes; }
}
