package ar.edu.utn.odontologia.comunicaciones;

import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

public class AdaptadorResend implements CorreoSalida {
    static final String ENDPOINT = "https://api.resend.com/emails";
    private static final long[] BACKOFF_MS = {20L, 60L};

    private final ConfiguracionEmail config;
    private final AllowlistEmail allowlist;
    private final EmailsEnviados repositorio;
    private final HttpClient httpClient;
    private final String endpoint;

    public AdaptadorResend(ConfiguracionEmail config, EmailsEnviados repositorio) {
        this(config, new AllowlistEmail(config.testAllowlist()), repositorio, HttpClient.newHttpClient(), ENDPOINT);
    }

    public AdaptadorResend(ConfiguracionEmail config, AllowlistEmail allowlist,
                          EmailsEnviados repositorio, HttpClient httpClient) {
        this(config, allowlist, repositorio, httpClient, ENDPOINT);
    }

    public AdaptadorResend(ConfiguracionEmail config, AllowlistEmail allowlist,
                          EmailsEnviados repositorio, HttpClient httpClient, String endpoint) {
        this.config = config;
        this.allowlist = allowlist;
        this.repositorio = repositorio;
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    @Override
    public ResultadoEnvio enviar(SolicitudEnvio solicitud) {
        try {
            if (solicitud == null) {
                return new ResultadoEnvio(false, 0, "solicitud nula", null);
            }
            String tipo = solicitud.tipo();
            UUID turnoId = solicitud.turnoId();
            String destinatario = solicitud.destinatario();
            String key = ClaveIdempotencia.generar(tipo, turnoId, destinatario);

            if (!allowlist.permite(destinatario)) {
                registrarFallo(tipo, turnoId, destinatario, key, 0, "destinatario no permitido");
                return new ResultadoEnvio(false, 0, "destinatario no permitido", key);
            }

            EmailEnvio existente = buscarExistente(key);
            if (existente != null) {
                if ("sent".equals(existente.estado())) {
                    return new ResultadoEnvio(true, existente.intentos(), null, key);
                }
                if ("failed".equals(existente.estado())) {
                    return new ResultadoEnvio(false, existente.intentos(), existente.error(), key);
                }
            }

            EmailEnvio registro = new EmailEnvio(tipo, turnoId, destinatario, key);
            guardar(registro);

            String configError = validarConfiguracion();
            if (configError != null) {
                registro.marcar(false, 0, configError, null);
                guardar(registro);
                return new ResultadoEnvio(false, 0, configError, key);
            }

            int maxIntentos = Math.max(1, 1 + config.maxRetries());
            int intentos = 0;
            String ultimoError = null;
            boolean exito = false;
            for (int i = 0; i < maxIntentos && !exito; i++) {
                intentos++;
                try {
                    HttpResultado r = enviarHttp(solicitud);
                    if (r.exitoso) {
                        exito = true;
                    } else {
                        ultimoError = r.error;
                    }
                } catch (Exception e) {
                    ultimoError = saneado(e);
                }
                if (!exito && i < maxIntentos - 1) {
                    dormir(BACKOFF_MS[Math.min(i, BACKOFF_MS.length - 1)]);
                }
            }

            registro.marcar(exito, intentos, ultimoError, exito ? Instant.now() : null);
            guardar(registro);
            return new ResultadoEnvio(exito, intentos, ultimoError, key);
        } catch (Exception e) {
            String motivo = saneado(e);
            return new ResultadoEnvio(false, 0, motivo, null);
        }
    }

    private EmailEnvio buscarExistente(String key) {
        if (repositorio == null) return null;
        try {
            return repositorio.findByIdempotencyKey(key);
        } catch (Exception e) {
            return null;
        }
    }

    private void guardar(EmailEnvio registro) {
        if (repositorio == null) return;
        try {
            repositorio.save(registro);
        } catch (DataIntegrityViolationException e) {
            // carrera: recuperar existente
        } catch (Exception e) {
            // no interrumpir
        }
    }

    private String validarConfiguracion() {
        if (config.resendApiKey() == null || config.resendApiKey().isBlank()) {
            return "configuración de email incompleta: RESEND_API_KEY";
        }
        if (config.from() == null || config.from().isBlank()) {
            return "configuración de email incompleta: EMAIL_FROM";
        }
        return null;
    }

    private HttpResultado enviarHttp(SolicitudEnvio s) throws Exception {
        String json = construirJson(s);
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(config.timeout())
                .header("Authorization", "Bearer " + config.resendApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return new HttpResultado(true, null);
        }
        String cuerpo = response.body();
        String motivo = "HTTP " + status;
        if (cuerpo != null && !cuerpo.isBlank()) {
            motivo = motivo + ": " + truncar(saneadoTexto(cuerpo));
        }
        return new HttpResultado(false, motivo);
    }

    private String construirJson(SolicitudEnvio s) {
        return "{"
                + "\"from\":\"" + esc(config.from()) + "\","
                + "\"to\":[\"" + esc(s.destinatario()) + "\"],"
                + "\"subject\":\"" + esc(s.asunto()) + "\","
                + "\"text\":\"" + esc(s.cuerpo()) + "\""
                + "}";
    }

    private void registrarFallo(String tipo, UUID turnoId, String destinatario, String key, int intentos, String error) {
        if (repositorio == null) return;
        try {
            EmailEnvio r = new EmailEnvio(tipo, turnoId, destinatario, key);
            r.marcar(false, intentos, error, null);
            repositorio.save(r);
        } catch (Exception e) {
            // no interrumpir
        }
    }

    private static String esc(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static String saneado(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
        return truncar(msg);
    }

    private static String saneadoTexto(String s) {
        return s == null ? "" : s;
    }

    private static String truncar(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record HttpResultado(boolean exitoso, String error) {}
}