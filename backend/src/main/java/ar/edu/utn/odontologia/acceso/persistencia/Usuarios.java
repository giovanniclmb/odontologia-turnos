package ar.edu.utn.odontologia.acceso.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.Repository;

public interface Usuarios extends Repository<Usuario, UUID> {
    Usuario save(Usuario usuario);

    Optional<Usuario> findById(UUID id);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    List<Usuario> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> buscarParaCambio(@Param("id") UUID id);

    @Query(value = "SELECT pg_advisory_xact_lock(hashtext('odontologia-bootstrap'))", nativeQuery = true)
    Object adquirirBloqueoBootstrap();
}
