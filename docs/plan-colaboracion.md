# Plan de colaboración de cuatro integrantes

Fecha: 19/11/2026. Complemento de [CHANGES.md](../CHANGES.md). Recoge la recomendación docente aportada por Giovanni y la adapta al estado de este repositorio. Los IDs, alcances, dependencias y niveles de gobernanza del roadmap siguen vigentes.

## Decisiones confirmadas

- Equipo: Giovanni (líder y propietario del repositorio), Sofia, Luciano y Fabi.
- Giovanni prefiere backend, participará también en frontend e implementará la base y la lógica central de agenda. Los demás changes prioritarios se elegirán con él. Criticidad técnica y prioridad de entrega son conceptos distintos.
- Política elegida por Giovanni el 19/11: **propuestas y contratos en paralelo; implementación después de integrar las dependencias**. No se habilitan ramas encadenadas para adelantar implementación.
- El repositorio ya está creado y publicado. C-01 fue elegido por Giovanni, implementado, verificado y archivado; queda integrado en main con este cierre.
- La entrega del 25/11 exige completar un change con su evidencia y ciclo OpenSpec, no los 25 changes del MVP. No se promete un calendario diario sin conocer disponibilidad y estimaciones.
- Los agentes pueden compilar, probar y crear commits atómicos dentro del trabajo autorizado. Cada push requiere un pedido explícito.

## Separar preparación, desarrollo e integración

| Momento | Trabajo permitido | Condición para avanzar |
| --- | --- | --- |
| Preparación paralela | Leer KB, explorar, redactar propuestas, detectar preguntas y preparar contratos | Distinguir hechos aprobados de supuestos; coordinar archivos compartidos |
| Acuerdo de contratos | Definir interfaces necesarias entre changes y criterios de aceptación | Aprobación de las decisiones que sigan abiertas, con productores y consumidores presentes |
| Implementación | Código y pruebas del change | Todas sus dependencias integradas en main, alcance aprobado y gobernanza satisfecha |
| Integración | Revisar el diff y comprobar el resultado combinado antes de incorporar a main | Dependencias de CHANGES satisfechas y verificaciones pertinentes ejecutadas |

Una propuesta adelantada es provisional cuando depende de decisiones abiertas de otro change. Se revisa contra el código y los contratos finalmente integrados antes de implementar. Proponer crea artefactos de planificación: no equivale a implementar ni a cerrar preguntas de la KB. El CLI de OpenSpec administra esos artefactos.

Los gates del roadmap representan disponibilidad para implementar; no impiden preparar propuestas. Mientras Giovanni desarrolla la base, los demás pueden trabajar sobre sus propuestas y preguntas sin escribir código dependiente ni quedar inactivos.

## Reunión de contratos antes de abrir trabajo dependiente

Se incorpora al planning una sesión de coordinación entre productores y consumidores. Su duración se ajustará a las preguntas pendientes; la hora sugerida por el profesor es orientativa.

Registrar, para cada frontera que realmente necesite el change:

1. Changes involucrados, responsable del contrato y consumidores.
2. Entradas, salidas, errores y reglas de validación; operaciones HTTP y DTOs únicamente cuando corresponda acordarlos.
3. Identificadores, fechas, zona horaria, duración, intervalo y estados que deban compartir módulos.
4. Permisos y separación de datos administrativos y clínicos según las reglas aprobadas.
5. Casos de aceptación, ejemplos ficticios y qué pruebas comprobarán el contrato; distinguir simulación de evidencia de persistencia real.
6. Preguntas PA pendientes, decisión del usuario y efecto sobre propuestas ya preparadas.

Guardar cada acuerdo junto a los artefactos del change correspondiente y enlazarlo desde sus tareas. El contrato no introduce endpoints, tablas, ORM, proveedor de email o políticas de seguridad por inferencia. Si cambia una interfaz, avisar a sus consumidores y revisar los artefactos afectados antes de continuar.

## Distribución nominal propuesta, todavía no aprobada

