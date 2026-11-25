# Procedimiento para la prueba real de C-20

Envía **un único correo**: en un PostgreSQL temporal de Testcontainers prepara un turno ficticio y arranca la aplicación con el recordatorio activado, que lo envía sola en su revisión inicial. Recorre agenda, consulta de turnos por recordar, tarea programada, composición, adaptador y Resend. No usa la base de desarrollo ni se ejecuta en la suite automática.

El turno se da de alta con el reloj dos días atrás para que su horario conste como fijado con anticipación y hoy esté a menos de 24 horas. La aplicación que lo prepara no envía correos, así que no sale el email de alta.

## Requisitos

- Autorización expresa del usuario para esa ejecución y esa dirección de prueba.
- `RESEND_API_KEY` y `EMAIL_FROM` en un `.env` local ignorado por Git. `onboarding@resend.dev` solo entrega a la dirección de la cuenta de Resend.
- `EMAIL_TEST_ALLOWLIST` y `C20_REAL_EMAIL_TO` con exactamente el destinatario aprobado.
- `EMAIL_MAX_RETRIES=0`.
- `C20_REAL_EMAIL_FROM_VERIFIED=YES` tras comprobar el remitente.

No imprimir el entorno ni el contenido de `.env`.

## Ejecución

Desde `backend/`. Primero el ensayo, que usa un servidor local y no necesita credenciales:

```sh
C20_REAL_EMAIL_CONFIRM=ENSAYO mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=RecordatorioRealManual
```

Con la configuración cargada, validar sin enviar:

```sh
C20_REAL_EMAIL_CONFIRM=PREFLIGHT_ONLY mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=RecordatorioRealManual -Denvio.real=true
```

Solo con autorización, enviar una vez:

```sh
C20_REAL_EMAIL_CONFIRM=SEND_ONE_C20_TEST mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=RecordatorioRealManual -Denvio.real=true
```

El asunto es «Recordatorio de tu turno». El registro temporal debe quedar con un recordatorio `sent`, un intento, también después de una segunda revisión. Si falla o el resultado es incierto, **no repetir** sin revisar el panel de Resend.

## Constancia

Anotar fecha y hora, resultado de la prueba, estado observado en el panel y su identificador si está disponible, y la confirmación de recepción. No publicar dirección, cuerpo ni credenciales.

## Preparación

El 25/11/2026 el ensayo y la validación previa de la configuración pasaron. No se envió ningún correo real.
