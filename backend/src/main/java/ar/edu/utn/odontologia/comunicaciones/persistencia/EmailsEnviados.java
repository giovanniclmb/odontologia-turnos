package ar.edu.utn.odontologia.comunicaciones.persistencia;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface EmailsEnviados extends JpaRepository<EmailEnvio, UUID> {
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    <S extends EmailEnvio> S saveAndFlush(S envio);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    EmailEnvio findByIdempotencyKey(String idempotencyKey);

    List<EmailEnvio> findByTipoInAndTurnoIdIn(Collection<String> tipos, Collection<UUID> turnos);
}
