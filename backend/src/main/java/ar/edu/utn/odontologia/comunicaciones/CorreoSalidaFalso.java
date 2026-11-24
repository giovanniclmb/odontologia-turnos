package ar.edu.utn.odontologia.comunicaciones;

import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailEnvio;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CorreoSalidaFalso implements CorreoSalida {
    public record Registro(SolicitudEnvio solicitud, ResultadoEnvio resultado) {}

    private final EmailsEnviados repositorio;
    private final AllowlistEmail allowlist;
    private boolean forzarExito = true;
    private String forzarError = null;
    private int intentosForzados = 1;
    private final List<Registro> registros = new ArrayList<>();

    public CorreoSalidaFalso() {
        this(null, null);
    }

    public CorreoSalidaFalso(EmailsEnviados repositorio) {
        this(repositorio, null);
    }

    public CorreoSalidaFalso(EmailsEnviados repositorio, AllowlistEmail allowlist) {
        this.repositorio = repositorio;
        this.allowlist = allowlist;
    }

    public void forzarExito() {
        forzarExito = true;
        forzarError = null;
    }

    public void forzarFallo(String error) {
        forzarExito = false;
        forzarError = error == null ? "fallo forzado" : error;
    }

    public void establecerIntentos(int intentos) {
        this.intentosForzados = Math.max(1, intentos);
    }

    public List<Registro> registros() {
        return List.copyOf(registros);
    }

    public void limpiar() {
        registros.clear();
    }

    @Override
    public ResultadoEnvio enviar(SolicitudEnvio solicitud) {
        try {
            if (solicitud == null) {
                return new ResultadoEnvio(false, 0, "solicitud nula", null);
            }
            String key = ClaveIdempotencia.generar(solicitud.tipo(), solicitud.turnoId(), solicitud.destinatario());

            if (allowlist != null && !allowlist.permite(solicitud.destinatario())) {
                ResultadoEnvio r = new ResultadoEnvio(false, 0, "destinatario no permitido", key);
                registros.add(new Registro(solicitud, r));
                return r;
            }

            if (repositorio != null) {
                EmailEnvio existente = repositorio.findByIdempotencyKey(key);
                if (existente != null) {
                    if ("sent".equals(existente.estado())) {
                        ResultadoEnvio r = new ResultadoEnvio(true, existente.intentos(), null, key);
                        registros.add(new Registro(solicitud, r));
                        return r;
                    }
                    if ("failed".equals(existente.estado())) {
                        ResultadoEnvio r = new ResultadoEnvio(false, existente.intentos(), existente.error(), key);
                        registros.add(new Registro(solicitud, r));
                        return r;
                    }
                }
            }

            boolean exito = forzarExito;
            String error = exito ? null : forzarError;
            int intentos = intentosForzados;

            if (repositorio != null) {
                EmailEnvio registro = new EmailEnvio(solicitud.tipo(), solicitud.turnoId(), solicitud.destinatario(), key);
                registro.marcar(exito, intentos, error, exito ? Instant.now() : null);
                repositorio.save(registro);
            }

            ResultadoEnvio r = new ResultadoEnvio(exito, intentos, error, key);
            registros.add(new Registro(solicitud, r));
            return r;
        } catch (Exception e) {
            String motivo = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            ResultadoEnvio r = new ResultadoEnvio(false, 0, motivo, null);
            registros.add(new Registro(solicitud, r));
            return r;
        }
    }
}