# Proposal

## Why

La agenda necesita impedir que un profesional o sillón se asigne a turnos incompatibles y respetar el intervalo común de la sede. Giovanni eligió C-01 como primer change académico para demostrar esta regla con un dominio Java aislado, TDD y escenarios verificables antes de incorporar persistencia o acceso HTTP.

## What Changes

- Incorporar en `backend/` un módulo Java 25 con Maven y JUnit, aprobado por Giovanni el 19/11/2026. El build y los comandos se crearán en Apply.
- Modelar únicamente referencias ficticias de paciente, profesional, sillón y prestación, duración efectiva, solicitud de alta y reservas ocupantes recibidas explícitamente.
- Calcular la duración desde la prestación salvo ajuste explícito y comprobar disponibilidad de ambos recursos antes de devolver el turno propuesto.
- Aplicar el intervalo explícito antes y después de cada reserva relevante; admitir exactamente la separación mínima configurada, como confirmó el usuario.
- Preparar pruebas de caso feliz, conflictos independientes, ajuste de duración y bordes; registrar RED/GREEN y Verify al implementar.
- Entregar documentación para ejecutar el módulo. La selección de C-01 responde PA-13; el resto de las PA solo se acota a este dominio, no se declara globalmente resuelto.

## Capabilities

### New Capabilities

- `disponibilidad-de-turnos`: creación de un turno de dominio con profesional y sillón disponibles, duración efectiva e intervalo explícito, contra una colección de reservas ocupantes.

### Modified Capabilities

Ninguna. El inventario del CLI no contiene specs existentes.

## Impact

Se crearán `backend/pom.xml`, fuentes y pruebas de dominio y `backend/README.md` durante Apply. No existe todavía código de producto que migrar ni contratos públicos que romper. La planificación conserva las dependencias y el alcance de C-01 en CHANGES.

Responsable: Giovanni. Gobernanza: CRITICO; los documentos requieren revisión y un pedido explícito posterior de Apply antes de escribir código.

Fuera de alcance: Spring Boot y su versión, HTTP, login, roles, PostgreSQL, ORM, migraciones, UI, bloqueos administrables, cancelación, reprogramación, emails y lista de espera. El módulo no guarda turnos ni garantiza exclusión concurrente. Recibe únicamente reservas que ocupan recursos; el filtrado por estados, los horarios laborales y el valor inicial de producción siguen pendientes para los changes correspondientes.
