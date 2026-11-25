# Spec Delta

## ADDED Requirements

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
