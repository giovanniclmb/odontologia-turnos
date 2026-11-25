package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.comunicaciones.AdaptadorResend;
import ar.edu.utn.odontologia.comunicaciones.AllowlistEmail;
import ar.edu.utn.odontologia.comunicaciones.ClaveIdempotencia;
import ar.edu.utn.odontologia.comunicaciones.ConfiguracionEmail;
import ar.edu.utn.odontologia.comunicaciones.ResultadoEnvio;
import ar.edu.utn.odontologia.comunicaciones.SolicitudEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.dao.DataIntegrityViolationException;

class EmailEnvioPersistenciaIT {
    private HttpServer server;

    @AfterEach
    void detenerServidor() {
        if (server != null) server.stop(0);
    }

    @Test
    void guardaYRecuperaPorClave() {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            var repo = ctx.getBean(EmailsEnviados.class);
            UUID turno = UUID.randomUUID();
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
            var e = new EmailEnvio("ALTA", turno, "dest@test.com", key);
            e.marcar(true, 2, null, java.time.Instant.now());
            repo.save(e);

            var encontrado = repo.findByIdempotencyKey(key);
            assertNotNull(encontrado);
            assertEquals("sent", encontrado.estado());
            assertEquals(2, encontrado.intentos());
            assertEquals(turno, encontrado.turnoId());
        }
    }

    @Test
    void claveDuplicadaViolentaUnicidad() {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            var repo = ctx.getBean(EmailsEnviados.class);
            UUID turno = UUID.randomUUID();
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
            repo.save(new EmailEnvio("ALTA", turno, "dest@test.com", key));
            assertThrows(DataIntegrityViolationException.class,
                    () -> repo.save(new EmailEnvio("ALTA", turno, "dest@test.com", key)));
        }
    }

    @Test
    void conservaEnvioPersistidoAunqueLaTransaccionDelLlamadorSeRevierta() throws Exception {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            AtomicInteger requests = new AtomicInteger();
            String endpoint = iniciarServidor(requests, 0);
            var configuracion = configuracionEmail();
            var adaptador = new AdaptadorResend(configuracion,
                    new AllowlistEmail(configuracion.testAllowlist()), ctx.getBean(EmailsEnviados.class),
                    java.net.http.HttpClient.newHttpClient(), endpoint);
            var tx = new org.springframework.transaction.support.TransactionTemplate(
                    ctx.getBean(org.springframework.transaction.PlatformTransactionManager.class));
            UUID turno = UUID.randomUUID();
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
            var resultado = new AtomicReference<ResultadoEnvio>();

            tx.executeWithoutResult(status -> {
                resultado.set(adaptador.enviar(new SolicitudEnvio("ALTA", turno, "dest@test.com", "subj", "body")));
                status.setRollbackOnly();
            });

            assertTrue(resultado.get().exitoso());
            assertEquals(1, requests.get());
            assertEquals("sent", ctx.getBean(EmailsEnviados.class).findByIdempotencyKey(key).estado());
        }
    }

    @Test
    void soloUnaSolicitudConcurrenteDeLaMismaClaveContactaAlProveedor() throws Exception {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            AtomicInteger requests = new AtomicInteger();
            String endpoint = iniciarServidor(requests, 150);
            var configuracion = configuracionEmail();
            var adaptador = new AdaptadorResend(configuracion,
                    new AllowlistEmail(configuracion.testAllowlist()), ctx.getBean(EmailsEnviados.class),
                    java.net.http.HttpClient.newHttpClient(), endpoint);
            UUID turno = UUID.randomUUID();
            var solicitud = new SolicitudEnvio("ALTA", turno, "dest@test.com", "subj", "body");
            var inicio = new CountDownLatch(1);
            var listos = new CountDownLatch(2);
            ResultadoEnvio resultadoPrimero;
            ResultadoEnvio resultadoSegundo;
            try (var ejecutor = Executors.newFixedThreadPool(2)) {
                var primero = ejecutor.submit(() -> enviarConcurrente(adaptador, solicitud, inicio, listos));
                var segundo = ejecutor.submit(() -> enviarConcurrente(adaptador, solicitud, inicio, listos));
                assertTrue(listos.await(5, TimeUnit.SECONDS));
                inicio.countDown();
                resultadoPrimero = primero.get(10, TimeUnit.SECONDS);
                resultadoSegundo = segundo.get(10, TimeUnit.SECONDS);
            }

            assertEquals(1, requests.get());
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
            assertTrue(resultadoPrimero.exitoso() || resultadoSegundo.exitoso());
            assertEquals(key, resultadoPrimero.idempotencyKey());
            assertEquals(key, resultadoSegundo.idempotencyKey());
            assertTrue(resultadoPrimero.exitoso() || resultadoPrimero.error().contains("pendiente"));
            assertTrue(resultadoSegundo.exitoso() || resultadoSegundo.error().contains("pendiente"));
            assertEquals("sent", ctx.getBean(EmailsEnviados.class).findByIdempotencyKey(key).estado());
        }
    }

    @Test
    void falloAlPersistirResultadoFinalDevuelveFalloYDejaClaveReservada() throws Exception {
        try (var db = PostgresFixture.start(); var ctx = abrir(db)) {
            AtomicInteger requests = new AtomicInteger();
            String endpoint = iniciarServidor(requests, 0);
            EmailsEnviados postgres = ctx.getBean(EmailsEnviados.class);
            var repo = mock(EmailsEnviados.class);
            AtomicInteger escrituras = new AtomicInteger();
            when(repo.findByIdempotencyKey(any())).thenAnswer(invocation ->
                    postgres.findByIdempotencyKey(invocation.getArgument(0)));
            when(repo.saveAndFlush(any(EmailEnvio.class))).thenAnswer(invocation -> {
                if (escrituras.incrementAndGet() == 1) return postgres.saveAndFlush(invocation.getArgument(0));
                throw new IllegalStateException("database unavailable");
            });
            var configuracion = configuracionEmail();
            var adaptador = new AdaptadorResend(configuracion,
                    new AllowlistEmail(configuracion.testAllowlist()), repo,
                    java.net.http.HttpClient.newHttpClient(), endpoint);
            UUID turno = UUID.randomUUID();
            var solicitud = new SolicitudEnvio("ALTA", turno, "dest@test.com", "subj", "body");
            String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");

            var primero = adaptador.enviar(solicitud);
            var repetido = adaptador.enviar(solicitud);

            assertFalse(primero.exitoso());
            assertTrue(primero.error().contains("resultado"));
            assertFalse(repetido.exitoso());
            assertTrue(repetido.error().contains("pendiente"));
            assertEquals(1, requests.get());
            assertEquals("pending", postgres.findByIdempotencyKey(key).estado());
        }
    }

    private ResultadoEnvio enviarConcurrente(AdaptadorResend adaptador, SolicitudEnvio solicitud,
                                              CountDownLatch inicio, CountDownLatch listos) {
        listos.countDown();
        try {
            if (!inicio.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("no inició la barrera");
            return adaptador.enviar(solicitud);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private String iniciarServidor(AtomicInteger requests, long retrasoMs) throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            if (retrasoMs > 0) {
                try {
                    Thread.sleep(retrasoMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exchange.close();
                    return;
                }
            }
            byte[] body = "{\"id\":\"local-test\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    private ConfiguracionEmail configuracionEmail() {
        var config = new ConfiguracionEmail();
        config.setResendApiKey("key-test");
        config.setFrom("from@test.com");
        config.setTestAllowlist(java.util.List.of("dest@test.com"));
        config.setMaxRetries(0);
        config.setTimeout(Duration.ofSeconds(5));
        return config;
    }

    private static ConfigurableApplicationContext abrir(PostgresFixture db) {
        var env = new StandardEnvironment();
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        env.getPropertySources().addFirst(new MapPropertySource("postgresTemporal", db.properties()));
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE)
                .environment(env).run("--spring.config.location=classpath:/application.yaml");
    }
}
