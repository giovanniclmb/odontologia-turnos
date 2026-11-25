package ar.edu.utn.odontologia.comunicaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class RevisionRecordatoriosTest {
    @Test
    void ejecutaElEnvioDeLosPendientes() {
        var recordatorio = mock(RecordatorioTurnoPorEmail.class);

        new RevisionRecordatorios(recordatorio).revisar();

        verify(recordatorio).enviarPendientes();
    }

    @Test
    void unaRevisionFallidaNoSePropagaNiRegistraElDetalle(CapturedOutput salida) {
        var recordatorio = mock(RecordatorioTurnoPorEmail.class);
        doThrow(new IllegalStateException("detalle con ana@example.invalid")).when(recordatorio).enviarPendientes();

        assertDoesNotThrow(() -> new RevisionRecordatorios(recordatorio).revisar());

        assertTrue(salida.getAll().contains("No se pudieron revisar los recordatorios: IllegalStateException"));
        assertFalse(salida.getAll().contains("ana@example.invalid"));
    }
}
