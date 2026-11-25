# Proposal

## Why

US-012 y RN-EM-02 exigen un recordatorio automático por email 24 horas antes del turno. C-17 dejó el transporte y C-19 el email de alta y reprogramación, pero el backend no tiene ninguna tarea programada que envíe el recordatorio. Sus dependencias, C-19 y C-07, están integradas.

## What Changes

Decisiones elegidas por el usuario por menú el 25/11/2026:

- El backend revisa cada minuto qué turnos entraron en las 24 horas previas a su inicio.
- Si estaba apagado a esa hora, envía al volver, siempre que el turno no haya comenzado.
- Un turno cuyo horario se fijó con menos de 24 horas de anticipación no recibe recordatorio.
- Lo reciben los turnos pendientes y confirmados; los cancelados no.
- Va al email de la ficha del paciente, con nombre, fecha y hora de Mendoza y la aclaración de que ya no puede cancelarse por enlace. A 24 horas el plazo de 48 ya venció: nunca lleva enlace.
- Si el envío agota sus reintentos, solo se registra.
- El envío automático está apagado por defecto y se activa por configuración.

Además:

- Se guarda cuándo se fijó el horario de cada turno (migración nueva).

Interpretaciones de esta propuesta, para aprobar junto con ella:

- «Fijar el horario» es dar de alta el turno o reprogramarlo cambiando su inicio. Cambiar solo profesional o sillón no lo modifica.
- Los turnos anteriores a este change no tienen ese dato y se consideran fijados con anticipación suficiente.
- Con exactamente 24 horas de anticipación, el turno recibe recordatorio.
- Cada horario de un turno admite un solo recordatorio, aunque después cambie el email del paciente.

## Capabilities

### New Capabilities

- `recordatorio-de-turno`: recordatorio automático por email antes del turno.

### Modified Capabilities

Ninguna.

## Impact

- Backend: agenda (dato del horario y consulta de turnos por recordar), comunicaciones (tarea periódica, composición y envío), migración, configuración `EMAIL_RECORDATORIO_ACTIVO` y pruebas.
- Sin dependencias nuevas ni cambios HTTP o de interfaz.
- No modifica la confirmación manual de recepción, el enlace de cancelación, el email de alta ni el transporte de C-17.
- No incluye reintentos posteriores, vista de fallos, otros canales ni otras anticipaciones.
- La PR abierta de C-21 también agrega una migración: la que se integre después debe renumerar la suya.
- La evidencia con envío real requiere autorización expresa del usuario.
