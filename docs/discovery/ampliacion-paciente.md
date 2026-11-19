# Ampliación del alcance: paciente, email y lista de espera

Fecha: 17 de noviembre de 2026.
Estado: resumen de once puntos aprobado por menú: «Apruebo el resumen y actualizar los documentos». Consolidación documental completada y verificada el 19 de noviembre de 2026. No se inició implementación ni se seleccionó proveedor o enviaron emails.

## Precedencia e impacto

Esta ampliación posterior prevalece sobre las exclusiones del alcance anterior cuando se contradicen. La aprobación histórica del Discovery no se presenta como aprobación de las reglas nuevas que todavía faltan definir. El alcance anterior queda disponible como referencia, pero no debe usarse por sí solo para implementar.

Se actualizan el checklist, las decisiones estructuradas, el generador del informe y su Markdown/PDF, los diez documentos de KB y CHANGES.md. Se conservarán los identificadores existentes de reglas, historias, preguntas y changes. La investigación de competidores no cambia por esta ampliación.

## Requisitos solicitados expresamente

- Incorporar al paciente como usuario: recibe recordatorios, cancela su turno y se inscribe en la lista de espera.
- Agenda de sillón con vista diaria y semanal, duración variable, bloqueos y control de solapamientos.
- Recordatorio automático por email antes del turno.
- Cancelación del turno mediante un enlace del email, liberando el lugar sin llamar.
- Lista de espera con aviso automático por email cuando se libera un hueco.

## Respuestas recibidas por menú

| Tema | Decisión del usuario |
| --- | --- |
| Acceso del paciente | Enlace para cancelar; cuenta para gestionar la lista de espera. |
| Email de creación | Al crear el turno, enviar un email con enlace para cancelar. |
| Recordatorio | 24 horas antes del turno, sin opción de cancelar cuando el plazo venció. |
| Límite de cancelación por enlace | Hasta 48 horas antes del turno. Respuesta posterior que sustituye el plazo inicial de 24 horas. |
| Asignación de un hueco | Avisar a los interesados; recepción confirma quién toma el hueco. El aviso no asigna automáticamente el turno. |
| Evidencia de email en la entrega | Envíos reales a direcciones de prueba del equipo, con una opción gratuita por definir. |
| Bloqueos | Recepción bloquea sillón y/o profesional por período; se rechaza el bloqueo si afecta turnos existentes. |
| Preferencias de lista de espera | Prestación, rango de fechas y franjas horarias; profesional opcional. |

Estas respuestas no seleccionan un proveedor, un mecanismo de autenticación ni una tecnología de enlaces. Tampoco autorizan envíos de email durante la documentación.

## Resolución del flujo de email y cancelación

El usuario eligió: «Email al crear el turno con enlace; recordatorio a las 24 horas sin opción de cancelar cuando el plazo venció». Se conserva la cancelación hasta 48 horas antes del turno y el recordatorio 24 horas antes. No se habilita una excepción al plazo por recibir el recordatorio. El tratamiento exacto del límite y de turnos creados dentro de esos plazos deberá quedar especificado antes de implementar.

Las aclaraciones de esta ronda están resueltas. El usuario aprobó el resumen completo y la actualización de Discovery, KB, roadmap y PDF mediante menú. Se preservan las preguntas técnicas abiertas.

## Decisiones anteriores que se conservan

Una sede con varios odontólogos; profesional y sillón obligatorios, sin solapamientos; duración por prestación ajustable al crear; intervalo global configurable sin minutos iniciales elegidos; reprogramación conserva prestación y duración; cancelación libera el horario inmediatamente. Recepción puede leer la ficha clínica y solo odontólogos la editan.

Se conserva el listado de turnos por confirmar y el seguimiento manual por recepción. El recordatorio automático no implica una confirmación automática. La creación general de turnos sigue a cargo del equipo; el paciente no adquiere autorreserva general ni adjudicación automática de huecos por esta ampliación.

Se mantienen fuera del MVP WhatsApp automático, odontograma, pagos y facturación. Se conserva presupuesto externo cero, entrega el 25/11/2026, cuatro integrantes con autorización docente informada, datos clínicos ficticios y stack HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL. Para demostrar emails se usarán las direcciones de prueba que indique el equipo.

## Pendientes de especificación que no se resuelven por inferencia

