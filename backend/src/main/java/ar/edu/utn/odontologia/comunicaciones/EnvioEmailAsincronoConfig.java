package ar.edu.utn.odontologia.comunicaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class EnvioEmailAsincronoConfig {
    public static final String EJECUTOR = "ejecutorEmail";
    private static final int AVISOS_EN_ESPERA = 100;
    private static final int SEGUNDOS_AL_CERRAR = 10;
    private static final Logger LOG = LoggerFactory.getLogger(EnvioEmailAsincronoConfig.class);

    /**
     * Un solo hilo limita las conexiones que el envío toma del pool y conserva el orden. Con la cola llena se
     * descarta el aviso en lugar de bloquear la operación de agenda.
     */
    @Bean(EJECUTOR)
    ThreadPoolTaskExecutor ejecutorEmail() {
        var ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(1);
        ejecutor.setMaxPoolSize(1);
        ejecutor.setQueueCapacity(AVISOS_EN_ESPERA);
        ejecutor.setThreadNamePrefix("email-");
        ejecutor.setWaitForTasksToCompleteOnShutdown(true);
        ejecutor.setAwaitTerminationSeconds(SEGUNDOS_AL_CERRAR);
        ejecutor.setRejectedExecutionHandler((tarea, pool) ->
                LOG.warn("Aviso de turno descartado: la cola de envío de email no admite más tareas"));
        return ejecutor;
    }
}
