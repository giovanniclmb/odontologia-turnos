# Flujos principales

Los pasos describen interacciones del equipo y del paciente con la aplicación y el servicio de email. La autenticación y los permisos por operación se especificarán en PA-05, PA-16 y PA-17.

## Flujo 1 · Gestionar pacientes y consultar la ficha

Un integrante del equipo necesita organizar la atención de un paciente (US-001 y US-002).

1. El operador busca pacientes por código exacto o por coincidencia parcial de nombre/apellido sin distinguir mayúsculas. Recepción ve el sitio; odontología solo los pacientes asignados a su profesional.
2. Recepción u odontología crea o edita los datos administrativos mínimos dentro de su alcance. Si odontología crea un paciente, queda sin asignar y recepción debe asignarlo para habilitar el acceso posterior.
3. Recepción asigna o quita profesionales. Odontología no cambia asignaciones.
4. Al cargar antecedentes o la primera nota, la aplicación crea la ficha si aún no existe. Una consulta clínica devuelve antecedentes y notas solo a recepción o a odontología asignada.

Los contratos de servicio y campos mínimos de C-04 se especifican en OpenSpec; no se definen rutas HTTP en este change (resolución acotada de PA-05/06/07).

## Flujo 2 · Editar la ficha clínica

Un odontólogo necesita actualizar antecedentes o notas de un paciente (US-003).

1. Un odontólogo asignado consulta la ficha e indica una actualización de antecedentes o agrega una nota no vacía.
2. La aplicación comprueba el permiso y la asignación vigente en backend. Para editar una nota, comprueba también que el usuario sea su autor original.
3. La aplicación guarda el cambio; para notas conserva autor y fecha de creación, asignados por el backend.

Si recepción intenta editar o un odontólogo intenta modificar una nota ajena o una ficha fuera de su alcance, la aplicación deniega la operación en backend. No se ofrece eliminar pacientes, fichas, antecedentes ni notas (RN-CL-02; resolución acotada de PA-05/06/07).

## Flujo 3 · Crear un turno

Un integrante del equipo registra una nueva reserva para un paciente (US-004 y US-005).

1. El operador identifica al paciente y selecciona la prestación, la fecha, la hora, el profesional y el sillón o box.
2. La aplicación obtiene la duración de la prestación. El operador puede ajustarla durante el alta.
3. La aplicación comprueba las reservas, los bloqueos y el intervalo común de la sede para ambos recursos en el horario propuesto.
4. Si la asignación cumple las reglas de agenda, la aplicación guarda el turno y muestra la reserva registrada.
5. El alta da lugar al email con enlace de cancelación según el flujo 9.

La asignación se rechaza si falta un recurso, hay solapamientos o bloqueos, o se incumple el intervalo (RN-AG-02 a RN-AG-04 y RN-AG-10). Quedan pendientes los horarios y excepciones, el tratamiento de solicitudes simultáneas y la respuesta concreta de la interfaz (PA-01, PA-04 y PA-05).

## Flujo 4 · Reprogramar un turno

Un integrante del equipo necesita cambiar una reserva existente (US-006).

1. El operador selecciona el turno en la interfaz y propone cambios en la fecha, la hora, el profesional o el sillón.
2. La aplicación usa la prestación y la duración efectiva del turno y comprueba el intervalo, las reservas y los bloqueos del profesional y el sillón para la nueva asignación.
3. Si la asignación es válida, la aplicación guarda la reprogramación. La interfaz muestra los datos actualizados con la misma prestación y duración.

Si la asignación tiene un conflicto o falla por otro motivo, el resultado del turno original, su reserva y el aviso al operador quedan abiertos (PA-03). También siguen pendientes los estados que habilitan la operación, el horario aplicable y el efecto de la reprogramación sobre el enlace y los emails (PA-02, PA-04, PA-17 y PA-18).

## Flujo 5 · Cancelar un turno

Un integrante del equipo necesita liberar el horario de un turno existente (US-007).

1. El operador consulta el turno y presenta su identificador y versión vigente. Recepción tiene alcance global; odontología solo cancela turnos propios de pacientes con asignación activa.
2. El servicio bloquea el turno y los recursos, vuelve a comprobar el alcance y exige que el turno esté pendiente y su inicio siga siendo estrictamente futuro. Si la versión cambió, pide recargar; si ya está cancelado con versión vigente, rechaza la repetición.
3. La transacción cambia únicamente el estado a `CANCELADO` y la versión. Conserva los datos y el registro visible en la agenda; al confirmar, deja de ocupar profesional y sillón. Un rechazo o rollback mantiene el último estado y ocupación confirmados.
4. Recepción puede usar el horario para otra reserva que cumpla las reglas de agenda. El turno actualizado identifica el período y los recursos liberados para futuros consumidores después del commit. Si hay interesados en el hueco, C-22 desarrollará el flujo 12.

