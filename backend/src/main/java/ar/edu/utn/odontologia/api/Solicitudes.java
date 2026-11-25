package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/** Validaciones de entrada compartidas por los controladores de agenda. */
final class Solicitudes {
    private static final Instant MINIMO = Instant.parse("0001-01-01T00:00:00Z");
    private static final Instant MAXIMO = Instant.parse("9999-12-31T23:59:59Z");
    private static final Duration PERIODO_MAXIMO = Duration.ofDays(7);

    private Solicitudes() {}

    static Identidad identidad(HttpServletRequest request) {
        var identidad = (Identidad) request.getAttribute(TokenAutenticacionFilter.ATRIBUTO_IDENTIDAD);
        if (identidad == null) {
            throw new NoAutenticadoException();
        }
        return identidad;
    }

    /** Un valor ausente se delega al servicio, que informa qué dato es obligatorio. */
    static Instant instante(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            var instante = Instant.parse(texto);
            if (instante.isBefore(MINIMO) || instante.isAfter(MAXIMO)) {
                throw new IllegalArgumentException("La fecha excede el rango admitido");
            }
            return instante;
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("La fecha debe usar el formato ISO 8601 con zona");
        }
    }

    /** Rechaza decimales en lugar de truncarlos. */
    static Integer entero(BigDecimal numero) {
        if (numero == null) {
            return null;
        }
        try {
            return numero.intValueExact();
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("Se requiere un número entero");
        }
    }

    static Periodo periodoDeConsulta(String desde, String hasta) {
        var inicio = instante(desde);
        var fin = instante(hasta);
        if (inicio == null || fin == null || !inicio.isBefore(fin)
                || Duration.between(inicio, fin).compareTo(PERIODO_MAXIMO) > 0) {
            throw new IllegalArgumentException("El período debe ser válido y de hasta 7 días");
        }
        return new Periodo(inicio, fin);
    }

    record Periodo(Instant desde, Instant hasta) {}
}
