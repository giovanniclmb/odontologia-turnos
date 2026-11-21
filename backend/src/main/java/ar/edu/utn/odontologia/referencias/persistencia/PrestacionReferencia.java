package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="prestacion",schema="odontologia")
public class PrestacionReferencia extends Referencia {
    @Column(name = "duracion_referencia_minutos")
    private Integer duracionReferenciaMinutos;

    protected PrestacionReferencia() {}
    public PrestacionReferencia(String codigo) { super(codigo); }

    public PrestacionReferencia(String codigo, int duracionReferenciaMinutos) {
        super(codigo);
        this.duracionReferenciaMinutos = duracionReferenciaMinutos;
    }

    public Integer getDuracionReferenciaMinutos() {
        return duracionReferenciaMinutos;
    }

    public void ajustarDuracion(int minutos) {
        this.duracionReferenciaMinutos = minutos;
    }
}
