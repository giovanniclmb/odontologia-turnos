# Tasks

## 1. Condiciones previas a Apply

- [x] 1.1 Comprobar C-15 integrado en `main` (`e4183f8`, PR #33) y reflejar en los artefactos su contrato de lectura cruzada, bordes, precisión, intervalo y exclusión transaccional. Verificar con `openspec validate crear-turno-con-persistencia --strict`.
- [x] 1.2 Completar la revisión independiente del plan, revisarlo con Giovanni y registrar su aprobación y un pedido explícito de Apply antes de iniciar las tareas de implementación. Giovanni aprobó Apply solo para C-06 el 23/11/2026 (Mendoza).

## 2. Persistencia de la reserva

- [x] 2.1 Coordinar la próxima versión Flyway y agregar la migración de turnos y el almacenamiento separado del dominio. Verificar en PostgreSQL real la actualización desde V6, el arranque, las cuatro claves foráneas, la consistencia temporal y la persistencia con permisos del rol de aplicación.

## 3. Alta y consulta de agenda

- [x] 3.1 Aplicar TDD al alta autorizada por operación y por registro. Observar el fallo inicial y verificar recepción global y odontología vinculada, propia y asignada. Comprobar denegación de identidad no autorizada, profesional ajeno y paciente fuera de alcance sin persistencia ni datos ajenos. Verificar en PostgreSQL el rechazo cuando se confirma una desasignación mientras el alta espera los locks de recursos.
- [x] 3.2 Aplicar TDD a referencias, horario, duración y disponibilidad del alta. Verificar falta de referencias, recurso inactivo, intervalo ausente, inicio normalizado pasado o igual al reloj, inicio fuera de una jornada semanal habitual, duración de referencia y ajustada, estabilidad frente a cambios de catálogo, choques por cada recurso y separación exacta e insuficiente antes y después. Verificar en PostgreSQL que persisten los extremos usados al validar y conservan la diferencia exacta en minutos.
- [x] 3.3 Sustituir `AgendaSinTurnosPersistidos` por un único adaptador real de `ConsultaTurnosOcupantes` conectado a la tabla de turnos y usar `ConsultaBloqueos.buscar` con la exclusión compartida de C-15. Verificar el arranque con ese adaptador y, en PostgreSQL, los conflictos por profesional, sillón o ambos, la contigüidad y los solapamientos de un microsegundo en ambos extremos. Ejecutar las altas de turno y bloqueo por los dos servicios de producción con el esquema migrado.
- [x] 3.4 Verificar en PostgreSQL dos altas simultáneas por cada recurso compartido y altas sin recursos compartidos. Para la carrera entre turno y bloqueo, invocar el servicio real de alta de C-06 y `ServicioBloqueos.crear` con conexiones y transacciones independientes. Usar la tabla real de turnos y el único adaptador real de `ConsultaTurnosOcupantes`, sin `turno_fixture`, reemplazos `@Primary` de prueba, fallback ni escrituras directas para las operaciones evaluadas. Comprobar la revalidación luego de adquirir los locks, ambos órdenes de confirmación y la reversión de la primera operación. Verificar el resultado de las dos llamadas y las filas finales de turnos y bloqueos en cada caso.
- [x] 3.5 Implementar la consulta por sillón y período con DTO de servicio. Verificar orden, intersección, duración efectiva, bordes semiabiertos, período inválido tras normalización, alcance de ambos roles y denegación sin identidad válida. En PostgreSQL, crear un turno de otro profesional y paciente en el mismo sillón. Comprobar que no aparece en la consulta del odontólogo y que su intento de alta para sí y su paciente asignado produce un conflicto genérico por el sillón, sin datos ajenos ni una segunda reserva.

## 4. Verificación integrada

- [x] 4.1 Ejecutar la suite Maven con PostgreSQL real sobre el cambio final integrado y registrar comandos, resultados y limitaciones de las pruebas de exclusión. Revisar el contrato de servicio y actualizar las fuentes operativas con la evidencia verificada.

## Workflow follow-up

- Archivar con OpenSpec y comprobar la sincronización después de Verify y la revisión exigida. C-07 puede pasar a implementación cuando C-06 esté integrado.
