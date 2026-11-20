package ar.edu.utn.odontologia.acceso.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface TokensAcceso extends Repository<TokenAcceso, UUID> {
    TokenAcceso save(TokenAcceso token);

    Optional<TokenAcceso> findById(UUID id);

    List<TokenAcceso> findByUsuarioId(UUID usuarioId);
}
