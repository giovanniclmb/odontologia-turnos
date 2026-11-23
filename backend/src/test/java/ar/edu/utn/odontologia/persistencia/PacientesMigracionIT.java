package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

class PacientesMigracionIT {
    @Test
    void v5ConservaIdentidadYCodigoAlMigrarDesdeV4() throws Exception {
        try (var db = PostgresFixture.start()) {
            migrador(db).target("4").load().migrate();
            UUID id = UUID.randomUUID();
            try (var con = db.appConnection(); var sql = con.prepareStatement(
                    "insert into odontologia.paciente(id,codigo) values (?,?)")) {
                sql.setObject(1, id);
                sql.setString(2, "PACIENTE-MIGRACION-FICTICIO");
                sql.executeUpdate();
            }
            try (var con = db.migrationConnection(); var sql = con.createStatement()) {
                sql.execute("alter table odontologia.paciente add column nombre text, add column apellido text, add column email text, add column telefono text");
                sql.executeUpdate("update odontologia.paciente set nombre='Nombre ficticio', apellido='Apellido ficticio', email='migracion@example.invalid' where id='" + id + "'");
            }

            assertEquals(1, migrador(db).target("5").load().migrate().migrationsExecuted);
            try (var con = db.appConnection(); var sql = con.prepareStatement(
                    "select id,codigo from odontologia.paciente where id=?")) {
                sql.setObject(1, id);
                try (var resultado = sql.executeQuery()) {
                    assertTrue(resultado.next());
                    assertEquals(id, resultado.getObject("id", UUID.class));
                    assertEquals("PACIENTE-MIGRACION-FICTICIO", resultado.getString("codigo"));
                }
            }
        }
    }

    @Test
    void v5RechazaLegacyIncompletoSinCambiosParciales() throws Exception {
        try (var db = PostgresFixture.start()) {
            migrador(db).target("4").load().migrate();
            UUID id = UUID.randomUUID();
            try (var con = db.appConnection(); var sql = con.prepareStatement(
                    "insert into odontologia.paciente(id,codigo) values (?,?)")) {
                sql.setObject(1, id);
                sql.setString(2, "LEGACY-FICTICIO-INCOMPLETO");
                sql.executeUpdate();
            }

            assertThrows(FlywayException.class, () -> migrador(db).load().migrate());
            try (var con = db.migrationConnection(); var sql = con.createStatement()) {
                try (var columnas = sql.executeQuery("""
                        select count(*) from information_schema.columns
                        where table_schema='odontologia' and table_name='paciente'
                          and column_name in ('nombre','apellido','email','telefono')
                        """)) {
                    assertTrue(columnas.next());
                    assertEquals(0, columnas.getInt(1));
                }
                try (var paciente = sql.executeQuery("select count(*) from odontologia.paciente")) {
                    assertTrue(paciente.next());
                    assertEquals(1, paciente.getInt(1));
                }
            }
        }
    }

    @Test
    void v5ProtegeRelacionesContenidosYNoConcedeDelete() throws Exception {
        try (var db = PostgresFixture.start()) {
            migrador(db).load().migrate();
            UUID paciente = UUID.randomUUID();
            UUID profesional = UUID.randomUUID();
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.paciente"
                        + "(id,codigo,nombre,apellido,email) values ('" + UUID.randomUUID()
                        + "','CODIGO-INVALIDO',' ','Apellido','correo@example.invalid')"));
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.paciente"
                        + "(id,codigo,nombre,apellido,email) values ('" + UUID.randomUUID()
                        + "','CODIGO-INVALIDO-2','Nombre',' ','correo@example.invalid')"));
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.paciente"
                        + "(id,codigo,nombre,apellido,email) values ('" + UUID.randomUUID()
                        + "','CODIGO-INVALIDO-3','Nombre','Apellido',' ')"));
                sql.executeUpdate("insert into odontologia.paciente(id,codigo,nombre,apellido,email) values ('"
                        + paciente + "','PACIENTE-IT','Nombre','Apellido','test@example.invalid')");
                sql.executeUpdate("insert into odontologia.profesional(id,codigo,activo) values ('"
                        + profesional + "','PROFESIONAL-IT',true)");
                sql.executeUpdate("insert into odontologia.paciente_profesional(paciente_id,profesional_id,activo) values ('"
                        + paciente + "','" + profesional + "',true)");
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.paciente_profesional"
                        + "(paciente_id,profesional_id,activo) values ('" + UUID.randomUUID() + "','"
                        + profesional + "',true)"));
                sql.executeUpdate("insert into odontologia.ficha_clinica(paciente_id) values ('" + paciente + "')");
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.ficha_clinica"
                        + "(paciente_id) values ('" + UUID.randomUUID() + "')"));
                UUID autor = UUID.randomUUID();
                sql.executeUpdate("insert into odontologia.usuario(id,nombre_usuario,hash_credencial,rol,activo,creado_en) values ('"
                        + autor + "','odontologo-ficticio','hash-ficticio','odontologo',true,now())");
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.nota_clinica"
                        + "(id,paciente_id,autor_usuario_id,texto,creada_en) values ('" + UUID.randomUUID()
                        + "','" + UUID.randomUUID() + "','" + autor + "','Nota','2026-11-22T12:00:00Z')"));
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.nota_clinica"
                        + "(id,paciente_id,autor_usuario_id,texto,creada_en) values ('" + UUID.randomUUID()
                        + "','" + paciente + "','" + autor + "',' ','2026-11-22T12:00:00Z')"));
                assertThrows(SQLException.class, () -> sql.executeUpdate("insert into odontologia.nota_clinica"
                        + "(id,paciente_id,autor_usuario_id,texto,creada_en) values ('" + UUID.randomUUID()
                        + "','" + paciente + "','" + UUID.randomUUID() + "','Nota','2026-11-22T12:00:00Z')"));
            }
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                for (String tabla : new String[]{"paciente", "paciente_profesional", "ficha_clinica", "nota_clinica"}) {
                    try (var grant = sql.executeQuery("select has_table_privilege(current_user,'odontologia." + tabla
                            + "','DELETE')")) {
                        assertTrue(grant.next());
                        assertFalse(grant.getBoolean(1), "DELETE no concedido para " + tabla);
                    }
                }
            }
        }
    }

    private static FluentConfiguration migrador(PostgresFixture db) {
        return Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                .schemas("odontologia").createSchemas(false).cleanDisabled(true).outOfOrder(false);
    }
}
