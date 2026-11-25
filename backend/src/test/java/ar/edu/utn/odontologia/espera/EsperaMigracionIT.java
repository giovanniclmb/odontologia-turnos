package ar.edu.utn.odontologia.espera;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import jakarta.persistence.EntityManagerFactory;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class EsperaMigracionIT {
    @Test
    void actualizaV14YConservaReferenciasAlPersistirFranjas() throws Exception {
        try (var db = PostgresFixture.start()) {
            Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false).target("14").load().migrate();
            UUID paciente = insertarPaciente(db);
            UUID prestacion = UUID.randomUUID();
            try (var con = db.appConnection(); var sql = con.prepareStatement(
                    "insert into odontologia.prestacion(id,codigo,duracion_referencia_minutos) values (?, ?, 30)")) {
                sql.setObject(1, prestacion);
                sql.setString(2, "PRESTACION-ESPERA");
                sql.executeUpdate();
            }

            var fecha = LocalDate.of(2026, 11, 25);
            var preferencias = new PreferenciasEspera(prestacion, fecha, fecha, List.of(
                    new FranjaEspera(LocalTime.NOON, LocalTime.NOON.plusNanos(1)),
                    new FranjaEspera(LocalTime.of(16, 0), LocalTime.of(18, 0))), null);
            try (var contexto = abrir(db)) {
                var factory = contexto.getBean(EntityManagerFactory.class);
                UUID id = UUID.randomUUID();
                factory.runInTransaction(em -> em.persist(new InscripcionEsperaEntidad(id, paciente, preferencias)));
                assertEquals(preferencias, factory.callInTransaction(em ->
                        em.find(InscripcionEsperaEntidad.class, id).preferencias()));
                assertEquals("23503", assertThrows(SQLException.class,
                        () -> insertarInscripcion(db, paciente, UUID.randomUUID(), fecha, fecha)).getSQLState());
                assertEquals("23514", assertThrows(SQLException.class,
                        () -> insertarInscripcion(db, paciente, prestacion, fecha.plusDays(1), fecha)).getSQLState());
                try (var con = db.appConnection(); var sql = con.prepareStatement("""
                        insert into odontologia.franja_espera(inscripcion_id, orden, inicio_nanos, fin_nanos)
                        values (?, 2, 10, 9)
                        """)) {
                    sql.setObject(1, id);
                    assertEquals("23514", assertThrows(SQLException.class, sql::executeUpdate).getSQLState());
                }
            }
        }
    }

    static ConfigurableApplicationContext abrir(PostgresFixture db) {
        return abrir(db, contexto -> {});
    }

    static ConfigurableApplicationContext abrir(PostgresFixture db,
            ApplicationContextInitializer<ConfigurableApplicationContext> inicializador) {
        var entorno = new StandardEnvironment();
        entorno.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        entorno.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        entorno.getPropertySources().addFirst(new MapPropertySource("postgresTemporal", db.properties()));
        return new SpringApplicationBuilder(OdontologiaApplication.class)
                .web(WebApplicationType.NONE).environment(entorno).initializers(inicializador)
                .run("--spring.main.web-application-type=none", "--spring.config.location=classpath:/application.yaml");
    }

    static UUID insertarPaciente(PostgresFixture db) throws SQLException {
        UUID id = UUID.randomUUID();
        try (var con = db.appConnection(); var sql = con.prepareStatement("""
                insert into odontologia.paciente(id,codigo,nombre,apellido,email)
                values (?, ?, 'Paciente', 'Ficticio', 'paciente@example.invalid')
                """)) {
            sql.setObject(1, id);
            sql.setString(2, id.toString());
            sql.executeUpdate();
        }
        return id;
    }

    private static void insertarInscripcion(PostgresFixture db, UUID paciente, UUID prestacion,
                                             LocalDate desde, LocalDate hasta) throws SQLException {
        try (var con = db.appConnection(); var sql = con.prepareStatement("""
                insert into odontologia.inscripcion_espera(id,paciente_id,prestacion_id,desde,hasta)
                values (?, ?, ?, ?, ?)
                """)) {
            sql.setObject(1, UUID.randomUUID());
            sql.setObject(2, paciente);
            sql.setObject(3, prestacion);
            sql.setObject(4, desde);
            sql.setObject(5, hasta);
            sql.executeUpdate();
        }
    }
}
