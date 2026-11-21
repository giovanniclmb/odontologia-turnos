package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="profesional",schema="odontologia")
public class ProfesionalReferencia extends Referencia {
    private Boolean activo;

    protected ProfesionalReferencia() {}
    public ProfesionalReferencia(String codigo) { super(codigo); }

    public ProfesionalReferencia(String codigo, boolean activo) {
        super(codigo);
        this.activo = activo;
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }
}
