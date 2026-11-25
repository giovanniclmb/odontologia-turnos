package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

class ArranqueJarIT {
    @Test void jarEjecutableArrancaServidorHttp() throws Exception {
        try(var db=PostgresFixture.start()) {
            var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),
                    "-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"),
                    "-jar","target/agenda-dominio-0.1.0-SNAPSHOT.jar");
            builder.redirectErrorStream(true);
            builder.environment().clear();
            db.properties().forEach((key,value)->builder.environment().put(key,value.toString()));
            var process=builder.start();
            var output=new StringBuilder();
            var readerThread=new Thread(() -> {
                try (var in=process.getInputStream()) {
                    int b;
                    while ((b=in.read()) != -1) output.append((char)b);
                } catch (java.io.IOException ignored) {
                }
            });
            readerThread.start();
            boolean started=false;
            for (int i=0;i<90 && !started;i++) {
                Thread.sleep(1000);
                String text=output.toString();
                if (text.contains("Started OdontologiaApplication") || text.contains("Tomcat started")) started=true;
            }
            if (process.isAlive()) process.destroyForcibly().waitFor(5,TimeUnit.SECONDS);
            readerThread.join(5000);
            String log=output.toString();
            System.out.println("JAR_OUTPUT_START\n" + log + "\nJAR_OUTPUT_END");
            assertTrue(started, "El JAR debe arrancar el servidor HTTP");
            assertTrue(log.contains("Started OdontologiaApplication"));
            assertFalse(log.contains(db.appPassword()));assertFalse(log.contains(db.migrationPassword()));
        }
    }
}