package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class BloqueosMigracionIT {
    @Test
    void agregaBloqueosSobreV5ConReferenciasRestriccionesYPermisos() throws Exception {
        try (var db = PostgresFixture.start()) {
            var migrador = Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false);
            migrador.target("5").load().migrate();
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                UUID profesional = UUID.randomUUID();
                UUID sillon = UUID.randomUUID();
                sql.executeUpdate("insert into odontologia.profesional(id,codigo) values ('%s','PRO-FICTICIO')"
                        .formatted(profesional));
                sql.executeUpdate("insert into odontologia.sillon(id,codigo) values ('%s','SILLON-FICTICIO')"
                        .formatted(sillon));

                assertEquals(1, migrador.target("6").load().migrate().migrationsExecuted);
                insertar(sql, profesional, null, "11:00");
                insertar(sql, null, sillon, "11:00");
                insertar(sql, profesional, sillon, "11:00");
                try (var filas = sql.executeQuery("select count(*) from odontologia.bloqueo_recurso")) {
                    filas.next();
                    assertEquals(3, filas.getInt(1));
                }
                assertEquals("23514", assertThrows(SQLException.class,
                        () -> insertar(sql, null, null, "11:00")).getSQLState());
                assertEquals("23514", assertThrows(SQLException.class,
                        () -> insertar(sql, profesional, sillon, "10:00")).getSQLState());
                assertEquals("23503", assertThrows(SQLException.class,
                        () -> insertar(sql, UUID.randomUUID(), sillon, "11:00")).getSQLState());
                assertEquals("42501", assertThrows(SQLException.class,
                        () -> sql.executeUpdate("delete from odontologia.bloqueo_recurso")).getSQLState());
                assertEquals("42501", assertThrows(SQLException.class,
                        () -> sql.executeUpdate("update odontologia.bloqueo_recurso set fin = inicio")).getSQLState());
            }
        }
    }

    private static void insertar(Statement sql, UUID profesional, UUID sillon, String fin) throws SQLException {
        try (var insert = sql.getConnection().prepareStatement("""
                insert into odontologia.bloqueo_recurso(id, profesional_id, sillon_id, inicio, fin)
                values (?, ?, ?, timestamp with time zone '2026-01-01 10:00+00', cast(? as timestamptz))
                """)) {
            insert.setObject(1, UUID.randomUUID());
            insert.setObject(2, profesional);
            insert.setObject(3, sillon);
            insert.setString(4, "2026-01-01 " + fin + "+00");
            insert.executeUpdate();
        }
    }
}
