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
                return resultadoExistente(existente, key);
            }

            EmailEnvio registro = new EmailEnvio(tipo, turnoId, destinatario, key);
            boolean adquirido = guardarAtomicamente(registro);
            if (!adquirido) {
                existente = buscarExistente(key);
                if (existente != null) {
                    return resultadoExistente(existente, key);
                }
                return new ResultadoEnvio(false, 0, "no se pudo adquirir la clave de idempotencia", key);
            }

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
                    HttpResultado r = enviarHttp(solicitud, key);
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

    private static ResultadoEnvio resultadoExistente(EmailEnvio existente, String key) {
        return switch (existente.estado()) {
            case "sent" -> new ResultadoEnvio(true, existente.intentos(), null, key);
            case "failed" -> new ResultadoEnvio(false, existente.intentos(), existente.error(), key);
            case "pending" -> new ResultadoEnvio(false, existente.intentos(), "envio pendiente", key);
            default -> new ResultadoEnvio(false, existente.intentos(), "estado de envio desconocido", key);
        };
    }

    private boolean guardarAtomicamente(EmailEnvio registro) {
        if (repositorio == null) return true;
        try {
            repositorio.saveAndFlush(registro);
            return true;
        } catch (DataIntegrityViolationException e) {
            if (esConflictoDeIdempotencia(e)) return false;
            throw new PersistenciaEmailException("no se pudo persistir el registro de envio", e);
        } catch (Exception e) {
            throw new PersistenciaEmailException("no se pudo persistir el registro de envio", e);
        }
    }

    private static boolean esConflictoDeIdempotencia(Throwable error) {
        for (Throwable actual = error; actual != null; actual = actual.getCause()) {
            if (actual instanceof org.postgresql.util.PSQLException postgres) {
                var detalle = postgres.getServerErrorMessage();
                return "23505".equals(postgres.getSQLState()) && detalle != null
                        && "email_envio_idempotency_key_uk".equals(detalle.getConstraint());
            }
        }
        return false;
    }

    private void guardar(EmailEnvio registro) {
        if (repositorio == null) return;
        try {
            repositorio.saveAndFlush(registro);
        } catch (Exception e) {
            throw new PersistenciaEmailException("no se pudo persistir el resultado del envio", e);
        }
    }

    private EmailEnvio buscarExistente(String key) {
        if (repositorio == null) return null;
        try {
            return repositorio.findByIdempotencyKey(key);
        } catch (Exception e) {
            throw new PersistenciaEmailException("no se pudo consultar el registro de envio", e);
        }
    }

    private String validarConfiguracion() {
        if (config.resendApiKey() == null || config.resendApiKey().isBlank()) {
            return "configuracion de email incompleta: RESEND_API_KEY";
        }
        if (config.from() == null || config.from().isBlank()) {
            return "configuracion de email incompleta: EMAIL_FROM";
        }
        return null;
    }

    private HttpResultado enviarHttp(SolicitudEnvio s, String idempotencyKey) throws Exception {
        String json = construirJson(s);
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(config.timeout())
                .header("Authorization", "Bearer " + config.resendApiKey())
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return new HttpResultado(true, null);
        }
        // La respuesta del proveedor no se registra: puede repetir el mensaje y su enlace.
        return new HttpResultado(false, "HTTP " + status);
    }

    private String construirJson(SolicitudEnvio s) {
        return "{" +
                "\"from\":\"" + esc(config.from()) + "\"," +
                "\"to\":[\"" + esc(s.destinatario()) + "\"]," +
                "\"subject\":\"" + esc(s.asunto()) + "\"," +
                "\"text\":\"" + esc(s.cuerpo()) + "\"" +
                "}";
    }

    private void registrarFallo(String tipo, UUID turnoId, String destinatario, String key, int intentos, String error) {
        if (repositorio == null) return;
        try {
            EmailEnvio r = new EmailEnvio(tipo, turnoId, destinatario, key);
            r.marcar(false, intentos, error, null);
            repositorio.saveAndFlush(r);
        } catch (Exception e) {
            throw new PersistenciaEmailException("no se pudo persistir el rechazo del envio", e);
        }
    }

    private static String esc(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /** Solo los mensajes propios; de cualquier otra falla se informa el tipo, sin datos del envío. */
    private static String saneado(Exception e) {
        return e instanceof PersistenciaEmailException ? e.getMessage() : e.getClass().getSimpleName();
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
