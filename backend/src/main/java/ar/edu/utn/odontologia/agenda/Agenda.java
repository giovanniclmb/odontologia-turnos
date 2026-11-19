package ar.edu.utn.odontologia.agenda;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Reglas puras de creación; no lee catálogos ni reserva recursos de forma persistente. */
public final class Agenda {
    public record Prestacion(String referencia, Duration duracion) {}
    public record Solicitud(String paciente, String profesional, String sillon,
                            Prestacion prestacion, Instant inicio, Duration duracionAjustada) {}
    public record Reserva(String profesional, String sillon, Instant inicio, Instant fin) {}
    public record Turno(String paciente, String profesional, String sillon, String prestacion,
                        Instant inicio, Instant fin, Duration duracion) {}
    public enum Recurso { PROFESIONAL, SILLON, AMBOS }
    public sealed interface Resultado permits Exito, EntradaInvalida, NoDisponible {}
    public record Exito(Turno turno) implements Resultado {}
    public record EntradaInvalida(String motivo) implements Resultado {}
    public record NoDisponible(Recurso recurso) implements Resultado {}

    /**
     * Evalúa una solicitud contra reservas ocupantes válidas con un intervalo positivo explícito.
     * No modifica las entradas. El llamador debe seleccionar las reservas que ocupan recursos.
     */
    public Resultado crear(Solicitud solicitud, List<Reserva> reservas, Duration intervalo) {
        if (solicitud == null || reservas == null || !positiva(intervalo)) {
            return new EntradaInvalida("Solicitud, reservas e intervalo positivo son obligatorios");
        }
        var prestacion = solicitud.prestacion();
        if (!referenciaValida(solicitud.paciente()) || !referenciaValida(solicitud.profesional())
                || !referenciaValida(solicitud.sillon()) || prestacion == null
                || !referenciaValida(prestacion.referencia()) || !positiva(prestacion.duracion())
                || solicitud.inicio() == null) {
            return new EntradaInvalida("Referencias, inicio y duración de prestación deben ser válidos");
        }
        var duracion = solicitud.duracionAjustada() == null
                ? prestacion.duracion() : solicitud.duracionAjustada();
        if (!positiva(duracion)) return new EntradaInvalida("La duración efectiva debe ser positiva");
        for (var reserva : reservas) {
            if (reserva == null || !referenciaValida(reserva.profesional())
                    || !referenciaValida(reserva.sillon()) || reserva.inicio() == null
                    || reserva.fin() == null || !reserva.inicio().isBefore(reserva.fin())) {
                return new EntradaInvalida("Las reservas ocupantes deben tener recursos y períodos válidos");
            }
        }
        try {
            var fin = solicitud.inicio().plus(duracion);
            fin.plus(intervalo);
            for (var reserva : reservas) reserva.fin().plus(intervalo);
            for (var reserva : reservas) {
                boolean profesional = solicitud.profesional().equals(reserva.profesional());
                boolean sillon = solicitud.sillon().equals(reserva.sillon());
                // Igualdad con el intervalo mínimo es válida en ambos sentidos.
                if ((profesional || sillon) && fin.plus(intervalo).isAfter(reserva.inicio())
                        && reserva.fin().plus(intervalo).isAfter(solicitud.inicio())) {
                    return new NoDisponible(profesional && sillon ? Recurso.AMBOS
                            : profesional ? Recurso.PROFESIONAL : Recurso.SILLON);
                }
            }
            return new Exito(new Turno(solicitud.paciente(), solicitud.profesional(), solicitud.sillon(),
                    prestacion.referencia(), solicitud.inicio(), fin, duracion));
        } catch (DateTimeException | ArithmeticException error) {
            return new EntradaInvalida("El período o intervalo excede el rango temporal representable");
        }
    }

    private static boolean referenciaValida(String referencia) {
        return referencia != null && !referencia.isBlank();
    }

    private static boolean positiva(Duration duracion) {
        return duracion != null && duracion.compareTo(Duration.ZERO) > 0;
    }
}
