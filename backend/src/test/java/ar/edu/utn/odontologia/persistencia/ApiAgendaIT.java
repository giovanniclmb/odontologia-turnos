package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import ar.edu.utn.odontologia.agenda.ServicioAgenda;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Contrato HTTP de agenda, bloqueos, sede y catálogos sobre PostgreSQL real. Cada prueba usa un día propio. */
class ApiAgendaIT {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static ApiC11Fixture fixture;
    private static Identidad recepcion;
    private static ServicioRecursosAgenda recursos;
    private static ServicioConfiguracionSede sede;
    private static JdbcTemplate jdbc;
    private static String tokenRecepcion;
    private static String tokenOdontologia;

    @BeforeAll
    static void iniciar() throws Exception {
        fixture = new ApiC11Fixture();
        recepcion = fixture.context.getBean(ServicioAutenticacion.class)
                .autenticar("recepcion-demo", ApiC11Fixture.CLAVE);
        recursos = fixture.context.getBean(ServicioRecursosAgenda.class);
        sede = fixture.context.getBean(ServicioConfiguracionSede.class);
        jdbc = fixture.context.getBean(JdbcTemplate.class);
        tokenRecepcion = login("recepcion-demo");
        tokenOdontologia = login("odonto-demo");
    }

    @AfterAll
    static void cerrar() {
        if (fixture != null) fixture.close();
    }

    @BeforeEach
    void intervaloConocido() {
        sede.establecerIntervalo(recepcion, 10);
    }

    @Test
    void ningunaOperacionNuevaRespondeSinIdentidad() throws Exception {
        String sillon = nuevoSillon().toString();
        String periodo = "desde=2090-01-01T00:00:00Z&hasta=2090-01-02T00:00:00Z";
        for (var operacion : List.of(
                new String[] {"GET", "/sede/intervalo"},
                new String[] {"PUT", "/sede/intervalo"},
                new String[] {"GET", "/recursos/sillones"},
                new String[] {"GET", "/recursos/prestaciones"},
                new String[] {"GET", "/agenda/sillones/" + sillon + "/turnos?" + periodo},
                new String[] {"POST", "/agenda/turnos"},
                new String[] {"GET", "/agenda/bloqueos?sillonId=" + sillon + "&" + periodo},
                new String[] {"POST", "/agenda/bloqueos"})) {
            var respuesta = request(operacion[0], operacion[1], null,
                    operacion[0].equals("GET") ? null : Map.of("minutos", 99));
            assertEquals(401, respuesta.statusCode(), operacion[0] + " " + operacion[1]);
            assertEquals("no_autenticado", json(respuesta).get("codigo").asString());
        }
        assertEquals(10, sede.consultarIntervalo(recepcion).orElseThrow());
    }

    @Test
    void ambosRolesConsultanSillonesYPrestaciones() throws Exception {
        UUID sillon = nuevoSillon();
        UUID prestacion = nuevaPrestacion(45);
        for (String token : List.of(tokenRecepcion, tokenOdontologia)) {
            var sillones = ok(request("GET", "/recursos/sillones", token, null));
            var elegido = porId(sillones, sillon);
            assertTrue(elegido.get("activo").asBoolean());
            assertFalse(elegido.get("codigo").asString().isBlank());
            var prestaciones = ok(request("GET", "/recursos/prestaciones", token, null));
            assertEquals(45, porId(prestaciones, prestacion).get("duracionReferenciaMinutos").asInt());
        }
    }

    @Test
    void soloRecepcionEstableceElIntervaloYAmbosLoConsultan() throws Exception {
        jdbc.update("update odontologia.configuracion_sede set intervalo_minutos = null where fila = 1");
        var sinConfigurar = ok(request("GET", "/sede/intervalo", tokenRecepcion, null));
        assertTrue(sinConfigurar.has("minutos"));
        assertTrue(sinConfigurar.get("minutos").isNull());

        assertEquals(15, ok(request("PUT", "/sede/intervalo", tokenRecepcion, Map.of("minutos", 15)))
                .get("minutos").asInt());
        assertEquals(15, ok(request("GET", "/sede/intervalo", tokenOdontologia, null)).get("minutos").asInt());

        error(request("PUT", "/sede/intervalo", tokenOdontologia, Map.of("minutos", 20)),
                403, "autorizacion_denegada");
        for (String cuerpo : List.of("{\"minutos\":0}", "{\"minutos\":-5}", "{\"minutos\":7.5}",
                "{\"minutos\":null}", "{}", "{\"minutos\":\"diez\"}", "{\"minutos\":99999999999}",
                "null", "", "{mal")) {
            var respuesta = enviar("PUT", "/sede/intervalo", tokenRecepcion, cuerpo);
            error(respuesta, 400, "solicitud_invalida");
        }
        assertEquals(15, sede.consultarIntervalo(recepcion).orElseThrow());
    }

