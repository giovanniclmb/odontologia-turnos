# Bitácora de fundación

## Paso 1 Inicialización de OpenSpec

Fecha: 17 de noviembre de 2026.

El usuario aprobó el plan de fundación y autorizó ejecutar únicamente el paso 1 en esta sesión: «Apruebo, vamos con el paso 1».

Se ejecutó `openspec init --tools codex --scope project --language Spanish --no-animation`. El primer intento creó la estructura pero no pudo descargar las habilidades por la restricción de red del entorno. El reintento con acceso autorizado a la red terminó con código 0. Se preservó la configuración generada en el primer intento.

OpenSpec creó su estructura y seis habilidades locales: openspec-explore, openspec-propose, openspec-apply-change, openspec-update-change, openspec-sync-specs y openspec-archive-change. El destino registrado es Codex. La configuración establece artefactos en español y conserva encabezados estructurales y palabras normativas de OpenSpec en inglés.

Verificación: `openspec list --json` reconoce la raíz del proyecto y devuelve cero changes. Se comprobó la existencia de las seis habilidades, specs y changes/archive. La inicialización está registrada como completada en `.active-orchestrator-state.json`; la siguiente fase es Discovery y permanece pendiente.

Evidencia: `docs/evidencias/openspec-init.log`, `docs/evidencias/openspec-init-retry.log` y `docs/evidencias/openspec-list-inicial.json`.

El perfil instalado no incluye una habilidad de verify independiente. La consigna admite ejecutar la suite y documentar la verificación cuando no existe ese comando; se resolverá al llegar al change, sin ampliar ahora la instalación.

Inspección final: pendiente de la decisión del usuario entre Continuar, Ajustar o Parar acá. No se inició Discovery ni se eligió el stack de la aplicación.

## Paso 2 Investigación previa a la entrevista

Fecha: 17 de noviembre de 2026. El usuario autorizó Discovery y pidió investigar antes de presentar las preguntas mediante menú, sin adoptar decisiones por él. Esa instrucción también confirmó Continuar tras la inicialización.

Se generaron 15 fichas, una comparación de 15 sistemas y 38 referencias oficiales, un informe Markdown y PDF de 25 páginas y una matriz provisional de cobertura de evidencia. Se distingue declaración comercial de documentación pública y se registraron precios cambiantes, módulos futuros y fuentes bloqueadas. El PDF fue renderizado y revisado; se comprobaron 525 celdas de criterios y los valores ponderados. Evidencia de verificación en docs/evidencias/discovery-verificacion.json.

Discovery sigue en progreso: la entrevista de los once puntos, la selección de cinco competidores y tres referencias UX, el MVP y la aprobación final están pendientes. El usuario no eligió stack, integraciones ni reglas. Las comprobaciones del agente no sustituyen la revisión personal de al menos cinco fuentes exigida a los integrantes. No se generó discovery.md definitivo ni se avanzó a KB.

## Aprobación y consolidación de Discovery

Fecha: 17 de noviembre de 2026. Tras cuatro rondas de preguntas por menú, el usuario revisó el resumen de once puntos y respondió: «Continuar: apruebo el resumen y generar los documentos».

Se generaron docs/discovery/discovery.md y decisiones-aprobadas.json, y se actualizó la sección D del informe Markdown/PDF con el MVP, las exclusiones, cinco competidores y tres referencias UX elegidos por el usuario. El PDF consolidado tiene 26 páginas renderizadas y revisadas. Se preservaron las 15 fichas, 38 referencias y matrices de investigación. El respaldo anterior está en docs/evidencias/discovery-antes-consolidacion.

La sección discovery del estado registra las respuestas aprobadas. La fase documental está completada; la revisión personal de cinco fuentes sigue expresamente pendiente. El intervalo global no recibió un valor inventado. Cantidad de recursos, estados, campos, versiones y reparto de trabajo permanecen como preguntas abiertas. KB, roadmap e implementación no se iniciaron.

## Generación de la base de conocimiento

Fecha: 17 de noviembre de 2026. El usuario pidió continuar con el siguiente paso después del Discovery aprobado. Se ejecutó kb-creator en modo importación, usando el checklist, decisiones y relevamiento como fuentes.

