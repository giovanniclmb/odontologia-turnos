# Design

## Context

Ver `proposal.md` para el motivo y el alcance. Flyway dispone de once scripts consecutivos, `V1`–`V11`; las cuatro aserciones indicadas cuentan el historial completo tras migrar una instalación nueva o comprobar el arranque. La corrección es de expectativas de pruebas, no de esquema ni de producto.

## Goals / Non-Goals

**Goals:**
- Alinear con el historial acumulado vigente únicamente las cuatro expectativas que CI identificó como obsoletas.
- Hacer que el nombre del test de `SchemaIT` refleje el total que verifica.
- Mantener la intención de cada prueba y conservar las comprobaciones cuyo valor identifica una versión objetivo.

**Non-Goals:**
- No renumerar, editar ni agregar migraciones; no cambiar código de producción ni contratos de capacidades.
- No revertir `92acf53`, que corrige un error de compilación independiente.
- No alterar configuración, workflow ni alcance de la verificación CI.

## Decisions

### Ajustar solo conteos acumulados

Cambiar las tres expectativas acumuladas de 10 a 11 en `AccesoPersistenciaIT`, `CuentasPacienteMigracionIT` y `RecursosMigracionIT`; cambiar la expectativa acumulada de 8 a 11 en `SchemaIT`. Renombrar `aplicaOchoMigracionesYCatorceTablasUnaSolaVez` para expresar once migraciones. Se elige una corrección localizada frente a reordenar scripts o cambiar la migración aplicada, porque el historial V1–V11 es el estado intencional y confirmado.

Las aserciones que validan una migración objetivo —por ejemplo, ejecución hasta V9 o V10, o la comparación del historial excluyendo V10— no son conteos acumulados y permanecen sin cambios.

### Verificación con las pruebas existentes

Antes de editar, ejecutar las pruebas de integración afectadas para documentar el fallo rojo por conteo. Tras la corrección, ejecutar los tests afectados y la verificación completa `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify`; no reemplazar PostgreSQL/Testcontainers por mocks ni omitir Failsafe.

## Risks / Trade-offs

- [El número total puede cambiar con futuras migraciones] → Mantener este total explícito como comprobación del estado completo y actualizarlo al agregar migraciones, distinguiéndolo de los tests dirigidos a versiones específicas.
- [Un reemplazo amplio podría alterar expectativas de V9/V10] → Limitar el cambio a las cuatro aserciones señaladas por CI y revisar el diff de los cuatro tests.

## Migration Plan

No hay migración de base de datos. Aplicar el ajuste de pruebas, ejecutar primero los tests afectados y luego el comando completo de CI. Si la verificación falla por causas distintas, conservar el diagnóstico y no ampliar este cambio sin revisar el alcance.

## Open Questions

Ninguna.

## UI Mockups

Decision: no (no UI)