    @Test
    void altaValidaQuedaPendienteYLaAgendaExponeSoloElNombreDelPaciente() throws Exception {
        UUID sillon = nuevoSillon();
        var cuerpo = alta(fixture.paciente, fixture.profesional, sillon, nuevaPrestacion(30), "2090-02-01T12:00:00Z");

        var respuesta = request("POST", "/agenda/turnos", tokenRecepcion, cuerpo);

        assertEquals(201, respuesta.statusCode(), respuesta.body());
        var creado = json(respuesta);
        assertEquals("PENDIENTE", creado.get("estado").asString());
        assertEquals("2090-02-01T12:30:00Z", creado.get("fin").asString());
        var ajustado = new LinkedHashMap<>(alta(fixture.paciente, fixture.profesional, sillon,
                nuevaPrestacion(30), "2090-02-01T15:00:00Z"));
        ajustado.put("duracionAjustadaMinutos", 45);
        var segundo = request("POST", "/agenda/turnos", tokenRecepcion, ajustado);
        assertEquals(201, segundo.statusCode(), segundo.body());
        assertEquals(45, json(segundo).get("duracionMinutos").asInt());

        var agenda = ok(consultarTurnos(tokenRecepcion, sillon, "2090-02-01T00:00:00Z", "2090-02-02T00:00:00Z"));
        assertEquals(2, agenda.size());
        var turno = agenda.get(0);
        assertEquals(creado.get("id").asString(), turno.get("id").asString());
        assertEquals("Ana", turno.get("pacienteNombre").asString());
        assertEquals("Ejemplo", turno.get("pacienteApellido").asString());
        assertEquals(Set.of("id", "pacienteNombre", "pacienteApellido", "profesionalId", "sillonId", "prestacionId",
                "inicio", "fin", "duracionMinutos", "estado", "version"), campos(turno));
    }

    @Test
    void conflictosDeRecursoIntervaloYBloqueoDanElMismoErrorGenerico() throws Exception {
        UUID sillon = nuevoSillon();
        UUID otroSillon = nuevoSillon();
        UUID profesional = nuevoProfesional();
        UUID otroProfesional = nuevoProfesional();
        UUID prestacion = nuevaPrestacion(30);
        var ocupante = request("POST", "/agenda/turnos", tokenRecepcion,
                alta(fixture.paciente, profesional, sillon, prestacion, "2090-03-01T12:00:00Z"));
        assertEquals(201, ocupante.statusCode(), ocupante.body());
        var bloqueo = request("POST", "/agenda/bloqueos", tokenRecepcion, Map.of("sillonId", sillon,
                "inicio", "2090-03-01T18:00:00Z", "fin", "2090-03-01T19:00:00Z"));
        assertEquals(201, bloqueo.statusCode(), bloqueo.body());

        for (var intento : List.of(
                alta(fixture.paciente, profesional, otroSillon, prestacion, "2090-03-01T12:15:00Z"),
                alta(fixture.paciente, otroProfesional, sillon, prestacion, "2090-03-01T12:15:00Z"),
                alta(fixture.paciente, profesional, otroSillon, prestacion, "2090-03-01T12:35:00Z"),
                alta(fixture.paciente, otroProfesional, sillon, prestacion, "2090-03-01T18:15:00Z"))) {
            var respuesta = request("POST", "/agenda/turnos", tokenRecepcion, intento);
            error(respuesta, 409, "horario_no_disponible");
            assertFalse(respuesta.body().contains(json(ocupante).get("id").asString()));
            assertFalse(respuesta.body().contains(json(bloqueo).get("id").asString()));
        }
        assertEquals(1, cantidadTurnos(sillon));
        assertEquals(0, cantidadTurnos(otroSillon));
    }

    @Test
    void altaSinIntervaloConfiguradoLoIdentificaComoConfiguracionFaltante() throws Exception {
        UUID sillon = nuevoSillon();
        var cuerpo = alta(fixture.paciente, fixture.profesional, sillon, nuevaPrestacion(30), "2090-04-01T12:00:00Z");
        jdbc.update("update odontologia.configuracion_sede set intervalo_minutos = null where fila = 1");

        error(request("POST", "/agenda/turnos", tokenRecepcion, cuerpo), 409, "intervalo_sin_configurar");

        assertEquals(0, cantidadTurnos(sillon));
    }

    @Test
    void altaRechazaDatosInvalidosSinPersistir() throws Exception {
        UUID sillon = nuevoSillon();
        var valida = alta(fixture.paciente, fixture.profesional, sillon, nuevaPrestacion(30), "2090-05-01T12:00:00Z");
        for (var cambio : List.<Map.Entry<String, Object>>of(
                Map.entry("duracionAjustadaMinutos", 30.5),
                Map.entry("duracionAjustadaMinutos", 0),
                Map.entry("inicio", "2090-05-01 12:00"),
                Map.entry("inicio", "+999999-01-01T00:00:00Z"),
                Map.entry("inicio", "2001-01-01T12:00:00Z"),
                Map.entry("pacienteId", "no-es-un-uuid"))) {
            var cuerpo = new LinkedHashMap<String, Object>(valida);
            cuerpo.put(cambio.getKey(), cambio.getValue());
            error(request("POST", "/agenda/turnos", tokenRecepcion, cuerpo), 400, "solicitud_invalida");
        }
        var sinInicio = new LinkedHashMap<String, Object>(valida);
        sinInicio.remove("inicio");
        error(request("POST", "/agenda/turnos", tokenRecepcion, sinInicio), 400, "solicitud_invalida");
        error(enviar("POST", "/agenda/turnos", tokenRecepcion, "null"), 400, "solicitud_invalida");
        var pacienteInexistente = new LinkedHashMap<String, Object>(valida);
        pacienteInexistente.put("pacienteId", UUID.randomUUID());
        error(request("POST", "/agenda/turnos", tokenRecepcion, pacienteInexistente), 404, "solicitud_invalida");
        assertEquals(0, cantidadTurnos(sillon));
    }

