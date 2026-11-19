package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BootstrapIT {

    private static PostgresFixture postgres;

    @BeforeAll
    static void iniciarPostgres() {
        postgres = PostgresFixture.start();
    }

    @AfterAll
    static void cerrarPostgres() {
        if (postgres != null) {
            postgres.close();
        }
    }

    @Test
    void creaRolesDeConexionSinPrivilegiosAdministrativos() throws SQLException {
        try (var admin = postgres.adminConnection();
             var statement = admin.createStatement();
             var roles = statement.executeQuery("""
                     SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole
                     FROM pg_roles WHERE rolname IN ('odonto_app', 'odonto_migrator')
                     ORDER BY rolname
                     """)) {
            List<String> encontrados = new ArrayList<>();
            while (roles.next()) {
                String rol = roles.getString("rolname");
                encontrados.add(rol);
                assertTrue(roles.getBoolean("rolcanlogin"), rol);
                assertFalse(roles.getBoolean("rolsuper"), rol);
                assertFalse(roles.getBoolean("rolcreatedb"), rol);
                assertFalse(roles.getBoolean("rolcreaterole"), rol);
            }
            assertEquals(List.of("odonto_app", "odonto_migrator"), encontrados);
        }
    }

    @Test
    void preparaEsquemaDelMigradorConAccesoOrdinarioLimitado() throws SQLException {
        try (var admin = postgres.adminConnection();
             var statement = admin.createStatement();
             var esquema = statement.executeQuery("""
                     SELECT nspowner::regrole::text AS propietario,
                            has_schema_privilege('odonto_app', oid, 'USAGE') AS app_usage,
                            has_schema_privilege('odonto_app', oid, 'CREATE') AS app_create
                     FROM pg_namespace WHERE nspname = 'odontologia'
                     """)) {
            assertTrue(esquema.next(), "Falta el esquema odontologia");
            assertEquals("odonto_migrator", esquema.getString("propietario"));
            assertTrue(esquema.getBoolean("app_usage"));
            assertFalse(esquema.getBoolean("app_create"));
        }
    }

    @Test
    void verificaIdentidadDestinoYPrivilegiosDeAmbasConexiones() throws SQLException {
        try (var app = postgres.appConnection(); var migrador = postgres.migrationConnection()) {
            verificarConexion(app, "odonto_app");
            verificarConexion(migrador, "odonto_migrator");
        }
    }

    private void verificarConexion(Connection conexion, String usuario) throws SQLException {
        try (var statement = conexion.createStatement();
             var sesion = statement.executeQuery("""
                     SELECT current_user AS usuario, current_database() AS base,
                            has_database_privilege(current_database(), 'CREATE') AS database_create,
                            has_database_privilege(current_database(), 'TEMP') AS database_temp,
                            has_database_privilege('postgres', 'CONNECT') AS mantenimiento_connect,
                            has_database_privilege('template1', 'CONNECT') AS plantilla_connect,
                            has_schema_privilege('public', 'CREATE') AS public_create
                     """)) {
            assertTrue(sesion.next());
            assertEquals(usuario, sesion.getString("usuario"));
            assertEquals("odontologia_test", sesion.getString("base"));
            assertFalse(sesion.getBoolean("database_create"));
            assertFalse(sesion.getBoolean("database_temp"));
            assertFalse(sesion.getBoolean("mantenimiento_connect"));
            assertFalse(sesion.getBoolean("plantilla_connect"));
            assertFalse(sesion.getBoolean("public_create"));
        }
    }
}
