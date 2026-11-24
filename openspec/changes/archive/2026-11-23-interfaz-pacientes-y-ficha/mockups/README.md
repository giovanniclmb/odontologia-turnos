# Mockups — C-11 interfaz-pacientes-y-ficha

Approval: approved (2026-11-23, by user)
Preview: rendered (2026-11-23, 18 captures in previews/)

## Pantallas

| Archivo | Descripción | Escenarios de spec |
| --- | --- | --- |
| `login.html` | Formulario de login + estado de rechazo genérico. | Inicio de sesión y sesión autenticada (Login válido/inválido/Cierre de sesión); Autenticación por token opaco. |
| `pacientes-lista.html` | Listado + búsqueda; vista recepción (todo el sitio) y odontólogo (asignados); estado vacío. | Listado y búsqueda de pacientes (Recepción lista pacientes del sitio; Odontología lista pacientes asignados; Búsqueda parcial). |
| `paciente-alta.html` | Alta de paciente (código, nombre, apellido, email obligatorios; teléfono opcional) + error de datos inválidos. | Alta de paciente (Alta válida por recepción; Alta por odontología sin asignación; Datos inválidos). |
| `paciente-edicion.html` | Edición de datos administrativos (código solo lectura) + denegación fuera de alcance. | Edición de datos administrativos (Edición válida; Edición fuera de alcance). |
| `paciente-detalle.html` | Detalle + profesionales asignados; gestión de asignaciones visible solo recepción. | Detalle de paciente (Detalle autorizado; Detalle no autorizado); Asignación de profesionales (Recepción asigna/quita; Odontología no gestiona). |
| `ficha-clinica.html` | Visualización de antecedentes y notas; recepción solo lectura; odontólogo con edición. | Visualización de ficha clínica (Recepción visualiza; Odontología asignada visualiza; Odontología no asignada denegada). |
| `antecedentes-edicion.html` | Edición de antecedentes (textarea + guardar; crea ficha la primera vez) + denegación recepción. | Edición de antecedentes (Guardar por primera vez; Actualizar; Recepción no edita). |
| `notas.html` | Listado de notas con autor/fecha; agregar nota; editar nota propia; denegar ajena; nota vacía. | Gestión de notas (Agregar; Editar propia; Editar ajena; Nota vacía). |
| `estados.html` | Estados de carga, error y vacío. | Gating por rol; Ausencia de eliminación. |

## Fuentes

- `openspec/changes/interfaz-pacientes-y-ficha/specs/...` (escenarios citados arriba).
- `design-system/` Revision 1 (tokens, brand.js, assets).
- `knowledge-base/03_actores_y_roles.md`, `06_funcionalidades.md`.

## Pasos de vista previa

1. Abrir cada archivo HTML en un navegador (doble clic o servidor estático).
2. Verificar a 390×844 y 1440×900.
3. Revisar gating por rol (toggle Recepción/Odontólogo).
4. Confirmar ausencia de acciones de eliminación.

## Capturas

Se guardan en `previews/` con nombres `<pantalla>-mobile.png` y `<pantalla>-desktop.png`.

## Aprobación

Approval: pending (requiere revisión explícita del usuario).