    @Test
    void ningunAltaPuedeTerminarFueraDelRangoDeFechasAdmitido() throws Exception {
        UUID sillon = nuevoSillon();
        var valida = alta(fixture.paciente, fixture.profesional, sillon, nuevaPrestacion(30), "2090-10-01T12:00:00Z");
        var desbordada = new LinkedHashMap<String, Object>(valida);
        desbordada.put("inicio", "7000-01-01T12:00:00Z");
        desbordada.put("duracionAjustadaMinutos", Integer.MAX_VALUE);
        error(request("POST", "/agenda/turnos", tokenRecepcion, desbordada), 400, "solicitud_invalida");
        var alLimite = new LinkedHashMap<String, Object>(valida);
        alLimite.put("inicio", "9999-12-31T23:59:00Z");
        error(request("POST", "/agenda/turnos", tokenRecepcion, alLimite), 400, "solicitud_invalida");
        assertEquals(0, cantidadTurnos(sillon));
    }

    @Test
    void unaPrestacionInexistenteSeInformaComoReferenciaInexistente() throws Exception {
        UUID sillon = nuevoSillon();
        var cuerpo = alta(fixture.paciente, fixture.profesional, sillon, UUID.randomUUID(), "2090-11-01T12:00:00Z");

        error(request("POST", "/agenda/turnos", tokenRecepcion, cuerpo), 404, "solicitud_invalida");

        assertEquals(0, cantidadTurnos(sillon));
    }

    @Test
    void unTokenInvalidoSeRechazaEnLasOperacionesDeAgenda() throws Exception {
        for (String ruta : List.of("/sede/intervalo", "/recursos/sillones", "/recursos/prestaciones")) {
            error(request("GET", ruta, "token-inexistente", null), 401, "no_autenticado");
        }
        error(request("POST", "/agenda/turnos", "token-inexistente", Map.of()), 401, "no_autenticado");
    }

    @Test
    void lasConsultasExigenUnPeriodoValidoDeHastaSieteDias() throws Exception {
        UUID sillon = nuevoSillon();
        for (String ruta : List.of("/agenda/sillones/" + sillon + "/turnos?",
                "/agenda/bloqueos?sillonId=" + sillon + "&")) {
            assertEquals(200, request("GET", ruta + "desde=2090-06-01T00:00:00Z&hasta=2090-06-08T00:00:00Z",
                    tokenRecepcion, null).statusCode());
            for (String periodo : List.of(
                    "desde=2090-06-01T00:00:00Z&hasta=2090-06-08T00:00:01Z",
                    "desde=2090-06-01T00:00:00Z&hasta=2090-06-01T00:00:00Z",
                    "desde=2090-06-02T00:00:00Z&hasta=2090-06-01T00:00:00Z",
                    "desde=ayer&hasta=2090-06-01T00:00:00Z",
                    "desde=-999999-01-01T00:00:00Z&hasta=-999999-01-02T00:00:00Z",
                    "desde=2090-06-01T00:00:00Z")) {
                error(request("GET", ruta + periodo, tokenRecepcion, null), 400, "solicitud_invalida");
            }
        }
        error(request("GET", "/agenda/sillones/abc/turnos?desde=2090-06-01T00:00:00Z&hasta=2090-06-02T00:00:00Z",
                tokenRecepcion, null), 400, "solicitud_invalida");
        error(request("GET", "/agenda/bloqueos?desde=2090-06-01T00:00:00Z&hasta=2090-06-02T00:00:00Z",
                tokenRecepcion, null), 400, "solicitud_invalida");
    }

