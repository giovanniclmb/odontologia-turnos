# Proposal

## Why

La revisión del PR #44 (C-18, `cancelacion-del-paciente-por-enlace`), sobre `381b0bc`, encontró tres defectos: confirmar un turno dejaba sin efecto el enlace ya emitido; cancelar por enlace y reprogramar adquirían sus bloqueos en orden inverso y podían quedar en espera circular; y el plazo de 48 horas se evaluaba con el instante anterior a las esperas, por lo que podía aceptarse una cancelación vencida.

## What Changes

- El enlace conserva su validez cuando el turno se confirma, con el mismo plazo. Es la decisión que el usuario eligió el 25/11/2026 frente a invalidarlo y reemplazarlo.
- La cancelación por enlace adquiere sus bloqueos en el mismo orden que el resto de la agenda y vuelve a validar el enlace bajo ellos.
- El plazo y el comienzo del turno se evalúan con el instante consultado después de obtener todos los bloqueos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `cancelacion-por-enlace`: vigencia ante la confirmación, momento de evaluación del plazo y coordinación con otras operaciones de agenda.

## Impact

- Backend: `ServicioAgenda.confirmar`, `ServicioCancelacionPorEnlace`, `TokensCancelacionPersistidos` y una clase nueva de pruebas de integración.
- Sin migraciones, cambios de contrato HTTP, interfaz ni dependencias.
- No incorpora entrega ni envío del enlace: corresponde a C-19.
