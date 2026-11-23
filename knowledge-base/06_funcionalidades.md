# Funcionalidades

Las historias describen el MVP aprobado para el roadmap. Giovanni eligió C-01 para la entrega académica (PA-13 resuelta); su dominio cubre parcialmente US-004 y la regla de intervalo de US-005, sin completar todos los recorridos de estas historias.

## Épica 1 · Pacientes y ficha clínica

### US-001 · Gestionar pacientes

Como integrante del equipo, quiero gestionar pacientes y consultar sus datos para organizar su atención.

- [ ] Recepción puede crear, consultar, editar y buscar pacientes en todo el sitio; odontología puede realizar esas operaciones dentro del alcance de pacientes asignados. La búsqueda admite código exacto y nombre/apellido parcial sin distinguir mayúsculas.
- [ ] Los datos mínimos son código, nombre, apellido y email obligatorios, más teléfono opcional. El email tiene formato válido y no es único.
- [ ] Recepción asigna y quita uno o más profesionales por paciente. Un paciente creado por odontología queda sin asignación hasta que recepción lo asigne.
- [ ] Los ejemplos y pruebas de la gestión usan datos ficticios.

La creación, consulta, edición y búsqueda no conceden eliminación. El permiso de datos administrativos se verifica en el backend y respeta el alcance por registro (resolución acotada de PA-05/06). Regla relacionada RN-TR-01.

### US-002 · Consultar la ficha clínica

Como integrante de recepción u odontólogo, quiero consultar los antecedentes y notas para contar con la información clínica disponible.

- [ ] Recepción puede leer los antecedentes y las notas de cualquier paciente del sitio.
- [ ] Los odontólogos pueden leer antecedentes y notas solo de pacientes asignados a su profesional.
- [ ] La ficha se crea al registrar los primeros antecedentes o la primera nota; antecedentes se guardan como texto y las notas como entradas fechadas con autor.

La ficha se consulta separada de los resultados administrativos de búsqueda. El alcance clínico por registro queda resuelto para C-04 en PA-05/07. Reglas relacionadas RN-CL-01 y RN-CL-02.

### US-003 · Editar antecedentes y notas

Como odontólogo, quiero editar antecedentes y notas para mantener al día la ficha clínica.

- [ ] Un odontólogo asignado puede crear/editar antecedentes y agregar notas.
- [ ] Solo el odontólogo autor puede editar el texto de una nota y solo mientras siga asignado al paciente.
- [ ] Una persona de recepción puede leer, pero no editar antecedentes ni notas.
- [ ] No se permite eliminar pacientes, fichas, antecedentes ni notas.

La autoría y fecha de creación de una nota las fija el backend y permanecen inmutables. Reglas relacionadas RN-CL-01 y RN-CL-02.

## Épica 2 · Agenda interna

### US-004 · Crear un turno

Como integrante del equipo del consultorio, quiero reservar un turno para un paciente con profesional y sillón disponibles para organizar su atención.

- [ ] El equipo del consultorio crea el turno para la sede.
- [ ] El turno requiere un profesional y un sillón o box.
- [ ] La duración se obtiene de la prestación elegida y el equipo puede ajustarla durante el alta.
- [ ] Un solapamiento del profesional impide crear el turno aunque el sillón esté libre.
- [ ] Un solapamiento del sillón impide crear el turno aunque el profesional esté libre.
- [ ] El alta respeta el intervalo común de la sede entre turnos.

Quedan pendientes la aplicación del intervalo, los criterios de disponibilidad, los permisos detallados y la operación del catálogo (PA-01, PA-04, PA-05 y PA-14). Reglas relacionadas RN-AG-01 a RN-AG-05.

### US-005 · Disponer de un intervalo común configurable

Como equipo del consultorio, quiero que el intervalo entre turnos pueda configurarse para que la agenda respete una misma regla en toda la sede.

- [ ] La sede tiene un único intervalo configurable.
- [ ] La agenda aplica ese intervalo al verificar reservas.

Quedan pendientes el valor inicial, los límites y los permisos de configuración (PA-01). Regla relacionada RN-AG-04.

### US-006 · Reprogramar un turno

Como integrante del equipo del consultorio, quiero cambiar la asignación de un turno para reorganizar la agenda.

- [ ] Se pueden cambiar la fecha, la hora, el profesional y el sillón.
- [ ] La nueva asignación requiere disponibilidad de ambos recursos y respeta el intervalo de la sede.
- [ ] Tras reprogramar, se conservan la prestación y la duración efectiva que tenía el turno.

Quedan pendientes los estados habilitados, los permisos detallados y el comportamiento ante un fallo (PA-02, PA-03, PA-04 y PA-05). Reglas relacionadas RN-AG-02 a RN-AG-04 y RN-AG-06.

### US-007 · Cancelar un turno

Como integrante del equipo del consultorio, quiero cancelar un turno para permitir que recepción vuelva a disponer del horario.

- [ ] La cancelación libera de inmediato el horario reservado.
- [ ] Recepción puede usar el horario liberado al crear otro turno que cumpla las reglas de agenda.

Quedan pendientes los estados, las transiciones y el permiso de cancelación por operación (PA-02 y PA-05). Reglas relacionadas RN-AG-02 a RN-AG-04 y RN-AG-07.

