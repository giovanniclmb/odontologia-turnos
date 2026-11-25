package ar.edu.utn.odontologia.espera;

import ar.edu.utn.odontologia.acceso.paciente.IdentidadCuentaPaciente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoServicioEspera {
    InscripcionEspera crear(IdentidadCuentaPaciente identidad, PreferenciasEspera preferencias);

    List<InscripcionEspera> listar(IdentidadCuentaPaciente identidad);

    Optional<InscripcionEspera> consultar(IdentidadCuentaPaciente identidad, UUID id);

    InscripcionEspera editar(IdentidadCuentaPaciente identidad, UUID id, PreferenciasEspera preferencias);

    InscripcionEspera darDeBaja(IdentidadCuentaPaciente identidad, UUID id);
}
