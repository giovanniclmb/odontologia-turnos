package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.AutenticacionFallidaException;
import ar.edu.utn.odontologia.acceso.DatosBootstrap;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.acceso.ServicioBootstrap;
import ar.edu.utn.odontologia.acceso.ServicioCredenciales;
import ar.edu.utn.odontologia.acceso.paciente.DatosRegistroCuentaPaciente;
import ar.edu.utn.odontologia.acceso.paciente.EstadoVinculoCuentaPaciente;
import ar.edu.utn.odontologia.acceso.paciente.ServicioCuentasPaciente;
import ar.edu.utn.odontologia.acceso.paciente.VinculacionCuentaPaciente;
import ar.edu.utn.odontologia.acceso.persistencia.TokenAcceso;
import ar.edu.utn.odontologia.acceso.persistencia.TokensAcceso;
import ar.edu.utn.odontologia.acceso.persistencia.CuentaPaciente;
import ar.edu.utn.odontologia.acceso.persistencia.CuentasPacientes;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

class AccesoPersistenciaIT {
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackages = {"ar.edu.utn.odontologia.acceso.persistencia",
            "ar.edu.utn.odontologia.referencias.persistencia"})
    @EnableJpaRepositories(basePackages = {"ar.edu.utn.odontologia.acceso.persistencia",
            "ar.edu.utn.odontologia.referencias.persistencia"})
    static class JpaConfiguration {
        @org.springframework.context.annotation.Bean
        ServicioCredenciales servicioCredenciales() {
            return new ServicioCredenciales();
        }

        @org.springframework.context.annotation.Bean
        ServicioAutenticacion servicioAutenticacion(Usuarios usuarios, ServicioCredenciales credenciales) {
            return new ServicioAutenticacion(usuarios, credenciales);
        }

        @org.springframework.context.annotation.Bean
        ServicioAutorizacion servicioAutorizacion() {
            return new ServicioAutorizacion();
        }

        @org.springframework.context.annotation.Bean
        ServicioBootstrap servicioBootstrap(Usuarios usuarios, ServicioCredenciales credenciales, ServicioAutorizacion autorizacion) {
            return new ServicioBootstrap(usuarios, credenciales, autorizacion);
        }

        @org.springframework.context.annotation.Bean
        ServicioCuentasPaciente servicioCuentasPaciente(CuentasPacientes cuentas, ServicioCredenciales credenciales,
                                                        PacienteReferencias pacientes) {
            return new ServicioCuentasPaciente(cuentas, credenciales, pacientes);
        }
    }

    @BeforeAll
    static void start() {
        db = PostgresFixture.start();
        try {
            contexto = abrir();
        } catch (RuntimeException e) {
            db.close();
            throw e;
        }
    }

    @AfterAll
    static void stop() {
        if (contexto != null) contexto.close();
        if (db != null) db.close();
    }

    private static ConfigurableApplicationContext abrir() {
        Map<String, Object> props = new HashMap<>(db.properties());
        props.put("spring.datasource.url", db.jdbcUrl());
        props.put("spring.datasource.username", "odonto_app");
        props.put("spring.datasource.password", db.appPassword());
        props.put("spring.jpa.hibernate.ddl-auto", "validate");
        props.put("spring.jpa.properties.hibernate.default_schema", "odontologia");
        props.put("spring.flyway.url", db.jdbcUrl());
        props.put("spring.flyway.user", "odonto_migrator");
        props.put("spring.flyway.password", db.migrationPassword());
        props.put("spring.flyway.schemas", "odontologia");
        props.put("spring.flyway.default-schema", "odontologia");
        props.put("spring.flyway.create-schemas", false);
        props.put("spring.flyway.clean-disabled", true);
        props.put("spring.flyway.out-of-order", false);
        props.put("spring.main.banner-mode", "off");
        var environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        return new SpringApplicationBuilder(JpaConfiguration.class).web(WebApplicationType.NONE).properties("spring.main.web-application-type=none").environment(environment)
                .initializers(c -> c.getEnvironment().getPropertySources().addFirst(new MapPropertySource("postgresTemporal", props)))
                .run("--spring.config.location=classpath:/application.yaml");
    }

    @Test
    void guardaYRecuperaUsuario() {
        Usuarios repo = contexto.getBean(Usuarios.class);
        Usuario u = new Usuario("JPA-" + UUID.randomUUID(), "hash", ar.edu.utn.odontologia.acceso.Rol.RECEPCION, true, null);
        Usuario guardado = repo.save(u);
        assertNotNull(guardado.getId());
        Usuario leido = repo.findById(guardado.getId()).orElseThrow();
        assertEquals(guardado.getId(), leido.getId());
        assertEquals(u.getNombreUsuario(), leido.getNombreUsuario());
    }

    @Test
    void guardaYRecuperaCuentaPacientePendiente() {
        CuentasPacientes cuentas = contexto.getBean(CuentasPacientes.class);
        String email = "paciente-" + UUID.randomUUID() + "@example.invalid";
        CuentaPaciente guardada = cuentas.save(new CuentaPaciente(email, "$2-ficticio"));
        assertNotNull(guardada.getId());
        CuentaPaciente leida = cuentas.findByEmail(email).orElseThrow();
        assertEquals(guardada.getId(), leida.getId());
        assertNull(leida.getPacienteRef());
    }

    @Test
    void cuentaConEmailDeFichaSiguePendienteHastaVinculacionManualYFichaAdmiteUnaCuenta() {
        var servicio = contexto.getBean(ServicioCuentasPaciente.class);
        var pacientes = contexto.getBean(PacienteReferencias.class);
        String email = "vinculo-" + UUID.randomUUID() + "@example.invalid";
        PacienteReferencia paciente = pacientes.save(new PacienteReferencia(
                "C16-" + UUID.randomUUID(), "Paciente", "Ficticio", email, null));

        var pendiente = servicio.registrar(new DatosRegistroCuentaPaciente(email, "PacienteClaveSegura123"));
        assertEquals(EstadoVinculoCuentaPaciente.PENDIENTE, pendiente.estadoVinculo());

        var recepcion = new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
        var vinculada = servicio.confirmarVinculo(recepcion,
                new VinculacionCuentaPaciente(pendiente.cuentaId(), paciente.getId()));
        assertEquals(EstadoVinculoCuentaPaciente.VINCULADA, vinculada.estadoVinculo());
        assertEquals(paciente.getId(), servicio.autenticar(
                new ar.edu.utn.odontologia.acceso.paciente.CredencialesCuentaPaciente(email,
                        "PacienteClaveSegura123")).pacienteId());

        var segundaCuenta = servicio.registrar(new DatosRegistroCuentaPaciente(
                "segunda-" + UUID.randomUUID() + "@example.invalid", "OtraClaveSeguraPaciente123"));
        assertThrows(IllegalArgumentException.class, () -> servicio.confirmarVinculo(recepcion,
                new VinculacionCuentaPaciente(segundaCuenta.cuentaId(), paciente.getId())));
    }

    @Test
    void guardaYRecuperaToken() {
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        Usuario u = usuarios.save(new Usuario("TOKEN-" + UUID.randomUUID(), "hash", ar.edu.utn.odontologia.acceso.Rol.ODONTOLOGO, true, null));
        TokensAcceso tokens = contexto.getBean(TokensAcceso.class);
        TokenAcceso t = tokens.save(new TokenAcceso(u.getId(), "hash-token", Instant.now().plusSeconds(3600)));
        assertNotNull(t.getId());
        TokenAcceso leido = tokens.findById(t.getId()).orElseThrow();
        assertEquals(u.getId(), leido.getUsuarioId());
        assertFalse(leido.isRevocado());
        assertTrue(leido.getHashToken().equals("hash-token"));
    }

    @Test
    void tokenEnClaroNoSeAlmacena() throws SQLException {
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        Usuario u = usuarios.save(new Usuario("HASH-" + UUID.randomUUID(), "hash-credencial", ar.edu.utn.odontologia.acceso.Rol.ODONTOLOGO, true, null));
        TokensAcceso tokens = contexto.getBean(TokensAcceso.class);
        String hashToken = "hash-token-" + UUID.randomUUID();
        tokens.save(new TokenAcceso(u.getId(), hashToken, Instant.now().plusSeconds(3600)));
        try (var c = db.appConnection(); var q = c.prepareStatement("select hash_token from odontologia.token_acceso where usuario_id = ?")) {
            q.setObject(1, u.getId());
            var r = q.executeQuery();
            assertTrue(r.next(), "debe existir fila de token");
            assertEquals(hashToken, r.getString(1));
            assertFalse(r.next(), "no debe haber filas duplicadas inesperadas");
        }
    }

    @Test
    void hashCredencialNoEsTextoPlano() throws SQLException {
        ServicioCredenciales cred = contexto.getBean(ServicioCredenciales.class);
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        String claro = "ClaveSegura123";
        String hash = cred.hash(claro);
        Usuario u = usuarios.save(new Usuario("HASH2-" + UUID.randomUUID(), hash, ar.edu.utn.odontologia.acceso.Rol.RECEPCION, true, null));
        try (var c = db.appConnection(); var q = c.prepareStatement("select hash_credencial from odontologia.usuario where id = ?")) {
            q.setObject(1, u.getId());
            var r = q.executeQuery();
            assertTrue(r.next());
            String almacenado = r.getString(1);
            assertEquals(hash, almacenado);
            assertTrue(almacenado.startsWith("$2"));
            assertNotEquals(claro, almacenado);
        }
    }

    @Test
    void autenticacionRealExitosaYFallida() {
        ServicioCredenciales cred = contexto.getBean(ServicioCredenciales.class);
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        ServicioAutenticacion auth = contexto.getBean(ServicioAutenticacion.class);
        String nombre = "AUTH-" + UUID.randomUUID();
        String claro = "ClaveSegura123";
        usuarios.save(new Usuario(nombre, cred.hash(claro), ar.edu.utn.odontologia.acceso.Rol.RECEPCION, true, null));
        Identidad identidad = auth.autenticar(nombre, claro);
        assertEquals(Rol.RECEPCION, identidad.rol());
        assertThrows(AutenticacionFallidaException.class, () -> auth.autenticar(nombre, "otra-clave"));
        assertThrows(AutenticacionFallidaException.class, () -> auth.autenticar("no-existe-" + UUID.randomUUID(), claro));
    }

    @Test
    void autenticacionRechazaUsuarioInactivo() {
        ServicioCredenciales cred = contexto.getBean(ServicioCredenciales.class);
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        ServicioAutenticacion auth = contexto.getBean(ServicioAutenticacion.class);
        String nombre = "INACTIVO-" + UUID.randomUUID();
        String claro = "ClaveSegura123";
        usuarios.save(new Usuario(nombre, cred.hash(claro), Rol.RECEPCION, false, null));
        assertThrows(AutenticacionFallidaException.class, () -> auth.autenticar(nombre, claro));
    }

    @Test
    void autorizacionEfectivaReal() {
        ServicioCredenciales cred = contexto.getBean(ServicioCredenciales.class);
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        ServicioAutenticacion auth = contexto.getBean(ServicioAutenticacion.class);
        ServicioAutorizacion authz = contexto.getBean(ServicioAutorizacion.class);
        String nombre = "AUTHZ-RECEP-" + UUID.randomUUID();
        String claro = "ClaveSegura123";
        usuarios.save(new Usuario(nombre, cred.hash(claro), Rol.RECEPCION, true, null));
        Identidad recepcion = auth.autenticar(nombre, claro);
        assertTrue(authz.tienePermiso(recepcion, Permiso.FICHA_LEER));
        assertFalse(authz.tienePermiso(recepcion, Permiso.FICHA_EDITAR));
        assertThrows(AutorizacionDenegadaException.class, () -> authz.exigir(recepcion, Permiso.FICHA_EDITAR));
        assertNullIdentidad(authz);
    }

    private void assertNullIdentidad(ServicioAutorizacion authz) {
        assertFalse(authz.tienePermiso(null, Permiso.FICHA_LEER));
        assertThrows(AutorizacionDenegadaException.class, () -> authz.exigir(null, Permiso.FICHA_LEER));
    }

    @Test
    void alcancePorRegistroReal() throws SQLException {
        ServicioCredenciales cred = contexto.getBean(ServicioCredenciales.class);
        Usuarios usuarios = contexto.getBean(Usuarios.class);
        ServicioAutenticacion auth = contexto.getBean(ServicioAutenticacion.class);
        ServicioAutorizacion authz = contexto.getBean(ServicioAutorizacion.class);
        UUID profesional = UUID.randomUUID();
        try (var c = db.appConnection(); var q = c.prepareStatement(
                "insert into odontologia.profesional(id,codigo) values (?,?)")) {
            q.setObject(1, profesional);
            q.setString(2, "PROFESIONAL-FICTICIO-" + profesional);
            q.executeUpdate();
        }
        String claro = "ClaveSegura123";
        String nombreVinculado = "ODONTO-VINC-" + UUID.randomUUID();
        usuarios.save(new Usuario(nombreVinculado, cred.hash(claro), Rol.ODONTOLOGO, true, profesional));
        Identidad odontologoVinculado = auth.autenticar(nombreVinculado, claro);
        assertTrue(authz.puedeConsultarPaciente(odontologoVinculado, profesional));
        assertFalse(authz.puedeConsultarPaciente(odontologoVinculado, UUID.randomUUID()));
        String nombreSinVinculo = "ODONTO-SIN-" + UUID.randomUUID();
        usuarios.save(new Usuario(nombreSinVinculo, cred.hash(claro), Rol.ODONTOLOGO, true, null));
        Identidad odontologoSinVinculo = auth.autenticar(nombreSinVinculo, claro);
        assertFalse(authz.puedeConsultarPaciente(odontologoSinVinculo, profesional));
        String nombreRecepcion = "RECEP-SCOPE-" + UUID.randomUUID();
        usuarios.save(new Usuario(nombreRecepcion, cred.hash(claro), Rol.RECEPCION, true, null));
        Identidad recepcion = auth.autenticar(nombreRecepcion, claro);
        assertTrue(authz.puedeConsultarPaciente(recepcion, profesional));
    }

    @Test
    void migracionNoSeReaplica() throws Exception {
        try (var c = db.migrationConnection(); var q = c.createStatement(); var r = q.executeQuery("select count(*) from odontologia.flyway_schema_history where success")) {
            assertTrue(r.next());
            assertEquals(10, r.getInt(1));
        }
    }
}
