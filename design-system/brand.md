# Marca — Sistema de turnos odontológicos

## Nombre

- **Nombre comercial:** (TBD) no definido.
- **Descriptor del producto:** Sistema de turnos odontológicos.
- **Nombre de trabajo provisional en la UI:** "Consultorio odontológico (nombre por definir)" (configurable en `brand.js`).
- Fuente: no existe un nombre comercial aprobado en KB ni Discovery; se mantiene como (TBD) según regla de no inventar hechos de marca.

## Tono visual

- Profesional, claro y sobrio. Orientado a uso intensivo de formularios y tablas.
- Sin adornos decorativos ni ilustraciones complejas. Prioridad a legibilidad y escaneo rápido.
- Densidad media-alta en escritorio; espaciado cómodo en móvil.
- Estados visibles y no ambiguos (éxito, advertencia, error, información).

## Logo

- Archivo estable: `assets/logo.svg`.
- Estado: **placeholder provisional**, visiblemente marcado como "LOGO (TBD)".
- Reemplazo: sustituir el archivo manteniendo la ruta. Si el logo real tiene proporción distinta, ajustar el contenedor en `components.html` y documentar en `README.md`.
- No usar el placeholder como si fuera marca aprobada.

## Imaginería

- No se usan fotos de pacientes ni imágenes clínicas reales.
- Iconografía lineal simple (SVG inline), monocromática, heredando `currentColor`.
- Evitar ilustraciones que sugieran una identidad no aprobada.

## Color

- Primario: teal clínico (`--color-primary`), usado para acciones principales y foco.
- Neutros: slate para texto, bordes y superficies.
- Semánticos: éxito (verde), advertencia (ámbar), error (rojo), información (azul).
- El color nunca es el único indicador de estado; acompañar con texto/icono.

## Tipografía

- Pila del sistema (sin fuentes remotas): Segoe UI, Helvetica Neue, Arial, sans-serif.
- Monoespaciada para códigos y datos técnicos: Consolas, Monaco, monospace.
- Escala definida en `tokens.css` (`--text-*`).

## Voz y copy

- Español rioplatense neutro, directo y claro.
- Etiquetas de acción en infinitivo ("Guardar", "Cancelar", "Buscar").
- Mensajes de error explican la causa y la acción sugerida.
- No usar jerga técnica en la UI del paciente.

## Accesibilidad

- Contraste mínimo AA para texto normal (4.5:1) y grande (3:1).
- Foco visible en todos los elementos interactivos.
- Soporte a `prefers-reduced-motion`.
- No depender solo del color para comunicar estado.
- Formularios con `label` asociado y mensajes de error vinculados (`aria-describedby`).

## Reglas de uso

- Usar tokens de `tokens.css`; no hardcodear colores/espaciados en componentes.
- Reutilizar componentes de `components.html` como referencia; adaptar sin romper la base.
- Al cambiar un token compartido, incrementar `Revision` y re-renderizar previews.