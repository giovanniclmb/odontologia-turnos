package ar.edu.utn.odontologia.comunicaciones;

import java.util.UUID;

public record SolicitudEnvio(
        String tipo,
        UUID turnoId,
        String destinatario,
        String asunto,
        String cuerpo) {
    /** Sin destinatario ni contenido: el cuerpo puede llevar un enlace de un solo uso. */
    @Override
    public String toString() {
        return "SolicitudEnvio[tipo=" + tipo + ", turnoId=" + turnoId + ", destinatario=oculto, cuerpo=oculto]";
    }
}