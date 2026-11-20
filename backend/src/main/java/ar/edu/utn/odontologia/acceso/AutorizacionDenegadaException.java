package ar.edu.utn.odontologia.acceso;

public class AutorizacionDenegadaException extends RuntimeException {
    public AutorizacionDenegadaException() {
        super("Autorizacion denegada");
    }
}
