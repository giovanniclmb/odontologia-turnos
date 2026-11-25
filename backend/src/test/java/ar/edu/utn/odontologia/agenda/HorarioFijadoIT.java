package ar.edu.utn.odontologia.agenda;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** La agenda guarda cuándo se fijó el horario vigente de cada turno. */
class HorarioFijadoIT {
    private static AgendaConReloj escenario;
    private AgendaConReloj.Datos datos;

    @BeforeAll
    static void iniciar() {
        escenario = AgendaConReloj.iniciar();
    }

    @AfterAll
    static void cerrar() {
        if (escenario != null) escenario.close();
    }

    @BeforeEach
    void preparar() {
        escenario.reloj.fijar(AgendaConReloj.BASE);
        datos = escenario.nuevosDatos();
    }

    @Test
    void elAltaGuardaCuandoSeFijoElHorario() {
        var turno = escenario.alta(datos, AgendaConReloj.INICIO);

        assertEquals(AgendaConReloj.BASE, fijado(turno.id()));
    }

    @Test
    void reprogramarAOtroInicioActualizaElDato() {
        var turno = escenario.alta(datos, AgendaConReloj.INICIO);
        escenario.reloj.avanzar(Duration.ofHours(1));

        escenario.mover(turno, AgendaConReloj.INICIO.plus(Duration.ofHours(2)));

        assertEquals(AgendaConReloj.BASE.plus(Duration.ofHours(1)), fijado(turno.id()));
    }

    @Test
    void guardaElDatoTruncadoComoLosIniciosParaNoAlterarElBordeDe24Horas() {
        var fraccion = Duration.ofNanos(999);
        escenario.reloj.fijar(AgendaConReloj.BASE.plus(fraccion));
        var turno = escenario.alta(datos, AgendaConReloj.INICIO);
        assertEquals(AgendaConReloj.BASE, fijado(turno.id()));

        escenario.reloj.avanzar(Duration.ofHours(1));
        escenario.mover(turno, AgendaConReloj.INICIO.plus(Duration.ofHours(2)));

        assertEquals(AgendaConReloj.BASE.plus(Duration.ofHours(1)), fijado(turno.id()));
    }

    @Test
    void cambiarSoloElSillonConservaElDato() {
        var turno = escenario.alta(datos, AgendaConReloj.INICIO);
        escenario.reloj.avanzar(Duration.ofHours(1));

        escenario.mover(turno, AgendaConReloj.INICIO, escenario.otroSillon());

        assertEquals(AgendaConReloj.BASE, fijado(turno.id()));
    }

    @Test
    void confirmarYCancelarConservanElDato() {
        var turno = escenario.alta(datos, AgendaConReloj.INICIO);
        escenario.reloj.avanzar(Duration.ofHours(1));

        var confirmado = escenario.agenda.confirmar(AgendaConReloj.RECEPCION, turno.id(), turno.version());
        assertEquals(AgendaConReloj.BASE, fijado(turno.id()));
        escenario.agenda.cancelar(AgendaConReloj.RECEPCION,
                new SolicitudCancelacion(confirmado.id(), confirmado.version()));

        assertEquals(AgendaConReloj.BASE, fijado(turno.id()));
    }

    @Test
    void unaReprogramacionRevertidaConservaElDato() {
        var turno = escenario.alta(datos, AgendaConReloj.INICIO);
        escenario.reloj.avanzar(Duration.ofHours(1));
        var transacciones = new TransactionTemplate(escenario.contexto.getBean(PlatformTransactionManager.class));

        transacciones.executeWithoutResult(transaccion -> {
            escenario.mover(turno, AgendaConReloj.INICIO.plus(Duration.ofHours(2)));
            transaccion.setRollbackOnly();
        });

        assertEquals(AgendaConReloj.BASE, fijado(turno.id()));
    }

    private static java.time.Instant fijado(UUID turno) {
        return escenario.jdbc.queryForObject(
                "select horario_fijado_en from odontologia.turno where id = ?", OffsetDateTime.class, turno)
                .toInstant();
    }
}
