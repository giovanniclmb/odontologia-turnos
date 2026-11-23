# Design

## Context

El dominio `Agenda.crear` de C-01 recibe reservas ocupantes, un intervalo explícito y un `Instant` de inicio. C-03 aporta identidad y permisos por operación. C-04 conserva los UUID de paciente y permite comprobar la asignación activa. C-05 aporta las referencias y su estado, la duración de prestación y el intervalo vigente. C-15 aporta la migración V6, el protocolo transaccional y la consulta interna de bloqueos. Su contrato de lectura de turnos espera un adaptador real cuando C-06 incorpore la tabla de turnos.

## Goals / Non-Goals

**Goals:** usar los contratos de C-01 a C-05, devolver resultados de servicio autorizados y persistir el alta con exclusión transaccional sobre ambos recursos.

**Non-Goals:** transporte HTTP, cambios de identidad o permisos base, interfaz, comunicaciones y transiciones posteriores al alta.

## Decisions

### Contrato de servicio interno acordado

- La entrada de alta contiene identidad autenticada, UUID de paciente, prestación, profesional y sillón, inicio como `Instant` y ajuste opcional en minutos enteros positivos. La operación aplica `turno:crear` y comprueba alcance por registro antes de exponer datos de referencia o conflictos. Los resultados distinguen entrada o referencia inválida, intervalo sin configurar, conflicto y acceso denegado. Un conflicto no identifica al paciente ni devuelve datos de otro turno.
- La consulta recibe identidad, UUID de sillón e inicio y fin del período. Aplica `agenda-sillon:consultar` y filtra por profesional vinculado en odontología antes de construir el DTO. Trunca los extremos a microsegundos, valida `inicio < fin` y devuelve la intersección semiabierta en orden por inicio. Devuelve datos de agenda y duración efectiva.

### Precisión y disponibilidad

- Truncar el inicio solicitado una vez a microsegundos. Comparar ese instante con el `Clock` existente y exigir que sea estrictamente posterior. Pasarlo a `Agenda.crear` junto con la duración de C-05 y el posible ajuste. El fin se calcula sumando los minutos efectivos completos al inicio normalizado. Usar exactamente los mismos extremos para validar contra turnos, consultar bloqueos y persistir. Tratar fechas o cálculos fuera de rango como entradas inválidas.
- Consultar las reservas ocupantes que compartan profesional o sillón y puedan infringir el margen, incluidas las que terminan antes o empiezan después del turno solicitado. Usar `Agenda.crear` para aplicar el intervalo vigente y aceptar la separación exacta. Para bloqueos, consultar `ConsultaBloqueos.buscar` con `RecursosBloqueo` y `PeriodoBloqueo`, usando intersección estricta `inicioExistente < finSolicitado && finExistente > inicioSolicitado`, sin margen entre turno y bloqueo.

### Modelo físico propuesto para revisión

Entidad de persistencia separada de `Agenda`, con UUID generado por la aplicación, cuatro claves foráneas obligatorias a referencias existentes, inicio y fin `timestamptz`, duración efectiva positiva en minutos enteros y estado inicial `PENDIENTE`. La migración debe proteger la consistencia entre comienzo, fin y duración y otorgar al rol de aplicación únicamente los permisos que requiere C-06. Los nombres físicos y la numeración se revisarán sobre la secuencia Flyway vigente.

### Exclusión compartida con C-15

Ejecutar el alta en una transacción `READ COMMITTED`. Tras validar el permiso y el alcance, adquirir ambos recursos con `LocksRecursosAgenda.adquirir`. El método requiere una transacción existente (`MANDATORY`) y toma `FOR NO KEY UPDATE` por profesional y luego por sillón, con UUID ordenados dentro de cada tipo. Después de adquirir los locks, leer disponibilidad y conflictos, validar el horario y guardar en la misma transacción. Mantener los locks hasta la confirmación o reversión. La operación que espera vuelve a validar con las filas confirmadas cuando adquiere los locks.

Sustituir `AgendaSinTurnosPersistidos` por un único adaptador real de `ConsultaTurnosOcupantes`. Dentro de la transacción del bloqueo, comprobar si existe un turno ocupante que se intersecta con el período y coincide en alguno de los recursos indicados. La consulta interna necesita ver la ocupación de ambos recursos aunque la vista de agenda del operador tenga alcance restringido. La comprobación de bloqueos se realiza por su propio contrato para admitir bloqueos de un solo recurso.

## Risks / Trade-offs

- Verificar la exclusión con conexiones y transacciones independientes en PostgreSQL. Las pruebas entre turno y bloqueo deben ejecutar los servicios de producción de C-06 y `ServicioBloqueos.crear`, con el adaptador real de ocupación y las tablas del esquema migrado.
- La consulta de reservas ocupantes del alta debe incluir los turnos de cualquier profesional que comparta el sillón. El filtro por profesional se aplica a la consulta de agenda de odontología. Cuando un turno ajeno ocupa el sillón solicitado, el alta devuelve un conflicto genérico sin revelar datos de ese turno.
- El intervalo y la duración de catálogo pueden cambiar. Usar el valor vigente al validar el alta y mantener los datos persistidos de turnos existentes.

## Migration Plan

Coordinar la siguiente versión Flyway sobre la base integrada, que llega a `V6__bloqueos_de_recursos.sql`. Verificar la actualización desde V6 y el arranque con el rol de aplicación. Conservar las migraciones aplicadas. Ante un fallo de una versión ya migrada, preservar las reservas y corregir mediante otra migración.

## UI Mockups

Decision: no (no UI)
