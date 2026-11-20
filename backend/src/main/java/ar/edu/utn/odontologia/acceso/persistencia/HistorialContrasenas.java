package ar.edu.utn.odontologia.acceso.persistencia;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface HistorialContrasenas extends Repository<HistorialContrasena,UUID> {
    HistorialContrasena save(HistorialContrasena anterior);
    List<HistorialContrasena> findTop4ByUsuarioIdOrderBySecuenciaDesc(UUID usuarioId);
}
