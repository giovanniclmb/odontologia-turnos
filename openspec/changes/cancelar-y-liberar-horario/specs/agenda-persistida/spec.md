# Spec Delta

## ADDED Requirements

### Requirement: Alcance de cancelación interna por rol y registro

El servicio SHALL exigir una identidad interna y el permiso `turno:cancelar`. Recepción SHALL poder cancelar cualquier turno elegible de la sede. Odontología SHALL poder cancelar solo turnos de su profesional vinculado y de pacientes con asignación activa. El servicio SHALL revalidar el alcance tras esperar por el turno y sus recursos.

#### Scenario: Recepción cancela un turno de la sede
- **WHEN** recepción solicita cancelar un turno elegible de cualquier profesional con su versión vigente
- **THEN** el servicio permite la cancelación

#### Scenario: Odontología cancela un turno propio de un paciente asignado
- **WHEN** odontología solicita cancelar un turno elegible de su profesional vinculado y de un paciente con asignación activa a él con la versión vigente
- **THEN** el servicio permite la cancelación

#### Scenario: Solicitud fuera de alcance
- **WHEN** falta identidad o permiso, o un odontólogo solicita cancelar un turno ajeno, inexistente, sin asignación activa o carece de profesional vinculado
- **THEN** el servicio devuelve una denegación genérica de acceso y no revela datos ni modifica el turno

#### Scenario: Asignación revocada mientras la operación espera
- **WHEN** recepción confirma que quitó la asignación mientras la cancelación de odontología espera por el turno o por sus recursos
- **THEN** el servicio deniega la cancelación sin revelar datos ni cambiar la versión, el estado o la ocupación

### Requirement: Versión y elegibilidad de la cancelación

El servicio SHALL exigir un identificador y la versión opaca esperada. Con acceso autorizado, SHALL rechazar una versión desactualizada e indicar que se vuelva a consultar. Ante versión vigente, SHALL aceptar únicamente un turno `PENDIENTE` cuyo inicio sea estrictamente posterior al instante de validación después de adquirir los recursos. Un turno `CANCELADO` con versión vigente SHALL producir un rechazo que indique que ya está cancelado.

#### Scenario: Referencia o versión faltante
- **WHEN** un operador con permiso omite el identificador o presenta una versión vacía o ausente
- **THEN** el servicio rechaza la entrada sin efectuar una cancelación

#### Scenario: Turno inexistente para recepción
- **WHEN** recepción solicita cancelar un identificador inexistente con una versión informada
- **THEN** el servicio indica que el turno no existe

#### Scenario: Versión desactualizada
- **WHEN** el operador autorizado solicita cancelar con una versión anterior a la vigente, incluso si otra operación ya canceló el turno
- **THEN** el servicio devuelve el conflicto de versión, solicita volver a consultar y conserva los datos confirmados

#### Scenario: Repetición con versión vigente
- **WHEN** el operador autorizado solicita cancelar un turno ya cancelado con su versión vigente
- **THEN** el servicio indica que el turno ya está cancelado y conserva su versión

#### Scenario: Turno no futuro al validar
- **WHEN** el inicio del turno pendiente es anterior o igual al instante de validación, incluso si era futuro al iniciar la solicitud y alcanzó su inicio durante la espera
- **THEN** el servicio rechaza la cancelación y conserva su ocupación, estado y versión

### Requirement: Liberación transaccional del horario al cancelar

La cancelación SHALL guardar `CANCELADO` y una versión nueva en la misma transacción, conservando los demás datos del turno. Al confirmar, el turno SHALL dejar de ocupar al profesional y al sillón para altas, reprogramaciones y bloqueos. El resultado actualizado SHALL identificar el período y los recursos liberados a partir de la confirmación de la transacción. Un rechazo o reversión SHALL conservar el último estado, versión y ocupación confirmados.

