package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.AutenticacionFallidaException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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