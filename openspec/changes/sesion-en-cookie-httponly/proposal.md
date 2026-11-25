# Proposal

## Why

La interfaz guarda el token de sesión y la identidad en `localStorage` (`frontend/src/api/client.ts` y `frontend/src/auth/auth.ts`), legibles por cualquier script de la página, contra la regla de seguridad del proyecto y sin excepción documentada. La deuda se señaló en el diseño de C-10 (25/11/2026), que reutiliza el mecanismo de C-11 sin ampliarlo.

El 25/11/2026 el usuario eligió resolverla con cookie `HttpOnly` y token anti-CSRF propio. Esa elección aprueba esta propuesta, no su implementación.

## What Changes

- **BREAKING** El token de sesión viaja solo en una cookie `HttpOnly`, `Secure` y `SameSite=Strict`. El login deja de devolverlo en el cuerpo y la API deja de aceptar `Authorization: Bearer`.
- Las operaciones con efecto exigen un token anti-CSRF ligado a la sesión.
- Nueva consulta de la sesión vigente para recuperar identidad y token anti-CSRF al recargar la página.
- La API admite solicitudes con credenciales solo desde orígenes configurados explícitamente.
- La interfaz no guarda token ni identidad en almacenamiento legible por JavaScript y borra los restos que dejó la versión anterior.
- El cierre de sesión vuelve al login solo cuando el servidor confirma el cierre o la sesión ya no era válida.

### Alternativas descartadas

| Alternativa | Costo | Motivo para no elegirla |
| --- | --- | --- |
| Token solo en memoria | Bajo, solo interfaz | Cada recarga o pestaña nueva pide login y el token sigue al alcance de un script inyectado |
| Excepción documentada para `localStorage` | Muy bajo; para ser defendible suma CSP y menor expiración | La deuda se mantiene: un script inyectado puede extraer el token y usarlo hasta que venza |
| Cookie con Spring Security | Alto: dependencia, cadena de filtros, login, CORS, errores y pruebas | Amplía el alcance a reemplazar la autenticación vigente y choca con C-10 |
| `sessionStorage` | Muy bajo | Sigue legible por JavaScript y requiere la misma excepción; no se sometió como opción |

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `acceso-y-permisos-internos`: el token pasa del encabezado `Authorization` a una cookie; se agregan protección anti-CSRF, consulta de sesión y orígenes con credenciales.
- `interfaz-pacientes-y-ficha`: la sesión de la interfaz deja de conservar el token, se restaura desde el servidor y cambia el cierre de sesión.

## Impact

- Backend: filtro de autenticación, `AuthController`, configuración CORS y pruebas HTTP que usan `Bearer` (incluidas las que agrega C-10).
- Interfaz: cliente HTTP, módulo de autenticación, arranque de `main.ts` y sus pruebas.
- Sin dependencias nuevas, migraciones ni cambios de roles o permisos.
- Contrato incompatible: backend e interfaz se publican juntos.
- Restringe el despliegue futuro: interfaz y API deben compartir sitio, con HTTPS fuera de loopback.
- Orden: Apply recién después de integrar C-10 (`feature/c-10-interfaz-agenda-y-alta`) y la corrección de CORS en el `401` por sesión vencida, que tocan los mismos archivos.
- Hasta ese Apply la deuda sigue abierta; este change no documenta una excepción para el estado actual.
- No incorpora sesión de pacientes (C-16, C-24), enlaces de cancelación, Spring Security, CSP ni despliegue.
