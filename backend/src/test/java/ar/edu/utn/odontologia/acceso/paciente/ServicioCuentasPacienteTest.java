package ar.edu.utn.odontologia.acceso.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.odontologia.acceso.AutenticacionFallidaException;
import ar.edu.utn.odontologia.acceso.PoliticaContrasenaException;
import ar.edu.utn.odontologia.acceso.ServicioCredenciales;
import ar.edu.utn.odontologia.acceso.persistencia.CuentaPaciente;
import ar.edu.utn.odontologia.acceso.persistencia.CuentasPacientes;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class ServicioCuentasPacienteTest {
    private static final String CONTRASENA = "PacienteSeguraClave123";
    private final ServicioCredenciales credenciales = new ServicioCredenciales();
    private final PacienteReferencias pacientes = mock(PacienteReferencias.class);

    @Test
    void registraCuentaPendienteConContrasenaHasheada() {
        var cuentas = new RepositorioMemoria();
        var servicio = new ServicioCuentasPaciente(cuentas, credenciales, pacientes);
        String email = emailUnico();

        IdentidadCuentaPaciente identidad = servicio.registrar(
                new DatosRegistroCuentaPaciente(email, CONTRASENA));

        assertEquals(email, identidad.email());
        assertEquals(EstadoVinculoCuentaPaciente.PENDIENTE, identidad.estadoVinculo());
        assertNull(identidad.pacienteId());
        CuentaPaciente guardada = cuentas.findByEmail(email).orElseThrow();
        assertNotEquals(CONTRASENA, guardada.getHashCredencial());
        assertTrue(credenciales.verificar(CONTRASENA, guardada.getHashCredencial()));
    }

    @Test
    void autenticaCuentaPendiente() {
        var cuentas = new RepositorioMemoria();
        var servicio = new ServicioCuentasPaciente(cuentas, credenciales, pacientes);
        String email = emailUnico();
        servicio.registrar(new DatosRegistroCuentaPaciente(email, CONTRASENA));

        IdentidadCuentaPaciente identidad = servicio.autenticar(
                new CredencialesCuentaPaciente(email, CONTRASENA));

        assertEquals(EstadoVinculoCuentaPaciente.PENDIENTE, identidad.estadoVinculo());
        assertFalse(identidad.estadoVinculo() == EstadoVinculoCuentaPaciente.VINCULADA);
    }

    @Test
    void credencialesIncorrectasYEmailDesconocidoTienenRechazoGenerico() {
        var cuentas = new RepositorioMemoria();
        var servicio = new ServicioCuentasPaciente(cuentas, credenciales, pacientes);
        String email = emailUnico();
        servicio.registrar(new DatosRegistroCuentaPaciente(email, CONTRASENA));

        var claveIncorrecta = assertThrows(AutenticacionFallidaException.class,
                () -> servicio.autenticar(new CredencialesCuentaPaciente(email, "OtraClaveIncorrecta123")));
        var emailDesconocido = assertThrows(AutenticacionFallidaException.class,
                () -> servicio.autenticar(new CredencialesCuentaPaciente(emailUnico(), CONTRASENA)));

        assertEquals(claveIncorrecta.getMessage(), emailDesconocido.getMessage());
    }

    @Test
    void rechazaEmailYaUsadoPorOtraCuenta() {
        var cuentas = new RepositorioMemoria();
        var servicio = new ServicioCuentasPaciente(cuentas, credenciales, pacientes);
        String email = emailUnico();
        servicio.registrar(new DatosRegistroCuentaPaciente(email, CONTRASENA));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(new DatosRegistroCuentaPaciente(email, CONTRASENA)));
        assertEquals(1, cuentas.total());
    }

    @Test
    void rechazaContrasenaFueraDeLaPoliticaExistente() {
        var servicio = new ServicioCuentasPaciente(new RepositorioMemoria(), credenciales, pacientes);

        assertThrows(PoliticaContrasenaException.class, () -> servicio.registrar(
                new DatosRegistroCuentaPaciente(emailUnico(), "corta")));
    }

    private static String emailUnico() {
        return "paciente-" + UUID.randomUUID() + "@example.invalid";
    }

    private static void asignarId(CuentaPaciente cuenta, UUID id) {
        try {
            Field campo = CuentaPaciente.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(cuenta, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class RepositorioMemoria implements CuentasPacientes {
        private final Map<UUID, CuentaPaciente> porId = new HashMap<>();

        @Override
        public CuentaPaciente save(CuentaPaciente cuenta) {
            if (findByEmail(cuenta.getEmail()).isPresent()) {
                throw new IllegalStateException("email duplicado");
            }
            asignarId(cuenta, UUID.randomUUID());
            porId.put(cuenta.getId(), cuenta);
            return cuenta;
        }

        @Override
        public Optional<CuentaPaciente> findById(UUID id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<CuentaPaciente> findByEmail(String email) {
            return porId.values().stream().filter(cuenta -> cuenta.getEmail().equals(email)).findFirst();
        }

        @Override
        public Optional<CuentaPaciente> findByPacienteRef(UUID pacienteRef) {
            return porId.values().stream()
                    .filter(cuenta -> pacienteRef.equals(cuenta.getPacienteRef())).findFirst();
        }

        @Override
        public Optional<CuentaPaciente> buscarParaVincular(UUID id) {
            return findById(id);
        }

        int total() {
            return porId.size();
        }
    }
}
