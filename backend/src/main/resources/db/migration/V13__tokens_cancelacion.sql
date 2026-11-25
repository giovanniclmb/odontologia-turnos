-- C-18: token de cancelación por enlace del paciente.
CREATE TABLE odontologia.token_cancelacion (
    id uuid PRIMARY KEY,
    turno_id uuid NOT NULL REFERENCES odontologia.turno (id) ON DELETE CASCADE,
    hash_token text NOT NULL UNIQUE CHECK (length(btrim(hash_token)) > 0),
    version_turno bigint NOT NULL,
    consumido boolean NOT NULL DEFAULT false,
    consumido_en timestamptz NULL,
    creado_en timestamptz NOT NULL
);

CREATE INDEX token_cancelacion_turno_idx ON odontologia.token_cancelacion (turno_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON odontologia.token_cancelacion TO odonto_app;