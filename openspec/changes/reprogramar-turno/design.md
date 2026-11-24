# Design

## Context

C-06 ya provee el alta y la consulta autorizadas en `ServicioAgenda`, almacenamiento de turnos en V7 y el adaptador real de ocupación para los bloqueos. V7 permite `SELECT` e `INSERT` al rol de aplicación. C-15 provee `LocksRecursosAgenda.adquirir` para escrituras que comparten recursos. C-07 agrega la primera actualización de una asignación existente.

## Goals / Non-Goals

**Goals:** mover el mismo turno y proteger origen, destino y versión en una transacción, con interoperación comprobable con el alta de C-06 y los bloqueos de C-15.

**Non-Goals:** cambiar paciente, prestación, duración o estado, y agregar cancelación, confirmación, transporte HTTP, interfaz o comunicaciones.

## Decisions

### Contrato y versión

`ServicioAgenda.reprogramar` recibe identidad autenticada y un DTO con UUID de turno, versión esperada opaca e inicio, profesional y sillón de destino. Devuelve el turno vigente con su versión. Los resultados de entrada inválida, conflicto de disponibilidad, versión desactualizada y acceso denegado se distinguen. El error de versión indica volver a consultar. Las respuestas de rechazo no incluyen datos de turnos ajenos.

Exponer la versión opaca como campo aditivo de `TurnoRegistrado`, conservar las firmas de alta y consulta y ofrecer el constructor existente además del que recibe la versión. Toda respuesta de servicio se construye con la versión real de la fila. Persistir una versión numérica monotónica para detectar un movimiento aun cuando el turno vuelva al horario anterior. La representación de la versión queda opaca para quien llama al servicio.

### Orden de validación y de locks

1. Exigir `turno:reprogramar` y validar la estructura de la solicitud. Adquirir el lock de la fila del turno mediante una lectura actual, limitada al alcance del operador. Bloquear únicamente la fila de `turno` y leer las asignaciones sin bloquearlas. Recepción tiene alcance de sede. Odontología requiere su propio profesional, asignación activa y el mismo profesional en el destino. Evitar que un identificador ajeno revele que existe el turno.
2. Con la fila bloqueada, comprobar la versión esperada. Pasar simultáneamente las selecciones de origen y destino a `LocksRecursosAgenda.adquirir`. C-15 adquiere primero profesionales y luego sillones, con UUID ordenados y sin duplicados. Mantener la fila y los recursos bloqueados hasta confirmar o revertir la transacción `READ COMMITTED`.
3. Revalidar el alcance de odontología tras la espera por locks. Comprobar que el turno sigue pendiente y que ambos inicios son estrictamente posteriores a una lectura del `Clock`. Si el destino normalizado coincide, devolver la asignación vigente y su versión. Un destino idéntico con versión desactualizada, acceso denegado o elegibilidad vencida conserva el rechazo correspondiente.
4. Para un cambio efectivo, reusar la validación de recursos activos, intervalo vigente, ocupación persistida y bloqueos de C-06. Excluir el UUID del turno original de la selección de ocupantes. Usar la duración efectiva persistida para calcular el fin desde el inicio truncado a microsegundos, con los mismos extremos en validación y escritura. El intervalo se aplica entre turnos. Los bloqueos usan la intersección semiabierta de C-15.

### Actualización del turno

Usar una actualización explícita y parametrizada de profesional, sillón, inicio, fin y versión, condicionada al UUID y la versión esperada. Un cambio efectivo incrementa la versión en esa misma escritura. La transacción conserva todos los demás campos de la fila. Comprobar que el resultado contiene la asignación y versión efectivamente guardadas, por ejemplo con el retorno de la propia actualización. Evitar construir el resultado o la consulta posterior a partir de una entidad JPA cacheada. Una actualización explícita permite que el rol de aplicación tenga permisos de escritura solo sobre las columnas modificables.

## Risks / Trade-offs

- Dos escrituras sobre el mismo turno se serializan al bloquear su fila. Dos movimientos de turnos distintos usan la unión ordenada de recursos para evitar inversión de locks. El alta y el bloqueo consultan ocupación con lecturas sin lock de filas de turnos, por lo que conservan el protocolo de recursos sin invertir ese orden. Verificar carreras con los servicios de producción y observar el estado después de cada confirmación o reversión.
- Un permiso que cambia durante la espera puede invalidar un movimiento que parecía permitido. La comprobación temprana protege el alcance ante la primera lectura. La comprobación tras adquirir los recursos usa las asignaciones confirmadas antes de validar o escribir el destino.
- La versión persistida protege un cambio incluso si otro operador volvió a la misma asignación. Este control es adicional a los locks de C-15 que protegen turnos distintos y bloqueos sobre recursos compartidos.

## Migration Plan

Confirmar la próxima versión Flyway al iniciar Apply. La base revisada `82a6e72` llega a V7, por lo que se propone V8. Agregar una versión inicial para las filas de V7 y las altas posteriores, y conceder `UPDATE` de profesional, sillón, inicio, fin y versión al rol `odonto_app`. Verificar actualización de una instancia migrada con turnos conservados y probar los permisos con el `UPDATE` que ejecuta el servicio. Las migraciones aplicadas se conservan. Ante un fallo de una versión ya aplicada, preservar los turnos y corregir mediante una nueva migración.

## UI Mockups

Decision: no (no UI)
