package ar.edu.utn.odontologia.agenda;

import java.time.Instant;
import java.util.UUID;

/**
 * Alta o reprogramación de un turno. Debe consumirse con {@code @TransactionalEventListener} después del commit:
 * un {@code @EventListener} lo recibiría aunque la transacción revierta. {@code token} es el enlace de
 * cancelación en claro, o nulo con la ventana cerrada. No se persiste ni se registra: como los errores de un
 * oyente se registran, no deben incluirlo.
 */
public record TurnoProgramado(UUID turnoId, long version, Instant inicio, Motivo motivo, String token) {
    public enum Motivo { ALTA, REPROGRAMACION }

    @Override
    public String toString() {
        return "TurnoProgramado[turnoId=" + turnoId + ", version=" + version + ", motivo=" + motivo
                + ", token=" + (token == null ? "sin enlace" : "oculto") + "]";
    }
}
