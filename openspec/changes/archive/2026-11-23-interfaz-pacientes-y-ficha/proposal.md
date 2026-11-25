# Proposal

## Why

El backend ya gestiona pacientes, ficha clínica, acceso y agenda a nivel de servicio, pero ningún consumidor puede operarlos: no existe transporte HTTP ni interfaz de usuario. C-11 habilita el primer uso real del sistema por recepción y odontología, estableciendo la base compartida de frontend (estructura, TypeScript estricto, build, convenciones y autenticación por token opaco) que C-10 reutilizará. Sin este change, US-001, US-002 y US-003 permanecen inaccesibles y el MVP no puede demostrarse.

## What Changes

- Introducir transporte HTTP en el backend: `spring-boot-starter-web`, controladores de autenticación (login que emite token opaco reutilizando C-03), pacientes y ficha clínica, mapeo DTO↔JSON y manejo de errores consistente. Documentar la migración desde `web-application-type: none`.
- Establecer la base de frontend vanilla: estructura de directorios, TypeScript estricto sin `any`, build con `tsc` puro, servidor de desarrollo, convenciones de código y capa HTTP compartida con token en `Authorization`.
- Implementar US-001: listado y búsqueda de pacientes (código exacto + nombre/apellido parcial), alta, edición, detalle y asignación de profesionales (reutilizando C-05).
- Implementar US-002/US-003: visualización de antecedentes y notas, edición de antecedentes, creación/edición de notas con autoría, y modo solo lectura para recepción.
- Aplicar gating por rol en la interfaz, sin sustituir la validación del backend.
- No introducir eliminación en ningún nivel (pacientes, fichas, antecedentes, notas).
- Capturar la política visual como diferida; no crear mockups en esta etapa.

## Capabilities

### New Capabilities

- `interfaz-pacientes-y-ficha`: operación de pacientes y ficha clínica desde la interfaz web vanilla, con autenticación por token opaco, capa HTTP compartida y gating por rol.

### Modified Capabilities

- `pacientes-y-ficha-clinica`: permitir el consumo HTTP de los servicios existentes (ajustar el requisito de ausencia de transporte para este change, con justificación y aprobación).
- `acceso-y-permisos-internos`: introducir login por token opaco y transporte HTTP justificado, conservando la autorización a nivel de servicio.

## Impact

- Backend: nuevo starter web, controladores, configuración de seguridad/CORS mínima, serialización JSON, manejo de errores; cambios en `application.yaml` (`web-application-type`).
- Frontend: nuevo directorio y toolchain (TypeScript, `tsc`, dev server), sin frameworks.
- Dependencias: C-04 integrado (servicios y migraciones). Reutiliza `ServicioRecursosAgenda.consultarProfesionales` y C-03 para identidad.
- Pruebas: TDD para validaciones/autorización; integración con Testcontainers para controladores; verificación manual de UI.
- Coordinación con Luciano (C-10): C-11 entrega la base compartida de frontend y la capa HTTP; C-10 la reutiliza sin duplicar.