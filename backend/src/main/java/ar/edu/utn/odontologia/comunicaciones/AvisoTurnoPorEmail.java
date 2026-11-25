package ar.edu.utn.odontologia.comunicaciones;

import ar.edu.utn.odontologia.agenda.ConsultaAvisoTurno;
import ar.edu.utn.odontologia.agenda.TurnoProgramado;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Informa al paciente cada alta o reprogramación confirmada. Ningún fallo llega a la agenda, y el registro solo
 * identifica el turno y el tipo de error: ni dirección, ni mensaje, ni enlace.
 */
@Component
public class AvisoTurnoPorEmail {
    private static final Logger LOG = LoggerFactory.getLogger(AvisoTurnoPorEmail.class);

    private final ConsultaAvisoTurno avisos;
    private final PacienteReferencias pacientes;
    private final CorreoSalida correo;
    private final ConfiguracionEmail configuracion;

    public AvisoTurnoPorEmail(ConsultaAvisoTurno avisos, PacienteReferencias pacientes, CorreoSalida correo,
                              ConfiguracionEmail configuracion) {
        this.avisos = avisos;
        this.pacientes = pacientes;
        this.correo = correo;
        this.configuracion = configuracion;
    }

    @Async(EnvioEmailAsincronoConfig.EJECUTOR)
    @TransactionalEventListener
    public void informar(TurnoProgramado aviso) {
        try {
            enviarSiSigueVigente(aviso);
        } catch (RuntimeException error) {
            LOG.warn("No se pudo enviar el email del turno {}: {}", aviso.turnoId(),
                    error.getClass().getSimpleName());
        }
    }

    private void enviarSiSigueVigente(TurnoProgramado aviso) {
        String enlace = null;
        if (aviso.token() != null) {
            var formado = MensajeTurno.enlace(configuracion.cancelacionUrlBase(), aviso.token());
            if (formado.isEmpty()) {
                LOG.warn("No se envió el email del turno {}: EMAIL_CANCELACION_URL_BASE falta o no es una URL web",
                        aviso.turnoId());
                return;
            }
            enlace = formado.get();
        }
        // La vigencia se comprueba lo más cerca posible del envío.
        var paciente = avisos.pacienteSiSigueVigente(aviso).flatMap(pacientes::findById);
        if (paciente.isEmpty()) {
            return;
        }
        var mensaje = MensajeTurno.componer(paciente.get().getNombre(), aviso.inicio(), aviso.motivo(), enlace);
        var resultado = correo.enviar(new SolicitudEnvio(tipo(aviso), aviso.turnoId(), paciente.get().getEmail(),
                mensaje.asunto(), mensaje.cuerpo()));
        if (!resultado.exitoso()) {
            // El motivo del transporte no lleva dirección, mensaje ni respuesta del proveedor.
            LOG.warn("Envío fallido del turno {}: {}", aviso.turnoId(), resultado.error());
        }
    }

    /** Cada reprogramación tiene su tipo: la deduplicación del transporte no debe bloquear la siguiente. */
    private static String tipo(TurnoProgramado aviso) {
        return switch (aviso.motivo()) {
            case ALTA -> "turno-alta";
            case REPROGRAMACION -> "turno-reprogramacion-v" + aviso.version();
        };
    }
}
