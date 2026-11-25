ALTER TABLE odontologia.turno
    DROP CONSTRAINT turno_estado_check,
    ADD CONSTRAINT turno_estado_check CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'CANCELADO'));
