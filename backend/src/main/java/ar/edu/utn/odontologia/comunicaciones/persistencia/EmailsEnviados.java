package ar.edu.utn.odontologia.comunicaciones.persistencia;

import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface EmailsEnviados extends Repository<EmailEnvio, UUID> {
    EmailEnvio save(EmailEnvio envio);

    EmailEnvio findByIdempotencyKey(String idempotencyKey);
}