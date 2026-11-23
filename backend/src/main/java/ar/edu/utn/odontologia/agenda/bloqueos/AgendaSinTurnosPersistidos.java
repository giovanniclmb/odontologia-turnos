package ar.edu.utn.odontologia.agenda.bloqueos;

import org.springframework.stereotype.Component;

/** Adaptador previo a C-06. Sustituir cuando se incorpore la persistencia de turnos. */
@Component
class AgendaSinTurnosPersistidos implements ConsultaTurnosOcupantes {
    @Override
    public boolean existeConflicto(RecursosBloqueo recursos, PeriodoBloqueo periodo) {
        return false;
    }
}