    @Test
    void odontologiaCreaSoloEnSuAlcanceYVeSoloSusTurnos() throws Exception {
        UUID sillon = nuevoSillon();
        UUID prestacion = nuevaPrestacion(30);
        UUID otroProfesional = nuevoProfesional();
        UUID sinAsignar = fixture.context.getBean(ServicioPacientes.class).crear(recepcion,
                new ServicioPacientes.CrearPaciente(UUID.randomUUID().toString(), "Elena", "Demo",
                        "elena@example.invalid", null)).id();
        var propio = request("POST", "/agenda/turnos", tokenOdontologia,
                alta(fixture.paciente, fixture.profesional, sillon, prestacion, "2090-07-01T12:00:00Z"));
        assertEquals(201, propio.statusCode(), propio.body());
        error(request("POST", "/agenda/turnos", tokenOdontologia,
                alta(fixture.paciente, otroProfesional, sillon, prestacion, "2090-07-01T14:00:00Z")),
                403, "autorizacion_denegada");
        error(request("POST", "/agenda/turnos", tokenOdontologia,
                alta(sinAsignar, fixture.profesional, sillon, prestacion, "2090-07-01T16:00:00Z")),
                403, "autorizacion_denegada");
        assertEquals(201, request("POST", "/agenda/turnos", tokenRecepcion,
                alta(sinAsignar, otroProfesional, sillon, prestacion, "2090-07-01T18:00:00Z")).statusCode());

        var respuesta = consultarTurnos(tokenOdontologia, sillon, "2090-07-01T00:00:00Z", "2090-07-02T00:00:00Z");

        var agenda = ok(respuesta);
        assertEquals(1, agenda.size());
        assertEquals(json(propio).get("id").asString(), agenda.get(0).get("id").asString());
        assertFalse(respuesta.body().contains("Elena"));
    }

    @Test
    void recepcionBloqueaYOdontologiaSoloConsultaSinReferenciasAjenas() throws Exception {
        UUID sillon = nuevoSillon();
        UUID ajeno = nuevoProfesional();
        var cuerpo = Map.of("sillonId", sillon, "profesionalId", ajeno,
                "inicio", "2090-08-01T12:00:00Z", "fin", "2090-08-01T13:00:00Z");
        error(request("POST", "/agenda/bloqueos", tokenOdontologia, cuerpo), 403, "autorizacion_denegada");

        var creado = request("POST", "/agenda/bloqueos", tokenRecepcion, cuerpo);

        assertEquals(201, creado.statusCode(), creado.body());
        String ruta = "/agenda/bloqueos?sillonId=" + sillon
                + "&desde=2090-08-01T00:00:00Z&hasta=2090-08-02T00:00:00Z";
        var paraRecepcion = ok(request("GET", ruta, tokenRecepcion, null));
        assertEquals(1, paraRecepcion.size());
        assertEquals(ajeno.toString(), paraRecepcion.get(0).get("profesionalId").asString());
        var respuesta = request("GET", ruta, tokenOdontologia, null);
        var paraOdontologia = ok(respuesta);
        assertEquals(1, paraOdontologia.size());
        assertEquals(sillon.toString(), paraOdontologia.get(0).get("sillonId").asString());
        assertTrue(paraOdontologia.get(0).get("profesionalId").isNull());
        assertFalse(respuesta.body().contains(ajeno.toString()));
        assertEquals(Set.of("id", "inicio", "fin", "sillonId", "profesionalId"), campos(paraOdontologia.get(0)));
    }

    @Test
    void bloqueoRechazaTurnoOcupanteReferenciasYSolicitudesInvalidas() throws Exception {
        UUID sillon = nuevoSillon();
        assertEquals(201, request("POST", "/agenda/turnos", tokenRecepcion, alta(fixture.paciente,
                nuevoProfesional(), sillon, nuevaPrestacion(30), "2090-09-01T12:00:00Z")).statusCode());
        error(request("POST", "/agenda/bloqueos", tokenRecepcion, Map.of("sillonId", sillon,
                "inicio", "2090-09-01T12:15:00Z", "fin", "2090-09-01T13:00:00Z")), 409, "bloqueo_afecta_turno");
        error(request("POST", "/agenda/bloqueos", tokenRecepcion,
                Map.of("inicio", "2090-09-01T15:00:00Z", "fin", "2090-09-01T16:00:00Z")), 400, "solicitud_invalida");
        error(request("POST", "/agenda/bloqueos", tokenRecepcion, Map.of("sillonId", sillon,
                "inicio", "2090-09-01T16:00:00Z", "fin", "2090-09-01T16:00:00Z")), 400, "solicitud_invalida");
        error(request("POST", "/agenda/bloqueos", tokenRecepcion, Map.of("sillonId", sillon,
                "inicio", "2090-09-01T16:00:00Z", "fin", "+999999-01-01T00:00:00Z")), 400, "solicitud_invalida");
        error(request("POST", "/agenda/bloqueos", tokenRecepcion, Map.of("sillonId", UUID.randomUUID(),
                "inicio", "2090-09-01T15:00:00Z", "fin", "2090-09-01T16:00:00Z")), 404, "solicitud_invalida");
        error(enviar("POST", "/agenda/bloqueos", tokenRecepcion, "null"), 400, "solicitud_invalida");
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from odontologia.bloqueo_recurso where sillon_id = ?", Integer.class, sillon));
    }

