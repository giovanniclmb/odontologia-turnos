-- Historial inmutable: las últimas cuatro credenciales previas se consultan junto a la vigente.
CREATE TABLE odontologia.historial_contrasena (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES odontologia.usuario(id),
    secuencia bigint NOT NULL CHECK (secuencia > 0),
    hash_credencial text NOT NULL CHECK (length(btrim(hash_credencial)) > 0),
    creado_en timestamptz NOT NULL,
    UNIQUE (usuario_id, secuencia)
);

-- El índice UNIQUE también sirve para buscar por usuario y secuencia descendente.
GRANT SELECT, INSERT ON odontologia.historial_contrasena TO odonto_app;
