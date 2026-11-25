# Procedimiento para la prueba real de C-19

Envía **un único correo** creando un turno ficticio en un PostgreSQL temporal de Testcontainers: recorre agenda, aviso posterior al commit, composición, adaptador y Resend. No usa la base de desarrollo ni se ejecuta en la suite automática.

## Requisitos

- Autorización expresa del usuario para esa ejecución y esa dirección de prueba.
- `RESEND_API_KEY` y `EMAIL_FROM` en un `.env` local ignorado por Git. `onboarding@resend.dev` solo entrega a la dirección de la cuenta de Resend.
- `EMAIL_TEST_ALLOWLIST` y `C19_REAL_EMAIL_TO` con exactamente el destinatario aprobado.
- `EMAIL_MAX_RETRIES=0`.
- `EMAIL_CANCELACION_URL_BASE`: sin C-23 conviene una dirección reservada, por ejemplo `https://turnos.example.invalid/cancelar`. El enlace no podrá usarse y su token pertenece a la base temporal.
- `C19_REAL_EMAIL_FROM_VERIFIED=YES` tras comprobar el remitente.

No imprimir el entorno ni el contenido de `.env`.

## Ejecución

Desde `backend/`. Primero el ensayo, que usa un servidor local y no necesita credenciales:

```sh
C19_REAL_EMAIL_CONFIRM=ENSAYO mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=EnvioTurnoRealManual
```

Con la configuración cargada, validar sin enviar:

```sh
C19_REAL_EMAIL_CONFIRM=PREFLIGHT_ONLY mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=EnvioTurnoRealManual -Denvio.real=true
```

Solo con autorización, enviar una vez:

```sh
C19_REAL_EMAIL_CONFIRM=SEND_ONE_C19_TEST mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=EnvioTurnoRealManual -Denvio.real=true
```

El asunto es «Tu turno en el consultorio». El registro temporal debe quedar `turno-alta`, `sent`, un intento. Si falla o el resultado es incierto, **no repetir** sin revisar el panel de Resend.

## Constancia

Anotar fecha y hora, resultado de la prueba, estado observado en el panel y su identificador si está disponible, y la confirmación de recepción. No publicar dirección, cuerpo, enlace ni credenciales.
