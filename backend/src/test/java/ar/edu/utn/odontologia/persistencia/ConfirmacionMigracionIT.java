package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class ConfirmacionMigracionIT {
    @Test
    void conservaElTurnoPrevioYPermiteConfirmarlo() throws SQLException {
        try (var db = PostgresFixture.start()) {
            var migrador = Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false);
            migrador.target("10").load().migrate();
            assertEquals(1, migrador.target("11").load().migrate().migrationsExecuted,
                    "V11 debe conservar la migración de email ya integrada");
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                var jdbc = new JdbcTemplate(new SingleConnectionDataSource(con, true));
                var paciente = UUID.randomUUID();
                var profesional = UUID.randomUUID();
                var sillon = UUID.randomUUID();
                var prestacion = UUID.randomUUID();
                var turno = UUID.randomUUID();
                jdbc.update("""
                        insert into odontologia.paciente (id, codigo, nombre, apellido, email)
                        values (?, 'P-C09', 'Ana', 'Ejemplo', 'ana@example.invalid')
                        """, paciente);
                jdbc.update("insert into odontologia.profesional (id, codigo) values (?, 'PRO-C09')", profesional);
                jdbc.update("insert into odontologia.sillon (id, codigo) values (?, 'S-C09')", sillon);
                jdbc.update("insert into odontologia.prestacion (id, codigo) values (?, 'PRE-C09')", prestacion);
                var inicio = OffsetDateTime.parse("2040-04-05T10:00:00.123456Z");
                jdbc.update("""
                        insert into odontologia.turno (id, paciente_id, profesional_id, sillon_id, prestacion_id,
                                                       inicio, fin, duracion_minutos, estado, version)
                        values (?, ?, ?, ?, ?, ?, ?, 30, 'PENDIENTE', 4)
                        """, turno, paciente, profesional, sillon, prestacion, inicio, inicio.plusMinutes(30));
                String consulta = "select to_jsonb(t)::text from odontologia.turno t where id = ?";
                String filaAnterior = jdbc.queryForObject(consulta, String.class, turno);
                assertEquals("23514", assertThrows(SQLException.class, () -> sql.executeUpdate(
                        "update odontologia.turno set estado = 'CONFIRMADO'")).getSQLState());

                assertEquals(1, migrador.target("12").load().migrate().migrationsExecuted,
                        "Debe existir una migración posterior a V11 que habilite la confirmación");
                assertEquals(filaAnterior, jdbc.queryForObject(consulta, String.class, turno));
                assertEquals(1, jdbc.update("""
                        update odontologia.turno set estado = 'CONFIRMADO', version = version + 1
                        where id = ? and estado = 'PENDIENTE' and version = 4
                        """, turno));
                assertEquals("CONFIRMADO", jdbc.queryForObject(
                        "select estado from odontologia.turno where id = ?", String.class, turno));
                assertEquals(5L, jdbc.queryForObject(
                        "select version from odontologia.turno where id = ?", Long.class, turno));

                assertEquals("23514", assertThrows(SQLException.class, () -> sql.executeUpdate(
                        "update odontologia.turno set estado = 'DESCONOCIDO'")).getSQLState());
                for (String columna : List.of("id", "paciente_id", "prestacion_id", "duracion_minutos")) {
                    assertEquals("42501", assertThrows(SQLException.class, () -> sql.executeUpdate(
                            "update odontologia.turno set " + columna + " = " + columna)).getSQLState(), columna);
                }
                assertEquals("42501", assertThrows(SQLException.class, () -> sql.executeUpdate(
                        "delete from odontologia.turno")).getSQLState());
            }
        }
    }
}
