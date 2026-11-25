# Proposal

## Why

La agenda ya existe como servicio (alta, consulta por sillón, bloqueos, intervalo), pero no tiene transporte HTTP ni interfaz: recepción y odontología no pueden usarla. C-10 la hace operable (US-004, US-005, US-009 y US-010) y es dependencia de C-12, C-13, C-23, C-24 y C-25.

## What Changes

- Exponer por HTTP la consulta de agenda por sillón y período, el alta de turnos, el alta y la consulta de bloqueos, el intervalo de sede y los catálogos de sillones y prestaciones que consumen los formularios.
- Agregar la vista diaria y semanal (lunes a domingo) por sillón, con duración efectiva, estado y bloqueos, en hora de `America/Argentina/Mendoza` y sin imponer jornada laboral.
- Agregar el formulario de alta de turno con duración ajustable y mensajes de validación y disponibilidad.
- Agregar el formulario de bloqueo, solo para recepción.
- Agregar la sección Configuración, visible solo para recepción, para consultar y establecer el intervalo de la sede.
- Agregar el permiso `bloqueo:consultar` para ambos roles. Recepción ve todos los bloqueos del período; odontología solo los que afectan al sillón consultado o a su profesional, sin identificar a otros profesionales.
- Mostrar nombre y apellido del paciente en cada turno de la agenda. Para odontología **amplía el acceso**: incluye turnos propios de pacientes sin asignación activa, únicamente con esos dos datos.

Quedan fuera: reprogramación y cancelación (C-12), confirmaciones (C-13), modificación y desbloqueo (PA-19 sigue abierta), alta o edición de profesionales, sillones y prestaciones, y cualquier valor inicial del intervalo.

## Capabilities

### New Capabilities

- `interfaz-agenda-y-alta`: superficie interna y su contrato HTTP para agenda diaria y semanal, alta de turnos, bloqueos y configuración del intervalo.

### Modified Capabilities

- `acceso-y-permisos-internos`: nuevo permiso `bloqueo:consultar` y excepción acotada de nombre y apellido en la agenda propia de odontología.
- `agenda-persistida`: la consulta de agenda identifica al paciente de cada turno por nombre y apellido.
- `bloqueos-de-recursos`: consulta autorizada de bloqueos con alcance por rol.

## Impact

- Backend: controladores y DTO de agenda, bloqueos, sede y recursos; manejo de errores; permiso nuevo; consulta de bloqueos y de nombres. Sin migración Flyway ni dependencias nuevas.
- Frontend: módulos nuevos de agenda y configuración, navegación y estilos. Sin framework ni dependencias nuevas.
- Política visual `system-and-mockups` (revisión 1): requiere aprobar los mockups de este change antes de Apply y verificar a 390×844 y 1440×900.
- Responsable nominal: Luciano; Giovanni confirmó el 25/11/2026 que la toma del change está coordinada.
