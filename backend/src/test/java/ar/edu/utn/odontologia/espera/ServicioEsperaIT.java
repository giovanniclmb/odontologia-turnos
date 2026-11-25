package ar.edu.utn.odontologia.espera;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.paciente.ContratoServicioCuentasPaciente;
import ar.edu.utn.odontologia.acceso.paciente.CredencialesCuentaPaciente;
import ar.edu.utn.odontologia.acceso.paciente.DatosRegistroCuentaPaciente;
import ar.edu.utn.odontologia.acceso.paciente.IdentidadCuentaPaciente;
import ar.edu.utn.odontologia.acceso.paciente.VinculacionCuentaPaciente;
import ar.edu.utn.odontologia.persistencia.PostgresFixture;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServicioEsperaIT {
    private static final Identidad RECEPCION = new Identidad(UUID.randomUUID(), "recepcion", Rol.RECEPCION, null);
    private static final String CLAVE = "Clave-Ficticia#2026";
    private static final Instant INSTANTE = Instant.parse("2026-11-25T15:00:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 25);
    private final AtomicReference<Instant> instante = new AtomicReference<>(INSTANTE);
    private PostgresFixture db;
    private ConfigurableApplicationContext contexto;
    private ContratoServicioEspera espera;
    private ContratoServicioCuentasPaciente cuentas;
    private ServicioRecursosAgenda recursos;

    @BeforeAll
    void iniciar() {
        db = PostgresFixture.start();
        contexto = EsperaMigracionIT.abrir(db, ctx -> ((GenericApplicationContext) ctx)
                .registerBean("relojPruebaEspera", Clock.class, () -> new Clock() {
                    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
                    @Override public Clock withZone(ZoneId zona) { return Clock.fixed(instant(), zona); }
                    @Override public Instant instant() { return instante.get(); }
                }, bean -> bean.setPrimary(true)));
        espera = contexto.getBean(ContratoServicioEspera.class);
        cuentas = contexto.getBean(ContratoServicioCuentasPaciente.class);
        recursos = contexto.getBean(ServicioRecursosAgenda.class);
    }

    @AfterAll
    void cerrar() {
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
    void restablecerReloj() {
        instante.set(INSTANTE);
    }

    @Test
    void altaYConsultaValidanVinculoTitularYReferencias() throws Exception {
        var pendiente = registrar();
        var titular = vincular(pendiente);
        var otro = vincular(registrar());
        var preferencias = preferencias(prestacion());
        assertThrows(AutorizacionDenegadaException.class, () -> espera.crear(pendiente, preferencias));
        assertThrows(AutorizacionDenegadaException.class, () -> espera.listar(null));
        assertThrows(IllegalArgumentException.class, () -> espera.crear(titular, preferencias(UUID.randomUUID())));
        var profesionalInexistente = new PreferenciasEspera(preferencias.prestacionId(), HOY, HOY,
                preferencias.franjas(), UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> espera.crear(titular, profesionalInexistente));
        var inscripcion = espera.crear(titular, preferencias);
        assertEquals(preferencias, inscripcion.preferencias());
        assertEquals(List.of(inscripcion), espera.listar(titular));
        assertTrue(espera.listar(otro).isEmpty());
        assertEquals(espera.consultar(otro, UUID.randomUUID()), espera.consultar(otro, inscripcion.id()));
        assertTrue(espera.consultar(otro, inscripcion.id()).isEmpty());
        assertEquals(inscripcion, espera.consultar(titular, inscripcion.id()).orElseThrow());
    }

    @Test
    void editaDaDeBajaYConservaElRegistroSinEfectosEnAgendaNiEmail() throws Exception {
        var titular = vincular(registrar());
        var otro = vincular(registrar());
        int turnos = contar("turno");
        int avisos = contar("email_envio");
        var original = espera.crear(titular, preferencias(prestacion()));
        UUID profesional = recursos.altaProfesional(RECEPCION, UUID.randomUUID().toString(), true).id();
        var nuevas = new PreferenciasEspera(prestacion(), HOY, HOY,
                List.of(new FranjaEspera(LocalTime.of(14, 0), LocalTime.of(16, 0)),
                        new FranjaEspera(LocalTime.of(17, 0), LocalTime.of(18, 0))), profesional);
        assertThrows(java.util.NoSuchElementException.class, () -> espera.editar(otro, original.id(), nuevas));
        assertThrows(java.util.NoSuchElementException.class, () -> espera.darDeBaja(otro, original.id()));
        assertEquals(original, espera.consultar(titular, original.id()).orElseThrow());
        assertEquals(nuevas, espera.editar(titular, original.id(), nuevas).preferencias());
        assertEquals(nuevas, espera.consultar(titular, original.id()).orElseThrow().preferencias());
        var baja = espera.darDeBaja(titular, original.id());
        instante.set(INSTANTE.plusSeconds(1));
        assertEquals(baja, espera.darDeBaja(titular, original.id()));
        assertEquals(VigenciaEspera.BAJA, baja.vigencia());
        assertEquals(INSTANTE, baja.bajaEn());
        assertEquals(baja, espera.consultar(titular, original.id()).orElseThrow());
        assertThrows(IllegalStateException.class, () -> espera.editar(titular, original.id(), nuevas));
        assertEquals(turnos, contar("turno"));
        assertEquals(avisos, contar("email_envio"));
    }

    @Test
    void liberaElCupoPorBajaOAlComenzarElDiaSiguiente() throws Exception {
        var titular = vincular(registrar());
        var preferencias = preferencias(prestacion());
        var primera = espera.crear(titular, preferencias);
        var segunda = espera.crear(titular, preferencias(prestacion()));
        assertThrows(IllegalStateException.class, () -> espera.editar(titular, segunda.id(), preferencias));
        assertEquals(segunda, espera.consultar(titular, segunda.id()).orElseThrow());
        espera.darDeBaja(titular, primera.id());
        var posterior = espera.crear(titular, preferencias);
        instante.set(Instant.parse("2026-11-26T03:00:00Z"));
        var vencida = espera.consultar(titular, posterior.id()).orElseThrow();
        assertEquals(VigenciaEspera.VENCIDA, vencida.vigencia());
        assertEquals(vencida, espera.darDeBaja(titular, posterior.id()));
        var nuevas = new PreferenciasEspera(preferencias.prestacionId(), HOY.plusDays(1), HOY.plusDays(1),
                preferencias.franjas(), null);
        assertThrows(IllegalStateException.class, () -> espera.editar(titular, posterior.id(), nuevas));
        assertEquals(VigenciaEspera.VIGENTE, espera.crear(titular, nuevas).vigencia());
    }

    @Test
    void altasSimultaneasYAltaConcurrenteConEdicionRespetanElCupo() throws Exception {
        var titular = vincular(registrar());
        var preferencias = preferencias(prestacion());
        assertUnaOperacionExitosa(() -> espera.crear(titular, preferencias), () -> espera.crear(titular, preferencias));
        assertEquals(1, espera.listar(titular).size());

        var original = espera.crear(titular, preferencias(prestacion()));
        var objetivo = preferencias(prestacion());
        assertUnaOperacionExitosa(() -> espera.crear(titular, objetivo),
                () -> espera.editar(titular, original.id(), objetivo));
        assertEquals(1, espera.listar(titular).stream()
                .filter(i -> i.preferencias().prestacionId().equals(objetivo.prestacionId())).count());
    }

    private void assertUnaOperacionExitosa(Callable<?> primera, Callable<?> segunda) throws Exception {
        var barrera = new CyclicBarrier(2);
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var a = ejecutor.submit(() -> ejecutarConBarrera(primera, barrera));
            var b = ejecutor.submit(() -> ejecutarConBarrera(segunda, barrera));
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
    }

    private boolean ejecutarConBarrera(Callable<?> operacion, CyclicBarrier barrera) throws Exception {
        barrera.await(10, TimeUnit.SECONDS);
        try {
            operacion.call();
            return true;
        } catch (IllegalStateException conflicto) {
            assertEquals("Ya existe una inscripción vigente para la prestación", conflicto.getMessage());
            return false;
        }
    }

    private IdentidadCuentaPaciente registrar() {
        return cuentas.registrar(new DatosRegistroCuentaPaciente(UUID.randomUUID() + "@example.invalid", CLAVE));
    }

    private IdentidadCuentaPaciente vincular(IdentidadCuentaPaciente pendiente) throws SQLException {
        cuentas.confirmarVinculo(RECEPCION,
                new VinculacionCuentaPaciente(pendiente.cuentaId(), EsperaMigracionIT.insertarPaciente(db)));
        return cuentas.autenticar(new CredencialesCuentaPaciente(pendiente.email(), CLAVE));
    }

    private UUID prestacion() {
        return recursos.altaPrestacion(RECEPCION, UUID.randomUUID().toString(), 30).id();
    }

    private PreferenciasEspera preferencias(UUID prestacion) {
        return new PreferenciasEspera(prestacion, HOY, HOY,
                List.of(new FranjaEspera(LocalTime.of(9, 0), LocalTime.of(10, 0))), null);
    }

    private int contar(String tabla) throws SQLException {
        try (var con = db.appConnection(); var sql = con.createStatement();
             var filas = sql.executeQuery("select count(*) from odontologia." + tabla)) {
            assertTrue(filas.next());
            return filas.getInt(1);
        }
    }
}
