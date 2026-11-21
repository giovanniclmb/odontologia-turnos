package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.ContratoVinculacionUsuarioProfesional.Vinculacion;
import ar.edu.utn.odontologia.acceso.DatosBootstrap;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.acceso.ServicioBootstrap;
import ar.edu.utn.odontologia.acceso.ServicioVinculacionProfesional;
import ar.edu.utn.odontologia.agenda.Agenda;
import ar.edu.utn.odontologia.recursos.ServicioConfiguracionSede;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.dao.DataIntegrityViolationException;

class ConfiguracionRecursosIT {
    private static final Identidad RECEPCION =
            new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
    private static final Identidad ODONTOLOGO =
            new Identidad(UUID.randomUUID(), "odontologo-ficticio", Rol.ODONTOLOGO, null);

    @Test
    void registraProfesionalesSinPrecargaYConservaElOriginalAnteDuplicados() {
        try (var db = PostgresFixture.start(); var contexto = abrir(db)) {
            var servicio = contexto.getBean(ServicioRecursosAgenda.class);
            assertTrue(servicio.consultarProfesionales(ODONTOLOGO).isEmpty());
            var profesional = servicio.altaProfesional(RECEPCION, "PROF-FICTICIO", true);
            assertTrue(profesional.activo());
            assertNotNull(profesional.id());
            assertThrows(DataIntegrityViolationException.class,
                    () -> servicio.altaProfesional(RECEPCION, "PROF-FICTICIO", false));
            assertThrows(AutorizacionDenegadaException.class,
                    () -> servicio.altaProfesional(ODONTOLOGO, "NO-AUTORIZADO", true));
            assertThrows(AutorizacionDenegadaException.class,
                    () -> servicio.consultarProfesionales(null));
            assertEquals(java.util.List.of(profesional), servicio.consultarProfesionales(ODONTOLOGO));
        }
    }

    @Test
    void registraSillonesSinPrecargaYConservaElEstadoPersistido() {
        try (var db = PostgresFixture.start(); var contexto = abrir(db)) {
            var servicio = contexto.getBean(ServicioRecursosAgenda.class);
            assertTrue(servicio.consultarSillones(ODONTOLOGO).isEmpty());
            var sillon = servicio.altaSillon(RECEPCION, "SILLON-FICTICIO", false);
            assertFalse(sillon.activo());
            assertNotNull(sillon.id());
            assertThrows(DataIntegrityViolationException.class,
                    () -> servicio.altaSillon(RECEPCION, "SILLON-FICTICIO", true));
            assertThrows(AutorizacionDenegadaException.class,
                    () -> servicio.altaSillon(ODONTOLOGO, "NO-AUTORIZADO", true));
            assertEquals(java.util.List.of(sillon), servicio.consultarSillones(ODONTOLOGO));
        }
    }

    @Test
    void disponibilidadMinimaRequiereIntervaloYAmbosRecursosActivos() {
        try (var db = PostgresFixture.start(); var contexto = abrir(db)) {
            var recursos = contexto.getBean(ServicioRecursosAgenda.class);
            var sede = contexto.getBean(ServicioConfiguracionSede.class);
            var profesional = recursos.altaProfesional(RECEPCION, "PROF-ACTIVO", true).id();
            var profesionalInactivo = recursos.altaProfesional(RECEPCION, "PROF-INACTIVO", false).id();
            var sillon = recursos.altaSillon(RECEPCION, "SILLON-ACTIVO", true).id();
            var sillonInactivo = recursos.altaSillon(RECEPCION, "SILLON-INACTIVO", false).id();

            assertThrows(IllegalStateException.class,
                    () -> recursos.validarDisponibilidadMinima(ODONTOLOGO, profesional, sillon));
            sede.establecerIntervalo(RECEPCION, 15);
            assertEquals(15, recursos.validarDisponibilidadMinima(ODONTOLOGO, profesional, sillon));
            sede.establecerIntervalo(RECEPCION, 25);
            assertEquals(25, recursos.validarDisponibilidadMinima(ODONTOLOGO, profesional, sillon));
            for (UUID invalido : new UUID[]{null, UUID.randomUUID(), profesionalInactivo}) {
                assertThrows(IllegalArgumentException.class,
                        () -> recursos.validarDisponibilidadMinima(ODONTOLOGO, invalido, sillon));
            }
            for (UUID invalido : new UUID[]{null, UUID.randomUUID(), sillonInactivo}) {
                assertThrows(IllegalArgumentException.class,
                        () -> recursos.validarDisponibilidadMinima(ODONTOLOGO, profesional, invalido));
            }
        }
    }

    @Test
    void catalogoVacioAdmiteAltasValidasYRechazaDuplicados() {
        try (var db = PostgresFixture.start(); var contexto = abrir(db)) {
            var servicio = contexto.getBean(ServicioRecursosAgenda.class);
            assertTrue(servicio.consultarPrestaciones(ODONTOLOGO).isEmpty());
            var prestacion = servicio.altaPrestacion(RECEPCION, "PRESTACION-FICTICIA", 30);
            assertNotNull(prestacion.id());
            assertEquals(30, prestacion.duracionReferenciaMinutos());
            for (Integer invalido : new Integer[]{null, 0, -2}) {
                assertThrows(IllegalArgumentException.class,
                        () -> servicio.altaPrestacion(RECEPCION, "DURACION-INVALIDA", invalido));
            }
            assertThrows(DataIntegrityViolationException.class,
                    () -> servicio.altaPrestacion(RECEPCION, "PRESTACION-FICTICIA", 40));
            assertThrows(AutorizacionDenegadaException.class,
                    () -> servicio.altaPrestacion(ODONTOLOGO, "NO-AUTORIZADO", 30));
            assertEquals(java.util.List.of(prestacion), servicio.consultarPrestaciones(ODONTOLOGO));
        }
    }

