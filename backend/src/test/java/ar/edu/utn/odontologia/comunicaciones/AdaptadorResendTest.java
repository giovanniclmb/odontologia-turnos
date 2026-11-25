package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AdaptadorResendTest {
    private HttpServer server;

    private HttpServer start(int status, String body, AtomicInteger counter) throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", ex -> {
            counter.incrementAndGet();
            byte[] bytes = body.getBytes();
            ex.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        });
        server.start();
        return server;
    }

    @AfterEach
    void stop() { if (server != null) server.stop(0); }

    private ConfiguracionEmail config(String allow) {
        ConfiguracionEmail c = new ConfiguracionEmail();
        c.setResendApiKey("key-test");
        c.setFrom("from@test.com");
        c.setTestAllowlist(java.util.List.of(allow));
        c.setMaxRetries(2);
        c.setTimeout(Duration.ofSeconds(5));
        return c;
    }

    @Test
    void envioExitoso() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        start(200, "{\"id\":\"abc\"}", counter);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        var cfg = config("dest@test.com");
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), null, java.net.http.HttpClient.newHttpClient(), url);
        var r = adapter.enviar(new SolicitudEnvio("ALTA", UUID.randomUUID(), "dest@test.com", "subj", "body"));
        assertTrue(r.exitoso());
        assertEquals(1, r.intentos());
        assertEquals(1, counter.get());
    }

    @Test
    void allowlistRechazaSinContactar() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        start(200, "{}", counter);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        var cfg = config("permitido@test.com");
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), null, java.net.http.HttpClient.newHttpClient(), url);
        var r = adapter.enviar(new SolicitudEnvio("ALTA", UUID.randomUUID(), "otro@test.com", "subj", "body"));
        assertFalse(r.exitoso());
        assertTrue(r.error().contains("permitido") || r.error().contains("permit"));
        assertEquals(0, counter.get());
    }

    @Test
    void recuperaEnReintento() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        var keys = new java.util.concurrent.CopyOnWriteArrayList<String>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", ex -> {
            int n = counter.incrementAndGet();
            keys.add(ex.getRequestHeaders().getFirst("Idempotency-Key"));
            if (n == 1) {
                byte[] b = "err".getBytes();
                ex.sendResponseHeaders(500, b.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(b); }
            } else {
                byte[] b = "{}".getBytes();
                ex.sendResponseHeaders(200, b.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(b); }
            }
        });
        server.start();
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        var cfg = config("dest@test.com");
        cfg.setMaxRetries(2);
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), null, java.net.http.HttpClient.newHttpClient(), url);
        var r = adapter.enviar(new SolicitudEnvio("ALTA", UUID.randomUUID(), "dest@test.com", "subj", "body"));
        assertTrue(r.exitoso());
        assertEquals(2, r.intentos());
        assertEquals(2, keys.size());
        assertEquals(r.idempotencyKey(), keys.get(0));
        assertEquals(r.idempotencyKey(), keys.get(1));
    }

    @Test
    void agotaReintentos() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        start(500, "err", counter);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        var cfg = config("dest@test.com");
        cfg.setMaxRetries(1);
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), null, java.net.http.HttpClient.newHttpClient(), url);
        var r = adapter.enviar(new SolicitudEnvio("ALTA", UUID.randomUUID(), "dest@test.com", "subj", "body"));
        assertFalse(r.exitoso());
        assertEquals(2, r.intentos());
        assertEquals(2, counter.get());
    }

    @Test
    void noPropagaExcepcionCuandoServidorCae() throws Exception {
        // endpoint a puerto cerrado
        var cfg = config("dest@test.com");
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), null, java.net.http.HttpClient.newHttpClient(), "http://127.0.0.1:1/");
        var r = adapter.enviar(new SolicitudEnvio("ALTA", UUID.randomUUID(), "dest@test.com", "subj", "body"));
        assertFalse(r.exitoso());
        assertNotNull(r.error());
    }

    @Test
    void noDevuelveExitoCuandoNoPuedePersistirResultadoFinal() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        start(200, "{\"id\":\"abc\"}", counter);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        var cfg = config("dest@test.com");
        var repo = mock(EmailsEnviados.class);
        when(repo.saveAndFlush(any(EmailEnvio.class))).thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new IllegalStateException("database unavailable"));
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), repo,
                java.net.http.HttpClient.newHttpClient(), url);

        var result = adapter.enviar(new SolicitudEnvio("ALTA", UUID.randomUUID(), "dest@test.com", "subj", "body"));

        assertFalse(result.exitoso());
        assertNotNull(result.error());
        assertTrue(result.error().contains("persist"));
        assertEquals(1, counter.get());
    }

    @Test
    void noReenviaCuandoLaClaveYaTieneUnEnvioPendiente() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        start(200, "{\"id\":\"abc\"}", counter);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        var cfg = config("dest@test.com");
        var repo = mock(EmailsEnviados.class);
        UUID turno = UUID.randomUUID();
        String key = ClaveIdempotencia.generar("ALTA", turno, "dest@test.com");
        when(repo.findByIdempotencyKey(key)).thenReturn(new EmailEnvio("ALTA", turno, "dest@test.com", key));
        var adapter = new AdaptadorResend(cfg, new AllowlistEmail(cfg.testAllowlist()), repo,
                java.net.http.HttpClient.newHttpClient(), url);

        var result = adapter.enviar(new SolicitudEnvio("ALTA", turno, "dest@test.com", "subj", "body"));

        assertFalse(result.exitoso());
        assertTrue(result.error().contains("pendiente"));
        assertEquals(0, counter.get());
        verify(repo, never()).saveAndFlush(any(EmailEnvio.class));
    }
}
