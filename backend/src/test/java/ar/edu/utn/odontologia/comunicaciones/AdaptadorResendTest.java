package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;
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
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", ex -> {
            int n = counter.incrementAndGet();
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
}