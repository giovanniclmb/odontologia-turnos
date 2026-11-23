package ar.edu.utn.odontologia.agenda.persistencia;

import ar.edu.utn.odontologia.agenda.EstadoTurno;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TurnosPersistidos extends Repository<TurnoPersistido, UUID> {
    TurnoPersistido save(TurnoPersistido turno);

    @Query("""
            select t from TurnoPersistido t
            where t.estado = :estado and (t.profesionalId = :profesional or t.sillonId = :sillon)
              and t.inicio < :fin and t.fin > :inicio
            """)
    List<TurnoPersistido> buscarOcupantes(UUID profesional, UUID sillon, Instant inicio, Instant fin,
                                        EstadoTurno estado);

    @Query("""
            select exists (
                select t.id from TurnoPersistido t
                where t.estado = :estado and (t.profesionalId = :profesional or t.sillonId = :sillon)
                  and t.inicio < :fin and t.fin > :inicio
            )
            """)
    boolean existeOcupante(UUID profesional, UUID sillon, Instant inicio, Instant fin, EstadoTurno estado);

    @Query("""
            select t from TurnoPersistido t
            where t.sillonId = :sillon and (:profesional is null or t.profesionalId = :profesional)
              and t.inicio < :fin and t.fin > :inicio
            order by t.inicio, t.id
            """)
    List<TurnoPersistido> consultarAgendaSillon(UUID sillon, UUID profesional, Instant inicio, Instant fin);
}
