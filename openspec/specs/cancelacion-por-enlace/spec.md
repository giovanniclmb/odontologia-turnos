# cancelacion-por-enlace Specification

## Purpose

Autorizar la cancelación de un turno por parte del paciente mediante un enlace de un solo uso, con límite temporal de 48 horas y sin exponer la autenticación interna ni datos clínicos.

## Requirements

### Requirement: Emisión de un token de cancelación por turno elegible

El sistema SHALL generar un token de cancelación opaco y aleatorio cuando un turno `PENDIENTE` o `CONFIRMADO` tenga un inicio estrictamente posterior al instante de emisión más 48 horas. SHALL persistir únicamente el hash del token, asociado al identificador del turno y a la versión vigente al momento de la emisión. SHALL no emitir token cuando queden menos de 48 horas para el inicio del turno.

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
