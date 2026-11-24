package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.comunicaciones.ClaveIdempotencia;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.dao.DataIntegrityViolationException;

class EmailEnvioPersistenciaIT {
    @Test
    void guardaYRecuperaPorClave() {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            var repo = ctx.getBean(EmailsEnviados.class);
            UUID turno = UUID.randomUUID();
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
            var e = new EmailEnvio("ALTA", turno, "dest@test.com", key);
            e.marcar(true, 2, null, java.time.Instant.now());
            repo.save(e);

            var encontrado = repo.findByIdempotencyKey(key);
            assertNotNull(encontrado);
            assertEquals("sent", encontrado.estado());
            assertEquals(2, encontrado.intentos());
            assertEquals(turno, encontrado.turnoId());
        }
    }

    @Test
    void claveDuplicadaViolentaUnicidad() {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            var repo = ctx.getBean(EmailsEnviados.class);
            UUID turno = UUID.randomUUID();
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
            repo.save(new EmailEnvio("ALTA", turno, "dest@test.com", key));
            assertThrows(DataIntegrityViolationException.class,
                    () -> repo.save(new EmailEnvio("ALTA", turno, "dest@test.com", key)));
        }
    }

    private static ConfigurableApplicationContext abrir(PostgresFixture db) {
        var env = new StandardEnvironment();
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        env.getPropertySources().addFirst(new MapPropertySource("postgresTemporal", db.properties()));
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE)
                .environment(env).run("--spring.config.location=classpath:/application.yaml");
    }
}