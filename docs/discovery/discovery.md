# Discovery del sistema de turnos odontológicos

Fecha: 17 de noviembre de 2026. Revisión 2. Ampliación aprobada explícitamente mediante menú: «Apruebo el resumen y actualizar los documentos». Se conserva la aprobación histórica en decisiones-aprobadas.json; esta revisión incorpora paciente, emails, lista de espera y bloqueos.

Este documento registra las decisiones del usuario tras la investigación de 15 sistemas y 38 referencias oficiales. El relevamiento está en `informe-discovery.md` y su PDF; las notas de fuentes están en `sources/`. Las respuestas estructuradas y la aprobación están en `decisiones-aprobadas.json`.

La documentación queda consolidada con preguntas abiertas explícitas. La revisión personal de cinco fuentes exigida por la consigna continúa pendiente; las verificaciones del agente no se presentan como verificaciones de los integrantes.

## 1. Problema que resuelve

Un consultorio de una única sede con varios odontólogos necesita evitar solapamientos y errores al organizar los turnos, y reducir los espacios vacíos que producen las inasistencias y cancelaciones. El usuario considera prioritarios ambos problemas, sin establecer un orden entre ellos.

## 2. Usuarios y roles

- Recepción/administración: gestiona la agenda, registra confirmaciones y realiza el seguimiento manual. Puede leer la ficha clínica.
- Odontólogos: operan el sistema y pueden leer y editar los antecedentes y notas clínicas. Solo ellos pueden editar la información clínica.
- Paciente: recibe emails, cancela su turno desde un enlace y gestiona con cuenta su inscripción en la lista de espera. La creación general de turnos y la asignación de huecos siguen a cargo del equipo; no hay autorreserva general. El mecanismo técnico de la cuenta y los permisos detallados siguen pendientes.

La cantidad concreta de profesionales y sillones está pendiente. El mecanismo de autenticación y el detalle de permisos por operación se especificarán antes de implementar; no se inventan aquí roles adicionales con acceso total.

## 3. Casos de uso

1. Como integrante del equipo, quiero gestionar pacientes y consultar sus datos para organizar su atención; la edición de datos clínicos queda limitada a odontólogos.
2. Como integrante del equipo, quiero crear un turno con profesional y sillón disponibles para evitar solapamientos.
3. Como integrante del equipo, quiero reprogramar fecha, hora, profesional o sillón manteniendo prestación y duración para reorganizar la agenda sin alterar el servicio previsto.
4. Como integrante del equipo, quiero cancelar un turno y liberar inmediatamente el horario para permitir que recepción cree otro.
5. Como recepción, quiero consultar turnos pendientes de confirmación y registrar confirmaciones manuales para realizar seguimiento de ausencias y cancelaciones.
6. Como paciente, quiero recibir un email al crear el turno con enlace para cancelar y un recordatorio 24 horas antes para no olvidarlo.
7. Como paciente, quiero cancelar mediante el enlace hasta 48 horas antes del turno para liberar el lugar sin llamar.
8. Como paciente, quiero gestionar con cuenta mi inscripción en lista de espera y recibir un aviso por email cuando se libera un hueco de mi interés.
9. Como recepción, quiero confirmar quién toma el hueco anunciado, comprobando su disponibilidad.
10. Como equipo, quiero consultar la agenda por sillón en vista diaria y semanal.
11. Como recepción, quiero bloquear sillón y/o profesional por período, rechazando bloqueos que afecten turnos existentes.

## 4. Competidores y soluciones existentes

La investigación compara 15 sistemas. El usuario seleccionó cinco para un análisis o demo posterior: Dentalink, DentApp, Dentify, Dentidesk y AgendaPro. Las referencias de experiencia de usuario elegidas son Dentalink, AgendaPro y Dentrix.

Dentalink y DentApp aportan oferta dental e integraciones locales declaradas; Dentify aporta una propuesta de consultorio argentino; Dentidesk documenta roles y condiciones de exportación; AgendaPro permite examinar una interfaz pública de reserva. La selección no implica contratar ni afirmar que todas sus funciones deban reproducirse. Las referencias de autorreserva no agregan esa función como autorreserva general al MVP.