    @Test
    void recepcionReprogramaUnTurnoConfirmadoConservandoPrestacionYDuracion() throws Exception {
        UUID origen = nuevoSillon();
        UUID destino = nuevoSillon();
        UUID profesional = nuevoProfesional();
        UUID prestacion = nuevaPrestacion(30);
        var solicitud = new LinkedHashMap<>(alta(fixture.paciente, fixture.profesional, origen,
                prestacion, "2090-12-01T12:00:00.123456Z"));
        solicitud.put("duracionAjustadaMinutos", 45);
        JsonNode turno = crearTurno(solicitud);
        UUID id = UUID.fromString(turno.get("id").asString());
        String version = fixture.context.getBean(ServicioAgenda.class)
                .confirmar(recepcion, id, turno.get("version").asString()).version();

        JsonNode movido = ok(reprogramar(tokenRecepcion, id.toString(), version,
                "2090-12-02T13:00:00.123456Z", profesional, destino));

        assertEquals(id.toString(), movido.get("id").asString());
        assertEquals(destino.toString(), movido.get("sillonId").asString());
        assertEquals(profesional.toString(), movido.get("profesionalId").asString());
        assertEquals(prestacion.toString(), movido.get("prestacionId").asString());
        assertEquals(45, movido.get("duracionMinutos").asInt());
        assertEquals("2090-12-02T13:45:00.123456Z", movido.get("fin").asString());
        assertEquals("PENDIENTE", movido.get("estado").asString());
        assertNotEquals(version, movido.get("version").asString());
        assertEquals(Set.of("id", "profesionalId", "sillonId", "prestacionId",
                "inicio", "fin", "duracionMinutos", "estado", "version"), campos(movido));

        assertTrue(ok(consultarTurnos(tokenRecepcion, origen, "2090-12-01T00:00:00Z",
                "2090-12-02T00:00:00Z")).isEmpty());
        JsonNode vigente = porId(ok(consultarTurnos(tokenRecepcion, destino, "2090-12-02T00:00:00Z",
                "2090-12-03T00:00:00Z")), id);
        assertEquals("Ana", vigente.get("pacienteNombre").asString());
        assertEquals(movido.get("version"), vigente.get("version"));
    }

    @Test
    void recepcionConservaVersionYEstadoDeConfirmadoSinMovimiento() throws Exception {
        UUID sillon = nuevoSillon();
        UUID profesional = nuevoProfesional();
        String inicio = "2090-12-03T12:00:00.987654Z";
        JsonNode turno = crearTurno(alta(fixture.paciente, profesional, sillon, nuevaPrestacion(30), inicio));
        UUID id = UUID.fromString(turno.get("id").asString());
        String version = fixture.context.getBean(ServicioAgenda.class)
                .confirmar(recepcion, id, turno.get("version").asString()).version();

        JsonNode sinCambios = ok(reprogramar(tokenRecepcion, id.toString(), version,
                "2090-12-03T12:00:00.987654999Z", profesional, sillon));

        assertEquals("CONFIRMADO", sinCambios.get("estado").asString());
        assertEquals(version, sinCambios.get("version").asString());
        assertEquals(inicio, sinCambios.get("inicio").asString());
    }

    @Test
    void reprogramacionRechazadaConservaVersionHorarioYOcupacion() throws Exception {
        UUID origen = nuevoSillon();
        UUID destino = nuevoSillon();
        UUID profesional = nuevoProfesional();
        UUID prestacion = nuevaPrestacion(30);
        String inicio = "2090-12-04T12:00:00Z";
        String inicioOcupado = "2090-12-04T14:00:00Z";
        JsonNode turno = crearTurno(alta(fixture.paciente, profesional, origen, prestacion, inicio));
        String id = turno.get("id").asString();
        String version = turno.get("version").asString();
        JsonNode ocupante = crearTurno(alta(fixture.paciente, nuevoProfesional(), destino, prestacion, inicioOcupado));

        var respuesta = reprogramar(tokenRecepcion, id, version, inicioOcupado, profesional, destino);
        error(respuesta, 409, "horario_no_disponible");
        assertFalse(respuesta.body().contains(ocupante.get("id").asString()));
        var nuevoTurnoEnOrigen = alta(fixture.paciente, profesional, origen, prestacion, inicio);
        error(request("POST", "/agenda/turnos", tokenRecepcion, nuevoTurnoEnOrigen), 409, "horario_no_disponible");
        jdbc.update("update odontologia.configuracion_sede set intervalo_minutos = null where fila = 1");
        error(reprogramar(tokenRecepcion, id, version, "2090-12-04T16:00:00Z", profesional, destino),
                409, "intervalo_sin_configurar");

        JsonNode vigente = porId(ok(consultarTurnos(tokenRecepcion, origen, "2090-12-04T00:00:00Z",
                "2090-12-05T00:00:00Z")), UUID.fromString(id));
        assertEquals(inicio, vigente.get("inicio").asString());
        assertEquals(version, vigente.get("version").asString());
    }

