package ar.edu.utn.odontologia.agenda.bloqueos.persistencia;

import ar.edu.utn.odontologia.agenda.bloqueos.Bloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bloqueo_recurso")
public class BloqueoPersistido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "profesional_id")
    private UUID profesionalId;
    @Column(name = "sillon_id")
    private UUID sillonId;
    private Instant inicio;
    private Instant fin;

    protected BloqueoPersistido() {}

    public BloqueoPersistido(RecursosBloqueo recursos, PeriodoBloqueo periodo) {
        profesionalId = recursos.profesionalId();
        sillonId = recursos.sillonId();
        inicio = periodo.inicio();
        fin = periodo.fin();
    }

    public Bloqueo aBloqueo() {
        return new Bloqueo(id, new RecursosBloqueo(profesionalId, sillonId), new PeriodoBloqueo(inicio, fin));
    }
}
