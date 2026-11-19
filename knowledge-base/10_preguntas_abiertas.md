# Preguntas abiertas

## Inconsistencias y límites de la evidencia

- **IN-01 · Tamaño del equipo.** La consigna fija un máximo general de tres integrantes. El usuario informó una excepción docente para el equipo de cuatro, cuya composición puede variar. La declaración está registrada en Discovery. Su respaldo para la entrega, si lo requiere la cátedra, se considera en PA-10. [Discovery, sección 9](../docs/discovery/discovery.md)
- **IN-02 · Precios y planes de competidores.** En tab32, Clinic Cloud y Dentify, la apertura de fuentes oficiales corrigió cifras o condiciones de extracciones previas. El informe conserva los valores observados y fechados, sujetos a corroboración en PA-12. [Informe, verificación de fuentes](../docs/discovery/informe-discovery.md#verificación-de-fuentes)
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
| PA-11 | Alta | ¿Qué integrantes revisarán personalmente al menos cinco fuentes y dónde quedarán registrados sus hallazgos? | Evidencia exigida por la consigna | Integrantes |
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
| PA-20 | Alta | ¿Cómo se determina la compatibilidad del hueco con preferencias, duración, recursos e intervalo? ¿Qué operaciones y vigencia tiene la inscripción, cómo decide recepción entre interesados y cómo se revalida la disponibilidad al asignar? | Lista de espera y agenda | Usuario y equipo |
| PA-21 | Alta | ¿Qué ocurre con quienes recibieron un aviso si el hueco se ocupa antes de la asignación? ¿Qué información se muestra o comunica y cómo se tratan avisos pendientes o repetidos? | Disponibilidad y avisos de lista de espera | Usuario y equipo |

[Ampliación aprobada y sus pendientes de especificación](../docs/discovery/ampliacion-paciente.md#pendientes-de-especificación-que-no-se-resuelven-por-inferencia)

## Investigación pendiente

La revisión personal de al menos cinco fuentes sigue pendiente. El informe registra fuentes candidatas y comprobaciones del agente. Cada revisión personal debe quedar asociada al integrante y a su hallazgo. [Informe, verificación de fuentes](../docs/discovery/informe-discovery.md#verificación-de-fuentes)

Para PA-12, «No evidenciado» describe el alcance de las fuentes revisadas para cada proveedor. La integración fiscal local, el uso efectivo de WhatsApp Business API, la concurrencia y la exportación clínica completa requieren evidencia específica. Hay condiciones documentadas que cubren partes del problema, como el Excel estándar y las condiciones de cierre de Dentidesk. La selección para contraste posterior comprende Dentalink, DentApp, Dentify, Dentidesk y AgendaPro. [Informe de Discovery](../docs/discovery/informe-discovery.md) · [Fichas de competidores](../docs/discovery/competidores.json)

Fuentes de las preguntas. [Discovery, secciones 2 y 7 a 11](../docs/discovery/discovery.md) · [Decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json) · [Estado de Discovery](../.active-orchestrator-state.json)

## Resolución acotada para C-01 (19/11/2026)

PA-01/PA-04: separación mínima inclusiva antes y después, para profesional y sillón; intervalo positivo explícito por invocación, sin valor inicial de producción. PA-02: el dominio recibe solo reservas ocupantes; no define estados ni su selección. PA-07: referencias opacas ficticias, sin modelo físico. PA-09: Java 25, Maven y JUnit en backend/; Spring Boot, PostgreSQL y frontend siguen pendientes. Estas precisiones no cierran las preguntas globales.

## Resolución acotada para C-02 (19/11/2026)

PA-07: cuatro referencias independientes, UUID generados por aplicación, código único por tipo; el modelo completo de usuarios, clínica y turnos sigue en los changes correspondientes. PA-09: Java 25, Boot 4.1.2, Maven, JPA/Hibernate, Flyway y PostgreSQL 18.7, Compose local y Testcontainers temporal aprobados. Migraciones consecutivas coordinadas al integrar, V1 inicial, sin out-of-order. Frontend y despliegue productivo siguen pendientes. C-02 implementado/Verify local no lo declara integrado ni cierra PA ajenas.
