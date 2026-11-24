# Design system — Sistema de turnos odontológicos

Mode: system-and-mockups
Revision: 1
Approval: approved (2026-11-23, by user)
Created: 2026-11-23
Created by: design-system skill

## Propósito

Fundación visual portable para el proyecto. Define tokens, marca provisional, componentes de referencia y reglas de uso para que las interfaces de C-11 y cambios visuales posteriores reutilicen una misma base. Se entrega como archivos locales (HTML/CSS/JS/SVG), sin dependencias remotas, servicios pagos ni herramientas propietarias.

## Política visual del proyecto

- Modo durable: `system-and-mockups`.
- En este modo, cada cambio visual requiere un sistema aprobado y un mockup por cambio antes de Apply.
- Esta fundación **no está aprobada todavía**. No se crean mockups de C-11 hasta que el usuario apruebe esta revisión.
- La política está registrada también en `.active-orchestrator-state.json` (`visual_design.mode = system-and-mockups`, `status = pending`).

## Fuentes

- `knowledge-base/01_vision_y_objetivos.md` — propósito, actores, alcance MVP.
- `knowledge-base/02_descripcion_general.md` — stack (HTML/CSS/TS vanilla; sin framework de UI).
- `knowledge-base/03_actores_y_roles.md` — recepción, odontólogos, pacientes y permisos.
- `knowledge-base/06_funcionalidades.md` — US-001 a US-003 (primer cambio visual C-11).
- `AGENTS.md` / `CLAUDE.md` — reglas duras (RA-01 strict TS, datos ficticios, presupuesto cero).
- `openspec/changes/interfaz-pacientes-y-ficha/design.md` — política visual diferida y base frontend.
- `CHANGES.md` — C-11 (interfaz-pacientes-y-ficha) como primer cambio visual pendiente.

## Brief de diseño

- **Producto:** sistema web de turnos para un consultorio odontológico de una sede, con agenda, pacientes/ficha clínica, emails y lista de espera.
- **Audiencia principal:** recepción y odontólogos (uso profesional, escritorio). Pacientes usan móvil para cancelación por enlace y cuenta de lista de espera.
- **Tareas clave:** iniciar sesión; buscar/crear/editar pacientes; consultar ficha clínica; gestionar antecedentes y notas según rol; operar agenda (pendiente en cambios posteriores).
- **Plataformas:** desktop mayoritario para el equipo; mobile para pacientes (390×844). El sistema debe ser responsive.
- **Referencias UX:** Dentalink, AgendaPro, Dentrix (solo referencia; no se copian marcas).
- **Restricciones:** presupuesto externo cero; sin frameworks de UI; sin fuentes remotas ni servicios pagos; TypeScript strict; datos clínicos ficticios en ejemplos.
- **Tono:** profesional, claro, denso pero legible, sin adornos. Prioridad a legibilidad de tablas y formularios.

## Archivos

- `brand.md` — identidad, tono, logo y reglas de uso.
- `brand.js` — constante local del nombre y binding para `[data-brand-name]`.
- `tokens.css` — variables de color, tipografía, espaciado, radios, sombras, foco y movimiento.
- `components.html` — referencia de componentes y estados.
- `assets/logo.svg` — logo provisional, visiblemente marcado como placeholder.
- `previews/` — capturas mobile (390×844) y desktop (1440×900).

## Decisiones abiertas (TBD)

1. **Nombre comercial del consultorio** — no definido. Se usa el descriptor provisional "Consultorio odontológico (nombre por definir)".
2. **Logo** — placeholder provisional; reemplazar por el logo real en `assets/logo.svg`.
3. **Imágenes/ilustraciones** — no definidas; se evitan fotos de pacientes.
4. **Paleta final** — propuesta provisional (teal clínico + slate). Requiere aprobación o ajuste.
5. **Tipografía** — pila de fuentes del sistema (sin fuentes remotas). Si se quiere una fuente específica, debe ser local/OSS y agregarse a `assets/`.
6. **Estados exactos del turno y sus colores** — pendiente (PA-02). Los badges de estado son provisionales.
7. **Cantidad de recursos (profesionales/sillones)** — pendiente (PA-04); la UI debe tolerar listas largas.

## Instrucciones de reemplazo

- **Nombre:** editar la constante `BRAND_NAME` en `brand.js`; se actualiza en todas las vistas que usen `[data-brand-name]`.
- **Logo:** reemplazar `assets/logo.svg` manteniendo el mismo path y tamaño recomendado (160×48 o equivalente). Actualizar `brand.md` si cambia el estilo.
- **Colores/tipografía/espaciado:** editar `tokens.css`. Al cambiar tokens compartidos, incrementar `Revision`, re-renderizar `components.html`, identificar mockups afectados y solicitar nueva aprobación.
- **Componentes:** editar `components.html`; mantener importación de `tokens.css` y `brand.js`.

## Vista previa

- `previews/components-mobile.png` (390×844) — generado automáticamente.
- `previews/components-desktop.png` (1440×900) — generado automáticamente.
- Archivo de referencia: `components.html` (abrir localmente en el navegador).

## Aprobación

Approval: approved (2026-11-23, by user)

Para aprobar, responder explícitamente. Al aprobar se actualizará `Approval: approved (<fecha>, por usuario)` y `visual_design.status: approved` en el estado. Un sistema usable puede aprobarse con elementos (TBD) visiblemente provisionales.