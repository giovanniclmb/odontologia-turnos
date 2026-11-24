# Proposal

## Why

El PR #37 (change `transporte-de-email-de-prueba`, C-17) fue revisado y recibió cinco hallazgos; el hallazgo [P1] se resolvió renombrando la migración de `V8__email_envio.sql` a `V11__email_envio.sql` por colisión con versiones ya existentes en `main`. Los cuatro hallazgos restantes, todos de severidad [P2], se aplicaron sobre el código ya integrado para endurecer el contrato del transporte de email antes de conectar los consumidores de negocio (C-19, C-20 y C-22).

## What Changes

- Documentar en `application.yaml` los placeholders de entorno que materializan la configuración tipada: `email.resend-api-key: ${RESEND_API_KEY:}`, `email.from: ${EMAIL_FROM:}`, `email.test-allowlist: ${EMAIL_TEST_ALLOWLIST:}` y `email.max-retries: ${EMAIL_MAX_RETRIES:2}`.
- En el adaptador de salida, distinguir una violación de unicidad de idempotencia (`DataIntegrityViolationException`) de un fallo de persistencia.
- Ante una condición de carrera en la adquisición de la clave de idempotencia, recuperar el registro existente y reportar su estado.
- Ante un fallo de persistencia, devolver un fallo controlado sin contactar al proveedor.
- Enviar el header `Idempotency-Key` en cada reintento HTTP hacia Resend.
- Registrar el bean `CorreoSalida` en el contexto de Spring mediante `CorreoSalidaConfig`.

## Capabilities

### New Capabilities

- Ninguna.

### Modified Capabilities

- `transporte-de-email`: se ajusta el comportamiento de deduplicación por idempotencia ante condiciones de carrera y fallos de persistencia, y se refuerza el reintento con la misma clave de idempotencia.

## Impact

- Backend Java 25 + Spring Boot 4.1.2: `ar.edu.utn.odontologia.comunicaciones` (adaptador `AdaptadorResend`, configuración, contexto Spring) y `backend/src/main/resources/application.yaml`.
- Sin cambios de esquema, HTTP, frontend ni diseño visual.
- Los consumidores futuros C-19, C-20 y C-22 conservan el contrato endurecido sin modificaciones.
