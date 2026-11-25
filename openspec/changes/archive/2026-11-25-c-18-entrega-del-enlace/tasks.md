# Tasks

## 1. Entrega del enlace

- [x] 1.1 Definir el evento sin publicarlo y escribir las pruebas con PostgreSQL real; verificar que fallan por falta de entrega y por exponer el token en texto.
- [x] 1.2 Publicar el evento al emitir y ocultar el token en su texto; verificar entrega tras commit, reversión, ventana cerrada, confirmación y fallo del consumidor.

## 2. Prueba de versión futura

- [x] 2.1 Exigir el motivo del rechazo, observar el fallo y calcular la versión siguiente a las migraciones existentes; verificar que la prueba pasa por la versión desconocida.

## 3. Verificación

- [x] 3.1 Ejecutar `mvn -f backend/pom.xml clean verify`, `openspec validate` y las pruebas del roadmap; registrar resultados en `verify.md`.
- [x] 3.2 Obtener revisión independiente y registrar la decisión en la KB.

## Workflow follow-up

- Archivar con el CLI de OpenSpec después de la verificación.
- Push e integración requieren pedido explícito.
