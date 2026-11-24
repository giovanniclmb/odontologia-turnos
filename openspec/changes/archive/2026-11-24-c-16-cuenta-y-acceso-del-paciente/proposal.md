# Proposal

## Why

US-014 requiere que cada paciente gestione su propia inscripción en la lista de espera, pero el sistema aún no tiene identidad de paciente ni una forma segura de relacionar una cuenta con la ficha existente. Este cambio define el autorregistro, la confirmación manual del vínculo por recepción y el acceso limitado a las operaciones propias.

## What Changes

- Incorporar el registro de cuentas de paciente y su ingreso mientras el vínculo está pendiente.
- Permitir que recepción confirme manualmente qué ficha de paciente corresponde a una cuenta registrada; el ingreso pendiente no habilita operaciones de lista de espera.
- Habilitar el acceso autenticado únicamente a las inscripciones pertenecientes al paciente vinculado, rechazando operaciones sobre inscripciones de otra persona.
- Mantener fuera del alcance la ficha clínica, la administración clínica, la reserva de turnos y la asignación de huecos.
- No seleccionar JWT, sesión, proveedor de identidad ni política de recuperación. Definir errores y recuperación solo en los términos aprobados para este cambio.
- La implementación queda condicionada a la integración de C-03 y C-04 y a las aprobaciones de gobernanza CRITICO.

## Capabilities

### New Capabilities
- `cuentas-paciente`: registro, ingreso y vinculación manual de una cuenta de paciente, con autorización limitada a sus propias inscripciones.

### Modified Capabilities
- Ninguna. El acceso para pacientes se incorpora como capacidad separada del acceso interno y de la ficha clínica.

## Impact

- Backend Java/Spring: servicios de cuenta y autorización apoyados en la identidad y persistencia existentes; el contrato se define a nivel de servicio, sin agregar transporte HTTP en este change.
- Persistencia PostgreSQL/Flyway: relación entre la cuenta y el paciente, coordinada con las migraciones existentes y la integración de C-04.
- C-21 consumirá la identidad de paciente y la autorización propia para gestionar inscripciones; C-16 no define el ciclo de vida ni las preferencias de la inscripción.
- Pruebas: acceso válido e inválido, cuenta pendiente y vinculada, operaciones habilitadas tras confirmación y rechazo de acceso a inscripciones ajenas.
