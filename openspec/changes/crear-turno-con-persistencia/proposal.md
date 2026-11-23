# Proposal

## Why

C-01 calcula la disponibilidad a partir de reservas entregadas por el llamador. C-06 integra esa regla con pacientes, recursos y bloqueos persistidos para que el equipo pueda crear turnos y consultar la agenda del sillón sin registrar asignaciones incompatibles cuando hay solicitudes simultáneas.

## What Changes

- Agregar un servicio interno de alta que valide la identidad y el alcance del operador, las referencias del paciente y de la prestación, y la disponibilidad del profesional y del sillón. Recepción puede crear con cualquier profesional elegible. Odontología solo puede crear para su propio profesional y pacientes ya asignados a él.
- Aceptar inicios estrictamente futuros, sin aplicar franjas semanales. Guardar la duración efectiva y el estado inicial pendiente de confirmación. El turno ocupa ambos recursos desde que se confirma la transacción.
- Consultar turnos por sillón y período. Recepción puede ver los turnos del sitio. Odontología ve únicamente los de su profesional vinculado.
- Proteger el alta contra carreras por profesional, sillón y bloqueos. Verificar la exclusión con transacciones concurrentes en PostgreSQL real.
- Mantener el alcance en el servicio backend. Reprogramación, cancelación, confirmación manual, comunicaciones y presentación corresponden a changes posteriores.

## Capabilities

### New Capabilities

- `agenda-persistida`: alta y consulta autorizadas de turnos y consistencia de reservas persistidas.

### Modified Capabilities

## Impact

- Backend Java y Spring Boot, entidades JPA/Hibernate, migraciones Flyway y pruebas con PostgreSQL 18.7. Reutiliza el dominio de C-01, identidad de C-03, pacientes y asignaciones de C-04, y recursos e intervalo de C-05.
- Propuesta para revisión. Antes de Apply deben integrarse [C-15](https://github.com/giovanniclmb/odontologia-turnos/issues/16) y su contrato de bloqueos, acordarse los bordes temporales y la exclusión compartida, y revisarse la propuesta escrita actualizada. La secuencia de migraciones se coordina sobre la base integrada. El [Issue #7](https://github.com/giovanniclmb/odontologia-turnos/issues/7) sigue abierto.
