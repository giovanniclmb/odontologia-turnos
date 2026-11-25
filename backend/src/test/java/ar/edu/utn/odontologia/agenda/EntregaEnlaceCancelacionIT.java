package ar.edu.utn.odontologia.agenda;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
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
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/** Entrega interna del token de cancelación para C-19, sin envío de email. */
@ExtendWith(OutputCaptureExtension.class)
class EntregaEnlaceCancelacionIT {
    private static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    private static final Instant INICIO = BASE.plus(Duration.ofDays(5));
    private static final Identidad RECEPCION =
            new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);

    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static ServicioAgenda agenda;
    private static Consumidor consumidor;
    private static TransactionTemplate transacciones;

    private Datos datos;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            var generico = (GenericApplicationContext) ctx;
            generico.registerBean("relojEntregaPruebas", Clock.class,
                    () -> Clock.fixed(BASE, ZoneOffset.UTC), bean -> bean.setPrimary(true));
            generico.registerBean(Consumidor.class, Consumidor::new);
        }).run();
        agenda = contexto.getBean(ServicioAgenda.class);
        consumidor = contexto.getBean(Consumidor.class);
        transacciones = new TransactionTemplate(contexto.getBean(PlatformTransactionManager.class));
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
        consumidor.reiniciar();
    }

    @Test
    void entregaUnaVezElTokenDelAltaDespuesDelCommit() {
        var turno = transacciones.execute(transaccion -> {
            var creado = alta(INICIO);
            assertTrue(consumidor.recibidos.isEmpty(), "No debe entregarse antes del commit");
            return creado;
        });

        assertEquals(1, consumidor.recibidos.size());
        var enlace = consumidor.recibidos.getFirst();
        assertEquals(turno.id(), enlace.turnoId());
        assertEquals(turno.id(), agenda.cancelarPorToken(enlace.token()).id());
    }

    @Test
    void entregaUnTokenNuevoAlReprogramarYElAnteriorQuedaRechazado() {
        var turno = alta(INICIO);
        var anterior = consumidor.recibidos.getFirst().token();
        consumidor.reiniciar();

        mover(turno, INICIO.plus(Duration.ofHours(2)));

        var nuevo = consumidor.recibidos.getFirst().token();
        assertEquals(1, consumidor.recibidos.size());
        assertNotEquals(anterior, nuevo);
        assertThrows(TokenCancelacionInvalidoException.class, () -> agenda.cancelarPorToken(anterior));
        assertEquals(EstadoTurno.CANCELADO, agenda.cancelarPorToken(nuevo).estado());
    }

    @Test
    void noEntregaCuandoLaTransaccionRevierte() {
        transacciones.executeWithoutResult(transaccion -> {
            alta(INICIO);
            transaccion.setRollbackOnly();
        });

        assertTrue(consumidor.recibidos.isEmpty());
    }

    @Test
    void noEntregaConLaVentanaCerrada() {
        alta(BASE.plus(Duration.ofHours(47)));

        assertTrue(consumidor.recibidos.isEmpty());
    }

    @Test
    void confirmarNoEntregaOtroToken() {
        var turno = alta(INICIO);
        consumidor.reiniciar();

        agenda.confirmar(RECEPCION, turno.id(), turno.version());

        assertTrue(consumidor.recibidos.isEmpty());
    }

    @Test
    void unFalloDelConsumidorNoAfectaElAltaNiRegistraElToken(CapturedOutput salida) {
        consumidor.fallar = true;

        var turno = assertDoesNotThrow(() -> alta(INICIO));

        assertEquals(1, consumidor.recibidos.size());
        assertEquals(List.of(turno), agenda.consultarAgendaSillon(RECEPCION, datos.sillon(), INICIO, turno.fin()));
        assertTrue(salida.getAll().contains("Fallo simulado del consumidor"), "El fallo debe quedar registrado");
        assertFalse(salida.getAll().contains(consumidor.recibidos.getFirst().token()));
    }

    @Test
    void elTextoDelEventoNoContieneElToken() {
        var texto = new EnlaceCancelacionEmitido(UUID.randomUUID(), "valor-secreto-ficticio").toString();

        assertFalse(texto.contains("valor-secreto-ficticio"), texto);
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

    /** Hace el papel de C-19. */
    static class Consumidor {
        final List<EnlaceCancelacionEmitido> recibidos = new CopyOnWriteArrayList<>();
        volatile boolean fallar;

        @TransactionalEventListener
        public void recibir(EnlaceCancelacionEmitido enlace) {
            recibidos.add(enlace);
            if (fallar) throw new IllegalStateException("Fallo simulado del consumidor");
        }

        void reiniciar() {
            recibidos.clear();
            fallar = false;
        }
    }
}
