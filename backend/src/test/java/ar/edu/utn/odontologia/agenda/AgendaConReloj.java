package ar.edu.utn.odontologia.agenda;

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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

/** Aplicación con PostgreSQL real, datos ficticios y un reloj que la prueba mueve. */
public final class AgendaConReloj implements AutoCloseable {
    public static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    public static final Instant INICIO = BASE.plus(Duration.ofDays(5));
    public static final Identidad RECEPCION =
            new Identidad(UUID.randomUUID(), "usuario-ficticio", Rol.RECEPCION, null);

    public final PostgresFixture db;
    public final ConfigurableApplicationContext contexto;
    public final ServicioAgenda agenda;
    public final JdbcTemplate jdbc;
    public final Reloj reloj;

    private AgendaConReloj(PostgresFixture db, ConfigurableApplicationContext contexto, Reloj reloj) {
        this.db = db;
        this.contexto = contexto;
        this.reloj = reloj;
        agenda = contexto.getBean(ServicioAgenda.class);
        jdbc = contexto.getBean(JdbcTemplate.class);
    }

    public static AgendaConReloj iniciar() {
        return iniciar(Map.of());
    }

    public static AgendaConReloj iniciar(Map<String, Object> propiedades) {
        return iniciar(propiedades, contexto -> {});
    }

    public static AgendaConReloj iniciar(Map<String, Object> propiedades, Consumer<GenericApplicationContext> beans) {
        var db = PostgresFixture.start();
        var reloj = new Reloj();
        try {
            var contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
                var fuentes = ctx.getEnvironment().getPropertySources();
                fuentes.addFirst(new MapPropertySource("postgres-fixture", db.properties()));
                fuentes.addFirst(new MapPropertySource("prueba", propiedades));
                ((GenericApplicationContext) ctx).registerBean("relojMovibleDePruebas", Clock.class,
                        () -> reloj, bean -> bean.setPrimary(true));
                beans.accept((GenericApplicationContext) ctx);
            }).run();
            contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(RECEPCION, 10);
            return new AgendaConReloj(db, contexto, reloj);
        } catch (RuntimeException | Error falla) {
            db.close();
            throw falla;
        }
    }

    public Datos nuevosDatos() {
        return nuevosDatos("Nombre ficticio", "turno@example.invalid");
    }

    public Datos nuevosDatos(String nombre, String email) {
        var recursos = contexto.getBean(ServicioRecursosAgenda.class);
        var paciente = contexto.getBean(ServicioPacientes.class).crear(RECEPCION,
                new CrearPaciente(codigo(), nombre, "Apellido ficticio", email, null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, codigo(), true).id(), otroSillon(),
                recursos.altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    public UUID otroSillon() {
        return contexto.getBean(ServicioRecursosAgenda.class).altaSillon(RECEPCION, codigo(), true).id();
    }

    public TurnoRegistrado alta(Datos datos, Instant inicio) {
        return agenda.crear(RECEPCION, new SolicitudAlta(datos.paciente(), datos.profesional(), datos.sillon(),
                datos.prestacion(), inicio, 30));
    }

    public TurnoRegistrado mover(TurnoRegistrado turno, Instant inicio) {
        return mover(turno, inicio, turno.sillonId());
    }

    public TurnoRegistrado mover(TurnoRegistrado turno, Instant inicio, UUID sillon) {
        return agenda.reprogramar(RECEPCION, new SolicitudReprogramacion(
                turno.id(), turno.version(), inicio, turno.profesionalId(), sillon));
    }

    @Override
    public void close() {
        try {
            contexto.close();
        } finally {
            db.close();
        }
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

    public record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {}

    public static final class Reloj extends Clock {
        private volatile Instant ahora = BASE;

        public void fijar(Instant instante) {
            ahora = instante;
        }

        public void avanzar(Duration paso) {
            ahora = ahora.plus(paso);
        }

        @Override
        public Instant instant() {
            return ahora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            throw new UnsupportedOperationException("Las pruebas usan UTC");
        }
    }
}
