package ar.edu.utn.odontologia.referencias.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface Referencias<T extends Referencia> extends Repository<T,UUID> {
    T save(T referencia);
    Optional<T> findById(UUID id);
    List<T> findAll();
}
