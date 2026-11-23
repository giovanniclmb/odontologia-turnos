# Design

## Context

C-05 aporta referencias UUID de profesional y sillón. C-03 ya concede `bloqueo:crear` a recepción. Las migraciones vigentes llegan a V5 y la agenda de C-01 opera en memoria con reservas suministradas por quien la invoca. La persistencia de turnos corresponde a C-06.

## Goals / Non-Goals

- Persistir y consultar bloqueos, y dejar un protocolo de escritura utilizable por C-06 y C-07.
- La modificación, el desbloqueo, HTTP y la persistencia de turnos quedan fuera de C-15.

## Decisions

1. **Persistencia.** Incorporar V6 con una fila por bloqueo, identificador UUID, extremos temporales `timestamptz` y referencias opcionales a profesional y sillón. Las claves foráneas preservan las referencias. Dos restricciones verifican que exista al menos un recurso y que el fin sea posterior al inicio. Indexar por recurso y comienzo para la consulta. Conceder al rol de aplicación lectura y alta.
2. **Período.** Representar los extremos como instantes y truncarlos a microsegundos antes de validar para usar la misma precisión que PostgreSQL. Dos períodos se solapan si cada inicio es anterior al fin del otro. Una consulta con ambos recursos busca coincidencia con cualquiera. El intervalo de sede se sigue evaluando al comparar turnos.
3. **Contrato Java.** El servicio de alta valida permiso y entrada, toma los locks, consulta turnos ocupantes e inserta en la misma transacción. La consulta de bloqueos devuelve coincidencias por recurso y período. Las respuestas de alta distinguen falta de permiso, entrada o referencia inválida y conflicto de turno. El contrato usa valores de aplicación separados de entidades JPA.
4. **Coordinación en PostgreSQL.** `LocksRecursosAgenda.adquirir` usa `FOR NO KEY UPDATE` sobre las referencias en orden de profesionales y luego sillones, con UUID ordenados dentro de cada tipo. Exige una transacción READ COMMITTED. Ejecutar las lecturas de conflicto después de adquirir los locks y conservarlos hasta el resultado de la transacción. C-06 deberá consultar `ConsultaBloqueos.buscar` y persistir el alta bajo esos locks. C-07 deberá adquirir conjuntamente los recursos de origen y destino, comprobar el destino y persistir en una transacción.
5. **Límite de integración.** Definir un puerto de consulta de ocupación que opere dentro de la transacción del alta. El adaptador inicial refleja que el esquema actual carece de persistencia de turnos. C-06 deberá sustituirlo por la consulta real al incorporar la primera escritura de turnos. En C-15, un fixture PostgreSQL de agenda permite verificar ambos órdenes de confirmación con el protocolo compartido.

## Risks / Trade-offs

- Un recurso muy solicitado serializa sus escrituras. Mantener la transacción breve y comprobar concurrencia con sesiones PostgreSQL independientes.
- La consulta informativa es una instantánea. Una escritura requiere repetir la comprobación dentro de su propia transacción con los locks adquiridos.
- Las pruebas del contrato con fixture cubren el protocolo; C-06 y C-07 deben verificar sus propios puntos de escritura con sus adaptadores reales.
