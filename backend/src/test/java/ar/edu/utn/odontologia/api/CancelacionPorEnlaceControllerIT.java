package ar.edu.utn.odontologia.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudAlta;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.agenda.ServicioCancelacionPorEnlace;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes.CrearPaciente;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas de integracion del contrato HTTP de C-18 contra PostgreSQL real (Testcontainers).
 * Cubre GET no mutante, POST cancelacion, respuestas genericas sin filtracion y ausencia de bypass.
 */
class CancelacionPorEnlaceControllerIT {
    private static final Instant BASE = Instant.parse("2040-04-05T10:00:00Z");
    private static final long CINCO_DIAS_SEGUNDOS = 5L * 24 * 3600;
    private static final Instant INICIO = BASE.plusSeconds(CINCO_DIAS_SEGUNDOS);
    private static final Identidad RECEPCION = identidad(Rol.RECEPCION, null);

    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;
    private static MockMvc mockMvc;
    private static ServicioAgenda agenda;
    private static ServicioCancelacionPorEnlace cancelacion;
    private static ServicioPacientes pacientes;
    private static ServicioRecursosAgenda recursos;
    private static JdbcTemplate jdbc;

    private Datos datos;
    private TurnoRegistrado turno;

    @BeforeAll
    static void iniciar() {
        db = PostgresFixture.start();
        contexto = new SpringApplicationBuilder(OdontologiaApplication.class).initializers(ctx -> {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgres-fixture", db.properties()));
            ((GenericApplicationContext) ctx).registerBean("relojCancelacionPruebas", Clock.class,
                    () -> Clock.systemUTC(), bean -> bean.setPrimary(true));
        }).run();
        mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) contexto).build();
        agenda = contexto.getBean(ServicioAgenda.class);
        cancelacion = contexto.getBean(ServicioCancelacionPorEnlace.class);
        pacientes = contexto.getBean(ServicioPacientes.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
        jdbc = contexto.getBean(JdbcTemplate.class);
        contexto.getBean(ServicioConfiguracionSede.class).establecerIntervalo(RECEPCION, 10);
    }

    @AfterAll
    static void cerrar() {
        try {
            if (contexto != null) {
                contexto.close();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @BeforeEach
    void preparar() {
        datos = nuevosDatos();
        turno = alta(datos, INICIO, 30);
    }

    /* ==================== Task 4: contrato HTTP ==================== */

    @Test
    void postCancelaTurnoYDevuelveRespuestaSinDatosClinicos() throws Exception {
        var crudo = tokenDe(turno);
        var antes = versionDe(turno);

        var resultado = mockMvc.perform(post("/cancelacion/{token}", crudo)).andReturn();
        assertEquals(200, resultado.getResponse().getStatus());
        assertTrue(resultado.getResponse().getContentAsString().contains("turno_cancelado"));

        var estado = estadoDe(turno);
        assertEquals("CANCELADO", estado.estado().name());
        assertNotEquals(antes, estado.version());
        assertTrue(tokenConsumido(turno.id()), "El token debe estar consumido");
    }

    @Test
    void getEsNoMutanteYNoCancelaNiConsume() throws Exception {
        var crudo = tokenDe(turno);

        var resultado = mockMvc.perform(get("/cancelacion/{token}", crudo)).andReturn();
        assertEquals(200, resultado.getResponse().getStatus());
        assertTrue(resultado.getResponse().getContentAsString().contains("cancelacion_disponible"));

        var estado = estadoDe(turno);
        assertEquals("PENDIENTE", estado.estado().name(), "GET no debe cancelar el turno");
        assertFalse(tokenConsumido(turno.id()), "GET no debe consumir el token");
    }

    @Test
    void postTokenDesconocidoRespondeGenericoSinFiltrarExistencia() throws Exception {
        var resultado = mockMvc.perform(post("/cancelacion/{token}", "token-inexistente")).andReturn();
        assertEquals(404, resultado.getResponse().getStatus());
        assertTrue(resultado.getResponse().getContentAsString().contains("enlace_no_disponible"));
    }

    @Test
    void postTokenConsumidoRespondeGenerico() throws Exception {
        var crudo = tokenDe(turno);
        mockMvc.perform(post("/cancelacion/{token}", crudo)).andExpect(status().isOk());

        var repetida = mockMvc.perform(post("/cancelacion/{token}", crudo)).andReturn();
        assertEquals(404, repetida.getResponse().getStatus());
        assertTrue(repetida.getResponse().getContentAsString().contains("enlace_no_disponible"));
    }

    @Test
    void getTokenInvalidoRespondeGenerico() throws Exception {
        var resultado = mockMvc.perform(get("/cancelacion/{token}", "token-inexistente")).andReturn();
        assertEquals(404, resultado.getResponse().getStatus());
        assertTrue(resultado.getResponse().getContentAsString().contains("enlace_no_disponible"));
    }

    /* ==================== Task 5.2: no hay bypass de autorizacion interna ==================== */

    @Test
    void funcionaSinIdentidadNiSesionElEndpointEsPublicoPorToken() throws Exception {
        var crudo = tokenDe(turno);
        // No se envia ninguna identidad, cookie de sesion ni credencial interna: solo el token.
        mockMvc.perform(post("/cancelacion/{token}", crudo)).andExpect(status().isOk());
        assertEquals("CANCELADO", estadoDe(turno).estado().name());
    }

    /* ==================== helpers ==================== */

    private String tokenDe(TurnoRegistrado t) {
        return cancelacion.emitir(t.id(), versionDe(t), t.inicio(), BASE);
    }

    private long versionDe(TurnoRegistrado t) {
        return jdbc.queryForObject("select version from odontologia.turno where id = ?", Long.class, t.id());
    }

    private boolean tokenConsumido(UUID turnoId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select consumido from odontologia.token_cancelacion where turno_id = ?",
                Boolean.class, turnoId));
    }

    private TurnoRegistrado estadoDe(TurnoRegistrado t) {
        return agenda.consultarAgendaSillon(RECEPCION, t.sillonId(), t.inicio(), t.fin())
                .stream().filter(vista -> vista.id().equals(t.id())).findFirst().orElseThrow();
    }

    private static TurnoRegistrado alta(Datos d, Instant inicio, Integer duracion) {
        return agenda.crear(RECEPCION, new SolicitudAlta(d.paciente(), d.profesional(), d.sillon(),
                d.prestacion(), inicio, duracion));
    }

    private static Datos nuevosDatos() {
        var paciente = pacientes.crear(RECEPCION,
                new CrearPaciente(codigo(), "Nombre ficticio", "Apellido ficticio", "turno@example.invalid", null)).id();
        return new Datos(paciente, recursos.altaProfesional(RECEPCION, codigo(), true).id(),
                recursos.altaSillon(RECEPCION, codigo(), true).id(),
                recursos.altaPrestacion(RECEPCION, codigo(), 30).id());
    }

    private static String codigo() {
        return UUID.randomUUID().toString();
    }

    private static Identidad identidad(Rol rol, UUID profesional) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio", rol, profesional);
    }

    private record Datos(UUID paciente, UUID profesional, UUID sillon, UUID prestacion) {}
}
