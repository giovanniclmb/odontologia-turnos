# Verify — actualización de migración de C-16

Fecha: 24/11/2026 (America/Mendoza).

## Alcance de la corrección

Por pedido de Giovanni, se actualiza localmente el PR #38 desde `be09662242b8d15ca8e53e419a3e2d9862dce493` con `main` en `146a962e70bc0f0349eabb97b57d9b90041b0414`. Se conserva el historial mediante merge, sin rebase ni force push. La migración de cuentas pendiente del PR pasa de V9 a V10 porque V9 ya pertenece a la cancelación de C-08.

Las migraciones V1–V9 se compararon byte por byte con `main` y se conservan intactas. El SQL de `V10__cuentas_paciente.sql` coincide exactamente con el de `V9__cuentas_paciente.sql` del PR original. Tampoco cambian las clases de autenticación, autorización o persistencia de cuentas, ni la lógica de agenda de `main`.

Se ajustan los conteos a diez migraciones y catorce tablas, el fixture de historial futuro a V11 y las referencias documentales de cuentas. La prueba histórica de reprogramación comprueba los permisos sobre V8 antes de arrancar la aplicación con el esquema completo. El cambio no modifica bases de datos compartidas ni su historial aplicado.

## Evidencia RED/GREEN

- **Base limpia del PR:** `mvn -f backend/pom.xml clean verify -Dit.test=CuentasPacienteMigracionIT` pasó 114 pruebas unitarias y una de integración, sin fallos, errores ni omisiones. Log local: `/tmp/odonto-pr38-baseline-clean.log`.
- El primer intento sin `clean` arrastró un recurso de C-08 en `target`; se descartó como comprobación de base y se repitió con compilación limpia.
- **RED tras incorporar main:** la misma comprobación limpia detectó `Found more than one migration with version 9` por los dos archivos presentes en el código. Log: `/tmp/odonto-pr38-migracion-red.log`.
- **RED de regresión ampliada:** antes de renombrar el archivo, dos pruebas de integración fallaron por esa misma colisión, incluida la nueva actualización desde la versión integrada. Log: `/tmp/odonto-pr38-upgrade-red.log`.
- **GREEN con V10:** la comprobación limpia pasó 114 pruebas unitarias y dos de integración, sin fallos, errores ni omisiones. Se comprueba una instalación nueva y la actualización desde V9: se conserva un paciente existente, se vincula su cuenta nueva y los checksums del historial anterior permanecen iguales. Log: `/tmp/odonto-pr38-migracion-green.log`.

## Revisión independiente

El revisor independiente, GPT-6 Luna con razonamiento medio, no encontró bloqueantes al revisar el diff, la conservación del SQL, los conteos, el fixture futuro y la regresión de actualización. Trabajó en modo de lectura y no ejecutó pruebas. `git diff --check` y la comprobación del diff preparado no reportaron errores.

## Verificación integrada

La primera suite completa detectó un error en `ReprogramacionMigracionIT`: al limitar el arranque a V8, Hibernate no encontraba la nueva entidad de cuentas de V10. Las 114 pruebas unitarias pasaron y hubo un error entre 176 integraciones. Log: `/tmp/odonto-pr38-final-verify.log`.

La corrección conserva las aserciones de datos y permisos históricos en V8, antes del arranque de Spring. Después arranca sobre el esquema completo con `ddl-auto: validate` y comprueba las versiones del turno reprogramado y del nuevo. La revisión independiente verificó ese orden y no encontró bloqueantes. La ejecución focal `mvn -f backend/pom.xml clean verify -Dit.test=ReprogramacionMigracionIT,CuentasPacienteMigracionIT` pasó 114 pruebas unitarias y tres de integración, sin fallos, errores ni omisiones. Log: `/tmp/odonto-pr38-historico-green.log`.

La suite completa posterior a ese ajuste, `mvn -f backend/pom.xml clean verify`, terminó con `BUILD SUCCESS` en 02:05 minutos: **290 pruebas**, 114 unitarias y 176 integraciones con PostgreSQL 18.7 real, sin fallos, errores ni omisiones. Los totales se corroboraron leyendo los XML de Surefire y Failsafe. Log: `/tmp/odonto-pr38-final-green.log`.

`openspec validate --specs` pasó las ocho capacidades; conserva una advertencia previa de longitud en acceso interno. `openspec validate cuentas-paciente --type spec --strict` y `git diff --check` pasaron. No quedan conflictos de merge. Al terminar se volvió a comprobar que `main` seguía en `146a962` y el PR remoto en `be09662`.

## Publicación

Corrección local en `codex/c-16-correccion-migracion`, basada en la rama del PR #38. No se autorizaron push, aprobación ni integración de este PR. La numeración debe coordinarse de nuevo si otro PR incorpora una migración antes de publicarlo o integrarlo.
