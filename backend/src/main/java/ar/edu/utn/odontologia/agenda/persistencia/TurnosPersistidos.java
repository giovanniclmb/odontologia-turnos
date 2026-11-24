package ar.edu.utn.odontologia.agenda.persistencia;

import ar.edu.utn.odontologia.agenda.EstadoTurno;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TurnosPersistidos extends Repository<TurnoPersistido, UUID> {
    // Las proyecciones leen la fila actual aun si el contexto JPA contiene una entidad anterior al UPDATE explícito.
    String PROYECTAR_TURNO = """
            select new ar.edu.utn.odontologia.agenda.persistencia.TurnoLeido(t.id, t.pacienteId, t.profesionalId,
                t.sillonId, t.prestacionId, t.inicio, t.fin, t.duracionMinutos, t.estado, t.version)
            from TurnoPersistido t
            """;

    TurnoPersistido save(TurnoPersistido turno);

    @Query(PROYECTAR_TURNO + """
            where t.estado = :estado and (t.profesionalId = :profesional or t.sillonId = :sillon)
              and t.inicio < :fin and t.fin > :inicio
              and (:turnoExcluido is null or t.id <> :turnoExcluido)
            """)
    List<TurnoLeido> buscarOcupantes(UUID profesional, UUID sillon, Instant inicio, Instant fin,
                                   EstadoTurno estado, UUID turnoExcluido);

    @Query("""
            select exists (
                select t.id from TurnoPersistido t
                where t.estado = :estado and (t.profesionalId = :profesional or t.sillonId = :sillon)
                  and t.inicio < :fin and t.fin > :inicio
            )
            """)
    boolean existeOcupante(UUID profesional, UUID sillon, Instant inicio, Instant fin, EstadoTurno estado);

    @Query(PROYECTAR_TURNO + """
            where t.sillonId = :sillon and (:profesional is null or t.profesionalId = :profesional)
              and t.inicio < :fin and t.fin > :inicio
            order by t.inicio, t.id
            """)
    List<TurnoLeido> consultarAgendaSillon(UUID sillon, UUID profesional, Instant inicio, Instant fin);
}
