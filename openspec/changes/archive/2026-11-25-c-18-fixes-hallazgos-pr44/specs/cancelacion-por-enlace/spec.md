# Spec Delta

## MODIFIED Requirements

### Requirement: Emisión de un token de cancelación por turno elegible

El sistema SHALL generar un token de cancelación opaco y aleatorio cuando un turno `PENDIENTE` o `CONFIRMADO` tenga un inicio estrictamente posterior al instante de emisión más 48 horas. SHALL persistir únicamente el hash del token, asociado al identificador del turno y a la versión vigente al momento de la emisión, que la confirmación del turno actualiza. SHALL no emitir token cuando queden menos de 48 horas para el inicio del turno.

#### Scenario: Emisión válida
- **WHEN** se crea o reprograma un turno cuyo inicio es posterior al instante de emisión más 48 horas
- **THEN** el sistema genera un token opaco, guarda solo su hash asociado al turno y su versión vigente, y pone el token a disposición de C-19 para el envío del enlace

#### Scenario: Ventana temporal cerrada
- **WHEN** el turno se crea o reprograma con menos de 48 horas restantes para su inicio
- **THEN** el sistema no emite token y C-19 omite el enlace de cancelación del email

## ADDED Requirements

### Requirement: Vigencia del enlace ante la confirmación

La confirmación de un turno SHALL conservar la validez del enlace vigente y no consumido, sin cambiar su plazo ni emitir otro. El sistema SHALL asociarlo a la nueva versión en la misma transacción que confirma. SHALL NOT recuperar un enlace consumido, invalidado o asociado a una versión anterior.

#### Scenario: Cancelación después de confirmar
- **WHEN** se confirma un turno con un enlace vigente y el paciente lo usa dentro del plazo
- **THEN** el sistema cancela el turno, consume el enlace y libera el horario

#### Scenario: Validación durante la confirmación
- **WHEN** se valida el enlace mientras una confirmación del turno está en curso y esta se confirma
- **THEN** el sistema informa que el enlace sigue disponible

#### Scenario: Confirmación revertida
- **WHEN** la transacción que confirma el turno revierte
- **THEN** el enlace sigue asociado a la versión anterior y vigente

#### Scenario: Enlace que ya no era vigente
- **WHEN** se confirma un turno cuyo enlace correspondía a una versión anterior
- **THEN** el enlace sigue rechazado

#### Scenario: Reprogramación y repetición después de confirmar
- **WHEN** un turno confirmado se reprograma, o su enlace ya usado se presenta de nuevo
- **THEN** el sistema rechaza ese enlace sin modificar el turno

### Requirement: Evaluación del plazo al momento de cancelar

El sistema SHALL evaluar el comienzo del turno y el plazo de 48 horas con el instante consultado después de obtener la exclusividad sobre el turno, sus recursos y el enlace, inmediatamente antes de modificar datos.

#### Scenario: El plazo vence durante la espera
- **WHEN** una solicitud comienza dentro del plazo, espera a otra operación y obtiene la exclusividad con el plazo vencido
- **THEN** el sistema la rechaza sin consumir el enlace ni cambiar estado, versión u ocupación

#### Scenario: El borde del plazo se alcanza durante la espera
- **WHEN** la exclusividad se obtiene exactamente 48 horas antes del inicio
- **THEN** el sistema cancela el turno

### Requirement: Coordinación de la cancelación por enlace con la agenda

La cancelación por enlace SHALL coordinarse con altas, bloqueos, reprogramaciones, confirmaciones y cancelaciones internas sin esperas circulares, y SHALL volver a validar el enlace con el estado confirmado después de esperar.

#### Scenario: Reprogramación confirmada primero
- **WHEN** una reprogramación del turno se confirma mientras la cancelación por enlace espera
- **THEN** la cancelación se rechaza y el turno conserva el resultado de la reprogramación

#### Scenario: Cancelación por enlace confirmada primero
- **WHEN** la cancelación por enlace se confirma mientras una reprogramación del mismo turno espera
- **THEN** la reprogramación se rechaza por versión desactualizada y el turno queda cancelado

#### Scenario: Altas y bloqueos en espera
- **WHEN** un alta o un bloqueo sobre los mismos recursos espera a una cancelación por enlace
- **THEN** ambas operaciones terminan y la que esperaba evalúa el horario ya liberado
