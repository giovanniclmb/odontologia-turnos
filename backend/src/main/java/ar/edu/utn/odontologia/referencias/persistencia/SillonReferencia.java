package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="sillon",schema="odontologia")
public class SillonReferencia extends Referencia {
    private Boolean activo;

    protected SillonReferencia() {}
    public SillonReferencia(String codigo) { super(codigo); }

    public SillonReferencia(String codigo, boolean activo) {
        super(codigo);
        this.activo = activo;
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }
}
