package ar.edu.utn.odontologia.agenda.persistencia;

import ar.edu.utn.odontologia.agenda.Agenda;
import ar.edu.utn.odontologia.agenda.EstadoTurno;
import jakarta.persistence.EntityManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EdicionesTurnoPersistidas {
    private static final String COLUMNAS = """
            t.id, t.paciente_id, t.profesional_id, t.sillon_id, t.prestacion_id,
            t.inicio, t.fin, t.duracion_minutos, t.estado, t.version
            """;

    private final JdbcClient jdbc;
    private final EntityManager entidades;

    public EdicionesTurnoPersistidas(JdbcClient jdbc, EntityManager entidades) {
        this.jdbc = jdbc;
        this.entidades = entidades;
    }

    public Optional<TurnoLeido> bloquear(UUID id, UUID profesionalOdontologo) {
        // La lectura JDBC debe ver también las altas JPA de la transacción actual.
        entidades.flush();
        return jdbc.sql("select " + COLUMNAS + """
                from odontologia.turno t
                where t.id = :id and (cast(:profesional as uuid) is null or (
                    t.profesional_id = :profesional and exists (
                        select 1 from odontologia.paciente_profesional a
                        where a.paciente_id = t.paciente_id and a.profesional_id = t.profesional_id and a.activo
                    )
                ))
                for no key update of t
                """)
                .param("id", id)
                .param("profesional", profesionalOdontologo)
                .query(EdicionesTurnoPersistidas::leer)
                .optional();
    }

    public Optional<TurnoLeido> mover(UUID id, long version, Agenda.Turno turno) {
        return jdbc.sql("""
                update odontologia.turno t
                set profesional_id = :profesional, sillon_id = :sillon,
                    inicio = :inicio, fin = :fin, estado = 'PENDIENTE', version = t.version + 1
                where t.id = :id and t.version = :version
                returning
                """ + COLUMNAS)
                .param("id", id)
                .param("version", version)
                .param("profesional", UUID.fromString(turno.profesional()))
                .param("sillon", UUID.fromString(turno.sillon()))
                .param("inicio", turno.inicio().atOffset(ZoneOffset.UTC))
                .param("fin", turno.fin().atOffset(ZoneOffset.UTC))
                .query(EdicionesTurnoPersistidas::leer)
                .optional();
    }

    public Optional<TurnoLeido> cambiarEstado(UUID id, long version, EstadoTurno origen, EstadoTurno destino) {
        return jdbc.sql("""
                update odontologia.turno t
                set estado = :destino, version = t.version + 1
                where t.id = :id and t.version = :version and t.estado = :origen
                returning
                """ + COLUMNAS)
                .param("id", id)
                .param("version", version)
                .param("origen", origen.name())
                .param("destino", destino.name())
                .query(EdicionesTurnoPersistidas::leer)
                .optional();
    }

    private static TurnoLeido leer(ResultSet fila, int numeroFila) throws SQLException {
        return new TurnoLeido(fila.getObject("id", UUID.class), fila.getObject("paciente_id", UUID.class),
                fila.getObject("profesional_id", UUID.class), fila.getObject("sillon_id", UUID.class),
                fila.getObject("prestacion_id", UUID.class), fila.getObject("inicio", OffsetDateTime.class).toInstant(),
                fila.getObject("fin", OffsetDateTime.class).toInstant(), fila.getInt("duracion_minutos"),
                EstadoTurno.valueOf(fila.getString("estado")), fila.getLong("version"));
    }
}
