# Proposal

## Why

US-011 y RN-EM-01 exigen que el paciente reciba un email al crearse su turno, con el enlace de cancelación. C-17 dejó el transporte y C-18 el enlace, pero nada los conecta: el evento vigente no distingue alta de reprogramación, no se publica cuando faltan menos de 48 horas y nadie lo consume.

## What Changes

Decisiones elegidas por el usuario el 25/11/2026:

- Se envía al email guardado en la ficha del paciente; la lista de direcciones de prueba sigue filtrando.
- El texto lleva nombre del paciente, fecha y hora de Mendoza y el enlace con su plazo, sin prestación, profesional ni datos clínicos.
- El enlace usa una URL base configurable y lleva el token después de `#`, para la página que construirá C-23.
- Con menos de 48 horas se envía el email sin enlace.
- Reprogramar también envía un email, con la nueva fecha y el enlace nuevo si corresponde.
- Si se agotan los reintentos solo se registra el fallo.
- Un mensaje que quedó desactualizado antes del envío se descarta.

Además:

- **BREAKING** interno: el evento `EnlaceCancelacionEmitido` se reemplaza por un evento por cada alta o reprogramación, con el token opcional.
- El transporte deja de registrar la respuesta del proveedor, que podría contener el enlace.

## Capabilities

### New Capabilities

- `email-de-turno`: email al paciente cuando se crea o reprograma su turno.

### Modified Capabilities

- `cancelacion-por-enlace`: forma de la entrega interna del token.
- `transporte-de-email`: contenido admitido en el motivo de un fallo.

## Impact

- Backend: agenda (publicación del evento), comunicaciones (consumo, composición y envío asíncrono), configuración `EMAIL_CANCELACION_URL_BASE`, adaptador de salida y pruebas.
- Sin migraciones, dependencias nuevas, cambios HTTP ni de interfaz.
- Hasta C-23 el paciente recibe el enlace pero no puede completar la cancelación desde el navegador.
- No incluye recordatorio (C-20), reenvíos posteriores, vista de fallos ni email de cancelación o confirmación.
- La evidencia con envío real requiere autorización expresa del usuario.
