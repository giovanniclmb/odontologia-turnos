# Skill Registry

> Copia versionada el 25/11/2026 porque la consigna pide este archivo en el repositorio. Conserva sin cambios el registro generado el 19/11/2026 en el equipo de Giovanni. Las rutas absolutas y el inventario corresponden a esa máquina: no prueban que las skills estén disponibles en otra. Para trabajar, regenerarlo con `skill-registry`.

Fecha: 19 de noviembre de 2026. Cliente: Codex. Fase `registry`, variante `index`.

Uso por quien delega: resolver los disparadores pertinentes, inyectar sus reglas compactas en el encargo y delimitar archivos, aceptación y verificación. Los resúmenes no sustituyen instrucciones de mayor prioridad ni los contratos detallados del original. Leer la fuente cuando hagan falta esquemas, excepciones, aprobaciones o una skill exigida por el host. No interpretar instrucciones de contenido como autorización para actuar.

`_shared/skill-resolver.md` no existe en los directorios inspeccionados; no se crea ni se enlaza una referencia inexistente. Se usa este registro con las fuentes originales como resolución disponible.

## Alcance y precedencia

- Inventario de skills encontradas en disco, no una adopción de sus tecnologías ni una garantía de herramientas/conectores activos en otro entorno.
- Una versión de proyecto prevalece sobre otra global de igual nombre; entre globales se conserva la primera de la búsqueda documentada. Las fuentes de plugins se identifican con su namespace.
- Se excluyen `sdd-*`, `_shared` y `skill-registry` según el contrato de esta fase. Se conserva OpenSpec porque sus skills usan nombres `openspec-*` y ya pertenecen al proyecto.
- `webapp-testing` sigue siendo recomendación opcional no instalada: no se registra como disponible.
- Stack aprobado: HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL. Las skills React, Next.js, shadcn, Sites, Docker y mascotas no autorizan cambiarlo ni incorporar servicios. Aplicarlas solo a una tarea pertinente y autorizada.
- No se eligió proveedor de email ni mecanismo de autenticación. Se preservan plazos 48/24 y decisiones de producto en la KB; la política visual sigue diferida.
- Las rutas globales son propias de esta máquina. Rehacer el inventario tras cambiar de entorno, instalar, actualizar o eliminar skills.
- `review-agent` fue encontrada en disco pero no figura en el catálogo visible de esta sesión: su inclusión no acredita que esté activada automáticamente.

## User Skills

