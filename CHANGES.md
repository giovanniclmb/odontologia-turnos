# CHANGES — Secuencia de Implementación

> Índice canónico del sistema de turnos odontológicos de una sede.
> Cada ficha delimita una unidad de trabajo para especificar y verificar en una sesión orientativa de 4–6 horas; la estimación se revisará al resolver sus pendientes.
> Leer este archivo y su KB antes de empezar un item.

## Cómo usar este documento

1. Identificar el item, comprobar dependencias y resolver sus preguntas bloqueantes. Todos los items comienzan pendientes; estar primero en el orden no autoriza implementar.
2. Leer las referencias de KB de la ficha y las decisiones aprobadas. El roadmap organiza trabajo; no elige versiones, intervalos, endpoints, tablas o permisos todavía abiertos.
3. Acordar alcance y verificación. Para el único change académico, elegirlo con el usuario y seguir explore → propose → apply → verify → archive mediante OpenSpec. Resolver preguntas en el menú y aprobar el spec antes de Apply.
4. Implementar dentro del alcance aprobado, revisar lo necesario y ejecutar las pruebas del spec. Registrar evidencia real; una prueba con datos en memoria no demuestra integridad concurrente de PostgreSQL ni protección de una API.
5. Actualizar el estado del item tras verificar y, cuando corresponda, archivar mediante el CLI. Conservar IDs y cambios ajenos. El estado operativo local está en estas fichas; no se publican tareas ni se asignan personas al generar el archivo.

### Alcance, responsables y entrega

El producto conserva HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL. La fecha académica es 25/11/2026, con presupuesto externo cero y cuatro integrantes según autorización docente informada por el usuario. El reparto aprobado el 19/11/2026 asigna los 25 changes a Giovanni, Sofia, Luciano y Fabi. Los responsables y once ondas orientativas se registran abajo y en el plan de colaboración; PA-10 conserva únicamente los pendientes de defensa y respaldo académico.

El backlog completo no es un compromiso de implementar las 25 fichas para esa fecha. La consigna exige un único change con reglas verificables y ciclo OPSX completo. Giovanni eligió C-01 y aprobó su propuesta y aplicación; su dominio está implementado y verificado en la rama de trabajo, archivado e integrado en main. Los demás items describen cómo extender el MVP después.

Se reutilizan OpenSpec, Git, Discovery y KB ya preparados; no se agregan fichas para reinstalarlos. C-01 incluye únicamente el build y las pruebas mínimos que faltan para ejecutar su módulo de dominio. La persistencia y el acceso protegido se incorporan después, sin presentar el módulo aislado como aplicación desplegada.

La revisión 2 aprobada incorpora paciente, email real de prueba, lista de espera, agenda diaria/semanal y bloqueos. Autorreserva general, adjudicación automática de huecos, WhatsApp automático, odontograma, pagos y facturación siguen fuera del backlog. Las referencias Dentalink, DentApp, Dentify, Dentidesk y AgendaPro, y las referencias UX Dentalink/AgendaPro/Dentrix, sirven de contraste y no agregan funcionalidades.

### Riesgos y condiciones de avance

| Condición | Consecuencia operativa |
| --- | --- |
| Intervalo inicial y aplicación sin resolver, PA-01 | Acordar regla y valores de prueba antes del spec de C-01; no usar cero por defecto |
| Estados, disponibilidad y concurrencia abiertos, PA-02/PA-04 | Distinguir reservas que ocupan recursos y acordar bordes; validar persistencia concurrente en C-06 |
| Autenticación y permisos detallados pendientes, PA-05 | Acordar y revisar C-03 antes de exponer recursos protegidos; conservar recepción lectura y odontólogos edición clínica |
| Modelo y campos pendientes, PA-06/PA-07 | No inventar DTO, tipos físicos, cardinalidades o migraciones al planificar |
| Reprogramación fallida sin resultado acordado, PA-03 | Bloquea su spec; no elegir por inferencia qué ocurre con la reserva original |
| Diseño visual diferido | Antes de Apply de cualquier ficha visual resolver la política visual y sus aprobaciones cuando corresponda |
| Reparto aprobado; disponibilidad y defensa pendientes, PA-10 | Respetar responsables y revisión; coordinar archivos compartidos y no inferir horas disponibles |
| Verificación personal de cinco fuentes pendiente, PA-11 | La entrega académica no está completa por tener pruebas de software verdes |
| Funciones y condiciones no evidenciadas de proveedores, PA-12 | Mantener el contraste competitivo pendiente; no agregar integraciones al MVP por inferencia |
| Email, cuentas, enlaces y lista pendientes, PA-15 a PA-21 | Resolver cada contrato antes de implementarlo; no seleccionar proveedor ni permisos en el roadmap |
| Riesgo de plazo | Priorizar el único change elegido y su evidencia; el cronograma de abajo no garantiza fechas |

Los niveles CRITICO y ALTO requieren respetar la gobernanza del proyecto y las aprobaciones específicas antes de código o configuración afectada. Generar este roadmap no aprueba esos cambios de implementación.

## Árbol de dependencias

Dependencias completas en la tabla y en cada ficha. C-01 está implementado y verificado, archivado e integrado en main; C-02 está implementado, verificado, archivado e integrado en main local; los demás items siguen pendientes. Se conservan C-01 a C-14 y se añaden C-15 a C-25; el orden numérico no sustituye las dependencias.

