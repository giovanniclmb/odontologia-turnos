# Spec Delta

## ADDED Requirements

### Requirement: Listado de turnos por confirmar

El servicio SHALL exigir identidad interna de recepción y el permiso `pendientes:consultar`. SHALL devolver los turnos de toda la sede con estado `PENDIENTE` e inicio estrictamente posterior al instante de consulta, ordenados por inicio e identificador, con los datos de agenda y la versión vigente.

#### Scenario: Selección global con límite estricto
- **WHEN** recepción consulta y existen turnos en distintos sillones y profesionales, con estados e inicios variados
- **THEN** solo recibe los pendientes cuyo inicio es posterior al instante de consulta
- **AND** los empates de inicio se ordenan por identificador

#### Scenario: Consulta sin autorización
- **WHEN** consulta una identidad que carece del permiso o del rol de recepción
- **THEN** se deniega el acceso sin devolver datos de turnos

### Requirement: Registro manual de la confirmación

El servicio SHALL exigir identidad interna de recepción, permiso `confirmacion:registrar`, identificador de turno y versión esperada. Con versión vigente SHALL confirmar únicamente un turno `PENDIENTE` cuyo inicio siga siendo estrictamente futuro al validar después de adquirir el bloqueo de fila.

#### Scenario: Confirmación exitosa
- **WHEN** recepción confirma un turno elegible con su versión vigente
- **THEN** el servicio guarda `CONFIRMADO`, incrementa la versión una vez y devuelve el turno actualizado
- **AND** conserva los demás datos y la ocupación de ambos recursos, y la siguiente consulta de pendientes omite ese turno

#### Scenario: Repetición o versión desactualizada
- **WHEN** recepción confirma con una versión que dejó de estar vigente
- **THEN** el servicio solicita volver a consultar y conserva los datos vigentes
- **AND** una repetición con la versión vigente de un turno confirmado indica que ya está confirmado y conserva la versión

#### Scenario: Solicitud inválida o turno inelegible
- **WHEN** recepción omite el identificador o la versión, indica un turno inexistente o intenta confirmar un turno cancelado o cuyo inicio ya se alcanzó
- **THEN** el servicio distingue el motivo y la operación no modifica datos
- **AND** el límite de inicio también se aplica si se alcanza mientras la operación espera el bloqueo

#### Scenario: Confirmación sin autorización
- **WHEN** alguien sin identidad interna de recepción o sin el permiso intenta confirmar un turno
- **THEN** se deniega el acceso antes de consultar el turno y sin revelar si existe

#### Scenario: Ediciones simultáneas del mismo turno
- **WHEN** compiten dos confirmaciones o una confirmación y una cancelación o reprogramación del mismo turno con la misma versión
- **THEN** a lo sumo una edición se confirma y cualquier operación que encuentre otra versión debe pedir que se vuelva a consultar
- **AND** si la primera transacción revierte, la siguiente valida contra el último estado confirmado

### Requirement: Ocupación de turnos confirmados

Los estados `PENDIENTE` y `CONFIRMADO` SHALL ocupar al profesional y al sillón. Las altas y reprogramaciones SHALL aplicar el intervalo vigente frente a ambos estados y los bloqueos SHALL rechazar los solapamientos de ambos estados.

#### Scenario: Conflicto con turno confirmado
- **WHEN** se intenta un alta, movimiento o bloqueo incompatible con un turno confirmado
- **THEN** la operación se rechaza con las mismas reglas aplicables a una reserva pendiente

## MODIFIED Requirements

### Requirement: Elegibilidad temporal para reprogramar

El servicio SHALL aceptar turnos en estado `PENDIENTE` o, si el operador es de recepción, `CONFIRMADO` cuyo inicio original y el inicio de destino, truncado a microsegundos, sean estrictamente posteriores al instante de validación. La evaluación SHALL realizarse después de adquirir la exclusión de los recursos.

#### Scenario: Origen o destino no futuro
- **WHEN** el comienzo original o el inicio normalizado de destino es anterior o igual al instante de validación
- **THEN** el servicio rechaza la reprogramación y conserva la asignación vigente

#### Scenario: El turno comienza durante la espera
- **WHEN** el turno es futuro al comenzar la solicitud y alcanza su inicio mientras la reprogramación espera los recursos
- **THEN** el servicio rechaza el movimiento después de adquirirlos y conserva la asignación y versión vigentes

### Requirement: Reprogramación con identidad estable y duración conservada

El servicio SHALL recibir el identificador del turno, su versión esperada y el inicio, profesional y sillón de destino. El cambio efectivo SHALL conservar identificador, paciente, prestación y duración efectiva. Un movimiento efectivo SHALL dejar el turno en `PENDIENTE`. El fin SHALL calcularse sumando la duración efectiva persistida al inicio normalizado de destino.

#### Scenario: Movimiento exitoso del mismo turno
- **WHEN** se reprograma un turno elegible hacia un destino válido con versión vigente
- **THEN** el servicio devuelve el turno con la nueva asignación y versión, y conserva su identidad, paciente, prestación y duración, con estado `PENDIENTE`

#### Scenario: El catálogo cambió desde el alta
- **WHEN** la duración de referencia de la prestación cambió después del alta
- **THEN** la reprogramación valida disponibilidad y calcula el nuevo fin con la duración efectiva guardada en el turno

#### Scenario: Solicitud sin movimiento de un turno confirmado
- **WHEN** recepción solicita reprogramar un turno confirmado elegible con versión vigente y destino normalizado idéntico
- **THEN** el servicio devuelve el turno con estado `CONFIRMADO` y su versión vigente

### Requirement: Versión y elegibilidad de la cancelación

El servicio SHALL exigir un identificador y la versión opaca esperada. Con acceso autorizado, SHALL rechazar una versión desactualizada e indicar que se vuelva a consultar. Ante versión vigente, SHALL aceptar un turno `PENDIENTE` o, si el operador es de recepción, `CONFIRMADO` cuyo inicio sea estrictamente posterior al instante de validación después de adquirir los recursos. Un turno `CANCELADO` con versión vigente SHALL producir un rechazo que indique que ya está cancelado.

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
- **WHEN** el inicio del turno es anterior o igual al instante de validación, incluso si era futuro al iniciar la solicitud y alcanzó su inicio durante la espera
- **THEN** el servicio rechaza la cancelación y conserva su ocupación, estado y versión

#### Scenario: Cancelación de un turno confirmado
- **WHEN** recepción cancela un turno confirmado futuro con versión vigente
- **THEN** el turno pasa a `CANCELADO` y libera ambos recursos al confirmar la transacción