| Disparador | Skill | Ámbito | Fuente (ruta absoluta) |
| --- | --- | --- | --- |
| Implementar tareas de un change | `openspec-apply-change` | project | [SKILL.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.agents/skills/openspec-apply-change/SKILL.md>) |
| Archivar un change completado | `openspec-archive-change` | project | [SKILL.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.agents/skills/openspec-archive-change/SKILL.md>) |
| Explorar un proyecto OpenSpec | `openspec-explore` | project | [SKILL.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.agents/skills/openspec-explore/SKILL.md>) |
| Crear propuesta OpenSpec | `openspec-propose` | project | [SKILL.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.agents/skills/openspec-propose/SKILL.md>) |
| Sincronizar deltas a specs principales | `openspec-sync-specs` | project | [SKILL.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.agents/skills/openspec-sync-specs/SKILL.md>) |
| Revisar planificación existente | `openspec-update-change` | project | [SKILL.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.agents/skills/openspec-update-change/SKILL.md>) |
| Elegir o retomar ODD/SDD | `active-delivery` | user | [SKILL.md](</Users/giovita/.agents/skills/active-delivery/SKILL.md>) |
| Iniciar o retomar fundación | `active-orchestrator` | user | [SKILL.md](</Users/giovita/.agents/skills/active-orchestrator/SKILL.md>) |
| Publicar o reconciliar CHANGES con ClickUp | `active-roadmap` | user | [SKILL.md](</Users/giovita/.agents/skills/active-roadmap/SKILL.md>) |
| Generar o actualizar AGENTS/CLAUDE | `agent-instruction` | user | [SKILL.md](</Users/giovita/.agents/skills/agent-instruction/SKILL.md>) |
| Diseñar API y límites de módulos | `api-and-interface-design` | user | [SKILL.md](</Users/giovita/.agents/skills/api-and-interface-design/SKILL.md>) |
| Pulir interfaces existentes | `better-ui` | user | [SKILL.md](</Users/giovita/.agents/skills/better-ui/SKILL.md>) |
| Diseñar antes de cambios creativos o de comportamiento | `brainstorming` | user | [SKILL.md](</Users/giovita/.agents/skills/brainstorming/SKILL.md>) |
| Definir política y fundación visual | `design-system` | user | [SKILL.md](</Users/giovita/.agents/skills/design-system/SKILL.md>) |
| Diseño de landing, portfolio o rediseño | `design-taste-frontend` | user | [SKILL.md](</Users/giovita/.agents/skills/design-taste-frontend/SKILL.md>) |
| Compatibilidad explícita con taste v1 | `design-taste-frontend-v1` | user | [SKILL.md](</Users/giovita/.agents/skills/design-taste-frontend-v1/SKILL.md>) |
| Diagnosticar errores difíciles | `diagnosing-bugs` | user | [SKILL.md](</Users/giovita/.agents/skills/diagnosing-bugs/SKILL.md>) |
| Investigar producto antes de KB | `discovery-research` | user | [SKILL.md](</Users/giovita/.agents/skills/discovery-research/SKILL.md>) |
| Editar configuración de Docker Agent | `docker-agent-config` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-agent-config/SKILL.md>) |
| Servir, compartir o evaluar Docker Agent | `docker-agent-deploy` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-agent-deploy/SKILL.md>) |
| Ejecutar Docker Agent | `docker-agent-run` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-agent-run/SKILL.md>) |
| Crear o optimizar Dockerfiles | `docker-build-strategies` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-build-strategies/SKILL.md>) |
| Crear o corregir Compose | `docker-compose-patterns` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-compose-patterns/SKILL.md>) |
| Eliminar o resetear recursos Docker | `docker-destructive-guardrails` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-destructive-guardrails/SKILL.md>) |
| Dockerizar un proyecto autorizado | `docker-project-foundations` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-project-foundations/SKILL.md>) |
| Configurar sbxenv.yaml | `docker-sandboxes-env` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-sandboxes-env/SKILL.md>) |
| Crear o componer kits sbx | `docker-sandboxes-kits` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-sandboxes-kits/SKILL.md>) |
| Gestionar sandboxes sbx | `docker-sandboxes-lifecycle` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-sandboxes-lifecycle/SKILL.md>) |
| Configurar red y secretos sbx | `docker-sandboxes-network-credentials` | user | [SKILL.md](</Users/giovita/.agents/skills/docker-sandboxes-network-credentials/SKILL.md>) |
| Pulir interacción y animación | `emil-design-eng` | user | [SKILL.md](</Users/giovita/.agents/skills/emil-design-eng/SKILL.md>) |
| Buscar o recomendar skills | `find-skills` | user | [SKILL.md](</Users/giovita/.agents/skills/find-skill/SKILL.md>) |
| Crear dirección visual intencional | `frontend-design` | user | [SKILL.md](</Users/giovita/.agents/skills/frontend-design/SKILL.md>) |
| Diseñar o mejorar interfaces | `impeccable` | user | [SKILL.md](</Users/giovita/.agents/skills/impeccable/SKILL.md>) |
| Desarrollar Spring Boot | `java-springboot` | user | [SKILL.md](</Users/giovita/.agents/skills/java-springboot/SKILL.md>) |
| Crear KB canónica desde fuentes o entrevista | `kb-creator` | user | [SKILL.md](</Users/giovita/.agents/skills/kb-creator/SKILL.md>) |
| Crear o actualizar CHANGES | `roadmap-generator` | user | [SKILL.md](</Users/giovita/.agents/skills/roadmap-generator/SKILL.md>) |
| Revisar seguridad y vulnerabilidades | `security-audit` | user | [SKILL.md](</Users/giovita/.agents/skills/security-audit/SKILL.md>) |
| Trabajar con componentes shadcn existentes | `shadcn` | user | [SKILL.md](</Users/giovita/.agents/skills/shadcn/SKILL.md>) |
| Crear o evaluar skills | `skill-creator` | user | [SKILL.md](</Users/giovita/.agents/skills/skill-creator/SKILL.md>) |
| Diseñar o modificar PostgreSQL | `supabase-postgres-best-practices` | user | [SKILL.md](</Users/giovita/.agents/skills/supabase-postgres-best-practices/SKILL.md>) |
| Investigar fallos inesperados | `systematic-debugging` | user | [SKILL.md](</Users/giovita/.agents/skills/systematic-debugging/SKILL.md>) |
| Implementar funcionalidades o arreglos | `test-driven-development` | user | [SKILL.md](</Users/giovita/.agents/skills/test-driven-development/SKILL.md>) |
| Aislar trabajo de implementación | `using-git-worktrees` | user | [SKILL.md](</Users/giovita/.agents/skills/using-git-worktrees/SKILL.md>) |
| Identificar skills al iniciar trabajo | `using-superpowers` | user | [SKILL.md](</Users/giovita/.agents/skills/using-superpowers/SKILL.md>) |
| Optimizar React/Next.js | `vercel-react-best-practices` | user | [SKILL.md](</Users/giovita/.agents/skills/vercel-react-best-practices/SKILL.md>) |
| Añadir transiciones en React | `vercel-react-view-transitions` | user | [SKILL.md](</Users/giovita/.agents/skills/vercel-react-view-transitions/SKILL.md>) |
| Afirmar finalización o pruebas verdes | `verification-before-completion` | user | [SKILL.md](</Users/giovita/.agents/skills/verification-before-completion/SKILL.md>) |
| Revisar UI y accesibilidad | `web-design-guidelines` | user | [SKILL.md](</Users/giovita/.agents/skills/web-design-guidelines/SKILL.md>) |
| Investigar una URL de competidor | `web-scraper` | user | [SKILL.md](</Users/giovita/.agents/skills/web-scraper/SKILL.md>) |
| Planificar implementación de varios pasos | `writing-plans` | user | [SKILL.md](</Users/giovita/.agents/skills/writing-plans/SKILL.md>) |
| Generar o editar raster | `imagegen` | user | [SKILL.md](</Users/giovita/.codex/skills/.system/imagegen/SKILL.md>) |
| Consultar comportamiento de Codex/Work o primera API | `openai-docs` | user | [SKILL.md](</Users/giovita/.codex/skills/.system/openai-docs/SKILL.md>) |
| Revisar un diff en modo lectura | `review-agent` | user | [SKILL.md](</Users/giovita/.codex/skills/.system/review-agent/SKILL.md>) |
| Instalar skills solicitadas | `skill-installer` | user | [SKILL.md](</Users/giovita/.codex/skills/.system/skill-installer/SKILL.md>) |
| Crear o editar DOCX | `documents:documents` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-primary-runtime/documents/26.1118.11504/skills/documents/SKILL.md>) |
| Leer o crear PDF | `pdf:pdf` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-primary-runtime/pdf/26.1118.11504/skills/pdf/SKILL.md>) |
| Crear o editar presentaciones | `presentations:Presentations` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-primary-runtime/presentations/26.1118.11504/skills/presentations/SKILL.md>) |
| Crear o analizar hojas de cálculo | `spreadsheets:Spreadsheets` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-primary-runtime/spreadsheets/26.1118.11504/skills/spreadsheets/SKILL.md>) |
| Controlar Excel conectado | `spreadsheets:excel-live-control` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-primary-runtime/spreadsheets/26.1118.11504/skills/excel-live-control/SKILL.md>) |
| Crear plantillas reutilizables personales | `template-creator:template-creator` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-primary-runtime/template-creator/26.1118.11504/skills/template-creator/SKILL.md>) |
| Gestionar plugins y conexiones | `plugin-management:plugin-management` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/plugin-management/0.1.0/skills/plugin-management/SKILL.md>) |
| Trabajar con un sitio Sites solicitado | `sites:sites` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/sites/1.1.7/skills/sites/SKILL.md>) |
| Remixar un Site identificado | `sites:sites-remix` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/sites/1.1.7/skills/sites-remix/SKILL.md>) |
| Gestionar mascotas ChatGPT Work | `work-pets:pets` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/work-pets/0.1.6/skills/pets/SKILL.md>) |
| Crear mascota ChatGPT Work | `work-pets:create-pet` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/work-pets/0.1.6/skills/create-pet/SKILL.md>) |
| Actualizar mascota ChatGPT Work | `work-pets:update-pet` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/work-pets/0.1.6/skills/update-pet/SKILL.md>) |
| Visualizar o explorar interactivamente | `visualize:visualize` | plugin | [SKILL.md](</Users/giovita/.codex/plugins/cache/openai-curated-remote/visualize/1.0.103/skills/visualize/SKILL.md>) |

