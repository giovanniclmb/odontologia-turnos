# Design

## Context

Ver `proposal.md` — Why. C-18 se construye sobre `main` (f80c617), donde C-08 ya está implementado, verificado y archivado, y C-17 está integrado. La cancelación interna existe en `ServicioAgenda` con identidad, permisos, versión y transacción. C-18 no la reemplaza ni la expone: la usa como integración después de autorizar por token.

Los turnos persisten su inicio como `Instant`. La KB no define una zona horaria IANA para la sede, por lo que las 48 horas se miden como tiempo transcurrido entre instantes.

## Goals / Non-Goals

**Goals:**
- Autorizar una cancelación limitada del paciente mediante token opaco hasheado, vinculado al turno y su versión.
- Mantener la transacción atómica de cancelación y liberación de horario de `agenda-persistida`.
- Evitar replay, filtración de datos y cancelaciones accidentales por previsualizadores de correo.
- Dejar preparado el contrato que C-19 usará para enviar el enlace.

**Non-Goals:**
- No implementa envío de email ni C-19.
- No agrega interfaz visual ni superficie de paciente; corresponde a C-23.
- No cambia roles, permisos internos ni migraciones de turnos.
- No fija una zona horaria de sede ni convierte horarios para presentación.

## Decisions

### Token opaco y almacenamiento por hash

Se usará un token aleatorio de alta entropía, codificado en base64url sin relleno, y se persistirá únicamente su hash SHA-256. Esto evita que una lectura de la base de datos permita cancelar turnos y permite comparar el token presentado sin almacenarlo en claro.

Alternativa descartada: token aleatorio guardado en claro en la tabla de turnos. Rechazada por exposición ante lectura de la base y por no permitir revocación independiente.

### Vinculación al turno y a la versión

Cada token se asocia al identificador del turno y a la versión vigente al momento de la emisión. La cancelación por token exige que la versión siga vigente. Así, una reprogramación o cualquier cambio que incremente la versión invalida automáticamente el token anterior.

Alternativa descartada: token vinculado solo al turno. Rechazada porque no invalidaría el enlace al reprogramar sin lógica adicional.

### Validación, consumo y cancelación atómicos

La operación recibirá el token, lo hasheará, buscará el registro no consumido, validará elegibilidad, estado, corte de 48 horas y que el turno no haya comenzado, y luego cancelará mediante la lógica transaccional existente, consumiendo el token en la misma transacción. La concurrencia se resolverá con bloqueo de fila del token y del turno; solo una transacción podrá consumirlo.

Alternativa descartada: validar en una transacción y cancelar en otra. Rechazada por ventana de carrera y posibilidad de consumo sin cancelación.

### Corte de 48 horas por instantes

El límite se evaluará como `instanteActual <= inicioTurno.minus(Duration.ofHours(48))`, con borde inclusivo. No se introduce `ZoneId` porque la KB no lo define y los turnos se persisten como `Instant`.

Alternativa descartada: convertir a fecha/hora local de la sede. Rechazada por falta de zona aprobada y riesgo de inventar una regla no acordada.

### GET no mutante y POST explícito

`GET` solo validará o presentará la confirmación sin efectos persistentes. La cancelación ocurrirá únicamente mediante `POST` con acción explícita del paciente. Esto evita cancelaciones por previsualizadores o escáneres automáticos de correo.

Alternativa descartada: cancelar directamente con GET. Rechazada por riesgo de cancelación accidental.

### No emisión cuando la ventana ya está cerrada

Si al crear o reprogramar un turno quedan menos de 48 horas, no se emitirá token y C-19 omitirá el enlace. Esto evita ofrecer un enlace inmediatamente vencido.

### Respuestas sin filtración

Las respuestas para token ausente, inválido, expirado, consumido o no elegible serán genéricas y no revelarán existencia, estado ni datos del turno. El éxito devolverá el resultado acordado de cancelación sin datos clínicos.

### Integración con la cancelación existente

Se agregará un punto de entrada acotado en `ServicioAgenda` que reciba la cancelación ya autorizada por token y delegue en la misma lógica transaccional de cancelación interna. La autorización interna por identidad y permisos no se reutilizará como credencial del paciente.

## Risks / Trade-offs

- [Token copiado o filtrado antes del vencimiento] → el token es de un solo uso y se consume al cancelar; una filtración posterior no puede cancelar de nuevo.
- [Reprogramación que invalida el enlace después de que el paciente lo recibió] → C-19 enviará un enlace nuevo; el anterior responderá como inválido sin revelar datos.
- [Concurrencia entre cancelación por enlace y otra edición del turno] → se resolverá por bloqueo de fila y versión; la que encuentre otra versión será rechazada.
- [Dependencia de C-19 para la entrega del enlace] → C-18 deja el token disponible y define el contrato; sin C-19 no habrá envío real.
- [No se fija zona horaria] → el corte se mide por instantes; la presentación local queda para una decisión futura.

## Migration Plan

Sin cambios de esquema de turnos. Se agrega una tabla para tokens de cancelación. El despliegue puede realizarse junto con C-19; mientras C-19 no esté listo, los tokens se generan y validan pero no se envían por email.

## Open Questions

- Longitud exacta y alfabeto del token opaco, sujeto a revisión de seguridad durante Apply.
- Nombre final de la tabla y columnas de persistencia, sujeto a la convención de migraciones del proyecto.
- Ruta HTTP exacta y formato de respuesta para la superficie de C-23, sujeto al contrato de API que se acuerde en Apply.

## UI Mockups

Decision: no (no UI)
