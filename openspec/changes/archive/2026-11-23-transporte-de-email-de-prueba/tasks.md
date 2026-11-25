# Tasks

## 1. Configuración y base

- [x] 1.1 Agregar `ConfiguracionEmail` con `@ConfigurationProperties(prefix = "email")` y `@Validated`, con valores por defecto para reintentos (2) y timeout (10s), y verificar que el arranque no falla sin variables de email.
- [x] 1.2 Documentar en `backend/README.md` (o `.env.example`) las variables `RESEND_API_KEY`, `EMAIL_FROM`, `EMAIL_TEST_ALLOWLIST`, `EMAIL_MAX_RETRIES`, `EMAIL_TIMEOUT` y verificar que no se versionan secretos.

## 2. Persistencia de envíos

- [x] 2.1 Crear migración Flyway `V8__email_envio.sql` (o siguiente libre coordinado por Giovanni) con tabla `odontologia.email_envio`, unique en `idempotency_key` e índices, y verificar arranque con `ddl-auto: validate`.
- [x] 2.2 Implementar entidad JPA `EmailEnvio` y repositorio `EmailsEnviados` con `save` e `findByIdempotencyKey`, y verificar prueba de persistencia con PostgreSQL real (Testcontainers).

## 3. Puerto y adaptador

- [x] 3.1 Definir `CorreoSalida`, `SolicitudEnvio` y `ResultadoEnvio` en paquete `comunicaciones`, y verificar compilación y uso por un test de contrato mínimo.
- [x] 3.2 Implementar `AdaptadorResend` con `java.net.http.HttpClient`, timeout y mapeo saneado de respuestas, y verificar con tests de contrato usando servidor HTTP de prueba o fake de transporte.
- [x] 3.3 Implementar `AllowlistEmail` que rechace destinatarios fuera de la allowlist antes de contactar al proveedor, y verificar tests de rechazo.

## 4. Idempotencia, reintentos y best-effort

- [x] 4.1 Implementar clave de idempotencia y flujo de deduplicación (no reenviar si ya `sent`), y verificar tests de idempotencia.
- [x] 4.2 Implementar reintentos con backoff corto y registro de intentos, y verificar tests de recuperación y agotamiento.
- [x] 4.3 Asegurar que `enviar` no propaga excepciones no controladas y devuelve resultado fallido, y verificar tests de fallo controlado.

## 5. Sustituibilidad en pruebas

- [x] 5.1 Implementar `CorreoSalidaFalso` con forzado de éxito/fallo y registro, y verificar tests de contrato del puerto.

## 6. Verificación e integración

- [ ] 6.1 (bloqueada: requiere RESEND_API_KEY y allowlist autorizada del equipo) Ejecutar prueba real autorizada a una dirección de la allowlist y registrar evidencia sin secretos ni direcciones privadas en capturas públicas.
- [x] 6.2 Ejecutar suite completa del backend (`mvn test` y failsafe) y verificar que no hay regresiones.

## Workflow follow-up

- Archivar el change tras satisfacer los requisitos de revisión del proyecto.
- Verificar el resultado archivado.