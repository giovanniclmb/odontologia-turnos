# Spec Delta

## ADDED Requirements

### Requirement: Motivo de fallo sin contenido del mensaje
El motivo de fallo que el sistema registra o devuelve SHALL limitarse a la categoría del error y al estado del proveedor. SHALL NOT incluir la respuesta del proveedor, el cuerpo ni enlaces del mensaje.

#### Scenario: Rechazo del proveedor con detalle
- **WHEN** el proveedor rechaza el envío y su respuesta repite parte del mensaje
- **THEN** el motivo registrado contiene solo el estado devuelto
