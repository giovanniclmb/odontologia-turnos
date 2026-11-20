package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BootstrapCliIT {
    @Test void cliAisladoCreaPrimerUsuarioYRechazaSegundoSinHeredarSpring() throws Exception {
        // Centinelas sin credenciales reales: nunca se conecta el JVM padre a Spring.
        Map<String, String> anteriores = new HashMap<>();
        List<String> claves = List.of("spring.flyway.url", "spring.datasource.url", "spring.config.location", "BOOTSTRAP_CONTRASENA");
        claves.forEach(k -> anteriores.put(k, System.getProperty(k)));
        try (var db = PostgresFixture.start()) {
            System.setProperty("spring.flyway.url", "jdbc:postgresql://127.0.0.1:1/no_usar");
            System.setProperty("spring.datasource.url", "jdbc:postgresql://127.0.0.1:1/no_usar");
            System.setProperty("spring.config.location", "file:/no_usar/application.yaml");
            System.setProperty("BOOTSTRAP_CONTRASENA", "centinela_no_usar");
            String nombre = "CLI-" + UUID.randomUUID();
            Map<String,String> entorno = new HashMap<>();
            db.properties().forEach((k,v) -> entorno.put(k,v.toString()));
            entorno.put("BOOTSTRAP_NOMBRE_USUARIO", nombre);
            entorno.put("BOOTSTRAP_CONTRASENA", "ClaveSegura123");
            entorno.put("BOOTSTRAP_ROL", "recepcion");
            List<String> comando = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-Xmx256m", "-cp", System.getProperty("java.class.path"),
                    "ar.edu.utn.odontologia.acceso.cli.BootstrapCli", "--spring.config.location=classpath:/application.yaml");
            var primero = ProcesoAislado.ejecutar(comando, entorno, 45);
            assertEquals(0, primero.salida(), "El CLI debe crear la cuenta en el fixture");
            assertTrue(primero.log().contains("Bootstrap creado:"));
            assertFalse(primero.log().contains(entorno.get("BOOTSTRAP_CONTRASENA")));
            assertFalse(primero.log().contains(db.appPassword()));
            assertFalse(primero.log().contains(db.migrationPassword()));
            try (var c=db.appConnection();var q=c.prepareStatement("select hash_credencial from odontologia.usuario where nombre_usuario=?")) {
                q.setString(1,nombre);
                try(var r=q.executeQuery()) {
                    assertTrue(r.next());assertTrue(new BCryptPasswordEncoder().matches("ClaveSegura123",r.getString(1)));
                    assertFalse(r.next());
                }
            }
            var segundo = ProcesoAislado.ejecutar(comando, entorno, 45);
            assertNotEquals(0, segundo.salida());
            assertTrue(segundo.log().contains("Bootstrap ya ejecutado"));
            try(var c=db.appConnection();var q=c.createStatement();var r=q.executeQuery("select count(*) from odontologia.usuario")) {
                assertTrue(r.next());assertEquals(1,r.getInt(1));
            }
            assertEquals("jdbc:postgresql://127.0.0.1:1/no_usar",System.getProperty("spring.flyway.url"));
        } finally {
            anteriores.forEach((k,v)->{if(v==null)System.clearProperty(k);else System.setProperty(k,v);});
        }
    }
}
