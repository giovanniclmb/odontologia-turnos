# Design

## Context

Ver [propuesta](proposal.md) y [delta](specs/interfaz-agenda-y-alta/spec.md). C-09 ofrece `consultarPendientes` y `confirmar` en `ServicioAgenda`, con autorización, consulta de toda la sede y control de versión; no tiene transporte HTTP. `ConsultaAgenda` ya resuelve nombres de pacientes en lote después de limitar los turnos autorizados. C-10 aporta navegación, catálogos, zona de Mendoza y componentes; C-12 aporta manejo de ediciones concurrentes y errores.

## Goals / Non-Goals

**Goals:** conectar C-09 con una superficie operable desde agenda preservando autorización, proyección mínima y verdad del servidor.

**Non-Goals:** migraciones, envío de mensajes, contacto del paciente, confirmación automática o cambios en reglas y permisos de C-09.

## Decisions

### Contrato y proyección

Agregar `GET /agenda/turnos/pendientes` y `POST /agenda/turnos/{turnoId}/confirmacion`, con `versionEsperada` cadena opaca y respuesta de edición `TurnoResponse` existente. Reutilizar el manejo de errores de C-12: 401 `no_autenticado`, 403 `autorizacion_denegada`, 400 `solicitud_invalida`, 404 `solicitud_invalida` y 409 `version_turno_desactualizada`. El servicio permanece responsable de la autorización, orden, elegibilidad y transacción.

Para el listado, ampliar la consulta de proyección de agenda: primero obtener los turnos autorizados mediante C-09 y después resolver nombres en lote con `PacienteReferencias.buscarNombres`, como en la consulta por sillón. Devolver `TurnoAgendaResponse`, sin id, teléfono ni correo del paciente ni datos clínicos. Evitar consultas individuales y no filtrar clientes por fecha ni sillón; la regla de pertenencia del backend abarca toda la sede. Una alternativa de resolver datos en el navegador expondría identificadores de paciente y duplicaría consultas.

### Operación y recuperación

Proponer una vista de pendientes accesible desde Agenda solo para recepción, separada de los filtros de sillón y período. Se muestran nombre, horario de Mendoza, profesional, sillón y prestación mediante catálogos existentes. La acción registra directamente un hecho ya verificado por recepción y no inicia contacto; no es necesario inventar un paso de comunicación. El backend sigue decidiendo el acceso y el estado efectivo.

Reusar el control de envío por turno de C-12, conservar la versión exacta y consultar de nuevo tras éxito. En conflicto de versión, estado incompatible o inicio alcanzado, indicar el rechazo y ofrecer actualizar; con resultado de red incierto, deshabilitar el reenvío hasta consulta nueva. Si el registro fue aceptado y falla la consulta, distinguir ese éxito y reintentar solo la lectura. La navegación y respuestas tardías no deben actualizar una vista obsoleta.

## UI Mockups

- El usuario aprobó el plan y el mockup de C-13 el 25/11/2026 para continuar con Apply.
- Política `system-and-mockups`, [sistema de diseño revisión 1](../../../design-system/README.md). El diseño de C-13 y sus mockups requieren aprobación explícita antes de Apply.
- El [prototipo de C-13](mockups/README.md) presenta listado, vacío, registro y recuperación con datos ficticios. Verificar 390 × 844 y 1440 × 900; la aprobación de C-12 no aprueba automáticamente C-13.

## Risks / Trade-offs

- La lista puede quedar desactualizada al transcurrir el tiempo o por otra operación → el servicio valida estado, versión e inicio al registrar y la interfaz ofrece recargar.
- Una respuesta perdida no permite saber si se guardó → no reenviar sin consultar de nuevo.
- Catálogos pueden no contener recursos inactivos → conservar identificación utilizable sin omitir turnos autorizados; verificar el comportamiento de catálogo al implementar.
- El listado de toda la sede puede ser largo → mantener el contrato de C-09 sin inventar paginación o límites de negocio; evaluar rendimiento con datos representativos.

## Migration Plan

Publicar backend y frontend coordinados sin migración. Verificar permisos y los recorridos con PostgreSQL existente. El rollback consiste en restaurar ambos componentes previos; los turnos confirmados permanecen en el estado persistido compatible con C-09.
