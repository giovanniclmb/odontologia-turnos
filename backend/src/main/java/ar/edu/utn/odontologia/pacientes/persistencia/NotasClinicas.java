package ar.edu.utn.odontologia.pacientes.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface NotasClinicas extends Repository<NotaClinica, UUID> {
    NotaClinica save(NotaClinica nota);
    Optional<NotaClinica> findById(UUID notaId);
    List<NotaClinica> findAllByPacienteIdOrderByCreadaEnAscIdAsc(UUID pacienteId);
}
