package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.acceso.DatosBootstrap;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.acceso.ServicioBootstrap;
import ar.edu.utn.odontologia.acceso.ServicioCredenciales;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.*;

class BootstrapPersistenciaIT {
    private static PostgresFixture db;
    private static org.springframework.context.ConfigurableApplicationContext contexto;

    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    @org.springframework.boot.persistence.autoconfigure.EntityScan(basePackages = "ar.edu.utn.odontologia.acceso.persistencia")
    @org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "ar.edu.utn.odontologia.acceso.persistencia")
    static class JpaConfiguration {
        @org.springframework.context.annotation.Bean
        ServicioCredenciales servicioCredenciales() {
            return new ServicioCredenciales();
        }

        @org.springframework.context.annotation.Bean
        ServicioAutenticacion servicioAutenticacion(Usuarios usuarios, ServicioCredenciales credenciales) {
            return new ServicioAutenticacion(usuarios, credenciales);
        }

        @org.springframework.context.annotation.Bean
        ServicioBootstrap servicioBootstrap(Usuarios usuarios, ServicioCredenciales credenciales, ServicioAutorizacion autorizacion) {
            return new ServicioBootstrap(usuarios, credenciales, autorizacion);
        }

        @org.springframework.context.annotation.Bean
        ServicioAutorizacion servicioAutorizacion() {
            return new ServicioAutorizacion();
        }
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

    private static org.springframework.context.ConfigurableApplicationContext abrir() {
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
        var environment = new org.springframework.core.env.StandardEnvironment();
        environment.getPropertySources().remove(org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new org.springframework.boot.builder.SpringApplicationBuilder(JpaConfiguration.class)
                .web(org.springframework.boot.WebApplicationType.NONE).environment(environment)
                .initializers(c -> c.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource("postgresTemporal", props)))
                .run("--spring.config.location=classpath:/application.yaml");
    }

    @Test
    void bootstrapPersistentePrimerUsuarioAutenticacionYRechazoSegundo() {
        ServicioBootstrap bootstrap = contexto.getBean(ServicioBootstrap.class);
        ServicioCredenciales cred = contexto.getBean(ServicioCredenciales.class);
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        ServicioAutenticacion auth = contexto.getBean(ServicioAutenticacion.class);
        String nombre = "BOOTSTRAP-" + UUID.randomUUID();
        Usuario creado = bootstrap.bootstrapInicial(new DatosBootstrap(nombre, "ClaveSegura123", Rol.RECEPCION, null, null));
        assertNotNull(creado.getId());
        assertNotNull(creado.getId());
        assertTrue(usuarios.findByNombreUsuario(nombre).isPresent());
        Identidad identidad = auth.autenticar(nombre, "ClaveSegura123");
        assertEquals(Rol.RECEPCION, identidad.rol());
        assertThrows(IllegalStateException.class,
                () -> bootstrap.bootstrapInicial(new DatosBootstrap("OTRO-" + UUID.randomUUID(), "ClaveSegura123", Rol.RECEPCION, null, null)));
    }
}