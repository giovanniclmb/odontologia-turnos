# Tasks

Antes de Apply registrar en [design.md](design.md) la aprobación del mockup de C-13 conforme a `system-and-mockups`.

## 1. Contrato HTTP de seguimiento

- [x] 1.1 Exponer la consulta de pendientes con proyección mínima de nombres en lote; comprobar con pruebas HTTP y PostgreSQL que solo recepción recibe todos los pendientes futuros ordenados, sin otros datos personales o clínicos.
- [x] 1.2 Exponer el registro con versión opaca y respuesta del turno; comprobar mediante pruebas HTTP con PostgreSQL el éxito, permisos, entrada inválida, inexistencia, estado no elegible y conflicto de versión.

## 2. Interfaz de recepción

- [x] 2.1 Agregar cliente y validadores del contrato, con pruebas de lectura, envío y rechazo de respuestas inválidas.
- [x] 2.2 Incorporar la navegación y listado de toda la sede, con pruebas de rol, datos, zona horaria, carga, vacío y error.
- [x] 2.3 Implementar registro y recuperación, con pruebas de éxito, recarga fallida, rechazo, versión obsoleta, respuesta incierta, doble envío y navegación durante la operación.

## 3. Integración y verificación

- [x] 3.1 Ejecutar suites pertinentes de backend y frontend, verificar con PostgreSQL real la pertenencia, confirmación y permisos, y registrar evidencia.
- [x] 3.2 Recorrer en navegador con datos ficticios los estados y tamaños 390 × 844 y 1440 × 900 frente al mockup aprobado; comprobar foco, teclado y errores, y registrar resultados.

## Workflow follow-up

- Verificar el cambio y archivar mediante OpenSpec cuando corresponda.
