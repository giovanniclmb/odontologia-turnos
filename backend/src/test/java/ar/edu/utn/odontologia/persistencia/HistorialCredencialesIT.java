package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.*;
import ar.edu.utn.odontologia.acceso.persistencia.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class HistorialCredencialesIT {
    static PostgresFixture db;
    static ConfigurableApplicationContext contexto;
    @BeforeAll static void iniciar() {
        db=PostgresFixture.start();
        try { contexto=abrir(); } catch(RuntimeException failure) { db.close();throw failure; }
    }
    @AfterAll static void cerrar() { if(contexto!=null)contexto.close();if(db!=null)db.close(); }
    static ConfigurableApplicationContext abrir() {
        Map<String,Object> props=new HashMap<>(db.properties());
        props.put("spring.datasource.url",db.jdbcUrl());props.put("spring.flyway.url",db.jdbcUrl());
        props.put("spring.datasource.username","odonto_app");props.put("spring.flyway.user","odonto_migrator");
        props.put("spring.datasource.password",db.appPassword());props.put("spring.flyway.password",db.migrationPassword());
        var env=new StandardEnvironment();
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE).properties("spring.main.web-application-type=none").environment(env)
                .initializers(c->c.getEnvironment().getPropertySources().addFirst(new MapPropertySource("fixture",props)))
                .run("--spring.config.location=classpath:/application.yaml");
    }
    @Test void esquemaDeHistorialExisteConUsuarioOrdinarioSinDdl() throws Exception {
        try(var c=db.appConnection();var q=c.createStatement();var r=q.executeQuery("select to_regclass('odontologia.historial_contrasena')")) {
            assertTrue(r.next());assertNotNull(r.getString(1),"Falta el almacenamiento persistente del historial");
        }
    }

    static final String INICIAL="ClaveInicial123!";
    static Usuario usuario(Rol rol) {
        return contexto.getBean(Usuarios.class).save(new Usuario("CAMBIO-"+UUID.randomUUID(),
                contexto.getBean(ServicioCredenciales.class).hash(INICIAL),rol,true,null));
    }
    static Identidad identidad(Usuario u) {return contexto.getBean(ServicioAutenticacion.class).autenticar(u.getNombreUsuario(),INICIAL);}
    static void cambiar(Identidad id,String actual,String nueva) {contexto.getBean(ServicioCambioContrasena.class).cambiarContrasena(id,actual,nueva);}
    static long cantidad(UUID usuarioId) throws Exception {
        try(var c=db.appConnection();var q=c.prepareStatement("select count(*) from odontologia.historial_contrasena where usuario_id=?")) {
            q.setObject(1,usuarioId);try(var r=q.executeQuery()){assertTrue(r.next());return r.getLong(1);}
        }
    }
    static void autentica(Usuario u,String clave) {assertEquals(u.getId(),contexto.getBean(ServicioAutenticacion.class).autenticar(u.getNombreUsuario(),clave).usuarioId());}

    @ParameterizedTest @EnumSource(Rol.class)
    void usuarioActivoCambiaSuPropiaClave(Rol rol) throws Exception {
        var u=usuario(rol);var id=identidad(u);
        assertDoesNotThrow(()->cambiar(id,INICIAL,"ClaveRenovada456!"));
        autentica(u,"ClaveRenovada456!");
        assertThrows(AutenticacionFallidaException.class,()->autentica(u,INICIAL));
        assertEquals(1,cantidad(u.getId()));
    }

    @Test void rechazosConservanLaClaveYNoAgreganHistorial() throws Exception {
        var u=usuario(Rol.RECEPCION);var id=identidad(u);
        assertThrows(AutorizacionDenegadaException.class,()->cambiar(null,INICIAL,"ClaveRenovada456!"));
        assertThrows(AutenticacionFallidaException.class,()->cambiar(id,"incorrecta","ClaveRenovada456!"));
        assertThrows(PoliticaContrasenaException.class,()->cambiar(id,INICIAL,"Corta1"));
        assertThrows(PoliticaContrasenaException.class,()->cambiar(id,INICIAL,INICIAL));
        autentica(u,INICIAL);assertEquals(0,cantidad(u.getId()));
        u.setActivo(false);contexto.getBean(Usuarios.class).save(u);
        assertThrows(AutenticacionFallidaException.class,()->cambiar(id,INICIAL,"ClaveRenovada456!"));
        assertEquals(0,cantidad(u.getId()));
    }

    @Test void ventanaIncluyeVigenteYCuatroPreviasYSeConservaAlReiniciar() throws Exception {
        var u=usuario(Rol.ODONTOLOGO);var id=identidad(u);
        String anterior=INICIAL;
        for(int i=1;i<=4;i++) {
            String nueva="ClaveRotacion"+i+"Abc!";
            String actual=anterior;assertDoesNotThrow(()->cambiar(id,actual,nueva));anterior=nueva;
        }
        contexto.close();contexto=abrir();
        for(String usada:List.of(INICIAL,"ClaveRotacion1Abc!","ClaveRotacion2Abc!","ClaveRotacion3Abc!","ClaveRotacion4Abc!")) {
            assertThrows(PoliticaContrasenaException.class,()->cambiar(id,"ClaveRotacion4Abc!",usada));
        }
        assertEquals(4,cantidad(u.getId()));autentica(u,"ClaveRotacion4Abc!");
        cambiar(id,"ClaveRotacion4Abc!","ClaveRotacion5Abc!");
        // La inicial ya quedó fuera de la ventana de cinco.
        cambiar(id,"ClaveRotacion5Abc!",INICIAL);autentica(u,INICIAL);
        assertEquals(6,cantidad(u.getId()));
        try(var c=db.appConnection();var q=c.prepareStatement("select bool_and(hash_credencial like '$2%') from odontologia.historial_contrasena where usuario_id=?")) {
            q.setObject(1,u.getId());try(var r=q.executeQuery()){assertTrue(r.next());assertTrue(r.getBoolean(1));}
        }
    }

    @Test void dosCambiosConcurrentesConLaMismaClaveSoloPermitenUno() throws Exception {
        var u=usuario(Rol.RECEPCION);var id=identidad(u);var inicio=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            List<Future<Throwable>> respuestas=new ArrayList<>();
            for(String nueva:List.of("ClaveConcurrente123!","ClaveConcurrente456!")) {
                respuestas.add(pool.submit(()->{inicio.await();try{cambiar(id,INICIAL,nueva);return null;}catch(Throwable e){return e;}}));
            }
            inicio.countDown();int exitos=0,denegados=0;
            for(var f:respuestas){Throwable e=f.get(30,TimeUnit.SECONDS);if(e==null)exitos++;else{assertInstanceOf(AutenticacionFallidaException.class,e);denegados++;}}
            assertEquals(1,exitos);assertEquals(1,denegados);assertEquals(1,cantidad(u.getId()));
        }
    }

    @Test void faltaDePermisoInsertarHistorialRevierteTodaLaOperacion() throws Exception {
        var u=usuario(Rol.RECEPCION);var id=identidad(u);
        try(var c=db.migrationConnection();var q=c.createStatement()) {q.execute("REVOKE INSERT ON odontologia.historial_contrasena FROM odonto_app");}
        try {
            var e=assertThrows(RuntimeException.class,()->cambiar(id,INICIAL,"ClaveRenovada456!"));
            boolean permiso=false;
            for(Throwable t=e;t!=null;t=t.getCause()) if(t instanceof java.sql.SQLException sql && "42501".equals(sql.getSQLState()))permiso=true;
            assertTrue(permiso,"Debe fallar por privilegio SQL, no por otro motivo");
            autentica(u,INICIAL);assertEquals(0,cantidad(u.getId()));
        } finally {
            try(var c=db.migrationConnection();var q=c.createStatement()){q.execute("GRANT INSERT ON odontologia.historial_contrasena TO odonto_app");}
        }
    }

    @Test void integridadSqlYPermisosMinimosDelHistorial() throws Exception {
        var u=usuario(Rol.RECEPCION);UUID id=UUID.randomUUID();
        var ahora=java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
        String sql="insert into odontologia.historial_contrasena(id,usuario_id,secuencia,hash_credencial,creado_en) values(?,?,?,?,?)";
        try(var c=db.appConnection();var q=c.prepareStatement(sql)) {
            Object[] inicial={id,u.getId(),1L,"hash-ficticio",ahora};
            for(int i=0;i<5;i++)q.setObject(i+1,inicial[i]);assertEquals(1,q.executeUpdate());
            record Invalido(Object[] valores,String estado){}
            List<Invalido> invalidos=List.of(
                new Invalido(new Object[]{null,u.getId(),2L,"hash",ahora},"23502"),
                new Invalido(new Object[]{UUID.randomUUID(),null,2L,"hash",ahora},"23502"),
                new Invalido(new Object[]{UUID.randomUUID(),UUID.randomUUID(),2L,"hash",ahora},"23503"),
                new Invalido(new Object[]{UUID.randomUUID(),u.getId(),0L,"hash",ahora},"23514"),
                new Invalido(new Object[]{UUID.randomUUID(),u.getId(),2L,"  ",ahora},"23514"),
                new Invalido(new Object[]{id,u.getId(),2L,"hash",ahora},"23505"),
                new Invalido(new Object[]{UUID.randomUUID(),u.getId(),1L,"hash",ahora},"23505"),
                new Invalido(new Object[]{UUID.randomUUID(),u.getId(),2L,null,ahora},"23502"),
                new Invalido(new Object[]{UUID.randomUUID(),u.getId(),2L,"hash",null},"23502"));
            for(var invalido:invalidos) {
                for(int i=0;i<5;i++)q.setObject(i+1,invalido.valores()[i]);
                var e=assertThrows(java.sql.SQLException.class,q::executeUpdate);assertEquals(invalido.estado(),e.getSQLState());
            }
            try(var st=c.createStatement()) {
                assertEquals("42501",assertThrows(java.sql.SQLException.class,()->st.executeUpdate("DELETE FROM odontologia.historial_contrasena WHERE false")).getSQLState());
                assertEquals("42501",assertThrows(java.sql.SQLException.class,()->st.executeUpdate("UPDATE odontologia.historial_contrasena SET hash_credencial='no' WHERE false")).getSQLState());
            }
        }
        assertEquals(1,cantidad(u.getId()),"Los rechazos deben conservar la fila confirmada");
    }
}
