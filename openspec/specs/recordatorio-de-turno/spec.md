# recordatorio-de-turno Specification

## Purpose
Recordar al paciente por email un turno próximo, sin confirmar ni modificar el turno y sin ofrecer una cancelación cuyo plazo ya venció.

## Requirements

### Requirement: Activación del envío automático
El sistema SHALL enviar recordatorios solo cuando el envío automático esté activado por configuración, y SHALL tenerlo desactivado por defecto.

#### Scenario: Configuración por defecto
- **WHEN** la aplicación arranca sin activar el recordatorio y hay turnos en las próximas 24 horas
- **THEN** no se envía ningún recordatorio

#### Scenario: Activado
- **WHEN** la aplicación arranca con el recordatorio activado
- **THEN** revisa los turnos al arrancar y vuelve a hacerlo cada minuto después de terminar cada revisión

### Requirement: Momento del recordatorio
El sistema SHALL enviar el recordatorio de cada turno pendiente o confirmado al que le falten 24 horas o menos para comenzar y que todavía no haya comenzado, salvo las exclusiones por poca anticipación y por intento ya realizado.

#### Scenario: Faltan más de 24 horas
- **WHEN** se revisa un turno que comienza dentro de más de 24 horas
- **THEN** no se envía su recordatorio

#### Scenario: Entra en la ventana
- **WHEN** a un turno pendiente le faltan exactamente 24 horas
- **THEN** se envía su recordatorio

#### Scenario: Turno confirmado
- **WHEN** un turno confirmado entra en la ventana
- **THEN** se envía su recordatorio

#### Scenario: Recuperación tras un apagado
- **WHEN** la aplicación vuelve a funcionar y a un turno sin recordatorio le quedan menos de 24 horas
- **THEN** se envía en la siguiente revisión

#### Scenario: Turno cancelado o comenzado
- **WHEN** se revisa un turno cancelado o cuyo inicio ya llegó
- **THEN** no se envía su recordatorio

### Requirement: Turnos fijados con poca anticipación
El sistema SHALL NOT enviar recordatorio de un horario fijado, por alta o por reprogramación que cambie el inicio, con menos de 24 horas de anticipación. Los turnos anteriores a este registro SHALL tratarse como fijados con anticipación suficiente.

#### Scenario: Alta cercana
- **WHEN** se da de alta un turno que comienza en menos de 24 horas
- **THEN** no se envía recordatorio

#### Scenario: Reprogramación cercana
- **WHEN** un turno se reprograma a un inicio que está a menos de 24 horas
- **THEN** no se envía recordatorio del nuevo horario

#### Scenario: Anticipación exacta
- **WHEN** un horario se fija exactamente 24 horas antes del inicio
- **THEN** se envía su recordatorio

#### Scenario: Cambio sin mover el inicio
- **WHEN** a menos de 24 horas se cambia solo el profesional o el sillón de un turno cuyo recordatorio estaba pendiente
- **THEN** el recordatorio sigue correspondiendo

#### Scenario: Nuevo horario con anticipación
- **WHEN** un turno ya recordado se reprograma a otro inicio con al menos 24 horas de anticipación
- **THEN** el nuevo horario recibe su propio recordatorio al entrar en la ventana

#### Scenario: Turno anterior al registro
- **WHEN** un turno creado antes de este change entra en la ventana
- **THEN** se envía su recordatorio

### Requirement: Un solo intento por horario
El sistema SHALL solicitar como máximo un recordatorio por turno y horario. Un envío que agota sus reintentos SHALL quedar registrado y SHALL NOT volver a intentarse.

#### Scenario: Revisiones repetidas
- **WHEN** varias revisiones encuentran el mismo horario ya procesado
- **THEN** el paciente recibe un solo recordatorio

#### Scenario: Envío fallido
- **WHEN** el envío de un recordatorio falla
- **THEN** queda registrado, el turno no cambia y no se reintenta en revisiones posteriores

#### Scenario: Email del paciente modificado
- **WHEN** cambia el email del paciente después de procesar el recordatorio de un horario
- **THEN** no se envía otro para ese horario

#### Scenario: Un fallo no detiene a los demás
- **WHEN** falla el recordatorio de un turno
- **THEN** se procesan los demás turnos y las revisiones siguientes

### Requirement: Destinatario y contenido
El sistema SHALL enviar el recordatorio en texto plano a la dirección registrada del paciente del turno, con su nombre, la fecha y hora de `America/Argentina/Mendoza` y la aclaración de que ya venció el plazo para cancelar por enlace. SHALL NOT incluir enlace, token, prestación, profesional, sillón, identificadores internos ni información clínica, y SHALL NOT presentarlo como una confirmación.

#### Scenario: Sin opción de cancelar
- **WHEN** se envía un recordatorio
- **THEN** el texto no contiene enlaces y aclara que ya no puede cancelarse por esa vía

#### Scenario: Aislamiento entre pacientes
- **WHEN** entran en la ventana turnos de pacientes distintos
- **THEN** cada recordatorio va solo a su paciente y contiene solo sus datos

### Requirement: Descarte de recordatorios desactualizados
Inmediatamente antes de enviar, el sistema SHALL omitir el recordatorio si el turno fue cancelado, cambió su inicio o ya comenzó.

#### Scenario: Cambio antes del envío
- **WHEN** un turno se cancela o se reprograma después de la búsqueda y antes de su envío
- **THEN** no se envía el recordatorio del horario anterior

### Requirement: Independencia de la agenda y de la confirmación
El recordatorio SHALL NOT modificar el estado ni la versión del turno, registrar una confirmación ni alterar el enlace de cancelación.

#### Scenario: Turno pendiente recordado
- **WHEN** se envía el recordatorio de un turno pendiente
- **THEN** el turno sigue pendiente en el listado de recepción

### Requirement: Registro sin datos personales
Los avisos de fallo SHALL identificar el turno y el tipo de error, sin dirección, nombre ni contenido del mensaje.

#### Scenario: Fallo registrado
- **WHEN** falla la preparación o el envío de un recordatorio
- **THEN** el registro del servidor no contiene datos del paciente ni el texto enviado