    @Test
    void ajustaLaDuracionDeReferenciaSinCambiarLaDuracionYaElegida() {
        try (var db = PostgresFixture.start(); var contexto = abrir(db)) {
            var servicio = contexto.getBean(ServicioRecursosAgenda.class);
            var inicial = servicio.altaPrestacion(RECEPCION, "AJUSTE-FICTICIO", 30);
            var agenda = new Agenda();
            var solicitud = new Agenda.Solicitud("PACIENTE-FICTICIO", "PROF-FICTICIO", "SILLON-FICTICIO",
                    new Agenda.Prestacion(inicial.id().toString(), Duration.ofMinutes(inicial.duracionReferenciaMinutos())),
                    Instant.parse("2026-12-01T12:00:00Z"), Duration.ofMinutes(35));
            var turno = assertInstanceOf(Agenda.Exito.class,
                    agenda.crear(solicitud, java.util.List.of(), Duration.ofMinutes(10))).turno();
            servicio.ajustarDuracion(RECEPCION, inicial.id(), 40);
            assertThrows(IllegalArgumentException.class, () -> servicio.ajustarDuracion(RECEPCION, inicial.id(), 0));
            assertThrows(IllegalArgumentException.class,
                    () -> servicio.ajustarDuracion(RECEPCION, UUID.randomUUID(), 20));
            assertThrows(AutorizacionDenegadaException.class,
                    () -> servicio.ajustarDuracion(ODONTOLOGO, inicial.id(), 50));
            assertEquals(40, servicio.consultarPrestaciones(ODONTOLOGO).getFirst().duracionReferenciaMinutos());
            assertEquals(Duration.ofMinutes(35), turno.duracion());
        }
    }

    @Test
    void recepcionCreaYVinculaUsuariosSinAmpliarElAccesoClinicoDeOdontologia() {
        try (var db = PostgresFixture.start(); var contexto = abrir(db)) {
            var bootstrap = contexto.getBean(ServicioBootstrap.class);
            var autenticacion = contexto.getBean(ServicioAutenticacion.class);
            var autorizacion = contexto.getBean(ServicioAutorizacion.class);
            var vinculos = contexto.getBean(ServicioVinculacionProfesional.class);
            String clave = "PruebaOdontoSegura123";
            bootstrap.bootstrapInicial(new DatosBootstrap("recep-ficticia", clave, Rol.RECEPCION, null, null));
            var recepcion = autenticacion.autenticar("recep-ficticia", clave);
            var datos = new DatosBootstrap("odont-ficticio", clave, Rol.ODONTOLOGO, null, null);
            var usuario = bootstrap.crearUsuario(recepcion, datos);
            var sinVinculo = autenticacion.autenticar("odont-ficticio", clave);
            var profesional = contexto.getBean(ServicioRecursosAgenda.class)
                    .altaProfesional(recepcion, "PROF-VINCULO-FICTICIO", true).id();
            assertFalse(autorizacion.puedeConsultarPaciente(sinVinculo, profesional));
            assertThrows(AutorizacionDenegadaException.class, () -> bootstrap.crearUsuario(sinVinculo, datos));
            assertThrows(AutorizacionDenegadaException.class,
                    () -> vinculos.vincular(sinVinculo, new Vinculacion(usuario.getId(), profesional)));

            vinculos.vincular(recepcion, new Vinculacion(usuario.getId(), profesional));
            assertThrows(IllegalArgumentException.class,
                    () -> vinculos.vincular(recepcion, new Vinculacion(usuario.getId(), UUID.randomUUID())));
            assertThrows(IllegalArgumentException.class,
                    () -> vinculos.vincular(recepcion, new Vinculacion(UUID.randomUUID(), profesional)));
            var vinculado = autenticacion.autenticar("odont-ficticio", clave);
            assertEquals(profesional, vinculado.profesionalRef());
            assertTrue(autorizacion.puedeConsultarPaciente(vinculado, profesional));
            assertFalse(autorizacion.puedeConsultarPaciente(vinculado, UUID.randomUUID()));
        }
    }

    @Test
    void consultaIntervaloPersistidoYConservaElValorAnteUnRechazo() {
        try (var db = PostgresFixture.start()) {
            try (var contexto = abrir(db)) {
                var servicio = contexto.getBean(ServicioConfiguracionSede.class);
                assertEquals(Optional.empty(), servicio.consultarIntervalo(ODONTOLOGO));
                servicio.establecerIntervalo(RECEPCION, 10);
                servicio.establecerIntervalo(RECEPCION, 20);
                for (Integer invalido : new Integer[]{null, 0, -2}) {
                    assertThrows(IllegalArgumentException.class,
                            () -> servicio.establecerIntervalo(RECEPCION, invalido));
                }
                assertThrows(AutorizacionDenegadaException.class,
                        () -> servicio.establecerIntervalo(ODONTOLOGO, 30));
                assertThrows(AutorizacionDenegadaException.class, () -> servicio.consultarIntervalo(null));
            }
            try (var contexto = abrir(db)) {
                assertEquals(Optional.of(20),
                        contexto.getBean(ServicioConfiguracionSede.class).consultarIntervalo(RECEPCION));
            }
        }
    }

    private static ConfigurableApplicationContext abrir(PostgresFixture db) {
        var entorno = new StandardEnvironment();
        entorno.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        entorno.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        entorno.getPropertySources().addFirst(new MapPropertySource("postgresTemporal", db.properties()));
        return new SpringApplicationBuilder(OdontologiaApplication.class).web(WebApplicationType.NONE)
                .environment(entorno).run("--spring.config.location=classpath:/application.yaml");
    }
}
