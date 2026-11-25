package ar.edu.utn.odontologia.agenda.persistencia;

import ar.edu.utn.odontologia.agenda.EstadoTurno;
import ar.edu.utn.odontologia.agenda.bloqueos.ConsultaTurnosOcupantes;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import org.springframework.stereotype.Component;

@Component
class ConsultaTurnosPersistidos implements ConsultaTurnosOcupantes {
    private final TurnosPersistidos turnos;

    ConsultaTurnosPersistidos(TurnosPersistidos turnos) {
        this.turnos = turnos;
    }

    @Override
    public boolean existeConflicto(RecursosBloqueo recursos, PeriodoBloqueo periodo) {
        return turnos.existeOcupante(recursos.profesionalId(), recursos.sillonId(), periodo.inicio(), periodo.fin(),
                EstadoTurno.OCUPANTES);
    }
}