## Épica 3 · Seguimiento manual

### US-008 · Consultar pendientes y registrar confirmaciones

Como integrante de recepción, quiero consultar los turnos por confirmar y registrar las confirmaciones manualmente para dar seguimiento a las posibles ausencias y cancelaciones.

- [ ] Recepción puede consultar un listado de turnos por confirmar.
- [ ] Recepción puede registrar manualmente la confirmación de un turno.

Quedan pendientes los estados que determinan el listado y la transición o representación de la confirmación (PA-02 y PA-07). Regla relacionada RN-AG-08.

## Épica 4 · Agenda de sillón y bloqueos

### US-009 · Consultar la agenda de sillón

Como integrante del equipo, quiero ver los turnos de un sillón por día y por semana para conocer su ocupación.

- [ ] La agenda del sillón o box tiene vista diaria y semanal.
- [ ] Ambas vistas representan la duración efectiva de cada turno.

Regla relacionada RN-AG-09.

### US-010 · Bloquear recursos durante un período

Como integrante de recepción, quiero bloquear un sillón, un profesional o ambos durante un período para reflejar su indisponibilidad.

- [ ] Recepción puede elegir los recursos afectados y el período del bloqueo.
- [ ] Si el bloqueo afecta a un turno existente, se rechaza.
- [ ] Un bloqueo vigente impide asignar el recurso afectado durante el período.

Quedan pendientes el tratamiento de límites y la gestión de desbloqueos (PA-04 y PA-19). Regla relacionada RN-AG-10.

## Épica 5 · Email de turnos y cancelación por enlace

### US-011 · Recibir un email al crear un turno

Como paciente, quiero recibir un email al crearse mi turno para disponer del enlace de cancelación.

- [ ] La creación del turno da lugar al email con enlace de cancelación.
- [ ] La cancelación mediante el enlace queda sujeta al plazo de 48 horas.
- [ ] Los envíos de demostración llegan a las direcciones de prueba indicadas por el equipo mediante un servicio gratuito.

Quedan pendientes el proveedor y el tratamiento de altas cercanas al turno, destinatarios, duplicados y fallos (PA-15, PA-17 y PA-18). Reglas relacionadas RN-PA-02, RN-EM-01 y RN-EM-03.

### US-012 · Recibir un recordatorio

Como paciente, quiero recibir un recordatorio automático por email 24 horas antes para tener presente mi turno.

- [ ] El recordatorio se envía automáticamente 24 horas antes del turno.
- [ ] Si venció el plazo de cancelación, el recordatorio se envía sin opción de cancelar.
- [ ] Recepción conserva el registro manual de la confirmación.

Queda pendiente el tratamiento de reprogramaciones, cancelaciones, altas cercanas al turno y entregas fallidas (PA-18). Reglas relacionadas RN-AG-08 y RN-EM-02.

### US-013 · Cancelar un turno desde el enlace

Como paciente, quiero cancelar mi turno desde el enlace del email para liberar el horario sin llamar.

- [ ] El paciente puede cancelar su turno por enlace dentro del plazo aprobado, hasta 48 horas antes del turno.
- [ ] Vencido el plazo, el enlace no permite cancelar.
- [ ] Una cancelación exitosa libera inmediatamente el horario.

Quedan pendientes la evaluación exacta del límite y el tratamiento de enlaces vencidos, reutilizados o afectados por cambios del turno (PA-17 y PA-18). Reglas relacionadas RN-AG-07, RN-PA-01 y RN-PA-02.

## Épica 6 · Lista de espera

### US-014 · Gestionar la inscripción en lista de espera

Como paciente, quiero gestionar mi inscripción desde una cuenta e indicar mis preferencias para recibir avisos de huecos compatibles.

- [ ] El paciente usa una cuenta para gestionar su propia inscripción.
- [ ] La inscripción permite indicar prestación, rango de fechas y franjas horarias.
- [ ] El profesional de preferencia es opcional.

Quedan pendientes el acceso, la vinculación de la cuenta, los permisos detallados y el ciclo de la inscripción (PA-16 y PA-20). Reglas relacionadas RN-PA-01 y RN-LE-01.

### US-015 · Avisar de huecos y asignarlos desde recepción

Como integrante de recepción, quiero que el sistema avise a pacientes interesados y poder confirmar quién toma el hueco para volver a ocuparlo.

- [ ] Al liberarse un hueco, los interesados con preferencias compatibles reciben un aviso automático por email.
- [ ] Recibir el aviso no reserva el hueco.
- [ ] Recepción confirma a quién se asigna y aplica las validaciones de agenda al crear el turno.

Quedan pendientes los criterios de compatibilidad, la disponibilidad al asignar y el manejo de un hueco ocupado tras un aviso (PA-20 y PA-21). Reglas relacionadas RN-AG-07, RN-LE-02 y RN-LE-03.

Fuentes y referencias. [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md) · [Discovery, secciones 2, 3, 5 y 7](../docs/discovery/discovery.md) · [Reglas de negocio](05_reglas_de_negocio.md) · [Flujos principales](07_flujos_principales.md) · [Preguntas abiertas](10_preguntas_abiertas.md)
