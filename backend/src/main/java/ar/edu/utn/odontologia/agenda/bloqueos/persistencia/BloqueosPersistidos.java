package ar.edu.utn.odontologia.agenda.bloqueos.persistencia;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface BloqueosPersistidos extends Repository<BloqueoPersistido, UUID> {
    BloqueoPersistido save(BloqueoPersistido bloqueo);

    @Query("""
            select b from BloqueoPersistido b
            where (b.profesionalId = :profesionalId or b.sillonId = :sillonId)
              and b.inicio < :fin and b.fin > :inicio
            order by b.inicio, b.id
            """)
    List<BloqueoPersistido> buscar(UUID profesionalId, UUID sillonId, Instant inicio, Instant fin);
}
