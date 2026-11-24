package ar.edu.utn.odontologia.acceso.paciente;

import java.util.UUID;

public interface ContratoAutorizacionCuentaPaciente {
    UUID exigirPacienteVinculado(IdentidadCuentaPaciente identidad);

    void exigirPropiedad(IdentidadCuentaPaciente identidad, UUID pacientePropietarioId);
}
