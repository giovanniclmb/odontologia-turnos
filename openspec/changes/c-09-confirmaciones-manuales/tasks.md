# Tasks

## 1. Persistencia y ocupación

- [x] 1.1 Escribir y observar el fallo esperado de la prueba de migración desde V10. Ampliar la restricción Flyway y agregar `CONFIRMADO` al modelo. Verificar en PostgreSQL que conserva registros y versiones, permite el nuevo valor al rol de aplicación y sigue rechazando estados desconocidos.
- [x] 1.2 Escribir en rojo los casos de conflicto con turnos confirmados. Usar ambos estados ocupantes en las consultas de alta, reprogramación y bloqueo. Verificar con PostgreSQL conflictos por profesional o sillón y separación exacta conforme al intervalo vigente.

## 2. Contrato interno de recepción

- [x] 2.1 Escribir en rojo la prueba del mapa de permisos y reservar ambos permisos a recepción. Verificar que recepción los conserva y odontología pierde ambos sin cambios en sus otros permisos.

- [x] 2.2 Escribir en rojo los casos de listado y agregar la consulta autorizada. Verificar con reloj controlado y PostgreSQL alcance global, orden estable y exclusión de turnos confirmados, cancelados y pendientes que alcanzaron su inicio. Verificar denegación sin datos para otras identidades.
- [x] 2.3 Escribir en rojo los casos de confirmación e implementar la edición con bloqueo de fila y versión esperada. Verificar resultado actualizado, ocupación intacta, errores de entrada, inexistencia, permisos, versión obsoleta, repetición con versión vigente y límite temporal después de esperar. Cubrir doble confirmación y reversión en PostgreSQL.

## 3. Ediciones posteriores

- [x] 3.1 Escribir en rojo los casos de reprogramación y cancelación de confirmados. Implementar ambas transiciones para recepción y verificar en PostgreSQL la liberación tras cancelar, el regreso a pendiente al mover, la solicitud sin cambios y la conservación de estado y versión al fallar.
- [x] 3.2 Verificar por pruebas que odontología conserva su alcance previo y que las carreras de confirmación contra cancelación o reprogramación con la misma versión producen como máximo una modificación confirmada.

## 4. Integración

- [x] 4.1 Ejecutar la suite Maven completa con PostgreSQL de Testcontainers. Comprobar que el alta sigue creando turnos pendientes y que un turno confirmado aparece con su versión vigente en la consulta de agenda y desaparece del listado de pendientes.
