package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Contratos HTTP reales: filtro, controladores, servicios y PostgreSQL sin mocks. */
class ApiPacientesIT {
    private static ApiC11Fixture fixture;
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    @BeforeAll static void iniciar() { fixture = new ApiC11Fixture(); }
    @AfterAll static void cerrar() { if (fixture != null) fixture.close(); }

    @Test void loginUsaRolesDelFrontendYRechazaCredencialesSin500() throws Exception {
        assertEquals("recepcion", login("recepcion-demo").get("rol").asString());
        assertEquals("odontologo", login("odonto-demo").get("rol").asString());
        var rechazo = request("POST", "/auth/login", null,
                Map.of("nombreUsuario", "recepcion-demo", "contrasena", "incorrecta"));
        assertEquals(401, rechazo.statusCode());
        assertEquals("autenticacion_fallida", json(rechazo).get("codigo").asString());
        assertFalse(rechazo.body().contains("incorrecta"));
    }

    @Test void preflightPermiteSoloOrigenConfigurado() throws Exception {
        var allowed = preflight("http://localhost:5173");
        assertTrue(allowed.statusCode() >= 200 && allowed.statusCode() < 300);
        assertEquals("http://localhost:5173", allowed.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
        var denied = preflight("https://otro.example.invalid");
        assertEquals(403, denied.statusCode());
        assertTrue(denied.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    @Test void tokenInvalidoExponeEl401SoloAlOrigenConfigurado() throws Exception {
        var permitido = conToken("token-inexistente", "http://localhost:5173");
        assertEquals(401, permitido.statusCode());
        assertEquals("no_autenticado", json(permitido).get("codigo").asString());
        assertEquals("http://localhost:5173",
                permitido.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
        var ajeno = conToken("token-inexistente", "https://otro.example.invalid");
        assertEquals(403, ajeno.statusCode());
        assertTrue(ajeno.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    @Test @ExtendWith(OutputCaptureExtension.class)
    void fallaInternaAlResolverElTokenResponde500SinDetallesYQuedaRegistrada(CapturedOutput salida) throws Exception {
        String token = login("recepcion-demo").get("token").asString();
        HttpResponse<String> falla;
        sql("REVOKE SELECT ON odontologia.token_acceso FROM odonto_app");
        try {
            falla = conToken(token, "http://localhost:5173");
        } finally {
            sql("GRANT SELECT ON odontologia.token_acceso TO odonto_app");
        }
        assertEquals(500, falla.statusCode(), falla.body());
        assertEquals("http://localhost:5173",
                falla.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
        var cuerpo = json(falla);
        assertEquals(3, cuerpo.size(), falla.body());
        assertEquals("error_interno", cuerpo.get("codigo").asString());
        assertEquals("No se pudo procesar la solicitud", cuerpo.get("mensaje").asString());
        String correlacion = UUID.fromString(cuerpo.get("correlacion").asString()).toString();
        assertTrue(salida.getAll().contains(correlacion), "La falla debe quedar registrada con su id");
        assertFalse(salida.getAll().contains(token), "El registro no debe incluir el token");
    }

    @Test void tokenVencidoOdeUsuarioInactivoSigueRespondiendo401() throws Exception {
        String vencido = login("recepcion-demo").get("token").asString();
        sql("UPDATE odontologia.token_acceso SET expira_en = now() - interval '1 minute'");
        assertEquals(401, request("GET", "/pacientes", vencido, null).statusCode());
        String deInactivo = login("colega-demo").get("token").asString();
        sql("UPDATE odontologia.usuario SET activo = false WHERE nombre_usuario = 'colega-demo'");
        try {
            assertEquals(401, request("GET", "/pacientes", deInactivo, null).statusCode());
        } finally {
            sql("UPDATE odontologia.usuario SET activo = true WHERE nombre_usuario = 'colega-demo'");
        }
    }

    @Test void recepcionLeePeroNoEditaClinicaYOdontologoNoEditaNotaAjena() throws Exception {
        String recepcion = login("recepcion-demo").get("token").asString();
        String odonto = login("odonto-demo").get("token").asString();
        String ruta = "/pacientes/" + fixture.paciente + "/ficha";
        var lectura = request("GET", ruta, recepcion, null);
        assertEquals(200, lectura.statusCode());
        assertEquals(2, json(lectura).get("notas").size());
        assertEquals(403, request("PUT", ruta + "/antecedentes", recepcion,
                Map.of("antecedentes", "No permitido")).statusCode());
        assertEquals(403, request("PUT", ruta + "/notas/" + fixture.notaAjena, odonto,
                Map.of("texto", "No permitido")).statusCode());
        assertEquals(200, request("PUT", ruta + "/antecedentes", odonto,
                Map.of("antecedentes", "Antecedentes ficticios actualizados.")).statusCode());
        assertEquals(400, request("POST", ruta + "/notas", odonto, Map.of("texto", " ")).statusCode());
    }

    @Test void altaBusquedaEdicionYAlcancePasanPorIdentidadAutenticada() throws Exception {
        String recepcion = login("recepcion-demo").get("token").asString();
        String odonto = login("odonto-demo").get("token").asString();
        var creada = request("POST", "/pacientes", recepcion, Map.of(
                "codigo", "PAC-HTTP-NUEVO", "nombre", "Bea", "apellido", "Ficticia", "email", "bea@example.invalid"));
        assertEquals(200, creada.statusCode());
        String id = json(creada).get("id").asString();
        assertEquals(1, json(request("GET", "/pacientes?codigo=PAC-HTTP-NUEVO", recepcion, null)).size());
        assertEquals(0, json(request("GET", "/pacientes?codigo=PAC-HTTP-NUEVO", odonto, null)).size());
        assertEquals(403, request("GET", "/pacientes/" + id, odonto, null).statusCode());
        assertEquals(200, request("PUT", "/pacientes/" + id, recepcion, Map.of(
                "nombre", "Beatriz", "apellido", "Ficticia", "email", "bea@example.invalid")).statusCode());
        assertEquals(400, request("POST", "/pacientes", recepcion, Map.of("codigo", "")).statusCode());
    }

    @Test void logoutRevocaTokenReal() throws Exception {
        String token = login("recepcion-demo").get("token").asString();
        assertEquals(200, request("GET", "/pacientes", token, null).statusCode());
        assertEquals(204, request("POST", "/auth/logout", token, null).statusCode());
        assertEquals(401, request("GET", "/pacientes", token, null).statusCode());
    }

    private static JsonNode login(String usuario) throws Exception {
        var response = request("POST", "/auth/login", null,
                Map.of("nombreUsuario", usuario, "contrasena", ApiC11Fixture.CLAVE));
        assertEquals(200, response.statusCode(), response.body());
        return json(response);
    }
    private static JsonNode json(HttpResponse<String> response) { return JSON.readTree(response.body()); }
    private static HttpResponse<String> request(String method, String path, String token, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(fixture.base + path)).timeout(Duration.ofSeconds(15));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private static void sql(String sentencia) throws Exception {
        try (var conexion = fixture.conexionAdmin(); var comando = conexion.createStatement()) {
            comando.execute(sentencia);
        }
    }
    private static HttpResponse<String> conToken(String token, String origin) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(fixture.base + "/pacientes"))
                .timeout(Duration.ofSeconds(10)).header("Origin", origin)
                .header("Authorization", "Bearer " + token).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
    private static HttpResponse<String> preflight(String origin) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(fixture.base + "/pacientes"))
                .timeout(Duration.ofSeconds(10)).header("Origin", origin)
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
    }
}
