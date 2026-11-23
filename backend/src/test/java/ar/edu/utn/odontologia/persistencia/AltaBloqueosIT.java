package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.bloqueos.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

class AltaBloqueosIT {
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION);
    private static final PeriodoBloqueo PERIODO = new PeriodoBloqueo(
            Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"));

    @Test
    void validaElAltaYSerializaAltaYReprogramacionContraBloqueos() throws Exception {
        try (var db = PostgresFixture.start();
             var contexto = new SpringApplicationBuilder(OdontologiaApplication.class)
                     .initializers(ctx -> {
                         ctx.getEnvironment().getPropertySources()
                                 .addFirst(new MapPropertySource("postgres-fixture", db.properties()));
                         var registro = (GenericApplicationContext) ctx;
                         registro.registerBean("turnosFixture", ConsultaTurnosOcupantes.class,
                                 () -> turnosFixture(ctx.getBean(JdbcTemplate.class)),
                                 definicion -> definicion.setPrimary(true));
                     }).run();
             var executor = Executors.newSingleThreadExecutor()) {
            try (var con = db.adminConnection(); var sql = con.createStatement()) {
                sql.execute("""
                        create table odontologia.turno_fixture (
                            id uuid primary key, profesional_id uuid, sillon_id uuid,
                            inicio timestamptz not null, fin timestamptz not null
                        )
                        """);
                sql.execute("grant select, insert, update on odontologia.turno_fixture to odonto_app");
            }
            var jdbc = contexto.getBean(JdbcTemplate.class);
            var tx = contexto.getBean(TransactionTemplate.class);
            var locks = contexto.getBean(LocksRecursosAgenda.class);
            var alta = contexto.getBean(ServicioBloqueos.class);
            var consulta = contexto.getBean(ConsultaBloqueos.class);
            var origen = recursos(jdbc, "ORIGEN");
            var destino = recursos(jdbc, "DESTINO");
            var altaBloqueada = recursos(jdbc, "ALTA-BLOQUEADA");
            var rollback = recursos(jdbc, "ROLLBACK");
            var libres = recursos(jdbc, "LIBRES");

            assertThrows(AutorizacionDenegadaException.class,
                    () -> alta.crear(identidad(Rol.ODONTOLOGO), origen, PERIODO));
            assertThrows(IllegalArgumentException.class, () -> alta.crear(RECEPCION, null, PERIODO));
            assertThrows(IllegalArgumentException.class, () -> alta.crear(RECEPCION, origen, null));
            assertThrows(IllegalArgumentException.class, () -> new RecursosBloqueo(null, null));
            assertThrows(IllegalArgumentException.class, () -> new PeriodoBloqueo(PERIODO.fin(), PERIODO.inicio()));
            assertThrows(IllegalArgumentException.class,
                    () -> alta.crear(RECEPCION, new RecursosBloqueo(origen.profesionalId(), UUID.randomUUID()), PERIODO));
            assertTrue(consulta.buscar(origen, PERIODO).isEmpty());

            for (var seleccion : List.of(new RecursosBloqueo(libres.profesionalId(), null),
                    new RecursosBloqueo(null, libres.sillonId()), libres)) {
                assertEquals(seleccion, alta.crear(RECEPCION, seleccion, PERIODO).recursos());
            }
            assertEquals(3, consulta.buscar(libres, PERIODO).size());

            transacciones(executor, tx, () -> alta.crear(RECEPCION, altaBloqueada, PERIODO),
                    () -> assertEquals(false, tx.execute(ignorado -> {
                        locks.adquirir(List.of(altaBloqueada));
                        if (!consulta.buscar(altaBloqueada, PERIODO).isEmpty()) {
                            return false;
                        }
                        insertarTurno(jdbc, UUID.randomUUID(), altaBloqueada);
                        return true;
                    })), false);

            UUID turno = UUID.randomUUID();
            transacciones(executor, tx, () -> {
                locks.adquirir(List.of(origen));
                insertarTurno(jdbc, turno, origen);
            }, () -> assertThrows(TurnoOcupanteException.class, () -> alta.crear(RECEPCION, origen, PERIODO)), false);
            assertTrue(consulta.buscar(origen, PERIODO).isEmpty());

            transacciones(executor, tx, () -> alta.crear(RECEPCION, destino, PERIODO),
                    () -> assertEquals(false, tx.execute(ignorado -> {
                        locks.adquirir(List.of(origen, destino));
                        if (!consulta.buscar(destino, PERIODO).isEmpty()) {
                            return false;
                        }
                        jdbc.update("update odontologia.turno_fixture set profesional_id=?, sillon_id=? where id=?",
                                destino.profesionalId(), destino.sillonId(), turno);
                        return true;
                    })), false);
            assertEquals(origen.sillonId(), jdbc.queryForObject(
                    "select sillon_id from odontologia.turno_fixture where id=?", UUID.class, turno));

            transacciones(executor, tx, () -> alta.crear(RECEPCION, rollback, PERIODO),
                    () -> tx.executeWithoutResult(ignorado -> {
                        locks.adquirir(List.of(rollback));
                        assertTrue(consulta.buscar(rollback, PERIODO).isEmpty());
                        insertarTurno(jdbc, UUID.randomUUID(), rollback);
                    }), true);
            assertTrue(consulta.buscar(rollback, PERIODO).isEmpty());
        }
    }

    private static void transacciones(ExecutorService executor, TransactionTemplate tx, Runnable primera,
                                      Runnable segunda, boolean revertir) throws Exception {
        var inicio = new CountDownLatch(1);
        var futuro = tx.execute(estado -> {
            primera.run();
            var pendiente = executor.submit((Callable<Void>) () -> {
                inicio.countDown();
                segunda.run();
                return null;
            });
            assertDoesNotThrow(() -> assertTrue(inicio.await(5, TimeUnit.SECONDS)));
            assertThrows(TimeoutException.class, () -> pendiente.get(300, TimeUnit.MILLISECONDS));
            if (revertir) {
                estado.setRollbackOnly();
            }
            return pendiente;
        });
        futuro.get(10, TimeUnit.SECONDS);
    }

    private static Identidad identidad(Rol rol) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio", rol, null);
    }

    private static RecursosBloqueo recursos(JdbcTemplate jdbc, String codigo) {
        UUID profesional = UUID.randomUUID();
        UUID sillon = UUID.randomUUID();
        jdbc.update("insert into odontologia.profesional(id,codigo) values (?,?)", profesional, codigo);
        jdbc.update("insert into odontologia.sillon(id,codigo) values (?,?)", sillon, codigo);
        return new RecursosBloqueo(profesional, sillon);
    }

    private static void insertarTurno(JdbcTemplate jdbc, UUID turno, RecursosBloqueo recursos) {
        jdbc.update("insert into odontologia.turno_fixture values (?, ?, ?, ?, ?)", turno,
                recursos.profesionalId(), recursos.sillonId(),
                PERIODO.inicio().atOffset(ZoneOffset.UTC), PERIODO.fin().atOffset(ZoneOffset.UTC));
    }

    private static ConsultaTurnosOcupantes turnosFixture(JdbcTemplate jdbc) {
        return (recursos, periodo) -> Boolean.TRUE.equals(jdbc.queryForObject("""
                select exists (
                    select 1 from odontologia.turno_fixture
                    where (profesional_id = ? or sillon_id = ?) and inicio < ? and fin > ?
                )
                """, Boolean.class, recursos.profesionalId(), recursos.sillonId(),
                periodo.fin().atOffset(ZoneOffset.UTC), periodo.inicio().atOffset(ZoneOffset.UTC)));
    }
}
