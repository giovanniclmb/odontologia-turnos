# Spec Delta

## ADDED Requirements

### Requirement: Ediciones internas de turnos por HTTP

La agenda SHALL exponer `POST /agenda/turnos/{turnoId}/reprogramacion` y `POST /agenda/turnos/{turnoId}/cancelacion`. Ambas SHALL exigir identidad interna, versión opaca y los permisos y el alcance de la operación. Una respuesta exitosa SHALL devolver el turno actualizado mediante el contrato de alta y excluir datos del paciente.

#### Scenario: Solicitudes autorizadas
- **WHEN** el operador autorizado solicita reprogramar con versión vigente, inicio, profesional y sillón válidos, o cancelar con versión vigente
- **THEN** recibe HTTP 200 con la asignación, duración, estado y versión resultantes de la operación

#### Scenario: Solicitud inválida o sin identidad
- **WHEN** la identidad interna falta o la solicitud autenticada tiene un identificador inválido, omite la versión o envía un destino inválido
- **THEN** la respuesta indica falta de autenticación o solicitud inválida, según corresponda, sin modificar el turno

#### Scenario: Odontología fuera de alcance
- **WHEN** odontología intenta actuar sobre un turno ajeno, inexistente o de un paciente sin asignación activa, o reprograma con un profesional distinto
- **THEN** la respuesta es una denegación genérica que preserva el turno y no revela si existe fuera de su alcance

### Requirement: Reprogramación desde la agenda

La interfaz SHALL ofrecer la reprogramación en las vistas diaria y semanal de turnos futuros en estado `PENDIENTE`, y de `CONFIRMADO` para recepción. El formulario SHALL mostrar el paciente, la prestación y la duración efectiva como datos de solo lectura, y permitir cambiar fecha, hora y sillón. Solo recepción SHALL poder elegir otro profesional. El backend SHALL validar la elegibilidad definitiva.

#### Scenario: Movimiento válido
- **WHEN** el operador confirma un destino disponible para un turno elegible
- **THEN** la vista informa el éxito, recarga la agenda y refleja la ubicación actual del turno sin alterar paciente, prestación ni duración efectiva

#### Scenario: Recepción mueve un turno confirmado
- **WHEN** recepción cambia efectivamente la asignación de un turno confirmado con versión vigente
- **THEN** la agenda refleja el turno como pendiente y usa la nueva versión

#### Scenario: Reprogramación sin cambios
- **WHEN** recepción envía el destino idéntico de un confirmado elegible con versión vigente
- **THEN** el turno conserva su estado confirmado y su versión, y la interfaz evita anunciar un movimiento

#### Scenario: Opacidad de la agenda de odontología
- **WHEN** odontología abre un turno pendiente propio visible
- **THEN** el profesional se muestra fijo y el backend decide si el paciente sigue asignado al profesional al realizar la operación

### Requirement: Cancelación interna desde la agenda

La interfaz SHALL ofrecer la cancelación para los mismos estados y roles que la reprogramación, pedir confirmación explícita y recargar la agenda tras el éxito. El turno cancelado SHALL seguir visible, identificado por estado y con sus datos vigentes. El período SHALL poder utilizarse para otra reserva que cumpla las reglas del backend.

#### Scenario: Cancelación desistida
- **WHEN** el operador cierra la confirmación sin confirmar
- **THEN** se vuelve a la agenda sin enviar una cancelación

#### Scenario: Cancelación y reutilización del horario
- **WHEN** el operador confirma una cancelación autorizada y crea después otra reserva válida para el mismo período y recursos
- **THEN** la agenda muestra el registro cancelado y la nueva reserva según el alcance de consulta

#### Scenario: Turno confirmado, cancelado o iniciado
- **WHEN** se muestran turnos confirmados, cancelados o que alcanzaron su inicio
- **THEN** las acciones de edición y cancelación se ofrecen para confirmados futuros de recepción y se omiten para cancelados o iniciados

### Requirement: Resultado de una edición de agenda

La interfaz SHALL informar disponibilidad insuficiente, desactualización de versión y otros rechazos sin revelar detalles internos ni reservas ajenas. SHALL evitar envíos simultáneos desde el mismo turno y distinguir una operación confirmada de un fallo al recargar la agenda.

#### Scenario: Destino incompatible
- **WHEN** la reprogramación es rechazada por disponibilidad
- **THEN** el turno original conserva su asignación y el formulario mantiene lo ingresado para corregir el destino

#### Scenario: Versión desactualizada
- **WHEN** el servidor rechaza una cancelación o reprogramación porque el turno cambió desde la consulta
- **THEN** la respuesta distingue el conflicto de versión, la interfaz propone volver a consultar y no reenvía automáticamente

#### Scenario: No se pudo actualizar la agenda
- **WHEN** una edición se confirma y la consulta siguiente falla
- **THEN** la interfaz indica que la edición se guardó y ofrece reintentar únicamente la consulta

#### Scenario: Envío aún en curso
- **WHEN** el operador intenta disparar otra edición del mismo turno mientras espera el resultado
- **THEN** la interfaz evita un segundo envío
