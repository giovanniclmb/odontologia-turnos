package ar.edu.utn.odontologia.agenda;

import ar.edu.utn.odontologia.agenda.persistencia.EdicionesTurnoPersistidas;
import ar.edu.utn.odontologia.agenda.persistencia.TokenCancelacionLeido;
import ar.edu.utn.odontologia.agenda.persistencia.TokensCancelacionPersistidos;
import ar.edu.utn.odontologia.agenda.bloqueos.LocksRecursosAgenda;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioCancelacionPorEnlace {
    private static final Duration VENTANA = Duration.ofHours(48);
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final TokensCancelacionPersistidos tokens;
    private final EdicionesTurnoPersistidas ediciones;
    private final LocksRecursosAgenda locks;
    private final Clock reloj;

    public ServicioCancelacionPorEnlace(TokensCancelacionPersistidos tokens, EdicionesTurnoPersistidas ediciones,
                                       LocksRecursosAgenda locks, Clock reloj) {
        this.tokens = tokens;
        this.ediciones = ediciones;
        this.locks = locks;
        this.reloj = reloj;
    }

    /**
     * Emite un token solo si el inicio es estrictamente posterior a ahora + 48h.
     * Invalida los tokens previos del turno antes de emitir.
     * Devuelve el token en claro para que C-19 lo envíe; null si la ventana está cerrada.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String emitir(UUID turnoId, long version, Instant inicio, Instant ahora) {
        if (turnoId == null || inicio == null || ahora == null) {
            throw new IllegalArgumentException("Turno, versión e inicio son obligatorios");
        }
        if (!inicio.isAfter(ahora.plus(VENTANA))) {
            tokens.eliminarPorTurno(turnoId);
            return null;
        }
        tokens.eliminarPorTurno(turnoId);
        String crudo = generar();
        tokens.guardar(turnoId, hash(crudo), version, ahora);
        return crudo;
    }

    public void invalidar(UUID turnoId) {
        tokens.eliminarPorTurno(turnoId);
    }

    /** Mantiene vigente el enlace al confirmar. Exige la transacción que ya bloqueó la fila del turno. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void conservarTrasConfirmacion(UUID turnoId, long versionAnterior, long versionConfirmada) {
        tokens.actualizarVersion(turnoId, versionAnterior, versionConfirmada);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean esValido(String tokenCrudo, Instant ahora) {
        if (tokenCrudo == null || tokenCrudo.isBlank() || ahora == null) {
            return false;
        }
        var hashToken = hash(tokenCrudo);
        var registro = tokens.buscarPorHash(hashToken);
        if (registro.isEmpty()) {
            return false;
        }
        var turno = ediciones.bloquear(registro.get().turnoId(), null);
        if (turno.isEmpty()) {
            return false;
        }
        var t = turno.get();
        // El token se vuelve a leer: una confirmación pudo actualizar su versión durante la espera.
        var vigente = tokens.buscarPorHash(hashToken)
                .filter(r -> !r.consumido() && r.version() == t.version());
        if (vigente.isEmpty()) {
            return false;
        }
        if (t.estado() != EstadoTurno.PENDIENTE && t.estado() != EstadoTurno.CONFIRMADO) {
            return false;
        }
        if (!t.inicio().isAfter(ahora)) {
            return false;
        }
        // corte inclusivo: ahora <= inicio - 48h
        if (ahora.isAfter(t.inicio().minus(VENTANA))) {
            return false;
        }
        return true;
    }

    /**
     * Respeta el orden de locks de la agenda: fila del turno, recursos y, al final, token. Invertirlo
     * permite un ciclo de espera con la reprogramación.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ServicioAgenda.TurnoRegistrado cancelar(String tokenCrudo) {
        if (tokenCrudo == null || tokenCrudo.isBlank()) {
            throw new TokenCancelacionInvalidoException();
        }
        var hashToken = hash(tokenCrudo);
        // Lectura sin lock para identificar el turno; el token se revalida bajo los locks. Un enlace usado
        // o un turno cancelado no vuelven a servir: se rechazan sin retener los recursos.
        var turnoId = tokens.buscarPorHash(hashToken)
                .filter(r -> !r.consumido())
                .orElseThrow(TokenCancelacionInvalidoException::new).turnoId();
        var turno = ediciones.bloquear(turnoId, null)
                .filter(t -> t.estado() != EstadoTurno.CANCELADO)
                .orElseThrow(TokenCancelacionInvalidoException::new);
        locks.adquirir(List.of(new RecursosBloqueo(turno.profesionalId(), turno.sillonId())));
        var registro = tokens.bloquearPorHash(hashToken)
                .filter(r -> !r.consumido() && r.turnoId().equals(turno.id()) && r.version() == turno.version())
                .orElseThrow(TokenCancelacionInvalidoException::new);
        // El plazo se evalúa después de las esperas, inmediatamente antes de modificar datos.
        Instant ahora = reloj.instant();
        if (turno.estado() != EstadoTurno.PENDIENTE && turno.estado() != EstadoTurno.CONFIRMADO) {
            throw new TokenCancelacionInvalidoException();
        }
        if (!turno.inicio().isAfter(ahora)) {
            throw new TokenCancelacionInvalidoException();
        }
        if (ahora.isAfter(turno.inicio().minus(VENTANA))) {
            throw new TokenCancelacionInvalidoException();
        }

        var cancelado = ediciones.cambiarEstado(turno.id(), turno.version(), turno.estado(), EstadoTurno.CANCELADO)
                .orElseThrow(TokenCancelacionInvalidoException::new);

        int consumidos = tokens.consumir(registro.id(), ahora);
        if (consumidos != 1) {
            throw new TokenCancelacionInvalidoException();
        }

        return new ServicioAgenda.TurnoRegistrado(cancelado.id(), cancelado.pacienteId(), cancelado.profesionalId(),
                cancelado.sillonId(), cancelado.prestacionId(), cancelado.inicio(), cancelado.fin(),
                cancelado.duracionMinutos(), cancelado.estado(), Long.toString(cancelado.version()));
    }

    private static String generar() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String crudo) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(crudo.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}