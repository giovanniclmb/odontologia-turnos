package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class EjecutorEmailTest {
    @Test
    void conLaColaLlenaDescartaYRegistraSinBloquearAQuienEnvia(CapturedOutput salida) throws Exception {
        var ejecutor = new EnvioEmailAsincronoConfig().ejecutorEmail();
        ejecutor.initialize();
        var liberar = new CountDownLatch(1);
        var ejecutadas = new AtomicInteger();
        try {
            ejecutor.execute(() -> esperar(liberar));
            for (int i = 0; i < 101; i++) {
                assertDoesNotThrow(() -> ejecutor.execute(ejecutadas::incrementAndGet));
            }
            assertTrue(salida.getAll().contains("Aviso de turno descartado"));
            liberar.countDown();
            // El cierre ordenado ejecuta lo encolado; encolar otra tarea podría rechazarse con la cola aún llena.
            ejecutor.getThreadPoolExecutor().shutdown();
            assertTrue(ejecutor.getThreadPoolExecutor().awaitTermination(10, TimeUnit.SECONDS));
        } finally {
            liberar.countDown();
            ejecutor.shutdown();
        }
        assertEquals(100, ejecutadas.get());
        assertEquals(1, ejecutor.getCorePoolSize());
        assertEquals(1, ejecutor.getMaxPoolSize());
    }

    private static void esperar(CountDownLatch liberar) {
        try {
            liberar.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }
}
