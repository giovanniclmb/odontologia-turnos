-- C-20: cuándo se fijó el horario vigente. NULL identifica turnos anteriores a este registro.
ALTER TABLE odontologia.turno
    ADD COLUMN horario_fijado_en timestamptz NULL;

-- La revisión de recordatorios busca cada minuto por inicio.
CREATE INDEX turno_inicio_idx ON odontologia.turno (inicio);

GRANT UPDATE (horario_fijado_en) ON odontologia.turno TO odonto_app;
