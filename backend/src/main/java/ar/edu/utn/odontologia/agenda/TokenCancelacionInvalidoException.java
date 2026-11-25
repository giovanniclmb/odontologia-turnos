package ar.edu.utn.odontologia.agenda;

public class TokenCancelacionInvalidoException extends RuntimeException {
    public TokenCancelacionInvalidoException() {
        super("El enlace de cancelación no está disponible");
    }
}