package ar.edu.utn.odontologia.comunicaciones.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_envio", schema = "odontologia")
public class EmailEnvio {
    @Id
    private UUID id;
    @Column(name = "tipo", nullable = false)
    private String tipo;
    @Column(name = "turno_id")
    private UUID turnoId;
    @Column(name = "destinatario", nullable = false)
    private String destinatario;
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;
    @Column(name = "estado", nullable = false)
    private String estado;
    @Column(name = "intentos", nullable = false)
    private int intentos;
    @Column(name = "error")
    private String error;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
    @Column(name = "enviado_en")
    private Instant enviadoEn;

    protected EmailEnvio() {}

    public EmailEnvio(String tipo, UUID turnoId, String destinatario, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.turnoId = turnoId;
        this.destinatario = destinatario;
        this.idempotencyKey = idempotencyKey;
        this.estado = "pending";
        this.intentos = 0;
        this.creadoEn = Instant.now();
    }

    public void marcar(boolean exitoso, int intentos, String error, Instant enviadoEn) {
        this.estado = exitoso ? "sent" : "failed";
        this.intentos = intentos;
        this.error = error;
        this.enviadoEn = enviadoEn;
    }

    public UUID id() { return id; }
    public String tipo() { return tipo; }
    public UUID turnoId() { return turnoId; }
    public String destinatario() { return destinatario; }
    public String idempotencyKey() { return idempotencyKey; }
    public String estado() { return estado; }
    public int intentos() { return intentos; }
    public String error() { return error; }
    public Instant creadoEn() { return creadoEn; }
    public Instant enviadoEn() { return enviadoEn; }
}