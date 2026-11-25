# Proposal

## Why

El PR #44 emite el token de cancelación al crear y reprogramar, pero descarta el valor en claro y solo conserva su hash. C-19 no puede obtenerlo, contra el escenario «Emisión válida», que exige dejarlo a su disposición. Además, `ArranqueIT.rechazaHistorialDeUnaVersionFutura` simula como futura una versión 13 que ya existe.

## What Changes

- La agenda publica un evento interno con el turno y el token en claro, para entregarlo después de confirmar la transacción. Es la alternativa que el usuario eligió el 25/11/2026 frente a que C-19 emita al enviar y a dejarlo solo registrado.
- La prueba de versión futura calcula la versión siguiente a las migraciones existentes y comprueba el motivo del rechazo.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `cancelacion-por-enlace`: entrega interna del token emitido.

## Impact

- Backend: `ServicioCancelacionPorEnlace`, un record de evento, pruebas de integración y `ArranqueIT`.
- Sin dependencias, migraciones, cambios HTTP ni envío de email.
- C-19 debe consumir el evento y decidir contenido, destinatario y tratamiento de fallos (PA-18).
