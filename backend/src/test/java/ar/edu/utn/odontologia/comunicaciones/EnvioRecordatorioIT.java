package ar.edu.utn.odontologia.comunicaciones;

import static ar.edu.utn.odontologia.agenda.AgendaConReloj.INICIO;
import static ar.edu.utn.odontologia.agenda.AgendaConReloj.RECEPCION;
import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.agenda.AgendaConReloj;
import ar.edu.utn.odontologia.agenda.ConsultaRecordatorios;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.SolicitudCancelacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda.TurnoRegistrado;
import ar.edu.utn.odontologia.comunicaciones.persistencia.EmailsEnviados;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Revisión de recordatorios con PostgreSQL real, el adaptador real y un servidor local en lugar de Resend: no
 * sale ningún correo.
 */
@ExtendWith(OutputCaptureExtension.class)
class EnvioRecordatorioIT {
    private static final String ANA = "ana@example.invalid";
    private static final String BEA = "bea@example.invalid";
    private static final Instant EN_VENTANA = INICIO.minus(Duration.ofHours(20));
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final List<JsonNode> recibidos = new CopyOnWriteArrayList<>();
    private static final AtomicInteger estadoProveedor = new AtomicInteger(200);
    private static final AtomicReference<String> transporteRotoPara = new AtomicReference<>();
    private static final AtomicReference<Runnable> trasElPrimerEnvio = new AtomicReference<>();

    private static HttpServer proveedor;
    private static AgendaConReloj escenario;
    private static RecordatorioTurnoPorEmail recordatorio;