    @Test
    void reprogramacionDistingueVersionObsoletaDeSolicitudInvalidaYSinSesion() throws Exception {
        UUID sillon = nuevoSillon();
        UUID profesional = nuevoProfesional();
        String destino = "2090-12-05T13:00:00Z";
        JsonNode turno = crearTurno(alta(fixture.paciente, profesional, sillon,
                nuevaPrestacion(25), "2090-12-05T12:00:00Z"));
        String id = turno.get("id").asString();
        String version = turno.get("version").asString();

        error(reprogramar(tokenRecepcion, id, "anterior", destino, profesional, sillon),
                409, "version_turno_desactualizada");
        error(reprogramar(tokenRecepcion, id, "", destino, profesional, sillon), 400, "solicitud_invalida");
        error(reprogramar(tokenRecepcion, id, 0, destino, profesional, sillon), 400, "solicitud_invalida");
        error(reprogramar(tokenRecepcion, "id-malformado", version, destino, profesional, sillon),
                400, "solicitud_invalida");
        error(reprogramar(tokenRecepcion, id, version, "2090-12-05 13:00", profesional, sillon),
                400, "solicitud_invalida");
        error(reprogramar(null, id, version, destino, profesional, sillon), 401, "no_autenticado");
        error(reprogramar("token-invalido", id, version, destino, profesional, sillon), 401, "no_autenticado");
        error(reprogramar(tokenRecepcion, UUID.randomUUID().toString(), version, destino, profesional, sillon),
                404, "solicitud_invalida");
    }

    @Test
    void reprogramacionRechazaFinFueraDeRangoAntesDeCambiarElTurno() throws Exception {
        UUID sillon = nuevoSillon();
        UUID profesional = nuevoProfesional();
        var solicitud = new LinkedHashMap<>(alta(fixture.paciente, profesional, sillon,
                nuevaPrestacion(30), "2090-12-06T12:00:00Z"));
        solicitud.put("duracionAjustadaMinutos", 2_000_000);
        JsonNode turno = crearTurno(solicitud);
        String id = turno.get("id").asString();
        String version = turno.get("version").asString();

        error(reprogramar(tokenRecepcion, id, version, "9998-12-31T20:00:00Z", profesional, sillon),
                400, "solicitud_invalida");
        JsonNode vigente = porId(ok(consultarTurnos(tokenRecepcion, sillon, "2090-12-06T00:00:00Z",
                "2090-12-07T00:00:00Z")), UUID.fromString(id));
        assertEquals("2090-12-06T12:00:00Z", vigente.get("inicio").asString());
        assertEquals(version, vigente.get("version").asString());
    }

    @Test
    void odontologiaReprogramaSuTurnoAsignadoYRecibeDenegacionGenericaFueraDeAlcance() throws Exception {
        UUID sillon = nuevoSillon();
        UUID otroProfesional = nuevoProfesional();
        UUID prestacion = nuevaPrestacion(30);
        UUID sinAsignar = pacienteSinAsignacion();
        JsonNode propio = crearTurno(alta(fixture.paciente, fixture.profesional, sillon, prestacion,
                "2090-12-10T12:00:00Z"));
        JsonNode sinPermiso = crearTurno(alta(sinAsignar, fixture.profesional, sillon, prestacion,
                "2090-12-10T14:00:00Z"));
        JsonNode ajeno = crearTurno(alta(fixture.paciente, otroProfesional, sillon, prestacion,
                "2090-12-10T16:00:00Z"));
        String inicio = "2090-12-10T18:00:00Z";

        for (JsonNode turno : List.of(ajeno, sinPermiso)) {
            error(reprogramar(tokenOdontologia, turno.get("id").asString(), turno.get("version").asString(),
                    inicio, fixture.profesional, sillon), 403, "autorizacion_denegada");
        }
        String id = propio.get("id").asString();
        String version = propio.get("version").asString();
        error(reprogramar(tokenOdontologia, UUID.randomUUID().toString(), version,
                inicio, fixture.profesional, sillon), 403, "autorizacion_denegada");
        error(reprogramar(tokenOdontologia, id, version, inicio, otroProfesional, sillon),
                403, "autorizacion_denegada");
        JsonNode movido = ok(reprogramar(tokenOdontologia, id, version, inicio, fixture.profesional, sillon));
        assertEquals(inicio, movido.get("inicio").asString());
        assertEquals(fixture.profesional.toString(), movido.get("profesionalId").asString());
    }

