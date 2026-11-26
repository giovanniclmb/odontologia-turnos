# Entrega del 25/11/2026

Seguimiento de lo que pide la [consigna](<../desarrollo-active-stack%20Global%20(1).docx>). **La entrega no está completa:** faltan los trabajos personales de la tabla siguiente.

## Pendiente del equipo

| Pendiente | Puntos de la rúbrica | Cómo cerrarlo | Estado |
| --- | --- | --- | --- |
| Video de defensa, 8 a 12 minutos | 10 | Todos hablan y muestran cámara al presentarse; pantalla trabajando. Orden: Discovery y MVP; KB, roadmap y `AGENTS.md`; revisión del Propose; Apply y Verify; Archive; consulta a Engram; conclusiones | Pendiente |
| Revisión final de la [reflexión](reflexion.md) | 5 | Confirmar entre los integrantes que la redacción es propia y que refleja lo que hizo cada uno | Texto incorporado; falta confirmar |
| Respaldo de la excepción para trabajar de a cuatro | Modalidad | La consigna fija un máximo de tres; adjuntarlo si la cátedra lo pide (PA-10) | Pendiente |

## Ya reunido

- **Etapa 0:** [captura del instalador finalizado y de los harnesses](../evidencias/captura-instalador.png), en una sola imagen. Las salidas provienen del registro de una instalación ya hecha, sobre Codex, no de una nueva.
- **C-01:** [relato de la revisión de la propuesta](inspecciones.md#relato-confirmado-de-c-01), confirmado por Giovanni el 25/11.

## A declarar en la defensa o la reflexión

- La fundación se hizo con **Codex**, por decisión registrada en [preparación del entorno](../preparacion-entorno.md); la consigna nombra OpenCode o Claude Code. Parte del trabajo posterior se hizo con Claude Code.
- No hay un artefacto propio de **Explore** para C-01: el relevamiento consta en el contexto del [diseño](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/design.md) y en la memoria de Engram. No se creó uno después.
- En C-01 **no hubo un «Ajustar»** sobre la propuesta ni la especificación: las decisiones de alcance se tomaron antes de redactarlas.
- La propuesta de C-01 y su implementación entraron en el mismo commit (`fe41275`). La fundación del 17/11 se versionó a partir del 19/11.
- Queda activo, sin aplicar, el change [`sesion-en-cookie-httponly`](../../openspec/changes/sesion-en-cookie-httponly/proposal.md), ajeno al change académico.
- **Deuda de seguridad conocida:** la interfaz guarda el token de sesión en `localStorage` ([DD-16](../../knowledge-base/09_decisiones_y_supuestos.md#dd-16--sesión-interna-en-cookie-httponly-25112026)).
- El informe declara en «Alcance y método» los datos que no se relevaron de forma sistemática y que no se consultaron videos, marketplaces ni reseñas verificadas.
- El nombre comercial y el logo son provisionales.
- El backlog no está completo: C-14 y C-22 a C-25 siguen pendientes. La consigna exige un solo change.

## Documentos

| Documento | Contenido |
| --- | --- |
| [Cotejo contra la consigna](cotejo.md) | Cada requisito, su cobertura, estado y responsable |
| [Verificación de fuentes](verificacion-fuentes.md) | Revisión personal de cinco fuentes, correcciones del agente y detalle del importe de tab32 |
| [Inspecciones](inspecciones.md) | Decisiones Continuar o Ajustar que constan por etapa |
| [Engram](engram.md) | Qué hay guardado y una consulta real |
| [Verificación en un clon limpio](verificacion-clon-limpio.md) | Resultados de las pruebas siguiendo el README |

## Enlaces a los entregables

- Repositorio: <https://github.com/giovanniclmb/odontologia-turnos>
- Informe de Discovery: [Markdown](../discovery/informe-discovery.md) y [PDF](../discovery/informe-discovery.pdf)
- Change académico: [C-01 archivado](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/proposal.md)
- Video: pendiente.
- Reflexión: [texto incorporado](reflexion.md), con la revisión final pendiente.

Estos documentos se publicaron mediante el [PR 55](https://github.com/giovanniclmb/odontologia-turnos/pull/55). El enlace público muestra el contenido de `main`: antes de entregar, confirmar que lo incluye.
