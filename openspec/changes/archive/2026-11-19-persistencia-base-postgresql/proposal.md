# Proposal

## Why

C-01 ya valida disponibilidad en memoria, pero los siguientes changes necesitan una base reproducible y referencias persistidas. C-02 establece ese soporte con PostgreSQL real y migraciones controladas, manteniendo las reglas de agenda separadas y sin exponer una aplicación todavía desprotegida.

## What Changes

- Incorporar Spring Boot 4.1.2 al backend Maven/Java 25, sin modificar el comportamiento del dominio ni sus 55 pruebas de C-01.
- Configurar Spring Data JPA/Hibernate con entidades separadas del dominio y Flyway como único mecanismo de evolución del esquema; Hibernate valida, no crea ni actualiza tablas.
- Crear cuatro referencias mínimas: paciente, profesional, sillón y prestación, cada una con UUID generado por la aplicación y código único por tipo.
- Proveer PostgreSQL 18.7 de desarrollo mediante Docker Compose y PostgreSQL temporal de Testcontainers para las pruebas de integración, con credenciales separadas de migración y ejecución.
- Iniciar Flyway con V1 y documentar versiones globales consecutivas coordinadas por Giovanni al integrar. No editar migraciones aplicadas ni habilitar out-of-order.
- Probar arranque, conexión, persistencia y restricciones reales, aplicación única de migraciones, fallos de configuración y separación de privilegios. Mantener datos ficticios y configuración privada fuera de Git.

## Capabilities

### New Capabilities

- `persistencia-de-referencias`: almacenamiento íntegro de las cuatro referencias de agenda y arranque reproducible sobre un esquema versionado, con aislamiento entre desarrollo y pruebas.

### Modified Capabilities

Ninguna. `disponibilidad-de-turnos` permanece sin cambios; se verifica como regresión de C-01.

## Impact

Responsable: Giovanni. Dependencia C-01 cumplida. Issue: [#3](https://github.com/giovanniclmb/odontologia-turnos/issues/3). Gobernanza CRITICO: esta propuesta requiere revisión y pedido posterior de Apply antes de código.

Archivos previstos para Apply: `backend/pom.xml`, arranque/configuración de Spring Boot, entidades y repositorios de referencias, `backend/src/main/resources/db/migration/V1__referencias_base.sql`, pruebas de integración, `compose.yaml`, bootstrap local de roles, `.env.example`, documentación y ajuste de CI para exigir Docker. Las decisiones técnicas fueron respondidas por Giovanni el 19/11/2026.

Fuera de alcance: API HTTP, seguridad de usuarios, cuentas, ficha clínica, catálogo completo, intervalo configurable, turnos persistidos, exclusión concurrente de agenda, UI, email, despliegue productivo y cambios en Postgres.app. PostgreSQL de desarrollo será una instancia separada. No se crean servicios ni bases durante Propose, ni se hace push.
