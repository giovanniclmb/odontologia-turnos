# Preguntas abiertas

## Inconsistencias y límites de la evidencia

- **IN-01 · Tamaño del equipo.** La consigna fija un máximo general de tres integrantes. El usuario informó una excepción docente para el equipo de cuatro, cuya composición puede variar. La declaración está registrada en Discovery. Su respaldo para la entrega, si lo requiere la cátedra, se considera en PA-10. [Discovery, sección 9](../docs/discovery/discovery.md)
- **IN-02 · Precios y planes de competidores.** En tab32, Clinic Cloud y Dentify, la apertura de fuentes oficiales corrigió cifras o condiciones de extracciones previas. El 25/11/2026 una segunda comprobación del agente actualizó o anotó fichas de ocho proveedores y cuatro celdas de la matriz; ese día se registró además la revisión personal de cinco fuentes. El informe conserva los valores observados y fechados, sujetos a corroboración en PA-12. [Informe, verificación de fuentes](../docs/discovery/informe-discovery.md#verificación-de-fuentes)
- El valor inicial del intervalo figura como `null` en las decisiones y en el estado de Discovery. Ese registro expresa la falta de un valor acordado. La pregunta sigue en PA-01. [Decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json) · [Estado de Discovery](../.active-orchestrator-state.json)

## Preguntas priorizadas

La columna de consulta identifica a quién le correspondería resolver cada pregunta. Las prioridades son de trabajo y pueden variar según el primer change elegido.

| ID | Prioridad | Pregunta | Afecta | Consulta |
| --- | --- | --- | --- | --- |
| PA-01 | Alta | ¿Cuántos minutos tendrá inicialmente el intervalo global, qué límites y permisos tendrá su configuración y cómo se aplicarán los cambios de valor a la agenda? | Configuración y validación de turnos | Usuario y equipo |
| PA-02 | Alta | ¿Cuáles son los estados exactos y sus transiciones? ¿Qué estados ocupan recursos, qué identifica a un turno por confirmar y qué efecto tiene el registro manual de confirmación? | Agenda, cancelación y seguimiento | Usuario y equipo |
| PA-03 | Alta | ¿Qué sucede con el turno original, su reserva y el aviso al operador si una reprogramación falla por falta de disponibilidad u otro error? | Reprogramación | Usuario y equipo |
| PA-04 | Alta | ¿Qué horarios, excepciones y criterios de disponibilidad se aplican a la sede y a sus recursos? ¿Cómo se evalúan las citas contiguas, el intervalo y las solicitudes simultáneas? | Agenda y prevención de solapamientos | Usuario y equipo |
| PA-05 | Alta | ¿Cómo se autentica el equipo y qué alcance tiene cada rol por operación y por registro, en particular para agenda, gestión de pacientes y ficha clínica? | Acceso y protección de información clínica | Usuario y equipo |
| PA-06 | Alta | ¿Qué campos mínimos, obligatoriedad y validaciones necesitan los datos de paciente, los antecedentes y las notas clínicas? | Gestión de pacientes y ficha | Usuario y equipo |
| PA-07 | Media | ¿Qué identificadores, cardinalidades y representación de persistencia necesita el modelo al especificar cada operación? | Modelo y persistencia en PostgreSQL | Equipo técnico |
| PA-08 | Media | ¿Cuántos profesionales y sillones tendrá el consultorio de referencia y con qué datos ficticios iniciales se probará? | Configuración y escenarios de agenda | Usuario y equipo |
| PA-09 | Media | ¿Qué versiones concretas de Java, Spring Boot, PostgreSQL y herramientas de frontend se usarán? ¿Cómo se ejecutará el sistema y dónde se alojará PostgreSQL? | Entorno y puesta en marcha | Equipo técnico |
| PA-10 | Alta | Equipo y reparto confirmados: Giovanni, Sofia, Luciano y Fabi; asignación de 25 changes aprobada. ¿Cómo se organizarán la defensa y el respaldo de la excepción docente si la cátedra lo requiere? | Coordinación de la entrega | Integrantes y cátedra |
| PA-11 | Resuelta | Giovanni, Sofia, Fabi y Luciano revisaron cinco fuentes; los hallazgos se registraron el 25/11/2026 en el informe y en docs/entrega. | Evidencia exigida por la consigna | Integrantes |
| PA-12 | Media | ¿Qué fuentes o demos permitirán comprobar ARCA operativa, WhatsApp Business API efectiva, disponibilidad conjunta bajo concurrencia y alcance de la exportación clínica, además de precios y condiciones? | Contraste competitivo posterior | Integrantes |
| PA-13 | Resuelta | Giovanni eligió C-01 y aprobó su propuesta y Apply el 19/11/2026. La implementación y Verify no sustituyen Archive ni la evidencia humana. | Alcance de la entrega del 25/11/2026 | Decisión expresa del usuario |
| PA-14 | Media | ¿Qué prestaciones se necesitan para comenzar y cómo se registrará y mantendrá su duración de referencia? | Configuración y alta de turnos | Usuario y equipo |

### Pendientes de la ampliación aprobada

| ID | Prioridad | Pregunta | Afecta | Consulta |
| --- | --- | --- | --- | --- |
| PA-15 | Alta | ¿Qué proveedor gratuito cumple los límites de volumen y las necesidades de entrega? ¿Cómo se verifica el destinatario, qué direcciones de prueba indica el equipo y cómo se controla que el envío de prueba se limite a ellas? | Configuración de email y demostración | Equipo técnico y usuario |
| PA-16 | Alta | ¿Cómo se registra o habilita la cuenta, cómo se verifica y vincula al paciente y cómo se limita el acceso a sus propios datos de lista de espera? | Identidad del paciente y autorización | Usuario y equipo |
| PA-17 | Alta | ¿Cómo se evalúa el instante exacto del corte de 48 horas, incluyendo la referencia horaria aplicable? ¿Qué vigencia y condiciones de uso tiene el enlace y qué ocurre si se reutiliza, vence o el turno cambia? | Cancelación por enlace | Usuario y equipo |
| PA-18 | Alta | ¿Cómo se tratan el email de alta y el recordatorio ante reprogramación, cancelación o creación dentro de las ventanas de 48 o 24 horas? ¿Qué tratamiento reciben los envíos duplicados o fallidos en cada tipo de email? | Programación y entrega de email | Usuario y equipo |
| PA-19 | Alta | ¿Quién puede quitar o cambiar un bloqueo, con qué condiciones y qué ocurre con la disponibilidad resultante? ¿Cómo se validan límites de períodos y solicitudes simultáneas de bloqueo y turno? | Agenda y bloqueos | Usuario y equipo |
| PA-20 | Alta | C-21 tiene aprobadas las operaciones de alta, consulta, edición y baja propias, una inscripción vigente por prestación y el fin de vigencia al finalizar el rango de fechas o al darla de baja. Para C-22 queda pendiente cómo se determina la compatibilidad con preferencias, duración, recursos e intervalo, cómo decide recepción entre interesados y cómo se revalida la disponibilidad al asignar. | Lista de espera y agenda | Usuario y equipo |
| PA-21 | Alta | ¿Qué ocurre con quienes recibieron un aviso si el hueco se ocupa antes de la asignación? ¿Qué información se muestra o comunica y cómo se tratan avisos pendientes o repetidos? | Disponibilidad y avisos de lista de espera | Usuario y equipo |

[Ampliación aprobada y sus pendientes de especificación](../docs/discovery/ampliacion-paciente.md#pendientes-de-especificación-que-no-se-resuelven-por-inferencia)

## Investigación pendiente

La revisión personal de cinco fuentes se registró el 25/11/2026, cada una asociada a su integrante y hallazgo, y aparte de las comprobaciones del agente. Queda a confirmar el alcance de la simulación en AgendaPro. [Planilla y punto a confirmar](../docs/entrega/verificacion-fuentes.md#planilla-personal) · [Informe, verificación de fuentes](../docs/discovery/informe-discovery.md#verificación-de-fuentes)

Para PA-12, «No evidenciado» describe el alcance de las fuentes revisadas para cada proveedor. La integración fiscal local, el uso efectivo de WhatsApp Business API, la concurrencia y la exportación clínica completa requieren evidencia específica. Hay condiciones documentadas que cubren partes del problema, como el Excel estándar y las condiciones de cierre de Dentidesk. La selección para contraste posterior comprende Dentalink, DentApp, Dentify, Dentidesk y AgendaPro. [Informe de Discovery](../docs/discovery/informe-discovery.md) · [Fichas de competidores](../docs/discovery/competidores.json)

Fuentes de las preguntas. [Discovery, secciones 2 y 7 a 11](../docs/discovery/discovery.md) · [Decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json) · [Estado de Discovery](../.active-orchestrator-state.json)

## Resolución acotada para C-01 (19/11/2026)

PA-01/PA-04: separación mínima inclusiva antes y después, para profesional y sillón; intervalo positivo explícito por invocación, sin valor inicial de producción. PA-02: el dominio recibe solo reservas ocupantes; no define estados ni su selección. PA-07: referencias opacas ficticias, sin modelo físico. PA-09: Java 25, Maven y JUnit en backend/; Spring Boot, PostgreSQL y frontend siguen pendientes. Estas precisiones no cierran las preguntas globales.

## Resolución acotada para C-02 (19/11/2026)

PA-07: cuatro referencias independientes, UUID generados por aplicación, código único por tipo; el modelo completo de usuarios, clínica y turnos sigue en los changes correspondientes. PA-09: Java 25, Boot 4.1.2, Maven, JPA/Hibernate, Flyway y PostgreSQL 18.7, Compose local y Testcontainers temporal aprobados. Migraciones consecutivas coordinadas al integrar, V1 inicial, sin out-of-order. Frontend y despliegue productivo siguen pendientes. C-02 implementado/Verify local no lo declara integrado ni cierra PA ajenas.


Aclaración de PA-05 (20/11/2026): DD-14 resuelve el contrato mínimo de cambio de contraseña propia y ventana de cinco; no habilita crear/asignar cuentas, recuperación ni modificar credenciales ajenas. Los permisos restantes continúan pendientes/denegados según C-03.

## Resolución acotada para C-04 (22/11/2026)

- **PA-05 — pacientes y ficha:** recepción y odontología pueden crear, consultar y editar datos administrativos de pacientes, sin permiso de eliminación. Recepción tiene alcance global y asigna/quita profesionales. Odontología solo accede a pacientes asignados a su profesional; el alta hecha por odontología queda sin asignación hasta que recepción la asigne. Recepción lee cualquier ficha del sitio; odontología asignada lee antecedentes y notas y puede editar antecedentes. Solo el odontólogo autor, mientras permanezca asignado, puede editar una nota. No se permite eliminar pacientes, fichas, antecedentes o notas. La búsqueda administrativa es global para recepción y limitada por asignación para odontología.
- **PA-06 — paciente y ficha:** paciente requiere `codigo`, nombre, apellido y email; teléfono es opcional. Nombre, apellido y email no pueden estar vacíos; el email debe tener formato válido pero no es único. El código conserva la comparación exacta y unicidad por tipo ya aprobadas en C-02. Antecedentes son texto libre; las notas requieren texto no vacío y registran autor y fecha de creación asignados por el backend, que permanecen inmutables al editar.
- **PA-07 — pacientes y ficha:** se reutiliza el UUID y código existentes de paciente. La relación paciente-profesional es muchos-a-muchos y admite cero asignaciones; la ficha es cero-a-una y se crea al guardar los primeros antecedentes o la primera nota; las notas son entradas independientes de uno-a-muchos con referencia a paciente y usuario autor. Búsqueda por código exacto y por nombre/apellido parcial sin distinguir mayúsculas. Los contratos de C-04 son de servicio/DTO, sin HTTP. La migración física y sus nombres se especifican en OpenSpec C-04 y se coordinan con la secuencia Flyway al integrar.

Estas decisiones cierran PA-05/06/07 únicamente para el alcance de C-04; no resuelven permisos ni modelos de agenda, cuentas de paciente u otros recursos. Antes de migrar una base persistente, verificar sin exponer filas que la tabla paciente no contenga registros que requieran datos obligatorios no disponibles; no rellenarlos por inferencia.

## Resolución acotada para C-06 (23/11/2026)

- **PA-02 — alta:** el turno nace pendiente de confirmación y ocupa al profesional y al sillón. La confirmación manual permanece en C-09. Las demás transiciones se resuelven en sus changes.
- **PA-04 — horario de alta:** solo se crean turnos con inicio posterior al instante de validación, sin exigir franjas semanales adicionales. C-06 usa el contrato integrado de C-15 para los bordes semiabiertos y el protocolo de concurrencia. La precisión compartida es de microsegundos y el intervalo global se aplica entre turnos.
- **PA-05 — alcance del alta:** recepción puede crear con cualquier profesional elegible del sitio. Odontología solo puede reservar para su propio profesional vinculado y pacientes con asignación activa a él. El alcance de las demás operaciones de agenda se especifica en sus respectivos changes.
- **PA-07 — persistencia de alta:** V7 agrega el turno con UUID de aplicación, referencias obligatorias a paciente, profesional, sillón y prestación, inicio y fin como instantes, duración efectiva en minutos y estado `PENDIENTE`. Giovanni aprobó el servicio interno y Apply de C-06 el 23/11/2026. PA-07 se mantiene abierta en los alcances restantes de agenda. [Diseño](../openspec/changes/archive/2026-11-23-crear-turno-con-persistencia/design.md)

## Resolución acotada para C-15 (23/11/2026)

- **PA-04 y PA-19.** El período del bloqueo incluye el inicio y excluye el fin. Se admiten bordes contiguos y el intervalo global se aplica entre turnos. Una escritura de agenda y un bloqueo incompatibles se coordinan en PostgreSQL por los recursos compartidos. Prevalece la primera transacción que confirma. Un bloqueo de sillón y profesional se confirma de manera atómica.
- **PA-19.** C-15 incluye el alta por recepción y la consulta interna de indisponibilidad. La modificación y el desbloqueo quedan pendientes de definición. El contrato es Java con migración Flyway. C-06 conectará la persistencia de turnos y C-07 conectará la reprogramación al protocolo transaccional.

## Resolución acotada para C-07 (23/11/2026)

- **PA-02 y PA-04 — elegibilidad:** se reprograman turnos pendientes de confirmación que todavía no comenzaron. El inicio original y el destino normalizado deben ser estrictamente futuros al validar. Se conserva el estado, la prestación y la duración efectiva.
- **PA-03 — fallo:** el turno y su ocupación vigentes se conservan ante un rechazo o una reversión. El servicio distingue la disponibilidad insuficiente de los datos inválidos y protege los datos de turnos ajenos.
- **PA-04 — edición concurrente:** si la versión ya cambió, el operador debe volver a consultar. Un destino normalizado idéntico devuelve el turno vigente sin incrementar la versión después de verificar acceso, versión y elegibilidad temporal.
- **PA-05 — alcance:** recepción puede reprogramar turnos elegibles de la sede y cambiar el profesional. Odontología solo puede mover sus propios turnos de pacientes asignados de forma activa y conservar el profesional original.
- Giovanni aprobó el [diseño de servicio interno](../openspec/changes/archive/2026-11-23-reprogramar-turno/design.md) y Apply de C-07 el 23/11/2026. Las decisiones de otras transiciones y operaciones siguen abiertas en sus respectivos changes.

## Resolución acotada para C-08 (24/11/2026)

- **PA-02 y PA-04 — cancelación interna:** solo `PENDIENTE` con inicio estrictamente futuro al validar tras los locks pasa a `CANCELADO`. El registro permanece visible en agenda y no ocupa profesional ni sillón al confirmar. Se conserva el resto de los datos y se incrementa una vez la versión.
- **PA-05 — alcance:** recepción tiene alcance global; odontología solo turnos propios de pacientes con asignación activa a su profesional. El alcance se revalida después de las esperas. Una referencia inexistente es un error para recepción y una denegación genérica para odontología.
- **PA-02 — versión y repetición:** una versión desactualizada se rechaza pidiendo recargar. Con versión vigente, repetir sobre un turno cancelado se rechaza indicando que ya está cancelado. Un rechazo o rollback no modifica los datos ni genera una nueva liberación.
- **Integración:** el resultado devuelve el turno actualizado para identificar recursos y período liberados después del commit, también cuando la confirmación depende de una transacción exterior. C-08 no agrega infraestructura de eventos.
- Giovanni aprobó el [diseño](../openspec/changes/archive/2026-11-24-cancelar-y-liberar-horario/design.md) y el [plan de Apply](../openspec/changes/archive/2026-11-24-cancelar-y-liberar-horario/tasks.md) el 24/11/2026. El corte de 48 horas del enlace de C-18 y el tratamiento de emails/avisos siguen fuera de esta resolución.

## Resolución acotada para C-09 (aprobada por Giovanni, 24/11/2026)

- **PA-02 y PA-05.** Solo recepción consulta y confirma los turnos pendientes de toda la sede. El listado y la confirmación requieren un inicio estrictamente futuro. La confirmación manual cambia el estado a `CONFIRMADO`, conserva la ocupación de ambos recursos, incrementa la versión y rechaza repeticiones.
- **PA-02 y PA-05.** Recepción puede cancelar o reprogramar un turno `CONFIRMADO` cuyo inicio siga estrictamente futuro al validar después de adquirir locks. Odontología conserva su alcance anterior: solo turnos `PENDIENTE` propios de pacientes asignados activamente.
- **PA-02.** Una confirmación, cancelación o reprogramación exige la versión vigente; una versión desactualizada pide volver a consultar sin mutar datos. La confirmación ocupa ambos recursos y aumenta una vez la versión. Repetirla con la versión vigente se rechaza sin cambio.
- **PA-02.** Una reprogramación efectiva de un confirmado lo devuelve a `PENDIENTE`, incrementa la versión y conserva paciente, prestación y duración efectiva. Si el destino normalizado es idéntico, conserva estado y versión. Cancelarlo lo lleva a `CANCELADO`, libera los recursos al commit e incrementa la versión.
- El detalle del contrato interno está en el [delta de agenda archivado](../openspec/changes/archive/2026-11-25-c-09-confirmaciones-manuales/specs/agenda-persistida/spec.md); la migración C-09 debe seguir a V11 de email de C-17.

## Resolución acotada para C-10 (aprobada por Giovanni, 25/11/2026)

- **PA-01 — operación del intervalo:** recepción lo consulta y establece en una sección Configuración propia, visible solo para su rol. Se mantienen las reglas de C-05: entero positivo, sin valor inicial y vigente para validaciones futuras.
- **PA-05 — paciente en la agenda:** recepción ve nombre y apellido en todos los turnos. Odontología los ve en todos sus turnos propios, aunque no tenga asignación activa con el paciente; la excepción no habilita otros datos administrativos ni clínicos.
- **PA-05 y PA-19 — consulta de bloqueos:** recepción ve todos. Odontología ve período y recurso de los que afectan al sillón consultado o a su profesional, sin datos de otros profesionales. Solo recepción crea. Modificación y desbloqueo siguen pendientes en PA-19.
- **PA-04 — presentación horaria:** las vistas diaria y semanal (lunes a domingo) usan `America/Argentina/Mendoza` y no imponen jornada laboral.

## Resolución acotada para C-18 (25/11/2026)

- **PA-17 — confirmación:** el enlace vigente y no usado sigue sirviendo después de confirmar el turno (DD-17). Reprogramar lo invalida.
- **PA-17 — instante del plazo:** el comienzo del turno y el corte de 48 horas se evalúan después de las esperas por otras operaciones, al momento de cancelar. Es la corrección de un defecto sobre la regla ya especificada en C-18.
- **PA-17 — entrega interna:** C-19 recibe el token por un evento posterior al commit (DD-18).
- Quedan sin resolver aquí el envío del enlace y el tratamiento de los emails (PA-18, C-19).

## Resolución acotada para C-19 (25/11/2026)

- **PA-15 — destinatario:** email registrado del paciente; el transporte sigue limitado a las direcciones de prueba.
- **PA-17 — entrega del enlace:** URL base configurable con el token después de `#`; la página corresponde a C-23.
- **PA-18 — email de alta y reprogramación:** se envía en ambas; con menos de 48 horas, sin enlace. Un fallo agotado solo se registra; no hay reenvío posterior. Un mensaje desactualizado antes del envío se descarta. La repetición del mismo aviso se deduplica.
- El recordatorio y su programación siguen abiertos para C-20.

## Resolución acotada para C-20 (25/11/2026)

- **PA-18 — programación:** el backend revisa cada minuto los turnos a 24 horas o menos; si estaba apagado, envía al volver mientras el turno no haya comenzado. El envío automático se activa por configuración y está apagado por defecto.
- **PA-18 — cambios y altas cercanas:** reciben recordatorio los turnos pendientes y confirmados. Un horario creado o reprogramado con menos de 24 horas no lo recibe.
- **PA-18 — contenido y fallos:** nombre, fecha y hora de Mendoza y la aclaración de que ya no puede cancelarse por enlace, al email de la ficha. Un fallo agotado solo se registra.
- Las precisiones sobre cada punto constan en la [propuesta de C-20](../openspec/changes/archive/2026-11-25-recordatorio-automatico-24-horas/proposal.md), aprobada el 25/11/2026.

## Resolución acotada para C-21 (25/11/2026)

Confirmada por Giovanni por menú al revisar el [PR #48](https://github.com/giovanniclmb/odontologia-turnos/pull/48).

- **PA-20 — ciclo de la inscripción:** el paciente vinculado crea, consulta, edita y da de baja sus inscripciones; hay una vigente por prestación; vence al terminar el rango de fechas o con la baja.
- **PA-20 — preferencias:** se admite un rango de un solo día, varias franjas y cambiar la prestación al editar.
- **Referencia horaria:** la vigencia usa `America/Argentina/Buenos_Aires`. Agenda y emails siguen en `America/Argentina/Mendoza`; hoy ambas dan la misma fecha.
- **Acceso del paciente por HTTP:** las rutas y el ingreso se suman a C-24. El diseño de autenticación y sesión se acuerda en su propuesta.
- La compatibilidad de huecos, los avisos y la asignación siguen pendientes para C-22.
