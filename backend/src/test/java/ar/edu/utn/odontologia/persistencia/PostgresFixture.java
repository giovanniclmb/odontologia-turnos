package ar.edu.utn.odontologia.persistencia;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public final class PostgresFixture implements AutoCloseable {

    private static final String APP_USER = "odonto_app";
    private static final String MIGRATION_USER = "odonto_migrator";

    private final PostgreSQLContainer container;
    private final String appPassword;
    private final String migrationPassword;

    private PostgresFixture(PostgreSQLContainer container, String appPassword, String migrationPassword) {
        this.container = container;
        this.appPassword = appPassword;
        this.migrationPassword = migrationPassword;
    }

    public static PostgresFixture start() {
        String appPassword = UUID.randomUUID() + "'\\ $";
        String migrationPassword = UUID.randomUUID() + "'\\ $";
        PostgreSQLContainer container = new PostgreSQLContainer("postgres:18.7")
                .withDatabaseName("odontologia_test")
                .withUsername("odonto_bootstrap")
                .withPassword(UUID.randomUUID().toString())
                .withEnv("DB_PASSWORD", appPassword)
                .withEnv("DB_MIGRATION_PASSWORD", migrationPassword)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(Path.of("..", "docker", "postgres", "10-bootstrap.sh"), 0755),
                        "/docker-entrypoint-initdb.d/10-bootstrap.sh");
        try {
            container.start();
            return new PostgresFixture(container, appPassword, migrationPassword);
        } catch (RuntimeException | Error failure) {
            try {
                container.close();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
    }

    public String jdbcUrl() {
        return container.getJdbcUrl();
    }

    public String appPassword() {
        return appPassword;
    }

    public String migrationPassword() {
        return migrationPassword;
    }

    public Connection adminConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), container.getUsername(), container.getPassword());
    }

    public Connection appConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), APP_USER, appPassword);
    }

    public Connection migrationConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), MIGRATION_USER, migrationPassword);
    }

    public Map<String, Object> properties() {
        return Map.of(
                "DB_URL", jdbcUrl(),
                "DB_USER", APP_USER,
                "DB_PASSWORD", appPassword,
                "DB_MIGRATION_USER", MIGRATION_USER,
                "DB_MIGRATION_PASSWORD", migrationPassword);
    }

    @Override
    public void close() {
        container.close();
    }
}
