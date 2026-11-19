package ar.edu.utn.odontologia.agenda;

import static ar.edu.utn.odontologia.agenda.Agenda.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AgendaTest {
    private final Agenda agenda = new Agenda();
    private static final Duration INTERVALO = Duration.ofMinutes(10);

    private static Instant hora(String hora) {
        return Instant.parse("2026-11-20T" + hora + ":00Z");
    }

    private static Solicitud solicitud(Instant inicio, Duration ajuste) {
        return new Solicitud("PAC-PRUEBA", "P1", "S1",
                new Prestacion("CONSULTA", Duration.ofMinutes(30)), inicio, ajuste);
    }

    private static Reserva reserva(String profesional, String sillon, String inicio, String fin) {
        return new Reserva(profesional, sillon, hora(inicio), hora(fin));
    }

    @Test void creaConDuracionDePrestacionYConservaReferencias() {
        var resultado = agenda.crear(solicitud(hora("10:00"), null), List.of(), INTERVALO);
        var turno = assertInstanceOf(Exito.class, resultado).turno();
        assertAll(() -> assertEquals("PAC-PRUEBA", turno.paciente()),
                () -> assertEquals("P1", turno.profesional()),
                () -> assertEquals("S1", turno.sillon()),
                () -> assertEquals("CONSULTA", turno.prestacion()),
                () -> assertEquals(hora("10:00"), turno.inicio()),
                () -> assertEquals(hora("10:30"), turno.fin()),
                () -> assertEquals(Duration.ofMinutes(30), turno.duracion()));
    }

    @Test void usaAjusteExplicitoDeDuracion() {
        var resultado = agenda.crear(solicitud(hora("10:00"), Duration.ofMinutes(45)), List.of(), INTERVALO);
        var turno = assertInstanceOf(Exito.class, resultado).turno();
        assertEquals(hora("10:45"), turno.fin());
        assertEquals(Duration.ofMinutes(45), turno.duracion());
    }

    static Stream<Arguments> entradasInvalidas() {
        var base = solicitud(hora("10:00"), null);
        var consulta = base.prestacion();
        var casos = new ArrayList<Arguments>();
        casos.add(Arguments.of("solicitud ausente", null, List.of(), INTERVALO));
        for (String referencia : Arrays.asList(null, "", "  ")) {
            casos.add(Arguments.of("paciente: " + referencia,
                    new Solicitud(referencia, "P1", "S1", consulta, hora("10:00"), null), List.of(), INTERVALO));
            casos.add(Arguments.of("profesional: " + referencia,
                    new Solicitud("PAC", referencia, "S1", consulta, hora("10:00"), null), List.of(), INTERVALO));
            casos.add(Arguments.of("sillon: " + referencia,
                    new Solicitud("PAC", "P1", referencia, consulta, hora("10:00"), null), List.of(), INTERVALO));
            casos.add(Arguments.of("prestacion: " + referencia,
                    new Solicitud("PAC", "P1", "S1", new Prestacion(referencia, Duration.ofMinutes(30)), hora("10:00"), null), List.of(), INTERVALO));
        }
        casos.add(Arguments.of("prestacion ausente", new Solicitud("PAC", "P1", "S1", null, hora("10:00"), null), List.of(), INTERVALO));
        casos.add(Arguments.of("inicio ausente", solicitud(null, null), List.of(), INTERVALO));
        for (Duration duracion : Arrays.asList(null, Duration.ZERO, Duration.ofMinutes(-1))) {
            casos.add(Arguments.of("duracion referencia " + duracion,
                    new Solicitud("PAC", "P1", "S1", new Prestacion("CONSULTA", duracion), hora("10:00"), null), List.of(), INTERVALO));
        }
        for (Duration ajuste : List.of(Duration.ZERO, Duration.ofMinutes(-1))) {
            casos.add(Arguments.of("ajuste " + ajuste, solicitud(hora("10:00"), ajuste), List.of(), INTERVALO));
        }
        for (Duration intervalo : Arrays.asList(null, Duration.ZERO, Duration.ofMinutes(-1))) {
            casos.add(Arguments.of("intervalo " + intervalo, base, List.of(), intervalo));
        }
        casos.add(Arguments.of("reservas ausentes", base, null, INTERVALO));
        casos.add(Arguments.of("reserva nula", base, Arrays.asList((Reserva) null), INTERVALO));
        for (Reserva reserva : List.of(
                new Reserva(null, "S1", hora("09:00"), hora("09:30")),
                new Reserva("P1", " ", hora("09:00"), hora("09:30")),
                new Reserva("P1", "S1", null, hora("09:30")),
                new Reserva("P1", "S1", hora("09:00"), null),
                reserva("P1", "S1", "09:30", "09:30"),
                reserva("P1", "S1", "09:30", "09:00"))) {
            casos.add(Arguments.of("reserva invalida " + reserva, base, List.of(reserva), INTERVALO));
        }
        casos.add(Arguments.of("desborde fin", solicitud(Instant.MAX, null), List.of(), INTERVALO));
        casos.add(Arguments.of("desborde intervalo", base, List.of(reserva("P1", "S1", "09:00", "09:30")), Duration.ofSeconds(Long.MAX_VALUE)));
        return casos.stream();
    }

    @ParameterizedTest(name = "{0}") @MethodSource("entradasInvalidas")
    void rechazaEntradaInvalida(String caso, Solicitud solicitud, List<Reserva> reservas, Duration intervalo) {
        assertInstanceOf(EntradaInvalida.class, agenda.crear(solicitud, reservas, intervalo), caso);
    }

    static Stream<Arguments> conflictos() {
        return Stream.of(
                Arguments.of("P1", "S2", "10:15", "10:45", Recurso.PROFESIONAL),
                Arguments.of("P2", "S1", "10:15", "10:45", Recurso.SILLON),
                Arguments.of("P1", "S1", "10:15", "10:45", Recurso.AMBOS),
                Arguments.of("P1", "S2", "10:00", "10:30", Recurso.PROFESIONAL),
                Arguments.of("P1", "S2", "09:00", "11:00", Recurso.PROFESIONAL),
                Arguments.of("P2", "S1", "10:10", "10:20", Recurso.SILLON));
    }

    @ParameterizedTest @MethodSource("conflictos")
    void rechazaSolapamiento(String profesional, String sillon, String inicio, String fin, Recurso esperado) {
        var resultado = agenda.crear(solicitud(hora("10:00"), null),
                List.of(reserva(profesional, sillon, inicio, fin)), INTERVALO);
        assertEquals(esperado, assertInstanceOf(NoDisponible.class, resultado).recurso());
    }

    @Test void permiteMismaHoraConRecursosDistintos() {
        assertInstanceOf(Exito.class, agenda.crear(solicitud(hora("10:00"), null),
                List.of(reserva("P2", "S2", "10:00", "10:30")), INTERVALO));
    }

    static Stream<Arguments> bordes() {
        return Stream.of("PROFESIONAL", "SILLON").flatMap(recurso -> Stream.of(
                Arguments.of(recurso, "09:20", "09:50", true),
                Arguments.of(recurso, "09:21", "09:51", false),
                Arguments.of(recurso, "10:40", "11:10", true),
                Arguments.of(recurso, "10:39", "11:09", false)));
    }

    @ParameterizedTest @MethodSource("bordes")
    void respetaIntervaloInclusivoAntesYDespues(String recurso, String inicio, String fin, boolean permitido) {
        var reserva = reserva(recurso.equals("PROFESIONAL") ? "P1" : "P2",
                recurso.equals("SILLON") ? "S1" : "S2", inicio, fin);
        var resultado = agenda.crear(solicitud(hora("10:00"), null), List.of(reserva), INTERVALO);
        if (permitido) assertInstanceOf(Exito.class, resultado);
        else assertInstanceOf(NoDisponible.class, resultado);
    }

    @Test void usaElIntervaloRecibidoSinFijarDiezMinutos() {
        assertInstanceOf(NoDisponible.class, agenda.crear(solicitud(hora("10:00"), null),
                List.of(reserva("P1", "S2", "09:20", "09:50")), Duration.ofMinutes(15)));
    }

    @Test void ajusteDeDuracionPuedeGenerarConflicto() {
        var reservas = List.of(reserva("P1", "S2", "11:00", "11:30"));
        assertInstanceOf(Exito.class, agenda.crear(solicitud(hora("10:00"), null), reservas, INTERVALO));
        assertInstanceOf(NoDisponible.class, agenda.crear(solicitud(hora("10:00"), Duration.ofMinutes(55)), reservas, INTERVALO));
    }

    @Test void encuentraConflictoEnColeccionNoOrdenada() {
        var ajena = reserva("P2", "S2", "08:00", "08:30");
        var conflicto = reserva("P1", "S2", "10:15", "10:45");
        var posterior = reserva("P1", "S2", "15:00", "15:30");
        for (var reservas : List.of(List.of(posterior, ajena, conflicto), List.of(conflicto, posterior, ajena))) {
            assertInstanceOf(NoDisponible.class, agenda.crear(solicitud(hora("10:00"), null), reservas, INTERVALO));
        }
    }

    @Test void rechazaReservaMalformadaAunqueNoCompartaRecursos() {
        var reservas = List.of(reserva("P1", "S2", "10:15", "10:45"),
                new Reserva("P2", "S2", hora("11:00"), hora("10:00")));
        assertInstanceOf(EntradaInvalida.class, agenda.crear(solicitud(hora("10:00"), null), reservas, INTERVALO));
    }

    @Test void noModificaReservasAlAceptarNiRechazar() {
        for (var reserva : List.of(reserva("P2", "S2", "10:15", "10:45"), reserva("P1", "S2", "10:15", "10:45"))) {
            var reservas = new ArrayList<>(List.of(reserva));
            var copia = List.copyOf(reservas);
            var resultado = agenda.crear(solicitud(hora("10:00"), null), reservas, INTERVALO);
            if (reserva.profesional().equals("P1")) assertInstanceOf(NoDisponible.class, resultado);
            else assertInstanceOf(Exito.class, resultado);
            assertEquals(copia, reservas);
            assertDoesNotThrow(() -> agenda.crear(solicitud(hora("10:00"), null), copia, INTERVALO));
        }
    }
}
