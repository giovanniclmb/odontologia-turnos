ALTER TABLE odontologia.profesional ADD COLUMN activo boolean;
ALTER TABLE odontologia.sillon ADD COLUMN activo boolean;

ALTER TABLE odontologia.prestacion
    ADD COLUMN duracion_referencia_minutos integer CHECK (duracion_referencia_minutos > 0);

ALTER TABLE odontologia.usuario
    ADD CONSTRAINT usuario_profesional_fk
    FOREIGN KEY (profesional_ref) REFERENCES odontologia.profesional (id);

CREATE INDEX usuario_profesional_ref_idx ON odontologia.usuario (profesional_ref);

CREATE TABLE odontologia.configuracion_sede (
    fila smallint PRIMARY KEY CHECK (fila = 1),
    intervalo_minutos integer CHECK (intervalo_minutos > 0)
);

INSERT INTO odontologia.configuracion_sede (fila) VALUES (1);

GRANT SELECT ON odontologia.configuracion_sede TO odonto_app;
GRANT UPDATE (intervalo_minutos) ON odontologia.configuracion_sede TO odonto_app;