Proveedor gratuito y límites de envío; verificación de direcciones; registro y vinculación de cuentas con pacientes; permisos limitados a datos propios; vigencia y uso de enlaces; tratamiento de reprogramaciones, cancelaciones y turnos creados dentro del plazo de cancelación de 48 horas o de recordatorio de 24 horas; control de duplicados y fallos de envío; compatibilidad de huecos con duración, recursos e intervalo; disponibilidad al asignar y avisos si el hueco ya fue ocupado. Estos puntos deberán resolverse antes del change correspondiente.

La validación personal de al menos cinco fuentes y los demás pendientes anteriores continúan abiertos. No se implementó código, no se conectó un proveedor y no se enviaron mensajes.

## Resumen de Discovery para aprobación de la revisión

1. **Problema:** evitar solapamientos y errores de agenda, y reducir huecos por inasistencias y cancelaciones; ambas prioridades se conservan sin orden definido.
2. **Usuarios:** recepción/administración, odontólogos y paciente. Recepción lee la ficha clínica; solo odontólogos la editan. El paciente cancela por enlace y usa una cuenta para gestionar su inscripción en la lista de espera; los mecanismos técnicos y permisos detallados siguen pendientes.
3. **Casos de uso:** gestionar pacientes y ficha sencilla; crear, reprogramar y cancelar turnos; registrar confirmaciones manuales; recibir recordatorio; cancelar desde email; inscribirse en lista y recibir aviso de huecos. Recepción confirma quién toma cada hueco.
4. **Competidores y referencias:** se conservan Dentalink, DentApp, Dentify, Dentidesk y AgendaPro para la selección, y Dentalink, AgendaPro y Dentrix como referencias UX. La investigación de 15 sistemas y 38 fuentes no se rehace.
5. **Funciones necesarias:** agenda de sillón diaria y semanal, duración variable, bloqueos y control de solapamientos; pacientes/ficha; creación, reprogramación, cancelación y seguimiento manual; email al crear, recordatorio automático y lista de espera con aviso automático.
6. **Fuera del MVP:** autorreserva general del paciente, adjudicación automática de huecos, WhatsApp automático, odontograma, pagos y facturación. Recibir un aviso no reserva el espacio.
7. **Reglas:** profesional y sillón obligatorios y disponibles; intervalo global configurable, sin minutos iniciales elegidos; duración por prestación ajustable al crear; reprogramación conserva prestación y duración; cancelación libera inmediatamente. Recepción bloquea sillón y/o profesional por período; el bloqueo se rechaza si afecta turnos existentes. Cancelación por enlace hasta 48 horas antes. Email al crear con enlace; recordatorio 24 horas antes sin opción de cancelar al haber vencido el plazo. La lista recoge prestación, rango de fechas y franjas horarias, con profesional opcional; la asignación final es de recepción.
8. **Integraciones:** email obligatorio para envíos reales a direcciones de prueba del equipo; proveedor gratuito y detalles de integración pendientes. Se conserva presupuesto externo cero. No se requieren otras integraciones por esta ampliación.
9. **Restricciones:** una sede, varios odontólogos; entrega 25/11/2026; cuatro integrantes con autorización docente informada; datos clínicos ficticios. Stack HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL; versiones pendientes.
10. **Riesgos:** permisos y datos clínicos, solapamientos/reprogramación, acceso por enlaces y cuentas, fallos/duplicación de emails, avisos de huecos ya ocupados y límites del proveedor gratuito. El plazo también preocupa al usuario, en menor grado que los dos primeros riesgos previos; los nuevos riesgos son técnicos identificados y no un nuevo ranking elegido por él.
11. **Preguntas abiertas:** minutos y administración del intervalo, estados/campos, cantidades de recursos, reprogramación fallida, versiones/despliegue y reparto nominal; proveedor/email y tratamiento de plazos/fallos, registro y vinculación de cuentas, seguridad y vigencia de enlaces, desbloqueos y compatibilidad de huecos. Se conserva la revisión personal pendiente de cinco fuentes. Estas decisiones no se inventarán al consolidar.

**Efecto de aprobar:** actualizar coherentemente el checklist, las decisiones estructuradas, el generador y el informe Markdown/PDF, los diez archivos de KB y su índice, CHANGES.md y evidencias/estado. Preservar IDs existentes, historial y preguntas abiertas. No iniciar implementación, seleccionar un proveedor, enviar emails ni publicar tareas.
