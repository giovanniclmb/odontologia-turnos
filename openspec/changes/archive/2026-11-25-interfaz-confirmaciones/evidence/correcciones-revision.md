# Correcciones tras la revisión del PR 54

Fecha: 25/11/2026. Una revisión por lectura del código señaló fallos en la vista de confirmaciones y en la documentación del change.

## Comportamiento corregido

| Fallo | Corrección |
| --- | --- |
| Al salir de la vista, un resultado incierto se informaba como envío no completado | La vista entrega su propio texto: no se sabe si se registró e identifica el turno |
| Un fallo se perdía si la lista se redibujaba durante el envío; al volver a la vista, la fila quedaba trabada | El resultado llega a la vista montada, que vuelve a consultar; los fallos se muestran hasta actualizar la lista |
| Otra edición en curso deshabilitaba el botón sin explicación | Se muestra el motivo y el botón sigue habilitado |
| Mensajes de rechazo duplicados o sin puntuación | Una sola indicación, con las oraciones separadas |
| El foco se perdía al actualizar, reintentar o fallar | Vuelve al control usado o pasa al aviso |

## Verificación

- RED: con las siete pruebas nuevas, `npm test` dio 93 correctas y 7 fallidas, por el comportamiento descrito. Una aserción agregada después, sobre conservar el aviso de éxito, falló antes de su corrección.
- GREEN: `npm test`, 100 de 100.

## Límites

- No se repitió el recorrido en navegador sobre los avisos nuevos.
- El backend no cambió en estas correcciones; no se ejecutaron sus pruebas en este equipo.
- El listado de pendientes sigue sin paginación, como decidió el [diseño](../design.md). Queda a acordar por el equipo.
