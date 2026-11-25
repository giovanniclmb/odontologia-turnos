# Spec Delta

## ADDED Requirements

### Requirement: Identificación del paciente en la agenda
Cada turno devuelto por la consulta de agenda SHALL incluir nombre y apellido de su paciente y ningún otro dato personal ni clínico. La regla SHALL aplicarse a todos los turnos visibles para el solicitante, incluidos los cancelados.

#### Scenario: Recepción consulta la agenda
- **WHEN** recepción consulta un sillón con turnos de distintos profesionales
- **THEN** cada turno incluye nombre y apellido del paciente, sin email, teléfono ni contenido clínico

#### Scenario: Odontología sin asignación activa
- **WHEN** un odontólogo consulta un turno propio de un paciente que ya no tiene asignado
- **THEN** el turno incluye solo nombre y apellido como datos del paciente

#### Scenario: Turnos de otro profesional
- **WHEN** un odontólogo consulta un sillón que tiene turnos de otro profesional
- **THEN** no recibe esos turnos ni los nombres de sus pacientes
