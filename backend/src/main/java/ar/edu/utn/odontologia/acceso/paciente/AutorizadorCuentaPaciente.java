package ar.edu.utn.odontologia.acceso.paciente;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AutorizadorCuentaPaciente implements ContratoAutorizacionCuentaPaciente {
    @Override
    public UUID exigirPacienteVinculado(IdentidadCuentaPaciente identidad) {
        if (identidad == null || identidad.estadoVinculo() != EstadoVinculoCuentaPaciente.VINCULADA
                || identidad.pacienteId() == null) {
            throw new AutorizacionDenegadaException();
        }
        return identidad.pacienteId();
    }

    @Override
    public void exigirPropiedad(IdentidadCuentaPaciente identidad, UUID pacientePropietarioId) {
        UUID pacienteAutenticadoId = exigirPacienteVinculado(identidad);
        if (pacientePropietarioId == null || !pacienteAutenticadoId.equals(pacientePropietarioId)) {
            throw new AutorizacionDenegadaException();
        }
    }
}
