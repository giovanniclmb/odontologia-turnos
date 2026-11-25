package ar.edu.utn.odontologia.agenda;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudReprogramacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.transaction.event.TransactionalEventListener;

/** Un aviso solo se envía mientras siga describiendo al turno. */
class VigenciaAvisoTurnoIT {
    private static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    private static final Instant INICIO = BASE.plus(Duration.ofDays(5));
    private static final Identidad RECEPCION =
            new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);

    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static ConsultaAvisoTurno consulta;
    private static Avisos avisos;

    private Datos datos;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            var generico = (GenericApplicationContext) ctx;
            generico.registerBean("relojVigenciaPruebas", Clock.class,
                    () -> Clock.fixed(BASE, ZoneOffset.UTC), bean -> bean.setPrimary(true));
            generico.registerBean(Avisos.class, Avisos::new);
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        consulta = contexto.getBean(ConsultaAvisoTurno.class);
        avisos = contexto.getBean(Avisos.class);
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

    @BeforeEach
    void preparar() {
        datos = nuevosDatos();
        avisos.recibidos.clear();
    }

    @Test
    void elAvisoRecienPublicadoCorrespondeAlPacienteDelTurno() {
        alta(INICIO);

        assertEquals(Optional.of(datos.paciente()), consulta.pacienteSiSigueVigente(ultimo()));
    }

    @Test
    void confirmarNoDesactualizaElAviso() {
        var turno = alta(INICIO);
        var aviso = ultimo();

        agenda.confirmar(RECEPCION, turno.id(), turno.version());

        assertEquals(Optional.of(datos.paciente()), consulta.pacienteSiSigueVigente(aviso));
    }

    @Test
    void laCancelacionInternaODelPacienteDesactualizaElAviso() {
        var turno = alta(INICIO);
        var interno = ultimo();
        agenda.cancelar(RECEPCION, new SolicitudCancelacion(turno.id(), turno.version()));
        alta(INICIO.plus(Duration.ofHours(2)));
        var porEnlace = ultimo();
        agenda.cancelarPorToken(porEnlace.token());

        assertTrue(consulta.pacienteSiSigueVigente(interno).isEmpty());
        assertTrue(consulta.pacienteSiSigueVigente(porEnlace).isEmpty());
    }

    @Test
    void reprogramarDesactualizaElAvisoAnteriorAunqueVuelvaAlMismoHorario() {
        var turno = alta(INICIO);
        var original = ultimo();
        var movido = mover(turno, INICIO.plus(Duration.ofHours(2)));
        var intermedio = ultimo();
        assertTrue(consulta.pacienteSiSigueVigente(original).isEmpty());

        mover(movido, INICIO);

        assertTrue(consulta.pacienteSiSigueVigente(original).isEmpty());
        assertTrue(consulta.pacienteSiSigueVigente(intermedio).isEmpty());
        assertTrue(consulta.pacienteSiSigueVigente(ultimo()).isPresent());
    }

    @Test
    void unEnlaceReemplazadoDesactualizaElAvisoAunqueElHorarioNoCambie() {
        var turno = alta(INICIO);
        var aviso = ultimo();

        contexto.getBean(ServicioCancelacionPorEnlace.class).emitir(turno.id(), 0, turno.inicio(), BASE);

        assertTrue(consulta.pacienteSiSigueVigente(aviso).isEmpty());
    }

    @Test
    void sinEnlaceSigueVigenteHastaQueCambieElHorario() {
        var turno = alta(BASE.plus(Duration.ofHours(10)));
        var aviso = ultimo();
        assertTrue(consulta.pacienteSiSigueVigente(aviso).isPresent());

        mover(turno, BASE.plus(Duration.ofHours(20)));

        assertTrue(consulta.pacienteSiSigueVigente(aviso).isEmpty());
        assertTrue(consulta.pacienteSiSigueVigente(ultimo()).isPresent());
    }

    private static TurnoProgramado ultimo() {
        return avisos.recibidos.getLast();
    }

    private TurnoRegistrado alta(Instant inicio) {
        return agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(),
                datos.prestacion(), inicio, 30));
    }

    private static TurnoRegistrado mover(TurnoRegistrado turno, Instant inicio) {
        return agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                turno.id(), turno.version(), inicio, turno.profesionalId(), turno.sillonId()));
    }

    private static Datos nuevosDatos() {
        var recursos = contexto.getBean(ServicioRecursosAgenda.class);
        var paciente = contexto.getBean(ServicioPacientes.class).crear(RECEPCION,
                new CrearPaciente(codigo(), "Nombre ficticio", "Apellido ficticio", "turno@example.invalid", null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, codigo(), true).id(),
                recursos.altaSillon(RECEPCION, codigo(), true).id(),
                recursos.altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

    private record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {}

    static class Avisos {
        final List<TurnoProgramado> recibidos = new CopyOnWriteArrayList<>();

        @TransactionalEventListener
        public void recibir(TurnoProgramado aviso) {
            recibidos.add(aviso);
        }
    }
}
