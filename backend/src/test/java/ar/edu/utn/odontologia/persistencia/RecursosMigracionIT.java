package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;

class RecursosMigracionIT {
    @Test
    void unaInstalacionNuevaNoSiembraRecursosNiPrestaciones() throws Exception {
        try (var db = PostgresFixture.start()) {
            assertEquals(4, migrador(db).load().migrate().migrationsExecuted);
            try (var con = db.appConnection(); var sql = con.createStatement();
                 var resultado = sql.executeQuery("""
                         select (select count(*) from odontologia.profesional)
                              + (select count(*) from odontologia.sillon)
                              + (select count(*) from odontologia.prestacion)
                         """)) {
                assertTrue(resultado.next());
                assertEquals(0, resultado.getInt(1));
            }
        }
    }

    @Test
    void conservaReferenciasPreviasYAplicaRestriccionesEnV4() throws Exception {
        try (var db = PostgresFixture.start()) {
            migrador(db).target("3").load().migrate();
            UUID prestacion = UUID.randomUUID();
            UUID profesional = UUID.randomUUID();
            UUID usuario = UUID.randomUUID();
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                sql.executeUpdate("insert into odontologia.prestacion(id,codigo) values ('"
                        + prestacion + "','PRESTACION-FICTICIA')");
                sql.executeUpdate("insert into odontologia.profesional(id,codigo) values ('"
                        + profesional + "','PROFESIONAL-FICTICIO')");
                sql.executeUpdate("""
                        insert into odontologia.usuario
                            (id,nombre_usuario,hash_credencial,rol,activo,profesional_ref,creado_en)
                        values ('%s','usuario-ficticio','hash-ficticio','odontologo',true,'%s',now())
                        """.formatted(usuario, profesional));
            }
            assertEquals(1, migrador(db).load().migrate().migrationsExecuted);
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                try (var resultado = sql.executeQuery("select * from odontologia.prestacion where id='"
                        + prestacion + "'")) {
                    assertTrue(resultado.next());
                    assertEquals("PRESTACION-FICTICIA", resultado.getString("codigo"));
                    assertNull(resultado.getObject("duracion_referencia_minutos"));
                }
                try (var resultado = sql.executeQuery("select profesional_ref from odontologia.usuario where id='"
                        + usuario + "'")) {
                    assertTrue(resultado.next());
                    assertEquals(profesional, resultado.getObject(1, UUID.class));
                }
                for (int invalido : new int[]{0, -1}) {
                    var error = assertThrows(SQLException.class, () -> sql.executeUpdate(
                            "update odontologia.prestacion set duracion_referencia_minutos=" + invalido));
                    assertEquals("23514", error.getSQLState());
                }
                var errorFk = assertThrows(SQLException.class, () -> sql.executeUpdate(
                        "update odontologia.usuario set profesional_ref='" + UUID.randomUUID() + "'"));
                assertEquals("23503", errorFk.getSQLState());
            }
        }
    }

    private static FluentConfiguration migrador(PostgresFixture db) {
        return Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                .schemas("odontologia").createSchemas(false);
    }
}
