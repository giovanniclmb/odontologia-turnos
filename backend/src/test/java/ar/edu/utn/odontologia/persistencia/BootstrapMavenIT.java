package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BootstrapMavenIT {
    @TempDir Path temporal;
    @Test void comandoMavenDocumentadoEjecutaBootstrapReal() throws Exception {
        try(var db=PostgresFixture.start()) {
            // Sin settings del equipo, sin descargas y sin configuración heredada de Spring/Maven.
            Path settings=temporal.resolve("settings.xml");
            Files.writeString(settings,"<settings xmlns=\"http://maven.apache.org/SETTINGS/1.2.0\"/>");
            Path maven=Arrays.stream(System.getenv().getOrDefault("PATH", "").split(java.io.File.pathSeparator))
                    .filter(p->!p.isBlank()).map(p->Path.of(p,"mvn")).filter(Files::isExecutable)
                    .findFirst().orElseThrow(()->new IllegalStateException("Maven no está disponible en PATH"));
            String repositorio=System.getProperty("maven.repo.local",Path.of(System.getProperty("user.home"),".m2","repository").toString());
            Map<String,String> env=new HashMap<>();db.properties().forEach((k,v)->env.put(k,v.toString()));
            String nombre="MAVEN-"+UUID.randomUUID();
            env.put("BOOTSTRAP_NOMBRE_USUARIO",nombre);env.put("BOOTSTRAP_CONTRASENA","ClaveSegura123");env.put("BOOTSTRAP_ROL","recepcion");
            env.put("JAVA_HOME",System.getProperty("java.home"));env.put("HOME",temporal.toString());
            env.put("MAVEN_OPTS","-Xmx256m -Duser.home="+temporal);
            List<String> comando=List.of(maven.toString(),"-B","-ntp","-o","-s",settings.toString(),"-gs",settings.toString(),
                    "-Dmaven.repo.local="+repositorio,"-f","backend/pom.xml","spring-boot:run",
                    "-Dspring-boot.run.main-class=ar.edu.utn.odontologia.acceso.cli.BootstrapCli",
                    "-Dspring-boot.run.arguments=--spring.config.location=classpath:/application.yaml");
            var primero=ProcesoAislado.ejecutar(comando,env,120,Path.of("..").toAbsolutePath().normalize());
            assertEquals(0,primero.salida(),"El comando Maven documentado debe funcionar sin configuración del anfitrión");
            assertTrue(primero.log().contains("Bootstrap creado:"));
            assertFalse(primero.log().contains(env.get("BOOTSTRAP_CONTRASENA")));
            assertFalse(primero.log().contains(db.appPassword()));assertFalse(primero.log().contains(db.migrationPassword()));
            try(var c=db.appConnection();var q=c.prepareStatement("select hash_credencial from odontologia.usuario where nombre_usuario=?")) {
                q.setString(1,nombre);try(var r=q.executeQuery()) {assertTrue(r.next());assertTrue(new BCryptPasswordEncoder().matches("ClaveSegura123",r.getString(1)));assertFalse(r.next());}
            }
            var segundo=ProcesoAislado.ejecutar(comando,env,120,Path.of("..").toAbsolutePath().normalize());
            assertNotEquals(0,segundo.salida());assertTrue(segundo.log().contains("Bootstrap ya ejecutado"));
        }
    }
}
