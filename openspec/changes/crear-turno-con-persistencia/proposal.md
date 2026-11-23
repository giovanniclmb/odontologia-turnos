# Proposal

## Why

C-01 calcula la disponibilidad a partir de reservas entregadas por el llamador. C-06 integra esa regla con pacientes, recursos y bloqueos persistidos para que el equipo pueda crear turnos y consultar la agenda del sillón sin registrar asignaciones incompatibles cuando hay solicitudes simultáneas.

## What Changes

- Agregar un servicio interno de alta que valide la identidad y el alcance del operador, las referencias del paciente y de la prestación, y la disponibilidad del profesional y del sillón. Recepción puede crear con cualquier profesional elegible. Odontología solo puede crear para su propio profesional y pacientes ya asignados a él.
- Aceptar inicios estrictamente futuros, sin aplicar franjas semanales. Guardar la duración efectiva y el estado inicial pendiente de confirmación. El turno ocupa ambos recursos desde que se confirma la transacción.
- Consultar turnos por sillón y período. Recepción puede ver los turnos del sitio. Odontología ve únicamente los de su profesional vinculado.
- Reutilizar el protocolo transaccional de C-15 para proteger el alta contra carreras por profesional, sillón y bloqueos. Conectar la consulta de turnos ocupantes de C-15 a las reservas persistidas. Verificar las carreras con transacciones concurrentes en PostgreSQL real.
- Mantener el alcance en el servicio backend. Reprogramación, cancelación, confirmación manual, comunicaciones y presentación corresponden a changes posteriores.

## Capabilities

### New Capabilities

- `agenda-persistida`: alta y consulta autorizadas de turnos y consistencia de reservas persistidas.

### Modified Capabilities

## Impact

- Backend Java y Spring Boot, entidades JPA/Hibernate, migraciones Flyway y pruebas con PostgreSQL 18.7. Reutiliza el dominio de C-01, identidad de C-03, pacientes y asignaciones de C-04, y recursos e intervalo de C-05.
- [C-15 está integrado en `main`](https://github.com/giovanniclmb/odontologia-turnos/pull/33). Esta propuesta incorpora su [contrato de bloqueos](../../specs/bloqueos-de-recursos/spec.md). La propuesta escrita sigue en revisión y Apply requiere un pedido explícito. La numeración de la migración se coordina sobre la base vigente. El [Issue #7](https://github.com/giovanniclmb/odontologia-turnos/issues/7) sigue abierto.
