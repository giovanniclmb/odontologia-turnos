# Spec Delta

## ADDED Requirements

### Requirement: Consumo HTTP de servicios de pacientes y ficha
El sistema SHALL permitir que los servicios de pacientes y ficha clínica se consuman mediante endpoints HTTP autenticados, reutilizando los DTO y validaciones existentes y aplicando los mismos permisos y alcance por rol.

#### Scenario: Listado vía HTTP
- **WHEN** un cliente autenticado solicita el listado de pacientes
- **THEN** el sistema devuelve los datos administrativos básicos según el alcance del rol

#### Scenario: Operación denegada vía HTTP
- **WHEN** un cliente sin permiso o fuera de alcance invoca una operación
- **THEN** el sistema rechaza la solicitud y conserva los datos confirmados

#### Scenario: Creación de ficha vía HTTP
- **WHEN** un odontólogo asignado guarda antecedentes o una nota mediante HTTP
- **THEN** el sistema crea o actualiza la ficha y devuelve el resultado contractual