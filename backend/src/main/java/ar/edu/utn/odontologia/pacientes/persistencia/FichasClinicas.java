package ar.edu.utn.odontologia.pacientes.persistencia;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface FichasClinicas extends Repository<FichaClinica, UUID> {
    Optional<FichaClinica> findById(UUID pacienteId);
    FichaClinica save(FichaClinica ficha);

    @Modifying
    @Query(value = "insert into odontologia.ficha_clinica (paciente_id) values (:pacienteId) on conflict (paciente_id) do nothing",
            nativeQuery = true)
    void crearSiNoExiste(@Param("pacienteId") UUID pacienteId);
}
