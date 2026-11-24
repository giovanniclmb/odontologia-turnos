package ar.edu.utn.odontologia.agenda;

public class VersionTurnoDesactualizadaException extends RuntimeException {
    public VersionTurnoDesactualizadaException() {
        this("reprogramarlo");
    }

    VersionTurnoDesactualizadaException(String operacion) {
        super("El turno cambió. Volvé a consultar la agenda antes de " + operacion);
    }
}
