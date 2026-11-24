package ar.edu.utn.odontologia.acceso.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cuenta_paciente", schema = "odontologia")
public class CuentaPaciente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, columnDefinition = "text", unique = true)
    private String email;

    @Column(name = "hash_credencial", nullable = false, columnDefinition = "text")
    private String hashCredencial;

    @Column(name = "paciente_ref", unique = true)
    private UUID pacienteRef;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected CuentaPaciente() {
    }

    public CuentaPaciente(String email, String hashCredencial) {
        this(email, hashCredencial, null);
    }

    public CuentaPaciente(String email, String hashCredencial, UUID pacienteRef) {
        this.email = email;
        this.hashCredencial = hashCredencial;
        this.pacienteRef = pacienteRef;
        this.creadoEn = Instant.now();
    }

    public void confirmarVinculo(UUID pacienteId) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("La referencia del paciente es obligatoria");
        }
        if (pacienteRef != null) {
            throw new IllegalStateException("La cuenta ya está vinculada a un paciente");
        }
        pacienteRef = pacienteId;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getHashCredencial() {
        return hashCredencial;
    }

    public UUID getPacienteRef() {
        return pacienteRef;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
