-- C-02: referencias mínimas. No incluye turnos ni información clínica.

CREATE TABLE odontologia.paciente (
    id uuid PRIMARY KEY,
    codigo text NOT NULL UNIQUE CHECK (length(btrim(codigo)) > 0)
);

CREATE TABLE odontologia.profesional (
    id uuid PRIMARY KEY,
    codigo text NOT NULL UNIQUE CHECK (length(btrim(codigo)) > 0)
);

CREATE TABLE odontologia.sillon (
    id uuid PRIMARY KEY,
    codigo text NOT NULL UNIQUE CHECK (length(btrim(codigo)) > 0)
);

CREATE TABLE odontologia.prestacion (
    id uuid PRIMARY KEY,
    codigo text NOT NULL UNIQUE CHECK (length(btrim(codigo)) > 0)
);

GRANT SELECT, INSERT, UPDATE ON odontologia.paciente, odontologia.profesional, odontologia.sillon, odontologia.prestacion TO odonto_app;
