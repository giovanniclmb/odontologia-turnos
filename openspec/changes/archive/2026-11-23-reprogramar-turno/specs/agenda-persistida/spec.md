# Spec Delta

## ADDED Requirements

### Requirement: Alcance de reprogramación por rol y registro

El servicio SHALL exigir identidad interna y permiso `turno:reprogramar`. Recepción SHALL poder reprogramar cualquier turno elegible de la sede, incluido el cambio de profesional. Odontología SHALL poder cambiar el horario y el sillón de sus propios turnos de pacientes con asignación activa, conservando el profesional original.

#### Scenario: Recepción cambia el profesional
- **WHEN** recepción solicita un destino válido con otro profesional para un turno elegible de la sede
- **THEN** el turno se reprograma con el profesional de destino

#### Scenario: Odontología cambia el horario o el sillón
- **WHEN** odontología solicita un destino válido para su propio turno de un paciente asignado y conserva el profesional
- **THEN** el servicio permite el cambio de horario o sillón

#### Scenario: Solicitud fuera de alcance
- **WHEN** falta identidad o permiso, o un odontólogo intenta mover un turno ajeno, sin asignación activa, sin profesional vinculado o con un profesional de destino diferente
- **THEN** el servicio deniega la operación y no devuelve datos del turno ni modifica la reserva

#### Scenario: Asignación revocada durante la espera
- **WHEN** recepción confirma que quitó la asignación del paciente mientras la reprogramación de odontología espera por los recursos
- **THEN** el servicio vuelve a comprobar la asignación tras adquirir los recursos y deniega el movimiento sin modificar el turno

### Requirement: Elegibilidad temporal para reprogramar

El servicio SHALL aceptar solo turnos en estado pendiente de confirmación cuyo inicio original y el inicio de destino, truncado a microsegundos, sean estrictamente posteriores al instante de validación. La evaluación SHALL realizarse después de adquirir la exclusión de los recursos.

#### Scenario: Origen o destino no futuro
- **WHEN** el comienzo original o el inicio normalizado de destino es anterior o igual al instante de validación
- **THEN** el servicio rechaza la reprogramación y conserva la asignación vigente

#### Scenario: El turno comienza durante la espera
- **WHEN** el turno es futuro al comenzar la solicitud y alcanza su inicio mientras la reprogramación espera los recursos
- **THEN** el servicio rechaza el movimiento después de adquirirlos y conserva la asignación y versión vigentes

### Requirement: Reprogramación con identidad estable y duración conservada

El servicio SHALL recibir el identificador del turno, su versión esperada y el inicio, profesional y sillón de destino. El cambio efectivo SHALL conservar identificador, paciente, prestación, duración efectiva y estado. El fin SHALL calcularse sumando la duración efectiva persistida al inicio normalizado de destino.

#### Scenario: Movimiento exitoso del mismo turno
- **WHEN** se reprograma un turno elegible hacia un destino válido con versión vigente
- **THEN** el servicio devuelve el turno con la nueva asignación y versión, y conserva su identidad, paciente, prestación, duración y estado

#### Scenario: El catálogo cambió desde el alta
- **WHEN** la duración de referencia de la prestación cambió después del alta
- **THEN** la reprogramación valida disponibilidad y calcula el nuevo fin con la duración efectiva guardada en el turno

### Requirement: Disponibilidad del destino

El servicio SHALL exigir recursos de destino activos, aplicar el intervalo vigente frente a turnos ocupantes de ambos recursos y comprobar bloqueos según `bloqueos-de-recursos`. La comprobación SHALL excluir únicamente el turno que se reprograma y aplicar el intervalo entre turnos. Un conflicto SHALL carecer de datos de reservas ajenas.

#### Scenario: La nueva asignación se intersecta con la propia
- **WHEN** el destino se intersecta con la asignación actual del mismo turno y cumple las demás condiciones
- **THEN** el turno propio no impide el movimiento

#### Scenario: Otro turno ocupa el recurso
- **WHEN** otro turno ocupante comparte profesional o sillón con el destino y el intervalo es insuficiente antes o después
- **THEN** el destino se rechaza por disponibilidad y el turno conserva su asignación actual

#### Scenario: Turno ajeno fuera de la vista de odontología
- **WHEN** un turno de otro profesional que no se muestra al odontólogo ocupa el sillón de destino de su propio turno elegible
- **THEN** la reprogramación devuelve un conflicto genérico sin revelar el turno ajeno y mantiene la asignación vigente

#### Scenario: Márgenes exactos y bloqueos contiguos
- **WHEN** los demás turnos quedan separados exactamente por el intervalo vigente y los bloqueos solo son contiguos al período de destino
- **THEN** el servicio admite la asignación si cumple las demás condiciones

#### Scenario: Solapamiento de un microsegundo con bloqueo
- **WHEN** el destino persistible se solapa en un microsegundo con un bloqueo de profesional o sillón
- **THEN** el servicio devuelve conflicto de disponibilidad y conserva la asignación original

