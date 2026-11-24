ALTER TABLE odontologia.turno
    ADD COLUMN version bigint NOT NULL DEFAULT 0 CHECK (version >= 0);

GRANT UPDATE (profesional_id, sillon_id, inicio, fin, version) ON odontologia.turno TO odonto_app;
