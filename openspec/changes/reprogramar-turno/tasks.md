# Tasks

## 1. Condiciones previas a Apply

- [x] 1.1 Registrar la revisión de los artefactos escritos y el pedido explícito de Apply de Giovanni. Giovanni aprobó Apply solo para C-07 el 23/11/2026 (Mendoza). `main` en `82a6e72` incluye C-06 y llega a V7. La siguiente migración disponible es V8.

## 2. Persistencia y versiones de agenda

- [x] 2.1 Aplicar TDD a la migración desde V7. Verificar en PostgreSQL real que los turnos existentes conservan sus datos y reciben una versión inicial. Comprobar la asignación de versión en nuevas altas, el arranque y que `odonto_app` pueda actualizar solo profesional, sillón, inicio, fin y versión, manteniendo denegadas las escrituras de identidad, paciente, prestación, duración y estado y la eliminación.
- [x] 2.2 Agregar la versión opaca al DTO de alta y consulta, conservando las firmas y el constructor existente. Verificar compatibilidad de las llamadas previas y que alta y consulta devuelvan la misma versión inicial real, con el alcance y orden de C-06. Probar en PostgreSQL las consultas de filas migradas y nuevas.

## 3. Reprogramación autorizada

- [x] 3.1 Aplicar TDD al permiso y alcance. Probar que recepción puede cambiar profesional, odontología puede mover solo su turno para un paciente asignado sin cambiar de profesional y las solicitudes ajenas o no autorizadas no revelan datos ni modifican reservas. Verificar en PostgreSQL que la desasignación confirmada mientras espera los locks causa denegación.
- [x] 3.2 Aplicar TDD a la elegibilidad de origen y destino con `Clock` controlado. Verificar en PostgreSQL el rechazo del origen o destino igual o anterior al instante de validación, y que se rechace cuando el turno comienza mientras espera los locks. Verificar el truncado a microsegundos antes de validar y que el fin conserve la duración efectiva completa.
- [x] 3.3 Aplicar TDD a la disponibilidad de destino. Verificar en PostgreSQL la exclusión exclusiva del UUID propio, recursos inexistentes o inactivos, intervalo actual no configurado, margen exacto o insuficiente antes y después por profesional o sillón, bloqueos contiguos y solapados por un microsegundo en ambos bordes, y conflicto genérico cuando un turno ajeno ocupa el sillón. Cambiar la duración de catálogo después de un alta y comprobar que mover ese turno usa su duración efectiva persistida.
- [x] 3.4 Implementar la actualización explícita con UUID y versión esperada. Verificar en PostgreSQL el privilegio acotado con el `UPDATE` de producción, la nueva versión y asignación en la respuesta y la consulta posterior, incluso si la transacción ya leyó el turno. Comprobar que el destino idéntico devuelve la versión vigente sin escribir tras validar permiso, versión y tiempo. Ante datos inválidos, destino ocupado o rollback después de escribir, comprobar que UUID, paciente, prestación, duración, estado, asignación, versión y ocupación original se conservan.

## 4. Concurrencia de agenda

- [x] 4.1 Probar en PostgreSQL dos movimientos concurrentes con la misma versión del turno. Observar la espera y comprobar que el segundo pide recargar sin alterar el movimiento confirmado. Probar también una edición con versión previa a dos movimientos de ida y vuelta, y dos reprogramaciones de turnos distintos con recursos de origen y destino cruzados y períodos compatibles. Comprobar el resultado y las asignaciones finales.
- [x] 4.2 Probar en PostgreSQL `ServicioAgenda.reprogramar` frente a `ServicioAgenda.crear` y `ServicioBloqueos.crear` para un destino incompatible, en los dos órdenes de confirmación y con reversión de la primera operación. Usar servicios y tablas reales con conexiones y transacciones independientes, observar la espera, comprobar los resultados y verificar que el origen quede libre solo al confirmarse la reprogramación.

## 5. Verificación integrada

- [x] 5.1 Ejecutar `mvn -f backend/pom.xml clean verify` con PostgreSQL real de Testcontainers. Registrar RED/GREEN, resultados de las carreras y limitaciones. Pasar `openspec validate reprogramar-turno --strict` y revisar los contratos de C-06 y C-15 después de integrar localmente C-07.

## Workflow follow-up

- Presentar la evidencia y la revisión de implementación de C-07. El archivo y la publicación se gestionan después de la autorización correspondiente.
