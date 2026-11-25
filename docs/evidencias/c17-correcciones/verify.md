# Verificación de correcciones de C-17

Fecha: 2026-11-24 (America/Mendoza; log Maven fechado 2026-11-25 UTC)
Alcance: correcciones locales del transporte C-17 al integrar PR #37 con C-11. No se cambiaron la migración V11 ni consumidores de email.

## Hallazgos reproducidos (RED)

- `AdaptadorResendTest#noDevuelveExitoCuandoNoPuedePersistirResultadoFinal`: el servidor HTTP local devolvía 200 y el repositorio fallaba al guardar el estado terminal; el adaptador original respondía `exitoso=true` aunque el registro seguía `pending`.
- `AdaptadorResendTest#noReenviaCuandoLaClaveYaTieneUnEnvioPendiente`: con una fila `pending` preexistente, el adaptador original intentaba el envío HTTP y respondía éxito en vez de reportar el estado existente.
- `ConfiguracionEmailTest#enlazaElTimeoutDocumentadoDesdeConfiguracion`: `EMAIL_TIMEOUT=750ms` no tenía efecto y la propiedad conservaba el default de 10 segundos.
- La primera invocación de la prueba HTTP local fue rechazada por el sandbox al enlazar un puerto. La misma prueba se ejecutó después con permiso de sandbox y observó el fallo de comportamiento esperado; no se confundió el rechazo del entorno con el resultado RED.

## Cambios y cobertura

- Reserva y consulta de la clave usan operaciones `REQUIRES_NEW`; la escritura emplea `saveAndFlush` antes de contactar al proveedor y antes de informar éxito.
- Solo la violación PostgreSQL `23505` de `email_envio_idempotency_key_uk` se trata como carrera de idempotencia. Otros errores de lectura o persistencia producen un resultado de fallo controlado.
- Una clave con estado `pending` devuelve fallo controlado y no vuelve a contactar al proveedor. Si el proveedor aceptó el correo pero falla la escritura final, queda `pending`; la entrega es incierta y la clave queda reservada para prevenir un duplicado.
- Las pruebas de contrato verifican el header `Idempotency-Key` estable en cada intento HTTP.
- Configuración agrega el binding `EMAIL_TIMEOUT` con default `10s`, ya documentado.
- IT con PostgreSQL Testcontainers y servidor HTTP local verifican carrera de dos solicitudes para una misma clave (una sola petición HTTP), persistencia independiente del rollback de la transacción llamadora y fallo de persistencia del resultado final con reserva `pending`.

## Evidencia GREEN

- `mvn -q -Dtest=AdaptadorResendTest,ConfiguracionEmailTest test`: correcto.
- `mvn -q failsafe:integration-test failsafe:verify -Dit.test=EmailEnvioPersistenciaIT`: **5 pruebas, 0 fallos, 0 errores** con PostgreSQL 18.7 real en Testcontainers.
- `mvn clean verify`: **BUILD SUCCESS**; Surefire **128**, Failsafe **186**, total **314 pruebas**, 0 fallos, 0 errores, 0 omitidas. Log completo: `/tmp/c17-clean-verify.log`.
- Los XML generados en `backend/target/surefire-reports` suman 128 y los de `backend/target/failsafe-reports` suman 186.
- La conexión fallida a `127.0.0.1:1` que aparece en el log pertenece a una prueba que confirma que el arranque falla cuando se configura deliberadamente una base inválida; la prueba y la suite terminaron en verde.
- `git diff --check`: sin errores. No se ejecutó `npm test`: no se modificaron frontend ni contratos frontend/backend.

## Prueba real autorizada: ejecución y confirmaciones posteriores

El 24-11-2026 a las 23:36:45 (America/Mendoza) se ejecutó una única prueba real autorizada mediante `CorreoRealManual`: Maven salió con código 0, una prueba ejecutada, sin fallos/errores/omisiones; el proveedor respondió 2xx y la prueba confirmó una fila `sent` con un intento en PostgreSQL temporal. Esto acredita la aceptación del API, no la entrega final.

El adaptador no conserva el `id` JSON del proveedor; no se registró un ID. Falta comprobar el evento de entrega en el panel de Resend y que el mensaje llegó al buzón destinatario. Hasta documentar esa constancia, C-17 y el issue #18 permanecen abiertos e incompletos. No repetir el envío ante un resultado incierto.

### Confirmación posterior del destinatario

El 24-11-2026 a las 23:40:19 (America/Mendoza), el usuario confirmó que el mensaje llegó a su buzón. Se registra como confirmación del destinatario; no se inspeccionó el panel de Resend ni se obtuvo ID/estado del proveedor. Por tanto, no se afirma que el evento del panel sea `delivered`; C-17 y el issue #18 siguen abiertos hasta que se documente esa constancia del proveedor.

### Verificación final del proveedor

En una revisión posterior del panel Emails de Resend, el estado del mensaje apareció como `Delivered`, con ID de proveedor `01a21090-09ec-7520-8d10-61817a35d31b`. Esto, junto con la confirmación del usuario de recepción en su buzón, completa localmente la evidencia de 6.1. Las notas previas de estado pendiente reflejan el momento en que se escribieron; esta verificación las actualiza. El adaptador sigue sin persistir el ID del proveedor. El issue #18 remoto no se modificó.
