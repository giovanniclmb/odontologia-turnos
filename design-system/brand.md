# Marca — Sistema de turnos odontológicos

## Nombre

- **Nombre de la aplicación:** sonrisa.app, en minúsculas. Definido por el usuario el 25/11/2026.
- **Descriptor del producto:** Sistema de turnos odontológicos.
- Se configura en la constante `BRAND_NAME` de `brand.js`.

## Tono visual

- Profesional, claro y sobrio. Orientado a uso intensivo de formularios y tablas.
- Sin adornos decorativos ni ilustraciones complejas. Prioridad a legibilidad y escaneo rápido.
- Densidad media-alta en escritorio; espaciado cómodo en móvil.
- Estados visibles y no ambiguos (éxito, advertencia, error, información).

## Logo

- Archivo estable: `assets/logo.svg`, también usado como icono de la pestaña.
- Icono cuadrado: una muela blanca con una sonrisa sobre el color primario (`#0f766e`), pedido por el usuario el 25/11/2026 como diseño simple y minimalista.
- Proporción 1:1; legible desde 16 px. Acompañado por el nombre, la imagen usa texto alternativo vacío.
- El color está escrito en el SVG: si cambia `--color-primary`, actualizarlo también ahí.

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