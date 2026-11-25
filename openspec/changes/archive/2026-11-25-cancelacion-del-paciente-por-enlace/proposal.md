# Proposal

## Why

El MVP permite que recepción cancele turnos, pero el paciente no tiene una vía propia para cancelar su turno. La regla de negocio exige que el paciente pueda cancelar por enlace hasta 48 horas antes y que la cancelación libere inmediatamente el horario. Hoy no existe ni la emisión/validación del enlace ni la autorización limitada que ese enlace debe otorgar sin exponer la autenticación interna ni datos clínicos. C-18 resuelve esa brecha de autorización y C-19 se ocupará de enviar el enlace por email.

## What Changes

- Nueva capacidad de cancelación del paciente por enlace, separada de la cancelación interna de `agenda-persistida`.
- Emisión de un token opaco aleatorio por turno elegible, persistido únicamente como hash y vinculado al turno y a su versión vigente.
- Validación del token, consumo y cancelación en una sola transacción atómica, con máximo un resultado exitoso ante solicitudes repetidas o concurrentes.
- Autorización limitada por token: permite cancelar turnos `PENDIENTE` y `CONFIRMADO` que aún no hayan comenzado y se encuentren dentro del plazo de 48 horas.
- Corte inclusivo de 48 horas medido como tiempo transcurrido entre instantes (`Instant`), sin fijar una zona horaria de sede no aprobada.
- No se emite token si al crear o reprogramar el turno quedan menos de 48 horas; C-19 omite el enlace en ese caso.
- La reprogramación de un turno invalida el token previo; C-19 envía un enlace nuevo.
- Contrato HTTP no mutante para `GET`: la cancelación solo ocurre tras una acción explícita del paciente mediante `POST`.
- Las respuestas no revelan el estado del turno ni datos ajenos en casos de token ausente, inválido, expirado o reutilizado.
- Integración con la cancelación transaccional existente de `agenda-persistida` sin convertirla en un bypass de la autorización interna.

## Capabilities

### New Capabilities

- `cancelacion-por-enlace`: emisión, validación, consumo y cancelación limitada de turnos por enlace del paciente, con token opaco hasheado, límite de 48 horas y protección frente a replay y concurrencia.

### Modified Capabilities

Ninguna. C-18 no cambia los requisitos de `agenda-persistida`; reutiliza su operación interna como integración y mantiene su frontera de autorización. La entrega del enlace por email corresponde a C-19.

## Impact

- Backend: nuevo servicio y persistencia para tokens de cancelación, integración con `ServicioAgenda`, contrato HTTP para validación y confirmación de cancelación, y pruebas de autorización, corte temporal, replay y concurrencia.
- Persistencia: nueva tabla o representación para tokens de cancelación por turno, sin modificar la migración de turnos existente.
- No hay cambios de roles, migraciones de turnos ni cambios en la interfaz en este change. La superficie visual del paciente corresponde a C-23.
- C-18 depende de C-08, ya integrado y archivado en `main`.
- No se implementa código en esta fase; solo se captura el plan OpenSpec.
