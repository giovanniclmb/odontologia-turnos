# cancelacion-por-enlace Specification

## Purpose

Autorizar la cancelación de un turno por parte del paciente mediante un enlace de un solo uso, con límite temporal de 48 horas y sin exponer la autenticación interna ni datos clínicos.

## Requirements

### Requirement: Emisión de un token de cancelación por turno elegible

El sistema SHALL generar un token de cancelación opaco y aleatorio cuando un turno `PENDIENTE` o `CONFIRMADO` tenga un inicio estrictamente posterior al instante de emisión más 48 horas. SHALL persistir únicamente el hash del token, asociado al identificador del turno y a la versión vigente al momento de la emisión, que la confirmación del turno actualiza. SHALL no emitir token cuando queden menos de 48 horas para el inicio del turno.

#### Scenario: Emisión válida
- **WHEN** se crea o reprograma un turno cuyo inicio es posterior al instante de emisión más 48 horas
- **THEN** el sistema genera un token opaco, guarda solo su hash asociado al turno y su versión vigente, y pone el token a disposición de C-19 para el envío del enlace

#### Scenario: Ventana temporal cerrada
- **WHEN** el turno se crea o reprograma con menos de 48 horas restantes para su inicio
- **THEN** el sistema no emite token y C-19 omite el enlace de cancelación del email

### Requirement: Invalidación del token al reprogramar

El sistema SHALL invalidar cualquier token de cancelación previamente emitido cuando el turno se reprograma, y SHALL emitir un nuevo token solo si el turno reprogramado cumple la ventana de 48 horas.

#### Scenario: Reprogramación revoca el enlace anterior
- **WHEN** un turno con token emitido se reprograma
- **THEN** el token anterior queda inválido y una cancelación posterior con él es rechazada, y C-19 envía un enlace nuevo si corresponde

### Requirement: Validación y cancelación por token

El sistema SHALL permitir cancelar un turno mediante un token válido solo si el token no ha sido consumido, no ha expirado, el turno está en estado `PENDIENTE` o `CONFIRMADO`, el turno no ha comenzado y quedan al menos 48 horas hasta su inicio medidas como tiempo transcurrido entre instantes. La validación, el consumo del token y la cancelación SHALL ocurrir en una sola transacción atómica.

#### Scenario: Cancelación confirmada
- **WHEN** el paciente presenta un token válido para un turno elegible dentro del plazo
- **THEN** el sistema cancela el turno, consume el token y libera el horario al confirmar la transacción

#### Scenario: Token fuera de plazo
- **WHEN** el paciente presenta un token cuyo turno tiene menos de 48 horas restantes o ya comenzó
- **THEN** el sistema rechaza la cancelación, no consume el token por ese intento y conserva el turno y su ocupación

### Requirement: Protección frente a replay y concurrencia

El sistema SHALL permitir como máximo una cancelación exitosa por token. Las solicitudes repetidas o concurrentes con el mismo token SHALL no volver a mutar el turno ni revelar su estado, datos o existencia.

#### Scenario: Replay del token
- **WHEN** se presenta un token ya consumido
- **THEN** el sistema rechaza la operación sin cancelar de nuevo ni devolver datos del turno

#### Scenario: Solicitudes concurrentes
- **WHEN** dos solicitudes presentan el mismo token válido de forma concurrente
- **THEN** como máximo una cancela el turno y la otra es rechazada, sin filtrar información sobre el turno

### Requirement: Contrato HTTP no mutante para GET

El sistema SHALL no cancelar ni mutar un turno ante una solicitud `GET` del enlace. La cancelación SHALL ocurrir únicamente tras una acción explícita del paciente mediante `POST`.

#### Scenario: GET no cancela
- **WHEN** un cliente o previsualizador automático abre el enlace con GET
- **THEN** el sistema no cancela ni muta el turno y puede presentar o validar la confirmación sin efectos persistentes

#### Scenario: POST cancela
- **WHEN** el paciente confirma explícitamente la cancelación mediante POST
- **THEN** el sistema ejecuta la validación y cancelación atómica correspondiente

### Requirement: Respuestas sin filtración de datos

El sistema SHALL no revelar en sus respuestas el estado del turno, su existencia ni datos clínicos o personales cuando el token sea ausente, inválido, expirado, consumido o no elegible.

#### Scenario: Token inválido o ausente
- **WHEN** se presenta un token ausente, inválido o no elegible
- **THEN** el sistema responde con un resultado genérico sin datos del turno y sin confirmar su existencia

### Requirement: Integración con la cancelación transaccional existente

El sistema SHALL reutilizar la cancelación transaccional y los invariantes de `agenda-persistida` para liberar el horario al cancelar por enlace, sin convertir la operación interna en una vía de autorización de paciente ni exponer sus permisos internos.

#### Scenario: Liberación del horario
- **WHEN** la cancelación por enlace se confirma
- **THEN** el turno pasa a `CANCELADO`, libera al profesional y al sillón para altas, reprogramaciones y bloqueos, y conserva los demás datos del turno

#### Scenario: Sin bypass de autorización interna
- **WHEN** se invoca la cancelación por enlace
- **THEN** la autorización proviene únicamente del token válido; no se acepta identidad interna, cookie de sesión ni identificador de turno como credencial

### Requirement: Elegibilidad de estados del turno

El sistema SHALL permitir la cancelación por enlace de turnos en estado `PENDIENTE` o `CONFIRMADO`, siempre que el turno no haya comenzado y se cumpla el límite de 48 horas.

#### Scenario: Turno pendiente elegible
- **WHEN** el paciente presenta un token válido para un turno `PENDIENTE` elegible
- **THEN** el sistema permite la cancelación

#### Scenario: Turno confirmado elegible
- **WHEN** el paciente presenta un token válido para un turno `CONFIRMADO` elegible
- **THEN** el sistema permite la cancelación

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

### Requirement: Entrega interna del token emitido
El sistema SHALL entregar a los consumidores internos el token en claro emitido al crear o reprogramar un turno, solo después de confirmar la transacción. SHALL NOT persistirlo en claro, incluirlo en registros ni exponerlo por HTTP. Un fallo del consumidor SHALL NOT revertir ni hacer fallar la operación de agenda.

#### Scenario: Entrega tras confirmar el alta
- **WHEN** se confirma la transacción que crea un turno con la ventana abierta
- **THEN** el consumidor recibe una vez el turno y un token que permite cancelarlo

#### Scenario: Entrega tras reprogramar
- **WHEN** se confirma una reprogramación con la ventana abierta
- **THEN** el consumidor recibe un token nuevo y el anterior queda rechazado

#### Scenario: Sin entrega
- **WHEN** la transacción revierte, la ventana está cerrada o el turno solo se confirma
- **THEN** el consumidor no recibe ningún token

#### Scenario: Fallo del consumidor
- **WHEN** el consumidor falla al recibir el token
- **THEN** la operación de agenda conserva su resultado confirmado

#### Scenario: Representación del evento
- **WHEN** el evento se convierte a texto
- **THEN** el texto no contiene el token
