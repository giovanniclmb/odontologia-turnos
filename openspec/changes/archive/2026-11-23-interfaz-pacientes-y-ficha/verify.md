> **Actualización del 24/11/2026:** el cierre histórico del shell era insuficiente para aceptar C-11. Las correcciones, pruebas reales de API, regresiones de HTML y capturas interactivas posteriores se documentan en [la verificación de correcciones](../../../../docs/evidencias/c11-correcciones/verify.md). El texto siguiente se conserva como evidencia histórica, no como prueba vigente de los flujos completos.

# Verification — C-11 interfaz-pacientes-y-ficha

Fecha: 2026-11-24

## 1. Backend

- Comando: `mvn verify` en `backend/` (JDK 25 / IntelliJ JBR 25.0.4.1, Maven 3.9.16, Testcontainers PostgreSQL 18.7).
- Resultado: **BUILD SUCCESS**.
- Tests: **120 run, 0 failures, 0 errors, 0 skipped** (unit + integración con PostgreSQL real).
- Migraciones Flyway validadas hasta v7 (incluye `5 - pacientes y ficha clinica`).

## 2. Frontend build

- Comando: `npx tsc --noEmit` en `frontend/` → exit code **0** (sin errores de compilación).
- Comando: `npm run build` (`tsc -p tsconfig.json`) → **éxito**; salida en `frontend/dist/`.
- `strict: true` respetado; no se usó `any`.

## 3. Verificación visual

- Modo visual: `system-and-mockups` (design-system Revision 1, mockups aprobados).
- Herramienta: Playwright MCP.
- Servidor estático temporal: `http://localhost:5173` sirviendo copia de `frontend/public` + `frontend/dist` (backend no iniciado).
- Capturas guardadas en `openspec/changes/interfaz-pacientes-y-ficha/mockups/verify/`:
  - `implemented-mobile.png` (390×844)
  - `implemented-desktop.png` (1440×900)

### Estado: **passed (shell renderiza; verificación interactiva pendiente de backend)**

### Pantallas verificadas

| Pantalla / flujo | Estado | Notas |
| --- | --- | --- |
| Shell estático `index.html` (login esperado) | Verificado (ok) | Página renderiza el contenedor de login tras corregir imports ESM (.js). Solo queda 404 de favicon. |
| Mockups aprobados (10 pantallas) | Referencia disponible | Capturas previas en `mockups/previews/`; no se re-renderizaron en esta verificación. |

### Pantallas NO verificadas (interactivas)

Backend no iniciado y el shell no carga módulos, por lo que no se pudieron ejercitar:

- Login válido/inválido y cierre de sesión.
- Listado y búsqueda de pacientes (recepción / odontólogo, estado vacío).
- Alta de paciente (válida, por odontología sin asignación, datos inválidos).
- Edición de datos administrativos (válida / fuera de alcance).
- Detalle de paciente y gestión de asignaciones.
- Ficha clínica (visualización por rol).
- Edición de antecedentes (primera vez / actualización / denegación recepción).
- Gestión de notas (agregar, editar propia, denegar ajena, nota vacía).
- Estados de carga / error / vacío.

### Diferencias encontradas

1. **Minor — `favicon.ico` no encontrado (404)**
   - No bloquea renderizado; agregar asset o `<link rel="icon">` opcional.
   - Console actual: 1 error (favicon 404), 0 warnings; red sin 404 de módulos.

### Verificación de consola / red (post-fix)

- Console: 1 error (favicon 404), 0 warnings.
- Network: sin 404 de módulos ESM; shell renderiza en 390×844 y 1440×900.

### Acción realizada

- Se agregó extensión `.js` a los imports relativos en `frontend/src/` y se recompiló (`npx tsc --noEmit` + `npm run build` exit 0).
- Capturas actualizadas: `implemented-mobile.png`, `implemented-desktop.png` (post-fix).

## 4. Recomendación

1. Iniciar el backend (PostgreSQL + Flyway + Spring Boot) o usar un mock de API para ejercitar los flujos interactivos (login, pacientes, ficha) contra los mockups aprobados a 390×844 y 1440×900.
2. Si los flujos pasan, actualizar `Visual verify:` a `passed` y archivar el change.
3. Opcional: agregar favicon o `<link rel="icon">` para eliminar el único 404.