```text
C-01
└── C-02
    └── C-03
        ├── C-04 → C-16 → C-21 → C-22 → C-25
        └── C-05 → C-15 → C-06
                           ├── C-07
                           ├── C-08 → C-18 → C-19 → C-20
                           ├── C-09
                           ├── C-10 → C-12 / C-13 / C-23 / C-24
                           └── C-17 → C-19 / C-22
C-04 → C-11
C-11 + C-12 + C-13 + C-19 + C-20 + C-23 + C-24 + C-25 → C-14
```

| Item | Dependencias completas |
| --- | --- |
| C-01 | Ninguna |
| C-02 | C-01 |
| C-03 | C-02 |
| C-04 | C-03 |
| C-05 | C-03 |
| C-06 | C-01, C-04, C-05, C-15 |
| C-07 | C-06 |
| C-08 | C-06 |
| C-09 | C-06 |
| C-10 | C-06 |
| C-11 | C-04 |
| C-12 | C-07, C-08, C-10 |
| C-13 | C-09, C-10 |
| C-14 | C-11, C-12, C-13, C-19, C-20, C-23, C-24, C-25 |
| C-15 | C-05 |
| C-16 | C-03, C-04 |
| C-17 | C-06 |
| C-18 | C-08 |
| C-19 | C-17, C-18 |
| C-20 | C-19, C-07 |
| C-21 | C-16, C-05 |
| C-22 | C-21, C-17, C-08 |
| C-23 | C-18, C-10 |
| C-24 | C-21, C-10 |
| C-25 | C-22, C-10 |

### Paralelismo por fase

Los gates indican disponibilidad por dependencias, no ejecución simultánea obligatoria. Compartir build, contratos o migraciones requiere un propietario y coordinación; cada carril ejecuta un item por paso. Las fichas visuales requieren además resolver la política de diseño.

```text
GATE 0: fuentes y alcance revisados
  → C-01 crear-turno-sin-solapamientos-dominio [Giovanni; comprobar todas sus dependencias]
GATE 1: dependencias previas verificadas según tabla (nivel 1)
  → C-02 persistencia-base-postgresql [Giovanni; comprobar todas sus dependencias]
GATE 2: dependencias previas verificadas según tabla (nivel 2)
  → C-03 acceso-y-permisos-internos [Fabi; comprobar todas sus dependencias]
GATE 3: dependencias previas verificadas según tabla (nivel 3)
  → C-04 pacientes-y-ficha-clinica [Sofia; comprobar todas sus dependencias]
  → C-05 recursos-prestaciones-e-intervalo [Luciano; comprobar todas sus dependencias]
GATE 4: dependencias previas verificadas según tabla (nivel 4)
  → C-11 interfaz-pacientes-y-ficha [Fabi; comprobar todas sus dependencias]
  → C-15 bloqueos-de-recursos [Luciano; comprobar todas sus dependencias]
  → C-16 cuenta-y-acceso-del-paciente [Sofia; comprobar todas sus dependencias]
GATE 5: dependencias previas verificadas según tabla (nivel 5)
  → C-06 crear-turno-con-persistencia [Giovanni; comprobar todas sus dependencias]
  → C-21 inscripcion-y-preferencias-de-espera [Sofia; comprobar todas sus dependencias]
GATE 6: dependencias previas verificadas según tabla (nivel 6)
  → C-07 reprogramar-turno [Giovanni; comprobar todas sus dependencias]
  → C-08 cancelar-y-liberar-horario [Giovanni; comprobar todas sus dependencias]
  → C-09 confirmaciones-manuales [Sofia; comprobar todas sus dependencias]
  → C-10 interfaz-agenda-y-alta [Luciano; comprobar todas sus dependencias]
  → C-17 transporte-de-email-de-prueba [Fabi; comprobar todas sus dependencias]
GATE 7: dependencias previas verificadas según tabla (nivel 7)
  → C-12 interfaz-reprogramacion-y-cancelacion [Luciano; comprobar todas sus dependencias]
  → C-13 interfaz-confirmaciones [Luciano; comprobar todas sus dependencias]
  → C-18 cancelacion-del-paciente-por-enlace [Fabi; comprobar todas sus dependencias]
  → C-22 avisos-de-huecos-y-asignacion-por-recepcion [Sofia; comprobar todas sus dependencias]
  → C-24 interfaz-cuenta-y-lista-de-espera [Sofia; comprobar todas sus dependencias]
GATE 8: dependencias previas verificadas según tabla (nivel 8)
  → C-19 email-al-crear-turno [Fabi; comprobar todas sus dependencias]
  → C-23 interfaz-cancelacion-por-enlace [Luciano; comprobar todas sus dependencias]
  → C-25 interfaz-asignacion-de-huecos [Giovanni; comprobar todas sus dependencias]
GATE 9: dependencias previas verificadas según tabla (nivel 9)
  → C-20 recordatorio-automatico-24-horas [Fabi; comprobar todas sus dependencias]
GATE 10: dependencias previas verificadas según tabla (nivel 10)
  → C-14 integracion-y-ejecucion-reproducible [Giovanni; comprobar todas sus dependencias]
```

### Camino crítico (11 changes — mínimo por precedencias)

```text
C-01 → C-02 → C-03 → C-05 → C-15 → C-06 → C-08 → C-18 → C-19 → C-20 → C-14
```

