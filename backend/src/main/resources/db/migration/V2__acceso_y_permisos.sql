-- C-03: identidad interna y permisos. Sin transporte HTTP.
CREATE TABLE odontologia.usuario (
    id uuid PRIMARY KEY,
    nombre_usuario text NOT NULL UNIQUE CHECK (length(btrim(nombre_usuario)) > 0),
    hash_credencial text NOT NULL,
    rol text NOT NULL CHECK (rol IN ('recepcion', 'odontologo')),
    activo boolean NOT NULL DEFAULT true,
    profesional_ref uuid NULL,
    creado_en timestamptz NOT NULL
);

CREATE TABLE odontologia.token_acceso (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES odontologia.usuario (id),
    hash_token text NOT NULL,
    creado_en timestamptz NOT NULL,
    expira_en timestamptz NOT NULL,
    revocado boolean NOT NULL DEFAULT false
);

GRANT SELECT, INSERT, UPDATE ON odontologia.usuario, odontologia.token_acceso TO odonto_app;
