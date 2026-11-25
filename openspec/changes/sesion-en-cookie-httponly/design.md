# Design

## Context

Ver [proposal.md](proposal.md). Estado en `main` (401f78f) y en la rama de C-10:

- `POST /auth/login` devuelve un token opaco aleatorio; PostgreSQL guarda su SHA-256 en `token_acceso`, con expiración configurable (`app.token.expiracion-horas`, 24) y revocación al cerrar sesión.
- `TokenAutenticacionFilter` lee `Authorization: Bearer`; la autorización vive en los servicios. No se usa Spring Security, solo `spring-security-crypto`.
- En desarrollo la interfaz se sirve en `http://localhost:5173` y la API en `http://localhost:8080`: orígenes distintos, mismo sitio. No hay HTTPS ni despliegue (PA-09).
- CORS admite una lista de orígenes, sin credenciales.

## Goals / Non-Goals

**Goals:**

- Que ningún script de la página pueda leer el token de sesión.
- Cumplir la regla de cookies con `HttpOnly`, `Secure`, `SameSite` y token anti-CSRF, sin dependencias ni migraciones.

**Non-Goals:**

- Spring Security, cadena deny-by-default, CSP u otras cabeceras de seguridad.
- Cambiar expiración, roles, permisos, emisión o persistencia del token.
- Rotación o renovación de sesión, sesiones de pacientes y despliegue.
- Refactorizar la interfaz o migrar errores a `ProblemDetail`.

## Decisions

### Cookie de sesión

Cookie `sesion`, con `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/`, sin `Domain` y `Max-Age` igual a la expiración del token. `Secure` no es configurable.

- Sin prefijo `__Host-`: según la [consulta de compatibilidad](https://dev.to/mupin/bypassing-secure-cookie-limitation-in-webkit-for-localhost-playwright-testing-3o18) del 25/11/2026, Chrome rechaza esos prefijos en `http://localhost`. Se reevalúa cuando exista HTTPS.
- `Max-Age` conserva el comportamiento actual de sobrevivir al reinicio del navegador. Una cookie de sesión del navegador cambiaría ese comportamiento sin pedido.
- `Strict` alcanza porque no hay flujos que lleguen autenticados desde otro sitio.

### Anti-CSRF derivado de la sesión

`tokenCsrf = HMAC-SHA256(clave = token de sesión, mensaje = "csrf-v1")`, en hexadecimal. El filtro lo recalcula desde la cookie y lo compara en tiempo constante con `X-CSRF-Token` en todo método distinto de `GET`, `HEAD` y `OPTIONS`.

| Alternativa | Motivo para descartarla |
| --- | --- |
| Token aleatorio guardado en `token_acceso` | Requiere migración y escritura sin sumar protección en este caso |
| Doble envío con cookie legible | Depende de que interfaz y API compartan host y es débil si otro origen del sitio puede escribir cookies |
| Solo `SameSite` y verificación de `Origin` | La regla exige token; además, otros puertos de `localhost` son el mismo sitio |
| CSRF de Spring Security | Descartado por el usuario junto con la dependencia |

El token derivado no necesita secreto de servidor, cambia con cada sesión y no permite reconstruir el token de sesión.

### Contrato HTTP

| Método y ruta | Cambio |
| --- | --- |
| `POST /auth/login` | `200 {usuarioId, nombreUsuario, rol, profesionalRef, tokenCsrf}` y `Set-Cookie`; sin `token` |
| `GET /auth/sesion` | Nuevo. Mismo cuerpo; `401 no_autenticado` sin sesión |
| `POST /auth/logout` | Exige `X-CSRF-Token` con sesión válida; `204` y cookie expirada. Sin cookie responde `204` |

- Respuestas de login y sesión con `Cache-Control: no-store`.
- El filtro no evalúa la cookie en el login, para que una cookie vieja no bloquee el ingreso. En el resto, una cookie inválida produce `401` y la expira.
- El login no lleva token anti-CSRF porque aún no hay sesión: lo protegen el cuerpo JSON obligatorio, que fuerza preflight, y la lista de orígenes.
- Se conserva el formato `{codigo, mensaje}`; se agrega `403 csrf_invalido`.
- No se mantiene `Bearer` en paralelo: obligaría a seguir devolviendo el token legible. El único cliente es la interfaz del repositorio.

### CORS

`allowCredentials(true)`, orígenes exactos de `app.cors.allowed-origins` y encabezados `Accept`, `Content-Type` y `X-CSRF-Token`. Las respuestas `401` y `403` del filtro deben llevar las cabeceras CORS del origen permitido: se reutiliza la corrección en curso del `401`, sin duplicarla.

### Interfaz

- `client.ts` usa `credentials: "include"` y mantiene `tokenCsrf` en memoria; `auth.ts` mantiene la identidad en memoria.
- Al arrancar llama a `GET /auth/sesion`; `401` muestra el login y otro fallo muestra un error con reintento, sin simular sesión cerrada.
- Las respuestas de login y sesión entran como `unknown` y se validan con funciones propias, como en C-10.
- Ante `401` vuelve al login; ante `csrf_invalido` consulta la sesión y vuelve a renderizar, sin reenviar la escritura.
- Al arrancar elimina las claves `token` y `user` de `localStorage`.
- El cierre fallido deja de ignorarse: con la cookie `HttpOnly`, la interfaz no puede cerrar la sesión por sí sola y mostrar el login sería engañoso en un equipo compartido.

## UI Mockups

No agrega pantallas ni cambia el diseño: reutiliza login y mensajes existentes. Si la política visual vigente exige una decisión para el mensaje de cierre fallido o el error de arranque, se registra antes de Apply.

## Risks / Trade-offs

- [Un script inyectado todavía puede operar con la página abierta o capturar la contraseña al escribirla] → se mantiene el render seguro; CSP queda como trabajo aparte.
- [Safari puede no guardar cookies `Secure` en `http://localhost`; no verificado] → comprobarlo antes de implementar; en local se soportan los navegadores donde se compruebe. No quitar `Secure`; HTTPS local requiere otra decisión.
- [Chrome y Firefox: aceptación de la cookie entre los puertos 5173 y 8080 no verificada en este proyecto] → primera tarea; si falla, detener Apply y revisar el enfoque.
- [`localhost` y `127.0.0.1` son sitios distintos y la cookie `Strict` no viaja] → las pruebas de navegador usan el mismo host para interfaz y API.
- [Las cookies no se aíslan por puerto: otro servidor en `localhost` recibe la cookie] → aceptable con datos ficticios; en despliegue, host propio con HTTPS.
- [Interfaz y API en sitios distintos dejarían de funcionar] → al definir PA-09, servirlas en el mismo sitio.
- [Un token copiado antes del cambio sirve como valor de cookie hasta vencer] → máximo la expiración configurada; revocar las sesiones de una base persistente antes de publicar.
- [Filtro anti-CSRF propio en lugar de uno de plataforma] → alcance mínimo, pruebas negativas por método y revisión independiente.
- [Sin cadena deny-by-default] → deuda preexistente, fuera de este change.

## Migration Plan

Sin cambios de esquema. Backend e interfaz se publican juntos y todos vuelven a iniciar sesión. Revertir el cambio restituye el contrato anterior, junto con la deuda.
