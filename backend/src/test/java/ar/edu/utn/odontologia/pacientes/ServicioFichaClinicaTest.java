package ar.edu.utn.odontologia.pacientes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.pacientes.persistencia.FichaClinica;
import ar.edu.utn.odontologia.pacientes.persistencia.FichasClinicas;
import ar.edu.utn.odontologia.pacientes.persistencia.NotaClinica;
import ar.edu.utn.odontologia.pacientes.persistencia.NotasClinicas;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteProfesionales;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioFichaClinicaTest {
    private final PacienteReferencias pacientes = mock(PacienteReferencias.class);
    private final PacienteProfesionales asignaciones = mock(PacienteProfesionales.class);
    private final FichasClinicas fichas = mock(FichasClinicas.class);
    private final NotasClinicas notas = mock(NotasClinicas.class);
    private final UUID pacienteId = UUID.randomUUID();
    private final UUID profesionalId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();
    private final Identidad recepcion = new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
    private final Identidad odontologo = new Identidad(usuarioId, "odontologo-ficticio", Rol.ODONTOLOGO, profesionalId);
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-22T15:00:00Z"), ZoneOffset.UTC);
    private ServicioFichaClinica servicio;

    @BeforeEach
    void iniciar() {
        servicio = new ServicioFichaClinica(pacientes, asignaciones, fichas, notas,
                new ServicioAutorizacion(), reloj);
        var paciente = mock(PacienteReferencia.class);
        when(paciente.getId()).thenReturn(pacienteId);
        when(pacientes.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(asignaciones.tieneAsignacionActiva(pacienteId, profesionalId)).thenReturn(true);
    }

    @Test
    void fichaSeCreaAlGuardarAntecedentesYReceptionNoPuedeEditarlos() {
        when(fichas.findById(pacienteId)).thenReturn(Optional.empty(), Optional.of(new FichaClinica(pacienteId, "Alergia ficticia")));

        assertThrows(AutorizacionDenegadaException.class,
                () -> servicio.actualizarAntecedentes(recepcion, pacienteId, "Cambio no permitido"));
        var actualizada = servicio.actualizarAntecedentes(odontologo, pacienteId, "Alergia ficticia");

        assertEquals("Alergia ficticia", actualizada.antecedentes());
        verify(fichas).crearSiNoExiste(pacienteId);
    }

    @Test
    void recepcionYOdontologiaAsignadaPuedenLeerYOdontologoNoAsignadoNo() {
        when(fichas.findById(pacienteId)).thenReturn(Optional.of(new FichaClinica(pacienteId, "Antecedente ficticio")));
        when(notas.findAllByPacienteIdOrderByCreadaEnAscIdAsc(pacienteId)).thenReturn(List.of());
        var sinAsignacion = new Identidad(UUID.randomUUID(), "odontologo-otro", Rol.ODONTOLOGO, UUID.randomUUID());

        assertEquals("Antecedente ficticio", servicio.consultar(recepcion, pacienteId).antecedentes());
        assertEquals("Antecedente ficticio", servicio.consultar(odontologo, pacienteId).antecedentes());
        assertThrows(AutorizacionDenegadaException.class, () -> servicio.consultar(sinAsignacion, pacienteId));
    }

    @Test
    void notaRequiereContenidoYUsaAutorYFechaDelBackend() {
        when(fichas.findById(pacienteId)).thenReturn(Optional.empty(), Optional.of(new FichaClinica(pacienteId, null)));
        when(notas.save(any(NotaClinica.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregarNota(odontologo, pacienteId, new ServicioFichaClinica.CrearNotaClinica(" ")));
        var nota = servicio.agregarNota(odontologo, pacienteId,
                new ServicioFichaClinica.CrearNotaClinica("Dolor referido ficticio"));

        assertEquals(usuarioId, nota.autorId());
        assertEquals(Instant.parse("2026-11-22T15:00:00Z"), nota.creadaEn());
        verify(fichas).crearSiNoExiste(pacienteId);
    }

    @Test
    void soloElAutorAsignadoEditaYSeConservanAutorYFecha() {
        Instant creada = Instant.parse("2026-11-20T10:00:00Z");
        UUID notaId = UUID.randomUUID();
        var nota = new NotaClinica(notaId, pacienteId, usuarioId, "Texto inicial", creada);
        when(notas.findById(notaId)).thenReturn(Optional.of(nota));
        when(notas.save(nota)).thenReturn(nota);
        var otro = new Identidad(UUID.randomUUID(), "odontologo-otro", Rol.ODONTOLOGO, profesionalId);

        assertThrows(AutorizacionDenegadaException.class, () -> servicio.editarNota(otro, pacienteId, notaId,
                new ServicioFichaClinica.EditarNotaClinica("Texto ajeno")));
        var editada = servicio.editarNota(odontologo, pacienteId, notaId,
                new ServicioFichaClinica.EditarNotaClinica("Texto actualizado"));

        assertEquals("Texto actualizado", editada.texto());
        assertEquals(usuarioId, editada.autorId());
        assertEquals(creada, editada.creadaEn());
    }

    @Test
    void noExisteOperacionDeEliminacionClinica() {
        assertTrue(java.util.Arrays.stream(ServicioFichaClinica.class.getMethods())
                .noneMatch(metodo -> metodo.getName().toLowerCase().contains("eliminar")));
    }
}
