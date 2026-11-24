-- C-16: identidad propia del paciente; separada de usuarios internos.
CREATE TABLE odontologia.cuenta_paciente (
    id uuid PRIMARY KEY,
    email text NOT NULL UNIQUE CHECK (length(btrim(email)) > 0),
    hash_credencial text NOT NULL CHECK (length(btrim(hash_credencial)) > 0),
    paciente_ref uuid NULL UNIQUE REFERENCES odontologia.paciente (id),
    creado_en timestamptz NOT NULL
);

GRANT SELECT, INSERT, UPDATE ON odontologia.cuenta_paciente TO odonto_app;
