# Design

## Context

Ver `proposal.md` para la motivación. El backend ya usa Spring Boot 4.1.2, JPA/Hibernate y Flyway sobre PostgreSQL 18.7, con identidad por constructor y validación de entradas (RA-02). No existe aún paquete de comunicaciones ni tabla de envíos. La migración más reciente es V7 (turnos). C-17 depende de C-06 únicamente para compartir el esquema y las referencias de turno; no consume servicios de agenda en este alcance.

## Goals / Non-Goals

**Goals:** implementar el transporte real de email, su configuración tipada, el puerto neutro, la persistencia de envíos y la deduplicación, con tests de contrato y evidencia real a una dirección de prueba.

**Non-Goals:** lógica de negocio de alta/recordatorio/aviso, plantillas de correo, scheduling, reprogramación/cancelación por fallo de email (C-19/C-20/C-22), y HTTP/frontend.

## Decisions

### Paquete y contrato neutro
- Nuevo paquete `ar.edu.utn.odontologia.comunicaciones`.
- Interfaz `CorreoSalida` con método `enviar(SolicitudEnvio) -> ResultadoEnvio`. `SolicitudEnvio` contiene tipo de correo, turnoId (UUID nullable para casos sin turno), destinatario, asunto y cuerpo. `ResultadoEnvio` es un registro con `exitoso`, `intentos`, `error` (nullable) y `idempotencyKey`.
- El negocio (C-19+) dependerá solo de `CorreoSalida`, nunca del adaptador Resend ni del fake.

### Adaptador Resend
- `AdaptadorResend` implementa `CorreoSalida` usando `java.net.http.HttpClient` (JDK 25), evitando dependencias nuevas y manteniendo presupuesto cero.
- Endpoint `POST https://api.resend.com/emails`, header `Authorization: Bearer ${RESEND_API_KEY}`, body JSON con `from`, `to`, `subject`, `html`/`text`.
- Timeout configurable (`EMAIL_TIMEOUT`, default 10s). Mapeo de respuestas: 2xx → éxito; 4xx/5xx y timeout → fallo con motivo saneado (sin secretos).

### Configuración tipada
- `ConfiguracionEmail` con `@ConfigurationProperties(prefix = "email")` y validación `@Validated` (RA-02). Propiedades: `resendApiKey`, `from`, `testAllowlist` (lista), `maxRetries` (default 2), `timeout` (default 10s).
- Binding desde env: `EMAIL_*` a propiedades `email.*` mediante relajación de Spring Boot o mapeo explícito. Las propiedades no son obligatorias para arrancar; el adaptador falla de forma controlada si faltan al enviar.

### Allowlist
- `AllowlistEmail` valida que el destinatario esté en `EMAIL_TEST_ALLOWLIST`. Si no, se rechaza antes de contactar al proveedor y se registra `status=rejected_allowlist` (o fallo con motivo `destinatario no permitido`).

### Persistencia de envíos
- Entidad JPA `EmailEnvio` mapeada a `odontologia.email_envio`, con UUID de aplicación, campos: `tipo` (text), `turno_id` (uuid nullable), `destinatario`, `idempotency_key` (unique), `estado` (text: pending/sent/failed), `intentos`, `error` (text nullable), `creado_en`, `enviado_en` (nullable).
- Repositorio `EmailsEnviados extends Repository<EmailEnvio, UUID>` con `save`, `findByIdempotencyKey`.
- Migración Flyway `V8__email_envio.sql` (número coordinado por Giovanni; si V8 está tomado, usar el siguiente libre) con PK, unique en `idempotency_key`, índices por `(tipo, turno_id)` y por `destinatario`, grants SELECT/INSERT/UPDATE a `odonto_app`.

### Idempotencia y deduplicación
- Clave: `sha256(tipo + "|" + turnoId + "|" + destinatario)` o concatenación normalizada; se persiste en `idempotency_key`.
- Flujo `enviar`:
  1. Validar allowlist.
  2. Buscar registro por clave. Si existe con `sent`, devolver resultado previo sin enviar.
  3. Si existe con `failed` y no se solicita reintento explícito, devolver fallo previo (decisión de C-19+). En C-17, reintentar solo si `estado=pending/failed` y el llamador lo solicita; por defecto, un `failed` previo no se reenvía automáticamente (evita tormentas).
  4. Crear registro `pending`, enviar con reintentos, actualizar a `sent/failed` con intentos y error.

### Reintentos y backoff
- `IntentosEnvio` ejecuta hasta `1 + maxRetries` intentos (default 3 total) con backoff corto (100ms, 300ms) usando `Thread.sleep` dentro del hilo de envío. El backoff exacto es ajustable sin cambiar el contrato.

### Best-effort
- `CorreoSalida.enviar` no lanza excepciones no controladas: captura errores, registra y devuelve `ResultadoEnvio` fallido. El llamador decide qué hacer (C-19+).

### Sustituibilidad en pruebas
- `CorreoSalidaFalso` implementa el puerto, registra en memoria y permite forzar éxito/fallo; implementa el mismo contrato de resultado y persiste en `EmailEnvio` cuando hay repositorio disponible, o solo en memoria para tests unitarios.

## Risks / Trade-offs

- [Resend cambia límites o condiciones] → El puerto neutro permite cambiar de adaptador sin tocar el negocio; la allowlist y el registro permanecen.
- [Envío real en CI sin red] → Los tests de contrato usan el fake; la prueba real es manual/autorizada y se ejecuta solo con env configurado.
- [Carrera en idempotencia] → Unique constraint en `idempotency_key` protege contra duplicados concurrentes; el adaptador traduce violación de constraint a resultado previo.
- [Migración numerada en paralelo] → Número coordinado por Giovanni; no editar migraciones ya aplicadas.

## Migration Plan

1. Agregar `V8__email_envio.sql` (o siguiente libre) tras V7.
2. Aplicar con el usuario migrador (`odonto_migrator`) sobre base de desarrollo.
3. Verificar arranque con `ddl-auto: validate` y pruebas Testcontainers.
4. Rollback: nueva migración correctiva; no editar V8 ya aplicada.

## UI Mockups

Decision: no (no UI)

## Open Questions

- Duraciones exactas de backoff y timeout por defecto final pueden ajustarse en Apply sin cambiar el contrato.
- Mapeo detallado de códigos de error de Resend a mensajes saneados se define en implementación.