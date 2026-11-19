#!/bin/sh
set -eu

: "${DB_PASSWORD:?Falta DB_PASSWORD para odonto_app}"
: "${DB_MIGRATION_PASSWORD:?Falta DB_MIGRATION_PASSWORD para odonto_migrator}"

psql --no-psqlrc --no-password --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    --set=ON_ERROR_STOP=1 --set=VERBOSITY=terse <<'SQL'
\getenv app_password DB_PASSWORD
\getenv migration_password DB_MIGRATION_PASSWORD
\getenv project_database POSTGRES_DB

BEGIN;
SET LOCAL log_statement = 'none';
SET LOCAL log_min_error_statement = 'panic';

CREATE ROLE odonto_migrator LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE
    PASSWORD :'migration_password';
CREATE ROLE odonto_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE
    PASSWORD :'app_password';
\unset migration_password
\unset app_password

REVOKE CONNECT, TEMPORARY ON DATABASE :"project_database" FROM PUBLIC;
REVOKE CONNECT ON DATABASE postgres, template1 FROM PUBLIC;
GRANT CONNECT ON DATABASE :"project_database" TO odonto_migrator, odonto_app;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

CREATE SCHEMA odontologia AUTHORIZATION odonto_migrator;
GRANT USAGE ON SCHEMA odontologia TO odonto_app;
COMMIT;
SQL