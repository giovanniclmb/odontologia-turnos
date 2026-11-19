package ar.edu.utn.odontologia.referencias.persistencia;

import java.util.UUID;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class Referencia {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @Column(nullable=false)
    private UUID id;
    @Column(nullable=false, columnDefinition="text")
    private String codigo;

    protected Referencia() {}
    protected Referencia(String codigo) { this.codigo=codigo; }
    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
}
