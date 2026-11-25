# Design

## Context

C-16 expone `IdentidadCuentaPaciente` y `ContratoAutorizacionCuentaPaciente` desde el servicio de cuentas. El filtro HTTP existente resuelve identidades del equipo. C-05 provee las referencias persistidas de prestación y profesional. El backend usa migraciones Flyway y un `Clock` inyectable en UTC.

## Goals / Non-Goals

La gestión se expone como servicio de aplicación consumible por el backend, con persistencia transaccional. El adaptador HTTP del paciente y la interfaz quedan para C-24. El cálculo de compatibilidad de huecos con duración, recursos e intervalo queda para C-22.

## Decisions

- Recibir la identidad autenticada de C-16 y exigir el vínculo antes de acceder a los datos. Derivar el dueño de esa identidad y buscar por identificador y paciente. Devolver el mismo resultado de ausencia ante un identificador inexistente o ajeno. Usar DTOs del servicio separados de las entidades JPA.
- Persistir una fila por inscripción con UUID, referencia de paciente, prestación, profesional nullable, fechas y marca de baja. Guardar las franjas como filas dependientes y reemplazarlas en la misma transacción al editar. Usar claves foráneas, restricciones para rangos ordenados e índice por paciente y prestación.
- Calcular la vigencia con la fecha local obtenida del reloj inyectable. Se propone usar `America/Argentina/Buenos_Aires` para la sede y conservar UTC para instantes. La fecha final es inclusiva. Dar de baja mediante una marca persistida conserva el registro consultable. Reservar un cupo desde el alta abarca también los rangos que empiezan en el futuro.
- Serializar altas, ediciones y bajas del mismo paciente mediante un bloqueo pesimista de su fila persistida. Adquirirlo antes de leer la inscripción, calcular la fecha local y verificar el límite en la misma transacción. Una restricción única que dependa de la fecha actual requeriría materializar el vencimiento. Calcularlo al operar permite reutilizar el cupo en el cambio de día sin coordinación con un proceso programado.
- Validar las referencias contra C-05 y las franjas como pares de horas locales del mismo día. Se propone admitir una única fecha, franjas múltiples y el cambio de prestación durante la edición. La validación de una inscripción se limita a la integridad de sus preferencias.

## Risks / Trade-offs

- Escrituras simultáneas del mismo paciente → mantener breve la transacción y comprobar con conexiones independientes que las operaciones concurrentes respetan el límite.
- Error durante una edición que reemplaza franjas → persistir el cambio de preferencias y sus franjas en una única transacción.
- Reintento de baja → devolver la inscripción ya dada de baja con su marca original. Si ya venció, devolver su estado vencido y conservar el registro.

## Migration Plan

La migración Flyway V12 crea las tablas e índices de espera con referencias a las tablas vigentes. Se verifica el arranque limpio y la actualización desde V11. Una reversión de la aplicación que conserve los datos requiere una versión compatible con el historial V12, porque la validación estricta de Flyway rechaza un historial con una versión desconocida.
