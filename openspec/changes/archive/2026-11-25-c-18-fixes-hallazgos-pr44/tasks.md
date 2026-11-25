# Tasks

## 1. Pruebas de regresión

- [x] 1.1 Escribir las regresiones de vigencia ante confirmación, orden de bloqueos y plazo tras las esperas en `CancelacionPorEnlaceRegresionIT` con PostgreSQL real; verificar que fallan por el comportamiento defectuoso antes de cambiar producción y registrar el resultado.

## 2. Correcciones

- [x] 2.1 Actualizar la versión del token vigente dentro de la confirmación; verificar las regresiones de confirmación, reversión, enlace desactualizado, reprogramación y repetición.
- [x] 2.2 Reordenar los bloqueos de la cancelación por enlace y revalidar el token bajo ellos; verificar las regresiones frente a reprogramación, cancelación interna, alta y bloqueo.
- [x] 2.3 Consultar el reloj después del último bloqueo; verificar el rechazo y el borde inclusivo tras esperar el turno, el profesional, el sillón y el token.

## 3. Verificación

- [x] 3.1 Ejecutar `mvn -f backend/pom.xml clean verify` completo, `openspec validate` y las pruebas del roadmap; registrar resultados en `verify.md`.
- [x] 3.2 Obtener una revisión independiente final, resolver o registrar sus hallazgos y actualizar KB y `backend/README.md` según corresponda.

## Workflow follow-up

- Archivar con el CLI de OpenSpec después de la verificación.
- Push e integración requieren pedido explícito.
