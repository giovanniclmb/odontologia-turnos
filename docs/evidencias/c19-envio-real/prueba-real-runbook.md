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

## Resultado de la ejecución autorizada

El 25/11/2026 a las 15:33 (America/Mendoza), con autorización expresa del usuario, la validación previa pasó y se ejecutó una vez `SEND_ONE_C19_TEST`: Maven terminó con código 0, 1 prueba sin fallos. La prueba exige que el registro temporal quede `turno-alta`, `sent`, con un intento, lo que indica una respuesta 2xx de Resend. En el registro de la ejecución no apareció la credencial ni la dirección.

El mismo día el usuario confirmó por menú que el correo llegó. No se registraron el estado ni el identificador del panel de Resend. No repetir el envío.
