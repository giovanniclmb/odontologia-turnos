# Design

## Context

Ver [propuesta](proposal.md) y [delta de agenda](specs/interfaz-agenda-y-alta/spec.md). C-07 y C-08 ya exponen `ServicioAgenda.reprogramar` y `ServicioAgenda.cancelar` con control de versión y transacciones. C-09 extendió ambas operaciones a turnos confirmados futuros de recepción. C-10 ya entrega estado y versión opaca en su consulta por sillón y período y comparte el cliente HTTP, los catálogos y la zona de sede.

## Goals / Non-Goals

**Goals:** conectar las operaciones internas a la agenda mediante el contrato HTTP y los componentes existentes.

**Non-Goals:** esquema de PostgreSQL, edición de datos del paciente o la prestación, cancelación por enlace, notificaciones y gestión de interesados en un hueco.

## Decisions

### Transporte de comandos

Extender `AgendaController` con los dos comandos y mantener la transacción dentro del servicio. Usar la identidad interna resuelta por la API y los DTO de transporte. El contrato de consulta vigente ya proporciona los datos para iniciar el flujo. Una consulta de detalle adicional duplicaría esa proyección y una actualización genérica del turno ampliaría los campos editables.

| Método y ruta | Solicitud | Respuesta exitosa |
| --- | --- | --- |
| `POST /agenda/turnos/{turnoId}/reprogramacion` | `versionEsperada` como cadena opaca, `inicio` como instante ISO 8601, `profesionalId`, `sillonId` | `200` con `TurnoResponse` |
| `POST /agenda/turnos/{turnoId}/cancelacion` | `versionEsperada` como cadena opaca | `200` con `TurnoResponse` |

Validar forma y campos requeridos siguiendo `Solicitudes` y los límites de instantes del contrato de agenda. Preservar la precisión de la asignación original cuando no cambia su inicio. Usar el inicio y fin efectivos devueltos por el servicio.

En una reprogramación efectiva, el adaptador pasa el fin máximo del contrato HTTP al servicio. El servicio comprueba ese límite con la duración efectiva que leyó bajo exclusión antes de guardar. La invocación Java original conserva el rango de PostgreSQL. Un destino idéntico puede devolver la asignación vigente sin escritura. La cancelación y una reprogramación a un destino admisible siguen disponibles para una asignación anterior con fin fuera del rango HTTP.

| Resultado | HTTP y código |
| --- | --- |
| Sesión ausente o vencida | `401 no_autenticado` |
| Operación fuera de alcance | `403 autorizacion_denegada` |
| Datos inválidos, turno iniciado o estado incompatible | `400 solicitud_invalida` |
| Referencia inexistente para recepción | `404 solicitud_invalida`, consistente con el transporte actual |
| Destino ocupado, bloqueado o incompatible con el intervalo | `409 horario_no_disponible` |
| Intervalo sin configurar | `409 intervalo_sin_configurar` |
| Versión desactualizada | `409 version_turno_desactualizada`, con instrucción para recargar |

El mapeo de versión debe usar la excepción tipada. La interfaz distingue rechazos de agenda por código, sin interpretar el texto como condición lógica. La denegación genérica de odontología precede a cualquier detalle sobre un turno fuera de su alcance.

### Controles y alcance

Agregar acciones en las representaciones diaria y semanal de C-10. Mostrar los controles según estado, rol e inicio visible. La proyección de odontología incluye incluso turnos propios de pacientes sin asignación activa. El servicio sigue comprobando esa asignación al escribir y devuelve denegación genérica cuando corresponde. De esta manera no hace falta incorporar datos de asignación a la proyección ni crear una consulta por turno. La visibilidad previa del turno tampoco permite elegir otro profesional en odontología.

Abrir la edición y la confirmación desde el turno seleccionado, conservando filtros y una referencia de foco a la acción de origen. Mostrar el paciente, la prestación de catálogo y la duración guardada, incluso si el recurso original ya no está activo. Mostrar únicamente opciones de destino habilitadas por los catálogos existentes. Usar fechas y horas de Mendoza y la versión recibida de la agenda. Mantener la semántica de envío y navegación de C-10 con una clave compartida por turno para ambas operaciones.

### Actualización posterior

Hacer la recarga con la consulta de C-10 y conservar la vista, el período y el sillón seleccionados. Si el destino de una reprogramación queda fuera de los filtros, mostrar dónde quedó y ofrecer la acción Ver destino. Distinguir el éxito con reconsulta fallida y permitir recuperar la vista sin reenviar la mutación. Conservar los campos ante el conflicto de disponibilidad y bloquear el reenvío de una versión desactualizada hasta que se vuelva a consultar y el usuario elija qué hacer.

La agenda representa el turno cancelado como registro histórico visible. Su período vuelve a ser utilizable si el servidor acepta una nueva reserva considerando las demás restricciones. Una marca local de disponibilidad podría ocultar un bloqueo o una edición concurrente.

## UI Mockups

- Política `system-and-mockups`, [sistema de diseño revisión 1](../../../design-system/README.md) aprobado el 23/11/2026.
- Prototipo del cambio en [mockups/index.html](mockups/index.html), con estados y variantes para revisión.
- Plan y mockups de C-12 aprobados por el usuario el 25/11/2026. Verificar en Apply el resultado a 390×844 y 1440×900 contra el mockup aprobado.

## Risks / Trade-offs

- Una sesión o asignación puede cambiar mientras el formulario está abierto. El servicio vuelve a validar y la interfaz conserva una ruta de regreso sin aplicar cambios locales.
- Dos operadores pueden actuar sobre la misma reserva. El adaptador traslada la versión consultada, informa un conflicto de versión y pide recarga.
- Un cambio confirmado puede quedar fuera de la vista actual. El mensaje y la acción Ver destino permiten localizarlo sin perder los filtros del operador.
- Un fallo de red puede dejar incierto el resultado. La interfaz ofrece volver a consultar antes de reenviar.

## Migration Plan

Entregar backend y frontend coordinados. Verificar ambos comandos sobre la base existente y los recorridos de consulta y alta. El rollback del despliegue consiste en volver a publicar la versión anterior de ambos componentes. Las reservas que se hayan reprogramado o cancelado conservan el estado persistido.
