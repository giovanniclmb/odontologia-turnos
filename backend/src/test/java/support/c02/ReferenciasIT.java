package support.c02;

import ar.edu.utn.odontologia.persistencia.PostgresFixture;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.sql.DataSource;
import ar.edu.utn.odontologia.referencias.persistencia.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

class ReferenciasIT {
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;

    @TestConfiguration(proxyBeanMethods=false)
    @EnableAutoConfiguration
    @EntityScan(basePackages="ar.edu.utn.odontologia.referencias.persistencia")
    @EnableJpaRepositories(basePackages="ar.edu.utn.odontologia.referencias.persistencia")
    static class JpaConfiguration {}

    @BeforeAll static void start() {
        db=PostgresFixture.start();
        try { contexto=abrir(); } catch(RuntimeException e) { db.close(); throw e; }
    }
    @AfterAll static void stop() { if(contexto!=null)contexto.close();if(db!=null)db.close(); }

    private static ConfigurableApplicationContext abrir() {
        Map<String,Object> props=new HashMap<>(db.properties());
        props.put("spring.datasource.url",db.jdbcUrl());
        props.put("spring.datasource.username","odonto_app");props.put("spring.datasource.password",db.appPassword());
        props.put("spring.jpa.hibernate.ddl-auto","validate");props.put("spring.jpa.properties.hibernate.default_schema","odontologia");
        props.put("spring.flyway.url",db.jdbcUrl());props.put("spring.flyway.user","odonto_migrator");props.put("spring.flyway.password",db.migrationPassword());
        props.put("spring.flyway.schemas","odontologia");props.put("spring.flyway.default-schema","odontologia");props.put("spring.flyway.create-schemas",false);props.put("spring.flyway.clean-disabled",true);
        props.put("spring.flyway.out-of-order",false);props.put("spring.main.banner-mode","off");
        var environment=new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new SpringApplicationBuilder(JpaConfiguration.class).web(WebApplicationType.NONE).environment(environment)
                .initializers(c->c.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgresTemporal",props))).run("--spring.config.location=classpath:/application.yaml");
    }

    @ParameterizedTest @ValueSource(strings={"paciente","profesional","sillon","prestacion"})
    void generaUuidYRecuperaEnOtraTransaccion(String tipo) {
        var codigo="JPA-"+UUID.randomUUID();
        var primera=assertDoesNotThrow(()->guardar(tipo,codigo));
        assertNotNull(primera.getId());assertEquals(codigo,primera.getCodigo());
        var segunda=assertDoesNotThrow(()->guardar(tipo,codigo+"-2"));
        assertNotNull(segunda.getId());assertNotEquals(primera.getId(),segunda.getId());
        var leida=leer(tipo,primera.getId());
        assertEquals(primera.getId(),leida.getId());assertEquals(codigo,leida.getCodigo());
        assertNotSame(primera,leida);
    }

    @Test void conservaReferenciasEHistorialAlReiniciar() throws Exception {
        var guardada=assertDoesNotThrow(()->guardar("paciente","REINICIO-"+UUID.randomUUID()));
        var id=guardada.getId();assertNotNull(id);
        contexto.close();contexto=abrir();
        assertEquals(guardada.getCodigo(),leer("paciente",id).getCodigo());
        try(var c=db.migrationConnection();var q=c.createStatement();var r=q.executeQuery("select count(*) from odontologia.flyway_schema_history where version='1' and success")) {
            assertTrue(r.next());assertEquals(1,r.getInt(1));
        }
        try(var c=contexto.getBean(DataSource.class).getConnection()) {
            assertEquals(db.jdbcUrl(),c.getMetaData().getURL());assertEquals("odonto_app",c.getMetaData().getUserName());
        }
    }

    private static Referencia guardar(String tipo,String codigo) {
        return switch(tipo) {
            case "paciente"->contexto.getBean(PacienteReferencias.class).save(new PacienteReferencia(codigo,"Nombre ficticio","Apellido ficticio","referencias@example.invalid",null));
            case "profesional"->contexto.getBean(ProfesionalReferencias.class).save(new ProfesionalReferencia(codigo));
            case "sillon"->contexto.getBean(SillonReferencias.class).save(new SillonReferencia(codigo));
            case "prestacion"->contexto.getBean(PrestacionReferencias.class).save(new PrestacionReferencia(codigo));
            default->throw new IllegalArgumentException(tipo);
        };
    }
    private static Referencia leer(String tipo,UUID id) {
        return switch(tipo) {
            case "paciente"->contexto.getBean(PacienteReferencias.class).findById(id).orElseThrow();
            case "profesional"->contexto.getBean(ProfesionalReferencias.class).findById(id).orElseThrow();
            case "sillon"->contexto.getBean(SillonReferencias.class).findById(id).orElseThrow();
            case "prestacion"->contexto.getBean(PrestacionReferencias.class).findById(id).orElseThrow();
            default->throw new IllegalArgumentException(tipo);
        };
    }
}
