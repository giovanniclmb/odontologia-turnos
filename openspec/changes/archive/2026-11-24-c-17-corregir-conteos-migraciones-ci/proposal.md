# Proposal

## Why

El job `backend-verify` de GitHub Actions (run `45507223503`, SHA `92acf53`) ejecuta correctamente la compilación y las pruebas unitarias, pero Failsafe falla en cuatro pruebas de integración porque sus conteos acumulados todavía esperan 8 o 10 migraciones. El repositorio contiene actualmente las migraciones V1–V11; actualizar únicamente esas expectativas y el nombre del test resumen restablece una verificación coherente con el historial vigente, sin revertir el arreglo de compilación de `92acf53`.

## What Changes

- Corregir de 10 a 11 las expectativas de conteo acumulado en `AccesoPersistenciaIT`, `CuentasPacienteMigracionIT` y `RecursosMigracionIT`.
- Corregir de 8 a 11 el total acumulado de `SchemaIT` y renombrar su test para que describa once migraciones.
- Conservar las aserciones dirigidas a una versión concreta (incluidas las pruebas de V9 y V10) y no modificar producción, migraciones ni comportamiento.

## Capabilities

### New Capabilities

- Ninguna.

### Modified Capabilities

- Ninguna. Este cambio ajusta expectativas de pruebas de integración y no altera requisitos ni comportamiento observable del sistema; por ello declara `skip_specs: true`.

## Impact

- Solo los cuatro archivos de pruebas de persistencia bajo `backend/src/test/java/ar/edu/utn/odontologia/persistencia/` identificados en el alcance.
- La verificación afectada es `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify`, que ejecuta Failsafe contra PostgreSQL de Testcontainers.
- Sin cambios en código de producción, migraciones, APIs, dependencias, esquema ni especificaciones de capacidades.
