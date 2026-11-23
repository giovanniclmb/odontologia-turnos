CREATE TABLE odontologia.bloqueo_recurso (
    id uuid PRIMARY KEY,
    profesional_id uuid REFERENCES odontologia.profesional (id),
    sillon_id uuid REFERENCES odontologia.sillon (id),
    inicio timestamptz NOT NULL,
    fin timestamptz NOT NULL,
    CONSTRAINT bloqueo_con_recurso CHECK (profesional_id IS NOT NULL OR sillon_id IS NOT NULL),
    CONSTRAINT bloqueo_periodo_valido CHECK (isfinite(inicio) AND isfinite(fin) AND inicio < fin)
);

CREATE INDEX bloqueo_profesional_inicio_idx
    ON odontologia.bloqueo_recurso (profesional_id, inicio)
    WHERE profesional_id IS NOT NULL;

CREATE INDEX bloqueo_sillon_inicio_idx
    ON odontologia.bloqueo_recurso (sillon_id, inicio)
    WHERE sillon_id IS NOT NULL;

GRANT SELECT, INSERT ON odontologia.bloqueo_recurso TO odonto_app;
