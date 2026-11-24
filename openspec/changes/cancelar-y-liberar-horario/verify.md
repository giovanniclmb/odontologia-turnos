# Verify — C-08

## Base y secuencia Flyway

El 24/11/2026 a las 02:43:05 (Mendoza), `mvn -f backend/pom.xml clean verify` sobre la base de C-08 con los artefactos aprobados (`170cd1a`) pasó 95 pruebas unitarias y 143 integraciones con PostgreSQL 18.7, con cero fallos, errores u omisiones. El primer intento dentro del sandbox se interrumpió tras la denegación de acceso al socket de Docker. La ejecución con acceso a Docker completó la comprobación de base. Log local `/tmp/odonto-c08-baseline.log`.

Se comprobó en GitHub que `main` seguía en `66d55c0` y que los PR #37 y #38 seguían abiertos con migraciones V8 de sus ramas, en conflicto con V8 ya integrada de C-07. La próxima versión libre para este cambio era V9. La secuencia debe comprobarse nuevamente al integrar. [PR #37](https://github.com/giovanniclmb/odontologia-turnos/pull/37) · [PR #38](https://github.com/giovanniclmb/odontologia-turnos/pull/38)

## TDD y regresiones focalizadas

| Comprobación | RED observado | GREEN observado el 24/11/2026 (Mendoza) |
| --- | --- | --- |
| Migración desde V8 con turno existente | `mvn -f backend/pom.xml -Dit.test=CancelacionMigracionIT verify` ejecutó la prueba contra PostgreSQL y Flyway rechazó el destino V9 ausente. Log `/tmp/odonto-c08-migracion-red.log`. | A las 02:45:05, 95 unitarias y 66 integraciones con cero fallos, errores u omisiones. V9 conserva la fila previa, permite la transición y acota el nuevo privilegio a `estado`. Log `/tmp/odonto-c08-migracion-green.log`. |
| Servicio de cancelación | `mvn -f backend/pom.xml test-compile` detectó el contrato `SolicitudCancelacion` ausente. Con solo la firma y una implementación provisional que rechazaba toda cancelación, `mvn -f backend/pom.xml -Dit.test=CancelacionPersistidaIT verify` ejecutó 25 casos contra PostgreSQL y obtuvo 12 fallos y 13 errores por el comportamiento ausente. Logs `/tmp/odonto-c08-cancelacion-contrato-red.log` y `/tmp/odonto-c08-cancelacion-red.log`. | A las 02:47:45, 95 unitarias y 66 integraciones con cero fallos, errores u omisiones. Incluye los 25 casos nuevos de cancelación, 22 de reprogramación, 17 de agenda, uno de bloqueos y uno de locks. Log `/tmp/odonto-c08-cancelacion-green.log`. |

Comandos GREEN ejecutados desde la raíz del repositorio.

```sh
mvn -f backend/pom.xml -Dit.test=TurnosMigracionIT,ReprogramacionMigracionIT,CancelacionMigracionIT,SchemaIT,AccesoPersistenciaIT,RecursosMigracionIT,ArranqueIT verify
mvn -f backend/pom.xml -Dit.test=CancelacionPersistidaIT,AgendaPersistidaIT,ReprogramacionPersistidaIT,AltaBloqueosIT,LocksRecursosIT verify
```

## Evidencia de comportamiento

- Recepción tiene alcance global. Odontología requiere turno propio y asignación activa y recibe denegación genérica para turnos fuera de alcance. Las pruebas revocan la asignación mientras la sesión espera la fila o los recursos y adelantan el reloj durante la espera.
- Una cancelación conserva identificador, paciente, prestación, recursos, período y duración. La versión persistida aumenta una vez y la respuesta y la consulta muestran el estado nuevo incluso si JPA había cargado el turno antes. Las pruebas distinguen la versión desactualizada de la repetición con versión vigente y comprueban que el inicio igual o anterior al reloj invalida la operación.
- El turno cancelado sigue visible según los permisos y deja de impedir altas, reprogramaciones y bloqueos. Las nuevas reservas siguen comprobando el intervalo frente a otros turnos ocupantes y los bloqueos persistidos.
- Las carreras usan servicios reales, conexiones independientes y una comprobación de espera con `pg_blocking_pids`. Cubren dos cancelaciones, los dos órdenes frente a reprogramación y frente a alta o bloqueo, y la reversión de una cancelación. La transacción exterior puede tener el DTO cancelado mientras otra sesión sigue esperando. En commit la otra sesión revalida sobre el horario liberado, y en rollback observa el turno pendiente con su versión y ocupación originales.

## Revisión independiente

La revisión pidió comprobar por separado la contención con un bloqueo que comparte solo profesional o solo sillón. Se incorporaron ambos recursos de forma aislada en las pruebas de orden de validación y de confirmación o reversión exterior. `mvn -f backend/pom.xml -Dit.test=CancelacionPersistidaIT,SchemaIT verify` terminó a las 02:50:04 (Mendoza) con 95 unitarias y 58 integraciones, cero fallos, errores u omisiones. Incluye 28 casos de cancelación y 30 de esquema. Log `/tmp/odonto-c08-revision-green.log`.

## Verificación integrada

`mvn -f backend/pom.xml clean verify` terminó con `BUILD SUCCESS` el 24/11/2026 a las 02:54:06 (Mendoza). Pasaron **267 pruebas** con cero fallos, errores u omisiones: 95 unitarias y 172 integraciones con PostgreSQL 18.7 de Testcontainers. La suite incluye los 28 casos de cancelación y la migración con el rol de aplicación. Log `/tmp/odonto-c08-final-verify.log`.

`openspec validate cancelar-y-liberar-horario --strict` aprobó los artefactos del cambio. `git diff --check` pasó y se comprobó que el diff se limita al backend, los artefactos de C-08 y la documentación de sus decisiones y seguimiento en KB/CHANGES. La implementación y la revisión independiente usaron GPT-6.2 Sol con razonamiento medio. Queda cerrada la revisión de implementación y contención, sin hallazgos pendientes.

El cambio tiene 11/11 tareas completas. Su archivo, integración y publicación siguen pendientes; esta evidencia corresponde a la verificación local, sin push ni CI remota de C-08.