Se generaron los diez archivos canónicos y README en knowledge-base/. Se conservaron las reglas sobre profesional/sillón, intervalo global sin minutos fijados, duración ajustable al crear, reprogramación, cancelación y separación de permisos clínicos. Se incorporaron las referencias competitivas elegidas y las funciones excluidas del MVP. El modelo de datos es conceptual; esquema físico, autenticación, contratos y despliegue permanecen pendientes.

Verificación integrada: 11 archivos, 73 enlaces locales válidos, 11 códigos de reglas, 8 historias y 14 preguntas abiertas, sin duplicados o referencias inexistentes. Evidencia en docs/evidencias/kb-verificacion.json. El estado registra kb.source=ingest y preserva sin cambios la sección discovery aprobada. La revisión personal de cinco fuentes sigue pendiente. No se crearon tablas, código de aplicación, CHANGES.md ni diseño visual.

Inspección de la KB: pendiente de Continuar / Ajustar / Parar acá por el usuario. La siguiente fase del plan es roadmap; todavía no se ejecutó.

## Generación del roadmap local

Fecha: 17 de noviembre de 2026. El usuario indicó «Vamos con el siguiente paso», confirmando continuar tras la KB. Se ejecutó roadmap-generator en variante local-markdown y se generó CHANGES.md.

El roadmap organiza 14 changes en cinco fases, ocho gates y una cadena de precedencia representativa de ocho items; la distribución orientativa usa tres carriles en nueve pasos. Distingue el backlog completo de la entrega de un único change académico. C-01 propone lógica aislada de creación de turnos con scaffolding de pruebas mínimo; es candidato, todavía no elegido por el usuario. El guardado concurrente en PostgreSQL pertenece a C-06 y no se atribuye a las pruebas aisladas de C-01.

Se verificaron IDs, cinco campos por ficha, dependencias sin ciclos, concordancia de tabla y fichas, cronograma, referencias a KB y cobertura de historias. El control de fuentes detectó dos precisiones que se corrigieron: cobertura parcial de US-004 en C-01 y vía autorizada de configuración del intervalo en C-05/C-10. Ambas quedaron verificadas. Se preservaron valores, contratos y permisos pendientes. Evidencia: docs/evidencias/roadmap-verificacion.json.

OpenSpec sigue sin changes creados. No se implementó código, no se publicaron tareas ni se asignaron personas. El estado registra un índice thin de roadmap y mantiene Discovery/KB sin cambios. La fase siguiente del plan es seguimiento local y permanece pendiente; después corresponden recomendaciones de skills, registro y reglas del agente. Inspección del roadmap: pendiente de Continuar / Ajustar / Parar acá.

## Ampliación de alcance con paciente y email

Fecha: 17 de noviembre de 2026. El usuario pidió incorporar al paciente, recordatorios por email, cancelación por enlace, lista de espera con aviso automático y agenda de sillón diaria/semanal con bloqueos. Respondió por menú: enlace para cancelar y cuenta para lista de espera, recordatorio 24 horas antes, cancelación hasta 24 horas antes, recepción confirma la asignación del hueco y envíos reales a direcciones de prueba del equipo con opción gratuita por definir.

Se registraron requisitos y respuestas en docs/discovery/ampliacion-paciente.md. Se consultó el cruce entre ambos plazos de 24 horas, las reglas de bloqueos y las preferencias para identificar interesados. Estas tres aclaraciones permanecen pendientes; no se eligieron respuestas por inferencia.

Se preservaron respaldos del estado y los puntos de entrada en docs/evidencias/antes-ampliacion-paciente/. Los documentos anteriores y sus verificaciones conservan valor histórico; se señalaron como pendientes de consolidación para esta ampliación. El PDF existente corresponde al alcance anterior y aún no fue regenerado. No se inició implementación ni se conectó un proveedor o enviaron emails.

### Respuestas 6–8 de la ampliación

El usuario cambió expresamente el límite de cancelación por enlace a 48 horas antes y mantuvo el recordatorio 24 horas antes. Definió bloqueos por recepción de sillón y/o profesional por período, rechazados si afectan turnos existentes. La lista de espera registra prestación, rango de fechas y franjas horarias, con profesional opcional. Se actualizaron la ampliación y su seguimiento, preservando las respuestas anteriores como historia.

