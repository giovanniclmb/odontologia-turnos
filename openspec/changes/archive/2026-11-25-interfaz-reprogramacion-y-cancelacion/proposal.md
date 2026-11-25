# Proposal

## Why

La agenda interna permite consultar y crear turnos. Para completar US-006 y US-007, el equipo necesita reprogramar o cancelar una reserva desde la misma agenda usando las operaciones transaccionales que ya existen.

## What Changes

- Ofrecer las acciones Reprogramar y Cancelar turno desde las vistas diaria y semanal.
- Precargar la asignación al reprogramar, mostrar paciente, prestación y duración efectiva, y ofrecer los cambios de inicio, sillón y profesional permitidos por rol.
- Pedir confirmación antes de cancelar y actualizar la agenda desde el servidor. El turno cancelado queda identificado en su horario y deja de ocupar los recursos.
- Exponer por HTTP las dos operaciones de `ServicioAgenda` y trasladar sus resultados a mensajes específicos de la interfaz.
- Reutilizar la agenda y el sistema visual aprobados. La cancelación por enlace, las notificaciones y la asignación de huecos corresponden a otros cambios.

## Capabilities

### New Capabilities

### Modified Capabilities

- `interfaz-agenda-y-alta` incorpora los recorridos de reprogramación y cancelación internas y su contrato HTTP.

## Impact

- Depende de C-07, C-08 y C-10. Aplica también los estados y transiciones vigentes que introdujo C-09.
- Afecta el controlador HTTP de agenda, el contrato de errores agenda y el frontend HTML/CSS/TypeScript de C-10.
- Reutiliza los servicios y la persistencia vigentes. El cambio prescinde de migraciones.
- Aplica la política visual `system-and-mockups` con la revisión 1 del sistema y mockups de C-12 sujetos a aprobación antes de Apply.