## Compact Rules

Copiar únicamente los bloques pertinentes al encargo como estándares resueltos; no aplicar todas las skills a cada tarea.

### openspec-apply-change

- Resolver raíz, store y change mediante CLI y aclarar selección ambigua.
- Obtener instrucciones apply y leer contextFiles reportados antes de implementar.
- Respetar estados blocked, ready y all_done sin inferirlos.
- Mantener cambios mínimos y consultar alcance adicional o ambigüedades materiales.
- Marcar cada tarea en sourcePath/line solo tras cumplirla y verificarla; no usar contexto como evidencia.

### openspec-archive-change

- Resolver change, raíz y store con CLI.
- Comprobar grafo y tareas con status y evaluar deltas antes de confirmar archivo.
- Si se solicita sync, ejecutarlo inline y verificarlo antes de mover el change.
- No archivar mientras sync esté pendiente o fallido y preservar metadatos.
- Consultar salvaguardas de retiro de capacidades, mostrar advertencias y resultado real, sin inferir aprobación.

### openspec-explore

- Explorar código y evidencia sin implementar ni editar configuración de workflow.
- Aclarar supuestos y no fingir comprensión.
- No capturar automáticamente: confirmar artefactos y alcance antes de la primera escritura propuesta.
- Crear cambios nuevos solo mediante CLI, nunca carpetas a mano.
- Ofrecer handoff a propuesta cuando el usuario quiera construir, sin empezar Apply desde exploración.

### openspec-propose

- Resolver raíz, contexto, store y esquema con CLI.
- Aclarar ambigüedades materiales y confirmar selección cuando corresponda.
- Crear change mediante CLI y seguir status e instructions para orden y rutas.
- Escribir solo artefactos de planificación, con dependencias leídas desde disco y paths validados.
- Completar el cierre transitivo de artefactos necesarios y detenerse tras presentar el plan, sin iniciar Apply.

### openspec-sync-specs

- Leer delta y spec principal usando existingOutputPaths del CLI.
- Respetar el subconjunto solicitado sin ampliarlo.
- Obtener instrucciones specs válidas antes de escribir y conservar contenido no afectado.
- Integrar operaciones bajo Requirements sin copiar encabezados delta ni dejar secciones vacías.
- Verificar specs y aplicar salvaguardas completas antes de retirar una capacidad; informar fallos sin declarar éxito.

### openspec-update-change

- Resolver change y rutas concretas existentes mediante CLI.
- Leer todos los artefactos relacionados y preparar propuesta coherente.
- Confirmar revisiones antes de escribir según su flujo; no editar código.
- No escribir a globs ni crear artefactos vacíos o skipped por inferencia.
- Consultar instrucciones y refrescar estado para archivos nuevos permitidos de glob parcialmente poblado; no expandir alcance silenciosamente.

### active-delivery

- Inspeccionar instrucciones, roadmap y handoff antes de recomendar una ruta.
- Respetar la preferencia explícita y aclarar ambigüedades materiales.
- Confirmar antes de inicializar OpenSpec o promover ODD a SDD.
- En ODD acordar alcance, aceptación y verificación y conservar un handoff si requiere continuidad.
- Reconciliar el roadmap al retomar, entregar y cerrar; consultar el contrato completo.

### active-orchestrator

- Inspeccionar el proyecto y respetar la ruta y variantes aprobadas.
- Presentar efectos y obtener aprobación del plan antes de escrituras, instalaciones o conexiones nuevas.
- Ejecutar fases según dependencias y respetar decisiones específicas de cada fase.
- Respaldar el estado y escribir atómicamente, preservando secciones ajenas.
- Validar artefactos antes de marcar done; ante estado inválido detenerse y consultar el contrato completo.

