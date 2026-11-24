package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class CuentasPacienteMigracionIT {
    @Test
    void v9PermiteCuentaPendienteYProtegeEmailVinculoYPropiedad() throws Exception {
        try (var db = PostgresFixture.start()) {
            assertEquals(9, migrador(db).load().migrate().migrationsExecuted);
            UUID pacienteA = UUID.randomUUID();
            UUID pacienteB = UUID.randomUUID();
            insertarPaciente(db, pacienteA, "PACIENTE-CUENTA-A", "compartido@example.invalid");
            insertarPaciente(db, pacienteB, "PACIENTE-CUENTA-B", "compartido@example.invalid");

            UUID cuentaPendiente = UUID.randomUUID();
            insertarCuenta(db, cuentaPendiente, "pendiente@example.invalid", null);

            assertThrows(SQLException.class, () -> insertarCuenta(db, UUID.randomUUID(),
                    "pendiente@example.invalid", pacienteB));

            insertarCuenta(db, UUID.randomUUID(), "vinculada-a@example.invalid", pacienteA);
            insertarCuenta(db, UUID.randomUUID(), "vinculada-b@example.invalid", pacienteB);

            assertThrows(SQLException.class, () -> insertarCuenta(db, UUID.randomUUID(),
                    "segunda-cuenta@example.invalid", pacienteA));
            assertThrows(SQLException.class, () -> insertarCuenta(db, UUID.randomUUID(),
                    "paciente-inexistente@example.invalid", UUID.randomUUID()));

            try (var con = db.appConnection(); var sql = con.createStatement();
                 var resultado = sql.executeQuery("select paciente_ref from odontologia.cuenta_paciente where id='"
                         + cuentaPendiente + "'")) {
                assertTrue(resultado.next());
                assertNull(resultado.getObject("paciente_ref"));
            }
            try (var con = db.appConnection(); var sql = con.createStatement();
                 var resultado = sql.executeQuery("select has_table_privilege(current_user,'odontologia.cuenta_paciente','DELETE')")) {
                assertTrue(resultado.next());
                assertFalse(resultado.getBoolean(1), "DELETE no concedido para cuenta_paciente");
            }
        }
    }

    private static void insertarPaciente(PostgresFixture db, UUID id, String codigo, String email)
            throws SQLException {
        try (var con = db.appConnection(); var sql = con.prepareStatement("""
                insert into odontologia.paciente(id,codigo,nombre,apellido,email)
                values (?,?,?,?,?)
                """)) {
            sql.setObject(1, id);
            sql.setString(2, codigo);
            sql.setString(3, "Nombre ficticio");
            sql.setString(4, "Apellido ficticio");
            sql.setString(5, email);
            sql.executeUpdate();
        }
    }

    private static void insertarCuenta(PostgresFixture db, UUID id, String email, UUID pacienteId)
            throws SQLException {
        try (var con = db.appConnection(); var sql = con.prepareStatement("""
                insert into odontologia.cuenta_paciente(id,email,hash_credencial,paciente_ref,creado_en)
                values (?,?,?, ?,now())
                """)) {
            sql.setObject(1, id);
            sql.setString(2, email);
            sql.setString(3, "hash-ficticio-no-usable");
            sql.setObject(4, pacienteId);
            sql.executeUpdate();
        }
    }

    private static org.flywaydb.core.api.configuration.FluentConfiguration migrador(PostgresFixture db) {
        return Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                .schemas("odontologia").createSchemas(false).cleanDisabled(true).outOfOrder(false);
    }
}
