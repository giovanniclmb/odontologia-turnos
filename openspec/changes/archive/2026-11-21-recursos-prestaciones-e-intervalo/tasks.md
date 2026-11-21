# Tasks

Aplicar TDD estricto a cada comportamiento. Capturar el fallo esperado antes de escribir código de producción, ejecutar el verde, triangular con un caso adicional y volver a ejecutar las pruebas después de cada refactor. Usar datos ficticios y PostgreSQL 18.7 real mediante Testcontainers para persistencia, migración y concurrencia.

## 0. Línea base

- [x] 0.1 Ejecutar `mvn verify` en `backend/` antes de editar producción y registrar el resultado de las suites existentes (78 unitarias y 82 integraciones en PostgreSQL 18.7).

## 1. Persistencia e intervalo

- [x] 1.1 Evolucionar las referencias en V4. Verificar instalación limpia y migración desde V3 con conservación de UUID, códigos y vínculos válidos. Probar mediante SQL que PostgreSQL rechaza duraciones no positivas y vínculos huérfanos.
- [x] 1.2 Implementar la configuración única de sede. Probar lectura inicial sin configurar, persistencia tras reinicio y rechazo SQL de intervalos no positivos y de una segunda fila. Ejecutar dos ajustes concurrentes y comprobar que queda un único valor confirmado.
- [x] 1.3 Implementar consulta y ajuste del intervalo. Probar cambios válidos, rechazo de valores inválidos sin modificar el anterior y uso del último valor confirmado en validaciones posteriores.

## 2. Recursos y prestaciones

- [x] 2.1 Implementar alta y consulta de profesionales. Probar conservación de referencia y estado, rechazo de duplicados y ausencia de altas precargadas.
- [x] 2.2 Implementar alta y consulta de sillones. Probar conservación de referencia y estado, rechazo de duplicados y ausencia de altas precargadas.
- [x] 2.3 Implementar disponibilidad mínima. Probar recursos activos con intervalo vigente, falta de intervalo y omisión, ausencia o inactividad de cualquiera de los recursos.
- [x] 2.4 Implementar alta y consulta de prestaciones. Probar catálogo inicial vacío, persistencia de duración positiva y rechazo de códigos duplicados o duración inválida.
- [x] 2.5 Implementar el ajuste de duración de referencia. Probar lectura del nuevo valor, rechazo ante ausencia de prestación o duración inválida y preservación de la duración ya elegida por los turnos.

## 3. Acceso interno

- [x] 3.1 Ampliar el catálogo y la matriz de C-03. Probar los tres permisos administrativos exclusivos de recepción y mantener las pruebas de permisos existentes.
- [x] 3.2 Integrar la autorización en servicios de intervalo y recursos. Probar escrituras de recepción, consultas de ambos roles, escrituras denegadas a odontología y acceso sin identidad denegado.
- [x] 3.3 Implementar la vinculación efectiva. Probar persistencia y propagación a identidades nuevas, FK y rechazo sin cambios parciales para referencias inválidas o falta de permiso. Cubrir que recepción puede crear usuarios internos, odontología recibe denegación y el odontólogo sin vínculo conserva el acceso clínico denegado.

## 4. Verificación integrada

- [x] 4.1 Ejecutar `mvn verify` en `backend/` con PostgreSQL real y `openspec validate recursos-prestaciones-e-intervalo --strict`. Resultado final: 79 unitarias y 92 integraciones aprobadas, sin omisiones, con Maven 3.9.16 y JDK 26 compilando para Java 25.

### TDD Cycle Evidence

| Tarea | Pruebas | Capa | Línea base | RED observado | GREEN observado | Triangulación |
| --- | --- | --- | --- | --- | --- | --- |
| 1.1 | `RecursosMigracionIT` | PostgreSQL | 78 unitarias + 82 IT | V3 no tenía V4 pendiente | V4 aplicada desde V3 | Alta limpia, duración inválida y FK que rechaza huérfanos |
| 1.2 | `IntervaloPersistenciaIT` | PostgreSQL | Misma línea base | Tabla de sede ausente | Fila única y dos escrituras completadas | Intervalos inválidos y segunda fila rechazados |
| 1.3 | `ConfiguracionRecursosIT` | Servicio y PostgreSQL | Misma línea base | Servicio de sede ausente | Intervalo persistido al reiniciar el contexto | Cambios sucesivos y entradas inválidas |
| 2.1–2.2 | `ConfiguracionRecursosIT` | Servicio y PostgreSQL | Misma línea base | Operaciones de profesionales y sillones ausentes | Altas y consultas correctas | Catálogos vacíos, estados y códigos duplicados |
| 2.3 | `ConfiguracionRecursosIT` | Servicio y PostgreSQL | Misma línea base | Operación de disponibilidad ausente | Recursos activos usan el intervalo vigente | Intervalo ausente y recursos nulos, inexistentes o inactivos |
| 2.4–2.5 | `ConfiguracionRecursosIT` | Servicio y PostgreSQL | Misma línea base | Operaciones de alta, consulta y ajuste ausentes | Catálogo persistido y nueva duración consultable | Duplicados, duraciones inválidas y turnos ya construidos en el dominio |
| 3.1–3.2 | `ServicioAutorizacionTest`, `ConfiguracionRecursosIT` | Unitaria y servicio | Misma línea base | Permiso y operaciones protegidas ausentes | Permisos administrativos exclusivos de recepción | Escrituras de odontología y consulta anónima denegadas |
| 3.3 | `ConfiguracionRecursosIT`, `AccesoPersistenciaIT` | Servicio y PostgreSQL | Misma línea base | Servicio de vinculación ausente | Identidad autenticada refleja el vínculo | Vínculos inválidos, creación restringida y regresión de acceso clínico |

Los registros de las ejecuciones se conservan en `backend/target/c05-*.log`. Las correcciones posteriores al verde inicial incluyeron la barrera de concurrencia y la adaptación de fixtures existentes a V4. La verificación completa cubre esos cambios.
