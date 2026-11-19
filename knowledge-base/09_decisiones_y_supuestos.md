# Decisiones y supuestos

## Cómo interpretar este registro

Las decisiones DD provienen de respuestas explícitas del usuario, consolidadas en [decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json). No se reconstruyen razones personales ni alternativas que el usuario no expresó. Las inferencias SU son clasificaciones para documentar; no autorizan requisitos adicionales.

## Decisiones documentadas

### DD-01 Segmento y usuarios

**Decisión**: una sede con varios odontólogos; recepción/administración y odontólogos operan el sistema. El equipo crea los turnos. La ampliación incorpora al paciente para cancelación por enlace y lista de espera con cuenta.

**Contexto**: el Discovery ofrecía diferentes segmentos y formas de reserva. **Alternativas consideradas en el menú**: profesional independiente, consultorio de una sede o clínica multisede; creación interna, solicitud del paciente o reserva directa. **Justificación documentada**: elección explícita del usuario; no expresó una justificación comercial adicional. **Límite**: no implica multisede ni autorreserva general. La participación limitada del paciente fue aprobada posteriormente; la decisión inicial de usuarios solo internos queda como antecedente.

### DD-02 Prioridades y alcance clínico

**Decisión**: evitar solapamientos y reducir huecos por ausencias/cancelaciones; agenda y ficha sencilla con antecedentes y notas.

**Contexto**: distinguir imprescindibles de funciones comunes en competidores. **Alternativas del menú**: datos básicos de contacto, ficha sencilla o ficha con odontograma. **Justificación**: selección explícita; sin orden entre los dos problemas. **Límite aceptado**: odontograma fuera del MVP; reducción de ausencias sin mejora porcentual garantizada.

### DD-03 Recursos, duración e intervalo

**Decisión**: profesional y sillón obligatorios y sin solapamientos; duración por prestación ajustable al crear; un intervalo configurable para toda la sede.

**Alternativas del menú**: control de uno o ambos recursos, duración fija o por prestación y configuración de intervalo global o por recurso/prestación. **Justificación**: elección explícita del usuario. **Pendientes**: minutos iniciales, límites de configuración y solución de concurrencia.

### DD-04 Reprogramación y cancelación

**Decisión**: reprogramar puede cambiar fecha, hora, profesional y sillón, conservando prestación y duración; cancelar libera inmediatamente el horario.

**Contexto**: recuperar disponibilidad sin cambiar el servicio previsto. **Alternativas del menú**: distintos alcances de modificación y liberación inmediata o condicionada. **Justificación**: elección explícita. **Límite**: no se aprobó reemplazo automático ni comportamiento preciso ante una reprogramación fallida.

### DD-05 Permisos clínicos y seguimiento

**Decisión**: recepción lee la ficha; solo odontólogos editan datos clínicos. Recepción registra confirmaciones y sigue pendientes manualmente.

**Alternativas del menú**: distintos accesos clínicos; seguimiento manual, email automático o WhatsApp automático. **Justificación**: elecciones explícitas. **Límite**: no se amplían permisos clínicos. La ampliación posterior agrega emails automáticos sin sustituir las confirmaciones manuales.

### DD-06 Stack, presupuesto y entrega

**Decisión**: HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL; presupuesto externo cero; entrega el 25/11/2026.

**Contexto**: restricciones informadas por el usuario, no resultado obligatorio del ranking competitivo. **Alternativas consideradas**: no se documentó una comparación técnica de stacks; no se inventa. **Justificación**: preferencia explícita del usuario. **Pendientes**: versiones, build y despliegue.

### DD-07 Equipo académico

**Decisión informada**: cuatro integrantes, cantidad que puede variar. El usuario declaró autorización docente para superar el máximo de tres de la consigna.

**Contexto**: diferencia detectada entre documento y organización del equipo. **Justificación**: respuesta explícita del usuario; no se atribuye comprobación independiente. **Pendiente**: nombres, reparto y participación demostrable.

### DD-08 Exclusiones e integraciones

