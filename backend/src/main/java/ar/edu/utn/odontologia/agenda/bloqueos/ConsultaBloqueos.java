package ar.edu.utn.odontologia.agenda.bloqueos;

import ar.edu.utn.odontologia.agenda.bloqueos.persistencia.BloqueoPersistido;
import ar.edu.utn.odontologia.agenda.bloqueos.persistencia.BloqueosPersistidos;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaBloqueos {
    private final BloqueosPersistidos bloqueos;

    public ConsultaBloqueos(BloqueosPersistidos bloqueos) {
        this.bloqueos = bloqueos;
    }

    @Transactional(readOnly = true)
    public List<Bloqueo> buscar(RecursosBloqueo recursos, PeriodoBloqueo periodo) {
        if (recursos == null || periodo == null) {
            throw new IllegalArgumentException("Recursos y período son obligatorios");
        }
        return bloqueos.buscar(recursos.profesionalId(), recursos.sillonId(), periodo.inicio(), periodo.fin())
                .stream().map(BloqueoPersistido::aBloqueo).toList();
    }
}
