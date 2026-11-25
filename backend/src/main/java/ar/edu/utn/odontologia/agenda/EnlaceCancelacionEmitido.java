package ar.edu.utn.odontologia.agenda;

import java.util.UUID;

/**
 * Token de cancelación en claro, publicado al emitirlo. Debe consumirse con
 * {@code @TransactionalEventListener} después del commit: un {@code @EventListener} lo recibiría aunque la
 * transacción revierta. No se persiste ni se registra: como Spring registra las excepciones del oyente, sus
 * errores no deben incluir el token ni la URL que lo contiene.
 */
public record EnlaceCancelacionEmitido(UUID turnoId, String token) {
    @Override
    public String toString() {
        return "EnlaceCancelacionEmitido[turnoId=" + turnoId + ", token=oculto]";
    }
}
