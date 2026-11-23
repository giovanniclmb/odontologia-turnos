package ar.edu.utn.odontologia.agenda.bloqueos;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocksRecursosAgenda {
    private final JdbcTemplate jdbc;

    public LocksRecursosAgenda(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Todos los recursos de origen y destino deben adquirirse juntos antes de consultar conflictos. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void adquirir(Collection<RecursosBloqueo> selecciones) {
        if (!"read committed".equals(jdbc.queryForObject("show transaction_isolation", String.class))) {
            throw new IllegalStateException("La escritura de agenda requiere READ COMMITTED");
        }
        adquirir(selecciones, RecursosBloqueo::profesionalId, """
                select id from odontologia.profesional where id = ? for no key update
                """, "profesional");
        adquirir(selecciones, RecursosBloqueo::sillonId, """
                select id from odontologia.sillon where id = ? for no key update
                """, "sillón");
    }

    private void adquirir(Collection<RecursosBloqueo> selecciones, Function<RecursosBloqueo, UUID> referencia,
                          String consulta, String recurso) {
        var ids = selecciones.stream().map(referencia).filter(Objects::nonNull).distinct().sorted().toList();
        for (UUID id : ids) {
            if (jdbc.queryForList(consulta, UUID.class, id).isEmpty()) {
                throw new IllegalArgumentException("La referencia de " + recurso + " no existe");
            }
        }
    }
}
