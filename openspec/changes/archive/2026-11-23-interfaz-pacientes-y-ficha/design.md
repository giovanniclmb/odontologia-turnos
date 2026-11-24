# Design

## Context

Ver proposal.md (Why/What). El backend ya contiene servicios y DTO para pacientes, ficha y acceso (C-03/C-04), sin transporte HTTP y con `web-application-type: none`. C-05 aporta profesionales persistidos y `ServicioRecursosAgenda.consultarProfesionales`. No existe frontend. Este change introduce la capa de transporte y la base de UI, manteniendo la autorización a nivel de servicio.

## Goals / Non-Goals

**Goals:**

- Habilitar HTTP para autenticación (token opaco) y para pacientes/ficha, reutilizando servicios existentes.
- Establecer la base de frontend vanilla con TypeScript estricto y build con `tsc`.
- Implementar US-001/US-002/US-003 con gating por rol en UI y validación en backend.
- Compartir contratos HTTP/TS con C-10.

**Non-Goals:**

- No definir endpoints definitivos de agenda ni otros dominios (C-10).
- No implementar odontograma, pagos ni funciones administrativas ampliadas.
- No introducir frameworks de UI ni bundlers.

## Decisions

### Transporte HTTP y autenticación

- Agregar `spring-boot-starter-web` y cambiar `web-application-type` a `servlet`. Justificado por la necesidad de consumir el sistema desde la interfaz; se documenta la migración.
- Login emite token opaco (UUID o cadena aleatoria) hasheado y persistido en `token_acceso` (C-03), con expiración configurable. El cliente lo envía en `Authorization: Bearer <token>`. Un filtro/interceptor resuelve la `Identidad` a partir del token y la inyecta en los controladores.
- Controladores delgados: delegan en `ServicioAutenticacion`, `ServicioPacientes`, `ServicioFichaClinica`, `ServicioAsignacionPacientes`; no contienen reglas de negocio.
- DTO de transporte (records) separados de los DTO de servicio cuando sea necesario, con mapeo explícito; validación de entrada con Bean Validation o validación manual en servicio (ya existente).
- Manejo de errores centralizado: mapear `AutorizacionDenegadaException` a 403, `IllegalArgumentException` a 400, no encontrado a 404, conflictos a 409. Respuestas JSON con código y mensaje estable.

### Base de frontend

- Estructura `frontend/` con `src/` (TS), `public/` (HTML/CSS/assets), `tsconfig.json` con `strict: true`, `noEmitOnError`, salida a `dist/`.
- Build con `tsc` puro; dev server con un servidor estático simple (sin bundler).
- Capa HTTP compartida: cliente fetch con token en `Authorization`, manejo de errores y tipos TS para DTO.
- Convenciones: módulos por dominio (auth, pacientes, ficha), sin `any`, tipos concretos/`unknown` con validación.

### Consumo de servicios existentes

- Reutilizar `ServicioRecursosAgenda.consultarProfesionales` para el selector de profesionales en asignación; solo requiere identidad autenticada.
- Reutilizar DTO de servicio (`CrearPaciente`, `ActualizarDatosPaciente`, `FiltroBusquedaPaciente`, etc.) como contrato; los controladores los adaptan a JSON.

### Gating por rol

- La UI oculta acciones no autorizadas según el rol devuelto por login, pero el backend revalida cada operación. No se confía en controles visuales para autorización.

### Ficha y notas

- La UI muestra antecedentes editables por odontólogo asignado y notas con autor/fecha; recepción solo lectura. La edición de nota propia se habilita si el autor coincide y sigue asignado.

### Política visual

- **Visual policy: system-and-mockups (resuelta 2026-11-23).** Fundación en `design-system/` (Revision 1, aprobada 2026-11-23). Mockups de C-11 creados en `mockups/` y pendientes de aprobación.

## UI Mockups

- Design system revision: 1
- Decision: yes (2026-11-23)
- Mockups: openspec/changes/interfaz-pacientes-y-ficha/mockups/
- Approval: approved (2026-11-23, by user)
- Preview: rendered (2026-11-23, previews/)
- Visual verify: passed (shell, 2026-11-24; interactive flows pending backend)

## Risks / Trade-offs

- [Introducir web en un proyecto `none`] → migración documentada y pruebas de arranque; mantener `ddl-auto=validate`.
- [Token opaco y expiración] → persistencia en `token_acceso`, revocación al logout; expiración configurable; sin cookies.
- [CORS y seguridad mínima] → configurar solo lo necesario para dev; revisar antes de producción.
- [Coordinación con C-10] → C-11 entrega la base compartida; C-10 la reutiliza para evitar duplicación.
- [Política visual diferida] → bloquea Apply hasta resolverla; registrar explícitamente.
- [TDD estricto] → pruebas de controladores con Testcontainers; UI verificada manualmente.

## Migration Plan

1. Agregar dependencia web y ajustar `application.yaml` (`web-application-type: servlet`).
2. Implementar filtro de token y controlador de login.
3. Implementar controladores de pacientes y ficha con manejo de errores.
4. Crear base frontend y capa HTTP.
5. Implementar pantallas US-001/US-002/US-003.
6. Resolver política visual antes de Apply y crear mockups si corresponde.
7. Ejecutar TDD (unit + integración Testcontainers) y verificación manual de UI.

## Open Questions

- PA-05 parcial: contrato HTTP exacto (rutas, códigos, payloads) se define en Apply con `api-and-interface-design`.
- PA-09 parcial: versiones concretas de herramientas de frontend se fijan en Apply.
- PA-10/PA-11: items humanos (defensa, revisión de fuentes) no bloqueantes.