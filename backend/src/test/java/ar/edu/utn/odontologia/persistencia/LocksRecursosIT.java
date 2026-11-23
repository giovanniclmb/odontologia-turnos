package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.agenda.bloqueos.LocksRecursosAgenda;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

class LocksRecursosIT {
    @Test
    void usaLaTransaccionActualYSerializaPorRecursoConOrdenComun() throws Exception {
        try (var db = PostgresFixture.start();
             var contexto = new SpringApplicationBuilder(OdontologiaApplication.class)
                     .initializers(ctx -> ctx.getEnvironment().getPropertySources()
                             .addFirst(new MapPropertySource("postgres-fixture", db.properties()))).run();
             var executor = Executors.newSingleThreadExecutor()) {
            var jdbc = contexto.getBean(JdbcTemplate.class);
            var tx = contexto.getBean(TransactionTemplate.class);
            var locks = contexto.getBean(LocksRecursosAgenda.class);
            var profesional = new RecursosBloqueo(UUID.randomUUID(), null);
            var sillon = new RecursosBloqueo(null, UUID.randomUUID());
            jdbc.update("insert into odontologia.profesional(id,codigo) values (?, 'P-FICTICIO')",
                    profesional.profesionalId());
            jdbc.update("insert into odontologia.sillon(id,codigo) values (?, 'S-FICTICIO')", sillon.sillonId());

            assertThrows(IllegalTransactionStateException.class, () -> locks.adquirir(List.of(profesional)));
            tx.executeWithoutResult(ignorado -> {
                locks.adquirir(List.of(profesional));
                var pid = new CompletableFuture<Integer>();
                var concurrente = executor.submit(() -> {
                    tx.executeWithoutResult(estado -> {
                        locks.adquirir(List.of(sillon));
                        estado.setRollbackOnly();
                    });
                    var error = assertThrows(RuntimeException.class, () -> tx.executeWithoutResult(estado -> {
                        jdbc.execute("set local lock_timeout = '5s'");
                        pid.complete(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
                        locks.adquirir(List.of(sillon, profesional));
                    }));
                    assertEquals("55P03", ((SQLException) NestedExceptionUtils.getMostSpecificCause(error))
                            .getSQLState());
                });
                try {
                    int sesion = pid.get(5, TimeUnit.SECONDS);
                    long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                    while (!Boolean.TRUE.equals(jdbc.queryForObject(
                            "select pg_backend_pid() = any(pg_blocking_pids(?))", Boolean.class, sesion))) {
                        if (System.nanoTime() >= limite) {
                            throw new AssertionError("La segunda sesión no esperó por el profesional");
                        }
                        Thread.sleep(25);
                    }
                    jdbc.execute("set local lock_timeout = '300ms'");
                    locks.adquirir(List.of(sillon));
                    concurrente.get(10, TimeUnit.SECONDS);
                } catch (Exception error) {
                    throw new AssertionError("La transacción concurrente no terminó como se esperaba", error);
                }
            });
            tx.executeWithoutResult(ignorado -> locks.adquirir(List.of(sillon, profesional)));
        }
    }
}
