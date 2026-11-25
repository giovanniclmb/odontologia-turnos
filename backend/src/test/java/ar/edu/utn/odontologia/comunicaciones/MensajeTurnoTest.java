package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.agenda.TurnoProgramado.Motivo;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MensajeTurnoTest {
    private static final Instant INICIO = Instant.parse("2040-04-10T10:00:00Z");
    private static final String ENLACE = "https://turnos.example.invalid/cancelar#token-ficticio";

    @Test
    void elAltaInformaFechaDeMendozaEnlaceYFechaLimite() {
        var mensaje = MensajeTurno.componer("Ana", INICIO, Motivo.ALTA, ENLACE);

        assertEquals("Tu turno en el consultorio", mensaje.asunto());
        assertEquals("""
                Hola, Ana:

                Tu turno quedó registrado para el 10/04/2040 a las 07:00 (hora de Mendoza).

                Si necesitás cancelarlo, podés hacerlo hasta el 08/04/2040 a las 07:00 con este enlace:
                https://turnos.example.invalid/cancelar#token-ficticio
                """, mensaje.cuerpo());
    }

    @Test
    void laReprogramacionInformaLaNuevaFecha() {
        var mensaje = MensajeTurno.componer("Ana", INICIO, Motivo.REPROGRAMACION, ENLACE);

        assertEquals("Tu turno fue reprogramado", mensaje.asunto());
        assertTrue(mensaje.cuerpo().contains("Tu turno fue reprogramado para el 10/04/2040 a las 07:00"));
        assertTrue(mensaje.cuerpo().contains(ENLACE));
    }

    @Test
    void sinEnlaceAclaraQueYaNoPuedeCancelarsePorEsaVia() {
        var cuerpo = MensajeTurno.componer("Ana", INICIO, Motivo.ALTA, null).cuerpo();

        assertTrue(cuerpo.contains("Faltan menos de 48 horas para el turno, así que ya no puede cancelarse por enlace."));
        assertFalse(cuerpo.contains("http"));
        assertFalse(cuerpo.contains("#"));
    }

    @Test
    void elRecordatorioInformaLaFechaDeMendozaYQueYaNoPuedeCancelarsePorEnlace() {
        var mensaje = MensajeTurno.recordatorio("Ana", INICIO);

        assertEquals("Recordatorio de tu turno", mensaje.asunto());
        assertEquals("""
                Hola, Ana:

                Te recordamos tu turno del 10/04/2040 a las 07:00 (hora de Mendoza).

                Ya venció el plazo para cancelarlo por enlace.
                """, mensaje.cuerpo());
        assertFalse(mensaje.toString().contains("Ana"));
    }

    @Test
    void convierteALaFechaDeMendozaAunqueCambieElDia() {
        var cuerpo = MensajeTurno.componer("Ana", Instant.parse("2040-01-01T02:30:00Z"), Motivo.ALTA, null).cuerpo();

        assertTrue(cuerpo.contains("31/12/2039 a las 23:30"), cuerpo);
    }

    @Test
    void elTextoDelMensajeYDeLaSolicitudNoExponeDatosNiEnlace() {
        var mensaje = MensajeTurno.componer("Ana", INICIO, Motivo.ALTA, ENLACE);
        var turno = java.util.UUID.randomUUID();
        var solicitud = new SolicitudEnvio("turno-alta", turno, "ana@example.invalid",
                mensaje.asunto(), mensaje.cuerpo());

        for (var texto : java.util.List.of(mensaje.toString(), solicitud.toString())) {
            assertFalse(texto.contains("token-ficticio"), texto);
            assertFalse(texto.contains("Ana"), texto);
            assertFalse(texto.contains("ana@example.invalid"), texto);
        }
        assertTrue(solicitud.toString().contains(turno.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://turnos.example.invalid/cancelar", " https://turnos.example.invalid/cancelar ",
            "http://localhost:5173/frontend/public/cancelar.html"})
    void formaElEnlaceConElTokenDespuesDelNumeral(String base) {
        assertEquals(base.strip() + "#abc_-123", MensajeTurno.enlace(base, "abc_-123").orElseThrow());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "no-es-url", "ftp://turnos.example.invalid", "https://", "javascript:alert(1)",
            "https://turnos.example.invalid/cancelar#ya", "https://usuario:clave@turnos.example.invalid/x",
            "/cancelar", "https://turnos.example.invalid/con espacio"})
    void rechazaUnaUrlBaseQueNoSeaWeb(String base) {
        assertTrue(MensajeTurno.enlace(base, "abc").isEmpty());
    }
}
