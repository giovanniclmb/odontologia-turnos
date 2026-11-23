# Proposal

## Why

El backend ya identifica pacientes y profesionales, y C-03 aporta identidad y autorización, pero todavía no gestiona los datos mínimos del paciente ni una ficha clínica. Este change concreta el acceso por paciente y protege antecedentes y notas en el backend para poder usar US-001, US-002 y US-003 sin depender de controles visuales.

## What Changes

- Completar la gestión de pacientes con `codigo`, nombre, apellido, email obligatorio y teléfono opcional; permitir crear, consultar/listar y editar datos administrativos sin habilitar eliminación.
- Asignar explícitamente cada paciente a cero o más profesionales. Recepción administra las asignaciones; odontología consulta y edita solo pacientes asignados. Un paciente creado por odontología queda sin asignación hasta que recepción lo asigne.
- Crear la ficha al guardar los primeros antecedentes o la primera nota. La ficha contiene antecedentes en texto y notas clínicas separadas con fecha y autor; solo el odontólogo autor puede editar una nota y no se habilita eliminar fichas, antecedentes ni notas.
- Permitir lectura clínica a recepción y odontología conforme al alcance por registro; permitir edición de antecedentes y notas solo a odontología asignada.
- Conceder `paciente:datos-admin` a recepción y odontología. Mantener la autorización y el alcance en servicios de backend.
- Definir operaciones y DTO de servicio, sin rutas ni transporte HTTP. Incorporar modelos y migración Flyway nuevos, conservando el UUID y `codigo` de paciente existentes.
- Usar exclusivamente datos ficticios en pruebas y ejemplos.

## Capabilities

### New Capabilities

- `pacientes-y-ficha-clinica`: gestión administrativa de pacientes y acceso por registro a antecedentes y notas clínicas.

### Modified Capabilities

- `acceso-y-permisos-internos`: otorgar `paciente:datos-admin` a ambos roles y concretar la autorización sobre pacientes según su asignación profesional.

## Impact

Se afectan los servicios y la autorización del backend Java, los DTO de servicio, las entidades/repositorios JPA, una nueva migración Flyway y pruebas unitarias y de integración PostgreSQL. No se añade HTTP, UI, odontograma, funciones administrativas ampliadas ni permisos de eliminación. C-03 está implementado, verificado, archivado e integrado según CHANGES.md; antes de Apply se comprobará su integración en `main` conforme al flujo del repo.
