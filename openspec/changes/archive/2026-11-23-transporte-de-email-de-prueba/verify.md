# Verification — C-17 transporte-de-email-de-prueba

Fecha: 2026-11-24

## 1. Backend

- Comando: `mvn clean verify` en `backend/` (JDK 25, Maven 3.9.16, Testcontainers PostgreSQL 18.7).
- Resultado: **BUILD SUCCESS**.
- Tests: **122 run, 0 failures, 0 errors, 0 skipped** (unit + integración con PostgreSQL real).
- Migración V8 (`email_envio`) aplicada y validada con `ddl-auto: validate`.

## 2. Cobertura del change

- [x] 1.1 `ConfiguracionEmail` con `@ConfigurationProperties(prefix="email")` y `@Validated`; arranque OK sin variables.
- [x] 1.2 Variables documentadas en `.env.example` y `backend/README.md`; sin secretos versionados.
- [x] 2.1 Migración `V8__email_envio.sql` con tabla, unique `idempotency_key`, índices y grants.
- [x] 2.2 Entidad JPA `EmailEnvio` + repositorio `EmailsEnviados`; IT con PostgreSQL real.
- [x] 3.1 Puerto `CorreoSalida` + `SolicitudEnvio`/`ResultadoEnvio`.
- [x] 3.2 `AdaptadorResend` con `java.net.http.HttpClient`, timeout, mapeo saneado, reintentos/backoff; tests con servidor HTTP de prueba.
- [x] 3.3 `AllowlistEmail` (rechazo antes de contactar proveedor).
- [x] 4.1 Clave de idempotencia SHA-256 + deduplicación.
- [x] 4.2 Reintentos con backoff corto y registro de intentos.
- [x] 4.3 `enviar` best-effort: no propaga excepciones, devuelve resultado fallido.
- [x] 5.1 `CorreoSalidaFalso` con forzado de éxito/fallo y registro.
- [x] 6.2 Suite completa backend verde.

## 3. Limitación conocida

- [ ] 6.1 Prueba real autorizada a una dirección de la allowlist: **bloqueada** hasta que el equipo autorice `RESEND_API_KEY` y `EMAIL_TEST_ALLOWLIST`. No se ejecutó para no versionar secretos ni enviar a direcciones no autorizadas. Se ejecutará manualmente cuando el equipo lo autorice y se registrará la evidencia (constancia de entrega del proveedor).

## 4. Recomendación

1. Autorizar credenciales y allowlist del equipo; ejecutar la prueba real y registrar evidencia sin secretos/direcciones privadas.
2. C-19 (email al crear turno), C-20 (recordatorio 24h) y C-22 (avisos de huecos) consumirán el puerto `CorreoSalida` de este change.
3. Archivar cuando 6.1 quede resuelto o aceptado como pendiente humano.

## Addendum de revisión posterior — 2026-11-24 (America/Mendoza; logs fechados 2026-11-25 UTC)

La verificación anterior es un registro histórico de la rama original y no acredita el estado integrado actual ni completa C-17. La mención a `V8__email_envio.sql` quedó superada por la colisión de versiones: la migración vigente es `V11__email_envio.sql`. El conteo de 122 pruebas también corresponde solo a aquella ejecución histórica; consultar `docs/evidencias/c17-correcciones/verify.md` para la verificación actual.

La tarea 6.1 continúa pendiente: no se usaron credenciales ni destinatarios reales y no se envió ningún correo. El usuario autorizó integrar el código verificado y mantener C-17 incompleto hasta que el equipo autorice y aporte la prueba real.
