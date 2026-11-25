# Visión y objetivos

## Propósito del sistema

Organizar los turnos de un consultorio odontológico de una sede con varios profesionales, evitando solapamientos de profesional y sillón y ayudando a recepción a seguir confirmaciones, inasistencias y cancelaciones.

El usuario eligió ambos problemas como prioritarios, sin ordenarlos entre sí. El MVP incluye una ficha clínica sencilla con antecedentes y notas. Al consolidar esta KB no existía una implementación; el estado actual consta en el [roadmap](../CHANGES.md). No hay evidencia de reducción efectiva de ausencias.

## Objetivos por actor

| Actor | Objetivo principal | Objetivos complementarios |
| --- | --- | --- |
| Recepción/administración | Gestionar la agenda, los bloqueos y la asignación de huecos | Gestionar datos administrativos, registrar confirmaciones manuales y consultar la ficha clínica |
| Odontólogos | Organizar su atención y mantener antecedentes y notas | Operar la agenda respetando la disponibilidad de los recursos |
| Paciente | Recibir las comunicaciones de sus turnos y cancelar por enlace dentro del plazo | Gestionar su inscripción en lista de espera con una cuenta y recibir avisos de huecos |

## Alcance del MVP aprobado

- Una sede, varios odontólogos y sillones/boxes; cantidades concretas pendientes.
- Gestión de pacientes y ficha clínica sencilla.
- Creación y reprogramación por el equipo. El equipo puede cancelar; el paciente cancela mediante enlace hasta 48 horas antes. El límite de 48 horas corresponde al paciente y no fija una restricción adicional para el equipo.
- Profesional y sillón obligatorios y sin ocupación simultánea por otro turno.
- Duración por prestación ajustable al crear y conservada al reprogramar.
- Intervalo configurable común para la sede, sin valor inicial definido.
- Agenda de sillón diaria y semanal con duración variable. Recepción bloquea sillones y/o profesionales durante un período. Si el bloqueo afecta turnos existentes, se rechaza.
- Email con enlace al crear el turno y recordatorio automático 24 horas antes, sin opción de cancelar cuando el plazo venció.
- Lista de espera desde una cuenta, con prestación, rango de fechas, franjas horarias y profesional opcional. Al liberarse un hueco, se avisa por email a los interesados y recepción confirma quién lo toma.
- Listado de turnos por confirmar y seguimiento manual por recepción.
- Lectura clínica para recepción y lectura/edición para odontólogos.

## Fuera de alcance

Autorreserva general del paciente, adjudicación automática de huecos, WhatsApp automático, odontograma, pagos, facturación, sistema multiempresa y expansión multisede quedan fuera del MVP. El email es obligatorio, con servicio gratuito por definir y envíos reales a las direcciones de prueba indicadas por el equipo.

El MVP describe el backlog del producto. La entrega académica requiere un único change completado por OPSX, no implementar el MVP entero a la vez. Giovanni eligió C-01 como primer change y aprobó su propuesta y aplicación el 19/11/2026.

## Métricas de éxito

Como evidencia de cumplimiento se verificarán escenarios de disponibilidad conjunta, intervalo, bloqueos, cancelación, reprogramación, permisos clínicos y de cuenta, plazos de email y asignación de huecos. Las pruebas concretas y sus datos se definirán en el spec aprobado del change.

Reducir inasistencias es un objetivo, no una mejora porcentual prometida. La línea de base y el método de medición siguen pendientes; no se inventan tasas de ausentismo ni metas numéricas.

## Relación con el Discovery

La investigación de 15 sistemas encontró funciones de agenda, clínica y comunicación repetidas. La propuesta elegida combina las reglas de recursos, el seguimiento manual y los flujos de email, cancelación y lista de espera. No se presenta ese enfoque como una exclusividad del mercado ni como demanda validada en entrevistas con consultorios.

Competidores seleccionados para análisis posterior: Dentalink, DentApp, Dentify, Dentidesk y AgendaPro. Referencias UX: Dentalink, AgendaPro y Dentrix. La selección de requisitos surge de las respuestas del usuario.

## Fuentes y navegación

Fuentes. [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md) · [Discovery aprobado](../docs/discovery/discovery.md), puntos 1 a 6. Decisiones estructuradas: [respuestas aprobadas](../docs/discovery/decisiones-aprobadas.json). Evidencia de mercado: [informe, secciones C y D](../docs/discovery/informe-discovery.md).

Continuar con [actores y permisos](03_actores_y_roles.md), [funcionalidades](06_funcionalidades.md) y [preguntas abiertas](10_preguntas_abiertas.md).
