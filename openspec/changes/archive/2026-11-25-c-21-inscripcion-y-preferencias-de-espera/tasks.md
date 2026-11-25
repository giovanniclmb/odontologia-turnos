# Tasks

## 1. Preferencias y vigencia

- [x] 1.1 Escribir pruebas unitarias que fallen para preferencias inválidas, rango de una sola fecha, franjas múltiples y profesional omitido. Implementar la validación y comprobar que las pruebas pasan.
- [x] 1.2 Escribir pruebas con reloj fijo para el último día del rango, el comienzo del día siguiente y la baja. Implementar el cálculo de vigencia y verificar las tres transiciones.

## 2. Persistencia

- [x] 2.1 Crear la migración Flyway y el mapeo JPA. Verificar en PostgreSQL con Testcontainers la migración desde el esquema anterior, las claves foráneas, los rangos inválidos y la persistencia de varias franjas con profesional omitido.

## 3. Servicio de gestión propia

- [x] 3.1 Escribir pruebas fallidas de alta y consulta con autorización C-16. Implementar el contrato de servicio, la validación de referencias y la consulta por titular. Verificar el rechazo de cuentas pendientes e identidades no autorizadas, y la misma respuesta para una inscripción inexistente o ajena.
- [x] 3.2 Escribir pruebas fallidas de edición y baja. Implementar la actualización atómica de preferencias, el cambio de prestación y la baja repetible. Verificar que el titular conserva acceso de consulta al registro finalizado, que la edición de un registro finalizado se rechaza y que los intentos ajenos preservan el estado.
- [x] 3.3 Escribir pruebas con PostgreSQL real y transacciones independientes para altas simultáneas y concurrencia entre edición y alta hacia una misma prestación. Aplicar el bloqueo transaccional a todas las escrituras y comprobar el conflicto, el alta para otra prestación y la reutilización del cupo tras baja o vencimiento.

## 4. Verificación integrada

- [x] 4.1 Verificar el recorrido de una cuenta vinculada de C-16 con referencias de C-05 y comprobar que alta, edición y baja no crean turnos ni emiten avisos. Ejecutar la suite del backend y registrar los resultados.

## Evidence

- La ejecución focalizada completó 4 pruebas unitarias y 5 de integración con PostgreSQL 18.7 en Testcontainers. Cubrió la migración V11 a V12, la gestión por cuenta vinculada, el límite bajo concurrencia y la independencia de agenda y email.
- La ejecución completa con Maven `verify` aprobó 132 pruebas unitarias y 187 de 191 pruebas de integración. `ArranqueJarIT` y `BootstrapMavenIT` agotaron la espera de sus procesos de arranque. `BootstrapCliIT` y un caso de `ConfiguracionRecursosIT` agotaron la espera de PostgreSQL en Testcontainers.
- En la repetición focalizada pasaron el JAR, el bootstrap Maven y el caso de configuración de recursos. `BootstrapCliIT` llegó al arranque del proceso Java hijo y agotó su espera de 45 segundos.
- `BootstrapCliIT` pasó al ejecutarse de forma aislada. Las 191 pruebas de integración pasaron entre la ejecución completa y las repeticiones focalizadas.
