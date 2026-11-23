# agenda-persistida Specification

## Purpose
Permitir al equipo crear y consultar turnos de una sede con datos persistidos, autorización por operación y por registro, y consistencia ante solicitudes concurrentes.

## Requirements

### Requirement: Alta autorizada por rol y registro

El servicio SHALL exigir identidad interna autenticada y permiso `turno:crear`. Recepción SHALL poder usar cualquier profesional elegible y paciente existente del sitio. Odontología SHALL poder reservar solo para su propio profesional vinculado y para pacientes con asignación activa a ese profesional.

#### Scenario: Recepción crea para cualquier profesional elegible
- **WHEN** recepción solicita una asignación válida para un profesional activo y un paciente del sitio sin asignación a ese profesional
- **THEN** el sistema acepta el alta

#### Scenario: Odontología crea para sí y para un paciente asignado
- **WHEN** odontología solicita una asignación válida para su propio profesional activo y un paciente con asignación activa
- **THEN** el sistema acepta el alta

#### Scenario: Odontología fuera de alcance
- **WHEN** odontología intenta reservar para otro profesional, carece de vinculación profesional o indica un paciente sin asignación activa a su profesional
- **THEN** el sistema deniega el alta sin guardar el turno ni devolver información de registros ajenos

#### Scenario: Se quita una asignación durante la espera por recursos
- **WHEN** la asignación del paciente al profesional se desactiva y la transacción de ese cambio se confirma mientras el alta de odontología espera por los recursos
- **THEN** el servicio revalida la asignación tras adquirir los recursos, deniega el alta y no guarda el turno

#### Scenario: Operador sin identidad o permiso
- **WHEN** se solicita el alta sin una identidad autenticada con `turno:crear`
- **THEN** el sistema deniega la operación sin devolver datos de la agenda ni guardar un turno

### Requirement: Referencias válidas para reservar

El alta SHALL exigir un paciente existente, una prestación existente con duración configurada, un profesional activo, un sillón activo y el intervalo global vigente configurado. El intervalo SHALL aplicarse según las reglas de `disponibilidad-de-turnos`.

#### Scenario: Referencia inválida
- **WHEN** para un operador autorizado falta una referencia necesaria, el paciente o la prestación no existen, o un recurso no existe o está inactivo
- **THEN** el servicio rechaza el alta y no crea una reserva

#### Scenario: Sede sin intervalo
- **WHEN** los datos son válidos y la sede aún no tiene intervalo configurado
- **THEN** el servicio identifica la falta de configuración y no crea una reserva

### Requirement: Inicio estrictamente futuro

El alta SHALL exigir que el inicio que se persistirá, truncado a microsegundos, sea posterior al instante de validación. La evaluación SHALL realizarse sin restricciones de jornada semanal adicionales.

#### Scenario: Inicio transcurrido o igual a la validación
- **WHEN** el inicio truncado a microsegundos es anterior o igual al instante de validación
- **THEN** el servicio rechaza el alta y no guarda el turno

#### Scenario: Inicio futuro fuera de una jornada habitual
- **WHEN** el inicio que se persistirá es futuro y la asignación es válida aunque se solicite fuera de una jornada semanal habitual
- **THEN** el servicio permite el alta

### Requirement: Duración efectiva persistida

El alta SHALL usar la duración vigente de la prestación cuando el operador omite el ajuste. Un ajuste explícito SHALL ser una duración positiva en minutos enteros. El sistema SHALL evaluar disponibilidad y persistir el mismo período, calculando el fin con la duración efectiva completa desde el inicio truncado. La duración efectiva SHALL conservarse aunque más tarde cambie el catálogo.

#### Scenario: Duración de referencia conservada
- **WHEN** se crea un turno válido sin ajuste y después se modifica la duración de referencia de la prestación
- **THEN** la reserva conserva el fin y la duración efectiva que tenía al crearse

#### Scenario: Duración ajustada
- **WHEN** el operador indica una duración válida diferente de la de referencia y la asignación resultante está disponible
- **THEN** el sistema guarda la duración elegida y el fin calculado con ella

#### Scenario: Ajuste inválido
- **WHEN** el operador indica un ajuste de duración que no es un entero positivo en minutos
- **THEN** el servicio rechaza el alta sin guardar el turno

#### Scenario: Inicio con fracciones de microsegundo
- **WHEN** el inicio solicitado contiene fracciones inferiores a un microsegundo y el alta es válida
- **THEN** la reserva comienza en el instante truncado y el fin es exactamente ese instante más la duración efectiva en minutos completos

### Requirement: Reserva pendiente con identidad estable

El sistema SHALL crear una reserva con identificador estable y estado inicial pendiente de confirmación. La reserva SHALL ocupar al profesional y al sillón desde la confirmación de la transacción de alta.

#### Scenario: Alta exitosa y consulta posterior
- **WHEN** el equipo guarda una asignación válida
- **THEN** la operación devuelve el identificador y los datos del turno pendiente, y una consulta autorizada posterior recupera el mismo turno

### Requirement: Exclusión de turnos incompatibles

