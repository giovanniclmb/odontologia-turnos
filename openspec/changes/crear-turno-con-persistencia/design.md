# Design

## Context

El dominio `Agenda.crear` de C-01 recibe reservas ocupantes, un intervalo explícito y un `Instant` de inicio. C-03 aporta identidad y permisos por operación. C-04 conserva los UUID de paciente y permite comprobar la asignación activa. C-05 aporta las referencias y su estado, la duración de prestación y el intervalo vigente. El contrato de ocupación de C-15 sigue pendiente.

## Goals / Non-Goals

**Goals:** usar los contratos de C-01 a C-05, devolver resultados de servicio autorizados y persistir el alta con exclusión transaccional sobre ambos recursos.

**Non-Goals:** transporte HTTP, cambios de identidad o permisos base, interfaz, comunicaciones y transiciones posteriores al alta.

## Decisions

### Contrato de servicio interno acordado

- La entrada de alta contiene identidad autenticada, UUID de paciente, prestación, profesional y sillón, inicio como `Instant` y ajuste opcional en minutos enteros positivos. La operación aplica `turno:crear` y comprueba alcance por registro antes de exponer datos de referencia o conflictos. Los resultados distinguen entrada o referencia inválida, intervalo sin configurar, conflicto y acceso denegado. Un conflicto no identifica al paciente ni devuelve datos de otro turno.
- La consulta recibe identidad, UUID de sillón e inicio y fin del período. Aplica `agenda-sillon:consultar` y filtra por profesional vinculado en odontología antes de construir el DTO. Usa intersección temporal y orden por inicio. Devuelve datos de agenda y duración efectiva. Las consultas del equipo no sustituyen las comprobaciones transaccionales del alta.
- El alta compara el inicio con el `Clock` existente al validar. La duración de C-05 se toma al crear y se pasa, junto con el posible ajuste, a `Agenda.crear`. La reserva conserva su duración efectiva. La consulta de reservas ocupantes incluye las que puedan infringir el margen, incluso cuando terminan antes o empiezan después del turno solicitado.

### Modelo físico propuesto para revisión

Entidad de persistencia separada de `Agenda`, con UUID generado por la aplicación, cuatro claves foráneas a referencias existentes, inicio y fin persistidos como instantes, duración efectiva positiva y estado inicial pendiente. La migración debe proteger la consistencia del período y otorgar al rol de aplicación únicamente los permisos que requiere C-06. La migración y nombres físicos se revisarán sobre la secuencia Flyway integrada.

### Exclusión compartida pendiente de C-15

La alternativa recomendada es serializar transacciones que compartan profesional o sillón. Si C-15 adopta bloqueos por recurso, ambas operaciones deben tomar los bloqueos en el mismo orden y leer las reservas y bloqueos tras obtenerlos. También se puede reutilizar otro mecanismo acordado con C-15 si garantiza la misma exclusión cruzada. Antes de Apply hay que concretar el contrato de consulta en ambos sentidos, los límites y precisión temporales, cómo se aplica el intervalo alrededor de bloqueos, y el resultado del intento incompatible.

C-01 exige que cada reserva ocupante tenga los dos recursos. La comprobación de bloqueos debe representar un bloqueo unilateral según el contrato de C-15.

## Risks / Trade-offs

- Una consulta y un guardado sin exclusión compartida admiten una carrera entre turnos o entre turno y bloqueo. Verificar los casos de contención con transacciones y conexiones independientes en PostgreSQL, en ambos órdenes de la carrera turno–bloqueo.
- La vista de odontología muestra solo sus turnos, pero la disponibilidad depende de todas las reservas y bloqueos que afectan los recursos. Evaluar esa ocupación internamente y responder con un conflicto sin datos ajenos.
- El intervalo y la duración de catálogo pueden cambiar. Usar el valor vigente al validar el alta y mantener los datos persistidos de turnos existentes.

## Migration Plan

Asignar la siguiente versión Flyway sobre la base integrada de C-15. Verificar la actualización desde el esquema anterior y el arranque con el rol de aplicación. Ante un fallo en una base nueva, corregir y volver a verificar antes de desplegar. Para una base ya migrada, preservar las reservas y corregir mediante otra migración.

## UI Mockups

Decision: no (no UI)
