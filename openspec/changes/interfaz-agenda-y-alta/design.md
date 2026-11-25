# Design

## Context

Ver [proposal.md](proposal.md). `ServicioAgenda`, `ServicioBloqueos`, `ServicioConfiguracionSede` y `ServicioRecursosAgenda` ya resuelven las reglas y la concurrencia; solo `/auth`, `/pacientes` y `/recursos/profesionales` tienen HTTP. El frontend de C-11 es TypeScript vanilla con un `main.ts` único y sin router. No existe una consulta autorizada de bloqueos.

## Goals / Non-Goals

**Goals:**

- Controladores delgados sobre los servicios vigentes, sin duplicar reglas de agenda.
- Módulos de frontend nuevos y aislados, con respuestas validadas en el límite.

**Non-Goals:**

- Refactorizar la interfaz o el cliente de C-11, o cambiar el mecanismo de sesión.
- Migraciones, catálogos editables, reprogramación, cancelación, confirmación o desbloqueo.

## Decisions

### Contrato HTTP

| Método y ruta | Uso | Permiso |
| --- | --- | --- |
| `GET /agenda/sillones/{sillonId}/turnos?desde&hasta` | Turnos visibles del sillón | `agenda-sillon:consultar` |
| `POST /agenda/turnos` | Alta (`201`) | `turno:crear` |
| `GET /agenda/bloqueos?sillonId&desde&hasta` | Bloqueos visibles | `bloqueo:consultar` |
| `POST /agenda/bloqueos` | Alta (`201`) | `bloqueo:crear` |
| `GET /sede/intervalo` | `{ "minutos": number \| null }` | identidad interna |
| `PUT /sede/intervalo` | Establecer | `config:intervalo` |
| `GET /recursos/sillones`, `GET /recursos/prestaciones` | Catálogos | identidad interna |

- Instantes en ISO 8601 con zona. Las consultas admiten un período máximo de 7 días: alcanza para la vista semanal y evita listados sin límite. Se descartó paginar porque la grilla necesita el período completo.
- DTO `record` propios de transporte, validados al ingresar. `minutos` y `duracionAjustadaMinutos` rechazan decimales en vez de truncarlos.
- Se conserva el formato de error `{codigo, mensaje}` de C-11. Migrar a `ProblemDetail` rompería ese contrato y queda fuera.

| Caso | Estado y código |
| --- | --- |
| `ConflictoAgendaException` | `409 horario_no_disponible` |
| `TurnoOcupanteException` | `409 bloqueo_afecta_turno` |
| Intervalo sin configurar | `409 intervalo_sin_configurar` |
| Parámetro o cuerpo mal formado | `400 solicitud_invalida` |

El caso sin configurar usa una excepción propia que extiende `IllegalStateException`, para no traducir cualquier estado inválido del sistema.

### Lecturas con alcance

- El nombre se compone en un caso de uso de lectura junto a la agenda: primero se aplica el alcance de `consultarAgendaSillon` y después se resuelven en una sola consulta los pacientes de esos turnos, proyectando nombre y apellido. No se reutiliza `ServicioPacientes.consultar`, que deniega los no asignados, ni se relaja su regla. Una consulta por turno queda descartada.
- La consulta de bloqueos vive en un método autorizado distinto de `ConsultaBloqueos.buscar`, que sigue siendo interno para alta y reprogramación. El recorte por rol ocurre en el servidor: la respuesta es `{id, inicio, fin, sillonId, profesionalId}`, con en `null` las referencias que odontología no debe ver.

### Frontend

- `src/agenda/` (API, validadores, composición de vista, formularios), `src/configuracion/` y `src/tiempo/` (conversión Mendoza con `Intl`, sin desplazamiento fijo ni dependencias). `main.ts` solo suma navegación.
- Las respuestas nuevas entran como `unknown` y se validan con funciones propias; no se usa `as`. Zod queda descartado para no sumar dependencia ni bundler.
- El estado de la vista (sección, día o semana, fecha, sillón) va en el hash de la URL.
- El render usa nodos y `textContent`; los nombres no se interpolan en HTML.
- Vista como lista cronológica por día, no grilla horaria: sin jornada definida (PA-04), una grilla obligaría a inventar un rango. En 1440 px la semana usa columnas; en 390 px se apila.
- El botón de envío se deshabilita mientras la petición está en curso. Es la única protección contra bloqueos duplicados, que el modelo permite.

## UI Mockups

- Design system revision: 1
- Decision: yes (2026-11-25, por la política `system-and-mockups`)
- Mockups: openspec/changes/interfaz-agenda-y-alta/mockups/
- Approval: approved (2026-11-25, by user: mockups y plan)
- Preview: rendered (2026-11-25, previews/)
- Visual verify: pending

## Risks / Trade-offs

- [Odontología ve nombres de pacientes sin asignación] → decisión expresa del 25/11/2026; solo nombre y apellido, solo turnos propios, con pruebas negativas sobre los demás datos.
- [El token de C-11 se guarda en `localStorage`, contrario al estándar de seguridad y sin excepción documentada] → se reutiliza sin ampliarlo; queda señalado como deuda a resolver en un change propio.
- [No hay interfaz ni HTTP para crear sillones o prestaciones] → las pruebas usan datos ficticios por servicio; la carga de datos de demostración queda para acordar (C-14).
- [Odontología puede ver libre un horario ocupado por otro profesional] → comportamiento vigente; el alta responde con conflicto genérico.

## Migration Plan

Sin cambios de esquema. El permiso nuevo vive en código. Revertir el commit restituye el estado anterior.