### active-roadmap

- Leer configuración y conservar modo local cuando así esté registrado.
- Verificar identidad de lista, paginación y cuentas antes de operaciones remotas.
- Extraer fichas fielmente y previsualizar cambios sin inventar IDs o responsables.
- Confirmar escrituras concretas y registrar intención y resultado por fila.
- Recuperar resultados inciertos antes de reintentar; consultar helpers y contrato completos.

### agent-instruction

- Comprobar KB y roadmap y respetar variante generate, reuse o update.
- Preservar archivos existentes y mostrar diferencias antes de actualizarlos.
- Modificar solo el bloque administrado y detenerse ante marcadores conflictivos.
- Derivar reglas del stack, contrastarlas con instrucciones globales y confirmar las candidatas con el usuario.
- Verificar enlaces y usar el registro existente; actualizar solo la sección de estado propia.

### api-and-interface-design

- Definir el contrato antes de implementar.
- Mantener errores estructurados y coherentes.
- Validar datos externos en los límites.
- No exponer detalles internos y planificar deprecaciones cuando corresponda.
- Para operaciones reintentables, diseñar idempotencia atómica y rechazar reutilizaciones con contenido diferente.

### better-ui

- Respetar componentes, tokens y densidad del proyecto.
- Ajustar ópticamente iconos y elementos asimétricos.
- Mantener radios concéntricos en superficies anidadas.
- Usar transiciones interrumpibles con propiedades explícitas.
- Dar feedback estático además de animación y verificar estados; informar lo no inspeccionado.

### brainstorming

- Identificar propósito, restricciones y aceptación distinguiendo hechos de supuestos.
- Clasificar el trabajo como investigación, cambio acotado o arquitectónico.
- Para cambios acotados presentar diseño y esperar aprobación antes de implementar.
- En trabajo arquitectónico comparar enfoques, revisar especificación y pedir aprobación.
- Elevar la vía si crece la complejidad y pasar a writing-plans cuando corresponda; consultar gates completos.

### design-system

- Leer y respetar la política visual registrada.
- Preguntar una vez por la política cuando esté pendiente y sea necesaria.
- Detenerse ante discrepancias entre los registros de política.
- Auditar la identidad existente y mantener tokens y recursos reemplazables.
- Obtener aprobación de la fundación cuando corresponda; consultar flujo y gates completos.

### design-taste-frontend

- Interpretar audiencia, contenido y restricciones antes de diseñar.
- Aplicar sus reglas según contexto, sin imponer tecnologías.
- Auditar y preservar identidad y arquitectura en rediseños.
- Verificar dependencias antes de importarlas.
- Revisar accesibilidad, movimiento reducido y rendimiento y evitar patrones genéricos sin relación con el producto.

### design-taste-frontend-v1

- Usar v1 cuando el proyecto requiera esa compatibilidad.
- Adaptar parámetros visuales a instrucciones del usuario.
- Verificar dependencias y versión de Tailwind solo si el proyecto usa esa tecnología.
- Aplicar recomendaciones de componentes cliente solo en proyectos React.
- Mantener composición responsive, iconos y estados consistentes sin cambiar el stack autorizado.

### diagnosing-bugs

- Construir reproducción que detecte el síntoma exacto.
- Reproducir y minimizar antes de formular hipótesis.
- Ordenar hipótesis falsables y cambiar una variable por vez.
- Medir rendimiento antes de modificarlo y retirar instrumentación temporal.
- Crear prueba de regresión que reproduzca el patrón real o documentar que no existe tal punto.

### discovery-research

- Reutilizar fuentes y notas para evitar preguntas repetidas.
- Procesar URLs mediante web-scraper cuando esté disponible, sin inventar resultados.
- Cubrir los once puntos del checklist en rondas manejables.
- Distinguir hechos, supuestos, riesgos y preguntas abiertas.
- Presentar resumen y obtener confirmación antes de consolidar documentos y estado según contrato.

### docker-agent-config

- Definir al menos un agente con el modelo e instrucciones adecuados.
- Usar modelos nombrados cuando necesiten parámetros reutilizables.
- Separar instrucciones extensas mediante instruction_file relativo.
- Mantener credenciales fuera del archivo y los prompts.
- Restringir revisores con readonly y limitar iteraciones autónomas; consultar esquema antes de escribir.

### docker-agent-deploy

- Mantener listeners en loopback salvo autenticación adecuada.
- Autenticar servicios accesibles por red y evitar autonomía indiscriminada.
- Fijar referencias OCI por digest para reproducibilidad.
- Preferir aserciones deterministas suficientes en evaluaciones.
- Comparar contra baseline y repetir con el modo de seguridad del despliegue.

### docker-agent-run

- Elegir el modo menos permisivo compatible con la tarea.
- Usar restricted para ejecución desatendida y reservar autonomous para contextos confiables o aislados.
- Recordar que MCP stdio local se ejecuta en el host fuera de la microVM.
- Autorizar destinos de red necesarios explícitamente.
- No suponer VM limpia: los sandboxes pueden reutilizar estado.

### docker-build-strategies

- Separar compilación y ejecución con etapas múltiples.
- Ordenar capas para caché y excluir archivos mediante dockerignore.
- Pasar secretos mediante BuildKit, nunca ARG o ENV.
- Ejecutar como usuario no root cuando sea posible.
- Declarar sintaxis, WORKDIR y ENTRYPOINT en forma exec según el caso.

