# email-de-turno Specification

## Purpose
Informar al paciente por email la fecha de un turno creado o reprogramado y entregarle, cuando corresponde, el enlace para cancelarlo, sin condicionar la operación de agenda.

## Requirements

### Requirement: Email al crear o reprogramar un turno
El sistema SHALL solicitar un email al paciente por cada alta y por cada reprogramación efectiva, solo después de confirmar la transacción y sin demorar ni alterar la respuesta de la agenda.

#### Scenario: Alta confirmada
- **WHEN** se confirma el alta de un turno
- **THEN** se solicita un único email de alta al paciente de ese turno

#### Scenario: Reprogramación confirmada
- **WHEN** se confirma una reprogramación que cambia el turno, incluso por segunda vez
- **THEN** se solicita un email propio de esa reprogramación con la nueva fecha

#### Scenario: Sin email
- **WHEN** la transacción revierte, el destino es idéntico, o el turno solo se confirma o se cancela
- **THEN** no se solicita ningún email

### Requirement: Destinatario y contenido
El sistema SHALL enviar el email en texto plano a la dirección registrada del paciente del turno, con su nombre y la fecha y hora de `America/Argentina/Mendoza`. SHALL NOT incluir prestación, profesional, sillón, identificadores internos ni información clínica.

#### Scenario: Aislamiento entre pacientes
- **WHEN** se crean turnos de pacientes distintos
- **THEN** cada email va solo a su paciente y contiene solo sus datos y su enlace

#### Scenario: Zona horaria
- **WHEN** el turno se registra con un instante en cualquier zona
- **THEN** el texto muestra la fecha y hora de Mendoza

### Requirement: Enlace de cancelación en el email
Cuando existe un enlace vigente, el sistema SHALL incluirlo formado por la URL base configurada, `#` y el token, junto con la fecha límite. Cuando faltan menos de 48 horas SHALL enviar el email sin enlace e indicar que ya no puede cancelarse por esa vía.

#### Scenario: Con enlace
- **WHEN** el turno comienza con más de 48 horas de anticipación
- **THEN** el email incluye un enlace que cancela ese turno y la fecha límite

#### Scenario: Menos de 48 horas
- **WHEN** el turno se crea o reprograma con menos de 48 horas de anticipación
- **THEN** el email se envía sin enlace y lo aclara

#### Scenario: URL base sin configurar
- **WHEN** corresponde un enlace y la URL base falta o no es una dirección web válida
- **THEN** el email no se envía, la operación se conserva y queda un aviso sin dirección ni token

### Requirement: Descarte de mensajes desactualizados
Inmediatamente antes de enviar, el sistema SHALL descartar el mensaje si el turno está cancelado, cambió su inicio o su enlace dejó de ser el vigente. La confirmación del turno SHALL NOT descartarlo.

#### Scenario: Turno cancelado o reprogramado antes del envío
- **WHEN** el turno se cancela o se reprograma antes de enviar un mensaje anterior
- **THEN** ese mensaje no se envía

#### Scenario: Turno confirmado antes del envío
- **WHEN** el turno se confirma antes de enviar su mensaje
- **THEN** el mensaje se envía

### Requirement: Fallos y duplicados
El sistema SHALL usar un tipo de envío distinto para el alta y para cada reprogramación, de modo que la deduplicación impida repetir un mismo mensaje sin bloquear los siguientes. Un fallo SHALL quedar registrado sin reenvío posterior ni cambios en el turno.

#### Scenario: Entrega repetida del mismo aviso
- **WHEN** el mismo aviso de alta o reprogramación se procesa dos veces
- **THEN** el proveedor se contacta como máximo para un envío exitoso

#### Scenario: Proveedor no disponible
- **WHEN** se agotan los reintentos
- **THEN** el envío queda como fallido y el turno se conserva

#### Scenario: Error inesperado al preparar el email
- **WHEN** falla la preparación o el envío
- **THEN** la operación de agenda conserva su resultado y el aviso en el registro no contiene dirección, nombre, token ni enlace
