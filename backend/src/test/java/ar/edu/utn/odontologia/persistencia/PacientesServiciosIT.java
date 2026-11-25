package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.pacientes.ServicioAsignacionPacientes;
import ar.edu.utn.odontologia.pacientes.ServicioFichaClinica;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencias;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class PacientesServiciosIT {
    private static PostgresFixture db;
    private static ConfigurableApplicationContext contexto;

    @BeforeAll
    static void iniciarBaseYAplicacion() {
        db = PostgresFixture.start();
        try {
            Map<String, Object> propiedades = new HashMap<>(db.properties());
            propiedades.put("spring.main.banner-mode", "off");
            var entorno = new StandardEnvironment();
            entorno.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            entorno.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            contexto = new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE).properties("spring.main.web-application-type=none")
                    .environment(entorno)
                    .initializers(c -> c.getEnvironment().getPropertySources()
                            .addFirst(new MapPropertySource("postgresTemporal", propiedades)))
                    .run("--spring.config.location=classpath:/application.yaml");
        } catch (RuntimeException | Error failure) {
            db.close();
            throw failure;
        }
    }

    @AfterAll
    static void cerrar() {
        if (contexto != null) contexto.close();
        if (db != null) db.close();
    }

    @Test
    void pacientesSeBuscanPorCodigoYNombreSinExponerClinicaYRespetanAlcance() {
        var recepcion = identidad(Rol.RECEPCION, null);
        UUID profesionalId = profesional("PRO-BUSQUEDA");
        var odontologo = identidad(Rol.ODONTOLOGO, profesionalId);
        var servicio = contexto.getBean(ServicioPacientes.class);
        var asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        var asignado = servicio.crear(recepcion, crear("PAC-BUSQUEDA-A", "Ana", "Ejemplo", "mismo@example.invalid"));
        var sinAsignar = servicio.crear(odontologo, crear("PAC-BUSQUEDA-B", "Analia", "Ficticia", "mismo@example.invalid"));
        asignaciones.asignar(recepcion, asignado.id(), profesionalId);

        var porCodigo = servicio.buscar(recepcion,
                new ServicioPacientes.FiltroBusquedaPaciente("PAC-BUSQUEDA-A", null));
        var porNombre = servicio.buscar(recepcion,
                new ServicioPacientes.FiltroBusquedaPaciente(null, "eJeMpLo"));
        var soloAsignados = servicio.buscar(odontologo,
                new ServicioPacientes.FiltroBusquedaPaciente(null, "ana"));
        var noAsignadoVisible = servicio.buscar(odontologo,
                new ServicioPacientes.FiltroBusquedaPaciente("PAC-BUSQUEDA-B", null));

        assertEquals(List.of(asignado.id()), porCodigo.stream().map(ServicioPacientes.ResumenPaciente::id).toList());
        assertTrue(porNombre.stream().anyMatch(p -> p.id().equals(asignado.id())));
        assertEquals(List.of(asignado.id()), soloAsignados.stream().map(ServicioPacientes.ResumenPaciente::id).toList());
        assertTrue(noAsignadoVisible.isEmpty());
        assertNotEquals(asignado.id(), sinAsignar.id());
        assertTrue(servicio.consultar(recepcion, sinAsignar.id()).telefono() == null);
        assertTrue(java.util.Arrays.stream(ServicioPacientes.ResumenPaciente.class.getRecordComponents())
                .noneMatch(c -> c.getName().contains("nota") || c.getName().contains("antecedente")));
    }

    @Test
    void asignacionSePersisteDesactivaYReactivaElAccesoPorRegistro() {
        var recepcion = identidad(Rol.RECEPCION, null);
        UUID profesionalId = profesional("PRO-ASIGNACION");
        var odontologo = identidad(Rol.ODONTOLOGO, profesionalId);
        var pacientes = contexto.getBean(ServicioPacientes.class);
        var asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        var paciente = pacientes.crear(recepcion,
                crear("PAC-ASIGNACION", "Beto", "Prueba", "beto@example.invalid"));

        assertThrows(ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException.class,
                () -> pacientes.consultar(odontologo, paciente.id()));
        assertTrue(asignaciones.asignar(recepcion, paciente.id(), profesionalId).activa());
        assertEquals(paciente.id(), pacientes.consultar(odontologo, paciente.id()).id());
        assertFalse(asignaciones.desasignar(recepcion, paciente.id(), profesionalId).activa());
        assertThrows(ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException.class,
                () -> pacientes.consultar(odontologo, paciente.id()));
        assertTrue(asignaciones.asignar(recepcion, paciente.id(), profesionalId).activa());
        assertEquals(paciente.id(), pacientes.consultar(odontologo, paciente.id()).id());
    }

    @Test
    void fichaDiferidaNotasFirmadasYConcurrenciaPostgresReal() throws Exception {
        var recepcion = identidad(Rol.RECEPCION, null);
        UUID profesionalId = profesional("PRO-CLINICA");
        UUID segundoProfesionalId = profesional("PRO-CLINICA-2");
        var odontologo = odontologoConUsuario(profesionalId, "odontologo-clinico");
        var otroOdontologo = odontologoConUsuario(segundoProfesionalId, "odontologo-clinico-2");
        var pacientes = contexto.getBean(ServicioPacientes.class);
        var asignaciones = contexto.getBean(ServicioAsignacionPacientes.class);
        var clinica = contexto.getBean(ServicioFichaClinica.class);
        var paciente = pacientes.crear(recepcion,
                crear("PAC-CLINICA", "Celia", "Ficticia", "celia@example.invalid"));
        asignaciones.asignar(recepcion, paciente.id(), profesionalId);
        asignaciones.asignar(recepcion, paciente.id(), segundoProfesionalId);

        assertTrue(clinica.consultar(recepcion, paciente.id()).notas().isEmpty());
        var antecedentes = clinica.actualizarAntecedentes(odontologo, paciente.id(), "Alergia ficticia a X");
        assertEquals("Alergia ficticia a X", antecedentes.antecedentes());
        var nota = clinica.agregarNota(odontologo, paciente.id(),
                new ServicioFichaClinica.CrearNotaClinica("Consulta ficticia inicial"));
        assertEquals(odontologo.usuarioId(), nota.autorId());
        Instant fechaCreacion = nota.creadaEn();
        assertThrows(ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException.class,
                () -> clinica.editarNota(otroOdontologo, paciente.id(), nota.id(),
                        new ServicioFichaClinica.EditarNotaClinica("Edición no permitida")));
        var editada = clinica.editarNota(odontologo, paciente.id(), nota.id(),
                new ServicioFichaClinica.EditarNotaClinica("Consulta ficticia actualizada"));
        assertEquals("Consulta ficticia actualizada", editada.texto());
        assertEquals(fechaCreacion, editada.creadaEn());
        var recargada = clinica.consultar(recepcion, paciente.id()).notas().stream()
                .filter(n -> n.id().equals(nota.id())).findFirst().orElseThrow();
        assertEquals(odontologo.usuarioId(), recargada.autorId());
        assertEquals(fechaCreacion, recargada.creadaEn());
        assertThrows(ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException.class,
                () -> clinica.actualizarAntecedentes(recepcion, paciente.id(), "No autorizado"));
        assertEquals("Alergia ficticia a X", clinica.consultar(recepcion, paciente.id()).antecedentes());

        var concurrente = pacientes.crear(recepcion,
                crear("PAC-CONCURRENTE", "Dario", "Ficticio", "dario@example.invalid"));
        asignaciones.asignar(recepcion, concurrente.id(), profesionalId);
        lanzarDos(() -> clinica.agregarNota(odontologo, concurrente.id(),
                        new ServicioFichaClinica.CrearNotaClinica("Nota concurrente A")),
                () -> clinica.agregarNota(odontologo, concurrente.id(),
                        new ServicioFichaClinica.CrearNotaClinica("Nota concurrente B")));
        assertEquals(1, contar("select count(*) from odontologia.ficha_clinica where paciente_id='" + concurrente.id() + "'"));
        assertEquals(2, clinica.consultar(recepcion, concurrente.id()).notas().size());
    }

    private static ServicioPacientes.CrearPaciente crear(String codigo, String nombre, String apellido, String email) {
        return new ServicioPacientes.CrearPaciente(codigo, nombre, apellido, email, null);
    }

    private static Identidad identidad(Rol rol, UUID profesionalId) {
        return new Identidad(UUID.randomUUID(), "usuario-ficticio-" + UUID.randomUUID(), rol, profesionalId);
    }

    private static UUID profesional(String codigo) {
        return contexto.getBean(ProfesionalReferencias.class)
                .save(new ProfesionalReferencia(codigo + "-" + UUID.randomUUID(), true)).getId();
    }

    private static Identidad odontologoConUsuario(UUID profesionalId, String nombreUsuario) throws Exception {
        UUID usuarioId = UUID.randomUUID();
        try (Connection conexion = db.appConnection(); PreparedStatement insertar = conexion.prepareStatement("""
                insert into odontologia.usuario(id,nombre_usuario,hash_credencial,rol,activo,profesional_ref,creado_en)
                values (?,?,?,'odontologo',true,?,now())
                """)) {
            insertar.setObject(1, usuarioId);
            insertar.setString(2, nombreUsuario + "-" + usuarioId);
            insertar.setString(3, "hash-ficticio");
            insertar.setObject(4, profesionalId);
            insertar.executeUpdate();
        }
        return new Identidad(usuarioId, nombreUsuario, Rol.ODONTOLOGO, profesionalId);
    }

    private static int contar(String consulta) throws Exception {
        try (Connection conexion = db.appConnection(); var instruccion = conexion.createStatement();
             ResultSet resultado = instruccion.executeQuery(consulta)) {
            assertTrue(resultado.next());
            return resultado.getInt(1);
        }
    }

    @SafeVarargs
    private static void lanzarDos(CheckedRunnable... acciones) throws Exception {
        var espera = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(acciones.length)) {
            var tareas = java.util.Arrays.stream(acciones).map(accion -> pool.submit(() -> {
                if (!espera.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("No inició la barrera");
                accion.run();
                return null;
            })).toList();
            espera.countDown();
            for (var tarea : tareas) tarea.get(30, TimeUnit.SECONDS);
        }
    }

    @FunctionalInterface
    private interface CheckedRunnable { void run() throws Exception; }
}