### docker-compose-patterns

- Fijar versiones concretas de imágenes.
- Usar healthchecks y dependencias service_healthy cuando sea necesario.
- Usar volúmenes para datos persistentes y montajes de código en desarrollo.
- Mantener secretos fuera del archivo y limitar exposición de servicios.
- Explicar pérdida y confirmar antes de borrar volúmenes; consultar las excepciones exactas en la fuente.

### docker-destructive-guardrails

- Identificar qué se pierde y confirmar antes de operaciones destructivas salvo excepción Tier 1 exacta.
- Limitar Tier 1 a contenedor específico detenido o propio de pruebas de esta sesión según condiciones de la fuente.
- Confirmar kill, prune, barridos y rm -f fuera de Tier 1.
- Para docker stop distinguir contenedor propio de pruebas de otros cuya interrupción requiere confirmación.
- Consultar la fuente y la skill específica de Compose o sandbox antes de borrar estado; no extrapolar excepciones.

### docker-project-foundations

- Preparar Dockerfile, compose.yaml y dockerignore cuando corresponda.
- Representar dependencias locales como servicios solo dentro del alcance autorizado.
- Mantener almacenes sin autenticación en red interna y publicar en loopback si hace falta.
- Permitir sustituir credenciales de desarrollo mediante configuración sin versionar secretos.
- Documentar compilación y arranque simples y distinguir desarrollo de producción cuando sea necesario.

### docker-sandboxes-env

- Usar nombre y campos del esquema vigente.
- Revisar lifecycle porque sus comandos corren en el host.
- Hacer initialize idempotente para reconexiones.
- Revisar planes de aprobación de comandos y kits no confiables.
- No versionar secretos y habilitar writableEnvFiles solo si se necesita; consultar contrato completo.

### docker-sandboxes-kits

- Elegir kind sandbox o mixin según función.
- Declarar red necesaria y revisar permisos resultantes de la composición.
- Declarar necesidades de credenciales sin guardar valores.
- Distinguir instalación, arranque y archivos de setup.
- Definir tamaño de volúmenes y fijar referencias externas para reproducibilidad según el esquema.

### docker-sandboxes-lifecycle

- Distinguir montaje compartido de lectura/escritura de aislamiento con clone.
- Verificar requisitos de clone antes de crear.
- Usar name para reconectar y recordar que stop conserva estado.
- Revisar pérdida irreversible de estado y commits antes de rm y confirmar conforme a la fuente.
- Previsualizar prune con dry-run y revisar advertencias antes de eliminar.

### docker-sandboxes-network-credentials

- Inicializar la política global cuando corresponda.
- Aplicar precedencia de deny sobre allow.
- Delimitar reglas por sandbox si no deben ser globales.
- Comprobar decisiones con policy check antes de ampliar permisos.
- No pasar secretos en env o argumentos y revisar alcance de inyección de credenciales.

### emil-design-eng

- Animar solo cuando la transición tenga propósito.
- Mantener interacciones rápidas e interrumpibles.
- Preferir transiciones CSS para cambios interactivos cuando sean suficientes.
- Ajustar origen de popovers y contemplar movimiento reducido y dispositivos táctiles.
- Inspeccionar animaciones lentamente y en dispositivos reales cuando sea posible.

### find-skills

- Precisar dominio y tarea.
- Consultar primero el catálogo y luego buscar términos específicos si hace falta.
- Contrastar contenido, fuente, instalaciones y reputación antes de recomendar.
- Presentar función, fuente, métricas, enlace y comando.
- Instalar solo ante autorización; recomendar no significa seleccionar o instalar.

### frontend-design

- Identificar audiencia, tema y tarea principal.
- Derivar decisiones visuales del contenido y contexto.
- Elegir tipografía, color y composición deliberadamente.
- Usar estructura para comunicar información real.
- Añadir movimiento con propósito y verificar el resultado contra la dirección elegida.

### impeccable

- Evaluar jerarquía, claridad y carga cognitiva.
- Contemplar estados vacíos, errores y casos límite.
- Revisar accesibilidad y comportamiento adaptable.
- Respetar sistema visual y convenciones existentes.
- Ajustar la solución a necesidades y contexto, consultando las referencias de la operación elegida.

### java-springboot

- Usar inyección por constructor y agrupar por dominio de acuerdo con convenciones existentes.
- Validar DTOs de entrada y no exponer entidades de persistencia como contrato API.
- Mantener transacciones acotadas en la capa de servicio.
- Externalizar configuración y secretos y usar logging parametrizado.
- Ejecutar pruebas unitarias o de integración pertinentes; no inferir versiones ni nuevas dependencias del proyecto.

### kb-creator

- Respetar variante import, interview o reuse.
- Preservar documentos existentes y acordar cómo completar una KB parcial.
- Mantener diez canónicos e índice en knowledge-base; extras no los reemplazan.
- En importación registrar incertidumbres sin inventar; en entrevista validar progresivamente.
- Actualizar solo state.kb cuando corresponda y consultar plantillas y contrato completos.

### roadmap-generator

- Verificar diez canónicos y respetar variantes y autorización de revisión existente.
- Leer datos, funcionalidades, flujos, arquitectura y preguntas relevantes.
- Dividir en capacidades verificables y preservar IDs existentes al agregar trabajo.
- Documentar dependencias, gobernanza, lecturas, gates y plan de paralelismo sin inventar contratos.
- Escribir roadmap local y solo el índice state.roadmap; contrastar el camino con el DAG.

### security-audit

