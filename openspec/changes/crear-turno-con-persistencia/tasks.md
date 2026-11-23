# Tasks

## 1. Condiciones previas a Apply

- [ ] 1.1 Comprobar C-15 integrado en `main`. Acordar el contrato de lectura cruzada, los bordes y precisión de los períodos, la aplicación del intervalo frente a bloqueos y la exclusión transaccional común. Actualizar diseño, escenarios de bordes y pruebas para reflejar ese contrato y volver a pasar `openspec validate crear-turno-con-persistencia --strict`.
- [ ] 1.2 Revisar con Giovanni la propuesta escrita y el plan sobre la base que integra C-15. Registrar su aprobación y un pedido explícito de Apply antes de iniciar las tareas de implementación.

## 2. Persistencia de la reserva

- [ ] 2.1 Acordar la numeración Flyway y agregar la migración de turnos y el almacenamiento separado del dominio. Verificar con PostgreSQL real actualización desde el esquema previo, inicio de la aplicación, claves foráneas y persistencia con los permisos del rol de aplicación.

## 3. Alta y consulta de agenda

- [ ] 3.1 Aplicar TDD al alta autorizada por operación y por registro. Observar el fallo inicial y verificar recepción global y odontología vinculada, propia y asignada. Comprobar denegación de identidad no autorizada, profesional ajeno y paciente fuera de alcance sin persistencia ni datos ajenos.
- [ ] 3.2 Aplicar TDD a referencias, horario, duración y disponibilidad del alta. Verificar falta de referencias, recurso inactivo, intervalo ausente, inicio pasado o igual al reloj, duración de referencia y ajustada, estabilidad frente a cambios de catálogo, choques por cada recurso y separación exacta e insuficiente antes y después.
- [ ] 3.3 Integrar el alta con los bloqueos y la exclusión compartida acordados en 1.1. Verificar con PostgreSQL bloqueos de profesional, sillón o ambos, dos altas simultáneas con cada recurso compartido, y la carrera entre alta y bloqueo en ambos órdenes. Usar conexiones y transacciones independientes y verificar el estado final persistido. Comprobar también un par de altas sin recursos compartidos.
- [ ] 3.4 Implementar la consulta por sillón y período con DTO de servicio. Verificar orden, intersección, duración efectiva, límites del período, alcance de ambos roles y denegación sin identidad válida.

## 4. Verificación integrada

- [ ] 4.1 Ejecutar la suite Maven con PostgreSQL real sobre el cambio final integrado y registrar comandos, resultados y limitaciones de las pruebas de exclusión. Revisar el contrato de servicio y actualizar las fuentes operativas con la evidencia verificada.

## Workflow follow-up

- Archivar con OpenSpec y comprobar la sincronización después de Verify y la revisión exigida. C-07 puede pasar a implementación cuando C-06 esté integrado.
