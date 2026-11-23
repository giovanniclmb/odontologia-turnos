CREATE TABLE odontologia.turno (
    id uuid PRIMARY KEY,
    paciente_id uuid NOT NULL REFERENCES odontologia.paciente (id),
    profesional_id uuid NOT NULL REFERENCES odontologia.profesional (id),
    sillon_id uuid NOT NULL REFERENCES odontologia.sillon (id),
    prestacion_id uuid NOT NULL REFERENCES odontologia.prestacion (id),
    inicio timestamptz NOT NULL,
    fin timestamptz NOT NULL,
    duracion_minutos integer NOT NULL CHECK (duracion_minutos > 0),
    estado text NOT NULL CHECK (estado = 'PENDIENTE'),
    CONSTRAINT turno_periodo_valido CHECK (
        isfinite(inicio) AND isfinite(fin) AND inicio < fin
        AND fin - inicio = make_interval(mins => duracion_minutos)
    )
);

CREATE INDEX turno_profesional_inicio_idx ON odontologia.turno (profesional_id, inicio);
CREATE INDEX turno_sillon_inicio_idx ON odontologia.turno (sillon_id, inicio, id);
CREATE INDEX turno_paciente_idx ON odontologia.turno (paciente_id);
CREATE INDEX turno_prestacion_idx ON odontologia.turno (prestacion_id);

GRANT SELECT, INSERT ON odontologia.turno TO odonto_app;
