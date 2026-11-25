# Verificación de C-09

**Fecha:** 25/11/2026 (America/Argentina/Mendoza)

## Hallazgo y corrección

Antes de corregir, la prueba PostgreSQL `ConfirmacionMigracionIT` observó que Flyway encontraba dos migraciones con versión 11: `V11__email_envio.sql` (C-17, ya integrada en `main`) y `V11__confirmaciones_manuales.sql` (C-09). La prueba terminó con un error por versión duplicada. C-09 se movió a V12; V11 de email se mantuvo intacta. La prueba ahora migra V10, aplica V11 email y luego V12 confirmaciones, y verifica que la fila preexistente y su versión permanecen iguales hasta la edición autorizada.

## Pruebas ejecutadas

- `mvn clean test-compile -DskipTests`: pasó; compiló 97 fuentes principales y 53 de pruebas.
- Rojo de migración: `mvn -Dit.test=ConfirmacionMigracionIT failsafe:integration-test`; 1 prueba, 1 error por las dos versiones V11 descritas arriba.
- Verde focalizado: el mismo comando después de mover la migración; 1 prueba, 0 fallos, 0 errores, 0 omitidas. PostgreSQL 18.7 mediante Testcontainers; V10 → V11 → V12 aplicó una migración en cada paso.
- Primer `mvn clean verify`: 128 unitarias y 195 de integración; el único fallo fue la aserción del historial en `ArranqueIT`. La fixture aplica V13 futura con ubicaciones temporales, luego el arranque predeterminado la rechaza y conserva las 13 filas de historial. Se corrigió el conteo esperado a 13.
- Segundo `mvn clean verify`: **BUILD SUCCESS**; 128 unitarias y 195 de integración, 0 fallos, 0 errores, 0 omitidas. PostgreSQL 18.7 mediante Testcontainers.
- `openspec validate --specs`: 10/10 specs válidas; una advertencia de longitud en el requisito del catálogo de permisos. `openspec validate c-09-confirmaciones-manuales --type change`: válido.

## Revisión y límites

La revisión estática independiente de permisos, agenda y persistencia no encontró defectos concretos. El cambio no modificó las migraciones V1–V11 respecto de `origin/main`; C-09 añade V12. No se leyó `.env`, no se cargaron credenciales de email y no se ejecutó `CorreoRealManual` ni se envió correo.
