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

**Contexto**: diferencia detectada entre documento y organización del equipo. **Justificación**: respuesta explícita del usuario; no se atribuye comprobación independiente. **Actualización 19/11/2026**: Giovanni, Sofia, Luciano y Fabi confirmados; reparto de 25 changes aprobado expresamente y registrado en CHANGES.md y docs/plan-colaboracion.md. **Pendiente**: participación demostrable y organización de la defensa.

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

### DD-12 C-01 académico y base de dominio

**Decisión posterior expresa (19/11/2026)**: Giovanni eligió C-01 a su cargo y aprobó la propuesta y su implementación. Java 25, Maven y JUnit en `backend/`, objetos inmutables e instantes/duraciones explícitos. Intervalo positivo recibido y reservas ocupantes como entrada, sin definir estados ni minutos iniciales. Se admite exactamente el intervalo mínimo antes y después para ambos recursos.

**Alternativas del menú**: Java 21 objetivo con JDK instalado o Java 25; se eligió Java 25. Se aprobó un worktree aislado para C-01. **Límite**: no elige Spring Boot, proveedor ni esquema de PostgreSQL. Las descripciones previas que presentan PA-13 como elección pendiente son antecedentes superados por esta decisión; las instrucciones de agentes conservan sus reglas técnicas confirmadas. Implementación y Verify permanecen separados de Archive y la integración a main.

### DD-13 Persistencia base de C-02

**Decisión expresa (19/11/2026)**: C-02 aprobado para Apply, con Boot 4.1.2, PostgreSQL 18.7, JPA/Hibernate, Flyway, Compose y Testcontainers. Referencias de paciente/profesional/sillón/prestación con UUID de aplicación y código único por tipo. Versiones globales consecutivas coordinadas por Giovanni al integrar; sin out-of-order. Entidades ORM separadas del dominio y usuarios de base distintos para migración y operación. Reviewer Astra/medium autorizado como alternativa a Luna para la revisión independiente de este change.

**Alcance**: no-web y cuatro referencias mínimas; no cuentas, datos clínicos ni tabla de turnos. La prueba de persistencia real no implica integridad concurrente de agenda. El plan y Apply fueron aprobados; archivo, integración y push conservan sus respectivos pedidos y evidencias.

## Supuestos inferidos y límites

### SU-01 Clasificación como aplicación web

**Supuesto de clasificación**: `web_app`, inferido del frontend, backend y persistencia elegidos, con operación interna y acceso limitado del paciente. **Origen**: DD-01 y DD-06. **Riesgo si no representa la intención**: que la forma de servir la UI o el backend no coincida con la entrega esperada. **Cómo validar**: acordar el contrato y despliegue antes del change que los necesite. No implica REST, SPA ni framework adicional.

### SU-02 Escala de equipo y necesidad de infraestructura

**Inferencia**: escala `team` y necesidad de un entorno para PostgreSQL. **Origen**: varios usuarios internos, pacientes con cuenta para lista y base de datos explícita. **Riesgo**: confundir infraestructura con contratación externa. **Cómo validar**: resolver el entorno de ejecución en PA-09; no se seleccionó servicio pago.

### SU-03 Beneficio del seguimiento y las comunicaciones

**Hipótesis pendiente de validación**: el seguimiento manual, los recordatorios y la lista pueden ayudar a reducir ausencias y huecos. **Origen**: objetivo y mecanismo elegidos en Discovery. **Riesgo**: que la operación real no mejore o no se mida. **Cómo validar**: definir línea de base y observaciones; no inventar porcentajes.

No se adopta como supuesto un valor de intervalo, estados concretos, campos clínicos, cardinalidades físicas, permisos de eliminación o versiones. Esos puntos se mantienen en [preguntas abiertas](10_preguntas_abiertas.md).


## DD-14 — Correcciones de C-03 y cambio de contraseña propia (20/11/2026)

Giovanni pidió resolver Issue #30 antes de integrar PR #29 y aprobó expresamente: usuario activo cambia solo su propia contraseña presentando la actual; se rechaza la vigente o cualquiera de las cuatro anteriores (cinco en total). La validación y persistencia son transaccionales y el historial se conserva entre arranques. No se agregan HTTP, recuperación ni cambios de contraseñas ajenas. El bloqueo por usuario evita que dos solicitudes con la misma clave anterior prosperen. V3 es la siguiente migración libre observada para esta integración, coordinada por Giovanni; V1/V2 no se editan.

La prueba del CLI debe usar un proceso con entorno vacío y allowlist del fixture, sin heredar overrides de Spring ni modificar la configuración original del JVM de pruebas. Las correcciones se realizan antes de integrar; la aprobación anterior con pendientes no equivale a evidencia de su resolución. No cambian el reparto nominal ni las dependencias del roadmap.

## Decisiones aprobadas para C-17 (transporte de email de prueba) — 2026-11-24

