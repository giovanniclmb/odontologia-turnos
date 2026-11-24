CREATE TABLE odontologia.email_envio (
    id uuid PRIMARY KEY,
    tipo text NOT NULL,
    turno_id uuid,
    destinatario text NOT NULL,
    idempotency_key text NOT NULL,
    estado text NOT NULL CHECK (estado IN ('pending', 'sent', 'failed')),
    intentos integer NOT NULL DEFAULT 0 CHECK (intentos >= 0),
    error text,
    creado_en timestamptz NOT NULL DEFAULT now(),
    enviado_en timestamptz,
    CONSTRAINT email_envio_idempotency_key_uk UNIQUE (idempotency_key)
);

CREATE INDEX email_envio_tipo_turno_idx ON odontologia.email_envio (tipo, turno_id);
CREATE INDEX email_envio_destinatario_idx ON odontologia.email_envio (destinatario);

GRANT SELECT, INSERT, UPDATE ON odontologia.email_envio TO odonto_app;