Esta tabla conserva la propuesta conversada; no adjudica definitivamente trabajo a los integrantes. Faltan experiencia, disponibilidad y preferencias de Sofia, Luciano y Fabi. Se propone continuidad por áreas, con participación frontend/backend según las necesidades.

| Integrante | Enfoque propuesto | Changes propuestos |
| --- | --- | --- |
| Giovanni | Base, núcleo de agenda e integración | C-01, C-02, C-06, C-07, C-08, C-14, C-25 |
| Sofia | Pacientes, cuenta y lista de espera | C-04, C-09, C-16, C-21, C-22, C-24 |
| Luciano | Recursos, bloqueos e interfaces de agenda | C-05, C-10, C-12, C-13, C-15, C-23 |
| Fabi | Acceso interno, email e interfaz de pacientes | C-03, C-11, C-17, C-18, C-19, C-20 |

Hay cruces de áreas que deben revisarse con el equipo. C-03, C-04, C-16 y C-18 requieren especial atención por su criticidad; su responsable no queda aprobado por aparecer en esta propuesta. Giovanni conserva la elección conjunta de otros changes prioritarios.

### Ondas orientativas para evaluar el reparto

Cada fila supone que los changes de las filas anteriores ya se integraron. Son unidades abstractas de trabajo, no días: no consideran duraciones, revisiones o capacidad real. Los guiones permiten preparar contratos, revisar o aportar evidencia; no representan inactividad obligatoria.

| Onda | Giovanni | Sofia | Luciano | Fabi |
| --- | --- | --- | --- | --- |
| 1 | C-01 | — | — | — |
| 2 | C-02 | — | — | — |
| 3 | — | — | — | C-03 |
| 4 | — | C-04 | C-05 | — |
| 5 | — | C-16 | C-15 | C-11 |
| 6 | C-06 | C-21 | — | — |
| 7 | C-08 | C-09 | C-10 | C-17 |
| 8 | C-07 | C-22 | C-13 | C-18 |
| 9 | — | C-24 | C-12 | C-19 |
| 10 | C-25 | — | C-23 | C-20 |
| 11 | C-14 | — | — | — |

La propuesta cubre los 25 changes una vez y respeta sus precedencias. No sustituye el esquema técnico de tres carriles existente en CHANGES hasta aprobar el reparto nominal y la revisión puntual del roadmap.

## Coordinación y revisión propuestas

Las siguientes medidas quedan preparadas para acordar con el equipo; no configuran automáticamente GitHub ni crean nuevas reglas obligatorias:

- Un change de implementación activo por integrante; preparar el siguiente mientras se revisa el actual.
- Un responsable de implementación y un revisor distinto por change. Giovanni coordina dependencias y cambios de alcance; se propone rotar las revisiones para que no sea el único revisor. Otro integrante revisaría sus cambios.
- Una rama y un PR por change; para ramas creadas por Codex, ejemplo `codex/c-01-dominio-turnos`. Commits atómicos y descriptivos como ya está aprobado; adoptar Conventional Commits requeriría un acuerdo adicional.
- Antes de integrar, comprobar dependencias, aprobaciones de gobernanza, diff, resultados de pruebas y efectos en los consumidores del contrato.
- Definir propietarios de archivos compartidos cuando existan: build, configuración, seguridad, contratos y migraciones. Coordinar ediciones y no sobrescribir trabajo de otro integrante. No escribir CODEOWNERS con usuarios o rutas inventados.
- Seguimiento propuesto por change: pendiente de acuerdos → preparado → en implementación → en revisión → integrado. La verificación y el archivo OpenSpec conservan sus propios estados; el tablero no los sustituye.

## Entornos reproducibles y base de datos

Cada integrante deberá poder clonar, consultar instrucciones y regenerar su registro local de skills verificando disponibilidad real. `.atl/skill-registry.md` está ignorado por Git y no se distribuye automáticamente. [README](../README.md) documenta las limitaciones actuales de herramientas y utilidades.

El primer change que incorpore un entorno ejecutable deberá documentar versiones elegidas y comandos reproducibles. Cuando se definan variables de entorno, preparar un ejemplo sin secretos; `.env.example` ya está permitido por `.gitignore`.

