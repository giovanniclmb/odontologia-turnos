package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.agenda.Agenda;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import javax.sql.DataSource;

@org.junit.jupiter.api.parallel.Isolated("Comprueba propiedades del proceso con restauración garantizada")
class ArranqueIT {
    private static PostgresFixture db;
    @TempDir Path temporal;
    @BeforeAll static void start(){db=PostgresFixture.start();}
    @AfterAll static void stop(){if(db!=null)db.close();}

    private ConfigurableApplicationContext abrir(Map<String,Object> extras){
        return abrir(db,extras);
    }

    private ConfigurableApplicationContext abrir(PostgresFixture database,Map<String,Object> extras){
        Map<String,Object> props=new HashMap<>(database.properties());props.putAll(extras);
        props.put("spring.main.banner-mode","off");
        var environment=new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE).environment(environment)
            .initializers(c->c.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgresTemporal",props))).run("--spring.config.location=classpath:/application.yaml");
    }

    @Test void arrancaSinHttpYExponeAgenda() throws Exception {
        try(var c=abrir(Map.of())){
            assertTrue(c.containsBean("agenda"),"El arranque debe incorporar el dominio Agenda");
            assertNotNull(c.getBean(Agenda.class));
            assertFalse(c.getClass().getName().contains("WebServer"));
            try(var db=c.getBean(DataSource.class).getConnection()){
                assertEquals("odonto_app",db.getMetaData().getUserName());
            }
        }
    }

