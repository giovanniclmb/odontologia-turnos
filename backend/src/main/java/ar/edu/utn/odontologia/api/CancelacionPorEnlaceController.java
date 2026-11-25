package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioCancelacionPorEnlace;
import ar.edu.utn.odontologia.agenda.TokenCancelacionInvalidoException;
import java.time.Clock;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrato de cancelacion del paciente por enlace (C-18).
 *
 * GET es no mutante: solo valida o presenta la confirmacion sin consumir el token ni cancelar el turno.
 * POST ejecuta la cancelacion atomica tras una accion explicita del paciente.
 * Todas las respuestas de falla son genericas y no revelan existencia, estado ni datos del turno.
 */
@RestController
@RequestMapping("/cancelacion")
public class CancelacionPorEnlaceController {
    private final ServicioAgenda agenda;
    private final ServicioCancelacionPorEnlace cancelacion;
    private final Clock reloj;

    public CancelacionPorEnlaceController(ServicioAgenda agenda,
                                          ServicioCancelacionPorEnlace cancelacion,
                                          Clock reloj) {
        this.agenda = agenda;
        this.cancelacion = cancelacion;
        this.reloj = reloj;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, String>> validar(@PathVariable String token) {
        if (token == null || token.isBlank() || !cancelacion.esValido(token, reloj.instant())) {
            throw new TokenCancelacionInvalidoException();
        }
        return ResponseEntity.ok(Map.of("codigo", "cancelacion_disponible",
                "mensaje", "El enlace de cancelacion esta disponible"));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> cancelar(@PathVariable String token) {
        agenda.cancelarPorToken(token);
        return ResponseEntity.ok(Map.of("codigo", "turno_cancelado", "mensaje", "Turno cancelado"));
    }
}
