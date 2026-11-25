# Tasks

## 1. Condiciones previas

- [ ] 1.1 Obtener la aprobación expresa de Apply; verificar que quede registrada con su fecha en este change.
- [ ] 1.2 Comprobar que C-10 y la corrección de CORS del `401` están integrados en la base; verificar con el historial de `main` y revisar que los deltas de este change sigan coincidiendo con las specs vigentes.
- [ ] 1.3 Probar en Chromium y Firefox, y en Safari si está disponible, que una cookie `HttpOnly; Secure; SameSite=Strict` de `http://localhost:8080` se guarda y viaja desde `http://localhost:5173`, con una prueba desechable; registrar el resultado por navegador y detener Apply si falla en Chromium o Firefox.

## 2. Sesión por cookie en el backend

- [ ] 2.1 Con prueba previa fallida, emitir la cookie en el login sin token en el cuerpo y con `no-store`; verificar atributos, cuerpo y rechazo de contenido no JSON con PostgreSQL real.
- [ ] 2.2 Con prueba previa fallida, resolver la identidad solo desde la cookie; verificar válido, vencido, revocado, desconocido, `Authorization` sin efecto y login con cookie inválida.
- [ ] 2.3 Con prueba previa fallida, agregar `GET /auth/sesion`; verificar identidad, `tokenCsrf`, `401` y ausencia del token de sesión.
- [ ] 2.4 Con prueba previa fallida, exigir `X-CSRF-Token` en escrituras; verificar que ausente o ajeno responde `403 csrf_invalido` sin cambios en la base, y que las lecturas no lo requieren.
- [ ] 2.5 Con prueba previa fallida, revocar y expirar la cookie en el logout; verificar logout válido, sin token anti-CSRF y sin cookie.
- [ ] 2.6 Con prueba previa fallida, habilitar credenciales en CORS con lista exacta; verificar preflight, origen permitido y rechazado, comodín sin atención y cabeceras en `401` y `403`.
- [ ] 2.7 Migrar a cookie las pruebas HTTP y fixtures que usan `Bearer` y actualizar `backend/README.md`; verificar con `mvn -f backend/pom.xml clean verify`.

## 3. Interfaz

- [ ] 3.1 Con prueba previa fallida, enviar credenciales y `X-CSRF-Token` desde el cliente HTTP y validar login y sesión como `unknown`; verificar con `npm test` en `frontend/`.
- [ ] 3.2 Con prueba previa fallida, restaurar la sesión al arrancar, eliminar las claves anteriores y tratar `401`, fallo de arranque y `csrf_invalido`; verificar con `npm test` que nada de la sesión se escribe en almacenamiento.
- [ ] 3.3 Con prueba previa fallida, volver al login solo ante cierre confirmado o sesión inválida; verificar éxito, `401` y fallo de red con `npm test`.
- [ ] 3.4 Actualizar `frontend/README.md` con el requisito de mismo host y navegadores comprobados; verificar siguiendo sus pasos desde cero.

## 4. Verificación integrada

- [ ] 4.1 Recorrer en navegador, con API y PostgreSQL temporales y datos ficticios, login, recarga, escritura de pacientes, ficha y agenda, dos pestañas y cierre; registrar que `localStorage`, `sessionStorage` y `document.cookie` no contienen el token de sesión.
- [ ] 4.2 Obtener revisión independiente del filtro, CSRF y CORS, y ejecutar `openspec validate sesion-en-cookie-httponly --strict`; registrar resultados y límites en `verify.md`.

## Workflow follow-up

- Registrar el resultado en la KB y archivar el change tras la revisión requerida.
- Push, PR e integración requieren pedido explícito.
