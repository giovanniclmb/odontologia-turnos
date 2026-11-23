# Spec Delta

## Purpose

Permitir al equipo crear y consultar turnos de una sede con datos persistidos, autorización por operación y por registro, y consistencia ante solicitudes concurrentes.

## ADDED Requirements

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

El alta SHALL exigir que el inicio sea posterior al instante de validación. La evaluación SHALL realizarse sin restricciones de jornada semanal adicionales.

#### Scenario: Inicio transcurrido o igual a la validación
- **WHEN** el inicio es anterior o igual al instante de validación
- **THEN** el servicio rechaza el alta y no guarda el turno

### Requirement: Duración efectiva persistida

El alta SHALL usar la duración vigente de la prestación cuando el operador omite el ajuste. Si el operador suministra un ajuste, este SHALL ser una duración positiva en minutos enteros. El sistema SHALL evaluar disponibilidad con la duración efectiva y conservarla en la reserva aunque más tarde cambie el catálogo.

#### Scenario: Duración de referencia conservada
- **WHEN** se crea un turno válido sin ajuste y después se modifica la duración de referencia de la prestación
- **THEN** la reserva conserva el fin y la duración efectiva que tenía al crearse

#### Scenario: Duración ajustada
- **WHEN** el operador indica una duración válida diferente de la de referencia y la asignación resultante está disponible
- **THEN** el sistema guarda la duración elegida y el fin calculado con ella

#### Scenario: Ajuste inválido
- **WHEN** el operador indica un ajuste de duración que no es un entero positivo en minutos
- **THEN** el servicio rechaza el alta sin guardar el turno

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

#### Scenario: Separación exacta
- **WHEN** la separación respecto de una reserva en el mismo recurso, anterior o posterior, es exactamente el intervalo vigente y las demás reglas se cumplen
- **THEN** el sistema permite el alta

#### Scenario: Altas simultáneas incompatibles
- **WHEN** dos altas válidas por separado se superponen o incumplen el intervalo en el mismo profesional o sillón
- **THEN** se confirma la transacción de una de ellas, la otra devuelve un conflicto y queda una sola reserva nueva en estado pendiente

### Requirement: Exclusión entre alta y bloqueo de recurso

El alta SHALL respetar la indisponibilidad del profesional o del sillón según el contrato de C-15. Una carrera entre un alta y un bloqueo incompatibles SHALL producir como máximo una escritura confirmada. La regla de bordes se definirá junto con C-15 antes de Apply.

#### Scenario: Bloqueo vigente de uno o ambos recursos
- **WHEN** un bloqueo de C-15 impide el uso de uno o ambos recursos en el período solicitado
- **THEN** el alta devuelve un conflicto y no guarda un turno

#### Scenario: Alta y bloqueo incompatibles en simultáneo
- **WHEN** un alta y un bloqueo válidos por separado compiten por el mismo recurso en períodos incompatibles
- **THEN** se confirma una transacción, la otra operación devuelve un conflicto y el estado persistido respeta ambos contratos sin importar qué operación obtuvo primero el recurso

### Requirement: Consulta de agenda por sillón y período

El servicio SHALL exigir `agenda-sillon:consultar` para consultar los turnos del sillón que se intersectan con el período indicado. Recepción SHALL recibir los turnos del sitio. Odontología SHALL recibir solo los turnos de su profesional vinculado. La consulta SHALL devolver los turnos por inicio con su duración efectiva y excluir los datos clínicos.

#### Scenario: Consulta de recepción
- **WHEN** recepción consulta un sillón entre dos instantes válidos y hay turnos de distintos profesionales que se intersectan con el período
- **THEN** obtiene los turnos del sillón en orden por inicio, incluidos los que empezaron antes del período y siguen en curso al abrirlo

#### Scenario: Consulta de odontología
- **WHEN** odontología consulta un sillón que contiene turnos propios y ajenos
- **THEN** el servicio solo devuelve los turnos de su profesional y ningún dato de los turnos ajenos

#### Scenario: Consulta con período inválido o sin autorización
- **WHEN** el período no está ordenado o falta una identidad que pueda consultar la agenda
- **THEN** el servicio rechaza la consulta sin devolver turnos
