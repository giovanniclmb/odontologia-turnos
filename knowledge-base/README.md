# Base de conocimiento del sistema de turnos odontológicos

Esta KB organiza el Discovery aprobado de un consultorio de una sede con varios odontólogos. La revisión 2 incorpora paciente, agenda diaria/semanal por sillón y bloqueos, emails y lista de espera. Conserva ficha sencilla, confirmaciones manuales y permisos clínicos. El paciente cancela por enlace hasta 48 horas antes; el recordatorio se envía 24 horas antes. La asignación de huecos queda a cargo de recepción.

## Índice de archivos

| Archivo | Contenido |
| --- | --- |
| [01 Visión y objetivos](01_vision_y_objetivos.md) | Problemas, actores, MVP, exclusiones y evidencia de éxito |
| [02 Descripción general](02_descripcion_general.md) | Stack elegido, responsabilidades lógicas e integraciones |
| [03 Actores y roles](03_actores_y_roles.md) | Matriz de permisos aprobados y accesos pendientes |
| [04 Modelo de datos](04_modelo_de_datos.md) | Modelo conceptual, relaciones por confirmar y datos ficticios |
| [05 Reglas de negocio](05_reglas_de_negocio.md) | Reglas con códigos y trazabilidad al Discovery |
| [06 Funcionalidades](06_funcionalidades.md) | Épicas, historias y criterios respaldados por respuestas |
| [07 Flujos principales](07_flujos_principales.md) | Turnos, cancelación, reprogramación y operación clínica |
| [08 Arquitectura propuesta](08_arquitectura_propuesta.md) | Responsabilidades, seguridad y decisiones técnicas pendientes |
| [09 Decisiones y supuestos](09_decisiones_y_supuestos.md) | Elecciones explícitas, inferencias identificadas y límites |
| [10 Preguntas abiertas](10_preguntas_abiertas.md) | Bloqueos de especificación, investigación y entrega |

## Guía de lectura para el equipo

1. Entender el producto en 01 y los permisos en 03.
2. Revisar datos y reglas en 04 y 05.
3. Relacionar historias con flujos en 06 y 07.
4. Leer el stack y los pendientes técnicos en 02 y 08.
5. Verificar el origen de cada decisión en 09 y resolver los pendientes de 10 antes de especificar el change que los requiera.

El [roadmap local](../CHANGES.md) organiza el trabajo pendiente. La KB no inicia código, no instala dependencias y no aprueba un primer change.

## Fuentes y precedencia

- [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md): decisiones posteriores que prevalecen sobre el alcance histórico en los puntos modificados.

- [Decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json): respuestas explícitas y aprobación del usuario.
- [Checklist Discovery](../docs/discovery/discovery.md): once puntos, reglas y pendientes consolidados.
- [Informe de investigación](../docs/discovery/informe-discovery.md): 15 sistemas, 38 referencias, matriz documental y conclusiones.
- [Consigna académica](../docs/desarrollo-active-stack%20Global%20%281%29.docx): requisitos del trabajo.
- [Estado de fundación](../.active-orchestrator-state.json): avance actual; el registro histórico de decisiones describe el momento de aprobación.

Las respuestas del usuario prevalecen sobre hipótesis de mercado. Las recomendaciones del informe no se convierten en requisitos. «No evidenciado» significa información no respaldada por la muestra, no ausencia de una función en el proveedor.

## Pendientes que se preservan

Este apartado describe el corte de la consolidación (19/11/2026). Después, varias preguntas se resolvieron de forma acotada para cada change: ver [09](09_decisiones_y_supuestos.md) y [10](10_preguntas_abiertas.md).

Al consolidar, el proveedor gratuito, registro de cuentas, enlaces, tratamiento de plazos y fallos, desbloqueos y compatibilidad de huecos siguen pendientes (PA-15 a PA-21). El intervalo es global y configurable, pero sus minutos iniciales no están definidos. Tampoco se fijaron estados, campos, cantidades de recursos, versiones, autenticación ni reparto nominal. La arquitectura y el modelo físico no se cierran por inferencia.

La revisión personal de al menos cinco fuentes sigue pendiente al 25/11/2026. Las comprobaciones automatizadas del agente no la sustituyen. La excepción de cuatro integrantes es una autorización docente informada por el usuario, sin verificación documental independiente.

## Estado de esta entrega

Diez documentos canónicos y este índice, generados por importación del Discovery aprobado. El contenido organiza hechos y conserva preguntas; no implica que los pendientes académicos o de especificación estén resueltos. La política visual permanece diferida y se conserva en el roadmap como condición previa al trabajo visual. La consolidación documental no inicia implementación ni envía emails.

Al 25/11/2026 el sistema visual está aprobado ([design-system](../design-system/README.md), 23/11/2026), con nombre comercial y logo sin definir, y el estado de implementación consta en el [roadmap](../CHANGES.md).
