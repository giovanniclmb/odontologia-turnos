# Design

## Context

Ver `proposal.md` para la motivación. El change `c-17-fixes-hallazgos-pr37` corrige los hallazgos del PR #37 sobre el transporte de email implementado en C-17, dentro de la capability `transporte-de-email`. El hallazgo [P1] se resolvió con la renumeración de la migración a `V11__email_envio.sql`. Los hallazgos [P2] se aplicaron sobre el adaptador, la configuración y el contexto Spring. Los consumidores de negocio (C-19, C-20, C-22) no se tocaron porque dependen del contrato que se ajusta aquí.

## Goals / Non-Goals

**Goals:**
- Garantizar la adquisición atómica de la clave de idempotencia y la deduplicación segura ante condiciones de carrera.
- Mantener el fallo de persistencia bajo el resultado controlado del puerto.
- Reintentar con la misma clave de idempotencia.
- Registrar el bean del puerto en el contexto Spring.

**Non-Goals:**
- Lógica de negocio de los correos, plantillas, scheduling, HTTP, frontend o reenvío automático por fallo. Fuera del alcance de esta corrección.

## Decisions

### Configuración por placeholders
- Los valores de `email.*` se materializan desde variables de entorno con placeholders y defaults: `${RESEND_API_KEY:}`, `${EMAIL_FROM:}`, `${EMAIL_TEST_ALLOWLIST:}` y `${EMAIL_MAX_RETRIES:2}`.
- No se versionan secretos; los defaults solo cubren parámetros no sensibles.

### Idempotencia atómica
- El guardado distingue `DataIntegrityViolationException` (violación de unicidad → condición de carrera) de cualquier otro fallo (→ `PersistenciaEmailException`).
- Ante condición de carrera se recupera el registro existente y se reporta su estado (`sent` o `failed`).
- Ante fallo de persistencia se devuelve un resultado fallido controlado sin contactar al proveedor.

### Reintentos con clave de idempotencia
- Cada intento HTTP hacia Resend, incluidos los reintentos, transporta el header `Idempotency-Key` con la clave derivada de `(tipo, turno, destinatario)`.

### Contexto Spring
- `CorreoSalidaConfig` expone el bean del puerto `CorreoSalida` implementado por el adaptador Resend.

## Risks / Trade-offs

- [Condición de carrera] → La unicidad de `idempotency_key` en base es la barrera atómica; el adaptador traduce la violación a estado recuperado.
- [Fallo de persistencia] → El guardado best-effort registra el fallo en el resultado sin propagar excepción; se acepta no persistir el intento afectado.

## Migration Plan

- Sin cambios de esquema en esta corrección. La renumeración de la migración se resolvió con la nueva versión libre `V11__email_envio.sql`; no se editan migraciones ya aplicadas.

## Open Questions

- El mapeo detallado de códigos de error de Resend a mensajes sanitizados sigue en decisión de implementación, sin cambio de contrato.
- La duración exacta del backoff y el timeout por defecto pueden ajustarse en Apply sin cambiar el contrato.

## UI Mockups

Decision: no (no UI)
