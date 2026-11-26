# Cotejo contra la consigna

Fecha: 25/11/2026. Base revisada: `main` en `6aa8796`, más los cambios documentales de la rama `docs/entrega-academica`. Fuente: [consigna](<../desarrollo-active-stack%20Global%20(1).docx>).

Estados: **Cubierto** (hay evidencia), **Parcial**, **Pendiente**. En «Quién», «Equipo» indica trabajo personal que el agente no puede hacer ni dar por hecho.

## Modalidad y organización

| Requisito | Cobertura | Estado | Falta | Quién |
| --- | --- | --- | --- | --- |
| Grupo de hasta tres | [KB 10](../../knowledge-base/10_preguntas_abiertas.md), IN-01 y PA-10 | Parcial | Son cuatro; autorización docente informada, sin respaldo documental | Equipo |
| Líder conduce fundación y OPSX | [Bitácora](../bitacora.md) y [tareas de C-01](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/tasks.md) | Cubierto | — | — |
| Ramas por integrante y líder a cargo de la rama principal | [CONTRIBUTING](../../CONTRIBUTING.md) y [CODEOWNERS](../../.github/CODEOWNERS); 28 PR integrados al revisar (Giovanni 17, Sofia 4, Fabi 4, Luciano 3) | Cubierto | — | — |
| Secuencia y paralelismo del roadmap | [CHANGES](../../CHANGES.md) y [plan de colaboración](../plan-colaboracion.md) | Cubierto | — | — |
| Participación demostrable | Commits propios en `main`, sin contar merges: Giovanni 82, Fabi 34, Sofia 8, Luciano 7 | Parcial | Video | Equipo |

## Etapas