Las condiciones y límites se detallan en las fichas del informe, con URLs y fecha. «No evidenciado» no significa que una prestación no exista. La puntuación representa cobertura documental y no una prueba de calidad real.

## 5. Funcionalidades necesarias

- Gestión de pacientes y ficha clínica sencilla con antecedentes y notas.
- Agenda por sillón para varios odontólogos en una sede, con vista diaria y semanal, duración variable, bloqueos y control de solapamientos.
- Creación de turnos con profesional y sillón/box obligatorios y disponibles.
- Duración definida por prestación, ajustable por el equipo al crear el turno.
- Intervalo configurable común para toda la sede, cuyo valor inicial sigue pendiente.
- Reprogramación y cancelación de turnos con las reglas aprobadas.
- Listado de turnos por confirmar y registro de confirmaciones manuales.
- Separación del acceso clínico: lectura para recepción y edición solo para odontólogos.
- Email al crear el turno con enlace para cancelar; recordatorio automático 24 horas antes, sin opción de cancelar cuando el plazo venció.
- Cancelación del paciente por enlace hasta 48 horas antes del turno.
- Cuenta para gestionar la inscripción en lista de espera y aviso automático de huecos por email, con asignación final por recepción.

Estas funciones definen el MVP del roadmap; no significan que todas se implementen en el único primer change requerido por la consigna. La elección de ese change se realizará después del roadmap.

## 6. Funcionalidades opcionales y etapas posteriores

Autorreserva general del paciente, adjudicación automática de huecos, WhatsApp automático, odontograma, pagos y facturación quedan expresamente fuera del MVP. Se podrán evaluar después, sin compromiso ni prioridad asignada. La lista de espera y sus avisos por email sí forman parte del MVP revisado; un aviso no reserva el horario.

## 7. Reglas de negocio

- Todo turno requiere profesional y sillón/box; ninguno puede estar ocupado por otro turno durante el horario reservado.
- Entre turnos debe respetarse un intervalo configurable único para toda la sede. No se considera cero ni se fija una cantidad de minutos por defecto en este documento.
- La duración se obtiene de la prestación, pero el equipo puede ajustarla al crear el turno.
- Al reprogramar pueden cambiar fecha, hora, profesional y sillón, siempre validando la disponibilidad. Se conservan prestación y duración.
- Al cancelar se libera inmediatamente el horario, sin esperar una liberación manual posterior.
- El equipo crea todos los turnos del MVP. La confirmación se registra y sigue manualmente por recepción.
- Recepción puede leer la ficha clínica; solo los odontólogos editan antecedentes y notas.
- Recepción bloquea sillón y/o profesional por período; se rechaza el bloqueo si afecta turnos existentes.
- El paciente cancela mediante enlace hasta 48 horas antes. Recibe el enlace en el email de creación; el recordatorio se envía 24 horas antes y no ofrece cancelación si el plazo venció. El instante exacto del límite y los turnos creados dentro de esos plazos se especificarán antes de implementar.
- La lista de espera registra prestación, rango de fechas y franjas horarias, con profesional opcional. Al liberarse un hueco se avisa por email a los interesados; recepción confirma quién lo toma, sin reserva ni adjudicación automática.

El valor inicial del intervalo, los estados exactos y el comportamiento preciso ante una reprogramación fallida quedan abiertos para especificación. No se admiten sobreturnos por inferencia: la regla aprobada impide ocupaciones simultáneas de los recursos.

## 8. Integraciones

El email es una integración obligatoria del MVP revisado: deben demostrarse envíos reales a direcciones de prueba del equipo con una opción gratuita aún por definir. No se elige proveedor ni se envían correos durante esta consolidación. El seguimiento manual de confirmaciones se conserva junto a los emails automáticos; no implica WhatsApp. Google Calendar, pagos y servicios fiscales no se incorporan por defecto.

