package ar.edu.utn.odontologia.recursos;

public class IntervaloSinConfigurarException extends IllegalStateException {
    public IntervaloSinConfigurarException() {
        super("El intervalo de sede está sin configurar");
    }
}
