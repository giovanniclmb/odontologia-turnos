# interfaz-agenda-y-alta Specification

## Purpose
Permitir que recepción y odontología consulten la agenda por sillón, den de alta turnos y operen bloqueos e intervalo según su rol, desde la interfaz web interna y su contrato HTTP.

## Requirements

### Requirement: Operaciones de agenda protegidas
Las operaciones HTTP de agenda, bloqueos, sede y catálogos SHALL requerir una identidad interna autenticada y aplicar en el backend los permisos y el alcance vigentes, con independencia de lo que muestre la interfaz.

#### Scenario: Solicitud sin identidad
- **WHEN** una solicitud sin token válido invoca cualquiera de esas operaciones
- **THEN** se rechaza sin devolver datos ni modificar información

#### Scenario: Operación de recepción invocada por odontología
- **WHEN** un odontólogo invoca directamente el alta de un bloqueo o el cambio del intervalo
- **THEN** el backend responde acceso denegado y no persiste cambios

### Requirement: Errores de agenda distinguibles
Las respuestas de error SHALL incluir un código estable y un mensaje sin detalles internos, distinguiendo datos inválidos, acceso denegado, referencia inexistente, horario no disponible, bloqueo sobre un turno existente e intervalo sin configurar.

#### Scenario: Horario no disponible
- **WHEN** el alta choca por profesional, sillón, intervalo o bloqueo
- **THEN** la respuesta informa un conflicto genérico de disponibilidad sin revelar datos del turno o bloqueo que lo causa

#### Scenario: Intervalo sin configurar
- **WHEN** se intenta un alta y la sede no tiene intervalo
- **THEN** la respuesta lo identifica como configuración faltante y la interfaz indica que recepción debe configurarlo

### Requirement: Vista diaria y semanal por sillón
La interfaz SHALL mostrar los turnos de un sillón elegido en vista diaria o semanal de lunes a domingo, con horario, duración efectiva, paciente, profesional y estado, y los bloqueos visibles para el rol. No SHALL imponer una jornada laboral.

#### Scenario: Cambio entre día y semana
- **WHEN** el usuario cambia de vista, de fecha o de sillón
- **THEN** se muestran los turnos y bloqueos del período y sillón elegidos

#### Scenario: Turno cancelado
- **WHEN** el período contiene un turno cancelado visible
- **THEN** se muestra identificado como cancelado

#### Scenario: Período sin actividad
- **WHEN** no hay turnos ni bloqueos visibles
- **THEN** se muestra un estado vacío

#### Scenario: Carga y error
- **WHEN** la consulta está en curso o falla
- **THEN** se muestra el estado correspondiente y, ante un fallo, una acción para reintentar

#### Scenario: Recarga de la página
- **WHEN** el usuario recarga o comparte la dirección de una vista
- **THEN** se conservan la vista, la fecha y el sillón elegidos

### Requirement: Hora de la sede
La interfaz SHALL interpretar y mostrar fechas y horas en `America/Argentina/Mendoza`, con independencia de la zona del navegador.

#### Scenario: Navegador en otra zona horaria
- **WHEN** un usuario cuya computadora usa otra zona carga un turno a las 09:00
- **THEN** el turno se guarda y se muestra a las 09:00 de Mendoza

### Requirement: Alta de turno desde la interfaz
La interfaz SHALL permitir crear un turno eligiendo paciente, profesional, sillón, prestación e inicio, proponiendo la duración de referencia y permitiendo ajustarla. Odontología SHALL operar solo con su profesional y sus pacientes asignados.

#### Scenario: Alta válida
- **WHEN** el usuario confirma datos válidos sobre un horario disponible
- **THEN** el turno se crea como pendiente y aparece en la agenda

#### Scenario: Alta rechazada
- **WHEN** el backend rechaza el alta
- **THEN** se muestra el motivo y el formulario conserva lo ingresado

#### Scenario: Envío repetido
- **WHEN** un alta está en curso
- **THEN** la interfaz impide volver a enviarla

#### Scenario: Faltan datos de catálogo
- **WHEN** no hay profesionales, sillones o prestaciones disponibles para elegir
- **THEN** se informa qué falta y no se permite confirmar

### Requirement: Alta de bloqueo desde la interfaz
La interfaz SHALL ofrecer a recepción el alta de un bloqueo de un sillón, un profesional o ambos durante un período, sin ofrecer modificación ni desbloqueo.

#### Scenario: Bloqueo válido
- **WHEN** recepción confirma un período válido sin turnos afectados
- **THEN** el bloqueo aparece en la agenda

#### Scenario: Bloqueo sobre un turno existente
- **WHEN** el período afecta a un turno existente
- **THEN** se informa el rechazo y el formulario conserva lo ingresado

#### Scenario: Odontología
- **WHEN** un odontólogo usa la agenda
- **THEN** no ve la acción de crear bloqueos

