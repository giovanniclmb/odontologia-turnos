package ar.edu.utn.odontologia.agenda.persistencia;

import jakarta.persistence.EntityManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TokensCancelacionPersistidos {
    private final JdbcClient jdbc;
    private final EntityManager entidades;

    public TokensCancelacionPersistidos(JdbcClient jdbc, EntityManager entidades) {
        this.jdbc = jdbc;
        this.entidades = entidades;
    }

    @Transactional
    public void guardar(UUID turnoId, String hash, long version, java.time.Instant creadoEn) {
        entidades.flush();
        jdbc.sql("""
                insert into odontologia.token_cancelacion
                    (id, turno_id, hash_token, version_turno, consumido, creado_en)
                values (:id, :turno, :hash, :version, false, :creado)
                """)
                .param("id", UUID.randomUUID())
                .param("turno", turnoId)
                .param("hash", hash)
                .param("version", version)
                .param("creado", creadoEn.atOffset(ZoneOffset.UTC))
                .update();
    }

    @Transactional(readOnly = true)
    public Optional<TokenCancelacionLeido> buscarPorHash(String hash) {
        entidades.flush();
        return jdbc.sql("""
                select id, turno_id, hash_token, version_turno, consumido, creado_en
                from odontologia.token_cancelacion
                where hash_token = :hash
                """)
                .param("hash", hash)
                .query(TokensCancelacionPersistidos::leer)
                .optional();
    }

    @Transactional
    public Optional<TokenCancelacionLeido> bloquearPorHash(String hash) {
        entidades.flush();
        return jdbc.sql("""
                select id, turno_id, hash_token, version_turno, consumido, creado_en
                from odontologia.token_cancelacion
                where hash_token = :hash
                for update
                """)
                .param("hash", hash)
                .query(TokensCancelacionPersistidos::leer)
                .optional();
    }

    @Transactional
    public Optional<TokenCancelacionLeido> bloquearPorTurno(UUID turnoId) {
        entidades.flush();
        return jdbc.sql("""
                select id, turno_id, hash_token, version_turno, consumido, creado_en
                from odontologia.token_cancelacion
                where turno_id = :turno
                for update
                """)
                .param("turno", turnoId)
                .query(TokensCancelacionPersistidos::leer)
                .optional();
    }

    @Transactional
    public int consumir(UUID id, java.time.Instant consumidoEn) {
        return jdbc.sql("""
                update odontologia.token_cancelacion
                set consumido = true, consumido_en = :cuando
                where id = :id and consumido = false
                """)
                .param("id", id)
                .param("cuando", consumidoEn.atOffset(ZoneOffset.UTC))
                .update();
    }

    @Transactional
    public void eliminarPorTurno(UUID turnoId) {
        jdbc.sql("delete from odontologia.token_cancelacion where turno_id = :turno")
                .param("turno", turnoId)
                .update();
    }

    private static TokenCancelacionLeido leer(ResultSet fila, int numeroFila) throws SQLException {
        return new TokenCancelacionLeido(
                fila.getObject("id", UUID.class),
                fila.getObject("turno_id", UUID.class),
                fila.getString("hash_token"),
                fila.getLong("version_turno"),
                fila.getBoolean("consumido"),
                fila.getObject("creado_en", OffsetDateTime.class).toInstant());
    }
}