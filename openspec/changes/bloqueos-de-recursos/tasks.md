# Tasks

## 1. Base y persistencia

- [x] 1.1 Ejecutar el safety net del backend con Maven y PostgreSQL real, registrar resultados y conservar la evidencia TDD por tarea.
- [x] 1.2 Agregar V6 y el mapeo de bloqueos mediante RED/GREEN. Verificar en PostgreSQL las tres combinaciones de recursos, las claves foráneas, los períodos inválidos y las operaciones permitidas al rol de aplicación.

## 2. Contrato de aplicación

- [x] 2.1 Implementar la consulta interna de bloqueos por período mediante RED/GREEN. Verificar coincidencias por sillón y profesional, coincidencia con cualquiera de ambos, recursos ajenos y ambos bordes contiguos.
- [x] 2.2 Implementar el protocolo compartido de locks. Verificar en PostgreSQL con transacciones independientes que el mismo recurso serializa escrituras, que los recursos disjuntos permiten progreso independiente y que el orden de adquisición es común.
- [x] 2.3 Implementar el alta y la consulta de ocupación bajo los locks mediante RED/GREEN. Verificar permiso, entradas inválidas, referencias inexistentes, alta de cada combinación, turno ocupante conflictivo y ausencia de escrituras parciales.

## 3. Integración y evidencia

- [x] 3.1 Ejercitar en PostgreSQL el alta y el equivalente de escritura de turno con el contrato compartido. Verificar los dos órdenes de confirmación, rollback y el caso de reasignación con recursos de origen y destino mediante el fixture.
- [x] 3.2 Ejecutar la suite completa del backend y la validación estricta de OpenSpec. Registrar evidencia, puntos de integración de C-06 y C-07 y estado de C-15 en el roadmap.

## Evidencia TDD

Base aislada de `HEAD` anterior al cambio con Maven 3.9.16, JDK 26 compilando para Java 25 y PostgreSQL 18.7 en Testcontainers. Pasaron 95 pruebas unitarias y 98 de integración.

| Tarea | Prueba | Capa | RED | GREEN y triangulación |
| --- | --- | --- | --- | --- |
| 1.2 | `BloqueosMigracionIT` | PostgreSQL | Flyway no encontraba V6 | Migración desde V5, tres combinaciones, restricciones y permisos |
| 2.1 | `ConsultaBloqueosIT` | JPA y PostgreSQL | El contrato Java aún no existía | Coincidencias por cada recurso, recursos ajenos y ambos bordes |
| 2.2 | `LocksRecursosIT` | PostgreSQL | El protocolo de locks aún no existía | Transacción obligatoria, recurso compartido, recursos disjuntos y orden de adquisición observado en PostgreSQL |
| 2.3 y 3.1 | `AltaBloqueosIT` | Servicio y PostgreSQL | El servicio y el puerto de ocupación aún no existían | Permisos, entradas, tres combinaciones, rechazo sin escrituras parciales, ambos órdenes de confirmación y rollback |

La corrida completa de C-15 ejecutó 95 unitarias y 102 de integración. V6 exigió ajustar dos fixtures existentes: `ArranqueIT` pasó a simular V7 como versión futura y `PacientesMigracionIT` fijó V5 como destino del caso histórico. La reejecución de ambas clases sobre el código final pasó las 25 pruebas. Entre la suite completa y esa reejecución, las 197 pruebas quedaron verificadas.
