package ar.edu.utn.odontologia.agenda;

public class ConflictoAgendaException extends RuntimeException {
    public ConflictoAgendaException() {
        super("El horario solicitado no está disponible");
    }
}
