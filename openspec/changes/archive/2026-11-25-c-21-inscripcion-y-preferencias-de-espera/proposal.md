# Proposal

## Why

C-16 ya identifica al paciente vinculado a una ficha. C-21 le permite registrar y mantener sus preferencias de espera, la parte de backend de US-014 y RN-LE-01.

## What Changes

- Permitir al paciente vinculado crear, consultar, editar y dar de baja sus inscripciones.
- Registrar prestación, rango de fechas y franjas horarias con profesional opcional.
- Permitir hasta una inscripción vigente por paciente y prestación. La vigencia termina al finalizar el rango de fechas o al dar de baja la inscripción.
- Guardar las preferencias de forma independiente de la agenda. C-22 cubre la compatibilidad de huecos, los avisos y la asignación por recepción. C-24 cubre la interfaz del paciente.

## Capabilities

### New Capabilities

- `inscripciones-lista-de-espera` cubre el ciclo de la inscripción propia y la validación de preferencias.

### Modified Capabilities

## Impact

- Backend Spring Boot y PostgreSQL. Reutiliza el contrato de autorización de C-16 y las referencias de prestaciones y profesionales de C-05.
- Incorpora un contrato de servicio y persistencia para la lista de espera que sirve de base para C-22 y C-24.
