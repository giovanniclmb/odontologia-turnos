package ar.edu.utn.odontologia.comunicaciones;

import ar.edu.utn.odontologia.agenda.TurnoProgramado.Motivo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Texto del email de un turno. Solo recibe los datos que el usuario aprobó informar. */
public record MensajeTurno(String asunto, String cuerpo) {
    private static final Duration PLAZO_CANCELACION = Duration.ofHours(48);
    private static final DateTimeFormatter FECHA_Y_HORA = DateTimeFormatter
            .ofPattern("dd/MM/yyyy 'a las' HH:mm")
            .withZone(ZoneId.of("America/Argentina/Mendoza"));

    /** {@code enlace} es nulo cuando faltan menos de 48 horas. */
    public static MensajeTurno componer(String nombre, Instant inicio, Motivo motivo, String enlace) {
        var asunto = switch (motivo) {
            case ALTA -> "Tu turno en el consultorio";
            case REPROGRAMACION -> "Tu turno fue reprogramado";
        };
        var novedad = switch (motivo) {
            case ALTA -> "Tu turno quedó registrado";
            case REPROGRAMACION -> "Tu turno fue reprogramado";
        };
        var cancelacion = enlace == null
                ? "Faltan menos de 48 horas para el turno, así que ya no puede cancelarse por enlace."
                : "Si necesitás cancelarlo, podés hacerlo hasta el "
                        + FECHA_Y_HORA.format(inicio.minus(PLAZO_CANCELACION)) + " con este enlace:\n" + enlace;
        return new MensajeTurno(asunto, "Hola, " + nombre + ":\n\n" + novedad + " para el "
                + FECHA_Y_HORA.format(inicio) + " (hora de Mendoza).\n\n" + cancelacion + "\n");
    }

    /** El recordatorio sale cuando el plazo de 48 horas ya venció, por eso nunca lleva enlace. */
    public static MensajeTurno recordatorio(String nombre, Instant inicio) {
        return new MensajeTurno("Recordatorio de tu turno", "Hola, " + nombre + ":\n\nTe recordamos tu turno del "
                + FECHA_Y_HORA.format(inicio) + " (hora de Mendoza).\n\n"
                + "Ya venció el plazo para cancelarlo por enlace.\n");
    }

    /** El cuerpo lleva el nombre y el enlace: no debe llegar a un registro por accidente. */
    @Override
    public String toString() {
        return "MensajeTurno[asunto=" + asunto + ", cuerpo=oculto]";
    }

    /**
     * Une la URL base y el token con {@code #}, para que el navegador no lo envíe al servidor. Vacío si la base
     * no es una dirección http o https sin credenciales ni fragmento propio.
     */
    public static Optional<String> enlace(String urlBase, String token) {
        if (urlBase == null || urlBase.isBlank()) {
            return Optional.empty();
        }
        try {
            var base = new URI(urlBase.strip());
            boolean web = "http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme());
            if (!web || base.getHost() == null || base.getUserInfo() != null || base.getFragment() != null) {
                return Optional.empty();
            }
            return Optional.of(base + "#" + token);
        } catch (URISyntaxException error) {
            return Optional.empty();
        }
    }
}