El contrato de C-08 es un servicio interno: una referencia inexistente produce un error para recepción y una denegación genérica para odontología. La interfaz, el enlace del paciente y el tratamiento de los emails permanecen en sus changes; el corte de 48 horas de C-18 no se aplica a esta cancelación interna. [Diseño aprobado](../openspec/changes/archive/2026-11-24-cancelar-y-liberar-horario/design.md)

## Flujo 6 · Registrar una confirmación manual

Recepción realiza el seguimiento de los turnos por confirmar (US-008).

1. Recepción abre el listado de pendientes en la interfaz.
2. La aplicación consulta y muestra los turnos que cumplen la condición de estar por confirmar.
3. Tras el seguimiento manual, recepción registra la confirmación de un turno.
4. La aplicación guarda el registro y la interfaz refleja el resultado.

El recordatorio automático coexiste con el seguimiento manual. La condición de pertenencia al listado, la transición correspondiente, la presentación del resultado y los casos de duplicación quedan pendientes (PA-02).

## Flujo 7 · Consultar la agenda de sillón

1. Un integrante del equipo selecciona un sillón o box y el día o la semana que quiere consultar.
2. La agenda presenta los turnos del período con su duración efectiva (US-009 y RN-AG-09).

## Flujo 8 · Bloquear recursos

1. Recepción indica el período y el sillón, el profesional o ambos.
2. La aplicación comprueba si el bloqueo afecta a algún turno existente. Si lo afecta, rechaza el bloqueo.
3. Al aplicarse, el bloqueo impide asignar los recursos afectados durante su período (US-010 y RN-AG-10).

Los límites del período, los conflictos entre solicitudes simultáneas y la gestión de los desbloqueos siguen pendientes (PA-04 y PA-19).

## Flujo 9 · Enviar el email de alta y el recordatorio

1. Al crear el turno, la aplicación envía al paciente un email con el enlace para cancelar.
2. El recordatorio se envía automáticamente 24 horas antes del turno. Cuando el plazo de cancelación venció, se envía sin opción de cancelar.
3. Recepción registra las confirmaciones mediante el flujo 6 (US-011, US-012 y RN-EM-01 a RN-EM-03).

La demostración requiere envíos reales a las direcciones de prueba indicadas por el equipo mediante un servicio gratuito por definir. La verificación del destinatario y el tratamiento de altas cercanas al turno, reprogramaciones, cancelaciones, duplicados y fallos siguen pendientes (PA-15 y PA-18).

## Flujo 10 · Cancelar desde el enlace

1. El paciente abre el enlace del email de alta para solicitar la cancelación del turno asociado.
2. La aplicación comprueba que la solicitud esté dentro del plazo aprobado, hasta 48 horas antes del turno. Vencido el plazo, rechaza la cancelación por enlace.
3. La cancelación aceptada libera inmediatamente el horario y permite aplicar el flujo 12 a los interesados en el hueco (US-013, RN-AG-07 y RN-PA-01 a RN-PA-02).

La evaluación del instante exacto del corte y la vigencia, reutilización y protección del enlace quedan pendientes (PA-17 y PA-18).

## Flujo 11 · Gestionar una inscripción en lista de espera

1. El paciente accede con su cuenta para gestionar su propia inscripción.
2. Indica la prestación, el rango de fechas y las franjas horarias. Puede indicar un profesional de preferencia (US-014, RN-PA-01 y RN-LE-01).

El registro de la cuenta, su vinculación con el paciente, los permisos por registro y el ciclo de la inscripción siguen pendientes (PA-16 y PA-20).

## Flujo 12 · Avisar de un hueco y asignarlo

1. Al liberarse un hueco, la aplicación identifica las inscripciones con preferencias compatibles y avisa automáticamente por email a los interesados.
2. Recepción confirma quién toma el hueco y aplica las validaciones de agenda al asignar el turno. El aviso es informativo y la reserva se crea durante la asignación por recepción (US-015 y RN-LE-02 a RN-LE-03).

Los criterios de compatibilidad, la forma de revalidar disponibilidad al asignar y el tratamiento de avisos de huecos que ya se ocuparon quedan pendientes (PA-20 y PA-21).

Fuentes y referencias. [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md) · [Discovery, secciones 2, 3, 5 y 7](../docs/discovery/discovery.md) · [Reglas de negocio](05_reglas_de_negocio.md) · [Funcionalidades](06_funcionalidades.md) · [Preguntas abiertas](10_preguntas_abiertas.md)
