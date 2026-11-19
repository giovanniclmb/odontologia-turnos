package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="profesional",schema="odontologia")
public class ProfesionalReferencia extends Referencia {
    protected ProfesionalReferencia() {}
    public ProfesionalReferencia(String codigo) { super(codigo); }
}
