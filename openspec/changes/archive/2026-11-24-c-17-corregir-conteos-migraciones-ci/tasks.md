# Tasks

## 1. Confirmar y corregir las expectativas acumuladas

- [x] 1.1 Antes de editar, ejecutar `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify` y confirmar que las cuatro pruebas de integración fallan únicamente por esperar 8/10 migraciones frente a las 11 ejecutadas; conservar la salida como evidencia TDD roja.
- [x] 1.2 En `AccesoPersistenciaIT`, `CuentasPacienteMigracionIT` y `RecursosMigracionIT`, actualizar solo las tres expectativas acumuladas de 10 a 11; verificar que el diff muestra exactamente esos cambios y que las pruebas dejan de reportar ese desajuste.
- [x] 1.3 En `SchemaIT`, actualizar de 8 a 11 el total acumulado y renombrar el método para indicar once migraciones; verificar que la aserción de catorce tablas y la comprobación de no reaplicación siguen intactas.

## 2. Verificar el cambio completo

- [x] 2.1 Revisar el diff de los cuatro tests y confirmar que no cambian producción, scripts de migración ni aserciones dirigidas a versiones concretas como V9/V10; verificar además que no se reintroduce el error de compilación de `92acf53`.
- [x] 2.2 Ejecutar `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify` y confirmar `BUILD SUCCESS`, incluidos Failsafe y PostgreSQL/Testcontainers, con cero fallos y errores.
