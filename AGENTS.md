<!-- active-stack:agent-instruction -->
# Sistema de turnos odontológicos — Instrucciones para Agentes

Cliente de trabajo: Codex. `AGENTS.md` es la fuente principal de mantenimiento; `CLAUDE.md` conserva el mismo contenido por requisito académico. Actualizar ambos en la misma operación y comprobar que coinciden. Estos archivos describen el proyecto; no seleccionan modelos, conceden herramientas ni sustituyen las instrucciones globales del cliente activo.

## Stack Tecnológico

| Capa | Tecnología aprobada | Versión mínima |
| --- | --- | --- |
| Frontend | HTML, CSS y TypeScript vanilla | Pendiente; no se incorpora un framework de UI |
| Backend | Java 25 y Spring Boot 4.1.2 | Maven, JPA/Hibernate y Flyway aprobados en C-02 |
| Base de datos | PostgreSQL | 18.7; Compose desarrollo y Testcontainers pruebas |

C-01 y C-02 usan Java 25/Maven en backend/. C-02 incorpora Boot 4.1.2, JPA/Hibernate separado del dominio y Flyway; PostgreSQL 18.7 mediante Compose y pruebas Testcontainers. Desde C-11 el backend expone HTTP; las decisiones posteriores sobre contratos, autenticación y email constan en las KB 09 y 10. El despliegue productivo sigue pendiente. El frontend es vanilla; no incorporar React, Next.js o shadcn por disponer de sus skills. Presupuesto externo cero.

## Base de Conocimiento

Leer el [índice de KB](knowledge-base/README.md) y los documentos relevantes:

| Archivo | Contenido |
| --- | --- |
| [01_vision_y_objetivos.md](knowledge-base/01_vision_y_objetivos.md) | Visión y objetivos |
| [02_descripcion_general.md](knowledge-base/02_descripcion_general.md) | Descripción general |
| [03_actores_y_roles.md](knowledge-base/03_actores_y_roles.md) | Actores y roles |
| [04_modelo_de_datos.md](knowledge-base/04_modelo_de_datos.md) | Modelo de datos |
| [05_reglas_de_negocio.md](knowledge-base/05_reglas_de_negocio.md) | Reglas de negocio |
| [06_funcionalidades.md](knowledge-base/06_funcionalidades.md) | Funcionalidades |
| [07_flujos_principales.md](knowledge-base/07_flujos_principales.md) | Flujos principales |
| [08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md) | Arquitectura propuesta |
| [09_decisiones_y_supuestos.md](knowledge-base/09_decisiones_y_supuestos.md) | Decisiones y supuestos |
| [10_preguntas_abiertas.md](knowledge-base/10_preguntas_abiertas.md) | Preguntas abiertas |

Las [decisiones estructuradas](docs/discovery/decisiones-aprobadas.json) y la [ampliación aprobada](docs/discovery/ampliacion-paciente.md) documentan el origen del alcance vigente. Las decisiones expresas posteriores del usuario prevalecen sobre antecedentes históricos; no convertir supuestos o preguntas abiertas en reglas adoptadas.

## Skills Disponibles

Elegir solo las skills pertinentes al encargo, desde el [registro local](.atl/skill-registry.md); no cargar todo el catálogo ni instalar candidatas por estar recomendadas.

| Agente o rol | Responsabilidad en el proyecto | Skills pertinentes |
| --- | --- | --- |
| Orchestrator | Alcance, ruta de entrega y planificación | `active-delivery`, `active-orchestrator`, `openspec-propose`, `openspec-update-change`, `writing-plans` |
| Explorer | Exploración y diagnóstico de lectura | `openspec-explore`, `diagnosing-bugs`, `systematic-debugging` |
| Researcher | Contratos y decisiones técnicas | `api-and-interface-design`, `java-springboot`, `supabase-postgres-best-practices` |
| Worker backend | Reglas y persistencia | `java-springboot`, `api-and-interface-design`, `supabase-postgres-best-practices`, `test-driven-development`, `openspec-apply-change` |
| Worker frontend | Interfaz vanilla y accesibilidad | `frontend-design`, `impeccable`, `design-system`, `web-design-guidelines` |
| Reviewer | Revisión enfocada de lógica, permisos y UI | `security-audit`, `web-design-guidelines`, `verification-before-completion` |
| Integrator | Verificación y cierre | `verification-before-completion`, `openspec-sync-specs`, `openspec-archive-change` |

