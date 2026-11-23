package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.agenda.bloqueos.ConsultaBloqueos;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

class ConsultaBloqueosIT {
    private static final Instant INICIO = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant FIN = INICIO.plusSeconds(3600);

    @Test
    void consultaCualquieraDeLosRecursosYRespetaAmbosBordes() throws Exception {
        try (var db = PostgresFixture.start();
             var contexto = new SpringApplicationBuilder(OdontologiaApplication.class)
                     .initializers(ctx -> ctx.getEnvironment().getPropertySources().addFirst(
                             new org.springframework.core.env.MapPropertySource("postgres-fixture", db.properties())))
                     .run()) {
            UUID profesional = UUID.randomUUID();
            UUID sillon = UUID.randomUUID();
            UUID bloqueo = UUID.randomUUID();
            try (var con = db.appConnection(); var sql = con.createStatement()) {
                sql.executeUpdate("insert into odontologia.profesional(id,codigo) values ('%s','PRO-FICTICIO')"
                        .formatted(profesional));
                sql.executeUpdate("insert into odontologia.sillon(id,codigo) values ('%s','SILLON-FICTICIO')"
                        .formatted(sillon));
                sql.executeUpdate("""
                        insert into odontologia.bloqueo_recurso(id, profesional_id, sillon_id, inicio, fin)
                        values ('%s', '%s', '%s', '%s', '%s')
                        """.formatted(bloqueo, profesional, sillon, INICIO, FIN));
            }

            var consulta = contexto.getBean(ConsultaBloqueos.class);
            var periodo = new PeriodoBloqueo(INICIO, FIN);
            for (var recursos : List.of(new RecursosBloqueo(profesional, null),
                    new RecursosBloqueo(null, sillon), new RecursosBloqueo(UUID.randomUUID(), sillon))) {
                var coincidencias = consulta.buscar(recursos, periodo);
                assertEquals(1, coincidencias.size());
                assertEquals(bloqueo, coincidencias.getFirst().id());
                assertEquals(periodo, coincidencias.getFirst().periodo());
                assertEquals(new RecursosBloqueo(profesional, sillon), coincidencias.getFirst().recursos());
            }
            var recursos = new RecursosBloqueo(profesional, sillon);
            assertTrue(consulta.buscar(recursos, new PeriodoBloqueo(INICIO.minusSeconds(1), INICIO)).isEmpty());
            assertTrue(consulta.buscar(recursos, new PeriodoBloqueo(FIN, FIN.plusSeconds(1))).isEmpty());
            assertTrue(consulta.buscar(new RecursosBloqueo(UUID.randomUUID(), null), periodo).isEmpty());
        }
    }
}
