package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.acceso.cli.BootstrapCli;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BootstrapCliIT {
    private static PostgresFixture db;

    @BeforeAll
    static void start() {
        db = PostgresFixture.start();
    }

    @AfterAll
    static void stop() {
        if (db != null) db.close();
        for (String k : new String[]{"DB_URL", "DB_USER", "DB_PASSWORD", "DB_MIGRATION_USER",
                "DB_MIGRATION_PASSWORD", "BOOTSTRAP_NOMBRE_USUARIO", "BOOTSTRAP_CONTRASENA", "BOOTSTRAP_ROL"}) {
            System.clearProperty(k);
        }
    }

    @Test
    void cliCreaPrimerUsuarioYRechazaSegundo() {
        String nombreUsuario = "CLI-" + UUID.randomUUID();
        Map<String, Object> props = db.properties();
        props.forEach((k, v) -> System.setProperty(k, String.valueOf(v)));
        System.setProperty("BOOTSTRAP_NOMBRE_USUARIO", nombreUsuario);
        System.setProperty("BOOTSTRAP_CONTRASENA", "ClaveSegura123");
        System.setProperty("BOOTSTRAP_ROL", "recepcion");

        BootstrapCli.main(new String[]{});

        int count = 0;
        String hash = null;
        try (Connection c = db.appConnection();
             PreparedStatement ps = c.prepareStatement("select count(*) from odontologia.usuario where nombre_usuario = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, count);

        try (Connection c = db.appConnection();
             PreparedStatement ps = c.prepareStatement("select hash_credencial from odontologia.usuario where nombre_usuario = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                hash = rs.getString(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(hash);
        assertTrue(new BCryptPasswordEncoder().matches("ClaveSegura123", hash));

        assertThrows(Exception.class, () -> BootstrapCli.main(new String[]{}));

        try (Connection c = db.appConnection();
             PreparedStatement ps = c.prepareStatement("select count(*) from odontologia.usuario")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}