    @BeforeAll
    static void iniciar() throws Exception {
        proveedor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proveedor.createContext("/", intercambio -> {
            var cuerpo = intercambio.getRequestBody().readAllBytes();
            recibidos.add(JSON.readTree(new String(cuerpo, StandardCharsets.UTF_8)));
            // El rechazo repite el pedido, como podría hacerlo un proveedor.
            intercambio.sendResponseHeaders(estadoProveedor.get(), cuerpo.length);
            try (var salida = intercambio.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        proveedor.start();
        var endpoint = "http://127.0.0.1:" + proveedor.getAddress().getPort() + "/";
        escenario = AgendaConReloj.iniciar(Map.of(
                "email.resend-api-key", "clave-ficticia", "email.from", "consultorio@example.invalid",
                "email.test-allowlist", ANA + "," + BEA, "email.max-retries", "1"), contexto ->
                contexto.registerBean("correoDeRecordatorios", CorreoSalida.class, () -> {
                    var propiedades = contexto.getBean(ConfiguracionEmail.class);
                    var adaptador = new AdaptadorResend(propiedades,
                            new AllowlistEmail(propiedades.testAllowlist()),
                            contexto.getBean(EmailsEnviados.class), HttpClient.newHttpClient(), endpoint);
                    return solicitud -> {
                        // El email de alta de C-19 no forma parte de estas pruebas.
                        if (!solicitud.tipo().startsWith("turno-recordatorio-")) {
                            return new ResultadoEnvio(true, 0, null, null);
                        }
                        if (solicitud.destinatario().equals(transporteRotoPara.get())) {
                            throw new IllegalStateException("falla " + solicitud.destinatario() + solicitud.cuerpo());
                        }
                        var resultado = adaptador.enviar(solicitud);
                        var accion = trasElPrimerEnvio.getAndSet(null);
                        if (accion != null) {
                            accion.run();
                        }
                        return resultado;
                    };
                }, bean -> bean.setPrimary(true)));
        recordatorio = escenario.contexto.getBean(RecordatorioTurnoPorEmail.class);
    }

    @AfterAll
    static void cerrar() {
        try {
            if (escenario != null) escenario.close();
        } finally {
            if (proveedor != null) proveedor.stop(0);
        }
    }

    @BeforeEach
    void preparar() throws SQLException {
        // Cada prueba parte sin turnos: la revisión procesa todos los que estén en la ventana.
        try (var conexion = escenario.db.adminConnection(); var sql = conexion.createStatement()) {
            sql.execute("truncate odontologia.token_cancelacion, odontologia.email_envio, odontologia.turno");
        }
        escenario.reloj.fijar(AgendaConReloj.BASE);
        recibidos.clear();
        estadoProveedor.set(200);
        transporteRotoPara.set(null);
        trasElPrimerEnvio.set(null);
    }

    @Test
    void cadaPacienteRecibeSoloSuRecordatorio() {
        var deAna = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        var deBea = escenario.alta(escenario.nuevosDatos("Bea", BEA), INICIO.plus(Duration.ofHours(1)));
        escenario.reloj.fijar(EN_VENTANA);

        recordatorio.enviarPendientes();

        assertEquals(2, recibidos.size());
        assertEquals("Recordatorio de tu turno", recibidos.getFirst().get("subject").asString());
        assertTrue(textoPara(ANA).startsWith("Hola, Ana:"));
        assertTrue(textoPara(ANA).contains("10/04/2040 a las 07:00"));
        assertTrue(textoPara(BEA).startsWith("Hola, Bea:"));
        assertTrue(textoPara(BEA).contains("10/04/2040 a las 08:00"));
        assertFalse(textoPara(ANA).contains("Apellido"));
        assertFalse(textoPara(ANA).contains("http"));
        assertEquals(List.of(tipo(deAna) + ":sent"), envios(deAna));
        assertEquals(List.of(tipo(deBea) + ":sent"), envios(deBea));
    }

    @Test
    void noEnviaAntesDeLasVeinticuatroHoras() {
        escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(25)));

        recordatorio.enviarPendientes();

        assertTrue(recibidos.isEmpty());
    }

    @Test
    void revisionesRepetidasEnvianUnSoloRecordatorio() {
        var turno = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        escenario.reloj.fijar(EN_VENTANA);

        recordatorio.enviarPendientes();
        recordatorio.enviarPendientes();

        assertEquals(1, recibidos.size());
        assertEquals(List.of(tipo(turno) + ":sent"), envios(turno));
    }

    @Test
    void unProveedorQueRechazaDejaElFalloSinReintentarNiTocarElTurno(CapturedOutput salida) {
        var turno = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        escenario.reloj.fijar(EN_VENTANA);
        estadoProveedor.set(500);

        recordatorio.enviarPendientes();
        estadoProveedor.set(200);
        recordatorio.enviarPendientes();

        assertEquals(2, recibidos.size(), "Un intento y un reintento, solo en la primera revisión");
        assertEquals(List.of(tipo(turno) + ":failed"), envios(turno));
        assertEquals("PENDIENTE:0", estadoYVersion(turno));
        var aviso = "Envío fallido del recordatorio del turno " + turno.id() + ": HTTP 500";
        assertEquals(1, salida.getAll().split(java.util.regex.Pattern.quote(aviso), -1).length - 1);
        assertSinDatos(salida);
    }

    @Test
    void cambiarElEmailDelPacienteNoGeneraOtroRecordatorio() {
        var datos = escenario.nuevosDatos("Ana", ANA);
        escenario.alta(datos, INICIO);
        escenario.reloj.fijar(EN_VENTANA);
        recordatorio.enviarPendientes();

        escenario.jdbc.update("update odontologia.paciente set email = ? where id = ?", BEA, datos.paciente());
        recordatorio.enviarPendientes();

        assertEquals(1, recibidos.size());
    }

    @Test
    void unDestinatarioRechazadoOUnaFallaNoDetienenALosDemas(CapturedOutput salida) {
        var fueraDeLista = escenario.alta(escenario.nuevosDatos("Carla", "carla@example.invalid"), INICIO);
        var roto = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO.plus(Duration.ofHours(1)));
        var sano = escenario.alta(escenario.nuevosDatos("Bea", BEA), INICIO.plus(Duration.ofHours(2)));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));
        transporteRotoPara.set(ANA);

        assertDoesNotThrow(() -> recordatorio.enviarPendientes());

        assertEquals(1, recibidos.size());
        assertTrue(textoPara(BEA).startsWith("Hola, Bea:"));
        assertEquals(List.of(tipo(fueraDeLista) + ":failed"), envios(fueraDeLista));
        assertEquals(List.of(tipo(sano) + ":sent"), envios(sano));
        assertTrue(salida.getAll().contains(
                "No se pudo enviar el recordatorio del turno " + roto.id() + ": IllegalStateException"));
        assertSinDatos(salida);
    }

    @Test
    void unNuevoHorarioRecibeSuPropioRecordatorio() {
        var turno = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        escenario.reloj.fijar(EN_VENTANA);
        recordatorio.enviarPendientes();
        var nuevoInicio = INICIO.plus(Duration.ofDays(3));

        var movido = escenario.mover(turno, nuevoInicio);
        recordatorio.enviarPendientes();
        assertEquals(1, recibidos.size());
        escenario.reloj.fijar(nuevoInicio.minus(Duration.ofHours(24)));
        recordatorio.enviarPendientes();

        assertEquals(2, recibidos.size());
        assertTrue(recibidos.getLast().get("text").asString().contains("13/04/2040 a las 07:00"));
        assertEquals(List.of(tipo(turno) + ":sent", tipo(movido) + ":sent"), envios(turno));
    }

    @Test
    void dosIniciosDelMismoSegundoSonHorariosDistintos() {
        var turno = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(24)));
        recordatorio.enviarPendientes();
        var medioSegundo = Duration.ofMillis(500);

        var movido = escenario.mover(turno, INICIO.plus(medioSegundo));
        escenario.reloj.avanzar(medioSegundo);
        recordatorio.enviarPendientes();

        assertEquals(2, recibidos.size());
        assertEquals(Set.of(tipo(turno) + ":sent", tipo(movido) + ":sent"), Set.copyOf(envios(turno)));
    }

    @Test
    void cambiarSoloElSillonNoRepiteElRecordatorio() {
        var turno = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        escenario.reloj.fijar(EN_VENTANA);
        recordatorio.enviarPendientes();

        escenario.mover(turno, INICIO, escenario.otroSillon());
        recordatorio.enviarPendientes();

        assertEquals(1, recibidos.size());
    }

    @Test
    void recordarNoConfirmaNiModificaElTurnoNiSuEnlace() {
        var turno = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        var enlaceAntes = enlace(turno);
        escenario.reloj.fijar(EN_VENTANA);

        recordatorio.enviarPendientes();

        assertEquals("PENDIENTE:0", estadoYVersion(turno));
        assertTrue(escenario.agenda.consultarPendientes(RECEPCION).stream()
                .anyMatch(pendiente -> pendiente.id().equals(turno.id())));
        assertEquals(enlaceAntes, enlace(turno));
    }

    @Test
    void descartaElRecordatorioDeUnTurnoCanceladoDuranteLaRevision() {
        var primero = escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        var segundo = escenario.alta(escenario.nuevosDatos("Bea", BEA), INICIO.plus(Duration.ofHours(1)));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));
        trasElPrimerEnvio.set(() -> escenario.agenda.cancelar(RECEPCION,
                new SolicitudCancelacion(segundo.id(), segundo.version())));

        recordatorio.enviarPendientes();

        assertEquals(1, recibidos.size());
        assertEquals(List.of(tipo(primero) + ":sent"), envios(primero));
        assertTrue(envios(segundo).isEmpty());
    }

    @Test
    void alCerrarLaAplicacionNoIniciaMasEnviosYLosRestantesQuedanPendientes() {
        escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        var segundo = escenario.alta(escenario.nuevosDatos("Bea", BEA), INICIO.plus(Duration.ofHours(1)));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));
        var queSeCierra = new RecordatorioTurnoPorEmail(
                escenario.contexto.getBean(ConsultaRecordatorios.class),
                escenario.contexto.getBean(EmailsEnviados.class),
                escenario.contexto.getBean(PacienteReferencias.class),
                escenario.contexto.getBean("correoDeRecordatorios", CorreoSalida.class));
        trasElPrimerEnvio.set(queSeCierra::alCerrar);

        queSeCierra.enviarPendientes();

        assertEquals(1, recibidos.size());
        assertTrue(envios(segundo).isEmpty());
        recordatorio.enviarPendientes();
        assertEquals(2, recibidos.size());
    }

    @Test
    void unaInterrupcionDetieneLaRevisionSinMarcarLosRestantes() {
        escenario.alta(escenario.nuevosDatos("Ana", ANA), INICIO);
        var segundo = escenario.alta(escenario.nuevosDatos("Bea", BEA), INICIO.plus(Duration.ofHours(1)));
        escenario.reloj.fijar(INICIO.minus(Duration.ofHours(10)));
        trasElPrimerEnvio.set(() -> Thread.currentThread().interrupt());

        try {
            recordatorio.enviarPendientes();
        } finally {
            assertTrue(Thread.interrupted(), "La interrupción se conserva para quien llamó");
        }

        assertEquals(1, recibidos.size());
        assertTrue(envios(segundo).isEmpty());
    }

    private static String tipo(TurnoRegistrado turno) {
        return "turno-recordatorio-" + turno.inicio();
    }

    private static String textoPara(String destinatario) {
        return recibidos.stream()
                .filter(email -> email.get("to").get(0).asString().equals(destinatario))
                .findFirst().orElseThrow().get("text").asString();
    }

    private static List<String> envios(TurnoRegistrado turno) {
        return escenario.jdbc.queryForList("""
                select tipo || ':' || estado from odontologia.email_envio
                where turno_id = ? and tipo like 'turno-recordatorio-%' order by tipo
                """, String.class, turno.id());
    }

    private static String estadoYVersion(TurnoRegistrado turno) {
        return escenario.jdbc.queryForObject(
                "select estado || ':' || version from odontologia.turno where id = ?", String.class, turno.id());
    }

    private static String enlace(TurnoRegistrado turno) {
        return escenario.jdbc.queryForObject("""
                select to_jsonb(t)::text from odontologia.token_cancelacion t where turno_id = ?
                """, String.class, turno.id());
    }

    private static void assertSinDatos(CapturedOutput salida) {
        for (var dato : List.of(ANA, BEA, "carla@example.invalid", "Hola, ", "Te recordamos", "clave-ficticia")) {
            assertFalse(salida.getAll().contains(dato), "El registro no debe contener: " + dato);
        }
    }
}
