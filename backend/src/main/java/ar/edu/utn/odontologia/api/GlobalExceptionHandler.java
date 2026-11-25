package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.AutenticacionFallidaException;
import ar.edu.utn.odontologia.agenda.ConflictoAgendaException;
import ar.edu.utn.odontologia.agenda.VersionTurnoDesactualizadaException;
import ar.edu.utn.odontologia.agenda.bloqueos.TurnoOcupanteException;
import ar.edu.utn.odontologia.recursos.IntervaloSinConfigurarException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AutenticacionFallidaException.class)
    public ResponseEntity<Map<String, String>> autenticacion(AutenticacionFallidaException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("codigo", "autenticacion_fallida", "mensaje", "Credenciales invalidas"));
    }

    @ExceptionHandler(AutorizacionDenegadaException.class)
    public ResponseEntity<Map<String, String>> autorizacion(AutorizacionDenegadaException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("codigo", "autorizacion_denegada", "mensaje", "Operacion no autorizada"));
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> sinIdentidad() {
        return respuesta(HttpStatus.UNAUTHORIZED, "no_autenticado", "Token invalido o ausente");
    }

    @ExceptionHandler(ConflictoAgendaException.class)
    public ResponseEntity<Map<String, String>> horario() {
        return respuesta(HttpStatus.CONFLICT, "horario_no_disponible", "El horario solicitado no está disponible");
    }

    @ExceptionHandler(VersionTurnoDesactualizadaException.class)
    public ResponseEntity<Map<String, String>> versionDesactualizada(VersionTurnoDesactualizadaException error) {
        return respuesta(HttpStatus.CONFLICT, "version_turno_desactualizada", error.getMessage());
    }

    @ExceptionHandler(TurnoOcupanteException.class)
    public ResponseEntity<Map<String, String>> turnoOcupante() {
        return respuesta(HttpStatus.CONFLICT, "bloqueo_afecta_turno", "El bloqueo afecta a un turno existente");
    }

    @ExceptionHandler(IntervaloSinConfigurarException.class)
    public ResponseEntity<Map<String, String>> sinIntervalo() {
        return respuesta(HttpStatus.CONFLICT, "intervalo_sin_configurar", "El intervalo de sede está sin configurar");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, String>> malFormada() {
        return respuesta(HttpStatus.BAD_REQUEST, "solicitud_invalida", "Solicitud invalida");
    }

    private static ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("codigo", codigo, "mensaje", mensaje));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumento(IllegalArgumentException ex, HttpServletRequest request) {
        String mensaje = ex.getMessage() == null ? "Solicitud invalida" : ex.getMessage();
        HttpStatus status = mensaje.toLowerCase().contains("no existe") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("codigo", "solicitud_invalida", "mensaje", mensaje));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflicto(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("codigo", "conflicto", "mensaje", "Conflicto de datos"));
    }
}
