# Proposal

## Why

El equipo necesita mover un turno existente conservando su prestación y duración. La reprogramación debe proteger la reserva vigente si falla el cambio y evitar que una edición desactualizada sobrescriba un movimiento ya confirmado.

## What Changes

- Agregar la reprogramación al servicio interno de agenda para US-006 y RN-AG-06. El operador envía el turno, la versión consultada y la asignación de destino. Solo se mueven turnos pendientes que todavía no comenzaron, con un destino estrictamente futuro.
- Recepción puede cambiar horario, profesional y sillón dentro de la sede. Odontología puede cambiar horario y sillón de sus propios turnos de pacientes asignados, conservando el profesional.
- Conservar el identificador, paciente, prestación, duración efectiva y estado. El cambio efectivo de asignación actualiza la versión. Si el destino normalizado es idéntico, se devuelve el turno vigente tras las comprobaciones de acceso, versión y horario.
- Mantener la reserva vigente ante disponibilidad insuficiente, datos inválidos, falta de acceso o una versión desactualizada. El servicio distingue esas respuestas y permite volver a consultar si cambió la versión.
- Agregar una versión opaca a las respuestas de alta y consulta de C-06. Conservar las llamadas existentes y reutilizar el contrato transaccional de C-15 para que la reprogramación comparta recursos con altas y bloqueos.

## Capabilities

### New Capabilities

### Modified Capabilities

- `agenda-persistida` incorpora reprogramación autorizada, control de versión y calificación de conflictos. Alta y consulta exponen la versión vigente de cada turno visible.

## Impact

- Servicio `ServicioAgenda`, DTO de turno, consultas de ocupación, persistencia JPA y PostgreSQL. Se propone la siguiente migración consecutiva sobre V7 para inicializar la versión de turnos existentes y limitar permisos de actualización.
- Se mantiene el alcance en el servicio backend. La interfaz, el transporte HTTP, la cancelación, la confirmación manual y las comunicaciones corresponden a otros changes.
- C-06 está integrado en `main` por el PR #34. Giovanni aprobó el diseño conversacional y la preparación de la propuesta. La revisión de los artefactos escritos y la autorización explícita de Apply siguen pendientes. [Issue #8](https://github.com/giovanniclmb/odontologia-turnos/issues/8)