    @Test
    void recepcionCancelaUnConfirmadoYElHorarioOcupadoPuedeVolverAReservarse() throws Exception {
        UUID sillon = nuevoSillon();
        UUID profesional = nuevoProfesional();
        UUID prestacion = nuevaPrestacion(30);
        String inicio = "2090-12-07T12:00:00Z";
        var solicitud = alta(fixture.paciente, profesional, sillon, prestacion, inicio);
        JsonNode turno = crearTurno(solicitud);
        UUID id = UUID.fromString(turno.get("id").asString());
        String version = fixture.context.getBean(ServicioAgenda.class)
                .confirmar(recepcion, id, turno.get("version").asString()).version();
        error(request("POST", "/agenda/turnos", tokenRecepcion, solicitud), 409, "horario_no_disponible");

        JsonNode cancelado = ok(cancelar(tokenRecepcion, id.toString(), version));

        assertEquals(id.toString(), cancelado.get("id").asString());
        assertEquals("CANCELADO", cancelado.get("estado").asString());
        assertEquals(inicio, cancelado.get("inicio").asString());
        assertEquals("2090-12-07T12:30:00Z", cancelado.get("fin").asString());
        assertEquals(prestacion.toString(), cancelado.get("prestacionId").asString());
        assertNotEquals(version, cancelado.get("version").asString());
        assertFalse(cancelado.has("pacienteId"));
        JsonNode nuevo = crearTurno(solicitud);
        JsonNode agenda = ok(consultarTurnos(tokenRecepcion, sillon, "2090-12-07T00:00:00Z",
                "2090-12-08T00:00:00Z"));
        assertEquals(2, agenda.size());
        var vigente = porId(agenda, id);
        assertEquals("CANCELADO", vigente.get("estado").asString());
        assertEquals("Ana", vigente.get("pacienteNombre").asString());
        assertEquals(cancelado.get("version"), vigente.get("version"));
        assertEquals("PENDIENTE", porId(agenda, UUID.fromString(nuevo.get("id").asString())).get("estado").asString());
    }

    @Test
    void cancelacionDistingueVersionObsoletaDeRepeticionSinAlterarElResultado() throws Exception {
        UUID sillon = nuevoSillon();
        String inicio = "2090-12-08T12:00:00Z";
        JsonNode turno = crearTurno(alta(fixture.paciente, nuevoProfesional(), sillon, nuevaPrestacion(30), inicio));
        String id = turno.get("id").asString();
        String version = turno.get("version").asString();
        JsonNode cancelado = ok(cancelar(tokenRecepcion, id, version));
        String versionCancelada = cancelado.get("version").asString();

        error(cancelar(tokenRecepcion, id, version), 409, "version_turno_desactualizada");
        var repetido = cancelar(tokenRecepcion, id, versionCancelada);
        error(repetido, 400, "solicitud_invalida");
        assertEquals("El turno ya está cancelado", json(repetido).get("mensaje").asString());

        JsonNode vigente = porId(ok(consultarTurnos(tokenRecepcion, sillon, "2090-12-08T00:00:00Z",
                "2090-12-09T00:00:00Z")), UUID.fromString(id));
        assertEquals("CANCELADO", vigente.get("estado").asString());
        assertEquals(versionCancelada, vigente.get("version").asString());
    }

    @Test
    void cancelacionRechazaTurnoIniciadoYValidaElContratoHttp() throws Exception {
        UUID sillon = nuevoSillon();
        JsonNode turno = crearTurno(alta(fixture.paciente, nuevoProfesional(), sillon,
                nuevaPrestacion(30), "2090-12-09T12:00:00Z"));
        String id = turno.get("id").asString();
        String version = turno.get("version").asString();
        error(cancelar(null, id, version), 401, "no_autenticado");
        error(cancelar("token-invalido", id, version), 401, "no_autenticado");
        error(cancelar(tokenRecepcion, "id-malformado", version), 400, "solicitud_invalida");
        error(cancelar(tokenRecepcion, id, 0), 400, "solicitud_invalida");
        error(cancelar(tokenRecepcion, id, ""), 400, "solicitud_invalida");
        error(request("POST", rutaTurno(id, "cancelacion"), tokenRecepcion, Map.of()), 400, "solicitud_invalida");
        error(cancelar(tokenRecepcion, UUID.randomUUID().toString(), version), 404, "solicitud_invalida");
        jdbc.update("""
                update odontologia.turno
                   set inicio = timestamptz '2001-01-01 12:00:00+00',
                       fin = timestamptz '2001-01-01 12:30:00+00'
                 where id = ?
                """, UUID.fromString(id));
        error(cancelar(tokenRecepcion, id, version), 400, "solicitud_invalida");

        JsonNode vigente = porId(ok(consultarTurnos(tokenRecepcion, sillon, "2001-01-01T00:00:00Z",
                "2001-01-02T00:00:00Z")), UUID.fromString(id));
        assertEquals("PENDIENTE", vigente.get("estado").asString());
        assertEquals(version, vigente.get("version").asString());
    }