### Requirement: Versión esperada y operación sin cambios

El operador SHALL presentar la versión opaca que recibió del turno. El servicio SHALL rechazar una versión desactualizada con un conflicto que indique volver a consultar el turno. Cada cambio efectivo SHALL producir una versión nueva. Un destino normalizado idéntico SHALL devolver el turno vigente después de comprobar acceso, versión y elegibilidad temporal.

#### Scenario: Dos operadores editan la misma versión
- **WHEN** dos operadores intentan movimientos distintos del mismo turno con la misma versión y la primera transacción confirma el cambio
- **THEN** el segundo recibe un conflicto por versión desactualizada, y el movimiento confirmado y su versión se conservan

#### Scenario: Asignación que se movió y volvió al horario anterior
- **WHEN** dos cambios confirmados mueven un turno y luego lo devuelven a la asignación original
- **THEN** un intento con la versión leída antes de ambos cambios se rechaza por desactualizado aunque coincida el horario

#### Scenario: Destino idéntico con versión vigente
- **WHEN** un operador autorizado envía un destino que, después de normalizar el inicio, coincide con el turno original todavía futuro y usa la versión vigente
- **THEN** se devuelve el turno vigente y su versión, sin modificar la reserva ni incrementar la versión

#### Scenario: Destino idéntico con precondición incumplida
- **WHEN** el destino es idéntico pero falta acceso, la versión está desactualizada o el turno ya comenzó
- **THEN** el servicio devuelve el rechazo correspondiente y no trata la solicitud como un movimiento exitoso

### Requirement: Fallos conservan la asignación vigente

El servicio SHALL diferenciar entradas o referencias inválidas, conflictos de disponibilidad, versión desactualizada y denegación de acceso. Un rechazo o fallo de persistencia SHALL conservar el turno, la versión y la ocupación confirmados previamente, sin liberar el horario original ni revelar datos ajenos.

#### Scenario: Destino ocupado o datos inválidos
- **WHEN** el destino no está disponible o se presentan datos o referencias inválidas
- **THEN** el operador recibe el resultado correspondiente y el turno original sigue ocupando su mismo horario

#### Scenario: Una escritura iniciada revierte
- **WHEN** la transacción falla después de validar o intentar guardar el cambio
- **THEN** los datos y la versión del turno siguen iguales a los de la última transacción confirmada

### Requirement: Exclusión atómica al mover una reserva

La reprogramación SHALL coordinar en una transacción la ocupación de origen y destino mediante el protocolo de `bloqueos-de-recursos`. Las altas, bloqueos y reprogramaciones incompatibles SHALL observar el estado confirmado después de esperar. El horario de origen SHALL quedar libre solo al confirmarse un movimiento válido.

#### Scenario: Alta o bloqueo de destino confirma antes del movimiento
- **WHEN** un alta de turno o de bloqueo incompatible asegura primero el destino y confirma
- **THEN** la reprogramación devuelve un conflicto de disponibilidad y conserva el origen

#### Scenario: El movimiento confirma antes del alta o bloqueo incompatible
- **WHEN** la reprogramación confirma primero y otro servicio intenta un alta o bloqueo incompatible sobre ese destino
- **THEN** el segundo servicio detecta la reserva trasladada y rechaza la operación incompatible

#### Scenario: Reversión durante la contención
- **WHEN** la primera operación revierte mientras otra espera por los recursos en una carrera entre reprogramación, alta o bloqueo
- **THEN** la operación que esperaba vuelve a validar contra las filas confirmadas, manteniendo los datos originales de la primera operación si esta era la reprogramación

#### Scenario: Movimientos cruzados sobre recursos compartidos
- **WHEN** dos turnos distintos intercambian recursos de origen y destino sin incompatibilidad temporal
- **THEN** el resultado de ambos movimientos equivale a ejecutarlos en un orden serial, con cada turno y su duración conservados

## MODIFIED Requirements

### Requirement: Reserva pendiente con identidad estable

El sistema SHALL crear una reserva con identificador estable, estado inicial pendiente de confirmación y versión inicial opaca. La reserva SHALL ocupar al profesional y al sillón desde la confirmación de la transacción de alta.

#### Scenario: Alta exitosa y consulta posterior
- **WHEN** el equipo guarda una asignación válida
- **THEN** la operación devuelve el identificador, la versión y los datos del turno pendiente, y una consulta autorizada posterior recupera el mismo turno con su versión vigente

### Requirement: Consulta de agenda por sillón y período

El servicio SHALL exigir `agenda-sillon:consultar` y devolver los turnos del sillón solicitado que se intersectan con el período semiabierto consultado. Recepción SHALL recibir los turnos del sitio. Odontología SHALL recibir solo los del profesional vinculado. La consulta SHALL ordenar por inicio, incluir la duración efectiva y versión opaca vigente y excluir los datos clínicos.

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
