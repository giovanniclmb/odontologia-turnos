# Verify — C-02

Fecha: 19/11/2026. Responsable: Giovanni. Propuesta y Apply aprobados expresamente por el usuario. Rama: codex/c-02-persistencia, worktree aislado existente. Estado: implementado y verificado; pendiente de archivo e integración. Sin push.

## Resultado integrado observado

`./scripts/check-docker.sh` y `mvn -B -ntp -f backend/pom.xml clean verify` finalizaron correctamente. Surefire: 55 pruebas unitarias. Failsafe: 61 pruebas de integración. Cero fallos, errores u omisiones. El JAR ejecutable también se lanzó con credenciales temporales, terminó con código 0 y no inició servidor HTTP.

La línea base de C-01 pasó antes y después del cambio de build. Se comprobaron referencias, restricciones SQL reales, transacciones/lecturas nuevas, conservación entre arranques, migración aplicada una sola vez, checksums e historial futuro, permisos e identidades, configuración ausente, credenciales erróneas y conexión inaccesible.

## TDD y revisión

- 01-baseline y 02-boot-unit-tests: 55 pruebas unitarias correctas.
- infra-bootstrap-red/green: roles y esquema ausentes antes de implementar; tres pruebas correctas después.
- 04-red-schema / 05-green-schema: ausencia y creación de las cuatro tablas.
- 06-red-constraints / 07-green-schema-constraints: 24 fallos esperados de restricciones antes de implementarlas; luego 30 pruebas correctas del conjunto SQL.
- 08-red-jpa-uuid / 09-green-jpa-uuid: cinco fallos por UUID no generado; luego persistencia y reinicio correctos.
- 10-red-arranque y 11-red-configuracion / 12-green-arranque: bean y validaciones faltantes; luego diez casos correctos.
- 13 a 17: regresiones de revisión independiente y correcciones, detalladas en [review.md](review.md).
- 18-clean-verify: resultado del árbol integrado con 55 unitarias y 61 IT correctas.

[trazabilidad.md](trazabilidad.md) enlaza cada escenario con su prueba. Las migraciones solo se iteraron sobre contenedores desechables del proceso de desarrollo antes de promover V1; no se reparó ni alteró el historial de una base persistente.

## Infraestructura y límites

Compose validó sintaxis/configuración y un smoke temporal comprobó healthcheck, autenticación y acceso al esquema. El preflight devolvió 1 con daemon inaccesible o Docker ausente, sin detener Docker. Testcontainers no usa condiciones de omisión. El proyecto Compose temporal se cerró y las IT cierran sus propios contenedores. No se modificó Postgres.app, bases preexistentes ni volúmenes persistentes del usuario.

El archivo .env.example solo contiene claves y valores no sensibles; sus contraseñas están vacías. Para usar el entorno de desarrollo persistente, el usuario debe completar las credenciales privadas y arrancar su Compose según backend/README.md. No se almacenaron credenciales del usuario ni se dejó una base de desarrollo con contraseñas inventadas.

CI fue adaptado y su flujo se verificó localmente; la ejecución remota queda pendiente de un pedido de push. No hay API, cuentas, datos clínicos ni tabla de turnos. No se prueba todavía exclusión concurrente de agenda. La evidencia humana académica sigue pendiente donde corresponda.
