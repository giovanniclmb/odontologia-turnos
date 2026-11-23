package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

class ArranqueJarIT {
    @Test void jarEjecutableArrancaYTerminaSinServidor() throws Exception {
        try(var db=PostgresFixture.start()) {
            var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),
                    "-jar","target/agenda-dominio-0.1.0-SNAPSHOT.jar");
            builder.redirectErrorStream(true);
            builder.environment().clear();
            db.properties().forEach((key,value)->builder.environment().put(key,value.toString()));
            var process=builder.start();
            try {
                var output=CompletableFuture.supplyAsync(()->{
                    try{return new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8);}
                    catch(java.io.IOException e){throw new CompletionException(e);}
                });
                assertTrue(process.waitFor(90,TimeUnit.SECONDS),"El proceso no-web debe terminar");
                String log=output.get(5,TimeUnit.SECONDS);
                System.out.println("JAR_OUTPUT_START\n" + log + "\nJAR_OUTPUT_END");
        assertEquals(0,process.exitValue(),"El JAR debe poder arrancar con el entorno de prueba");
                assertTrue(log.contains("Started OdontologiaApplication"));
                assertFalse(log.contains("Tomcat started"));assertFalse(log.contains("Jetty started"));
                assertFalse(log.contains(db.appPassword()));assertFalse(log.contains(db.migrationPassword()));
            } finally {
                if(process.isAlive())process.destroyForcibly().waitFor(5,TimeUnit.SECONDS);
            }
        }
    }
}
