package ar.edu.utn.odontologia.comunicaciones;

public record ResultadoEnvio(
        boolean exitoso,
        int intentos,
        String error,
        String idempotencyKey) {
}