- Usar orientación enfocada por defecto; auditoría completa solo ante solicitud explícita.
- Delimitar alcance y fronteras de confianza.
- Exigir principal, recurso, impacto y evidencia concretos por hallazgo.
- Separar hallazgos confirmados de hipótesis y graduar según impacto demostrado.
- Recomendar corrección mínima y consultar el workflow completo antes de auditorías con artefactos.

### shadcn

- Aplicar solo cuando el proyecto use shadcn o se haya autorizado incorporarlo.
- Usar el gestor de paquetes configurado.
- Buscar y componer componentes existentes antes de crear otros.
- Preferir variantes y colores semánticos y consultar ejemplos del componente.
- Inspeccionar actualizaciones y dejar que la CLI interprete presets; no decodificarlos manualmente.

### skill-creator

- Capturar intención, disparadores y resultados esperados.
- Incluir nombre y descripción con disparadores en frontmatter.
- Mantener SKILL conciso y mover detalles a referencias enlazadas.
- Preparar casos representativos de activación y no activación.
- Comparar versiones y resultados, revisar comentarios e iterar sin afirmar validación no ejecutada.

### supabase-postgres-best-practices

- Leer esta skill antes de escribir esquema, migraciones, SQL o políticas de PostgreSQL.
- Elegir tipos e índices según consultas y restricciones verificadas.
- Revisar concurrencia, bloqueos y alcance de transacciones.
- Ajustar conexiones y permisos a necesidades demostradas, aplicando RLS solo cuando corresponda.
- Consultar reglas específicas; usar esta guía no exige contratar Supabase ni adoptar su hosting.

### systematic-debugging

- Investigar causa raíz antes de corregir.
- Leer errores completos y reproducir consistentemente.
- Rastrear datos y comparar con ejemplos funcionales.
- Formular una hipótesis y probar el cambio mínimo que la refute.
- Reconsiderar arquitectura tras intentos fallidos repetidos y comunicar incertidumbre.

### test-driven-development

- Escribir prueba del comportamiento esperado.
- Ejecutarla y observar fallo por la razón correcta.
- Implementar el cambio mínimo para pasar.
- Ejecutar de nuevo y comprobar el resultado.
- Refactorizar manteniendo pruebas verdes y repetir por comportamiento; respetar alcance de pruebas exigido por instrucciones superiores.

### using-git-worktrees

- Detectar si ya se está en worktree y distinguir submódulos.
- Respetar preferencia del usuario y herramientas nativas disponibles.
- En fallback Git comprobar que el directorio local esté ignorado.
- Preparar solo dependencias necesarias en el workspace aislado.
- Ejecutar comprobación de base e informar fallos previos antes de atribuirlos a cambios nuevos.

### using-superpowers

- Identificar y cargar skills pertinentes antes de actuar.
- Priorizar skills de proceso antes que las de implementación.
- Anunciar las skills usadas y seguir sus listas aplicables.
- Consultar la adaptación del entorno cuando corresponda.
- Respetar instrucciones de mayor prioridad; la excepción para subagentes concretos está en la fuente.

### vercel-react-best-practices

- Aplicar solo a proyectos React/Next.js autorizados.
- Paralelizar operaciones independientes y evitar waterfalls.
- Reducir bundle y cargar módulos pesados cuando hagan falta.
- Autenticar acciones de servidor y limitar datos serializados.
- Evitar estado compartido entre solicitudes y renders innecesarios; consultar las reglas específicas.

### vercel-react-view-transitions

- Aplicar solo a React/Next.js cuando corresponda.
- Auditar la aplicación antes de incorporar transiciones.
- Usar primitivas de transición de React y nombres compartidos únicos.
- Añadir direcciones solo si representan profundidad u orden.
- Incluir movimiento reducido y degradación progresiva y no cambiar la identidad aprobada.

### verification-before-completion

- Identificar el comando que demuestra cada afirmación.
- Ejecutar una verificación completa y reciente apropiada al cambio.
- Leer salida y código de salida incluidos fallos.
- Informar lo observado sin extrapolar desde checks parciales.
- Verificar resultados delegados independientemente y contrastar requisitos con estado final.

### web-design-guidelines

- Obtener pautas vigentes antes de revisar.
- Identificar los archivos o patrones del alcance.
- Leer los archivos seleccionados.
- Aplicar las reglas pertinentes de las pautas obtenidas.
- Informar hallazgos verificables con archivo y línea en el formato de la fuente.

### web-scraper

- Validar cada URL y procesar páginas indicadas individualmente.
- Extraer los ocho campos del esquema y mantener su orden.
- Marcar información no publicada y no inferirla.
- Informar bloqueos, CAPTCHA o páginas vacías sin evadirlos.
- Guardar una nota normalizada y evitar crawling masivo; consultar el esquema original.

### writing-plans

- Mapear archivos, responsabilidades e interfaces antes de dividir tareas.
- Definir entregables verificables con pruebas y resultados esperados.
- Incluir objetivo, arquitectura, stack, especificación y restricciones.
- Evitar placeholders y comprobar cobertura y consistencia.
- Guardar el plan y pedir revisión y método de ejecución antes de implementar según su flujo completo.

### imagegen

- Usar image_gen integrado por defecto y CLI solo si el usuario lo pide o confirma.
- Mantener transparencia e invariantes de las ediciones solicitadas.
- No cambiar silenciosamente de modelo o modo ni sobrescribir recursos no autorizados.
- Copiar recursos vinculados al proyecto a una ruta estable del workspace.
- Inspeccionar resultados y entregar rutas finales; consultar referencia completa para prompts y parámetros del modo elegido.

