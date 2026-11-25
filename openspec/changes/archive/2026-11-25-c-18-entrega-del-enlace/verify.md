# Verify — entrega interna del enlace y versión futura (PR #44)

Fecha: 25/11/2026. Base: `945eb72`, rama `codex/c-18-cancelacion-del-paciente-por-enlace`.

## Decisión confirmada

El usuario eligió por menú un evento interno entregado después del commit (DD-18 de la KB 09), frente a que C-19 emita el token al enviar y a dejar la entrega solo registrada.

## Fallo observado antes de corregir

Con el evento definido pero sin publicar y PostgreSQL real, ejecución focalizada de 8 casos: 4 fallos y 1 error.

| Caso | Motivo observado |
| --- | --- |
| Entrega del alta tras el commit | Ningún evento recibido |
| Token nuevo al reprogramar | Ningún evento recibido (error al leer el primero) |
| Fallo del consumidor | Ningún evento recibido |
| Texto del evento | Contenía el token |
| `ArranqueIT.rechazaHistorialDeUnaVersionFutura` | Rechazo por `checksum mismatch` de la versión 13, no por versión desconocida |

Reversión, ventana cerrada y confirmación pasaban también sin publicación: son guardas, no evidencia de rojo. La comprobación de que el token no aparece en la salida se agregó tras la revisión y es una guarda.

## Verificación final

| Comprobación | Resultado |
| --- | --- |
| `mvn -f backend/pom.xml clean verify` | 129 unitarias y 262 de integración, sin fallos, errores ni omisiones; PostgreSQL 18.7 con Testcontainers |
| `openspec validate c-18-entrega-del-enlace --strict` | Válido |
| `openspec validate --specs` | 12 de 12; persisten dos avisos de longitud en `acceso-y-permisos-internos` |
| `python3 -m unittest discover -s scripts/tests` | 9 sin fallos |

No se ejecutaron envíos de email. La interfaz no cambió.

## Revisión independiente

El mismo agente revisor, sin el contexto de la implementación, revisó en solo lectura y sin ejecutar Maven: sin bloqueantes ni fugas del token por registros del framework, representación en texto, persistencia o HTTP. Después se agregaron sus tres advertencias para C-19 al diseño y al record, y la comprobación de la salida en la prueba de fallo.

## Pendientes

- C-19: capturar sus errores sin registrar el token, consumir de forma asíncrona con transacción propia y comprobar la validez antes de enviar. La necesidad de la segunda conexión es una inferencia no reproducida.
- La entrega no se persiste: si el proceso cae antes del consumo, el enlace se pierde.
- No hay una prueba específica de que ninguna respuesta HTTP contenga el token.
- `ArranqueIT` supone nombres `V<número>__` y versiones sin huecos; hoy se cumple.
- Siguen los pendientes menores registrados en el change `c-18-fixes-hallazgos-pr44`, salvo la pérdida del token y la prueba de versión futura, resueltos aquí.