Antes de introducir migraciones, acordar herramienta, propietario y asignación de versiones para evitar colisiones entre ramas. Verificar el orden de aplicación tanto desde una base vacía como al actualizar una existente. No se adopta Flyway ni su opción `out-of-order`, tampoco Gradle o Maven, por la recomendación docente. La CI dependerá de los comandos de build y pruebas que realmente se incorporen y verifiquen.

## GitHub: configuración del equipo

Los usuarios ya están confirmados y las invitaciones de escritura fueron enviadas el 19/11/2026: Fabi @FabianoRamos05, Luciano @Dreamboy2006 y Sofia @jowy0. Cada integrante debe aceptar su invitación; Giovanni mantiene la administración.

Política aprobada: PR hacia main, aprobación de Giovanni mediante CODEOWNERS y CI backend-verify correcto, con rama actualizada. La excepción administrativa de los PRs propios requiere autorización explícita para cada integración y CI correcto, según [CONTRIBUTING.md](../CONTRIBUTING.md). Las Actions se fijan por SHA y se ejecutan con permisos de lectura.

Se prepara la publicación de esta configuración y C-01, seguida de la comprobación del primer CI remoto y la activación de protección. La evidencia de preparación distingue política aprobada de estado remoto observado; GitHub conserva la configuración efectiva. El permiso de push otorgado es solo para esta publicación inicial, no permanente.

Issues por change y GitHub Projects quedan pendientes de elección; las asignaciones nominales no se publican automáticamente. CHANGES conserva alcance y dependencias.

## Seguimiento del plazo y siguientes decisiones

Revisar al integrar cada change: dependencias desbloqueadas, bloqueos de contratos, capacidad disponible, evidencia pendiente y riesgos para la entrega del 25/11. Si cambia el plazo o la capacidad, presentar alternativas a Giovanni; no quitar funcionalidades automáticamente.

Pendientes para cerrar el planning:

1. Recoger perfiles y disponibilidad de Sofia, Luciano y Fabi; aprobar o ajustar el reparto nominal y los otros changes prioritarios de Giovanni.
2. Revisión e integración acordadas: aprobación de Giovanni y CI, con excepción propia explícita. Quedan pendientes reparto por áreas y elección de Issues/Projects.
3. PA-13 resuelta: C-01 elegido y completado. Resolver las preguntas bloqueantes del siguiente change antes de implementar.
4. Completar PA-10 sobre reparto, defensa y respaldo de la excepción docente si lo requiere la cátedra. La verificación personal de fuentes y la reflexión siguen a cargo de los integrantes.

Se mantienen las reglas del producto: recepción adjudica huecos; un aviso no reserva. No se incorpora la regla de “gana el primero” del ejemplo de otro equipo ni se recortan cuenta del paciente o lista de espera sin decisión del usuario.

## Configuración colaborativa aprobada el 19/11/2026

| Integrante | GitHub |
| --- | --- |
| Giovanni | @giovanniclmb |
| Fabi | @FabianoRamos05 |
| Luciano | @Dreamboy2006 |
| Sofia | @jowy0 |

El usuario aprobó invitar a los tres como colaboradores con escritura, exigir PR con aprobación de Giovanni y CI correcto, y publicar main con C-01 y esta configuración tras verificarla. Para PRs propios de Giovanni se acordó una excepción explícita al integrar: no puede autoaprobarse. La capacidad administrativa de bypass se conserva y su uso debe documentarse para el PR concreto, con CI correcto; no es autorización permanente para omitir controles.

[CONTRIBUTING.md](../CONTRIBUTING.md) contiene el procedimiento vigente. CODEOWNERS requiere la revisión de Giovanni para todo el repositorio, sin asignar implementación por áreas. CI usa el build Maven/Java 25 ya incorporado en C-01. Estas decisiones sustituyen los pendientes de usuarios, revisión y CI de las secciones anteriores; perfiles, disponibilidad, reparto de changes e Issues/Projects continúan pendientes. La evidencia de configuración distingue invitaciones pendientes de colaboradores activos y configuración preparada de configuración aplicada.
