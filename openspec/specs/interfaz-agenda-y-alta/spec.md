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