Se consultó cómo entregar el enlace antes de que venza, ya que el recordatorio llega después del cierre de cancelación. Ninguna opción fue adoptada por el agente. La consolidación de Discovery, KB, roadmap y PDF permanece pendiente.

### Resolución de entrega del enlace

El usuario eligió email al crear el turno con enlace y recordatorio 24 horas antes sin opción de cancelar cuando el plazo venció. Se mantienen las 48 horas de anticipación mínima para cancelación. Se incorporó la decisión y se preparó el resumen de once puntos para aprobación final de la ampliación. Las nueve respuestas están registradas y no quedan aclaraciones de esta ronda pendientes; permanece la aprobación del resumen antes de consolidar.

## Cierre de la ampliación aprobada

Fecha de consolidación: 19 de noviembre de 2026. Se retomó el trabajo interrumpido tras la aprobación explícita «Apruebo el resumen y actualizar los documentos». Se conservaron los respaldos y se completaron los documentos iniciados, sin repetir la entrevista ni adoptar reglas técnicas pendientes. Los subagentes de apoyo se detuvieron por límites de uso; la sesión principal completó e integró el trabajo restante.

Discovery y decisiones revisión 2 incorporan paciente, cancelación por enlace hasta 48 horas, email al crear con enlace y recordatorio 24 horas antes sin opción vencida, cuenta para lista con prestación/fechas/franjas y profesional opcional, aviso automático con asignación por recepción, agenda diaria/semanal y bloqueos por recepción rechazados si afectan turnos existentes. Confirmaciones manuales y permisos clínicos se conservan. Proveedor gratuito, cuentas, enlaces, bordes temporales y otras decisiones siguen abiertos.

Se actualizaron los diez canónicos e índice de KB: 21 reglas, 15 historias y 21 preguntas; se conservaron todos los IDs anteriores. El roadmap conserva C-01 a C-14 y agrega C-15 a C-25, todos pendientes: seis fases, once gates, cadena de precedencia de once items y cronograma orientativo de tres carriles en catorce pasos. Esto no constituye selección del change académico ni compromiso de implementar el backlog completo para la entrega.

Verificación: 93 enlaces locales de KB y 106 del roadmap resueltos; IDs únicos y referencias válidas; dependencias sin ciclos y cronograma con precedencias estrictas; cobertura de US-001 a US-015. El generador del informe conserva la ampliación y reproduce el Markdown. Se verificaron 15 productos, 38 fuentes, 525 criterios y 120 valores de puntuación. PDF actualizado de 28 páginas renderizadas y revisadas en siete hojas de contacto, sin recortes o superposiciones visibles. Evidencias actualizadas en discovery-verificacion.json, kb-verificacion.json, roadmap-verificacion.json y pdf-paciente-verificacion.json bajo docs/evidencias/.

Se sincronizó el estado con la revisión 2; la siguiente fase del plan sigue siendo seguimiento local. La validación personal de cinco fuentes permanece pendiente. No se implementó producto, no se eligió proveedor ni se enviaron emails.

## Seguimiento local del roadmap

Fecha: 19 de noviembre de 2026. El usuario pidió continuar con el siguiente paso. Se ejecutó la variante local ya prevista en el plan aprobado mediante active-orchestrator, creando .active-stack/roadmap.json con mode=local. CHANGES.md conserva el alcance, las dependencias y los estados operativos de los 25 items, todos pendientes.

Se verificó que el roadmap coincide con el hash de la consolidación revisión2 y con los IDs del estado. Se validó la configuración JSON y se conservó intacto CHANGES.md. Evidencia: docs/evidencias/seguimiento-local-verificacion.json; respaldo del estado previo en docs/evidencias/antes-seguimiento-local/. No se publicaron tareas, asignaron personas, instalaron herramientas ni inició implementación. La siguiente fase es recomendación de skills, todavía pendiente.

## Recomendación de skills

