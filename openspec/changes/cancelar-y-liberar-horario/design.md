# Design

## Context

C-06 persiste turnos `PENDIENTE` y usa sus períodos para validar la disponibilidad. C-07 agregó versión opaca y ya serializa las ediciones sobre una fila de turno antes de adquirir los locks de los recursos. La consulta por sillón usa una proyección con estado y versión que abarca todos los turnos visibles. [Especificación vigente](../../specs/agenda-persistida/spec.md) · [Diseño de C-07](../archive/2026-11-23-reprogramar-turno/design.md)

## Goals / Non-Goals

La cancelación agrega una transición a la agenda persistida que reutiliza la identidad interna, la versión del turno y el protocolo de concurrencia de C-07 y C-15. El servicio Java entrega el turno actualizado como punto de integración. C-18 define el acceso por enlace y su plazo de 48 horas. C-22 define cómo detectar y avisar de los huecos. El transporte HTTP, la interfaz, los envíos de email y la infraestructura de eventos quedan fuera del alcance de C-08.

## Decisions

### Contrato interno

Agregar a `ServicioAgenda` una operación `cancelar` con la `Identidad` y una solicitud que contiene `turnoId` y `versionEsperada`. Reutilizar el permiso `TURNO_CANCELAR`, concedido a recepción y odontología. El resultado es un `TurnoRegistrado` con el mismo identificador, paciente, prestación, profesional, sillón, período y duración efectiva, estado `CANCELADO` y la nueva versión. El resultado identifica los recursos y el período que se liberan al confirmar. Si el llamador participa en una transacción mayor, debe esperar la confirmación de esa transacción para tratar el resultado como liberación efectiva.

Una transacción exterior debe usar `READ COMMITTED` y respetar el orden global fila → recursos durante toda la transacción. El llamador entra en `cancelar` sin locks de agenda adquiridos previamente. Por ejemplo, llamar a `crear` y luego a `cancelar` en la misma transacción invertiría ese orden.

### Orden de locks y validación

1. Exigir identidad interna con el permiso y validar la estructura. En odontología, denegar una identidad sin profesional vinculado antes de consultar. Usar la lectura actual de C-07 con alcance por profesional y asignación activa, sin bloquear las filas de asignación. Para recepción, una fila ausente devuelve el error de turno inexistente. Para odontología, una fila ausente o fuera de alcance devuelve la misma denegación genérica.
2. Adquirir primero el lock de la fila del turno mediante `FOR NO KEY UPDATE`. Revalidar el acceso después de esa espera y comprobar la versión vigente antes de intentar cambiar la ocupación. Una autorización rechazada no expone que la versión cambió.
3. En la misma transacción `READ COMMITTED`, adquirir todos los recursos de la fila mediante `LocksRecursosAgenda`: profesionales por UUID y luego sillones por UUID. Tras esperar, revalidar el acceso y la asignación activa de odontología. La fila de turno sigue bloqueada durante esas comprobaciones. La cancelación usa las referencias persistidas y no ejecuta la validación de alta de recursos activos, prestación ni intervalo.
4. Con versión vigente e identidad autorizada, informar el rechazo si el estado es `CANCELADO`. Para un turno `PENDIENTE`, comparar su inicio persistido con el `Clock` después de adquirir los recursos y admitir la cancelación solo si el inicio es estrictamente futuro.

El orden fila → recursos es compartido con reprogramación. El alta y la creación de bloqueos toman los recursos y consultan la ocupación sin bloquear filas de turno. [Contrato de bloqueos](../../specs/bloqueos-de-recursos/spec.md)

### Escritura y lectura del resultado

Conservar la fila por su identidad y para que la agenda muestre el estado cancelado. Ejecutar una actualización explícita de `estado` y `version`, condicionada por identificador, estado pendiente y versión vigente. Obtener los datos del resultado desde la propia actualización, siguiendo el patrón JDBC de C-07. La proyección de consulta debe leer el estado y la versión vigentes incluso cuando el contexto JPA ya cargó una entidad anterior.

La búsqueda de ocupantes para alta y reprogramación y la comprobación de ocupación para bloqueos filtran por `PENDIENTE`. La consulta por sillón incluye el registro `CANCELADO` según el período y el alcance vigentes. Al reprogramar, la elegibilidad de C-07 requiere `PENDIENTE` y por eso rechaza el turno cancelado, incluso si el destino coincide con el origen y la versión está vigente.

## Risks / Trade-offs

- Adquirir el sillón o profesional antes de la fila podría crear una espera circular con C-07. Usar su mismo orden y comprobar la contención entre los servicios reales.
- Una asignación revocada o el inicio alcanzado mientras se espera pueden invalidar una solicitud que era admisible al llegar. Volver a evaluar con el estado confirmado después de los locks correspondientes.
- Un alta o bloqueo puede usar el horario tan pronto como confirma la cancelación. El resultado identifica una liberación confirmada y cada nueva reserva sigue validando disponibilidad con el protocolo de agenda.
- Un rechazo, conflicto de versión o fallo transaccional debe conservar fila y ocupación confirmadas. Las pruebas de contención deben distinguir confirmación y reversión mediante observaciones desde transacciones independientes.

## Migration Plan

- Verificar al iniciar Apply y al integrar cuál es la próxima versión Flyway consecutiva libre. Considerar el trabajo en paralelo al fijar el número. Preservar las migraciones V7 y V8.
- Ampliar el `CHECK` de estado de `odontologia.turno` a `PENDIENTE` y `CANCELADO` y otorgar `UPDATE` sobre `estado` a `odonto_app`. El rol ya puede actualizar `version`. Conservar las filas, versiones, referencias, restricciones de período y demás privilegios vigentes.
- Probar la actualización de una base en el estado previo con turnos existentes y con el rol de aplicación. Fijar el arranque de `ReprogramacionMigracionIT` en V8 para conservar su prueba histórica de privilegios. Corregir una migración ya aplicada mediante una nueva versión que preserve los datos.

## UI Mockups

Decision: no (no UI)
