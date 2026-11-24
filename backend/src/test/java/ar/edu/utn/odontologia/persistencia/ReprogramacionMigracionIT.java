package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class ReprogramacionMigracionIT {
    private static final OffsetDateTime INICIO = OffsetDateTime.parse("2032-01-01T10:00:00.123456Z");

    @Test
    void versionaTurnosV7YConcedeSoloEscriturasNecesarias() throws SQLException {
        try (var db = PostgresFixture.start()) {
            var migrador = Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                    .schemas("odontologia").createSchemas(false);
            migrador.target("7").load().migrate();
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                var jdbc = new JdbcTemplate(new SingleConnectionDataSource(con, true));
                var turno = UUID.randomUUID();
                var paciente = UUID.randomUUID();
                var profesional = UUID.randomUUID();
                var sillon = UUID.randomUUID();
                var prestacion = UUID.randomUUID();
                jdbc.update("insert into odontologia.paciente(id,codigo,nombre,apellido,email) values (?,'P','Ana','Ejemplo','a@example.invalid')", paciente);
                jdbc.update("insert into odontologia.profesional(id,codigo) values (?,'PRO')", profesional);
                jdbc.update("insert into odontologia.sillon(id,codigo) values (?,'S')", sillon);
                jdbc.update("insert into odontologia.prestacion(id,codigo) values (?,'PRE')", prestacion);
                insertar(jdbc, turno, paciente, profesional, sillon, prestacion, INICIO);

                assertEquals(1, migrador.target("8").load().migrate().migrationsExecuted);
                try (var fila = sql.executeQuery("select * from odontologia.turno")) {
                    assertTrue(fila.next());
                    assertEquals(turno, fila.getObject("id", UUID.class));
                    assertEquals(paciente, fila.getObject("paciente_id", UUID.class));
                    assertEquals(profesional, fila.getObject("profesional_id", UUID.class));
                    assertEquals(sillon, fila.getObject("sillon_id", UUID.class));
                    assertEquals(prestacion, fila.getObject("prestacion_id", UUID.class));
                    assertEquals(INICIO, fila.getObject("inicio", OffsetDateTime.class));
                    assertEquals(INICIO.plusMinutes(30), fila.getObject("fin", OffsetDateTime.class));
                    assertEquals(30, fila.getInt("duracion_minutos"));
                    assertEquals("PENDIENTE", fila.getString("estado"));
                    assertEquals(0L, fila.getLong("version"));
                    assertFalse(fila.next());
                }
                assertEquals(1, jdbc.update("""
                        update odontologia.turno set profesional_id=?, sillon_id=?, inicio=?, fin=?, version=version+1
                        where id=? and version=0
                        """, profesional, sillon, INICIO.plusHours(2), INICIO.plusHours(2).plusMinutes(30), turno));
                var nuevo = UUID.randomUUID();
                insertar(jdbc, nuevo, paciente, profesional, sillon, prestacion, INICIO.plusHours(4));
                assertEquals(0L, jdbc.queryForObject("select version from odontologia.turno where id=?", Long.class, nuevo));
                for (String columna : List.of("id", "paciente_id", "prestacion_id", "duracion_minutos", "estado")) {
                    assertEquals("42501", assertThrows(SQLException.class, () -> sql.executeUpdate(
                            "update odontologia.turno set " + columna + " = " + columna)).getSQLState(), columna);
                }
                assertEquals("42501", assertThrows(SQLException.class,
                        () -> sql.executeUpdate("delete from odontologia.turno")).getSQLState());

                // Los privilegios anteriores se comprueban en V8. La aplicación actual
                // necesita todas las migraciones para validar también las entidades nuevas.
                try (var app = new SpringApplicationBuilder(OdontologiaApplication.class)
                        .initializers(ctx -> ctx.getEnvironment().getPropertySources()
                                .addFirst(new MapPropertySource("postgres-fixture", db.properties()))).run()) {
                    var recepcion = new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
                    var leidos = app.getBean(ServicioAgenda.class).consultarAgendaSillon(recepcion, sillon,
                            INICIO.plusHours(2).toInstant(), INICIO.plusHours(4).plusMinutes(30).toInstant());
                    assertEquals(2, leidos.size());
                    assertEquals(turno, leidos.getFirst().id());
                    assertEquals("1", leidos.getFirst().version());
                    assertEquals(nuevo, leidos.getLast().id());
                    assertEquals("0", leidos.getLast().version());
                }
            }
        }
    }

    private static void insertar(JdbcTemplate jdbc, UUID turno, UUID paciente, UUID profesional, UUID sillon,
                                 UUID prestacion, OffsetDateTime inicio) {
        jdbc.update("""
                insert into odontologia.turno (id,paciente_id,profesional_id,sillon_id,prestacion_id,
                                               inicio,fin,duracion_minutos,estado)
                values (?,?,?,?,?,?,?,30,'PENDIENTE')
                """, turno, paciente, profesional, sillon, prestacion, inicio, inicio.plusMinutes(30));
    }
}