Fecha: 19 de noviembre de 2026. El usuario pidió continuar. Se ejecutó find-skills en modo recommend-only, reutilizando el stack y alcance aprobados. Se consultó el catálogo skills.sh y se verificaron repositorios, métricas y orígenes locales. Se recomiendan seis skills ya instaladas: java-springboot (github/awesome-copilot), supabase-postgres-best-practices, api-and-interface-design, test-driven-development, security-audit y web-design-guidelines. webapp-testing de anthropics/skills queda opcional para evaluar con una UI ejecutable, sin instalar.

Se generó docs/recomendaciones-skills.md con tabla, fuentes, métricas, encaje y límites; evidencia en docs/evidencias/recomendacion-skills-verificacion.json. El estado preserva instalaciones existentes y registra las recomendaciones. No se ejecutó npx ni se actualizaron skills o configuración global. No se cambió proveedor, stack, política visual, KB o roadmap. La siguiente fase es registry y permanece pendiente.

## Registro de skills del proyecto

Fecha: 19 de noviembre de 2026. El usuario pidió continuar. Se ejecutó skill-registry en variante index. Se inspeccionaron las ubicaciones globales conocidas, las skills de sistema Codex, las seis OpenSpec locales y las versiones activas de plugins. Se deduplicó por nombre dando prioridad al proyecto; se excluyeron sdd-*, _shared y la propia skill-registry.

Se generó .atl/skill-registry.md con 67 entradas: seis de proyecto, 48 globales/sistema y trece de plugins; cada una tiene cinco reglas compactas (335 en total). Se verificaron fuentes existentes, hashes y 17 fuentes de convenciones. review-agent solo está detectada en disco y no se afirma activación automática. La candidata webapp-testing no fue agregada porque no está instalada. Las rutas globales son específicas de esta máquina.

No se encontraron archivos AGENTS/CLAUDE ni el protocolo opcional _shared/skill-resolver.md; se registra esa ausencia sin crear enlaces rotos. Se agregaron .atl/ a .gitignore y evidencias en docs/evidencias/registro-skills-verificacion.json, con respaldo previo en antes-registro-skills/. El registro se conserva localmente y deberá incluirse explícitamente cuando se prepare la entrega académica. No se instalaron skills ni se adoptaron tecnologías del catálogo. La siguiente fase es generar instrucciones de agentes con las confirmaciones de reglas duras que correspondan.

## Generación de instrucciones y cierre documental de fundación

Fecha: 19 de noviembre de 2026. El usuario aprobó RA-01 (TypeScript strict/sin any explícito), RA-02 (constructor, validación y DTOs separados si se usa ORM) y RA-03 (TDD de reglas y PostgreSQL real para persistencia/concurrencia). Ajustó RA-04: compilar, probar y crear commits autónomamente dentro del alcance; commits atómicos por tarea, con mensajes específicos y detallados; push solo por pedido explícito.

Se generaron AGENTS.md y CLAUDE.md en raíz con contenido idéntico por requisito académico. Se mantienen marcadores administrados, stack exacto de KB02, diez enlaces canónicos, mapeo de skills reales por roles y resumen de 25 items en seis fases. Se conserva la gobernanza global sin duplicarla ni atribuirle políticas ausentes. C-01 sigue siendo candidato no seleccionado.

Se validaron seis secciones, cuatro reglas, enlaces locales, igualdad de contenido y fuentes sin cambios. La KB, el roadmap y las instrucciones globales permanecen intactos. Se actualizó el registro local para incluir los nuevos índices y sus referencias, sin instalar skills. Evidencia en docs/evidencias/instrucciones-agentes-verificacion.json y respaldo previo en antes-generar-instrucciones/.

Las siete fases seleccionadas de fundación documental quedan completadas. Diseño visual sigue diferido. La revisión personal de fuentes, datos del equipo, preguntas abiertas y selección del change siguen pendientes; no se afirma finalización de la entrega académica ni implementación del MVP. No se creó un change ni se hizo push.

## Vinculación del repositorio remoto

Fecha: 19 de noviembre de 2026. El usuario creó https://github.com/giovanniclmb/odontologia-turnos. Se verificó mediante el conector GitHub que es público y declara main como rama predeterminada. Se vinculó como origin y se renombró la rama local inicial de master a main. La escritura de .git/config requirió ejecución autorizada fuera de la restricción de sandbox; se verificó el resultado.

