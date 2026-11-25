package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.acceso.DatosBootstrap;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioBootstrap;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.*;

class BootstrapConcurrenciaIT {
    private static PostgresFixture db;
    private static org.springframework.context.ConfigurableApplicationContext contexto;

    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    @org.springframework.boot.persistence.autoconfigure.EntityScan(basePackages = "ar.edu.utn.odontologia.acceso.persistencia")
    @org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "ar.edu.utn.odontologia.acceso.persistencia")
    static class JpaConfiguration {
        @org.springframework.context.annotation.Bean
        ar.edu.utn.odontologia.acceso.ServicioCredenciales servicioCredenciales() {
            return new ar.edu.utn.odontologia.acceso.ServicioCredenciales();
        }

        @org.springframework.context.annotation.Bean
        ar.edu.utn.odontologia.acceso.ServicioAutorizacion servicioAutorizacion() {
            return new ar.edu.utn.odontologia.acceso.ServicioAutorizacion();
        }

        @org.springframework.context.annotation.Bean
        ar.edu.utn.odontologia.acceso.ServicioBootstrap servicioBootstrap(
                ar.edu.utn.odontologia.acceso.persistencia.Usuarios usuarios,
                ar.edu.utn.odontologia.acceso.ServicioCredenciales credenciales,
                ar.edu.utn.odontologia.acceso.ServicioAutorizacion autorizacion) {
            return new ar.edu.utn.odontologia.acceso.ServicioBootstrap(usuarios, credenciales, autorizacion);
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
        props.put("spring.main.web-application-type", "none");
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
                .web(org.springframework.boot.WebApplicationType.NONE).properties("spring.main.web-application-type=none").environment(environment)
                .initializers(c -> c.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource("postgresTemporal", props)))
                .run("--spring.config.location=classpath:/application.yaml");
    }

    record Resultado(boolean exito, Exception error) {
    }

    @Test
    void bootstrapConcurrenteSoloUno() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Resultado> tarea = () -> {
            try {
                ServicioBootstrap bootstrap = contexto.getBean(ServicioBootstrap.class);
                bootstrap.bootstrapInicial(new DatosBootstrap("CONC-" + UUID.randomUUID(), "ClaveSegura123", Rol.RECEPCION, null, null));
                return new Resultado(true, null);
            } catch (Exception e) {
                return new Resultado(false, e);
            }
        };
        List<Future<Resultado>> futuros = new ArrayList<>(pool.invokeAll(List.of(tarea, tarea)));
        pool.shutdown();
        int exitos = 0;
        int fallos = 0;
        for (Future<Resultado> futuro : futuros) {
            Resultado r = futuro.get();
            if (r.exito()) exitos++;
            else {
                fallos++;
                assertTrue(r.error() instanceof IllegalStateException, "El rechazo debe ser bootstrap ya ejecutado");
            }
        }
        assertEquals(1, exitos, "exactamente una creacion inicial debe tener exito");
        assertEquals(1, fallos, "la otra debe ser rechazada");
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        assertEquals(1, usuarios.findAll().size(), "debe persistir un unico usuario");
    }
}