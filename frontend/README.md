# Frontend — pacientes y ficha clínica

HTML/CSS y TypeScript vanilla estricto, sin framework de UI. Requiere Node 24 y npm para las comprobaciones; TypeScript se instala desde el lockfile.

Desde `frontend/`:

```sh
npm ci
npm test
```

`npm test` compila con TypeScript y ejecuta las regresiones con `node:test`. `npm run build` solo compila. La salida está en `frontend/dist/` y se ignora en Git.

Para abrir la aplicación, iniciar la API siguiendo [el README del backend](../backend/README.md). Desde la raíz del repositorio, servir los archivos en loopback:

```sh
python3 -m http.server 5173 --bind 127.0.0.1
```

Abrir [la interfaz local](http://localhost:5173/frontend/public/index.html). Usar `localhost` en el navegador: es el origen permitido por CORS. Este servidor es solo para desarrollo local; detenerlo con Ctrl-C. El servidor debe tener la raíz del repositorio para resolver `design-system/`, `frontend/public/` y los módulos compilados. No copiar únicamente `public/`.

En el puerto 5173, el cliente usa `http://localhost:8080` como API. Para otro destino, definir `window.__API_BASE_URL__` antes de cargar el módulo; las pruebas de navegador usan esta opción con la API temporal. El backend permite configurar otros orígenes mediante `app.cors.allowed-origins`; no abrirlos a `*` para resolver problemas de arranque.

La marca provisional y los tokens provienen de `design-system/`. Las acciones visibles dependen del rol devuelto por la API; el backend vuelve a validar cada operación. Recepción solo lee la ficha clínica y odontología edita según asignación y autoría. Los enlaces a otros módulos permanecen deshabilitados hasta que sus changes estén integrados.

La regresión de HTML se ejecuta también en el job `frontend-verify` de CI. Las pruebas HTTP con PostgreSQL real pertenecen a `ApiPacientesIT`; las capturas y la comprobación interactiva se registran en [la evidencia de C-11](../docs/evidencias/c11-correcciones/verify.md).
