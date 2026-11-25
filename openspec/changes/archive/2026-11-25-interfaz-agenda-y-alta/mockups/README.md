# Mockups — C-10 interfaz-agenda-y-alta

Approval: approved (2026-11-25, by user)
Preview: rendered (2026-11-25, 16 capturas en `previews/`, 390×844 y 1440×900, sin desbordamiento horizontal)
Design system: revisión 1, sin cambios de tokens ni assets.

| Archivo | Pantalla | Escenarios |
| --- | --- | --- |
| `agenda-dia.html` | Vista diaria de recepción: turnos, estados y bloqueo | Vista diaria y semanal; Turno cancelado |
| `agenda-semana.html` | Semana de lunes a domingo; apilada en pantallas angostas | Vista diaria y semanal |
| `agenda-odontologia.html` | Solo turnos propios y bloqueos recortados | Alcance de bloqueos para odontología |
| `turno-alta.html` | Alta por recepción con rechazo de disponibilidad | Alta de turno desde la interfaz |
| `turno-alta-odontologia.html` | Alta con profesional fijo y pacientes asignados | Alta de turno desde la interfaz |
| `bloqueo-alta.html` | Alta de bloqueo con rechazo por turno existente | Alta de bloqueo desde la interfaz |
| `configuracion.html` | Intervalo sin configurar, vigente e inválido (el segundo bloque es un ejemplo de estado) | Configuración del intervalo por recepción |
| `estados.html` | Carga, vacío, error, catálogo e intervalo faltantes (referencia, no es una pantalla) | Vista diaria y semanal; Errores de agenda distinguibles |

La vista es una lista cronológica y no una grilla horaria, porque no hay jornada laboral definida (PA-04). Los datos son ficticios.

Los HTML usan los tokens y `frontend/public/styles.css`; los estilos nuevos están en `agenda.css`.