Evidencia: docs/evidencias/repositorio-vinculado.json. Todavía no hay commits locales y no se hizo push. La vinculación no selecciona un change ni inicia implementación.

## Preparación del versionado inicial

Fecha: 19 de noviembre de 2026. El usuario autorizó continuar con README, selección de archivos y commits atómicos locales. Se creó README.md con alcance, estado real, stack pendiente de versiones, navegación documental y condiciones para trabajar desde otro equipo. No se inventaron comandos de arranque o tests de una aplicación todavía no implementada. Se documentaron las dependencias locales de los generadores de Discovery.

Se conservaron documentos vigentes, evidencias finales, logs citados y utilidades, sin detectar patrones de credenciales en las búsquedas de texto realizadas. Se excluyeron de Git respaldos intermedios, cachés y salidas de build, conservándolos en disco. .atl/ permanece local según la política vigente; la evidencia del inventario y los pasos para regenerarlo o incluirlo en la entrega están documentados. No se eligió una licencia ni se añadieron CI, estructura de aplicación o dependencias.

Se normalizaron 33 espacios finales de CHANGES.md sin modificar contenido o dependencias; se actualizó su hash de verificación. El versionado se divide por tareas: investigación y Discovery, KB y roadmap, y herramientas/instrucciones de trabajo con README. El resultado de los commits se comprueba en el historial Git; no se hace push. Evidencia previa al versionado: docs/evidencias/preparacion-repositorio-verificacion.json.

## Adaptación de la recomendación docente al equipo de cuatro

Fecha: 19 de noviembre de 2026. Giovanni pidió incorporar al planning las recomendaciones del profesor para otro equipo. Se documentaron el equipo nominal, su responsabilidad sobre la base y lógica central de agenda, la preparación de contratos y las medidas propuestas de revisión, archivos compartidos y entornos reproducibles en docs/plan-colaboracion.md. El usuario eligió en el menú: propuestas y contratos en paralelo; implementación después de integrar dependencias.

El reparto nominal y las once ondas son propuestas pendientes de aprobación y de conocer perfiles y disponibilidad. CHANGES.md conserva sus 25 fichas y dependencias sin cambios; el plan de tres carriles no se sustituye todavía. No se trasladaron IDs, fechas, personas, estado de implementación ni reglas de adjudicación del ejemplo. No se eligieron build, migrador, proveedor de email o CI, ni se realizaron acciones remotas. La entrega académica sigue diferenciada del backlog completo.

## Aplicación y Verify de C-01

Fecha: 19/11/2026. Giovanni aprobó la propuesta y el worktree aislado. Se implementó el dominio Java 25 con Maven/JUnit en backend/, sin API, Spring Boot, PostgreSQL ni UI. Se observaron 55 fallos de comportamiento sobre el esqueleto, luego GREEN por etapas y 55 pruebas sin fallos ni omisiones en clean verify. Un error inicial de inferencia de tipos de las assertions se corrigió antes del RED y se conserva como incidencia de preparación, no como prueba de TDD.

La evidencia está en openspec/changes/crear-turno-sin-solapamientos-dominio/evidence/. Se revisaron intervalo simétrico inclusivo, contención, colecciones no ordenadas, inmutabilidad y entradas malformadas. Revisión realizada en la misma sesión, sin afirmar revisión independiente ni uso del modelo Luna. Se conciliaron selección de C-01 y decisiones acotadas en KB, README y roadmap; no se cambiaron dependencias. Pendientes: Archive, integración a main y evidencia humana académica. Sin push.

## Archivo e integración de C-01 en main

Fecha: 19/11/2026. El usuario pidió archivar e integrar. El CLI archivó C-01 bajo openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio y promovió siete requisitos al spec principal disponibilidad-de-turnos, validado sin errores. Las 13 tareas estaban completas.

Se guardaron los documentos previos de colaboración en un commit separado. Se preservó una copia de los cambios y de la propuesta inicial en docs/evidencias/antes-integracion-c01-2026-11-19/, ignorada por Git. La integración conservó ambas entradas de bitácora al resolver un conflicto de añadidos al final. README, roadmap, plan y AGENTS/CLAUDE se conciliaron con las decisiones ya aprobadas, sin cambiar reglas duras ni dependencias. La evidencia final de integración registra las verificaciones ejecutadas sobre main. No se realizó push.