Los compact rules se resuelven desde `.atl/skill-registry.md`, generado por skill-registry; no se duplican aquí. La copia versionada para la entrega corresponde al equipo donde se generó: sus rutas globales son propias de ese entorno y conviene regenerarlo localmente sin publicar esa regeneración salvo acuerdo. Si falta el registro, verificar el inventario disponible antes de delegar; no asumir skills instaladas. Este mapeo conserva los roles globales y sus límites de disponibilidad, sin elegir modelos sustitutos ni activar una auditoría completa.

## Roadmap de Changes

[CHANGES.md](CHANGES.md) es la fuente de alcance, dependencias y estados de los 25 items. Se organiza en seis fases:

1. Dominio aislado y persistencia base.
2. Acceso y recursos.
3. Agenda, bloqueos y participación del paciente.
4. Comunicaciones y recuperación de huecos.
5. Interfaces del equipo y del paciente.
6. Integración reproducible.

Cadena de precedencia representativa de once items, usando unidades equivalentes y sin promesa de duración:

`C-01 → C-02 → C-03 → C-05 → C-15 → C-06 → C-08 → C-18 → C-19 → C-20 → C-14`

C-01 (`crear-turno-sin-solapamientos-dominio`) fue elegido por Giovanni, implementado, verificado y archivado; PA-13 está resuelta. Consultar su estado de integración en CHANGES.md. Su módulo aislado no prueba integridad concurrente de PostgreSQL. Antes de iniciar cualquier item, comprobar dependencias y lecturas de su ficha; no interpretar el backlog completo como compromiso de implementación para la entrega.

## Reglas Duras (específicas del proyecto)

Se heredan las reglas globales del cliente activo ya existentes sobre coordinación multiagente, gobernanza, Engram y diseño. Aquí se conservan las decisiones específicas y las reglas técnicas confirmadas, sin atribuir al global TDD o políticas Git que no contiene.

- **RA-01 — TypeScript:** NUNCA usar `any` explícito → habilitar `strict` y usar tipos concretos o `unknown` con validación.
- **RA-02 — Spring Boot:** usar inyección por constructor y validar las entradas. Si se usa ORM, separar los DTOs del contrato de las entidades de persistencia; esta regla no elige un ORM ni define endpoints.
- **RA-03 — Pruebas:** aplicar TDD a reglas de negocio: observar el fallo esperado antes de implementar y comprobar después el resultado. Usar pruebas unitarias para el dominio y PostgreSQL real para comprobar persistencia o concurrencia; NUNCA presentar mocks como evidencia de integridad de la base.
- **RA-04 — Autonomía y Git:** el agente puede compilar, ejecutar pruebas y crear commits por su cuenta dentro del alcance autorizado. Cada commit debe ser atómico, cubrir una tarea específica y tener un mensaje específico y detallado que explique el cambio y su propósito. NUNCA hacer push sin un pedido explícito del usuario.
- **Datos del dominio:** NUNCA almacenar datos reales de pacientes en el repositorio, ejemplos o capturas → usar datos clínicos ficticios. Para demostrar email real, usar únicamente las direcciones de prueba indicadas por el equipo.
- **Agenda y acceso:** conservar las reglas aprobadas en KB 05: profesional y sillón sin solapamientos, intervalo global configurable sin minutos inventados, bloqueos por recepción rechazados si afectan turnos existentes, lectura clínica para recepción y edición solo para odontólogos.
- **Paciente y comunicaciones:** conservar cancelación por enlace hasta 48 horas antes, email de creación con enlace y recordatorio 24 horas antes sin cancelación vencida. La cuenta gestiona la inscripción propia en lista; el aviso de hueco no reserva ni adjudica automáticamente y recepción asigna.
- **Pendientes:** NUNCA resolver una PA material por inferencia → consultar al usuario mediante el menú y actualizar las fuentes cuando confirme. Mantener presupuesto cero y proveedor de email sin elegir hasta acordarlo.

## Flujo de Trabajo

1. Leer la KB relevante y las decisiones vigentes.
2. Localizar el item en CHANGES y comprobar dependencias y preguntas bloqueantes, especialmente PA-01 a PA-06 y PA-15 a PA-21 cuando afecten al cambio.
3. Acordar alcance, aceptación y workflow de entrega. La política visual está diferida: resolverla antes de Apply visual y respetar las aprobaciones que correspondan.
4. Implementar solo el alcance autorizado; aplicar las reglas confirmadas y verificar con evidencia apropiada.
5. Registrar resultados y actualizar el estado del item. Crear commits atómicos cuando corresponda; solicitar un pedido explícito antes de push.
6. En el change académico elegido, cumplir el ciclo OpenSpec y verificar/archivar mediante el CLI. La revisión personal de cinco fuentes y demás pendientes humanos no se declaran resueltos por pruebas verdes.
<!-- /active-stack:agent-instruction -->
