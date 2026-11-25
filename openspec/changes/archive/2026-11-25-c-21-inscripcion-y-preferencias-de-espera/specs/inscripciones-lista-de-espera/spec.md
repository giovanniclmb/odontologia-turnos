# Spec Delta

## Purpose

Permite al paciente vinculado gestionar sus preferencias de lista de espera y mantiene una inscripción vigente por prestación como máximo.

## ADDED Requirements

### Requirement: Gestión de inscripciones propias
El sistema SHALL permitir crear, consultar, editar y dar de baja inscripciones propias mediante una cuenta autenticada y vinculada según C-16. El sistema SHALL determinar el propietario a partir del vínculo confirmado.

#### Scenario: Gestión por el titular
- **WHEN** una cuenta vinculada crea una inscripción válida, la consulta, edita sus preferencias y solicita su baja mientras está vigente
- **THEN** el sistema registra y devuelve los cambios para el mismo titular y la baja termina la vigencia

#### Scenario: Cuenta sin vínculo confirmado
- **WHEN** se solicita alguna operación sin una cuenta de paciente autenticada y vinculada
- **THEN** el sistema rechaza la solicitud sin revelar datos de inscripciones

#### Scenario: Inscripción ajena
- **WHEN** una cuenta vinculada intenta consultar, editar o dar de baja una inscripción de otro paciente
- **THEN** el sistema rechaza la solicitud sin revelar su existencia o sus datos y conserva la inscripción

### Requirement: Validación de preferencias
Al crear o editar una inscripción, el sistema SHALL exigir una prestación existente, un rango de fechas inclusivo y ordenado cuyo fin sea hoy o posterior, y al menos una franja horaria con inicio anterior al fin dentro del mismo día. El profesional SHALL ser opcional y, si se indica, SHALL existir en el catálogo.

#### Scenario: Profesional omitido
- **WHEN** el paciente envía una prestación existente, fechas válidas y franjas válidas sin indicar profesional
- **THEN** la inscripción conserva esas preferencias y queda sin preferencia de profesional

#### Scenario: Datos inválidos
- **WHEN** el paciente omite un dato obligatorio, indica una referencia inexistente, envía fechas invertidas o vencidas, o envía una franja vacía o invertida
- **THEN** el sistema rechaza la solicitud y conserva el estado previo

### Requirement: Vigencia y baja
El sistema SHALL considerar vigente una inscripción desde el alta hasta el final de la fecha de fin en la zona horaria de la sede o hasta su baja, lo que ocurra primero. El sistema SHALL conservar las inscripciones finalizadas para consulta propia. La edición SHALL aplicarse únicamente a inscripciones vigentes.

#### Scenario: Fecha final inclusiva
- **WHEN** se consulta una inscripción sin baja en su fecha final y luego al comenzar el día siguiente
- **THEN** la primera consulta la identifica como vigente y la segunda como vencida

#### Scenario: Baja explícita
- **WHEN** el titular da de baja una inscripción vigente
- **THEN** la inscripción deja de estar vigente inmediatamente y el titular puede consultar su baja

#### Scenario: Edición de una inscripción finalizada
- **WHEN** el titular intenta editar una inscripción vencida o dada de baja
- **THEN** el sistema rechaza la edición y conserva el registro

### Requirement: Límite por paciente y prestación
El sistema SHALL admitir como máximo una inscripción vigente por paciente y prestación, incluso ante operaciones simultáneas. El sistema SHALL aplicar el límite al alta y al cambio de prestación en una edición.

#### Scenario: Altas simultáneas para una misma prestación
- **WHEN** dos altas válidas del mismo paciente para la misma prestación concurren sin una inscripción vigente previa
- **THEN** una sola operación crea una inscripción y la otra recibe un conflicto

#### Scenario: Prestaciones distintas
- **WHEN** el paciente solicita un alta válida para otra prestación
- **THEN** el sistema acepta el alta aunque exista una inscripción vigente para una prestación diferente

#### Scenario: Cambio a una prestación ocupada
- **WHEN** el paciente cambia una inscripción vigente a una prestación para la cual ya tiene otra inscripción vigente
- **THEN** el sistema rechaza la edición y conserva ambas inscripciones

#### Scenario: Nueva alta tras la baja o el vencimiento
- **WHEN** el paciente se inscribe nuevamente tras la baja o el vencimiento de la inscripción anterior para la misma prestación
- **THEN** el sistema acepta el alta si las preferencias son válidas

### Requirement: Independencia de la agenda
La gestión de inscripciones SHALL NOT crear ni reservar turnos, asignar huecos o disparar avisos.

#### Scenario: Cambios sobre una inscripción
- **WHEN** el paciente crea, edita o da de baja una inscripción
- **THEN** el cambio afecta únicamente la lista de espera
