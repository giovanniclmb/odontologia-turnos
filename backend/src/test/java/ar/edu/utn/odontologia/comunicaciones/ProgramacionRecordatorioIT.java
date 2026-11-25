package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.TaskExecutionOutcome;

/** La revisión periódica solo existe en la aplicación web y cuando se la activa. */
class ProgramacionRecordatorioIT {
    /** Por la clave de la propiedad: la variable que fija Maven en las pruebas no debe ocultarla. */
    private static final Map<String, Object> ACTIVADA = Map.of("email.recordatorio.activo", "true");
    private static PostgresFixture db;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
    }

    @AfterAll
    static void cerrar() {
        if (db != null) db.close();
    }

    @Test
    void porDefectoNoProgramaNingunaRevision() {
        try (var contexto = aplicacion(WebApplicationType.SERVLET, Map.of())) {
            assertEquals("false", contexto.getEnvironment().getProperty("email.recordatorio.activo"));
            assertTrue(tareas(contexto).isEmpty());
        }
    }

    @Test
    void activadaRevisaAlArrancarYVuelveAHacerloUnMinutoDespuesDeTerminar() throws Exception {
        try (var contexto = aplicacion(WebApplicationType.SERVLET, ACTIVADA)) {
            var tareas = tareas(contexto);
            assertEquals(1, tareas.size());
            var tarea = assertInstanceOf(FixedDelayTask.class, tareas.getFirst().getTask());
            assertEquals(Duration.ofMinutes(1), tarea.getIntervalDuration());
            assertEquals(Duration.ZERO, tarea.getInitialDelayDuration());

            var limite = Instant.now().plusSeconds(10);
            while (tarea.getLastExecutionOutcome().status() != TaskExecutionOutcome.Status.SUCCESS
                    && Instant.now().isBefore(limite)) {
                Thread.sleep(20);
            }

            assertEquals(TaskExecutionOutcome.Status.SUCCESS, tarea.getLastExecutionOutcome().status());
        }
    }

    @Test
    void elProcesoNoWebNoProgramaRevisionesAunqueEsteActivada() {
        // Mismo argumento que usa el CLI de bootstrap: la configuración declara una aplicación web.
        try (var contexto = aplicacion(WebApplicationType.NONE, ACTIVADA, "--spring.main.web-application-type=none")) {
            assertTrue(tareas(contexto).isEmpty());
        }
    }

    private static ConfigurableApplicationContext aplicacion(WebApplicationType tipo, Map<String, Object> extra,
                                                             String... argumentos) {
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(tipo).initializers(contexto -> {
            var fuentes = contexto.getEnvironment().getPropertySources();
            fuentes.addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            fuentes.addFirst(new MapPropertySource("prueba", extra));
        }).properties("server.port=0").run(argumentos);
    }

    private static List<ScheduledTask> tareas(ConfigurableApplicationContext contexto) {
        return contexto.getBeansOfType(ScheduledTaskHolder.class).values().stream()
                .flatMap(holder -> holder.getScheduledTasks().stream())
                .distinct().toList();
    }
}
