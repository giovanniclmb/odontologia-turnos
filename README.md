# Odontología Turnos

Proyecto integrador de Desarrollo de Software de UTN: sistema de gestión de turnos para un consultorio de una sede con varios odontólogos.

**Estado:** C-01 elegido por Giovanni, aprobado e implementado como dominio Java aislado. Sus 55 pruebas y `clean verify` pasaron. Está pendiente de archivo e integración en main; el resto del roadmap describe trabajo futuro, no una aplicación ya disponible.

## Alcance aprobado

- Agenda diaria y semanal por sillón, con profesional y sillón obligatorios, duración variable e intervalo global configurable.
- Bloqueos por recepción de sillón y/o profesional; se rechazan si afectan turnos existentes.
- Gestión de pacientes y ficha sencilla con antecedentes y notas. Recepción puede leerla; solo odontólogos la editan.
- Creación y reprogramación de turnos por el equipo, cancelación y confirmaciones manuales.
- Email al crear el turno con enlace para cancelar hasta 48 horas antes; recordatorio automático 24 horas antes, sin opción de cancelar cuando venció el plazo.
- Cuenta del paciente para gestionar su inscripción en lista de espera y sus preferencias. Avisos automáticos por email; recepción confirma quién toma cada hueco. El aviso no reserva el horario.

La autorreserva general, WhatsApp automático, odontograma, pagos y facturación están fuera del MVP. El presupuesto externo es cero; el proveedor gratuito de email todavía no fue elegido. Los ejemplos, pruebas y capturas deben usar datos clínicos ficticios; los emails de demostración se dirigirán a direcciones de prueba indicadas por el equipo.

## Stack

| Capa | Tecnología elegida | Estado |
| --- | --- | --- |
| Frontend | HTML, CSS y TypeScript vanilla | Versión y herramientas de build pendientes |
| Backend | Java 25; Spring Boot previsto para C-02 | Maven y JUnit en C-01; versión Spring Boot pendiente |
| Base de datos | PostgreSQL | Versión, esquema y ejecución pendientes |

Las herramientas de fundación ya verificadas se documentan en [preparación del entorno](docs/preparacion-entorno.md). Sus versiones observadas no constituyen versiones mínimas del producto.

## Comenzar a trabajar

```sh
git clone https://github.com/giovanniclmb/odontologia-turnos.git
cd odontologia-turnos
```

1. Leer [AGENTS.md](AGENTS.md) y el [índice de la KB](knowledge-base/README.md).
2. Consultar [CHANGES.md](CHANGES.md), sus dependencias y las preguntas abiertas del item elegido.
3. Acordar el change, su alcance y los criterios de aceptación antes de implementar. La consigna requiere completar un change mediante el ciclo OpenSpec.
4. Usar el cliente y las herramientas disponibles en el entorno; el repositorio no instala configuraciones globales, MCP ni skills automáticamente.

`CLAUDE.md` conserva contenido idéntico a `AGENTS.md` por requisito académico. El registro local de skills se genera con `skill-registry` en `.atl/skill-registry.md` y está ignorado por Git: regenerarlo en cada entorno, verificando las skills realmente instaladas. La [evidencia del inventario original](docs/evidencias/registro-skills-verificacion.json) conserva la revisión realizada; sus rutas absolutas y hashes corresponden a ese entorno, no prueban disponibilidad en otra máquina.

## Documentación

| Fuente | Contenido |
| --- | --- |
| [Consigna](<docs/desarrollo-active-stack Global (1).docx>) | Requisitos académicos originales |
| [Discovery](docs/discovery/discovery.md) | Checklist y decisiones vigentes |
| [Ampliación aprobada](docs/discovery/ampliacion-paciente.md) | Paciente, emails, lista de espera y bloqueos |
| [Informe de investigación](docs/discovery/informe-discovery.md) / [PDF](docs/discovery/informe-discovery.pdf) | Comparación de 15 sistemas y 38 fuentes |
| [Knowledge base](knowledge-base/README.md) | Diez documentos canónicos del dominio y la arquitectura pendiente |
| [Roadmap](CHANGES.md) | 25 items pendientes con dependencias y alcance |
| [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) | Decisiones que necesitan resolverse antes del change afectado |
| [Bitácora](docs/bitacora.md) | Decisiones y verificaciones de cada etapa |
| [Evidencias](docs/evidencias/README.md) | Alcance y conservación de registros técnicos |

## Ejecución y pruebas

C-01 incorpora [el módulo de dominio](backend/README.md), con JDK 25 y Maven como requisitos. Desde la raíz: `mvn -f backend/pom.xml test` o `mvn -f backend/pom.xml clean verify`. Todavía no hay servidor, API, interfaz ni PostgreSQL. La primera ejecución descarga las dependencias de pruebas. La [evidencia Verify](openspec/changes/crear-turno-sin-solapamientos-dominio/evidence/verify.md) conserva resultados y límites.

OpenSpec ya tiene su configuración documental en [openspec/config.yaml](openspec/config.yaml) y seis skills de proyecto en `.agents/skills/`. Hay un change activo: `crear-turno-sin-solapamientos-dominio`, implementado y verificado, pendiente de archivo. El CLI OpenSpec debe estar disponible en el entorno para trabajar con ese flujo; su instalación no se realiza al clonar.

Los scripts de Discovery son utilidades documentales, no pruebas del producto. [compilar_investigacion.py](docs/discovery/compilar_investigacion.py) usa Python y el helper global `web-scraper/scripts/slugify.py` de `~/.agents/skills/`. [generar_pdf.py](docs/discovery/generar_pdf.py) usa ReportLab; el PDF requiere además renderizado e inspección visual después de regenerarlo. Si faltan esas dependencias, los documentos ya generados pueden consultarse sin ejecutar los scripts.

## Trabajo del equipo

El equipo tiene cuatro integrantes con autorización docente informada; nombres y reparto pendientes. La entrega está prevista para el 25/11/2026. Completar el backlog entero no es un requisito del único change académico.

Se permiten compilación, pruebas y commits autónomos dentro del alcance autorizado. Cada commit debe cubrir una tarea específica y tener un mensaje detallado que explique el cambio y su propósito. **El push requiere un pedido explícito del usuario.** Las reglas completas y los permisos por dominio están en las instrucciones aplicables; esta política no aprueba automáticamente cambios críticos.

La revisión personal de al menos cinco fuentes, la reflexión de los integrantes y demás evidencias humanas siguen pendientes. Las verificaciones del agente no las sustituyen.