PostgreSQL es parte del stack elegido por el usuario y no un servicio externo contratado. La ubicación y el modo de ejecución de la base de datos siguen sin decidirse.

## 9. Restricciones

- Entrega: 25 de noviembre de 2026.
- Equipo: cuatro integrantes, cantidad que puede variar. El usuario informó autorización docente para superar el máximo de tres de la consigna; no se atribuye una verificación documental independiente a esa declaración.
- Presupuesto para servicios externos: cero.
- Frontend: HTML, CSS y TypeScript vanilla.
- Backend: Java con Spring Boot.
- Base de datos: PostgreSQL.
- Todos los ejemplos, pruebas y capturas deben usar datos clínicos ficticios, como exige la consigna. Para la demostración de email se usarán direcciones de prueba indicadas por el equipo.
- El trabajo académico requiere un único change completo, especificado, probado y archivado; no una implementación apresurada de todo el MVP.

Las versiones concretas, el despliegue y el reparto nominal de tareas no están definidos. La selección de estas tecnologías fue explícita del usuario; no se presenta como conclusión obligada del análisis competitivo.

## 10. Riesgos

Los riesgos que preocupan especialmente al usuario son el manejo incorrecto de datos clínicos/permisos y los errores de solapamiento o reprogramación. No estableció un orden entre esos dos riesgos. El plazo también preocupa, aunque menos; su confianza en llegar no constituye una garantía de ejecución.

El control de agenda necesita escenarios verificables sobre ambos recursos y el intervalo. La separación de permisos necesita comprobar que recepción no pueda modificar información clínica. La efectividad del seguimiento manual para reducir inasistencias todavía no fue medida con usuarios reales.

La ampliación añade riesgos técnicos de acceso indebido por cuenta/enlace, emails fallidos o duplicados, límites del proveedor gratuito y avisos de huecos ya ocupados. Son riesgos identificados, no un nuevo orden de preocupación elegido por el usuario.

Persisten límites de investigación: declaraciones comerciales no auditadas, precios y planes cambiantes y condiciones de portabilidad incompletas. Las capacidades no evidenciadas de los competidores siguen como preguntas, no se convierten en supuestas ausencias.

## 11. Preguntas abiertas

- ¿Cuántos minutos debe tener inicialmente el intervalo global? ¿Qué límites y permisos tendrá su configuración?
- ¿Cuántos profesionales y sillones/boxes tendrá el consultorio de referencia?
- ¿Cuáles son los estados exactos del turno y sus transiciones?
- ¿Qué campos mínimos de paciente, antecedentes y notas se requieren?
- ¿Cuál será el comportamiento especificado si una reprogramación no puede completarse?
- ¿Quiénes integran el equipo y cómo se repartirán los cambios y la defensa?
- ¿Qué versiones concretas de Java, Spring Boot, PostgreSQL y herramientas de frontend se usarán?
- ¿Qué funcionalidades no evidenciadas y condiciones de precios/exportación se corroborarán al revisar las fuentes?
- ¿Qué integrantes comprobarán personalmente al menos cinco fuentes y registrarán sus hallazgos?

- Proveedor gratuito de email, límites y configuración de direcciones de prueba?
- Registro y vinculación de la cuenta con el paciente y permisos sobre datos propios?
- Seguridad, vigencia y uso de enlaces; tratamiento exacto del límite de 48 horas?
- Turnos creados dentro de las 48 o 24 horas; efectos de reprogramación y cancelación sobre emails?
- Duplicados, fallos, reintentos y constancia de envío?
- Gestión de desbloqueos y límites temporales de bloqueos?
- Compatibilidad exacta de huecos con duración, recursos, intervalo y preferencias; disponibilidad al asignar y comunicación si ya fue ocupado?

La revisión 2 fue aprobada con estos pendientes. Se consolida junto con la KB y el roadmap; no se inicia implementación ni se selecciona el primer change. La trazabilidad de la ampliación está en [ampliacion-paciente.md](ampliacion-paciente.md).