**Decisión vigente**: email obligatorio con envíos reales a direcciones de prueba del equipo y proveedor gratuito por definir. Autorreserva general, adjudicación automática de huecos, WhatsApp automático, odontograma, pagos y facturación quedan fuera del MVP.

**Evolución**: la decisión inicial no requería integraciones y no incluía lista de espera. La ampliación explícita aprobada sustituye esos límites en email y lista; conserva presupuesto cero.

**Contexto**: separar el producto elegido de la oferta competitiva. **Justificación**: el usuario confirmó expresamente estas exclusiones. **Límite**: no se atribuye prioridad ni compromiso a las ampliaciones.

### DD-09 Referencias y aprobación

**Decisión**: comparar Dentalink, DentApp, Dentify, Dentidesk y AgendaPro; referencias UX Dentalink, AgendaPro y Dentrix. El usuario aprobó el resumen de once puntos con los pendientes explícitos.

**Alternativas del menú**: conjuntos A/B o selección propia; se eligió A. **Límite**: no se hicieron demos con cuenta ni revisión personal de cinco fuentes. Esta KB importa lo aprobado y no selecciona el primer change.

### DD-10 Email, cancelación y cuenta del paciente

**Decisión**: email al crear con enlace para cancelar hasta 48 horas antes del turno; recordatorio automático 24 horas antes, sin opción de cancelar cuando venció el plazo. El paciente usa una cuenta para gestionar su inscripción en lista de espera. Envíos reales a direcciones de prueba del equipo, con opción gratuita por definir.

**Contexto y evolución**: el primer plazo respondido fue 24 horas; el usuario lo cambió expresamente a 48 y después eligió entregar el enlace al crear. No se conservan ambos valores como reglas vigentes. **Pendientes**: PA-15 a PA-18. No se elige proveedor ni se implementa acceso al documentar.

### DD-11 Agenda, bloqueos y lista de espera

**Decisión**: agenda por sillón diaria y semanal; recepción bloquea sillón y/o profesional por período y se rechaza si afecta turnos existentes. La lista registra prestación, rango de fechas y franjas horarias, con profesional opcional. Se avisa automáticamente por email a interesados y recepción confirma quién toma el hueco; el aviso no reserva.

**Justificación documentada**: elecciones explícitas por menú y aprobación final «Apruebo el resumen y actualizar los documentos». **Pendientes**: PA-19 a PA-21; se preservan límites y compatibilidad sin decisiones inferidas. [Ampliación y resumen aprobados](../docs/discovery/ampliacion-paciente.md).

## Supuestos inferidos y límites

### SU-01 Clasificación como aplicación web

**Supuesto de clasificación**: `web_app`, inferido del frontend, backend y persistencia elegidos, con operación interna y acceso limitado del paciente. **Origen**: DD-01 y DD-06. **Riesgo si no representa la intención**: que la forma de servir la UI o el backend no coincida con la entrega esperada. **Cómo validar**: acordar el contrato y despliegue antes del change que los necesite. No implica REST, SPA ni framework adicional.

### SU-02 Escala de equipo y necesidad de infraestructura

**Inferencia**: escala `team` y necesidad de un entorno para PostgreSQL. **Origen**: varios usuarios internos, pacientes con cuenta para lista y base de datos explícita. **Riesgo**: confundir infraestructura con contratación externa. **Cómo validar**: resolver el entorno de ejecución en PA-09; no se seleccionó servicio pago.

### SU-03 Beneficio del seguimiento y las comunicaciones

**Hipótesis pendiente de validación**: el seguimiento manual, los recordatorios y la lista pueden ayudar a reducir ausencias y huecos. **Origen**: objetivo y mecanismo elegidos en Discovery. **Riesgo**: que la operación real no mejore o no se mida. **Cómo validar**: definir línea de base y observaciones; no inventar porcentajes.

No se adopta como supuesto un valor de intervalo, estados concretos, campos clínicos, cardinalidades físicas, permisos de eliminación o versiones. Esos puntos se mantienen en [preguntas abiertas](10_preguntas_abiertas.md).
