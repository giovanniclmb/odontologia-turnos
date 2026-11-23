package ar.edu.utn.odontologia.agenda.bloqueos;

/** Consulta de la agenda ejecutada en la transacción que ya adquirió los locks de los recursos. */
public interface ConsultaTurnosOcupantes {
    boolean existeConflicto(RecursosBloqueo recursos, PeriodoBloqueo periodo);
}
