# Proposal

## Why

C-08 completa US-007 y RN-AG-07. El equipo necesita cancelar una reserva persistida y dejar su horario disponible de inmediato para que recepción pueda volver a asignarlo con las reglas de agenda vigentes.

## What Changes

- Incorporar la cancelación interna por identificador y versión esperada para recepción en toda la sede y para odontología en sus propios turnos de pacientes con asignación activa.
- Permitir cancelar un turno pendiente cuyo inicio siga siendo futuro al validar. Conservar el registro como cancelado y visible en la agenda por sillón, con una versión nueva y sin ocupación de profesional ni sillón.
- Confirmar en una sola transacción la cancelación y la liberación. La operación devuelve los datos actualizados del turno para identificar el período y los recursos liberados cuando la transacción confirma.

## Capabilities

### New Capabilities

### Modified Capabilities

- `agenda-persistida` incorporará la cancelación, la liberación transaccional y la consulta de turnos cancelados.

## Impact

- Servicio interno de agenda, persistencia de turnos, protocolo de concurrencia compartido con alta, reprogramación y bloqueos, y pruebas contra PostgreSQL.
- El alcance se limita al backend interno. La cancelación por enlace y sus 48 horas corresponden a C-18. Los avisos de huecos corresponden a C-22. La interfaz del equipo corresponde a C-12.