#### Scenario: Cancelación confirmada
- **WHEN** un operador autorizado cancela un turno elegible con su versión vigente y la transacción confirma
- **THEN** obtiene el turno cancelado con su nueva versión y los mismos identificador, paciente, prestación, profesional, sillón, inicio, fin y duración efectiva

#### Scenario: Reutilización del horario liberado
- **WHEN** la cancelación confirma y recepción solicita un alta para el período liberado
- **THEN** el turno cancelado no genera conflicto en el profesional ni en el sillón y el alta solo se acepta si respeta el intervalo vigente frente a los demás turnos ocupantes y las otras reglas de disponibilidad, incluidos los bloqueos

#### Scenario: Alta o bloqueo en competencia con la cancelación
- **WHEN** una cancelación y un alta o bloqueo incompatibles con la reserva pendiente compiten por un recurso
- **THEN** la disponibilidad se decide sobre el estado confirmado al validar, por lo que la operación que espera la cancelación confirmada puede usar el recurso si cumple sus demás condiciones, y la que valida con el turno todavía pendiente encuentra el conflicto

#### Scenario: Cancelación en competencia con la reprogramación
- **WHEN** un operador cancela y otro reprograma el mismo turno usando la misma versión
- **THEN** el primer cambio efectivo que confirme actualiza la versión y la segunda operación rechaza la versión anterior si el operador conserva el acceso

#### Scenario: Cancelaciones concurrentes con la misma versión
- **WHEN** dos operadores autorizados intentan cancelar el mismo turno con su versión inicial vigente
- **THEN** solo una cancelación confirma y cambia la versión una vez, y el otro operador recibe un conflicto de versión

#### Scenario: Reversión con una operación en espera
- **WHEN** la transacción de cancelación revierte mientras un alta, un bloqueo o una reprogramación espera por el turno o sus recursos
- **THEN** la otra operación vuelve a evaluar los datos confirmados y no observa el horario como liberado por la cancelación revertida

## MODIFIED Requirements

### Requirement: Consulta de agenda por sillón y período

El servicio SHALL exigir `agenda-sillon:consultar` y devolver los turnos del sillón solicitado que se intersectan con el período semiabierto consultado. Recepción SHALL recibir los turnos del sitio. Odontología SHALL recibir solo los del profesional vinculado. La consulta SHALL ordenar por inicio, incluir estado, duración efectiva y versión opaca vigente y excluir los datos clínicos. El resultado SHALL incluir los turnos cancelados dentro del alcance de la consulta.

#### Scenario: Consulta de recepción
- **WHEN** recepción consulta un sillón entre dos instantes válidos y hay turnos de distintos profesionales que se intersectan con el período
- **THEN** obtiene los turnos del sillón en orden por inicio y sus versiones vigentes, incluidos los que empezaron antes del período y siguen en curso al abrirlo

#### Scenario: Consulta de odontología
- **WHEN** odontología consulta un sillón que contiene turnos propios y ajenos
- **THEN** el servicio solo devuelve los turnos y las versiones de su profesional y ningún dato de los turnos ajenos

#### Scenario: Turnos contiguos al período consultado
- **WHEN** un turno termina al inicio del período consultado o comienza exactamente al final de ese período
- **THEN** el servicio no incluye ese turno en la consulta

#### Scenario: Consulta con período inválido o sin autorización
- **WHEN** el fin truncado a microsegundos no es posterior al inicio truncado o falta una identidad que pueda consultar la agenda
- **THEN** el servicio rechaza la consulta sin devolver turnos

#### Scenario: Consulta después de reprogramar
- **WHEN** el equipo consulta un turno visible después de que se confirmó su reprogramación
- **THEN** la consulta devuelve la asignación y versión vigentes para una futura edición

#### Scenario: Consulta después de cancelar y volver a reservar el horario
- **WHEN** un operador autorizado consulta el sillón y período de un turno cancelado después de una nueva reserva en el mismo horario
- **THEN** obtiene el registro cancelado con su estado y versión vigentes y también la nueva reserva si ambas están dentro de su alcance de consulta
