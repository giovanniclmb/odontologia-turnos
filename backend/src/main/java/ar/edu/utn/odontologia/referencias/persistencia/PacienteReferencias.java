package ar.edu.utn.odontologia.referencias.persistencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteReferencias extends Referencias<PacienteReferencia> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PacienteReferencia p where p.id = :id")
    Optional<PacienteReferencia> buscarParaModificarEspera(@Param("id") UUID id);

    @Query("""
            select new ar.edu.utn.odontologia.referencias.persistencia.NombrePaciente(p.id, p.nombre, p.apellido)
            from PacienteReferencia p where p.id in :ids
            """)
    List<NombrePaciente> buscarNombres(@Param("ids") Collection<UUID> ids);

    @Query("""
            select p from PacienteReferencia p
            where (coalesce(:codigo, '') = '' or p.codigo = :codigo)
              and (coalesce(:texto, '') = '' or lower(p.nombre) like concat('%', lower(coalesce(:texto, '')), '%')
                   or lower(p.apellido) like concat('%', lower(coalesce(:texto, '')), '%'))
              and (:profesionalId is null or exists (
                   select a from PacienteProfesional a
                   where a.id.pacienteId = p.id and a.id.profesionalId = :profesionalId and a.activo = true))
            order by lower(p.apellido), lower(p.nombre), p.id
            """)
    List<PacienteReferencia> buscarEnAlcance(
            @Param("codigo") String codigo,
            @Param("texto") String texto,
            @Param("profesionalId") UUID profesionalId);
}
