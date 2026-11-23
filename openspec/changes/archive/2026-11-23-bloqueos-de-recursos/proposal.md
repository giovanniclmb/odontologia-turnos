# Proposal

## Why

Recepción necesita registrar la indisponibilidad de sillones y profesionales sin invalidar turnos existentes (US-010 y RN-AG-10). C-05 ya aporta los recursos persistidos que C-15 necesita para ofrecer ese contrato a la agenda.

## What Changes

- Registrar un bloqueo temporal de un sillón, un profesional o ambos mediante el permiso existente `bloqueo:crear`.
- Rechazar conflictos con turnos ocupantes y ofrecer una consulta de indisponibilidad para C-06 y C-07.
- Definir el protocolo transaccional común para impedir que un turno y un bloqueo incompatibles se confirmen sobre el mismo recurso.
- Alcance de alta y consulta interna. La modificación, el desbloqueo y las rutas HTTP quedan fuera de C-15.

## Capabilities

### New Capabilities

- `bloqueos-de-recursos` — Alta de bloqueos, consulta por período y consistencia con las asignaciones de agenda.

### Modified Capabilities

Ninguna.

## Impact

- Backend Java y PostgreSQL. Se incorpora una migración Flyway y un contrato de aplicación.
- C-06 conectará el alta de turnos persistidos al contrato. C-07 hará lo mismo para reprogramar.
