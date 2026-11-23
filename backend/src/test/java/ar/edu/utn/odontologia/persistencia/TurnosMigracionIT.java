package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class TurnosMigracionIT {
    @Test
    void agregaTurnosSobreV6ConCuatroReferenciasYPrivilegiosMinimos() throws SQLException {
        try (var db = PostgresFixture.start()) {
            var migrador = Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false);
            migrador.target("6").load().migrate();
            assertEquals(1, migrador.target("7").load().migrate().migrationsExecuted);

            try (var con = db.appConnection()) {
                var jdbc = new JdbcTemplate(new SingleConnectionDataSource(con, true));
                UUID paciente = UUID.randomUUID();
                UUID profesional = UUID.randomUUID();
                UUID sillon = UUID.randomUUID();
                UUID prestacion = UUID.randomUUID();
                jdbc.update("insert into odontologia.paciente(id,codigo,nombre,apellido,email) values (?,'P','Ana','Ejemplo','a@example.invalid')", paciente);
                jdbc.update("insert into odontologia.profesional(id,codigo) values (?,'PRO')", profesional);
                jdbc.update("insert into odontologia.sillon(id,codigo) values (?,'S')", sillon);
                jdbc.update("insert into odontologia.prestacion(id,codigo) values (?,'PRE')", prestacion);

                var referencias = List.of(paciente, profesional, sillon, prestacion);
                insertar(con, referencias, 30, "PENDIENTE", "10:30");
                for (int i = 0; i < referencias.size(); i++) {
                    var inexistente = new java.util.ArrayList<>(referencias);
                    inexistente.set(i, UUID.randomUUID());
                    assertEquals("23503", assertThrows(SQLException.class,
                            () -> insertar(con, inexistente, 30, "PENDIENTE", "10:30")).getSQLState());
                }
                assertEquals("23514", assertThrows(SQLException.class,
                        () -> insertar(con, referencias, 0, "PENDIENTE", "10:30")).getSQLState());
                assertEquals("23514", assertThrows(SQLException.class,
                        () -> insertar(con, referencias, 30, "CANCELADO", "10:30")).getSQLState());
                assertEquals("23514", assertThrows(SQLException.class,
                        () -> insertar(con, referencias, 30, "PENDIENTE", "10:29")).getSQLState());
                try (var sql = con.createStatement()) {
                    assertEquals("42501", assertThrows(SQLException.class,
                            () -> sql.executeUpdate("update odontologia.turno set estado = 'PENDIENTE'")).getSQLState());
                    assertEquals("42501", assertThrows(SQLException.class,
                            () -> sql.executeUpdate("delete from odontologia.turno")).getSQLState());
                }
                assertEquals(1, jdbc.queryForObject("select count(*) from odontologia.turno", Integer.class));
            }
        }
    }

    private static void insertar(java.sql.Connection con, List<UUID> referencias, int minutos, String estado,
                                 String fin) throws SQLException {
        try (var sql = con.prepareStatement("""
                insert into odontologia.turno(id,paciente_id,profesional_id,sillon_id,prestacion_id,
                                               inicio,fin,duracion_minutos,estado)
                values (?,?,?,?,?,timestamptz '2030-01-01 10:00Z',cast(? as timestamptz),?,?)
                """)) {
            sql.setObject(1, UUID.randomUUID());
            for (int i = 0; i < referencias.size(); i++) {
                sql.setObject(2 + i, referencias.get(i));
            }
            sql.setString(6, "2030-01-01T" + fin + ":00Z");
            sql.setInt(7, minutos);
            sql.setString(8, estado);
            sql.executeUpdate();
        }
    }
}
