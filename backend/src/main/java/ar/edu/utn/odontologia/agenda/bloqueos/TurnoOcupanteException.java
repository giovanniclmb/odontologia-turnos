package ar.edu.utn.odontologia.agenda.bloqueos;

public class TurnoOcupanteException extends RuntimeException {
    public TurnoOcupanteException() {
        super("El bloqueo afecta a un turno existente");
    }
}
