# Spec Delta

## ADDED Requirements

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
