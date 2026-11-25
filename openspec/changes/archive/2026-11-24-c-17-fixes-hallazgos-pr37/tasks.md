# Tasks

## 1. Corrección del hallazgo P1 — migración

- [x] 1.1 Renombrar `V8__email_envio.sql` a `V11__email_envio.sql` por colisión con versiones existentes en `main` y verificar el estado del historial de migraciones. Verificación: revisión de nombres y validación de migraciones aplicadas.

## 2. Corrección del hallazgo P2 — configuración por placeholders

- [x] 2.1 Mapear los placeholders de entorno en `application.yaml` para `email.resend-api-key`, `email.from`, `email.test-allowlist` y `email.max-retries`, con defaults documentados. Verificación: arranque sin secretos versionados y configuración visible.

## 3. Corrección del hallazgo P2 — idempotencia atómica

- [x] 3.1 Diferenciar `DataIntegrityViolationException` de fallo de persistencia en `AdaptadorResend.guardarAtomicamente`. Verificación: pruebas de condición de carrera y fallo de persistencia.
- [x] 3.2 Ante condición de carrera, recuperar el registro existente y reportar su estado. Verificación: resultado previo de envío sin contacto con el proveedor.
- [x] 3.3 Ante fallo de persistencia, devolver fallo controlado sin contactar al proveedor. Verificación: resultado fallido sin excepción no controlada.

## 4. Corrección del hallazgo P2 — reintentos con idempotencia

- [x] 4.1 Enviar el header `Idempotency-Key` en cada intento HTTP de reintento hacia Resend. Verificación: rastreo de intentos con la misma clave.

## 5. Corrección del hallazgo P2 — contexto Spring

- [x] 5.1 Registrar el bean `CorreoSalida` en el contexto Spring mediante `CorreoSalidaConfig`. Verificación: arranque del contexto y resolución del puerto.

## 6. Verificación de regresión

- [x] 6.1 Ejecutar la suite backend de comunicaciones y la suite general de regresión (`mvn -f backend/pom.xml test` y `clean verify` según disponibilidad) y verificar el contrato de deduplicación, reintentos y best-effort.

## Workflow follow-up

- Archivar el change tras satisfacer los requisitos de revisión del proyecto.