### Requirement: Configuración del intervalo por recepción
La interfaz SHALL ofrecer a recepción una sección propia para consultar el intervalo vigente, o su estado sin configurar, y establecerlo como entero positivo de minutos, sin proponer un valor inicial.

#### Scenario: Sede sin configurar
- **WHEN** recepción abre la sección en una sede sin intervalo
- **THEN** ve el estado sin configurar y un campo vacío

#### Scenario: Cambio válido
- **WHEN** recepción guarda un entero positivo
- **THEN** ve el valor persistido y las altas posteriores lo aplican

#### Scenario: Valor inválido
- **WHEN** se envía un valor vacío, cero, negativo o fraccionario
- **THEN** se rechaza y se conserva la configuración previa

#### Scenario: Odontología
- **WHEN** un odontólogo usa la interfaz
- **THEN** no ve la sección Configuración

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

### Requirement: Consulta HTTP de pendientes para recepción

La agenda SHALL exponer `GET /agenda/turnos/pendientes` a recepción autenticada. SHALL devolver los pendientes futuros de toda la sede en el orden del servicio, con identificador, nombre y apellido del paciente, prestación, profesional, sillón, inicio, fin, duración, estado y versión opaca. No SHALL incluir otros datos personales ni clínicos.

#### Scenario: Pendientes de toda la sede
- **WHEN** recepción consulta con turnos pendientes futuros de distintos sillones y profesionales, y otros ya iniciados, cancelados o confirmados
- **THEN** recibe solo los pendientes futuros, ordenados por inicio e identificador, con los datos indicados

#### Scenario: Sin permiso o sesión
- **WHEN** odontología o una persona sin sesión válida consulta el listado directamente
- **THEN** el servidor deniega la solicitud sin devolver turnos

### Requirement: Registro HTTP de confirmación manual

La agenda SHALL exponer `POST /agenda/turnos/{turnoId}/confirmacion` a recepción autenticada, con `versionEsperada` como cadena opaca. SHALL delegar la elegibilidad y transición al servicio y devolver HTTP 200 con el turno actualizado sin datos del paciente.

#### Scenario: Confirmación válida
- **WHEN** recepción registra una confirmación de un turno elegible con su versión vigente
- **THEN** recibe el turno confirmado con una nueva versión y los mismos recursos y horario

#### Scenario: Acceso, entrada o referencia inválidos
- **WHEN** falta sesión, odontología solicita la operación, la versión es ausente o inválida, o recepción indica un turno inexistente
- **THEN** el servidor responde respectivamente 401, 403, 400 o 404 sin modificar el turno

#### Scenario: Versión anterior o turno no elegible
- **WHEN** recepción envía una versión anterior o intenta confirmar con versión vigente un turno ya confirmado, cancelado o iniciado
- **THEN** el servidor devuelve respectivamente 409 con código `version_turno_desactualizada` o 400 con código `solicitud_invalida`, sin modificar el turno

### Requirement: Seguimiento manual desde la agenda

La interfaz SHALL ofrecer a recepción un listado de pendientes de toda la sede y una acción de registrar confirmación por turno. SHALL mostrar identificación, horario en la zona de la sede, profesional, sillón y prestación; SHALL obtener la pertenencia desde el servidor y no ofrecer este flujo a odontología.

#### Scenario: Listado, carga y vacío
- **WHEN** recepción abre el seguimiento
- **THEN** ve la carga y, al completarse, los pendientes devueltos o un estado vacío; si falla, ve un error y una acción para reintentar la consulta

#### Scenario: Confirmación guardada
- **WHEN** recepción registra con éxito la confirmación de un turno
- **THEN** la interfaz informa el éxito y vuelve a consultar los pendientes; el turno confirmado deja de aparecer si la consulta posterior tiene éxito

#### Scenario: Usuario odontólogo
- **WHEN** odontología usa la agenda o intenta entrar directamente a la vista de seguimiento
- **THEN** no obtiene el listado ni la acción de confirmación

### Requirement: Recuperación del seguimiento de confirmaciones

La interfaz SHALL impedir envíos simultáneos para el mismo turno y distinguir un registro guardado, un rechazo y un resultado incierto. Ante una versión desactualizada o resultado incierto SHALL pedir consultar de nuevo antes de reintentar y no SHALL reenviar automáticamente.

#### Scenario: Conflicto concurrente
- **WHEN** otra operación cambia el turno antes del registro con la versión mostrada
- **THEN** se informa el conflicto y se solicita recargar antes de permitir otro intento

#### Scenario: Registro guardado y consulta fallida
- **WHEN** el servidor confirma el registro pero la actualización del listado falla
- **THEN** se informa que se guardó y se ofrece reintentar solo la consulta

#### Scenario: Respuesta de registro desconocida
- **WHEN** se pierde la respuesta de la operación y no se conoce si se guardó
- **THEN** no se afirma el éxito ni se reenvía la operación y se ofrece consultar nuevamente