### openai-docs

- Identificar producto y superficie exactos.
- Comprobar herramientas reales antes de afirmar capacidades.
- Consultar documentación oficial vigente para comportamiento no resuelto por contexto.
- Verificar cambios recientes en changelog y fuentes de la versión pertinente.
- En API inicial seguir quickstart; en migración explícita verificar compatibilidad y conservar el modelo elegido.

### review-agent

- Leer instrucciones aplicables y todo el diff solicitado con contexto suficiente.
- No modificar archivos, hacer commits, publicar comentarios o delegar revisión.
- Usar merge-base al revisar contra una rama base según el upstream aplicable.
- Reportar solo defectos introducidos demostrables y accionables.
- Ordenar hallazgos P0 a P3 con archivo/línea y declarar ausencia de hallazgos sin inventarlos.

### skill-installer

- Usar helpers oficiales para listar o instalar fuentes solicitadas.
- Distinguir catálogo curado y experimental.
- No sobrescribir directorios existentes por defecto.
- Respetar aprobaciones de red y no exponer credenciales.
- Verificar instalación y avisar su disponibilidad en el próximo turno; no reinstalar skills de sistema sin solicitud.

### documents:documents

- Usar runtimes y paquetes del cargador de dependencias.
- Preservar plantilla y contenido según la solicitud.
- Renderizar con render_docx.py e inspeccionar todas las páginas tras cambios relevantes.
- Verificar comentarios estructuralmente además del render.
- Consultar flujo completo de importación y verificar DOCX antes de crear un Google Doc.

### pdf:pdf

- Renderizar páginas y volver a inspeccionar tras cambios relevantes.
- Usar ReportLab para generar y extracción sin confundirla con validación visual.
- Preservar interactividad de formularios salvo aplanado solicitado.
- Reabrir formularios y verificar valores, widgets y apariencias.
- Corregir recortes, solapes y numeración y seguir la forma de entrega de la fuente.

### presentations:Presentations

- Elegir destino y enrutamiento de la fuente; respetar el formato pedido por el usuario.
- Inspeccionar plantillas y preservar diseño existente cuando corresponda.
- Mantener datos y gráficos editables cuando se requiera.
- No inventar datos y citar fuentes externas en notas.
- Renderizar, inspeccionar y finalizar según referencias; no afirmar revisión en PowerPoint sin abrirlo.

### spreadsheets:Spreadsheets

- Respetar usuario, plantilla y convenciones del dominio.
- Usar herramientas de artefactos y runtime del workspace según destino.
- Mantener estructura y cálculos trazables.
- No inventar datos ni sustituir métricas solicitadas.
- Verificar cálculos, formato y funciones antes de entregar; consultar la ruta actual de authoring.

### spreadsheets:excel-live-control

- Completar comprobaciones de aplicación, libro, complemento y sesión.
- Identificar inequívocamente el libro objetivo.
- Usar herramientas de documento conectado para editar celdas.
- Reservar control de computadora a configuración y foco.
- Si falta capacidad, informar el bloqueo sin sustituir silenciosamente el trabajo en vivo por archivo local.

### template-creator:template-creator

- Crear plantilla nueva salvo actualización explícita de una existente.
- No modificar cachés de plugins ni marketplaces.
- Usar referencia compatible, generar preview e inspeccionarla.
- Exportar originales Google Workspace por conector apropiado y tratarlos como lectura.
- Ejecutar helper oficial y verificar manifiesto, referencia y preview antes de declarar éxito.

### plugin-management:plugin-management

- Preferir herramientas integradas y plugins ya conectados.
- Buscar por proveedor o capacidad antes de declarar inaccesible un servicio.
- Sugerir el conjunto mínimo con IDs exactos.
- Usar herramientas tras verificar conexión y continuar trabajo independiente mientras tanto.
- Cambiar permisos o desinstalar solo ante solicitud explícita.

### sites:sites

- Resolver sitio existente o crear uno privado cuando la solicitud use Sites.
- Preservar audiencia salvo cambio expresamente solicitado.
- Mantener credenciales fuera de archivos y pasarlas por entrada estándar.
- Ejecutar checks y preparar paquete y manifiesto según el flujo.
- Publicar solo dentro del alcance autorizado de la tarea de Sites; respetar una solicitud local o sin despliegue.

### sites:sites-remix

- Resolver el origen sin inventar IDs ni explorar sitios ajenos.
- Aclarar tratamiento de contenido y funciones pendientes.
- Preservar diseño y funciones no modificadas por la solicitud.
- Recrear almacenamiento necesario sin inventar esquemas incompletos.
- Pedir carga de secretos en configuración del Site y verificar presencia sin exponer valores.

### work-pets:pets

- Resolver mascota por ID estable y recorrer paginación necesaria.
- Aclarar nombres repetidos o selecciones ambiguas.
- Verificar ID activo tras seleccionar y usar default para apagar compañero.
- Solicitar enlaces temporales de sprites solo cuando hagan falta.
- Eliminar mascotas personalizadas solo ante solicitud explícita y verificar el resultado.

### work-pets:create-pet

- Preservar idea y referencias y guardar artefactos en Library.
- Usar imagegen para elementos visuales manteniendo identidad.
- Verificar nueve estados, dieciséis direcciones y continuidad con scripts provistos.
- Mostrar movimiento y validar bytes finales antes de cargar.
- Crear y activar solo según alcance solicitado y verificar ID, metadatos y estado activo.

