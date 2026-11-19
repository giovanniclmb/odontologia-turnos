package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="sillon",schema="odontologia")
public class SillonReferencia extends Referencia {
    protected SillonReferencia() {}
    public SillonReferencia(String codigo) { super(codigo); }
}