| Requisito | Cobertura | Estado | Falta | Quién |
| --- | --- | --- | --- | --- |
| 0. Herramientas y agente | [Preparación del entorno](../preparacion-entorno.md) | Parcial | Se fundó con Codex; la consigna nombra OpenCode o Claude Code. Declararlo | Equipo |
| 0. Active Stack Full | [Registro de instalación](../evidencias/active-stack-codex-full.log), con compilación local parcheada | Cubierto | — | — |
| 0. Capturas del instalador y los harnesses | [Captura](../evidencias/captura-instalador.png) con ambas salidas, tomadas del registro de instalación | Cubierto | — | — |
| 0. Repositorio nuevo | [Bitácora](../bitacora.md) | Cubierto | — | — |
| 1. `active-orchestrator` y `openspec init` | [Estado](../../.active-orchestrator-state.json) y [log](../evidencias/openspec-init-retry.log) | Cubierto | — | — |
| Inspección por etapa, con decisión y motivo | [Recopilación](inspecciones.md) | Parcial | Casi ningún motivo quedó escrito | Equipo |
| 2. Informe con A–D, 15 sistemas, URL, fecha y «No evidenciado» | [Informe](../discovery/informe-discovery.md): 15 sistemas, 38 referencias; [correcciones del 25/11](verificacion-fuentes.md#correcciones-aplicadas) aplicadas | Parcial | Hay datos de la consigna de investigación sin relevar de forma sistemática, declarados en «Alcance y método» | Equipo decide |
| 2. Checklist de once puntos | [discovery.md](../discovery/discovery.md) | Cubierto | — | — |
| 3. Diez documentos de KB, reglas de agenda y preguntas | [Índice](../../knowledge-base/README.md) | Cubierto | — | — |
| 4. Roadmap alineado con el MVP | [CHANGES](../../CHANGES.md) | Cubierto | Estados corregidos en esta rama | — |
| 5. `find-skill` con justificación | [Recomendaciones](../recomendaciones-skills.md) | Cubierto | — | — |
| 5. Registro de skills | [skill-registry.md](../../.atl/skill-registry.md) | Cubierto | Describe un solo equipo | — |
| 5. `AGENTS.md` y `CLAUDE.md` con regla de dominio | [AGENTS](../../AGENTS.md) y [CLAUDE](../../CLAUDE.md), idénticos | Cubierto | Quedan frases anteriores a decisiones posteriores (ver abajo) | Equipo decide |
| 6. Elección justificada | [proposal.md](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/proposal.md), «Why» | Cubierto | — | — |
| 6. Explore guardado | Contexto del [diseño](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/design.md) y [Engram](engram.md) | Parcial | No hay artefacto propio; no se crea uno retroactivo | Equipo lo declara |
| 6. Propose revisado, con tres o más escenarios | [Spec](../../openspec/specs/disponibilidad-de-turnos/spec.md): 7 requisitos y 17 escenarios WHEN/THEN | Parcial | Los escenarios están. El [relato confirmado](inspecciones.md#relato-confirmado-de-c-01) indica que no hubo cambios en propuesta ni especificación; la consigna pide mostrar la corrección con «Ajustar» | Equipo lo declara |
| 6. Apply fiel y tests por escenario | [Trazabilidad](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/evidence/trazabilidad.md) y logs RED/GREEN | Cubierto | — | — |
| 6. Verify en verde | [Verify del 19/11](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/evidence/verify.md) y [corrida del 25/11](verificacion-clon-limpio.md) | Cubierto | — | — |
| 6. Archive y estado actualizado | [Change archivado](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/proposal.md) | Cubierto | Hay otro change activo, sin aplicar | Declararlo |
| 7. Qué guardó Engram | [Registro](engram.md) | Cubierto | — | — |
| 7. Consulta real y explicación | [Consulta](engram.md#consulta-real) | Parcial | Mostrarla y explicarla en el video | Equipo |

## Entregables

| Requisito | Cobertura | Estado | Falta | Quién |
| --- | --- | --- | --- | --- |
| Repositorio con la estructura mínima | Raíz, `docs/`, `knowledge-base/`, `openspec/`, `.atl/`; código y pruebas en `backend/` y `frontend/` | Cubierto en la rama | Publicar e integrar | Giovanni |
| Clonar y probar solo con el README | [README](../../README.md) y [corrida](verificacion-clon-limpio.md) | Cubierto en la rama | La corrida usó dependencias ya descargadas | — |
| Historial que muestre el proceso | Commits desde el 19/11 | Cubierto | La propuesta de C-01 comparte commit con la implementación | Declararlo |
| Sin secretos | Búsqueda por patrones de claves en archivos e historial de `main`, sin hallazgos; de los archivos `.env` solo figura `.env.example` | Cubierto con alcance limitado | No equivale a una auditoría | — |
| PDF: portada | [PDF](../discovery/informe-discovery.pdf), página 1 | Cubierto | — | — |
| PDF: resumen ejecutivo de una página como máximo | Página 2 | Cubierto | Describe el corte del 19/11, y lo dice | — |
| PDF: tabla A, matriz B con nota y análisis C | Sección A con funciones principales, matriz B y análisis C en las cuatro subsecciones, con cifras de la matriz | Cubierto | — | — |
| PDF: D con cinco competidores, tres referencias y MVP en tres listas | Sección D | Cubierto | — | — |
| PDF: verificación personal de fuentes | [Planilla](verificacion-fuentes.md#planilla-personal) e informe: cinco fuentes de cinco tipos, cuatro integrantes | Cubierto | — | — |
| PDF: referencias completas | Sección final | Cubierto | — | — |
| Video de 8 a 12 minutos | — | **Pendiente** | Grabación | Equipo |
| Reflexión de 1 a 2 páginas | [reflexion.md](reflexion.md): cinco preguntas y declaración de uso de IA | Parcial | Confirmar que la redacción es propia | Equipo |

## Consideraciones

| Requisito | Estado |
| --- | --- |
| Datos ficticios | Sin correos de dominios personales en archivos versionados; no se revisaron las imágenes una por una |
| Sin contactar proveedores ni crear cuentas | Declarado en el informe; la comprobación del 25/11 tampoco lo hizo |
| Uso de IA declarado en la reflexión | Incluido en la [reflexión](reflexion.md) |
| Un change completo antes que varios a medias | C-01 completo; el backlog restante no es exigido |

## Detectado y no modificado

- `AGENTS.md` y `CLAUDE.md` todavía piden mantener el proveedor de email sin elegir, cuando la [KB 09](../../knowledge-base/09_decisiones_y_supuestos.md) registra Resend desde el 24/11. Es una regla dura: cambiarla requiere decisión del equipo.
- Los mismos archivos describen la política visual como diferida, aunque el sistema visual figura aprobado el 23/11.
- La [bitácora](../bitacora.md) no registra las decisiones tomadas entre el 21 y el 25/11 antes de esta documentación; constan en OpenSpec y en la KB.
- La interfaz guarda el token de sesión en `localStorage` ([DD-16](../../knowledge-base/09_decisiones_y_supuestos.md#dd-16--sesión-interna-en-cookie-httponly-25112026)).
- Control de enlaces locales en los Markdown versionados: fallan 16 dentro de artefactos archivados de OpenSpec, que se conservan como registro, y los 87 absolutos del registro de skills, que solo resuelven en la máquina donde se generó. El resto resuelve.
