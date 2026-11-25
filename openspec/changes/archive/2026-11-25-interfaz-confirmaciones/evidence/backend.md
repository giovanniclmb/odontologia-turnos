# Verificación de backend — C-13

Fecha: 25/11/2026. Entorno: JDK 26 compilando con `--release 25`, Maven 3.9.16 y PostgreSQL 18.7 real mediante Testcontainers y Docker Desktop.

## Contrato HTTP y TDD

La línea base anterior al cambio ejecutó 129 pruebas unitarias y 24 pruebas de `ApiAgendaIT`, todas aprobadas. Para cada endpoint se observó primero el fallo HTTP 404 de la prueba nueva, y luego su aprobación tras implementar el adaptador.

La triangulación de `ApiAgendaIT` pasó 29/29 sobre PostgreSQL. Comprueba listado de toda la sede, proyección limitada a nombre y apellido, orden por inicio, exclusión de turnos iniciados, cancelados y confirmados, permisos y falta de sesión. Para el registro comprueba respuesta y versión nueva, acceso, entradas inválidas, inexistencia, versión desactualizada y estados no elegibles. La transición y sus comprobaciones concurrentes son responsabilidad del servicio de C-09 existente.

## Suite integrada

`mvn -o clean verify` ejecutó 129 pruebas unitarias sin fallos y 234 de integración con un fallo, sin errores ni pruebas omitidas. El único fallo fue `ArranqueJarIT`: el registro de arranque informó que el puerto 8080 estaba ocupado durante una ejecución concurrente de QA, impidiendo iniciar el servidor. Las otras 233 pruebas de integración pasaron.

Tras liberar y comprobar el puerto, se repitió `mvn -o -Dtest=ProcesoAisladoTest -Dit.test=ArranqueJarIT verify`: **1/1 integración y 1/1 unitaria aprobadas**, `BUILD SUCCESS`. Esto verifica la prueba afectada por separado; la ejecución completa inicial no terminó verde y no se presenta como una ejecución completa exitosa.
