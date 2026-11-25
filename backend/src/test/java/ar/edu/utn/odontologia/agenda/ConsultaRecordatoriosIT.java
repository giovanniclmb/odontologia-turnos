package ar.edu.utn.odontologia.agenda;

import static ar.edu.utn.odontologia.agenda.AgendaConReloj.BASE;
import static ar.edu.utn.odontologia.agenda.AgendaConReloj.INICIO;
import static ar.edu.utn.odontologia.agenda.AgendaConReloj.RECEPCION;
import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Qué turnos corresponde recordar según el reloj, el estado y la anticipación con que se fijó el horario. */
class ConsultaRecordatoriosIT {
    private static final Duration DIA = Duration.ofHours(24);
    private static final Duration MICRO = Duration.ofNanos(1_000);

    private static AgendaConReloj escenario;
    private static ConsultaRecordatorios consulta;
    private AgendaConReloj.Datos datos;

    @BeforeAll
    static void iniciar() {
        escenario = AgendaConReloj.iniciar();
        consulta = escenario.contexto.getBean(ConsultaRecordatorios.class);
    }

    @AfterAll
    static void cerrar() {
        if (escenario != null) escenario.close();
    }

    @BeforeEach
    void preparar() {
        escenario.reloj.fijar(BASE);
        datos = escenario.nuevosDatos();
    }

    @Test
    void entraCuandoFaltanExactamente24Horas() {
        var turno = escenario.alta(datos, INICIO);

        escenario.reloj.fijar(INICIO.minus(DIA).minus(MICRO));
        assertFalse(corresponde(turno));
        escenario.reloj.fijar(INICIO.minus(DIA));

        assertTrue(corresponde(turno));
    }

    @Test
    void correspondeHastaJustoAntesDelInicio() {
        var turno = escenario.alta(datos, INICIO);

        escenario.reloj.fijar(INICIO.minus(MICRO));
        assertTrue(corresponde(turno));
        escenario.reloj.fijar(INICIO);
        assertFalse(corresponde(turno));
        escenario.reloj.fijar(INICIO.plus(MICRO));

        assertFalse(corresponde(turno));
    }

    @Test
    void incluyeConfirmadosYExcluyeCancelados() {
        var confirmado = escenario.alta(datos, INICIO);
        escenario.agenda.confirmar(RECEPCION, confirmado.id(), confirmado.version());
        var cancelado = escenario.alta(datos, INICIO.plus(Duration.ofHours(1)));
        escenario.agenda.cancelar(RECEPCION, new SolicitudCancelacion(cancelado.id(), cancelado.version()));

        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(12)));
        assertTrue(corresponde(confirmado));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(1)));

        assertFalse(corresponde(cancelado));
    }

    @Test
    void unAltaConMenosDe24HorasNoSeRecuerdaYConExactamente24Si() {
        var cercano = escenario.alta(datos, BASE.plus(DIA).minus(MICRO));
        var exacto = escenario.alta(escenario.nuevosDatos(), BASE.plus(DIA));

        assertFalse(corresponde(cercano));
        assertTrue(corresponde(exacto));
    }

    @Test
    void unaReprogramacionConPocaAnticipacionNoSeRecuerda() {
        var turno = escenario.alta(datos, INICIO);

        var movido = escenario.mover(turno, BASE.plus(Duration.ofHours(10)));

        assertFalse(corresponde(movido));
    }

    @Test
    void cambiarSoloElSillonCercaDelTurnoNoQuitaElRecordatorio() {
        var turno = escenario.alta(datos, INICIO);
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));

        var movido = escenario.mover(turno, INICIO, escenario.otroSillon());

        assertTrue(corresponde(movido));
    }

    @Test
    void unNuevoHorarioConAnticipacionSeRecuerdaAlEntrarEnLaVentana() {
        var turno = escenario.alta(datos, INICIO);
        var nuevoInicio = INICIO.plus(Duration.ofDays(3));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));

        var movido = escenario.mover(turno, nuevoInicio);
        assertFalse(corresponde(movido));
        escenario.reloj.fijar(nuevoInicio.minus(DIA));

        assertEquals(Optional.of(nuevoInicio), consulta.porRecordar().stream()
                .filter(candidato -> candidato.turnoId().equals(turno.id()))
                .map(TurnoPorRecordar::inicio).findFirst());
    }

    @Test
    void unTurnoAnteriorSinElDatoSeRecuerda() {
        var turno = escenario.alta(datos, BASE.plus(Duration.ofHours(10)));
        assertFalse(corresponde(turno));

        escenario.jdbc.update("update odontologia.turno set horario_fijado_en = null where id = ?", turno.id());

        assertTrue(corresponde(turno));
    }

    @Test
    void laRelecturaDevuelveAlPacienteMientrasElHorarioSigaCorrespondiendo() {
        var turno = escenario.alta(datos, INICIO);
        escenario.reloj.fijar(INICIO.minus(DIA));
        var candidato = new TurnoPorRecordar(turno.id(), turno.inicio());
        assertEquals(Optional.of(datos.paciente()), consulta.pacienteSiSigueVigente(candidato));

        escenario.agenda.confirmar(RECEPCION, turno.id(), turno.version());
        assertEquals(Optional.of(datos.paciente()), consulta.pacienteSiSigueVigente(candidato));
        escenario.reloj.fijar(INICIO);

        assertTrue(consulta.pacienteSiSigueVigente(candidato).isEmpty());
    }

    @Test
    void laRelecturaDescartaUnTurnoCanceladoOMovido() {
        var movido = escenario.alta(datos, INICIO);
        var cancelado = escenario.alta(datos, INICIO.plus(Duration.ofHours(1)));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));
        var candidatoMovido = new TurnoPorRecordar(movido.id(), movido.inicio());
        var candidatoCancelado = new TurnoPorRecordar(cancelado.id(), cancelado.inicio());
        assertTrue(consulta.pacienteSiSigueVigente(candidatoMovido).isPresent());

        escenario.mover(movido, INICIO.plus(Duration.ofHours(2)));
        escenario.agenda.cancelar(RECEPCION, new SolicitudCancelacion(cancelado.id(), cancelado.version()));

        assertTrue(consulta.pacienteSiSigueVigente(candidatoMovido).isEmpty());
        assertTrue(consulta.pacienteSiSigueVigente(candidatoCancelado).isEmpty());
    }

    private static boolean corresponde(TurnoRegistrado turno) {
        Instant inicio = turno.inicio();
        return consulta.porRecordar().contains(new TurnoPorRecordar(turno.id(), inicio));
    }
}
