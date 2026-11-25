CREATE TABLE odontologia.inscripcion_espera (
    id uuid PRIMARY KEY,
    paciente_id uuid NOT NULL REFERENCES odontologia.paciente(id),
    prestacion_id uuid NOT NULL REFERENCES odontologia.prestacion(id),
    profesional_id uuid REFERENCES odontologia.profesional(id),
    desde date NOT NULL,
    hasta date NOT NULL,
    baja_en timestamptz,
    CHECK (desde <= hasta)
);

CREATE INDEX inscripcion_espera_paciente_prestacion_idx
    ON odontologia.inscripcion_espera (paciente_id, prestacion_id, hasta) WHERE baja_en IS NULL;

CREATE TABLE odontologia.franja_espera (
    inscripcion_id uuid NOT NULL REFERENCES odontologia.inscripcion_espera(id),
    orden integer NOT NULL CHECK (orden >= 0),
    inicio_nanos bigint NOT NULL,
    fin_nanos bigint NOT NULL,
    PRIMARY KEY (inscripcion_id, orden),
    CHECK (inicio_nanos >= 0 AND inicio_nanos < fin_nanos AND fin_nanos < 86400000000000)
);

GRANT SELECT, INSERT, UPDATE ON odontologia.inscripcion_espera TO odonto_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON odontologia.franja_espera TO odonto_app;
