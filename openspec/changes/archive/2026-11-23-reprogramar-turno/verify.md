# Verificación de C-07

Giovanni aprobó el diseño escrito e iniciar Apply solo para C-07 el 23/11/2026 en Mendoza. La propuesta se guardó localmente en `2969b7f`, sobre `main` en `82a6e72` con C-06 integrado. V8 se aplica sobre V7.

## TDD

| Comando desde la raíz | RED observado | GREEN observado |
| --- | --- | --- |
| `mvn -f backend/pom.xml -Dit.test=ReprogramacionMigracionIT verify` | Flyway migró a V7 y rechazó el destino porque faltaba V8. | 95 unitarias y 1 integración con cero fallos, errores u omisiones. V8 conserva el turno de V7, inicializa versiones y limita las columnas actualizables. |
| `mvn -f backend/pom.xml -Dit.test=ReprogramacionPersistidaIT verify` | Con el contrato Java compilado y sin lógica de reprogramación, PostgreSQL ejecutó 21 casos. Se observaron 13 fallos y 8 errores por reprogramación pendiente y ausencia de espera por los locks. | El comando focal combinado de abajo pasó los 21 casos de reprogramación. |

Los comandos de integración usan PostgreSQL 18.7 temporal de Testcontainers.

## Verificación focal

`mvn -f backend/pom.xml -Dit.test=ReprogramacionPersistidaIT,ReprogramacionMigracionIT,AgendaPersistidaIT,AltaBloqueosIT verify` terminó con `BUILD SUCCESS` el 23/11/2026 a las 21:16:15 (Mendoza), con 95 pruebas unitarias y 40 integraciones, cero fallos, errores u omisiones.

## Suite completa antes de ampliar la cobertura de revisión

`mvn -f backend/pom.xml clean verify` terminó con `BUILD SUCCESS` el 23/11/2026 a las 21:41:17 (Mendoza). Pasaron 95 unitarias y 142 integraciones, con cero fallos, errores u omisiones. Incluye la consulta de un turno migrado desde V7 y la verificación de que devolver un destino idéntico mantiene la misma fila de PostgreSQL.

## Revisión independiente y cobertura adicional

Un subagente independiente GPT-6.2 Sol con razonamiento medio revisó la implementación en modo de solo lectura y no encontró defectos funcionales. Señaló ampliar la cobertura combinando la revocación de asignación durante una espera por la fila con una versión desactualizada.

Se agregó `priorizaAlcanceTrasEsperarLaFilaConVersionDesactualizada`, sin cambios de producción. La prueba observa la espera real por la fila con `pg_blocking_pids` y `pg_stat_activity`, confirma la desasignación en otra transacción y exige denegación de acceso antes de informar la versión desactualizada. Conserva el movimiento y la versión confirmados por recepción.

`mvn -f backend/pom.xml -Dit.test=ReprogramacionPersistidaIT#priorizaAlcanceTrasEsperarLaFilaConVersionDesactualizada verify` terminó con `BUILD SUCCESS` el 23/11/2026 a las 21:44:17 (Mendoza): 95 unitarias y 1 integración, sin fallos, errores ni omisiones. El revisor comprobó el caso y cerró la observación, sin hallazgos pendientes.

## Verificación final del integrador

`mvn -f backend/pom.xml -Dit.test=ReprogramacionPersistidaIT,ReprogramacionMigracionIT,AgendaPersistidaIT,AltaBloqueosIT verify` terminó con `BUILD SUCCESS` el 23/11/2026 a las 21:49:34 (Mendoza): **95 unitarias y 41 integraciones**, sin fallos, errores ni omisiones. Incluye los 22 casos de reprogramación, la migración y las regresiones de agenda y bloqueos.

La producción permaneció idéntica a la suite completa: se compararon las huellas SHA-256 de sus 75 archivos. La corrida final revalida las clases afectadas e incorpora la única prueba agregada después del `clean verify`; las otras clases conservan la evidencia de aquella suite completa. Los resultados de cada corrida se registran por separado.

OpenSpec registra las diez tareas de Apply completas. El change se archivó mediante el CLI y sus requisitos se sincronizaron con la especificación principal de agenda. La integración se gestiona mediante PR hacia `main`.

## Alcance de la evidencia de concurrencia

- Las carreras invocan `ServicioAgenda.reprogramar`, `ServicioAgenda.crear` y `ServicioBloqueos.crear` según el caso. Usan conexiones y transacciones independientes sobre las tablas definitivas. `pg_blocking_pids` comprueba que la segunda transacción espera a la primera.
- Los dos órdenes de confirmación y las reversiones verifican el resultado de ambas llamadas, el estado final de la agenda y de los bloqueos, y que antes del commit la consulta independiente aún vea la asignación de origen.
- Dos solicitudes con la misma versión se serializan sobre el turno. La segunda recibe el conflicto que pide volver a consultar si la primera confirmó el cambio. Los recursos cruzados de dos turnos distintos se adquieren juntos en orden uniforme. El token anterior al movimiento de ida y vuelta queda desactualizado.
- Las pruebas de espera fuerzan la desasignación del paciente o el vencimiento del inicio original antes de liberar los locks. La reprogramación se deniega o rechaza y conserva el turno.

La evidencia de concurrencia corresponde a los servicios que participan del contrato transaccional de C-15. Se usaron bases temporales con datos ficticios. El alcance comprobado es el servicio interno de C-07.
