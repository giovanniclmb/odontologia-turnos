# Procedimiento para la prueba real de C-17

Este procedimiento envía **un único correo sintético** mediante el bean `CorreoSalida` real y un PostgreSQL temporal de Testcontainers. No usa la base de desarrollo ni se ejecuta durante la suite automática.

## Requisitos antes de habilitar el envío

- Tener una cuenta Resend y guardar localmente su API key como `RESEND_API_KEY`; no pegarla en chat, código, consola con eco ni capturas.
- Configurar `EMAIL_FROM` con un remitente de un dominio de envío ya verificado en esa cuenta. `onboarding@resend.dev` solo sirve para enviar al correo de la cuenta Resend.
- Poner exactamente el destinatario aprobado en `EMAIL_TEST_ALLOWLIST` y en `C17_REAL_EMAIL_TO`. El test falla antes de abrir la conexión HTTP si la allowlist no contiene únicamente ese destinatario.
- Definir `EMAIL_MAX_RETRIES=0` para que el adaptador haga como máximo una llamada HTTP. La petición incluye una clave estable de idempotencia para este mensaje sintético.
- Para habilitar esta prueba de manera deliberada, establecer `C17_REAL_EMAIL_FROM_VERIFIED=YES` tras comprobar el remitente en el panel y `C17_REAL_EMAIL_CONFIRM=SEND_ONE_C17_TEST` justo para esta ejecución.

Se pueden guardar estas variables solo en el `.env` local ignorado por Git. Maven no carga `.env` automáticamente; desde `backend/`, cargarlo sin imprimir valores en una shell local con `set -a; . ../.env; set +a`. No mostrar el contenido de `.env` ni ejecutar comandos que impriman el entorno.

## Preflight y ejecución

Desde `backend/`, compilar las pruebas sin enviarlas:

```sh
mvn -q test-compile
```

Solo después de confirmar los requisitos anteriores, ejecutar el test manual por selección explícita:

```sh
mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=CorreoRealManual
```

El archivo `CorreoRealManual.java` no coincide con los patrones automáticos de Surefire/Failsafe. La suite CI no lo descubre. Para comprobar únicamente las guardas sin credenciales ni email, seleccionar explícitamente el test con `C17_REAL_EMAIL_CONFIRM=PREFLIGHT_ONLY mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=CorreoRealManual`; esta modalidad valida datos sintéticos y termina antes de crear PostgreSQL o contactar al proveedor. Para el envío real, la selección explícita, `C17_REAL_EMAIL_CONFIRM=SEND_ONE_C17_TEST` y las demás confirmaciones locales son necesarias. El test crea y cierra su propio `PostgresFixture` de Testcontainers solo en la modalidad de envío.

El asunto es `Prueba real de transporte C-17`; el cuerpo solo describe la comprobación e indica que no contiene datos clínicos o personales. El registro PostgreSQL debe terminar en `sent`, con un intento. Si la API rechaza el mensaje, hay timeout o falla una persistencia, **no volver a ejecutar** hasta revisar el panel de Resend. Un timeout puede dejar incierta la aceptación externa.

## Constancia

El adaptador confirma `exitoso` cuando Resend responde 2xx, pero no conserva el `id` JSON que devuelve `POST /emails`. Por eso buscar el mensaje en la página Emails del panel por asunto y hora, anotar el ID del proveedor si está disponible y revisar su cronología. La respuesta 2xx prueba aceptación del API; el evento `sent` indica que el proveedor procesa la entrega; `delivered` indica que el servidor de correo receptor la aceptó. Confirmar además que el destinatario recibió el mensaje. [API oficial de envío](https://resend.com/docs/api-reference/emails/send-email), [claves de idempotencia](https://resend.com/docs/dashboard/emails/idempotency-keys), [estados y diagnóstico de entrega](https://resend.com/guides/email/users-never-got-login-code).

Resend conserva las claves de idempotencia durante 24 horas y exige el mismo payload en los reintentos. No se debe repetir esta prueba, en especial pasado ese plazo: una nueva petición podría producir otro mensaje. Registrar en la evidencia compartible solo fecha/hora, resultado de la prueba, estado observado y el ID del proveedor; no publicar la dirección, el cuerpo íntegro, credenciales ni capturas que los revelen.

## Resultado de la ejecución autorizada

El 24-11-2026 a las 23:36:45 (America/Mendoza) se ejecutó una vez el test manual: Maven terminó con código 0 y reportó 1 prueba, 0 fallos, 0 errores y 0 omitidas. La API respondió 2xx y el registro temporal quedó `sent`, con un intento. El usuario confirmó recepción en su buzón a las 23:40:19. En una revisión posterior, el panel de Resend mostró `Delivered`, ID `01a21090-09ec-7520-8d10-61817a35d31b`. El adapter no retiene ese ID. La evidencia 6.1 queda completa localmente; no se repitió el envío.
