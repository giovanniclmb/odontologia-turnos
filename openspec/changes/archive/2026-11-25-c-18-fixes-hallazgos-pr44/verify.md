# Verify — correcciones del PR #44 (C-18)

Fecha: 25/11/2026. Base revisada: `381b0bce1b56ec8a8e81069084224e8c5b7ff734`, rama `codex/c-18-cancelacion-del-paciente-por-enlace`.

## Decisión confirmada

El usuario eligió por menú conservar la validez del enlace ante la confirmación (DD-17 de la KB 09). Las alternativas presentadas fueron invalidar y emitir un reemplazo con entrega en C-19, y reemplazar fijando ahora el contrato de entrega.

## Fallo observado antes de corregir

`CancelacionPorEnlaceRegresionIT` con producción sin cambios y PostgreSQL real: 17 casos, 10 fallos y 1 error.

| Casos que fallaron | Motivo observado |
| --- | --- |
| Cancelación y validación tras confirmar (2) | El enlace se rechazaba |
| Reprogramación primero y enlace después (1) | SQLSTATE `40P01`, `deadlock detected`, confirmado en una segunda ejecución |
| Plazo vencido esperando turno, profesional, sillón o token (4) | La cancelación vencida se aceptaba |
| Borde inclusivo tras la espera (4) | La cancelación se aceptaba, pero `consumido_en` registraba el instante anterior a la espera |

Los otros 6 casos pasaban ya antes del cambio y son guardas de regresión, no evidencia de rojo: confirmación revertida, enlace desactualizado, reprogramar un turno confirmado, enlace primero, cancelación interna primero y alta con bloqueo en espera.

Hallazgos de la revisión independiente, con las primeras correcciones aplicadas: 3 casos nuevos fallaron antes de corregir. La validación durante una confirmación devolvía `false`, y un enlace usado o de un turno cancelado quedaba esperando el profesional (2).

## Verificación final

| Comprobación | Resultado |
| --- | --- |
| `mvn -f backend/pom.xml clean verify` | 129 unitarias y 255 de integración, sin fallos, errores ni omisiones; PostgreSQL 18.7 con Testcontainers |
| `CancelacionPorEnlaceRegresionIT`, 5 ejecuciones adicionales | 20 de 20 en cada una |
| `npm test` en `frontend/` (sin cambios de interfaz) | 65 sin fallos |
| `python3 -m unittest discover -s scripts/tests` | 9 sin fallos |
| `openspec validate c-18-fixes-hallazgos-pr44 --strict` | Válido |

No se ejecutaron envíos de email.

## Revisión independiente

Un agente sin el contexto de la implementación revisó en modo de solo lectura, sin ejecutar Maven. Primera pasada: sin bloqueantes; se corrigieron S1 (validación durante una confirmación), S2 para enlaces usados o de turnos cancelados, y N1. Segunda pasada sobre el árbol con esas correcciones: sin bloqueantes y sin carreras, ciclos de bloqueos ni filtraciones nuevas. Su análisis de concurrencia es estático.

## Pendientes

Introducidos o dejados por estas correcciones:

- Un `GET` con un enlace ya usado bloquea la fila del turno antes de rechazar.
- La prueba del enlace usado no aísla el filtro de token consumido, porque el turno también está cancelado.
- Un enlace vencido y no usado retiene brevemente profesional y sillón antes del rechazo.
- La validación por `GET` evalúa el plazo con el instante anterior a su espera; no modifica datos.

Preexistentes en el PR de C-18:

- `ServicioAgenda` descarta el token en claro al crear y reprogramar; solo queda el hash, por lo que C-19 no puede obtenerlo. Incumple el escenario «Emisión válida» y la tarea archivada 2.3.
- `ArranqueIT.rechazaHistorialDeUnaVersionFutura` usa una V13 de fixture que ahora coincide con `V13__tokens_cancelacion.sql`: el registro muestra un checksum distinto para la versión 13, de modo que la prueba ya no demuestra el rechazo de una versión futura.
- El token viaja en la ruta HTTP y puede quedar en registros o historial; no hay límite de frecuencia.
- En `CancelacionPorEnlaceServiceIT`, la reemisión tras reprogramar se hace a mano y la concurrencia no fuerza solapamiento ni exige el tipo de rechazo.
- `CHANGES.md` mantiene C-18 como pendiente, la KB no registra las decisiones originales del change y `backend/README.md` no describe la cancelación por enlace.
- `invalidar` no tiene llamadores en producción, `bloquearPorTurno` solo se usa en pruebas, un comentario de `ServicioAgenda.cancelarPorToken` describe una delegación inexistente y V13 no impone un token por turno.
