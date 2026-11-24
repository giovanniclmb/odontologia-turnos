# Reglas de negocio

## Agenda

| Código | Regla | Evidencia |
| --- | --- | --- |
| RN-AG-01 | La agenda del MVP cubre una sede con varios odontólogos. El equipo del consultorio crea todos los turnos. | [Discovery, secciones 1, 2 y 5](../docs/discovery/discovery.md) |
| RN-AG-02 | Al asignar un turno se requieren un profesional y un sillón o box. | [Decisiones aprobadas, `rules.professional_required` y `rules.chair_required`](../docs/discovery/decisiones-aprobadas.json) |
| RN-AG-03 | El profesional y el sillón o box deben estar disponibles durante el horario reservado. Un turno que ya ocupa el mismo profesional o el mismo sillón durante ese horario impide la asignación. | [Discovery, sección 7](../docs/discovery/discovery.md) · [Decisiones aprobadas, `rules.overlap_allowed`](../docs/discovery/decisiones-aprobadas.json) |
| RN-AG-04 | Entre turnos debe respetarse un intervalo configurable único para toda la sede. El valor inicial sigue pendiente y figura como `null` en las decisiones aprobadas. | [Discovery, sección 7](../docs/discovery/discovery.md) · [Decisiones aprobadas, `rules.buffer_scope` y `rules.initial_buffer_minutes`](../docs/discovery/decisiones-aprobadas.json) |
| RN-AG-05 | La duración parte de la prestación y el equipo puede ajustarla al crear el turno. | [Decisiones aprobadas, `rules.duration`](../docs/discovery/decisiones-aprobadas.json) |
| RN-AG-06 | Al reprogramar se pueden cambiar la fecha, la hora, el profesional y el sillón, previa validación de disponibilidad. Se conservan la prestación y la duración efectiva. | [Discovery, sección 7](../docs/discovery/discovery.md) · [Decisiones aprobadas, `rules.reschedule_mutable` y `rules.reschedule_preserved`](../docs/discovery/decisiones-aprobadas.json) |
| RN-AG-07 | La cancelación interna cambia un turno pendiente y todavía futuro a `CANCELADO`, conserva su registro visible y libera profesional y sillón al confirmar la transacción. Recepción tiene alcance global; odontología solo cancela turnos propios de pacientes con asignación activa. Se exige la versión vigente; una versión anterior o una cancelación repetida se rechazan sin modificar el turno. El horario puede reutilizarse sujeto a las reglas de agenda. | [Decisiones aprobadas, `rules.cancel_effect`](../docs/discovery/decisiones-aprobadas.json) · [Contrato aprobado de C-08](../openspec/changes/cancelar-y-liberar-horario/specs/agenda-persistida/spec.md) |
| RN-AG-08 | Recepción consulta los turnos por confirmar y se encarga del registro de confirmaciones y del seguimiento manual. | [Discovery, secciones 3, 5 y 7](../docs/discovery/discovery.md) · [Decisiones aprobadas, `rules.confirmation`](../docs/discovery/decisiones-aprobadas.json) |
| RN-AG-09 | La agenda de sillón o box se puede consultar por día y por semana, con la duración efectiva de cada turno. | [Ampliación, puntos 5 y 7](../docs/discovery/ampliacion-paciente.md#resumen-de-discovery-para-aprobación-de-la-revisión) |
| RN-AG-10 | Recepción puede bloquear un sillón o box, un profesional o ambos durante un período. El bloqueo impide asignar los recursos afectados en ese período. Si afecta turnos existentes, se rechaza. | [Ampliación, punto 7](../docs/discovery/ampliacion-paciente.md) |

## Acceso y cancelación del paciente

| Código | Regla | Evidencia |
| --- | --- | --- |
| RN-PA-01 | El paciente puede cancelar su turno desde el enlace recibido y usa una cuenta para gestionar su inscripción en lista de espera. | [Ampliación, puntos 2 y 3](../docs/discovery/ampliacion-paciente.md) |
| RN-PA-02 | El paciente puede cancelar por enlace hasta 48 horas antes del turno. La cancelación libera inmediatamente el horario según RN-AG-07. | [Ampliación, punto 7](../docs/discovery/ampliacion-paciente.md) |

## Email

| Código | Regla | Evidencia |
| --- | --- | --- |
| RN-EM-01 | Al crear el turno, se envía al paciente un email con el enlace de cancelación sujeto a RN-PA-02. | [Ampliación, punto 7](../docs/discovery/ampliacion-paciente.md) |
| RN-EM-02 | Se envía un recordatorio automático 24 horas antes del turno. Cuando venció el plazo de cancelación, el recordatorio se envía sin opción de cancelar. La confirmación sigue a cargo de recepción (RN-AG-08). | [Ampliación, punto 7](../docs/discovery/ampliacion-paciente.md) |
| RN-EM-03 | La evidencia de email en la entrega usa envíos reales a direcciones de prueba indicadas por el equipo. El servicio debe ser gratuito y su selección está pendiente. | [Ampliación, punto 8](../docs/discovery/ampliacion-paciente.md) |

## Lista de espera

| Código | Regla | Evidencia |
| --- | --- | --- |
| RN-LE-01 | La inscripción registra prestación, rango de fechas, franjas horarias y, cuando el paciente lo indica, profesional de preferencia. | [Ampliación, punto 7](../docs/discovery/ampliacion-paciente.md) |
| RN-LE-02 | Al liberarse un hueco, se avisa automáticamente por email a los interesados con preferencias compatibles. | [Ampliación, puntos 3 y 5](../docs/discovery/ampliacion-paciente.md) |
| RN-LE-03 | Recepción confirma quién toma el hueco y asigna el turno sujeto a las reglas de disponibilidad de la agenda. El aviso carece de efecto de reserva. | [Ampliación, puntos 3, 6 y 7](../docs/discovery/ampliacion-paciente.md) |

## Información clínica

| Código | Regla | Evidencia |
| --- | --- | --- |
| RN-CL-01 | La ficha clínica sencilla comprende antecedentes y notas. | [Discovery, sección 5](../docs/discovery/discovery.md) |
| RN-CL-02 | Recepción puede leer la ficha clínica. Los odontólogos pueden leerla y son los únicos que pueden editar los antecedentes y las notas. | [Discovery, secciones 2 y 7](../docs/discovery/discovery.md) · [Decisiones aprobadas, `roles`](../docs/discovery/decisiones-aprobadas.json) |

## Restricciones transversales

| Código | Regla | Evidencia |
| --- | --- | --- |
| RN-TR-01 | Los ejemplos, pruebas y capturas usan datos clínicos ficticios. Los envíos reales de demostración se limitan a direcciones de prueba indicadas por el equipo. | [Discovery, sección 9](../docs/discovery/discovery.md) |

## Especificación pendiente

El proveedor, las cuentas, los enlaces, los casos límite de envío, los desbloqueos y la compatibilidad de huecos se detallan en PA-15 a PA-21. El intervalo inicial y su aplicación (PA-01), los estados del turno (PA-02), el resultado de una reprogramación fallida (PA-03), los horarios y la disponibilidad (PA-04), los permisos detallados (PA-05) y los campos clínicos (PA-06) siguen abiertos. [Preguntas abiertas](10_preguntas_abiertas.md)


## RN-AC-01 — Cambio propio de contraseña (DD-14)

Un usuario interno activo puede cambiar únicamente su propia contraseña confirmando la vigente. Se rechaza reutilizar la actual o cualquiera de las cuatro anteriores. La operación conserva historial y credencial vigente de forma atómica; un rechazo no los modifica. Se vuelve a verificar el estado activo y se serializan cambios concurrentes por usuario. No autoriza recuperación ni administración de credenciales ajenas.
