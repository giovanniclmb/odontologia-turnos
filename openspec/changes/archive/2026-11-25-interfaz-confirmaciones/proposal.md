# Proposal

## Why

Recepción ya puede registrar confirmaciones mediante el servicio de C-09, pero carece de una interfaz y de un contrato HTTP para consultar los pendientes y realizar el seguimiento manual desde la agenda.

## What Changes

- Incorporar a la agenda un listado de los turnos pendientes futuros de toda la sede, con datos suficientes para identificarlos y una acción de registro manual para recepción.
- Exponer la consulta y el registro por HTTP mediante los servicios existentes de C-09, manteniendo sus permisos, orden y control de versión.
- Comunicar el resultado, las versiones desactualizadas y los errores, y volver a consultar tras el registro sin representar como confirmado un resultado incierto.
- Reutilizar el sistema visual y la agenda existentes. No se incorporan envíos, canales, seguimiento automatizado ni nuevas reglas de negocio.

## Capabilities

### New Capabilities

### Modified Capabilities

- `interfaz-agenda-y-alta`: incorpora el listado y registro manual de confirmaciones y su contrato HTTP.

## Impact

- Depende de C-09 y C-10 implementados y archivados. Afecta el adaptador HTTP de agenda y el frontend HTML/CSS/TypeScript de la agenda.
- Reutiliza servicios y persistencia existentes sin migración prevista. El listado necesita una proyección con nombre y apellido del paciente para identificar el turno.
- Rige `system-and-mockups`: los mockups de este cambio necesitan aprobación antes de Apply.
