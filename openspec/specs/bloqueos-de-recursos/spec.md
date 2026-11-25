# bloqueos-de-recursos Specification

## Purpose

Permitir que recepción registre la indisponibilidad temporal de sillones y profesionales sin afectar turnos ocupantes, y que la agenda consulte esos bloqueos antes de asignar recursos.

## Requirements

### Requirement: Alta de bloqueo por recepción

El backend SHALL exigir el permiso `bloqueo:crear` y aceptar un período válido con un sillón existente, un profesional existente o ambos. Un bloqueo que afecta ambos recursos SHALL confirmarse en una sola transacción.

#### Scenario: Bloquear cada combinación de recursos

- **WHEN** recepción solicita un período válido para un sillón, un profesional o ambos y no hay turnos ocupantes afectados
- **THEN** se persiste un único bloqueo con el período y todos los recursos solicitados

#### Scenario: Solicitud sin acceso o inválida

- **WHEN** el solicitante carece del permiso, falta un extremo, el fin es igual o anterior al inicio, no se indica ningún recurso o una referencia no existe
- **THEN** se rechaza el alta y no se persiste un bloqueo

### Requirement: Solapamiento de períodos

La detección de indisponibilidad SHALL usar inicio incluido y fin excluido. El intervalo global entre turnos SHALL conservar su aplicación entre turnos.

#### Scenario: Turno contiguo

- **WHEN** un turno del recurso termina al comenzar el bloqueo o comienza al terminar el bloqueo
- **THEN** el bloqueo y ese turno son compatibles por período

#### Scenario: Turno ocupante afectado

- **WHEN** un turno ocupante comparte sillón o profesional y se solapa con el período solicitado
- **THEN** se rechaza todo el bloqueo, incluso si el otro recurso está libre

### Requirement: Consulta interna de indisponibilidad

El backend SHALL exponer una consulta por período y recursos para el alta y la reprogramación de turnos.

#### Scenario: Coincidencia de un recurso

- **WHEN** el período consultado se solapa con un bloqueo de cualquiera de los recursos solicitados
- **THEN** la consulta devuelve el bloqueo como conflicto

#### Scenario: Período disponible respecto de bloqueos

- **WHEN** solo existen bloqueos contiguos o de otros recursos
- **THEN** la consulta no devuelve conflictos

### Requirement: Exclusión transaccional entre bloqueo y turno

El contrato de escritura SHALL serializar en PostgreSQL la comprobación y el guardado de bloqueos y asignaciones de turno que comparten recurso. Las operaciones de agenda SHALL usar este contrato dentro de su transacción de escritura.

#### Scenario: El bloqueo confirma primero

- **WHEN** el bloqueo confirma antes de que el alta o la reprogramación incompatible pueda completar su validación
- **THEN** la operación de turno detecta el bloqueo y se rechaza

#### Scenario: El turno confirma primero

- **WHEN** la asignación confirma antes de que el bloqueo incompatible pueda completar su validación
- **THEN** el bloqueo detecta el turno ocupante y se rechaza sin efectos parciales

#### Scenario: La primera operación revierte

- **WHEN** la primera transacción revierte y libera los recursos
- **THEN** la operación que esperaba vuelve a evaluar la disponibilidad con los datos confirmados

### Requirement: Consulta autorizada de bloqueos
El backend SHALL exigir `bloqueo:consultar` y devolver los bloqueos que intersectan el período semiabierto pedido para la vista de un sillón, ordenados por inicio. Recepción SHALL recibir todos los del sitio con sus recursos.

#### Scenario: Recepción consulta el período
- **WHEN** recepción consulta un período con bloqueos de distintos sillones y profesionales
- **THEN** recibe todos los que lo intersectan, con sus recursos

#### Scenario: Consulta sin identidad o con período inválido
- **WHEN** falta la identidad, falta un extremo o el fin no es posterior al inicio
- **THEN** se rechaza la consulta sin devolver bloqueos

### Requirement: Alcance de bloqueos para odontología
Odontología SHALL recibir solo los bloqueos que afectan al sillón consultado o a su profesional vinculado. Cada resultado SHALL indicar el período y cuáles de esos dos recursos afecta, sin identificar a otros profesionales ni a otros sillones.

#### Scenario: Bloqueo del sillón con otro profesional
- **WHEN** un bloqueo afecta al sillón consultado y a otro profesional
- **THEN** odontología recibe el período y la afectación del sillón, sin la referencia del otro profesional

#### Scenario: Bloqueo del propio profesional en otro sillón
- **WHEN** un bloqueo afecta a su profesional y a un sillón distinto del consultado
- **THEN** odontología recibe el período y la afectación de su profesional, sin la referencia de ese sillón

#### Scenario: Bloqueo ajeno
- **WHEN** un bloqueo no afecta al sillón consultado ni a su profesional
- **THEN** odontología no lo recibe

#### Scenario: Odontología sin profesional vinculado
- **WHEN** la identidad de odontología no tiene profesional vinculado
- **THEN** recibe solo la afectación del sillón consultado