## Preparación de la colaboración en GitHub

Fecha: 19/11/2026. Giovanni aportó los usuarios y aprobó colaboradores con escritura, PR con su aprobación obligatoria y CI correcto, excepción explícita para integrar PRs propios, y push verificado de main con C-01 y configuración. Se verificaron los usuarios y se enviaron invitaciones a FabianoRamos05, Dreamboy2006 y jowy0, pendientes de aceptación al preparar esta evidencia.

Se crearon CONTRIBUTING.md, CODEOWNERS con Giovanni como revisor, plantilla de PR y CI con Temurin 25/Maven, Actions fijadas por SHA y token de lectura. Build local: 55 pruebas sin fallos, errores u omisiones. La publicación precederá a la comprobación del CI remoto y a la protección de main, para dejar registrado el check real. El bypass administrativo se conserva para excepciones expresas del propietario; no es una autorización automática para saltar CI o revisiones. No se aprobaron asignaciones nominales ni se crearon Issues/Projects.

## Aprobación del reparto nominal

Fecha: 19/11/2026. Giovanni confirmó: «El reparto de changes queda aprobado». Se consolidó la distribución previamente presentada: Giovanni C-01/C-02/C-06/C-07/C-08/C-14/C-25; Sofia C-04/C-09/C-16/C-21/C-22/C-24; Luciano C-05/C-10/C-12/C-13/C-15/C-23; Fabi C-03/C-11/C-17/C-18/C-19/C-20. CHANGES incorpora responsables y once ondas orientativas sin alterar dependencias o alcances. C-01 sigue completado. La aprobación no inicia código CRITICO, no publica Issues y no autoriza un nuevo push. Permanecen pendientes disponibilidad, defensa y elección de seguimiento remoto.

## Exención de revisión para el líder

Fecha: 19/11/2026. Giovanni indicó: «mis pushes no deben ser aprobados por mi mismo. Cada cambio mio va sin aprobar, al ser el lider». Se sustituye la exigencia anterior de excepción explícita por PR propio: sus cambios no requieren aprobación de revisión y el agente no volverá a solicitar esa excepción cuando la tarea autorice integrar. Se mantienen CI correcto, trazabilidad por PR y pedido explícito de push. Los colaboradores requieren aprobación de Giovanni. La configuración remota ya tenía enforce_admins=false y se verificó, por lo que no fue necesario modificar permisos. Documentación actualizada localmente; sin nuevo push ni merge.

## Aplicación y Verify de C-02

Fecha: 19/11/2026. Giovanni aprobó el plan e iniciar Apply. Se implementó Boot 4.1.2 no-web/JPA/Flyway y PostgreSQL 18.7 con cuatro referencias UUID/código, privilegios separados, Compose y Testcontainers. C-01 conserva sus 55 pruebas; el clean verify integrado aprobó además 61 IT sin omisiones, incluido el JAR ejecutable. Worker Sol/medium preparó infraestructura y Reviewer Astra/medium fue autorizado por el usuario para revisión independiente ante indisponibilidad de Luna.

Los hallazgos de entorno de pruebas, identidad ordinaria/migrador, historial futuro y credenciales en URL se reprodujeron antes de corregir y se verificaron mediante regresiones. Evidencia y trazabilidad en el change C-02. No se modificó Postgres.app ni una base persistente del usuario. CI adaptado y comprobado localmente; ejecución remota, archivo e integración quedan pendientes. Sin push.

## Archivo e integración local de C-02

Fecha: 19/11/2026. Tras el pedido de continuar al siguiente paso, se archivó C-02 mediante el CLI OpenSpec como `2026-11-19-persistencia-base-postgresql`, incorporando nueve requisitos y dieciocho escenarios a la especificación principal. Las 17 tareas estaban completas y ambas especificaciones pasaron validación estricta. Integración local en main mediante fast-forward desde su base limpia, conservando los commits de propuesta, implementación y evidencia. Sin push; CI remoto y actualización del Issue #3 quedan pendientes de publicación autorizada. La dependencia de C-03 queda satisfecha en el main local, no todavía en GitHub.

