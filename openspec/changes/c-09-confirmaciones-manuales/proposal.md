# Proposal

## Why

Recepción necesita saber qué turnos futuros requieren seguimiento y registrar su confirmación manual en la agenda persistida. C-09 cubre US-008 y RN-AG-08 sobre la reserva y el control de versiones de C-06.

## What Changes

- Habilitar a recepción para consultar los turnos pendientes con inicio estrictamente futuro de toda la sede y confirmar uno por identificador y versión.
- Registrar `CONFIRMADO` en el turno, incrementar la versión y conservar la ocupación de profesional y sillón. Rechazar las repeticiones.
- Permitir que recepción cancele o reprograme turnos confirmados futuros. Un movimiento efectivo devuelve el turno a `PENDIENTE`.

## Capabilities

### New Capabilities

### Modified Capabilities

- `agenda-persistida` incorpora consulta y confirmación manual, define la ocupación de turnos confirmados y amplía su cancelación y reprogramación por recepción.
- `acceso-y-permisos-internos` reserva a recepción los permisos de consulta de pendientes y confirmación manual.

## Impact

Cambian los servicios Java de agenda, las consultas de ocupación, la persistencia y las pruebas PostgreSQL. Se reutilizan los permisos internos `pendientes:consultar` y `confirmacion:registrar`. La interfaz corresponde a C-13 y las comunicaciones automáticas a sus respectivos changes.
