package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class HorarioFijadoMigracionIT {
    @Test
    void agregaElDatoVacioAlTurnoPrevioYPermiteEscribirSoloEsaColumnaNueva() throws SQLException {
        try (var db = PostgresFixture.start()) {
            var migrador = Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false);
            migrador.target("13").load().migrate();
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                var jdbc = new JdbcTemplate(new SingleConnectionDataSource(con, true));
                var paciente = UUID.randomUUID();
                var profesional = UUID.randomUUID();
                var sillon = UUID.randomUUID();
                var prestacion = UUID.randomUUID();
                var turno = UUID.randomUUID();
                jdbc.update("""
                        insert into odontologia.paciente (id, codigo, nombre, apellido, email)
                        values (?, 'P-C20', 'Ana', 'Ejemplo', 'ana@example.invalid')
                        """, paciente);
                jdbc.update("insert into odontologia.profesional (id, codigo) values (?, 'PRO-C20')", profesional);
                jdbc.update("insert into odontologia.sillon (id, codigo) values (?, 'S-C20')", sillon);
                jdbc.update("insert into odontologia.prestacion (id, codigo) values (?, 'PRE-C20')", prestacion);
                var inicio = OffsetDateTime.parse("2040-04-05T10:00:00Z");
                jdbc.update("""
                        insert into odontologia.turno (id, paciente_id, profesional_id, sillon_id, prestacion_id,
                                                       inicio, fin, duracion_minutos, estado, version)
                        values (?, ?, ?, ?, ?, ?, ?, 30, 'CONFIRMADO', 2)
                        """, turno, paciente, profesional, sillon, prestacion, inicio, inicio.plusMinutes(30));
                String consulta = "select (to_jsonb(t) - 'horario_fijado_en')::text "
                        + "from odontologia.turno t where id = ?";
                String filaAnterior = jdbc.queryForObject(consulta, String.class, turno);

                assertEquals(1, migrador.target("14").load().migrate().migrationsExecuted,
                        "Debe existir la migración posterior a V13");

                assertEquals(filaAnterior, jdbc.queryForObject(consulta, String.class, turno));
                assertNull(jdbc.queryForObject("select horario_fijado_en from odontologia.turno where id = ?",
                        OffsetDateTime.class, turno));
                assertEquals(1, jdbc.update("update odontologia.turno set horario_fijado_en = ? where id = ?",
                        inicio.minusDays(3), turno));
                assertEquals("42501", assertThrows(SQLException.class, () -> sql.executeUpdate(
                        "update odontologia.turno set paciente_id = paciente_id")).getSQLState());
                assertEquals(1, jdbc.queryForObject("""
                        select count(*) from pg_indexes
                        where schemaname = 'odontologia' and indexname = 'turno_inicio_idx'
                        """, Integer.class));
            }
        }
    }
}
