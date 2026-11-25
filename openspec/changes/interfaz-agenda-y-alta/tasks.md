# Tasks

## 1. Gate visual

- [x] 1.1 Crear los mockups de agenda diaria y semanal, alta de turno, alta de bloqueo, configuración y estados, para ambos roles, con tokens de la revisión 1; verificar las capturas a 390×844 y 1440×900.
- [x] 1.2 Obtener la aprobación del usuario y registrarla en `design.md`; verificar `Approval: approved` antes de iniciar cualquier tarea siguiente.

## 2. Lecturas con alcance (backend)

- [x] 2.1 Con TDD, agregar `bloqueo:consultar` a ambos roles; verificar en `ServicioAutorizacionTest` la matriz de alta y consulta de bloqueos.
- [x] 2.2 Con TDD, implementar la consulta autorizada de bloqueos y su recorte por rol; verificar cada escenario del delta de bloqueos con PostgreSQL real.
- [x] 2.3 Con TDD, agregar nombre y apellido a la lectura de agenda en una sola consulta; verificar con PostgreSQL real el paciente sin asignación, la exclusión de turnos ajenos y que `ServicioPacientes` siga denegando.

## 3. Contrato HTTP

- [x] 3.1 Con TDD, exponer sillones, prestaciones e intervalo con sus errores; verificar con pruebas HTTP y PostgreSQL real 401, 403, decimales y valores no positivos sin cambios persistidos.
- [x] 3.2 Con TDD, exponer consulta y alta de turnos; verificar alta válida, conflictos por profesional, sillón, intervalo y bloqueo, intervalo sin configurar, período mayor a 7 días y alcance de odontología.
- [x] 3.3 Con TDD, exponer consulta y alta de bloqueos; verificar turno ocupante, referencias inexistentes, solicitudes inválidas y acceso por rol.
- [x] 3.4 Documentar las rutas y códigos nuevos en `backend/README.md`; verificar contra las pruebas HTTP.

## 4. Interfaz

- [ ] 4.1 Con TDD, implementar la conversión horaria de Mendoza y los límites de día y semana; verificar con `npm test` ejecutando también con otra zona en `TZ`.
- [ ] 4.2 Con TDD, implementar validadores de respuestas y estado en la URL; verificar respuestas mal formadas y parámetros inválidos.
- [ ] 4.3 Implementar vista diaria y semanal con carga, vacío, error y reintento según los mockups; verificar con pruebas de render que los textos no se interpreten como HTML.
- [ ] 4.4 Implementar alta de turno y alta de bloqueo según rol; verificar rechazo con datos conservados, envío único y catálogos vacíos.
- [ ] 4.5 Implementar la sección Configuración solo para recepción; verificar sin configurar, cambio válido, valor inválido y ausencia para odontología.
- [ ] 4.6 Actualizar `frontend/README.md`; verificar que sus pasos se ejecuten como están escritos.

## 5. Integración

- [ ] 5.1 Ejecutar `./scripts/check-docker.sh`, `mvn -f backend/pom.xml clean verify` y `npm test`; registrar resultados en `verify.md`.
- [ ] 5.2 Recorrer ambos roles contra la API real con datos ficticios, a 390×844 y 1440×900 y solo con teclado; registrar capturas y diferencias respecto de los mockups.
- [ ] 5.3 Solicitar revisión independiente de permisos y exposición de datos; verificar que no queden hallazgos bloqueantes.

## Workflow follow-up

- Actualizar el estado de C-10 en CHANGES.md, archivar con el CLI de OpenSpec y abrir el PR solo tras un pedido explícito de push.
