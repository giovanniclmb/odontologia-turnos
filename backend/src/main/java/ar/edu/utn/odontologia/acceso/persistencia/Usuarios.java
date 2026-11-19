package ar.edu.utn.odontologia.acceso.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface Usuarios extends Repository<Usuario, UUID> {
    Usuario save(Usuario usuario);

    Optional<Usuario> findById(UUID id);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    List<Usuario> findAll();
}