Es una cadena de mayor longitud del DAG usando unidades equivalentes, no una estimación en horas ni una promesa de entrega. Puede haber otras cadenas de igual longitud. C-14 integra el MVP local; no declara un despliegue clínico en producción. La entrega académica puede limitarse al único change elegido por el usuario, sin atribuirle las capacidades del resto.

### Plan aprobado con cuatro integrantes

Giovanni aprobó el reparto el 19/11/2026. Las ondas preservan las dependencias y consideran una tarea por integrante en cada onda; son unidades abstractas, no días ni promesa de un óptimo temporal. C-01 está completado. Se permiten propuestas y contratos anticipados, pero la implementación requiere dependencias integradas y aprobaciones de gobernanza.

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

## FASE 0 — Dominio aislado y persistencia base

### [C-01] `crear-turno-sin-solapamientos-dominio`
- **Estado**: `[x]` implementado, verificado, archivado e integrado en main. Responsable: Giovanni. Evidencia: [Verify](openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/evidence/verify.md).
- **Responsable**: Giovanni
- **Scope**: Unidad de lógica de creación de un turno, elegida como único change académico por Giovanni.
  - Acordar PA-01, PA-02, PA-04 y PA-09 en el alcance mínimo necesario; identificar en PA-07 los conceptos y datos ficticios indispensables. PA-13 está resuelta por la elección de C-01; las demás preguntas se acotan al dominio según su propuesta aprobada.
  - Crear el build y el comando de pruebas Java mínimos, con versiones, herramienta y ubicación aprobadas en Propose. Definir primero las representaciones de paciente de referencia, profesional, sillón, prestación, duración y reservas relevantes para esta regla, sin persistencia física.
  - Implementar la validación de ambos recursos, duración efectiva ajustable e intervalo recibido como configuración explícita; trabajar con reservas/fixtures controlados, sin inventar valor inicial de producción ni estados.
  - Cubrir parcialmente US-004, únicamente su lógica de disponibilidad/duración, y la aplicación del intervalo de US-005; verificar RN-AG-02 a RN-AG-05 y RN-TR-01. RN-AG-01 delimita el contexto de una sede y equipo interno, pero la identidad del operador se verifica posteriormente en C-06/C-10. No implementar administración completa de pacientes, recursos o configuración.
  - Tests del spec: caso feliz, conflicto de profesional, conflicto de sillón y borde del intervalo definido con el usuario; documentar al menos tres escenarios verificables. Ejecutar suite y documentar Verify antes de Archive.
  - Entregar módulo, pruebas y README para ejecutar exclusivamente este change. No incorpora HTTP, login, interfaz ni PostgreSQL; no demuestra exclusión concurrente entre solicitudes persistidas. No define todavía endpoints ni migraciones.
- **Dependencias**: ninguna
- **Governance**: CRITICO
- **Leer antes**:
  - [01 Visión y objetivos](knowledge-base/01_vision_y_objetivos.md) §Alcance del MVP aprobado
  - [04 Modelo de datos](knowledge-base/04_modelo_de_datos.md) §Entidades y conceptos
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-004 y US-005
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-01, PA-02, PA-04, PA-07, PA-09 y PA-13

