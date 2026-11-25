# Design

## Context

`ServicioAgenda` trabaja sobre turnos versionados y proyecta sus datos mediante `TurnoRegistrado`. V9 permite `PENDIENTE` y `CANCELADO`. Las búsquedas de conflictos y la consulta que usa el alta de bloqueos identifican ocupación por `PENDIENTE`. C-07 y C-08 adquieren primero la fila del turno y luego los recursos antes de editar.

## Goals / Non-Goals

**Goals:** Dar a C-13 un contrato interno Java; conservar las garantías de agenda durante la transición de estado y la actualización de una base existente.

**Non-Goals:** Transporte HTTP, interfaz, comunicaciones automáticas y seguimiento de inasistencias.

## Decisions

1. Usar la fila existente como fuente del estado de confirmación. Una tabla separada requeriría coordinar la escritura y lectura de dos registros. Reutilizar la proyección de agenda con el identificador y la versión, y consultar por estado e inicio con un instante tomado del reloj inyectado. Aplicar el filtro y orden en PostgreSQL.
2. Reservar ambos permisos a recepción en `ServicioAutorizacion`, cuyo mapa actual también los concede a odontología. Verificar la autorización antes de consultar. Para confirmar, bloquear la fila con el patrón de `EdicionesTurnoPersistidas`, verificar versión y elegibilidad y actualizar estado y versión juntos en `READ COMMITTED`. El reloj se evalúa tras la espera por la fila. Confirmar conserva los recursos y el período, por lo que basta con serializar las escrituras del mismo turno; cancelación y reprogramación conservan su protocolo de locks de recursos.
3. Compartir la definición de estados ocupantes entre consultas de disponibilidad y bloqueos. Actualizar las condiciones de servicio y escritura de C-07 y C-08 para que recepción opere sobre confirmados futuros. La reprogramación efectiva persiste `PENDIENTE` junto con el nuevo horario y versión. El destino idéntico conserva estado y versión. Se conserva para odontología su alcance vigente sobre turnos pendientes.

## Risks / Trade-offs

- Una consulta que filtre por un único estado podría aceptar un conflicto con un confirmado. Cubrir altas, movimientos y bloqueos contra ambos estados en PostgreSQL.
- Una agenda leída antes de otra edición puede contener una versión anterior. Resolver la carrera bajo bloqueo de fila y pedir recarga ante versión obsoleta.

## Migration Plan

Agregar una migración Flyway posterior a V10 que amplíe la restricción de estado existente, conservando todas las filas y permisos de operación. Desplegar el esquema y el backend actualizado coordinadamente, retirando las instancias antiguas antes de habilitar escrituras de `CONFIRMADO`. Un binario previo no puede leer ese estado y lo omite en sus consultas de ocupación. Si ya existen confirmaciones, aplicar una corrección hacia adelante o restaurar conjuntamente una copia compatible de aplicación y base bajo la política de recuperación del equipo.
