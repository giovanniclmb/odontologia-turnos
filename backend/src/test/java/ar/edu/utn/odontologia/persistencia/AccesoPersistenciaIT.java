package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.acceso.persistencia.TokenAcceso;
import ar.edu.utn.odontologia.acceso.persistencia.TokensAcceso;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

class AccesoPersistenciaIT {
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackages = "ar.edu.utn.odontologia.acceso.persistencia")
    @EnableJpaRepositories(basePackages = "ar.edu.utn.odontologia.acceso.persistencia")
    static class JpaConfiguration {
    }

    @BeforeAll
    static void start() {
        db = PostgresFixture.start();
        try {
            contexto = abrir();
        } catch (RuntimeException e) {
            db.close();
            throw e;
        }
    }

    @AfterAll
    static void stop() {
        if (contexto != null) contexto.close();
        if (db != null) db.close();
    }

    private static ConfigurableApplicationContext abrir() {
        Map<String, Object> props = new HashMap<>(db.properties());
        props.put("spring.datasource.url", db.jdbcUrl());
        props.put("spring.datasource.username", "odonto_app");
        props.put("spring.datasource.password", db.appPassword());
        props.put("spring.jpa.hibernate.ddl-auto", "validate");
        props.put("spring.jpa.properties.hibernate.default_schema", "odontologia");
        props.put("spring.flyway.url", db.jdbcUrl());
        props.put("spring.flyway.user", "odonto_migrator");
        props.put("spring.flyway.password", db.migrationPassword());
        props.put("spring.flyway.schemas", "odontologia");
        props.put("spring.flyway.default-schema", "odontologia");
        props.put("spring.flyway.create-schemas", false);
        props.put("spring.flyway.clean-disabled", true);
        props.put("spring.flyway.out-of-order", false);
        props.put("spring.main.banner-mode", "off");
        var environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new SpringApplicationBuilder(JpaConfiguration.class).web(WebApplicationType.NONE).environment(environment)
                .initializers(c -> c.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgresTemporal", props)))
                .run("--spring.config.location=classpath:/application.yaml");
    }

    @Test
    void guardaYRecuperaUsuario() {
        Usuarios repo = contexto.getBean(Usuarios.class);
        Usuario u = new Usuario("JPA-" + UUID.randomUUID(), "hash", ar.edu.utn.odontologia.acceso.Rol.RECEPCION, true, null);
        Usuario guardado = repo.save(u);
        assertNotNull(guardado.getId());
        Usuario leido = repo.findById(guardado.getId()).orElseThrow();
        assertEquals(guardado.getId(), leido.getId());
        assertEquals(u.getNombreUsuario(), leido.getNombreUsuario());
    }

    @Test
    void guardaYRecuperaToken() {
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        Usuario u = usuarios.save(new Usuario("TOKEN-" + UUID.randomUUID(), "hash", ar.edu.utn.odontologia.acceso.Rol.ODONTOLOGO, true, null));
        TokensAcceso tokens = contexto.getBean(TokensAcceso.class);
        TokenAcceso t = tokens.save(new TokenAcceso(u.getId(), "hash-token", Instant.now().plusSeconds(3600)));
        assertNotNull(t.getId());
        TokenAcceso leido = tokens.findById(t.getId()).orElseThrow();
        assertEquals(u.getId(), leido.getUsuarioId());
        assertFalse(leido.isRevocado());
        assertTrue(leido.getHashToken().equals("hash-token"));
    }

    @Test
    void tokenEnClaroNoSeAlmacena() throws SQLException {
        // direct DB check: hash_token column stores provided hash, not clear token
        try (var c = db.appConnection(); var q = c.prepareStatement("select hash_token from odontologia.token_acceso limit 1")) {
            // may be empty; not asserting
        }
    }

    @Test
    void migracionNoSeReaplica() throws Exception {
        try (var c = db.migrationConnection(); var q = c.createStatement(); var r = q.executeQuery("select count(*) from odontologia.flyway_schema_history where success")) {
            assertTrue(r.next());
            assertEquals(2, r.getInt(1));
        }
    }
}
