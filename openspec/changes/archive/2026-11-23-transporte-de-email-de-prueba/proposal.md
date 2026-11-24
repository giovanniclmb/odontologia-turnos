# Proposal

## Why

C-17 habilita el canal real de email gratuito que comparten los tres tipos de correo del sistema (alta, recordatorio y aviso de hueco). Sin un transporte probado y auditable, C-19, C-20 y C-22 no pueden demostrar RN-EM-01, RN-EM-02 y RN-LE-02 ni la evidencia real exigida por RN-EM-03 y RN-TR-01. La decisión aprobada el 24/11/2026 fija Resend como proveedor gratuito, semántica best-effort con registro y un contrato neutro que desacopla el negocio del proveedor.

## What Changes

- Agregar un puerto neutro `comunicaciones.CorreoSalida` con `enviar(...) -> ResultadoEnvio`, implementado por un adaptador Resend y por un fake para pruebas.
- Agregar configuración tipada y validada para `RESEND_API_KEY`, `EMAIL_FROM`, `EMAIL_TEST_ALLOWLIST`, `EMAIL_MAX_RETRIES` (default 2) y `EMAIL_TIMEOUT`, sin secretos versionados.
- Agregar la tabla `email_envio` (próxima migración Flyway, número coordinado por Giovanni) con clave de idempotencia `(tipo, turnoId, destinatario)`, estados `pending/sent/failed`, intentos y error.
- Implementar el envío best-effort: 2 reintentos con backoff corto, registro de éxito/fallo, deduplicación por clave de idempotencia. El transporte no interrumpe la operación de negocio; C-19/C-20/C-22 deciden según el registro.
- Restringir los envíos reales a la allowlist `EMAIL_TEST_ALLOWLIST` (placeholders en desarrollo hasta que el equipo complete direcciones reales).
- Proveer tests de contrato, errores, deduplicación e idempotencia, y una prueba real autorizada a una dirección de prueba del equipo.

## Capabilities

### New Capabilities

- `transporte-de-email`: transporte real de email gratuito, configuración, puerto neutro y registro auditable de envíos con deduplicación e idempotencia.

### Modified Capabilities

## Impact

- Backend Java 25 + Spring Boot 4.1.2: nuevo paquete `comunicaciones`, entidades JPA/Hibernate y migración Flyway sobre PostgreSQL 18.7.
- Variables de entorno nuevas (no obligatorias para arranque; obligatorias solo al enviar).
- Sin cambios de HTTP, frontend ni diseño visual. Sin lógica de negocio, plantillas ni scheduling (alcance de C-19/C-20/C-22).