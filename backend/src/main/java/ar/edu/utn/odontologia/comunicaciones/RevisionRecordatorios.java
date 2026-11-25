package ar.edu.utn.odontologia.comunicaciones;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Revisa los recordatorios al arrancar y un minuto después de terminar cada revisión. Solo existe en la
 * aplicación web, para que un proceso de línea de comandos no envíe correos, y solo si se activa.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "email.recordatorio.activo", havingValue = "true")
class RevisionRecordatorios {
    private static final Logger LOG = LoggerFactory.getLogger(RevisionRecordatorios.class);

    private final RecordatorioTurnoPorEmail recordatorio;

    RevisionRecordatorios(RecordatorioTurnoPorEmail recordatorio) {
        this.recordatorio = recordatorio;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    void revisar() {
        try {
            recordatorio.enviarPendientes();
        } catch (RuntimeException error) {
            LOG.warn("No se pudieron revisar los recordatorios: {}", error.getClass().getSimpleName());
        }
    }
}