## Preparación de asignación automática de Issues

Fecha: 19/11/2026. Giovanni aprobó el diseño y la preparación local de un workflow horario y manual. Se mantiene CHANGES como fuente de responsables y CONTRIBUTING como tabla de usuarios. El asignador consulta colaboradores activos con escritura y añade únicamente responsables faltantes, sin alterar dependencias ni reemplazar asignaciones. Se incorporaron pruebas y simulación real de lectura; ver [operación y límites](automatizacion-asignaciones.md). Sin push ni activación; ejecución con token de Actions pendiente de publicación autorizada.

## Resolución de pendientes de C-03 — Issue #30

20/11/2026. Giovanni autorizó resolver e integrar y aprobó el cambio propio de contraseña con ventana de cinco. Se aisló el CLI en un proceso con allowlist y se agregó V3 con historial/servicio transaccional, bloqueo por usuario y pruebas reales de ventana, reinicio, concurrencia, rollback y permisos. Se verificó el comando Maven literal con settings vacíos y modo offline. Resultado local: 78 unitarias, 82 IT, nueve pruebas Python y tres specs válidas. [Evidencia](evidencias/c03-pendientes/verify.md). Publicación, CI remoto e integración se verifican en PR #29; no se alteraron bases del equipo ni migraciones previas.

## Preparación de la documentación de entrega

25/11/2026. Se cotejó `main` (`6aa8796`) contra la consigna y el usuario respondió por menú: portada con nombre y apellido de los cuatro integrantes; versionar el registro de skills existente; actualizar README y estados, informe y PDF, y armar el paquete de entrega. Sobre las fuentes, pidió que el agente contrastara lo documentado antes y expusiera las diferencias. El resultado consta como comprobación del agente; la revisión personal de los integrantes sigue pendiente y no se completó ninguna fila en su nombre. Las diferencias no se aplicaron a fichas, matrices ni puntajes: requieren decisión del equipo.

Trabajo en la rama `docs/entrega-academica`, con commits locales y sin push. El detalle está en [docs/entrega](entrega/README.md). Esta entrada no reconstruye las decisiones tomadas entre el 21 y el 25/11, que constan en OpenSpec y en la KB.

## Corrección del informe tras la segunda comprobación de fuentes

25/11/2026. Ante la pregunta de si corregir en el informe las diferencias halladas, la respuesta fue «Sí, corregilas» (Ajustar). Antes de aplicarlas, el agente repitió dos comprobaciones y detectó que su primera pasada había informado mal el adicional por agenda de Clinic Cloud y los importes anuales de Dentify. Se actualizaron fichas, cuatro celdas de DentApp en la matriz B (total de 1,275 a 1,475), la sección C, el resumen de fuentes y el PDF. La revisión personal sigue pendiente. [Detalle](entrega/verificacion-fuentes.md#correcciones-aplicadas).

## Revisión personal de fuentes

25/11/2026. Giovanni Colombo transmitió los resultados de cinco fuentes: DentApp y tab32 a su cargo, Dentalink de Sofia Martinez, Dentidesk de Fabiano Ramos y AgendaPro de Luciano Izquierdo Cafici. Se incorporaron al informe y al PDF, con los cargos por servicio de tab32 y la distinción en Dentidesk entre 15 días de prueba y 30 días para pedir la exportación. El agente no localizó en tab32 un plan cercano a $400; ese dato y el alcance de la simulación en AgendaPro quedan a confirmar. [Planilla](entrega/verificacion-fuentes.md#planilla-personal).

25/11/2026. Giovanni Colombo confirmó que el importe observado en tab32 era $427. En la página corresponde al costo mensual estimado de Open Dental dentro de un comparador; el precio de tab32 en el informe no cambia. Sigue por confirmar el alcance de la simulación en AgendaPro. [Detalle](entrega/verificacion-fuentes.md#tab32-importe-observado).

25/11/2026. Giovanni Colombo confirmó que Luciano Izquierdo Cafici detuvo la simulación en AgendaPro antes de confirmar la reserva. Con eso no quedan datos abiertos de la revisión de fuentes.
