# Verify — correcciones de C-03 / Issue #30

20/11/2026. Giovanni pidió resolver los pendientes y luego integrar; aprobó el contrato de cambio propio y ventana de cinco mediante menú. Rama de correcciones basada en el head f88c560 del PR #29. Alcance y criterios: [Issue #30](https://github.com/giovanniclmb/odontologia-turnos/issues/30).

## Evidencia local

`./scripts/check-docker.sh` y `mvn -B -ntp -f backend/pom.xml clean verify`: BUILD SUCCESS, 78 unitarias y 82 IT, sin fallos/errores/omisiones. Las nueve pruebas Python del asignador también pasaron; las tres especificaciones pasan `openspec validate --specs --strict`.

- `proceso-red/green.log`: regresión de allowlist antes/después de eliminar la herencia de entorno. Los diagnósticos no muestran valores del entorno padre.
- `cli-aislado.log`: CLI real en subproceso; crea usuario, conserva hash verificable, rechaza segundo bootstrap y no hereda propiedades Spring centinela del padre.
- `historial-schema-red/green.log`: tabla ausente antes de V3 y presente después sobre PostgreSQL temporal.
- `cambio-red/green.log`: cambio propio faltante antes de implementar y escenarios correctos después.
- `maven-cli.log`: comando Maven con propiedad `spring-boot.run.main-class`, settings vacíos y repositorio offline; primer bootstrap y rechazo del segundo.
- `clean-verify.log` y `verification.json`: resultado completo y hashes de evidencia.

## Escenarios del historial

HistorialCredencialesIT verifica ambos roles, contraseña actual incorrecta, ausencia de identidad, usuario desactivado, política inválida, rechazo de vigente/cuatro previas, aceptación al quedar fuera de la ventana, conservación entre arranques, dos cambios concurrentes con exactamente un éxito, rollback al retirar INSERT, restricciones SQL con SQLStates y permisos de solo SELECT/INSERT del historial. Los rechazos conservan los datos confirmados. Los hashes se conservan; no se almacenan contraseñas en claro.

La secuencia por usuario evita depender del reloj. El bloqueo de fila se mantiene durante validación/actualización/historial. V1/V2 permanecen intactas; V3 es la siguiente versión observada para esta integración. La regresión de migración futura ahora copia V1/V2/V3 y prueba V4 ausente del binario.

## Revisión y límites

Revisión de la misma sesión, sin afirmar independencia ni modelos no usados, conforme a la preferencia del usuario. Se revisaron permisos, rollback, concurrencia, ausencia de parámetro de usuario destino y aislamiento del hijo. No se alteraron Postgres.app, bases persistentes del equipo ni datos reales.

El cambio no agrega HTTP, recuperación, gestión de contraseñas ajenas ni ciclo de tokens. La política usa los datos personales disponibles en el modelo (nombre de usuario); no inventa nombre/email. El historial previo a V3 no se fabrica. El archivo histórico de C-03 conserva sus decisiones de origen y agrega la resolución de este seguimiento.

La ejecución remota de CI y la integración se comprueban después de publicar. La evidencia académica humana continúa pendiente donde corresponda.