    @ParameterizedTest @ValueSource(strings={"DB_URL","DB_USER","DB_PASSWORD","DB_MIGRATION_USER","DB_MIGRATION_PASSWORD"})
    void rechazaConfiguracionAusente(String key){
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of(key,""))){} });
        assertTrue(cadena(error).contains(key),"Debe identificar la variable faltante sin revelar valores");
    }

    @ParameterizedTest @ValueSource(strings={"DB_PASSWORD","DB_MIGRATION_PASSWORD"})
    void rechazaCredencialIncorrectaSinRevelarla(String key){
        String password="credencial-incorrecta-"+UUID.randomUUID();
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of(key,password))){} });
        assertFalse(cadena(error).contains(password));
    }

    @Test void rechazaDestinoInaccesibleConEsperaAcotada(){
        long inicio=System.nanoTime();
        assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("DB_URL","jdbc:postgresql://127.0.0.1:1/inaccesible?connectTimeout=1&socketTimeout=1"))){} });
        assertTrue(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-inicio)<15);
    }

    @Test void checksumAlteradoFallaSinRepararNiBorrarDatos() throws Exception {
        try(var c=abrir(Map.of())){}
        var id=UUID.randomUUID();
        try(var c=db.appConnection();var q=c.prepareStatement("insert into odontologia.paciente(id,codigo,nombre,apellido,email) values (?,?,?,?,?)")){
            q.setObject(1,id);q.setString(2,"CHECKSUM-"+id);q.setString(3,"Nombre ficticio");q.setString(4,"Apellido ficticio");q.setString(5,"checksum@example.invalid");q.executeUpdate();
        }
        String sql=Files.readString(Path.of("src/main/resources/db/migration/V1__referencias_base.sql"));
        Files.writeString(temporal.resolve("V1__referencias_base.sql"),sql+"\nSELECT 42;\n");
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("spring.flyway.locations","filesystem:"+temporal))){} });
        assertTrue(cadena(error).toLowerCase(Locale.ROOT).contains("checksum"));
        try(var c=db.appConnection();var q=c.prepareStatement("select codigo from odontologia.paciente where id=?")){
            q.setObject(1,id);var r=q.executeQuery();assertTrue(r.next());assertEquals("CHECKSUM-"+id,r.getString(1));
        }
    }

    @ParameterizedTest @ValueSource(strings={"spring.flyway.url","spring.datasource.url"})
    void ignoraPropiedadesSpringDelProcesoAnfitrion(String key){
        String anterior=System.getProperty(key);
        try {
            System.setProperty(key,"jdbc:postgresql://127.0.0.1:1/centinela_ajena?connectTimeout=1");
            assertDoesNotThrow(()->{try(var c=abrir(Map.of())){
                try(var connection=c.getBean(DataSource.class).getConnection()){
                    assertEquals(db.jdbcUrl(),connection.getMetaData().getURL());
                }
            }});
        } finally {
            if(anterior==null)System.clearProperty(key);else System.setProperty(key,anterior);
        }
    }

    @Test void rechazaMigradorComoConexionOrdinaria(){
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("DB_USER","odonto_migrator","DB_PASSWORD",db.migrationPassword()))){} });
        assertTrue(cadena(error).contains("spring.datasource.username"));
    }

    @Test void rechazaIdentidadOrdinariaPeligrosaPorOverrideSpring(){
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("spring.datasource.username","odonto_migrator","spring.datasource.password",db.migrationPassword()))){} });
        assertTrue(cadena(error).contains("spring.datasource.username"));
    }

    @Test void rechazaBootstrapAntesDeIntentarConectar(){
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("DB_USER","odonto_bootstrap","DB_PASSWORD","no_utilizada"))){} });
        assertTrue(cadena(error).contains("spring.datasource.username"));
    }

    @Test void rechazaBootstrapComoMigradorAntesDeConectar(){
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("DB_MIGRATION_USER","odonto_bootstrap","DB_MIGRATION_PASSWORD","no_utilizada"))){} });
        assertTrue(cadena(error).contains("spring.flyway.user"));
    }

    @Test void rechazaHistorialDeUnaVersionFutura() throws Exception {
        try(var aislada=PostgresFixture.start()){
            String v1=Files.readString(Path.of("src/main/resources/db/migration/V1__referencias_base.sql"));
            String v2=Files.readString(Path.of("src/main/resources/db/migration/V2__acceso_y_permisos.sql"));
            Files.writeString(temporal.resolve("V1__referencias_base.sql"),v1);
            Files.writeString(temporal.resolve("V2__acceso_y_permisos.sql"),v2);
            Files.copy(Path.of("src/main/resources/db/migration/V3__historial_contrasena.sql"),temporal.resolve("V3__historial_contrasena.sql"));
            Files.copy(Path.of("src/main/resources/db/migration/V4__recursos_e_intervalo.sql"),temporal.resolve("V4__recursos_e_intervalo.sql"));
            Files.copy(Path.of("src/main/resources/db/migration/V5__pacientes_y_ficha_clinica.sql"),temporal.resolve("V5__pacientes_y_ficha_clinica.sql"));
            Files.copy(Path.of("src/main/resources/db/migration/V6__bloqueos_de_recursos.sql"),temporal.resolve("V6__bloqueos_de_recursos.sql"));
            Files.writeString(temporal.resolve("V7__futura.sql"),"CREATE TABLE odontologia.futura (id uuid PRIMARY KEY); SELECT 1;\n");
            try(var c=abrir(aislada,Map.of("spring.flyway.locations","filesystem:"+temporal))){}
            var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(aislada,Map.of())){} });
            String mensaje=cadena(error).toLowerCase(Locale.ROOT);
            assertTrue(mensaje.contains("migration"));
            assertTrue(mensaje.contains("7"), "El rechazo debe corresponder a la version futura 7: " + mensaje);
            try(var c=aislada.migrationConnection();var q=c.createStatement();var result=q.executeQuery("select count(*) from odontologia.flyway_schema_history where success")){
                assertTrue(result.next());assertEquals(7,result.getInt(1));
            }
        }
    }

    @ParameterizedTest @ValueSource(strings={"user","password","%75ser","%70assword"})
    void rechazaCredencialesEnUrl(String key){
        String value=key.endsWith("ser")?"odonto_migrator":"no_utilizada";
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("DB_URL",db.jdbcUrl()+"&"+key+"="+value))){} });
        assertTrue(cadena(error).contains("credenciales en la URL"));
    }

    @Test void rechazaUrlQueReutilizaMigrador(){
        String url=db.jdbcUrl()+"&user=odonto_migrator&password="+java.net.URLEncoder.encode(db.migrationPassword(),java.nio.charset.StandardCharsets.UTF_8);
        var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(Map.of("DB_URL",url))){} });
        assertTrue(cadena(error).contains("credenciales en la URL"));
        assertFalse(cadena(error).contains(db.migrationPassword()));
    }

    private static String cadena(Throwable error){
        StringBuilder out=new StringBuilder();
        for(Throwable e=error;e!=null;e=e.getCause())out.append(e.getMessage()).append('\n');
        return out.toString();
    }
}
