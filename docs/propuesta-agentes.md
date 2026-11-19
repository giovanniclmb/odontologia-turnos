# Propuesta de instrucciones del proyecto

Estado: las cuatro reglas fueron confirmadas; RA-04 fue ajustada por el usuario para permitir commits autónomos atómicos y reservar push a pedido explícito. Propuesta concretada en AGENTS.md y CLAUDE.md. Se conserva como registro de revisión, no como fuente activa.

## Archivos propuestos

Generar `AGENTS.md` y `CLAUDE.md` en la raíz, con el mismo contenido administrado. `AGENTS.md` será la fuente principal de mantenimiento y cualquier actualización se replicará a `CLAUDE.md` en la misma operación. La consigna requiere ambos; Codex usa AGENTS.md como nombre estándar. No se modifica la configuración global ni se selecciona otro cliente.

## Stack Tecnológico

| Capa | Tecnología aprobada | Versión mínima |
| --- | --- | --- |
| Frontend | HTML, CSS y TypeScript vanilla | Pendiente; no se incorpora un framework de UI |
| Backend | Java con Spring Boot | Pendiente |
| Base de datos | PostgreSQL | Pendiente |

Copiar la tabla de [KB 02](../knowledge-base/02_descripcion_general.md). No elegir versiones, build, ORM, proveedor de email ni autenticación por redactar las instrucciones.

## Base de Conocimiento

Fuentes y precedencia: decisiones expresas del usuario y ampliación aprobada, [índice KB](../knowledge-base/README.md), [reglas](../knowledge-base/05_reglas_de_negocio.md), [funcionalidades](../knowledge-base/06_funcionalidades.md), [flujos](../knowledge-base/07_flujos_principales.md), [arquitectura pendiente](../knowledge-base/08_arquitectura_propuesta.md) y [preguntas abiertas](../knowledge-base/10_preguntas_abiertas.md). El archivo final enlazará los diez canónicos desde la raíz.

## Skills Disponibles

Mapeo propuesto según responsabilidades; se cargan únicamente las pertinentes a cada encargo. Los nombres y rutas se resuelven desde el [registro existente](../.atl/skill-registry.md). No se copian sus 335 reglas ni se adoptan tecnologías del catálogo.

| Rol existente | Skills pertinentes |
| --- | --- |
| Orchestrator | active-delivery, active-orchestrator, openspec-propose, openspec-update-change, writing-plans |
| Explorer | openspec-explore, diagnosing-bugs, systematic-debugging |
| Researcher | api-and-interface-design, java-springboot, supabase-postgres-best-practices |
| Worker backend | java-springboot, api-and-interface-design, supabase-postgres-best-practices, test-driven-development, openspec-apply-change |
| Worker frontend | frontend-design, impeccable, design-system, web-design-guidelines; mantener TypeScript vanilla |
| Reviewer | security-audit, web-design-guidelines, verification-before-completion |
| Integrator | verification-before-completion, openspec-sync-specs, openspec-archive-change |

El mapeo no modifica modelos ni esfuerzos de los roles globales, no crea subagentes ni habilita herramientas ausentes. La revisión de seguridad es enfocada salvo solicitud expresa de auditoría completa.

## Roadmap de Changes

[CHANGES.md](../CHANGES.md) conserva 25 items pendientes en seis fases. Cadena representativa de once items: C-01 → C-02 → C-03 → C-05 → C-15 → C-06 → C-08 → C-18 → C-19 → C-20 → C-14.

C-01, crear-turno-sin-solapamientos-dominio, es candidato al único change académico, todavía no elegido por el usuario (PA-13). Las instrucciones no lo seleccionan ni empiezan su implementación. Seguir las dependencias completas y Leer antes de cada ficha.

## Reglas Duras (específicas del proyecto)

Se heredan el sistema multiagente, la gobernanza, Engram y el flujo de diseño del archivo global real de Codex. No se duplicarán. El global inspeccionado no contiene una regla explícita de TDD ni política de commits/build; se proponen abajo sin atribuirlas al global.

Reglas ya respaldadas por decisiones del proyecto: mantener el stack y presupuesto cero; no almacenar datos reales de pacientes en repositorio, ejemplos o capturas; usar direcciones de prueba indicadas por el equipo para demostrar email; conservar reglas de disponibilidad, permisos clínicos y plazos de la KB. No resolver las PA abiertas por inferencia.

Reglas revisadas por menú: RA-01 a RA-03 aprobadas; RA-04 aprobada con la redacción ajustada por el usuario.
- **RA-01:** TypeScript con strict habilitado; evitar any explícito y usar tipos concretos o unknown con validación.
- **RA-02:** Backend con inyección por constructor, validación de entradas y DTOs de contrato separados de entidades de persistencia si se usa ORM.
- **RA-03:** TDD para reglas de negocio: observar prueba fallida antes de implementar y verificar el resultado; usar pruebas unitarias de dominio y PostgreSQL real para verificar persistencia o concurrencia, sin atribuir esa evidencia a mocks.
- **RA-04:** El agente puede compilar, ejecutar pruebas y crear commits por su cuenta dentro del alcance autorizado. Cada commit debe ser atómico, cubrir una tarea específica y tener un mensaje específico y detallado que explique el cambio y su propósito. Hacer push requiere un pedido explícito del usuario.

No se agrega una política de Conventional Commits o de coautoría por defecto. Si el equipo quiere alguna, puede indicarla al ajustar las reglas.

## Flujo de Trabajo

Leer KB relevante → localizar item y dependencias en CHANGES → resolver preguntas bloqueantes con el usuario → acordar alcance/aceptación y workflow → implementar solo cuando corresponda y esté autorizado → verificar y registrar evidencia → actualizar roadmap y archivar por CLI cuando corresponda.

La política visual sigue diferida y debe resolverse antes de Apply visual. La revisión personal de cinco fuentes sigue pendiente. Las instrucciones generadas no reemplazan validación académica ni establecen proveedor de email, intervalos iniciales o reglas técnicas no aprobadas.

## Fuentes de la recomendación de archivos

- [Consigna académica](desarrollo-active-stack%20Global%20%281%29.docx): exige AGENTS.md y CLAUDE.md en la raíz.
- [Documentación oficial de instrucciones Codex](https://learn.chatgpt.com/docs/agent-configuration/agents-md): descubrimiento de AGENTS.md y nombres fallback configurables.

El perfil local no define project_doc_fallback_filenames. Esta comprobación es de lectura; no se cambió configuración global.
