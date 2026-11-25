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

## Addendum de prueba real posterior — 2026-11-24 23:36:45 (America/Mendoza)

La afirmación anterior describe el estado al momento de ese addendum. Posteriormente se ejecutó una vez `CorreoRealManual` con autorización local: Maven salió 0, 1 test, 0 fallos/errores/omisiones; el API de Resend respondió 2xx y el registro de PostgreSQL temporal quedó `sent` con un intento. Esto confirma aceptación del API, pero no la entrega final. El adaptador no conservó el ID del proveedor; sigue pendiente revisar el evento de entrega en Resend y confirmar recepción en el buzón. C-17 permanece incompleto hasta registrar esa constancia.

## Confirmación posterior del destinatario — 2026-11-24 23:40:19 (America/Mendoza)

El usuario confirmó que recibió el mensaje. Esta confirmación se registra sin guardar la dirección destinataria. No se consultó el panel de Resend ni se capturó el ID/estado del proveedor, por lo que no se declara un evento `delivered`. C-17 permanece incompleto hasta contar con la constancia de entrega del proveedor.

## Verificación final del proveedor — 2026-11-24

En una revisión posterior del panel Emails de Resend, el mensaje apareció como `Delivered`, con ID `01a21090-09ec-7520-8d10-61817a35d31b`. El usuario también confirmó recepción en su buzón. Esto completa localmente la evidencia 6.1. Los addenda previos registran el estado anterior a esa revisión. No se actualizó el issue remoto ni se incluyó la dirección destinataria.
