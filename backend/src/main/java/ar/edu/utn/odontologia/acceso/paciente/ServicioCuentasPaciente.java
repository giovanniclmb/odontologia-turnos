package ar.edu.utn.odontologia.acceso.paciente;

import ar.edu.utn.odontologia.acceso.AutenticacionFallidaException;
import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.DatosPersonales;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioCredenciales;
import ar.edu.utn.odontologia.acceso.persistencia.CuentaPaciente;
import ar.edu.utn.odontologia.acceso.persistencia.CuentasPacientes;
import ar.edu.utn.odontologia.referencias.persistencia.PacienteReferencias;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioCuentasPaciente implements ContratoServicioCuentasPaciente {
    private static final String EMAIL_VALIDO = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final CuentasPacientes cuentas;
    private final ServicioCredenciales credenciales;
    private final PacienteReferencias pacientes;

    public ServicioCuentasPaciente(CuentasPacientes cuentas, ServicioCredenciales credenciales,
                                   PacienteReferencias pacientes) {
        this.cuentas = cuentas;
        this.credenciales = credenciales;
        this.pacientes = pacientes;
    }

    @Transactional
    @Override
    public IdentidadCuentaPaciente registrar(DatosRegistroCuentaPaciente datos) {
        if (datos == null || !emailValido(datos.email()) || datos.contrasena() == null) {
            throw new IllegalArgumentException("Datos de registro inválidos");
        }
        if (cuentas.findByEmail(datos.email()).isPresent()) {
            throw new IllegalArgumentException("No se pudo registrar la cuenta");
        }
        credenciales.validarPolitica(datos.contrasena(),
                new DatosPersonales(null, null, datos.email()));
        CuentaPaciente cuenta = cuentas.save(new CuentaPaciente(
                datos.email(), credenciales.hash(datos.contrasena())));
        return identidad(cuenta);
    }

    @Transactional(readOnly = true)
    @Override
    public IdentidadCuentaPaciente autenticar(CredencialesCuentaPaciente datos) {
        if (datos == null || datos.email() == null || datos.contrasena() == null) {
            throw new AutenticacionFallidaException();
        }
        CuentaPaciente cuenta = cuentas.findByEmail(datos.email()).orElse(null);
        if (cuenta == null || !credenciales.verificar(datos.contrasena(), cuenta.getHashCredencial())) {
            throw new AutenticacionFallidaException();
        }
        return identidad(cuenta);
    }

    @Transactional
    @Override
    public IdentidadCuentaPaciente confirmarVinculo(Identidad solicitante,
                                                     VinculacionCuentaPaciente vinculacion) {
        if (solicitante == null || solicitante.rol() != Rol.RECEPCION) {
            throw new AutorizacionDenegadaException();
        }
        if (vinculacion == null || vinculacion.cuentaId() == null || vinculacion.pacienteId() == null) {
            throw new IllegalArgumentException("La cuenta y el paciente son obligatorios");
        }

        CuentaPaciente cuenta = cuentas.buscarParaVincular(vinculacion.cuentaId())
                .orElseThrow(() -> new IllegalArgumentException("No se puede confirmar el vínculo solicitado"));
        if (cuenta.getPacienteRef() != null || pacientes.findById(vinculacion.pacienteId()).isEmpty()
                || cuentas.findByPacienteRef(vinculacion.pacienteId()).isPresent()) {
            throw new IllegalArgumentException("No se puede confirmar el vínculo solicitado");
        }

        cuenta.confirmarVinculo(vinculacion.pacienteId());
        return identidad(cuentas.save(cuenta));
    }

    private static boolean emailValido(String email) {
        return email != null && !email.isBlank() && email.matches(EMAIL_VALIDO);
    }

    private static IdentidadCuentaPaciente identidad(CuentaPaciente cuenta) {
        var estado = cuenta.getPacienteRef() == null
                ? EstadoVinculoCuentaPaciente.PENDIENTE
                : EstadoVinculoCuentaPaciente.VINCULADA;
        return new IdentidadCuentaPaciente(cuenta.getId(), cuenta.getEmail(), cuenta.getPacienteRef(), estado);
    }
}
