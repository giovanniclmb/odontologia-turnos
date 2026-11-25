package ar.edu.utn.odontologia.comunicaciones;

import static ar.edu.utn.odontologia.agenda.AgendaConReloj.INICIO;
import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.agenda.AgendaConReloj;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

/** La aplicación activada envía al arrancar y, al cerrarse, termina el envío en curso sin iniciar otro. */
class CierreRecordatorioIT {
    @Test
    void cerrarDuranteUnEnvioLoDejaTerminarYNoIniciaElSiguiente() throws Exception {
        try (var escenario = AgendaConReloj.iniciar()) {
            escenario.alta(escenario.nuevosDatos(), INICIO);
            escenario.alta(escenario.nuevosDatos(), INICIO.plus(Duration.ofHours(1)));
            var enCurso = new CountDownLatch(1);
            var terminar = new CountDownLatch(1);
            var interrumpido = new AtomicBoolean();
            List<SolicitudEnvio> solicitudes = new CopyOnWriteArrayList<>();
            CorreoSalida correo = solicitud -> {
                solicitudes.add(solicitud);
                enCurso.countDown();
                try {
                    if (!terminar.await(20, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("La prueba no liberó el envío");
                    }
                } catch (InterruptedException interrupcion) {
                    interrumpido.set(true);
                    Thread.currentThread().interrupt();
                }
                return new ResultadoEnvio(true, 1, null, null);
            };

            var activada = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(contexto -> {
                var fuentes = contexto.getEnvironment().getPropertySources();
                fuentes.addFirst(new MapPropertySource("postgres-fixture", escenario.db.properties()));
                fuentes.addFirst(new MapPropertySource("activado", Map.of("email.recordatorio.activo", "true")));
                var generico = (GenericApplicationContext) contexto;
                generico.registerBean("relojDeCierre", Clock.class,
                        () -> Clock.fixed(INICIO.minus(Duration.ofHours(10)), ZoneOffset.UTC),
                        bean -> bean.setPrimary(true));
                generico.registerBean("correoDeCierre", CorreoSalida.class, () -> correo,
                        bean -> bean.setPrimary(true));
                // Se detiene antes que los demás, ya avisado el cierre; el envío termina un momento después, así
                // que solo no se interrumpe si la aplicación lo espera.
                var despues = CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS);
                generico.registerBean(AlDetener.class, () -> new AlDetener(() -> despues.execute(terminar::countDown)));
            }).properties("server.port=0").run();
            try {
                assertTrue(enCurso.await(10, TimeUnit.SECONDS), "Debe revisar al arrancar");
            } finally {
                activada.close();
            }

            assertFalse(interrumpido.get(), "El envío en curso no se interrumpe");
            assertEquals(1, solicitudes.size(), "Durante el cierre no se inicia otro envío");
        }
    }

    private static final class AlDetener implements SmartLifecycle {
        private final Runnable accion;
        private volatile boolean activo;

        AlDetener(Runnable accion) {
            this.accion = accion;
        }

        @Override
        public void start() {
            activo = true;
        }

        @Override
        public void stop() {
            activo = false;
            accion.run();
        }

        @Override
        public boolean isRunning() {
            return activo;
        }

        @Override
        public int getPhase() {
            return Integer.MAX_VALUE;
        }
    }
}
