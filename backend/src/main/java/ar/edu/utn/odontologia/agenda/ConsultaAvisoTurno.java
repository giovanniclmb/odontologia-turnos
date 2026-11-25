package ar.edu.utn.odontologia.agenda;

import ar.edu.utn.odontologia.agenda.persistencia.TokensCancelacionPersistidos;
import ar.edu.utn.odontologia.agenda.persistencia.TurnosPersistidos;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lectura sin locks para quien informa un {@link TurnoProgramado} después de guardado. */
@Service
public class ConsultaAvisoTurno {
    private final TurnosPersistidos turnos;
    private final TokensCancelacionPersistidos tokens;

    public ConsultaAvisoTurno(TurnosPersistidos turnos, TokensCancelacionPersistidos tokens) {
        this.turnos = turnos;
        this.tokens = tokens;
    }

    /**
     * Devuelve el paciente si el aviso todavía describe al turno: no está cancelado, conserva el inicio y, si el
     * aviso trae enlace, sigue siendo el vigente. No compara la versión porque confirmar la incrementa sin
     * cambiar lo informado.
     */
    @Transactional(readOnly = true)
    public Optional<UUID> pacienteSiSigueVigente(TurnoProgramado aviso) {
        return turnos.buscar(aviso.turnoId())
                .filter(turno -> turno.estado() != EstadoTurno.CANCELADO && turno.inicio().equals(aviso.inicio()))
                .filter(turno -> aviso.token() == null || esElEnlaceVigente(aviso))
                .map(turno -> turno.pacienteId());
    }

    private boolean esElEnlaceVigente(TurnoProgramado aviso) {
        return tokens.buscarPorHash(ServicioCancelacionPorEnlace.hash(aviso.token()))
                .filter(token -> !token.consumido() && token.turnoId().equals(aviso.turnoId()))
                .isPresent();
    }
}
