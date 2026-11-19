package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="paciente",schema="odontologia")
public class PacienteReferencia extends Referencia {
    protected PacienteReferencia() {}
    public PacienteReferencia(String codigo) { super(codigo); }
}
