package ar.edu.utn.odontologia.agenda.persistencia;

import ar.edu.utn.odontologia.agenda.Agenda;
import ar.edu.utn.odontologia.agenda.EstadoTurno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "turno", schema = "odontologia")
public class TurnoPersistido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;
    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;
    @Column(name = "sillon_id", nullable = false)
    private UUID sillonId;
    @Column(name = "prestacion_id", nullable = false)
    private UUID prestacionId;
    @Column(nullable = false)
    private Instant inicio;
    @Column(nullable = false)
    private Instant fin;
    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;
    @Column(nullable = false, updatable = false)
    private long version;

    protected TurnoPersistido() {}

    public TurnoPersistido(Agenda.Turno turno) {
        pacienteId = UUID.fromString(turno.paciente());
        profesionalId = UUID.fromString(turno.profesional());
        sillonId = UUID.fromString(turno.sillon());
        prestacionId = UUID.fromString(turno.prestacion());
        inicio = turno.inicio();
        fin = turno.fin();
        duracionMinutos = Math.toIntExact(turno.duracion().toMinutes());
        estado = EstadoTurno.PENDIENTE;
    }

    public UUID id() { return id; }
    public UUID pacienteId() { return pacienteId; }
    public UUID profesionalId() { return profesionalId; }
    public UUID sillonId() { return sillonId; }
    public UUID prestacionId() { return prestacionId; }
    public Instant inicio() { return inicio; }
    public Instant fin() { return fin; }
    public int duracionMinutos() { return duracionMinutos; }
    public EstadoTurno estado() { return estado; }

    public Agenda.Reserva reserva() {
        return new Agenda.Reserva(profesionalId.toString(), sillonId.toString(), inicio, fin);
    }

    public TurnoLeido datos() {
        return new TurnoLeido(id, pacienteId, profesionalId, sillonId, prestacionId, inicio, fin,
                duracionMinutos, estado, version);
    }
}
