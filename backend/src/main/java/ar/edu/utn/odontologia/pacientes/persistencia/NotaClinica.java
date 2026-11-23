package ar.edu.utn.odontologia.pacientes.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nota_clinica", schema = "odontologia")
public class NotaClinica {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "autor_usuario_id", nullable = false)
    private UUID autorUsuarioId;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    protected NotaClinica() {}

    public NotaClinica(UUID id, UUID pacienteId, UUID autorUsuarioId, String texto, Instant creadaEn) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.autorUsuarioId = autorUsuarioId;
        this.texto = texto;
        this.creadaEn = creadaEn;
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getAutorUsuarioId() { return autorUsuarioId; }
    public String getTexto() { return texto; }
    public Instant getCreadaEn() { return creadaEn; }
    public void setTexto(String texto) { this.texto = texto; }
}
