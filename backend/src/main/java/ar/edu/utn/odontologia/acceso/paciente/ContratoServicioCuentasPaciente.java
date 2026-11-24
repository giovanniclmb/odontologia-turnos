package ar.edu.utn.odontologia.acceso.paciente;

import ar.edu.utn.odontologia.acceso.Identidad;

public interface ContratoServicioCuentasPaciente {
    IdentidadCuentaPaciente registrar(DatosRegistroCuentaPaciente datos);

    IdentidadCuentaPaciente autenticar(CredencialesCuentaPaciente credenciales);

    IdentidadCuentaPaciente confirmarVinculo(Identidad solicitante, VinculacionCuentaPaciente vinculacion);
}
