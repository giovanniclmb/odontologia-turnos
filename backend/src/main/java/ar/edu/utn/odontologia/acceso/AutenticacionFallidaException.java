package ar.edu.utn.odontologia.acceso;

public class AutenticacionFallidaException extends RuntimeException {
    public AutenticacionFallidaException() {
        super("Credenciales invalidas");
    }
}