    @Test
    void odontologiaCancelaSoloSusTurnosPendientesConAsignacionActiva() throws Exception {
        UUID sillon = nuevoSillon();
        UUID prestacion = nuevaPrestacion(30);
        UUID otroProfesional = nuevoProfesional();
        JsonNode propio = crearTurno(alta(fixture.paciente, fixture.profesional, sillon, prestacion,
                "2090-12-11T12:00:00Z"));
        JsonNode sinPermiso = crearTurno(alta(pacienteSinAsignacion(), fixture.profesional, sillon, prestacion,
                "2090-12-11T14:00:00Z"));
        JsonNode ajeno = crearTurno(alta(fixture.paciente, otroProfesional, sillon, prestacion,
                "2090-12-11T16:00:00Z"));
        JsonNode propioConfirmado = crearTurno(alta(fixture.paciente, fixture.profesional, sillon, prestacion,
                "2090-12-11T18:00:00Z"));

        for (JsonNode fueraDeAlcance : List.of(ajeno, sinPermiso)) {
            error(cancelar(tokenOdontologia, fueraDeAlcance.get("id").asString(),
                    fueraDeAlcance.get("version").asString()), 403, "autorizacion_denegada");
        }
        UUID confirmadoId = UUID.fromString(propioConfirmado.get("id").asString());
        String versionConfirmada = fixture.context.getBean(ServicioAgenda.class)
                .confirmar(recepcion, confirmadoId, propioConfirmado.get("version").asString()).version();
        error(cancelar(tokenOdontologia, confirmadoId.toString(), versionConfirmada), 400, "solicitud_invalida");
        error(cancelar(tokenOdontologia, UUID.randomUUID().toString(), propio.get("version").asString()),
                403, "autorizacion_denegada");
        JsonNode cancelado = ok(cancelar(tokenOdontologia, propio.get("id").asString(), propio.get("version").asString()));
        assertEquals("CANCELADO", cancelado.get("estado").asString());
    }

    private static HttpResponse<String> cancelar(String token, String id, Object version) throws Exception {
        return request("POST", rutaTurno(id, "cancelacion"), token, Map.of("versionEsperada", version));
    }

    private static UUID pacienteSinAsignacion() {
        return fixture.context.getBean(ServicioPacientes.class).crear(recepcion,
                new ServicioPacientes.CrearPaciente(UUID.randomUUID().toString(), "Elena", "Demo",
                        "elena@example.invalid", null)).id();
    }

    private static JsonNode crearTurno(Map<String, Object> solicitud) throws Exception {
        var respuesta = request("POST", "/agenda/turnos", tokenRecepcion, solicitud);
        assertEquals(201, respuesta.statusCode(), respuesta.body());
        return json(respuesta);
    }

    private static HttpResponse<String> reprogramar(String token, String id, Object version,
                                                    String inicio, UUID profesional, UUID sillon) throws Exception {
        return request("POST", rutaTurno(id, "reprogramacion"), token, Map.of("versionEsperada", version,
                "inicio", inicio, "profesionalId", profesional, "sillonId", sillon));
    }

    private static String rutaTurno(String id, String operacion) {
        return "/agenda/turnos/" + id + "/" + operacion;
    }

    private static Map<String, Object> alta(UUID paciente, UUID profesional, UUID sillon, UUID prestacion,
                                           String inicio) {
        return Map.of("pacienteId", paciente, "profesionalId", profesional, "sillonId", sillon,
                "prestacionId", prestacion, "inicio", inicio);
    }

    private static UUID nuevoSillon() {
        return recursos.altaSillon(recepcion, UUID.randomUUID().toString(), true).id();
    }

    private static UUID nuevoProfesional() {
        return recursos.altaProfesional(recepcion, UUID.randomUUID().toString(), true).id();
    }

    private static UUID nuevaPrestacion(int minutos) {
        return recursos.altaPrestacion(recepcion, UUID.randomUUID().toString(), minutos).id();
    }

    private static int cantidadTurnos(UUID sillon) {
        return jdbc.queryForObject("select count(*) from odontologia.turno where sillon_id = ?", Integer.class, sillon);
    }

    private static HttpResponse<String> consultarTurnos(String token, UUID sillon, String desde, String hasta)
            throws Exception {
        return request("GET", "/agenda/sillones/" + sillon + "/turnos?desde=" + desde + "&hasta=" + hasta,
                token, null);
    }

    private static JsonNode porId(JsonNode lista, UUID id) {
        for (JsonNode elemento : lista) {
            if (elemento.get("id").asString().equals(id.toString())) return elemento;
        }
        return fail("No se encontró " + id);
    }

    private static Set<String> campos(JsonNode nodo) {
        return nodo.propertyStream().map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    private static JsonNode ok(HttpResponse<String> respuesta) {
        assertEquals(200, respuesta.statusCode(), respuesta.body());
        return json(respuesta);
    }

    private static void error(HttpResponse<String> respuesta, int estado, String codigo) {
        assertEquals(estado, respuesta.statusCode(), respuesta.body());
        assertEquals(codigo, json(respuesta).get("codigo").asString(), respuesta.body());
        assertEquals(Set.of("codigo", "mensaje"), campos(json(respuesta)));
    }

    private static String login(String usuario) throws Exception {
        return ok(request("POST", "/auth/login", null,
                Map.of("nombreUsuario", usuario, "contrasena", ApiC11Fixture.CLAVE))).get("token").asString();
    }

    private static JsonNode json(HttpResponse<String> respuesta) {
        return JSON.readTree(respuesta.body());
    }

    private static HttpResponse<String> request(String metodo, String ruta, String token, Object cuerpo)
            throws Exception {
        return enviar(metodo, ruta, token, cuerpo == null ? null : JSON.writeValueAsString(cuerpo));
    }

    private static HttpResponse<String> enviar(String metodo, String ruta, String token, String cuerpo)
            throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(fixture.base + ruta)).timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo));
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