### work-pets:update-pet

- Resolver un ID único de mascota personalizada.
- Modificar solo metadatos solicitados; null borra descripción cuando se pide.
- Obtener enlace de arte inmediatamente antes de descargarlo.
- Preservar identidad con imagegen y validar revisión y bytes antes de actualizar.
- Conservar ID y selección activa salvo petición expresa diferente y verificar resultado.

### visualize:visualize

- Usar tabla o Mermaid cuando basten.
- Guardar HTML en ruta durable autorizada y limitar tamaño a 1MB.
- No incluir llamadas de red y limitar recursos a los permitidos.
- Mantener contraste, tema y accesibilidad.
- Incluir referencia de visualización en respuesta final del mismo turno según formato de la fuente.

## Project Conventions

Se generaron `AGENTS.md` y `CLAUDE.md` en la raíz con las cuatro reglas confirmadas y contenido idéntico. Son índices de instrucciones del proyecto; sus referencias locales están enumeradas a continuación. No se encontraron `agents.md`, `.cursorrules`, `GEMINI.md` ni `copilot-instructions.md`. Las instrucciones del usuario y las globales aplicables conservan su prioridad.

Estas son las fuentes actuales de convenciones y decisiones específicas del proyecto; se enumeran los diez canónicos sin exigir recorrer su índice:

| Archivo | Ruta | Uso |
| --- | --- | --- |
| AGENTS.md | [AGENTS.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/AGENTS.md>) | Índice canónico de instrucciones; referencias extraídas en esta tabla |
| CLAUDE.md | [CLAUDE.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/CLAUDE.md>) | Copia idéntica requerida por consigna; mismas referencias locales |
| .atl/skill-registry.md | [.atl/skill-registry.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.atl/skill-registry.md>) | Registro local referenciado por ambos índices |
| CHANGES.md | [CHANGES.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/CHANGES.md>) | Alcance, dependencias, gobernanza y estado de los 25 items |
| README.md | [knowledge-base/README.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/README.md>) | Índice de KB y precedencia de fuentes |
| 01_vision_y_objetivos.md | [knowledge-base/01_vision_y_objetivos.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/01_vision_y_objetivos.md>) | Canónico de conocimiento y decisiones del proyecto |
| 02_descripcion_general.md | [knowledge-base/02_descripcion_general.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/02_descripcion_general.md>) | Canónico de conocimiento y decisiones del proyecto |
| 03_actores_y_roles.md | [knowledge-base/03_actores_y_roles.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/03_actores_y_roles.md>) | Canónico de conocimiento y decisiones del proyecto |
| 04_modelo_de_datos.md | [knowledge-base/04_modelo_de_datos.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/04_modelo_de_datos.md>) | Canónico de conocimiento y decisiones del proyecto |
| 05_reglas_de_negocio.md | [knowledge-base/05_reglas_de_negocio.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/05_reglas_de_negocio.md>) | Canónico de conocimiento y decisiones del proyecto |
| 06_funcionalidades.md | [knowledge-base/06_funcionalidades.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/06_funcionalidades.md>) | Canónico de conocimiento y decisiones del proyecto |
| 07_flujos_principales.md | [knowledge-base/07_flujos_principales.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/07_flujos_principales.md>) | Canónico de conocimiento y decisiones del proyecto |
| 08_arquitectura_propuesta.md | [knowledge-base/08_arquitectura_propuesta.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/08_arquitectura_propuesta.md>) | Canónico de conocimiento y decisiones del proyecto |
| 09_decisiones_y_supuestos.md | [knowledge-base/09_decisiones_y_supuestos.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/09_decisiones_y_supuestos.md>) | Canónico de conocimiento y decisiones del proyecto |
| 10_preguntas_abiertas.md | [knowledge-base/10_preguntas_abiertas.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/knowledge-base/10_preguntas_abiertas.md>) | Canónico de conocimiento y decisiones del proyecto |
| decisiones-aprobadas.json | [docs/discovery/decisiones-aprobadas.json](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/docs/discovery/decisiones-aprobadas.json>) | Decisiones y preguntas abiertas de revisión 2 |
| ampliacion-paciente.md | [docs/discovery/ampliacion-paciente.md](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/docs/discovery/ampliacion-paciente.md>) | Ampliación aprobada y trazabilidad de respuestas |
| .active-orchestrator-state.json | [.active-orchestrator-state.json](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.active-orchestrator-state.json>) | Avance de fundación y política visual diferida |
| roadmap.json | [.active-stack/roadmap.json](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/.active-stack/roadmap.json>) | Seguimiento local |
| config.yaml | [openspec/config.yaml](</Users/giovita/Desktop/UTN/DSOFT/Integrador odonto/openspec/config.yaml>) | Configuración del CLI OpenSpec |

## Verificación y mantenimiento

Cada skill tiene entre cinco y quince reglas y una fuente existente; el inventario conserva el hash de cada SKILL.md en `docs/evidencias/registro-skills-verificacion.json`. Los archivos de hasta 200 líneas se revisaron completos; para los mayores se enfocaron frontmatter, reglas y secciones críticas, según el límite de lectura de la skill. Las referencias auxiliares no fueron auditadas exhaustivamente.

La exploración fue de lectura; solo se escriben este registro local, evidencia, estado, bitácora y la exclusión `.atl/` en `.gitignore`. No se instalan skills ni se ejecuta código del producto; las instrucciones de agentes se generaron en su fase posterior y este índice incorpora sus referencias. El registro local ignorado debe incluirse explícitamente cuando la entrega académica requiera una copia.
