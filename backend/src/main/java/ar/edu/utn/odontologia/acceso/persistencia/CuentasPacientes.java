package ar.edu.utn.odontologia.acceso.persistencia;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CuentasPacientes extends Repository<CuentaPaciente, UUID> {
    CuentaPaciente save(CuentaPaciente cuenta);

    Optional<CuentaPaciente> findById(UUID id);

    Optional<CuentaPaciente> findByEmail(String email);

    Optional<CuentaPaciente> findByPacienteRef(UUID pacienteRef);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CuentaPaciente c where c.id = :id")
    Optional<CuentaPaciente> buscarParaVincular(@Param("id") UUID id);
}
