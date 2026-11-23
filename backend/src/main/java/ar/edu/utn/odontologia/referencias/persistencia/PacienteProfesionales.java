package ar.edu.utn.odontologia.referencias.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface PacienteProfesionales extends Repository<PacienteProfesional, PacienteProfesionalId> {
    PacienteProfesional save(PacienteProfesional asignacion);
    Optional<PacienteProfesional> findById(PacienteProfesionalId id);

    @Query("""
            select case when count(a) > 0 then true else false end
            from PacienteProfesional a
            where a.id.pacienteId = :pacienteId and a.id.profesionalId = :profesionalId and a.activo = true
            """)
    boolean tieneAsignacionActiva(@Param("pacienteId") UUID pacienteId, @Param("profesionalId") UUID profesionalId);

    @Query("select a from PacienteProfesional a where a.id.pacienteId = :pacienteId")
    List<PacienteProfesional> dePaciente(@Param("pacienteId") UUID pacienteId);
}
