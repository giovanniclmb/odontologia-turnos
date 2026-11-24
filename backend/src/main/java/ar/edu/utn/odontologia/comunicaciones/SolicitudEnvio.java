package ar.edu.utn.odontologia.comunicaciones;

import java.util.UUID;

public record SolicitudEnvio(
        String tipo,
        UUID turnoId,
        String destinatario,
        String asunto,
        String cuerpo) {
}