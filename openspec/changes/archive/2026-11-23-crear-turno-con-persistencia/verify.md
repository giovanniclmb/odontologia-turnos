# Verificación de C-06

Giovanni aprobó el diseño escrito e iniciar Apply solo para C-06 el 23/11/2026 en Mendoza. La rama local parte de `main` con C-15 integrado (`e4183f8`). La implementación agrega V7 y mantiene intactas V1 a V6.

## TDD

| Comando desde la raíz | RED observado | GREEN observado |
| --- | --- | --- |
| `mvn -f backend/pom.xml -Dit.test=TurnosMigracionIT verify` | Flyway no encuentra la versión objetivo 7 tras migrar hasta V6. | 95 unitarias y 1 integración. La actualización de V6 a V7 aplica una migración y verifica cuatro claves foráneas, duración, período, estado y privilegios de la aplicación. |
| `mvn -f backend/pom.xml -Dit.test=AgendaPersistidaIT verify` | Con el contrato del servicio compilado y sin su lógica, las 16 integraciones fallan por alta aún sin implementar y ausencia de espera por locks. | El comando combinado `mvn -f backend/pom.xml -Dit.test=AgendaPersistidaIT,TurnosMigracionIT verify` pasa 95 unitarias y 17 integraciones. |

Todas las integraciones focalizadas ejecutan PostgreSQL 18.7 temporal de Testcontainers con el esquema migrado cuando corresponde. El GREEN focal registró cero fallos, errores u omisiones.

## Suite completa anterior a los ajustes de revisión

`mvn -f backend/pom.xml clean verify` terminó con `BUILD SUCCESS` el 23/11/2026 a las 16:28:08 (Mendoza). Ejecutó 95 unitarias y 119 integraciones, con cero fallos, errores u omisiones. Los reportes locales se generan en `backend/target/surefire-reports` y `backend/target/failsafe-reports`.

## Correcciones de revisión

- **RED.** `mvn -f backend/pom.xml -Dit.test=AgendaPersistidaIT#deniegaAltaSiRecepcionQuitaLaAsignacionMientrasEsperaLosLocks verify` ejecutó el escenario contra PostgreSQL antes de la corrección. Detectó un fallo: el alta retornó sin `AutorizacionDenegadaException` después de que otra transacción quitó la asignación mientras el alta esperaba los recursos.
- **GREEN focal.** `mvn -f backend/pom.xml -Dit.test=AgendaPersistidaIT,TurnosMigracionIT verify` terminó con `BUILD SUCCESS` el 23/11/2026 a las 16:32:12 (Mendoza). Pasaron 95 unitarias y 18 integraciones con cero fallos, errores u omisiones. Incluye la denegación sin persistencia tras la desasignación y la aceptación de un bloqueo exactamente contiguo a turnos guardados, para profesional, sillón y ambos.

## Verificación final del integrador y revisión independiente

Después de las correcciones, el integrador ejecutó nuevamente `mvn -f backend/pom.xml clean verify`. Terminó con `BUILD SUCCESS` el 23/11/2026 a las 16:34:59 (Mendoza): **95 unitarias y 120 integraciones**, sin fallos, errores ni omisiones. Se contrastaron esos totales con los XML de Surefire y Failsafe.

Un subagente independiente GPT-6.2 Sol con razonamiento medio revisó la implementación y luego las correcciones. Confirmó el cierre del hallazgo de autorización durante la espera y de la cobertura de contigüidad inversa, sin hallazgos pendientes en la segunda revisión. Su revisión fue de solo lectura; la ejecución final de pruebas correspondió al integrador.

OpenSpec registra las nueve tareas de Apply completas. El change se archivó mediante el CLI y la capacidad se sincronizó con las especificaciones principales. La integración se tramita mediante PR hacia `main`.

## Qué prueban las carreras

- Dos altas que comparten solo profesional o solo sillón se serializan. Dos altas con recursos distintos pueden completar mientras la otra transacción sigue abierta.
- La carrera entre `ServicioAgenda.crear` y `ServicioBloqueos.crear` usa conexiones y transacciones independientes, las tablas definitivas y el adaptador real `ConsultaTurnosOcupantes`. `pg_blocking_pids` comprueba que la segunda operación espera en PostgreSQL. Ambos órdenes de confirmación producen un conflicto en la segunda llamada y una sola fila persistida. Si la primera transacción revierte, la segunda revalida y guarda su operación.
- La vista de odontología excluye turnos ajenos. La ocupación del mismo sillón impide el alta aunque ese turno no se vea y la respuesta de conflicto no devuelve datos ajenos.

La exclusión comprobada corresponde a escrituras realizadas por los servicios que participan en el protocolo de C-15. La consulta de agenda por sí sola no reserva recursos. Se verificó en contenedores temporales, sin migrar una base compartida. El alcance incluye alta y consulta internas de C-06.