- **PA-15 (proveedor):** Resend (3000 emails/mes, 100/día, sin tarjeta). Sender en sandbox (`onboarding@resend.dev`) mientras no haya dominio propio verificado (presupuesto cero).
- **Direcciones de prueba:** se usarán placeholders/allowlist en desarrollo; el equipo completará las direcciones reales antes de Apply. Variable `EMAIL_TEST_ALLOWLIST` (env).
- **Variables de entorno:** `RESEND_API_KEY`, `EMAIL_FROM`, `EMAIL_TEST_ALLOWLIST`, `EMAIL_MAX_RETRIES` (default 2), `EMAIL_TIMEOUT`.
- **PA-18 (semántica):** best-effort + registro. El transporte no rompe la operación de negocio; registra éxito/fallo en `email_envio`. Dedup por clave de idempotencia `(tipo, turnoId, destinatario)`. 2 reintentos con backoff corto; luego `status=failed`. C-20/C-19 deciden reprogramación/cancelación según el registro.
- **Contrato de salida:** interfaz neutra `comunicaciones.CorreoSalida` con `enviar(...) -> ResultadoEnvio`, implementada por adaptador Resend + fake para tests. Persistencia de envíos en tabla `email_envio` (próxima migración coordinada por Giovanni).
- **Alcance C-17:** solo transporte, configuración, puerto y registro de envío. Sin lógica de negocio, plantillas ni scheduling.

## DD-15 — Cancelación interna de C-08 (24/11/2026)

Giovanni aprobó el diseño y el plan escrito de C-08 para Apply con TDD, trabajador GPT-6.2 Sol medio y revisor independiente del mismo modelo. Recepción puede cancelar turnos elegibles de la sede; odontología solo turnos propios de pacientes con asignación activa. Solo se cancela un turno `PENDIENTE` con inicio estrictamente futuro después de esperar los locks. Se exige la versión vigente: una versión anterior pide recargar y una repetición con versión vigente se rechaza indicando que el turno ya está cancelado.

El registro conserva sus datos y permanece visible como `CANCELADO`, sin ocupar profesional ni sillón después del commit. La transición incrementa una vez la versión; cualquier rechazo o reversión conserva el estado confirmado anterior. El turno devuelto identifica el período y recursos liberados. En una transacción exterior `READ COMMITTED`, esa liberación solo es efectiva al confirmar y el llamador debe entrar sin locks previos de agenda, respetando el orden global fila de turno → profesionales → sillones.

La migración es nueva y consecutiva (V9), coordinada con el historial integrado; V7 y V8 no se editan. La aprobación de Apply no incorpora HTTP, UI, emails, eventos, cancelación por enlace ni avisos de lista. Después de verificar la implementación, Giovanni autorizó expresamente el archivo, la integración y el push el 24/11/2026. [Plan aprobado](../openspec/changes/archive/2026-11-24-cancelar-y-liberar-horario/tasks.md)

## DD-16 — Sesión interna en cookie HttpOnly (25/11/2026)

**Contexto**: la interfaz de C-11 guarda token de sesión e identidad en `localStorage`, contra la regla de seguridad y sin excepción documentada. **Decisión**: Giovanni eligió por menú una cookie `HttpOnly`, `Secure` y `SameSite=Strict` con token anti-CSRF propio. **Alternativas presentadas**: token solo en memoria, excepción documentada para `localStorage` y cookie con Spring Security.

**Límite**: se aprobó preparar la propuesta, sin Apply. La deuda sigue abierta y la spec vigente mantiene el token en `Authorization` hasta implementar y archivar el change. [Propuesta](../openspec/changes/sesion-en-cookie-httponly/proposal.md)

## DD-17 — Vigencia del enlace ante la confirmación (C-18, 25/11/2026)

**Contexto**: la revisión del PR #44 mostró que confirmar un turno incrementa su versión y dejaba sin efecto el enlace de cancelación ya emitido. **Decisión**: Giovanni eligió por menú que el enlace conserve su validez al confirmar, con el mismo plazo de 48 horas. **Alternativas presentadas**: invalidar y emitir un reemplazo con entrega diferida a C-19, o reemplazar y definir ahora el contrato de entrega. La reprogramación sigue invalidando el enlace y el uso repetido se rechaza.

**Límite**: no incorpora emails ni entrega del enlace (C-19). [Change](../openspec/changes/archive/2026-11-25-c-18-fixes-hallazgos-pr44/proposal.md)

## DD-18 — Entrega interna del enlace a C-19 (C-18, 25/11/2026)

**Contexto**: el token de cancelación se generaba al crear y reprogramar, pero su valor en claro se descartaba. **Decisión**: Giovanni eligió por menú un evento interno que el consumidor recibe después de confirmar la transacción. **Alternativas presentadas**: que C-19 emita el token al enviar, o dejar la entrega solo registrada como pendiente. El token no se persiste en claro ni se expone por HTTP.

**Límite**: no se envían emails. La entrega no se persiste: si el proceso cae antes del consumo, ese enlace se pierde. Contenido, destinatario, fallos y reintentos quedan para C-19. [Change](../openspec/changes/archive/2026-11-25-c-18-entrega-del-enlace/proposal.md)
