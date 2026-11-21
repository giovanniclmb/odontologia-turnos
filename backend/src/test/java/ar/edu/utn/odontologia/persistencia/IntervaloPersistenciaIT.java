package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class IntervaloPersistenciaIT {
    @Test
    void conservaUnaUnicaConfiguracionInclusoConEscriturasConcurrentes() throws Exception {
        try (var db = PostgresFixture.start()) {
            Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false).load().migrate();
            try (var con = db.appConnection(); var sql = con.createStatement();
                 var filas = sql.executeQuery("select intervalo_minutos from odontologia.configuracion_sede")) {
                assertTrue(filas.next());
                assertNull(filas.getObject(1));
                assertFalse(filas.next());
                for (int invalido : new int[]{0, -1}) {
                    var error = assertThrows(SQLException.class, () -> sql.executeUpdate(
                            "update odontologia.configuracion_sede set intervalo_minutos=" + invalido));
                    assertEquals("23514", error.getSQLState());
                }
            }
            try (var con = db.migrationConnection(); var sql = con.createStatement()) {
                var error = assertThrows(SQLException.class, () -> sql.executeUpdate(
                        "insert into odontologia.configuracion_sede (fila) values (2)"));
                assertEquals("23514", error.getSQLState());
            }

            var inicio = new CyclicBarrier(2);
            try (var ejecutor = Executors.newFixedThreadPool(2)) {
                var primero = ejecutor.submit(() -> ajustar(db, inicio, 10));
                var segundo = ejecutor.submit(() -> ajustar(db, inicio, 20));
                assertEquals(1, primero.get(10, TimeUnit.SECONDS));
                assertEquals(1, segundo.get(10, TimeUnit.SECONDS));
            }
            try (var con = db.appConnection(); var sql = con.createStatement();
                 var filas = sql.executeQuery("select intervalo_minutos from odontologia.configuracion_sede")) {
                assertTrue(filas.next());
                assertTrue(Set.of(10, 20).contains(filas.getInt(1)));
                assertFalse(filas.next());
            }
        }
    }

    private static int ajustar(PostgresFixture db, CyclicBarrier inicio, int minutos) throws Exception {
        try (var con = db.appConnection(); var sql = con.prepareStatement(
                "update odontologia.configuracion_sede set intervalo_minutos=? where fila=1")) {
            sql.setQueryTimeout(5);
            sql.setInt(1, minutos);
            inicio.await(5, TimeUnit.SECONDS);
            return sql.executeUpdate();
        }
    }
}
