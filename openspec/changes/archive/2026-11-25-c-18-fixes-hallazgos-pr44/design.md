# Design

## Context

Ver [proposal.md](proposal.md). La agenda bloquea la fila del turno y después sus recursos (profesionales antes que sillones, en orden de identificador). La reprogramación elimina el token después de esos bloqueos. La cancelación por enlace bloqueaba primero el token y leía el reloj antes de cualquier espera.

## Goals / Non-Goals

**Goals:**

- Corregir los tres defectos sin cambiar el contrato HTTP ni el esquema.

**Non-Goals:**

- Entregar el token a C-19, enviar email o cambiar el contrato de la validación por `GET`.

## Decisions

### Confirmar actualiza la versión ligada al token

En la transacción de `confirmar`, después de cambiar el estado, se actualiza `version_turno` solo del token no consumido que correspondía a la versión anterior. La actualización requiere una transacción abierta por el llamador.

| Alternativa | Motivo para descartarla |
| --- | --- |
| Dejar de comparar la versión al cancelar | La invalidación dependería solo del borrado al reprogramar y cualquier otra edición futura conservaría el enlace |
| Invalidar y emitir un reemplazo | Descartada por el usuario: exige entregar el enlace nuevo, que corresponde a C-19 |

### Orden único: turno, recursos, token

La cancelación por enlace lee el token sin bloquearlo solo para conocer el turno. Después bloquea el turno, sus recursos y por último el token por su hash. Bajo esos bloqueos exige que el token exista, no esté consumido, apunte al mismo turno y coincida con la versión. Los recursos se toman de la fila ya bloqueada.

- Reprogramar usa turno, recursos y token; confirmar, turno y token; cancelar internamente, turno y recursos. Son subsecuencias del mismo orden.
- Altas y bloqueos toman recursos sin bloquear turnos existentes ni sus tokens.
- Se descartó que la reprogramación bloquee el token antes que el turno: obligaría a invertir también confirmación y cancelación interna.

### Hallazgos de la revisión independiente

- La validación por `GET` comparaba la versión leída antes de esperar la fila del turno y rechazaba un enlace durante una confirmación. Ahora vuelve a leer el token después de bloquearla.
- Con el nuevo orden, un enlace ya usado o de un turno cancelado llegaba a retener profesional y sillón antes del rechazo. Se rechaza antes, porque ninguno de esos estados se revierte; la revalidación completa sigue bajo todos los bloqueos.

### Reloj después del último bloqueo

El instante se consulta una sola vez, después de bloquear el token, y se usa para las dos reglas temporales y para `consumido_en`.

## Risks / Trade-offs

- [Un enlace filtrado sigue sirviendo después de confirmar] → consecuencia aceptada con la decisión; vence a las 48 horas del inicio y se usa una vez.
- [Una operación futura que bloquee el token antes que el turno reintroduce el ciclo] → el orden queda documentado en el servicio y cubierto por prueba con PostgreSQL real.
- [La validación por `GET` conserva el reloj previo a su espera y bloquea la fila del turno] → no modifica datos; queda registrado como pendiente.
- [Un enlace vencido y no usado retiene brevemente los recursos antes del rechazo] → el plazo se decide solo después de los bloqueos; queda registrado como pendiente.

## Migration Plan

Sin cambios de esquema. Revertir el commit restituye el comportamiento anterior.