### [C-02] `persistencia-base-postgresql`
- **Estado**: `[x]` implementado, verificado, archivado e integrado en main local; publicación remota pendiente. [Verify](openspec/changes/archive/2026-11-19-persistencia-base-postgresql/evidence/verify.md). [Issue #3](https://github.com/giovanniclmb/odontologia-turnos/issues/3).
- **Responsable**: Giovanni
- **Scope**: Base mínima de persistencia para extender el dominio, sin implementar la agenda HTTP.
  - Resolver PA-07 y PA-09: versiones de Spring Boot/PostgreSQL, ejecución y estrategia de migraciones; mantener presupuesto externo cero sin elegir hosting o Docker por inferencia.
  - Integrar el módulo Java anterior en el backend Spring Boot acordado; definir el modelo físico mínimo de identidades y referencias compartidas que necesiten los siguientes changes, a partir del modelo conceptual.
  - Crear conexión y primera migración según convención aprobada; no hay número de migración, nombre de tabla, tipo de identificador ni biblioteca seleccionados por este roadmap.
  - Documentar preparación de datos ficticios y configuración de conexión sin secretos versionados.
  - Tests: arranque del backend, conexión al PostgreSQL de prueba y aplicación de las migraciones acordadas. No incluir una política de autenticación ni exponer pacientes todavía.
- **Dependencias**: C-01
- **Governance**: CRITICO
- **Leer antes**:
  - [02 Descripción general](knowledge-base/02_descripcion_general.md) §Stack tecnológico
  - [04 Modelo de datos](knowledge-base/04_modelo_de_datos.md) §Reglas que condicionan el modelo
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Variables de entorno
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-07 y PA-09


## FASE 1 — Acceso y recursos

### [C-03] `acceso-y-permisos-internos`
- **Estado**: `[x]` implementado, verificado y archivado (Issue #4). Correcciones del Issue #30 verificadas: [Verify](docs/evidencias/c03-pendientes/verify.md). Integración: [PR #29](https://github.com/giovanniclmb/odontologia-turnos/pull/29)
- **Responsable**: Fabi
- **Scope**: Identificación y autorización mínimas del equipo antes de recursos protegidos.
  - Resolver PA-05 y el alcance de usuarios/registros; aprobar mecanismo de autenticación y contrato de ingreso. No seleccionar JWT, sesiones, proveedor ni un rol superadministrador en este índice.
  - Incorporar el modelo de usuario y permisos mínimos acordados; conservar recepción lectura clínica y edición clínica exclusiva de odontólogos.
  - Implementar las operaciones de acceso y control del contrato aprobado, con rutas y formatos definidos en Propose; no hay endpoints concretos aprobados en la KB.
  - Tests: acceso válido e inválido y autorización permitida/denegada conforme al contrato; preparar escenarios reutilizables para los recursos protegidos posteriores.
- **Dependencias**: C-02
- **Governance**: CRITICO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §RBAC y matriz de permisos
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Información clínica
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Seguridad
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-05

### [C-04] `pacientes-y-ficha-clinica`
- **Estado**: `[x]` implementado, verificado y archivado en OpenSpec; integración pendiente. [Verify](openspec/changes/archive/2026-11-23-pacientes-y-ficha-clinica/evidence/verify.md)
- **Responsable**: Sofia
- **Scope**: Gestión mínima del paciente y lectura/edición de la ficha sencilla en backend.
  - Resolver PA-05, PA-06 y PA-07 para campos, operaciones y alcance por registro; no asumir permisos de eliminación.
  - Incorporar los modelos y migraciones de paciente, antecedentes y notas que se aprueben, sin odontograma ni funciones administrativas ampliadas.
  - Implementar los contratos backend acordados para US-001, US-002 y US-003, preservando RN-CL-01, RN-CL-02 y RN-TR-01. Las operaciones y DTO de servicio, y la migración, se nombrarán en Propose; este change no incorpora rutas HTTP.
  - Tests: gestión mínima autorizada, lectura clínica por ambos perfiles y edición rechazada para recepción; comprobar autorización en el backend, no solo ocultando controles.
- **Dependencias**: C-03
- **Governance**: CRITICO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §RBAC y matriz de permisos
  - [04 Modelo de datos](knowledge-base/04_modelo_de_datos.md) §Entidades y conceptos
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §Épica 1 · Pacientes y ficha clínica
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 1 y Flujo 2
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-05, PA-06 y PA-07

### [C-05] `recursos-prestaciones-e-intervalo`
- **Estado**: `[x]` implementado, verificado y archivado en OpenSpec. [Tareas y evidencia](openspec/changes/archive/2026-11-21-recursos-prestaciones-e-intervalo/tasks.md)
- **Responsable**: Luciano
- **Scope**: Configuración mínima de los recursos que necesita la agenda persistida.
  - Decisiones aprobadas para C-05 sobre PA-01, PA-04, PA-05, PA-08 y PA-14. Intervalo sin valor inicial, en minutos enteros positivos y aplicable a futuras validaciones. Disponibilidad mínima por existencia y estado de los recursos e intervalo configurado. Altas sin cantidades fijas y catálogo inicialmente vacío.
  - Recepción administra la configuración y tiene permiso para crear usuarios internos y vincularlos a profesionales. Odontología consulta la configuración y los recursos.
  - Incorporar las representaciones persistidas de profesionales, sillones, prestaciones y configuración de la sede acordadas; no añadir múltiples sedes ni valores por defecto inventados.
  - Implementar operaciones mínimas acordadas para consultar/establecer esos datos y aplicar US-005 con RN-AG-02, RN-AG-04 y RN-AG-05. No se presupone CRUD completo ni eliminación de recursos.
  - Tests: permisos de configuración, duración de referencia, intervalo único y uso de datos ficticios; validar límites que el usuario haya aprobado.
- **Dependencias**: C-03
- **Governance**: ALTO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §RBAC y matriz de permisos
  - [04 Modelo de datos](knowledge-base/04_modelo_de_datos.md) §Entidades y conceptos
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-005
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-01, PA-04, PA-05, PA-08 y PA-14

### [C-16] `cuenta-y-acceso-del-paciente`
- **Estado**: `[ ]` pendiente
- **Responsable**: Sofia
- **Scope**: Identificación del paciente para gestionar su propia inscripción; parte de US-014 y RN-PA-01.
  - Resolver PA-16 y PA-05: habilitación o registro, verificación, vínculo con paciente y alcance por operación; no elegir JWT, sesión o proveedor en el roadmap.
  - Definir y luego implementar el contrato aprobado de cuenta, ingreso y control de acceso propio, sin extender permisos clínicos ni reservar turnos.
  - Tests: acceso válido/inválido, cuenta vinculada y rechazo al intentar operar la inscripción de otra persona. Documentar recuperación y errores solo según reglas acordadas.
- **Dependencias**: C-03, C-04
- **Governance**: CRITICO
- **Leer antes**:
  - [Actores y roles](knowledge-base/03_actores_y_roles.md)
  - [Modelo conceptual](knowledge-base/04_modelo_de_datos.md)
  - [Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)


## FASE 2 — Agenda, bloqueos y participación del paciente

### [C-15] `bloqueos-de-recursos`
- **Estado**: `[x]` implementado, verificado localmente y archivado en OpenSpec. [Tareas y evidencia](openspec/changes/archive/2026-11-23-bloqueos-de-recursos/tasks.md)
- **Responsable**: Luciano
- **Scope**: Indisponibilidad por período gestionada por recepción para US-010 y RN-AG-10.
  - PA-04/PA-19 resueltas para C-15: períodos semiabiertos y coordinación transaccional por recurso. Modificación y desbloqueo quedan fuera del alcance.
  - Definir representación y contrato backend del bloqueo de sillón y/o profesional; rechazar si afecta turnos existentes. Hacer consultable la indisponibilidad para C-06 y C-07.
  - Tests: bloqueo de cada recurso y ambos, rechazo ante turno existente, alta y reprogramación impedidas sobre recursos bloqueados y solicitudes simultáneas según el spec. Acordar migraciones y rutas antes de código.
- **Dependencias**: C-05
- **Governance**: MEDIO
- **Leer antes**:
  - [Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [Modelo conceptual](knowledge-base/04_modelo_de_datos.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)

### [C-06] `crear-turno-con-persistencia`
- **Estado**: `[ ]` implementado y verificado sobre C-15 integrado, con revisión independiente cerrada. Pendientes archivo e integración. [Evidencia](openspec/changes/crear-turno-con-persistencia/verify.md)
- **Responsable**: Giovanni
- **Scope**: Conectar la regla de creación con pacientes, recursos y guardado real.
  - El alta crea turnos pendientes de confirmación que ocupan ambos recursos. Solo se admiten inicios futuros, sin franjas semanales adicionales. Recepción puede crear con cualquier profesional elegible. Odontología solo puede hacerlo para sí y pacientes con asignación activa. [Decisiones acotadas](knowledge-base/10_preguntas_abiertas.md#resolución-acotada-para-c-06-23112026)
  - Incorporar la persistencia y las operaciones de servicio autorizadas de alta y consulta necesarias para US-004. Reutilizar el [contrato transaccional de C-15](openspec/specs/bloqueos-de-recursos/spec.md) y conectar la detección de turnos ocupantes al alta de bloqueos. C-06 se limita al servicio backend.
  - Reutilizar C-01 al validar profesional, sillón, duración e intervalo y proteger el resultado bajo solicitudes simultáneas; cubrir RN-AG-01 a RN-AG-05 y RN-AG-10. Consultar y respetar los bloqueos de C-15 al crear turnos, también bajo concurrencia.
  - Tests: éxito, conflictos por cada recurso, duración ajustada y borde del intervalo; añadir pruebas contra PostgreSQL para solicitudes concurrentes que compiten por un recurso.
  - Documentar qué evidencia prueba la exclusión concurrente y qué limitaciones conserva; no presentar los fixtures de C-01 como esta verificación.
- **Dependencias**: C-01, C-04, C-05, C-15
- **Governance**: MEDIO
- **Leer antes**:
  - [04 Modelo de datos](knowledge-base/04_modelo_de_datos.md) §Reglas que condicionan el modelo
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 3 · Crear un turno
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Consistencia de turnos
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-02, PA-04, PA-05 y PA-07

### [C-07] `reprogramar-turno`
- **Estado**: `[ ]` pendiente
- **Responsable**: Giovanni
- **Scope**: Modificar la asignación de un turno existente conservando su servicio.
  - Resolver PA-03 y los estados/transiciones de PA-02 antes de escribir el spec; acordar qué sucede con la reserva original y con el aviso al operador ante un fallo.
  - Implementar US-006 y RN-AG-06 sobre el contrato backend aprobado: cambiar fecha, hora, profesional o sillón conservando prestación y duración efectiva.
  - Validar los bloqueos de C-15, la nueva disponibilidad e intervalo con el mecanismo persistido acordado. No incorporar cambio de prestación ni duración durante reprogramación.
  - Tests: reprogramación válida, conservación de datos, conflictos de cada recurso, intervalo y resultado de fallo definido; comprobar concurrencia persistida según su alcance.
- **Dependencias**: C-06
- **Governance**: MEDIO
- **Leer antes**:
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-006
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 4 · Reprogramar un turno
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-02, PA-03, PA-04 y PA-05

### [C-08] `cancelar-y-liberar-horario`
- **Estado**: `[ ]` pendiente
- **Responsable**: Giovanni
- **Scope**: Cancelación de una reserva existente y liberación inmediata de su horario.
  - Resolver PA-02 y el permiso aplicable de PA-05, incluidos los casos de repetición o turno inexistente.
  - Implementar US-007 y RN-AG-07 en la operación backend acordada, liberando el horario sin una acción manual adicional.
  - Tests: cancelación válida, disponibilidad posterior para otra reserva y casos de error definidos; mantener las reglas de profesional, sillón e intervalo al volver a reservar.
  - Delimitar esta ficha a la cancelación interna. La cancelación por enlace se añade en C-18 y los avisos de lista de espera en C-22; no hay reemplazo automático. Acordar el punto de integración para detectar la liberación sin imponer un mecanismo de eventos.
- **Dependencias**: C-06
- **Governance**: MEDIO
- **Leer antes**:
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-007
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 5 · Cancelar un turno
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-02 y PA-05

### [C-09] `confirmaciones-manuales`
- **Estado**: `[ ]` pendiente
- **Responsable**: Sofia
- **Scope**: Listado de pendientes y registro manual del seguimiento por recepción.
  - Resolver PA-02 y PA-05 para condición de pertenencia al listado, representación, transición y duplicación de confirmaciones.
  - Implementar el listado y la operación backend de registro de US-008 y RN-AG-08, con contratos acordados y datos ficticios.
  - Tests: solo aparecen los turnos que cumplen la condición definida, confirmación permitida a recepción y resultados ante errores/duplicación según el spec.
  - Esta ficha conserva el seguimiento manual; los emails automáticos pertenecen a C-17/C-19/C-20/C-22. No añadir WhatsApp ni convertir el recordatorio en confirmación automática.
- **Dependencias**: C-06
- **Governance**: MEDIO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §RBAC y matriz de permisos
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-008
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 6 · Registrar una confirmación manual
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-02 y PA-05

### [C-18] `cancelacion-del-paciente-por-enlace`
- **Estado**: `[ ]` pendiente
- **Responsable**: Fabi
- **Scope**: Cancelación del turno asociado por enlace hasta 48 horas antes (US-013, RN-PA-01 y RN-PA-02).
  - Resolver PA-17/PA-18: protección, vigencia, uso, instante exacto del límite, referencia horaria y efecto de cambios del turno. El plazo no se cambia por inferencia.
  - Implementar emisión y validación del enlace según diseño aprobado y conectar la cancelación aceptada a C-08 para liberar inmediatamente el horario. La entrega del enlace por email pertenece a C-19.
  - Tests: enlace válido en plazo, límite acordado, vencido, alterado o reutilizado según spec; impedir acceso a otro turno y verificar disponibilidad tras cancelar. No usar un identificador expuesto como autorización por defecto.
- **Dependencias**: C-08
- **Governance**: CRITICO
- **Leer antes**:
  - [Actores y roles](knowledge-base/03_actores_y_roles.md)
  - [Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)

### [C-21] `inscripcion-y-preferencias-de-espera`
- **Estado**: `[ ]` pendiente
- **Responsable**: Sofia
- **Scope**: Gestión backend de inscripción propia para US-014 y RN-LE-01.
  - Resolver PA-16/PA-20: ciclo y vigencia de inscripción, operaciones autorizadas y criterios de compatibilidad. No inventar vencimientos, prioridad o cantidad de inscripciones.
  - Implementar el contrato y representación aprobados para prestación, rango de fechas y franjas horarias, con profesional opcional; proteger cada operación mediante C-16.
  - Tests: preferencias válidas e inválidas según spec, profesional omitido y acceso propio/ajeno; mantener separada la inscripción de una reserva.
- **Dependencias**: C-16, C-05
- **Governance**: MEDIO
- **Leer antes**:
  - [Actores y roles](knowledge-base/03_actores_y_roles.md)
  - [Modelo conceptual](knowledge-base/04_modelo_de_datos.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)


## FASE 3 — Comunicaciones y recuperación de huecos

### [C-17] `transporte-de-email-de-prueba`
- **Estado**: `[ ]` pendiente
- **Responsable**: Fabi
- **Scope**: Canal real de email gratuito para RN-EM-03, compartido por los tres tipos de correo.
  - Evaluar y acordar PA-15: proveedor sin costo, límites, destinatarios de prueba indicados por el equipo y configuración. No contratar ni asumir servicio, SMTP o API.
  - Implementar adaptador de envío y configuración sin secretos versionados conforme al contrato aprobado. Acordar constancia de entrega, errores y duplicados en PA-18.
  - Tests de contrato y errores; demostrar un envío real autorizado a una dirección de prueba del equipo. Las pruebas locales simuladas no sustituyen esa evidencia.
- **Dependencias**: C-06
- **Governance**: ALTO
- **Leer antes**:
  - [Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md)
  - [Modelo conceptual](knowledge-base/04_modelo_de_datos.md)
  - [Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)

### [C-19] `email-al-crear-turno`
- **Estado**: `[ ]` pendiente
- **Responsable**: Fabi
- **Scope**: Email de alta con enlace de cancelación para US-011 y RN-EM-01.
  - Resolver PA-15/PA-17/PA-18: destinatario, contenido, efecto de fallos y creación dentro de las ventanas de 48/24 horas; no inventar excepciones al plazo.
  - Conectar la creación persistida con transporte C-17 y enlace C-18. Especificar qué ocurre ante fallo de email sin suponer que se elimina o confirma el turno.
  - Tests: email con enlace del turno correcto, aislamiento de destinatarios, casos duplicados/fallidos acordados y evidencia de envío real a dirección de prueba. No incluir datos clínicos no aprobados en el contenido.
- **Dependencias**: C-17, C-18
- **Governance**: ALTO
- **Leer antes**:
  - [Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)

### [C-20] `recordatorio-automatico-24-horas`
- **Estado**: `[ ]` pendiente
- **Responsable**: Fabi
- **Scope**: Recordatorio de US-012 y RN-EM-02, separado de confirmaciones manuales.
  - Resolver PA-18: programación y referencia horaria, reprogramaciones, cancelaciones, altas cercanas, fallos y duplicados. Acordar tecnología antes de implementar.
  - Enviar automáticamente 24 horas antes usando C-17, sin opción de cancelar cuando venció el plazo de 48 horas. Conservar el registro manual de recepción.
  - Tests temporales verificables con reloj controlado y política acordada para cambios del turno; demostrar el correo real en direcciones de prueba y distinguirlo de una confirmación.
- **Dependencias**: C-19, C-07
- **Governance**: ALTO
- **Leer antes**:
  - [Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)

### [C-22] `avisos-de-huecos-y-asignacion-por-recepcion`
- **Estado**: `[ ]` pendiente
- **Responsable**: Sofia
- **Scope**: Aviso automático a interesados y asignación manual del hueco para US-015 y RN-LE-02/RN-LE-03.
  - Resolver PA-20/PA-21 y PA-18: compatibilidad exacta de preferencias, duración, recursos e intervalo, selección por recepción, avisos repetidos y huecos ya ocupados.
  - Conectar liberación de horario a búsqueda de inscripciones compatibles y envío C-17. El aviso no reserva ni asigna automáticamente.
  - Implementar operación de recepción para confirmar quién toma el hueco, usando creación validada C-06 con bloqueos. Revalidar disponibilidad al asignar conforme al spec.
  - Tests: avisos solo a compatibles, profesional opcional, asignación por recepción, bloqueo/conflicto posterior al aviso y solicitudes simultáneas; evidencia de email real a destinatarios de prueba.
- **Dependencias**: C-21, C-17, C-08
- **Governance**: ALTO
- **Leer antes**:
  - [Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)


## FASE 4 — Interfaces del equipo y del paciente

La política visual sigue diferida: resolverla antes de Apply y cumplir las aprobaciones de sistema/mockups que correspondan. Mantener HTML/CSS/TypeScript vanilla.

### [C-10] `interfaz-agenda-y-alta`
- **Estado**: `[ ]` pendiente
- **Responsable**: Luciano
- **Scope**: Superficie interna de agenda diaria/semanal por sillón, alta y bloqueos (US-004, US-005, US-009 y US-010).
  - Resolver el gate visual, estructura/build frontend y contrato de uso del acceso interno aprobado. Incluir la superficie mínima de ingreso requerida por C-03, sin inventar un mecanismo nuevo.
  - Implementar la consulta y formulario mínimos de US-004, consumiendo C-06 y los contratos de pacientes/recursos que necesita. La configuración persistida de US-005 se consume sin inventar su valor ni un permiso de edición.
  - Incorporar para US-005 la vía de configuración del intervalo por el equipo que se acuerde en PA-01/PA-05, consumiendo C-05. Acordar su presentación y permisos antes de implementarla; no atribuir esa operación a recepción u odontólogos por defecto.
  - Incorporar vistas diaria y semanal del sillón, duración efectiva y bloqueos; recepción selecciona período y sillón y/o profesional, y visualiza el rechazo si afecta turnos existentes, consumiendo C-15. Resolver PA-19 antes de ofrecer modificación o desbloqueo.
  - Mostrar duración efectiva, recursos e información de validación/error aprobada; la protección efectiva de agenda permanece en backend.
  - Tests: recorrido de alta válida, conflictos de profesional/sillón/intervalo y errores de API; verificar los estados visuales acordados y acceso interno. Comprobar que un operador autorizado puede configurar el intervalo por la vía acordada y que la agenda utiliza el valor persistido.
- **Dependencias**: C-06
- **Governance**: MEDIO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §Acceso del paciente y del equipo
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-004 y US-005
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 3 · Crear un turno
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Operación y diseño visual

### [C-11] `interfaz-pacientes-y-ficha`
- **Estado**: `[ ]` pendiente
- **Responsable**: Fabi
- **Scope**: Operación mínima de pacientes y ficha clínica sencilla en la interfaz.
  - Resolver el gate visual y compartir contratos/build de frontend con C-10 mediante un propietario común; esta ficha no depende del alta de turnos. La primera de las dos fichas visuales que se ejecute aporta la base y superficie de ingreso mínimas acordadas con C-03; la siguiente las reutiliza.
  - Implementar US-001, US-002 y US-003 con los campos y operaciones mínimos de C-04; conservar lectura de recepción y edición clínica exclusiva de odontólogos.
  - Consumir el acceso de C-03. Si C-10 aún no existe, incluir solo la estructura de interfaz compartida imprescindible y transferir su contrato al otro item para evitar duplicación.
  - Tests: consulta de ficha por ambos perfiles y edición habilitada/denegada según permiso; no confiar exclusivamente en controles visuales para la autorización del backend.
- **Dependencias**: C-04
- **Governance**: ALTO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §RBAC y matriz de permisos
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §Épica 1 · Pacientes y ficha clínica
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 1 y Flujo 2
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Operación y diseño visual

### [C-12] `interfaz-reprogramacion-y-cancelacion`
- **Estado**: `[ ]` pendiente
- **Responsable**: Luciano
- **Scope**: Acciones de reorganización de una reserva desde la agenda existente.
  - Resolver el gate visual y consumir C-07/C-08 para US-006 y US-007, sin redefinir reglas o transiciones.
  - Permitir modificar los campos aprobados al reprogramar y mostrar prestación/duración conservadas; presentar el resultado acordado de conflictos o fallos.
  - Mostrar la cancelación y actualización del horario liberado según el contrato aprobado.
  - Tests: reprogramación válida, error de disponibilidad, campos preservados y cancelación con horario nuevamente disponible.
- **Dependencias**: C-07, C-08, C-10
- **Governance**: MEDIO
- **Leer antes**:
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md) §Agenda
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-006 y US-007
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 4 y Flujo 5
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Operación y diseño visual

### [C-13] `interfaz-confirmaciones`
- **Estado**: `[ ]` pendiente
- **Responsable**: Luciano
- **Scope**: Operación visual del seguimiento manual por recepción.
  - Resolver el gate visual e incorporar listado y acción de confirmación de US-008 consumiendo C-09 dentro de la agenda de C-10.
  - Mostrar datos y resultados acordados sin incorporar canales de envío ni automatización.
  - Tests: permisos de recepción, pertenencia al listado y resultado del registro, incluidos errores definidos en el contrato backend.
- **Dependencias**: C-09, C-10
- **Governance**: MEDIO
- **Leer antes**:
  - [03 Actores y roles](knowledge-base/03_actores_y_roles.md) §RBAC y matriz de permisos
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md) §US-008
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md) §Flujo 6 · Registrar una confirmación manual
  - [08 Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md) §Operación y diseño visual

### [C-23] `interfaz-cancelacion-por-enlace`
- **Estado**: `[ ]` pendiente
- **Responsable**: Luciano
- **Scope**: Superficie del paciente para US-013, consumiendo el acceso limitado de C-18.
  - Resolver el gate visual y contrato de C-18; mostrar turno y acción permitida sin conceder autorreserva ni acceso clínico.
  - Presentar éxito, plazo vencido, enlace inválido y resultados acordados. No ofrecer cancelación cuando venció el plazo ni dar acceso a otros turnos.
  - Tests del recorrido desde enlace, límite de 48 horas según contrato, liberación y estados visuales aprobados; verificar responsive según política elegida.
- **Dependencias**: C-18, C-10
- **Governance**: ALTO
- **Leer antes**:
  - [Actores y roles](knowledge-base/03_actores_y_roles.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md)

### [C-24] `interfaz-cuenta-y-lista-de-espera`
- **Estado**: `[ ]` pendiente
- **Responsable**: Sofia
- **Scope**: Acceso y gestión visual de la inscripción propia para US-014.
  - Resolver el gate visual y consumir ingreso/vínculo de C-16 y operaciones C-21; no redefinir registro o permisos desde la UI.
  - Presentar prestación, fechas, franjas y profesional opcional. Mostrar que la inscripción recibe avisos y no reserva el hueco.
  - Tests: acceso propio, preferencias y errores aprobados; comprobar que controles visuales y backend respetan el mismo contrato. Reutilizar frontend vanilla y base de C-10.
- **Dependencias**: C-21, C-10
- **Governance**: ALTO
- **Leer antes**:
  - [Actores y roles](knowledge-base/03_actores_y_roles.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md)

### [C-25] `interfaz-asignacion-de-huecos`
- **Estado**: `[ ]` pendiente
- **Responsable**: Giovanni
- **Scope**: Superficie de recepción para US-015 y asignación de C-22.
  - Resolver el gate visual y representar interesados y disponibilidad según contrato aprobado, sin inventar prioridad o selección automática.
  - Permitir que recepción confirme quién toma el hueco; reflejar reserva creada y conflicto si deja de estar disponible. Acordar el mensaje y tratamiento de avisos previos en PA-21.
  - Tests: asignación válida por recepción, permiso denegado, hueco ocupado y disponibilidad actualizada; comprobar que un aviso solo no cree una reserva.
- **Dependencias**: C-22, C-10
- **Governance**: MEDIO
- **Leer antes**:
  - [Actores y roles](knowledge-base/03_actores_y_roles.md)
  - [Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [Flujos principales](knowledge-base/07_flujos_principales.md)
  - [Arquitectura propuesta](knowledge-base/08_arquitectura_propuesta.md)


## FASE 5 — Integración reproducible

### [C-14] `integracion-y-ejecucion-reproducible`
- **Estado**: `[ ]` pendiente
- **Responsable**: Giovanni
- **Scope**: Comprobar la combinación de los items del MVP y preparar su ejecución local.
  - Integrar contratos backend/frontend y persistencia que ya fueron aprobados, resolviendo diferencias sin ampliar el alcance ni fijar nuevas reglas de negocio.
  - Ejecutar la suite integrada y los recorridos de agenda, pacientes/ficha y confirmaciones; verificar las reglas RN vigentes y US-001 a US-015, incluidos paciente, bloqueos, emails y lista de espera.
  - Verificar exclusión concurrente con PostgreSQL, rechazo de edición clínica para recepción, duración/intervalo, bloqueos, cancelación por enlace hasta 48 horas, recordatorio a 24 horas sin opción vencida, inscripción propia y asignación por recepción, con evidencia acorde a los specs efectivamente implementados.
  - Demostrar email de creación, recordatorio y aviso de hueco con envíos reales a direcciones de prueba autorizadas del equipo y proveedor gratuito previamente elegido en el change correspondiente; registrar evidencia sin credenciales ni direcciones privadas en capturas públicas. Probar fallos y duplicados según políticas acordadas, y que un aviso no adjudique el hueco.
  - Completar README reproducible, configuración de ejemplo sin secretos y datos ficticios; documentar limitaciones. Preparar evidencia de Verify/Archive del change académico elegido sin inventar estados de OpenSpec.
  - Conservar PA-10/PA-11 y la evidencia académica como pendientes humanos hasta que los integrantes la aporten; no redactar su reflexión personal ni declarar sus verificaciones como realizadas.
  - Este item corresponde al MVP completo. Si la entrega se limita al módulo de C-01, su README, pruebas y archivo se resuelven dentro de C-01 sin exigir implementar los demás items del backlog.
- **Dependencias**: C-11, C-12, C-13, C-19, C-20, C-23, C-24, C-25
- **Governance**: ALTO
- **Leer antes**:
  - [01 Visión y objetivos](knowledge-base/01_vision_y_objetivos.md) §Métricas de éxito
  - [05 Reglas de negocio](knowledge-base/05_reglas_de_negocio.md)
  - [06 Funcionalidades](knowledge-base/06_funcionalidades.md)
  - [07 Flujos principales](knowledge-base/07_flujos_principales.md)
  - [10 Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) §PA-10, PA-11 y PA-13
