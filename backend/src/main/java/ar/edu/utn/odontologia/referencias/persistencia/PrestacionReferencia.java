package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="prestacion",schema="odontologia")
public class PrestacionReferencia extends Referencia {
    protected PrestacionReferencia() {}
    public PrestacionReferencia(String codigo) { super(codigo); }
}