El sistema SHALL rechazar una asignación cuando una reserva ocupante del mismo profesional o sillón se solape o deje menos del intervalo vigente en cualquiera de los dos sentidos. La separación igual al intervalo SHALL admitirse si se cumplen las demás condiciones. Dos altas simultáneas incompatibles SHALL producir como máximo una reserva persistida.

#### Scenario: Choque en un solo recurso
- **WHEN** un turno ocupante incumple el intervalo del profesional o del sillón solicitados, aunque el otro recurso esté libre
- **THEN** el alta devuelve un conflicto sin guardar el turno

#### Scenario: Sillón ocupado por un turno fuera del alcance de odontología
- **GIVEN** un sillón ocupado por un turno de otro profesional y otro paciente que no aparece en la consulta de agenda del odontólogo
- **WHEN** el odontólogo solicita una reserva en ese sillón para su propio profesional y un paciente asignado, en un período que se solapa con el turno existente
- **THEN** el alta devuelve un conflicto genérico de disponibilidad sin guardar un turno ni revelar datos del turno ajeno

#### Scenario: Separación exacta
- **WHEN** la separación respecto de una reserva en el mismo recurso, anterior o posterior, es exactamente el intervalo vigente y las demás reglas se cumplen
- **THEN** el sistema permite el alta

#### Scenario: Altas simultáneas incompatibles
- **WHEN** dos altas válidas por separado se superponen o incumplen el intervalo en el mismo profesional o sillón
- **THEN** se confirma la transacción de una de ellas, la otra devuelve un conflicto y queda una sola reserva nueva en estado pendiente

### Requirement: Exclusión entre alta y bloqueo de recurso

El alta SHALL respetar los bloqueos de profesional o sillón según `bloqueos-de-recursos`. Los períodos SHALL tratarse como semiabiertos, con precisión de microsegundos. El margen de la sede SHALL aplicarse entre turnos. Una escritura de bloqueo SHALL rechazar un solapamiento con un turno ocupante del mismo recurso.

#### Scenario: Bloqueo vigente de uno o ambos recursos
- **WHEN** un bloqueo de C-15 impide el uso de uno o ambos recursos en el período solicitado
- **THEN** el alta devuelve un conflicto y no guarda un turno

#### Scenario: Bloqueo contiguo sin solapamiento
- **WHEN** un turno termina exactamente al comenzar su bloqueo o comienza exactamente al terminarlo, en el mismo recurso
- **THEN** ambos períodos son compatibles sin requerir margen entre ellos

#### Scenario: Solapamiento de un microsegundo
- **WHEN** los períodos persistibles de un turno y un bloqueo se solapan en un microsegundo y comparten profesional o sillón
- **THEN** se rechaza la segunda operación incompatible y queda solo la primera

### Requirement: Exclusión transaccional entre turno y bloqueo

El alta de turno y el alta de bloqueo SHALL coordinar la validación y el guardado con el contrato transaccional compartido de C-15. Si dos operaciones incompatibles compiten por un recurso, el resultado SHALL ser consistente con el estado confirmado de la primera transacción.

#### Scenario: Bloqueo confirmado primero
- **WHEN** la transacción del bloqueo se confirma antes de que el alta concurrente incompatible complete su validación
- **THEN** el alta detecta el bloqueo persistido y devuelve un conflicto sin crear un turno

#### Scenario: Turno persistido primero
- **WHEN** la transacción del turno se confirma antes de que el bloqueo concurrente incompatible complete su validación
- **THEN** el bloqueo detecta el turno pendiente de confirmación y devuelve un conflicto sin persistir un bloqueo

#### Scenario: La primera transacción revierte
- **WHEN** la primera operación adquiere el recurso y revierte mientras la otra espera
- **THEN** la operación que esperaba vuelve a validar con el estado confirmado y puede guardarse si cumple las demás reglas

### Requirement: Consulta de agenda por sillón y período

El servicio SHALL exigir `agenda-sillon:consultar` y devolver los turnos que se intersectan con el período semiabierto consultado. Recepción SHALL recibir los turnos del sitio. Odontología SHALL recibir solo los turnos de su profesional vinculado. La consulta SHALL devolver los turnos por inicio con su duración efectiva y excluir los datos clínicos.

#### Scenario: Consulta de recepción
- **WHEN** recepción consulta un sillón entre dos instantes válidos y hay turnos de distintos profesionales que se intersectan con el período
- **THEN** obtiene los turnos del sillón en orden por inicio, incluidos los que empezaron antes del período y siguen en curso al abrirlo

#### Scenario: Consulta de odontología
- **WHEN** odontología consulta un sillón que contiene turnos propios y ajenos
- **THEN** el servicio solo devuelve los turnos de su profesional y ningún dato de los turnos ajenos

#### Scenario: Turnos contiguos al período consultado
- **WHEN** un turno termina al inicio del período consultado o comienza exactamente al final de ese período
- **THEN** el servicio no incluye ese turno en la consulta

#### Scenario: Consulta con período inválido o sin autorización
- **WHEN** el fin truncado a microsegundos no es posterior al inicio truncado o falta una identidad que pueda consultar la agenda
- **THEN** el servicio rechaza la consulta sin devolver turnos
