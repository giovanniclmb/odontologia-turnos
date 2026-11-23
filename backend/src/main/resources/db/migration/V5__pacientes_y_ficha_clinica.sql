ALTER TABLE odontologia.paciente
    ADD COLUMN IF NOT EXISTS nombre text,
    ADD COLUMN IF NOT EXISTS apellido text,
    ADD COLUMN IF NOT EXISTS email text,
    ADD COLUMN IF NOT EXISTS telefono text;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM odontologia.paciente
        WHERE nombre IS NULL OR length(btrim(nombre)) = 0
           OR apellido IS NULL OR length(btrim(apellido)) = 0
           OR email IS NULL OR length(btrim(email)) = 0
    ) THEN
        RAISE EXCEPTION 'V5 requiere completar los datos administrativos obligatorios de pacientes legacy'
            USING ERRCODE = '23514';
    END IF;
END;
$$;

ALTER TABLE odontologia.paciente
    ALTER COLUMN nombre SET NOT NULL,
    ALTER COLUMN apellido SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT paciente_nombre_no_vacio CHECK (length(btrim(nombre)) > 0),
    ADD CONSTRAINT paciente_apellido_no_vacio CHECK (length(btrim(apellido)) > 0),
    ADD CONSTRAINT paciente_email_no_vacio CHECK (length(btrim(email)) > 0);

CREATE TABLE odontologia.paciente_profesional (
    paciente_id uuid NOT NULL REFERENCES odontologia.paciente(id),
    profesional_id uuid NOT NULL REFERENCES odontologia.profesional(id),
    activo boolean NOT NULL,
    PRIMARY KEY (paciente_id, profesional_id)
);

CREATE TABLE odontologia.ficha_clinica (
    paciente_id uuid PRIMARY KEY REFERENCES odontologia.paciente(id),
    antecedentes text
);

CREATE TABLE odontologia.nota_clinica (
    id uuid PRIMARY KEY,
    paciente_id uuid NOT NULL REFERENCES odontologia.ficha_clinica(paciente_id),
    autor_usuario_id uuid NOT NULL REFERENCES odontologia.usuario(id),
    texto text NOT NULL CHECK (length(btrim(texto)) > 0),
    creada_en timestamptz NOT NULL
);

CREATE INDEX paciente_profesional_activo_idx
    ON odontologia.paciente_profesional (profesional_id, paciente_id)
    WHERE activo;

CREATE INDEX nota_clinica_paciente_fecha_idx
    ON odontologia.nota_clinica (paciente_id, creada_en, id);

GRANT SELECT, INSERT, UPDATE ON odontologia.paciente_profesional,
    odontologia.ficha_clinica, odontologia.nota_clinica TO odonto_app;
