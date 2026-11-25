package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ConsultaAgenda;
import ar.edu.utn.odontologia.agenda.EstadoTurno;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.agenda.bloqueos.PeriodoBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.RecursosBloqueo;
import ar.edu.utn.odontologia.agenda.bloqueos.ServicioBloqueos;
import ar.edu.utn.odontologia.agenda.bloqueos.ServicioBloqueos.BloqueoVisible;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

/** Lecturas con alcance por rol que consume la interfaz de agenda. Cada prueba usa un día propio. */
class LecturasAgendaIT {
    private static final Instant AHORA = Instant.parse("2032-03-01T00:00:00Z");
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION, null);
    private static final Duration HORA = Duration.ofHours(1);
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ConsultaAgenda consultaAgenda;
    private static ServicioBloqueos bloqueos;
    private static ServicioPacientes pacientes;
    private static ServicioRecursosAgenda recursos;
    private static ServicioAsignacionPacientes asignaciones;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojAgendaPruebas", Clock.class,
                    () -> Clock.fixed(AHORA, ZoneOffset.UTC), bean -> bean.setPrimary(true));
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        consultaAgenda = contexto.getBean(ConsultaAgenda.class);
        bloqueos = contexto.getBean(ServicioBloqueos.class);
        pacientes = contexto.getBean(ServicioPacientes.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
        asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(RECEPCION, 10);
    }

    @AfterAll
    static void cerrar() {
        try {
            if (contexto != null) contexto.close();
        } finally {
            if (db != null) db.close();
        }
    }

    @Test
    void recepcionRecibeTodosLosBloqueosQueIntersectanElPeriodoOrdenados() {
        var desde = Instant.parse("2032-04-01T00:00:00Z");
        var hasta = desde.plus(Duration.ofDays(1));
        UUID sillon = sillon();
        UUID otroSillon = sillon();
        UUID profesional = profesional();
        var tarde = bloquear(null, otroSillon, desde.plus(HORA.multipliedBy(5)), desde.plus(HORA.multipliedBy(6)));
        var temprano = bloquear(profesional, sillon, desde.plus(HORA), desde.plus(HORA.multipliedBy(2)));
        bloquear(null, sillon, desde.minus(HORA), desde);
        bloquear(null, sillon, hasta, hasta.plus(HORA));

        var visibles = bloqueos.consultarVisibles(RECEPCION, sillon, desde, hasta);

        assertEquals(List.of(
                new BloqueoVisible(temprano, desde.plus(HORA), desde.plus(HORA.multipliedBy(2)), sillon, profesional),
                new BloqueoVisible(tarde, desde.plus(HORA.multipliedBy(5)), desde.plus(HORA.multipliedBy(6)),
                        otroSillon, null)), visibles);
    }

    @Test
    void odontologiaRecibeSoloLoQueAfectaAlSillonOASuProfesionalSinReferenciasAjenas() {
        var desde = Instant.parse("2032-05-01T00:00:00Z");
        var hasta = desde.plus(Duration.ofDays(1));
        UUID sillon = sillon();
        UUID otroSillon = sillon();
        UUID propio = profesional();
        UUID ajeno = profesional();
        var delSillon = bloquear(ajeno, sillon, desde.plus(HORA), desde.plus(HORA.multipliedBy(2)));
        var delProfesional = bloquear(propio, otroSillon, desde.plus(HORA.multipliedBy(3)), desde.plus(HORA.multipliedBy(4)));
        var deAmbos = bloquear(propio, sillon, desde.plus(HORA.multipliedBy(5)), desde.plus(HORA.multipliedBy(6)));
        bloquear(ajeno, otroSillon, desde.plus(HORA.multipliedBy(7)), desde.plus(HORA.multipliedBy(8)));

        var visibles = bloqueos.consultarVisibles(identidad(Rol.ODONTOLOGO, propio), sillon, desde, hasta);

        assertEquals(List.of(delSillon, delProfesional, deAmbos), visibles.stream().map(BloqueoVisible::id).toList());
        assertEquals(sillon, visibles.get(0).sillonId());
        assertNull(visibles.get(0).profesionalId());
        assertNull(visibles.get(1).sillonId());
        assertEquals(propio, visibles.get(1).profesionalId());
        assertEquals(sillon, visibles.get(2).sillonId());
        assertEquals(propio, visibles.get(2).profesionalId());
    }

    @Test
    void odontologiaSinProfesionalVinculadoRecibeSoloLaAfectacionDelSillon() {
        var desde = Instant.parse("2032-06-01T00:00:00Z");
        UUID sillon = sillon();
        var delSillon = bloquear(profesional(), sillon, desde.plus(HORA), desde.plus(HORA.multipliedBy(2)));
        bloquear(profesional(), null, desde.plus(HORA.multipliedBy(3)), desde.plus(HORA.multipliedBy(4)));

        var visibles = bloqueos.consultarVisibles(identidad(Rol.ODONTOLOGO, null), sillon, desde,
                desde.plus(Duration.ofDays(1)));

        assertEquals(List.of(new BloqueoVisible(delSillon, desde.plus(HORA), desde.plus(HORA.multipliedBy(2)),
                sillon, null)), visibles);
    }

    @Test
    void consultaDeBloqueosRechazaFaltaDeIdentidadReferenciaOPeriodoValido() {
        var desde = Instant.parse("2032-07-01T00:00:00Z");
        UUID sillon = sillon();
        assertThrows(AutorizacionDenegadaException.class,
                () -> bloqueos.consultarVisibles(null, sillon, desde, desde.plus(HORA)));
        assertThrows(IllegalArgumentException.class,
                () -> bloqueos.consultarVisibles(RECEPCION, null, desde, desde.plus(HORA)));
        assertThrows(IllegalArgumentException.class,
                () -> bloqueos.consultarVisibles(RECEPCION, sillon, desde, desde));
        assertThrows(IllegalArgumentException.class,
                () -> bloqueos.consultarVisibles(RECEPCION, sillon, null, desde));
    }

    @Test
    void laAgendaIdentificaAlPacienteDeCadaTurnoVisibleIncluidosLosCancelados() {
        var dia = Instant.parse("2032-08-01T00:00:00Z");
        UUID sillon = sillon();
        UUID prestacion = prestacion();
        var primero = turno(paciente("Ana", "Ficticia"), profesional(), sillon, prestacion, dia.plus(HORA));
        var segundo = turno(paciente("Bruno", "Ejemplo"), profesional(), sillon, prestacion, dia.plus(HORA.multipliedBy(3)));
        var cancelado = agenda.cancelar(RECEPCION,
                new ServicioAgenda.SolicitudCancelacion(segundo.id(), segundo.version()));

        var visibles = consultaAgenda.consultar(RECEPCION, sillon, dia, dia.plus(Duration.ofDays(1)));

        assertEquals(List.of(new ConsultaAgenda.TurnoAgenda(primero, "Ana", "Ficticia"),
                new ConsultaAgenda.TurnoAgenda(cancelado, "Bruno", "Ejemplo")), visibles);
        assertEquals(EstadoTurno.CANCELADO, visibles.get(1).turno().estado());
    }

    @Test
    void odontologiaVeElNombreEnSuTurnoSinAsignacionPeroNoAccedeAlPacienteNiATurnosAjenos() {
        var dia = Instant.parse("2032-09-01T00:00:00Z");
        UUID sillon = sillon();
        UUID prestacion = prestacion();
        UUID propio = profesional();
        UUID paciente = paciente("Carla", "Prueba");
        asignaciones.asignar(RECEPCION, paciente, propio);
        var mio = turno(paciente, propio, sillon, prestacion, dia.plus(HORA));
        turno(paciente("Diego", "Ajeno"), profesional(), sillon, prestacion, dia.plus(HORA.multipliedBy(3)));
        asignaciones.desasignar(RECEPCION, paciente, propio);
        var odontologo = identidad(Rol.ODONTOLOGO, propio);

        var visibles = consultaAgenda.consultar(odontologo, sillon, dia, dia.plus(Duration.ofDays(1)));

        assertEquals(List.of(new ConsultaAgenda.TurnoAgenda(mio, "Carla", "Prueba")), visibles);
        assertThrows(AutorizacionDenegadaException.class, () -> pacientes.consultar(odontologo, paciente));
    }

    @Test
    void laAgendaConNombresConservaLaAutorizacionYDevuelveVacio() {
        var dia = Instant.parse("2032-10-01T00:00:00Z");
        UUID sillon = sillon();
        assertThrows(AutorizacionDenegadaException.class,
                () -> consultaAgenda.consultar(null, sillon, dia, dia.plus(HORA)));
        assertTrue(consultaAgenda.consultar(RECEPCION, sillon, dia, dia.plus(HORA)).isEmpty());
    }

    private static UUID bloquear(UUID profesional, UUID sillon, Instant inicio, Instant fin) {
        return bloqueos.crear(RECEPCION, new RecursosBloqueo(profesional, sillon),
                new PeriodoBloqueo(inicio, fin)).id();
    }

    private static TurnoRegistrado turno(UUID paciente, UUID profesional, UUID sillon, UUID prestacion, Instant inicio) {
        return agenda.crear(RECEPCION, new SolicitudAlta(paciente, profesional, sillon, prestacion, inicio, null));
    }

    private static UUID paciente(String nombre, String apellido) {
        return pacientes.crear(RECEPCION,
                new CrearPaciente(codigo(), nombre, apellido, "lectura@example.invalid", "555-0100")).id();
    }

    private static UUID profesional() {
        return recursos.altaProfesional(RECEPCION, codigo(), true).id();
    }

    private static UUID sillon() {
        return recursos.altaSillon(RECEPCION, codigo(), true).id();
    }

    private static UUID prestacion() {
        return recursos.altaPrestacion(RECEPCION, codigo(), 30).id();
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

    private static Identidad identidad(Rol rol, UUID profesional) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio", rol, profesional);
    }
}
