package ar.edu.utn.odontologia.acceso;

public class PoliticaContrasenaException extends IllegalArgumentException {
    public PoliticaContrasenaException(String mensaje) {
        super(mensaje);
    }
}
