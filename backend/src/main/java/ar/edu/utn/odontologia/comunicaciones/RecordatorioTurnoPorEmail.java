package ar.edu.utn.odontologia.comunicaciones;

import ar.edu.utn.odontologia.agenda.ConsultaRecordatorios;
import ar.edu.utn.odontologia.agenda.TurnoPorRecordar;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Recuerda una sola vez cada horario próximo. El registro solo identifica el turno y el tipo de error: ni
 * dirección, ni nombre, ni mensaje.
 */
@Component
public class RecordatorioTurnoPorEmail {
    private static final Logger LOG = LoggerFactory.getLogger(RecordatorioTurnoPorEmail.class);
    private static final String PREFIJO_TIPO = "turno-recordatorio-";

    private final ConsultaRecordatorios recordatorios;
    private final EmailsEnviados envios;
    private final PacienteReferencias pacientes;
    private final CorreoSalida correo;
    private volatile boolean cerrando;

    public RecordatorioTurnoPorEmail(ConsultaRecordatorios recordatorios, EmailsEnviados envios,
                                     PacienteReferencias pacientes, CorreoSalida correo) {
        this.recordatorios = recordatorios;
        this.envios = envios;
        this.pacientes = pacientes;
        this.correo = correo;
    }

    public void enviarPendientes() {
        var candidatos = recordatorios.porRecordar();
        if (candidatos.isEmpty()) {
            return;
        }
        var intentados = intentados(candidatos);
        for (var candidato : candidatos) {
            // Un envío iniciado durante el cierre quedaría fallido y no se repetiría.
            if (cerrando || Thread.currentThread().isInterrupted()) {
                return;
            }
            if (!intentados.contains(new Intento(candidato.turnoId(), tipo(candidato)))) {
                enviar(candidato);
            }
        }
    }

    /** Los que no se hayan iniciado quedan para la próxima ejecución de la aplicación. */
    @EventListener(ContextClosedEvent.class)
    public void alCerrar() {
        cerrando = true;
    }

    /** Un registro, exitoso o no y con cualquier destinatario, agota el único intento de ese horario. */
    private Set<Intento> intentados(List<TurnoPorRecordar> candidatos) {
        var tipos = candidatos.stream().map(RecordatorioTurnoPorEmail::tipo).toList();
        var turnos = candidatos.stream().map(TurnoPorRecordar::turnoId).toList();
        return envios.findByTipoInAndTurnoIdIn(tipos, turnos).stream()
                .map(envio -> new Intento(envio.turnoId(), envio.tipo()))
                .collect(Collectors.toSet());
    }

    private void enviar(TurnoPorRecordar candidato) {
        try {
            // La vigencia se comprueba lo más cerca posible del envío.
            var paciente = recordatorios.pacienteSiSigueVigente(candidato).flatMap(pacientes::findById);
            if (paciente.isEmpty()) {
                return;
            }
            var mensaje = MensajeTurno.recordatorio(paciente.get().getNombre(), candidato.inicio());
            var resultado = correo.enviar(new SolicitudEnvio(tipo(candidato), candidato.turnoId(),
                    paciente.get().getEmail(), mensaje.asunto(), mensaje.cuerpo()));
            if (!resultado.exitoso()) {
                LOG.warn("Envío fallido del recordatorio del turno {}: {}", candidato.turnoId(), resultado.error());
            }
        } catch (RuntimeException error) {
            LOG.warn("No se pudo enviar el recordatorio del turno {}: {}", candidato.turnoId(),
                    error.getClass().getSimpleName());
        }
    }

    /** Usa el inicio completo y no la versión: confirmar la incrementa sin cambiar el horario. */
    private static String tipo(TurnoPorRecordar candidato) {
        return PREFIJO_TIPO + candidato.inicio();
    }

    private record Intento(UUID turnoId, String tipo) {}
}
