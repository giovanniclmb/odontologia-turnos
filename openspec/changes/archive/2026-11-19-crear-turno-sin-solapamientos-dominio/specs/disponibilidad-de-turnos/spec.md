# Spec Delta

## Purpose

Comprobar la creación de un turno de dominio contra reservas ocupantes explícitas, evitando conflictos de profesional y sillón y respetando duración e intervalo común. Esta capacidad constituye la base aislada para integrar posteriormente la agenda persistida.

## ADDED Requirements

### Requirement: Referencias necesarias para el alta

El módulo SHALL exigir referencias no vacías de paciente, profesional, sillón y prestación, inicio y duración de referencia de la prestación. No verificará su existencia en catálogos externos.

#### Scenario: Recursos y referencias presentes
- **WHEN** se recibe una solicitud con todas sus referencias, inicio y duración válidos, sin reservas ocupantes
- **THEN** el módulo devuelve un turno con esas referencias y el período calculado

#### Scenario: Falta un recurso obligatorio
- **WHEN** falta la referencia del profesional o del sillón
- **THEN** el módulo rechaza la solicitud como entrada inválida y no devuelve un turno

#### Scenario: Falta una referencia administrativa necesaria
- **WHEN** falta la referencia del paciente o de la prestación
- **THEN** el módulo rechaza la solicitud como entrada inválida

### Requirement: Duración efectiva del turno

El módulo SHALL usar la duración de referencia de la prestación cuando no hay ajuste explícito y la duración ajustada cuando se proporciona. La duración efectiva SHALL ser positiva y determinar el fin a partir del inicio.

#### Scenario: Se utiliza la duración de la prestación
- **WHEN** se solicita un turno desde las 10:00 con prestación de 30 minutos sin ajuste
- **THEN** el turno resultante finaliza a las 10:30 y conserva una duración efectiva de 30 minutos

#### Scenario: Se respeta el ajuste explícito
- **WHEN** se solicita un turno desde las 10:00 con prestación de 30 minutos y ajuste a 45 minutos
- **THEN** el turno termina a las 10:45 y la disponibilidad se evalúa usando esos 45 minutos

#### Scenario: Duración efectiva inválida
- **WHEN** se proporciona una duración efectiva nula, cero o negativa
- **THEN** el módulo rechaza la entrada y no devuelve un turno

### Requirement: Separación del profesional

El módulo SHALL rechazar un alta cuando una reserva ocupante del mismo profesional se solapa con el período solicitado o deja menos del intervalo explícito entre ambos turnos, aunque el sillón solicitado esté libre.

#### Scenario: Profesional ocupado y sillón distinto
- **WHEN** el profesional P1 está reservado de 10:00 a 10:30 en S1 y se solicita P1 en S2 de 10:15 a 10:45
- **THEN** se rechaza el alta por indisponibilidad del profesional

#### Scenario: Ajuste que provoca un conflicto
- **WHEN** P1 tiene una reserva desde las 11:00, el intervalo es 10 minutos y un turno de las 10:00 se ajusta de 30 a 55 minutos
- **THEN** se rechaza el alta porque la duración efectiva no deja el intervalo exigido

### Requirement: Separación del sillón

El módulo SHALL rechazar un alta cuando una reserva ocupante del mismo sillón se solapa con el período solicitado o deja menos del intervalo explícito entre ambos turnos, aunque el profesional solicitado esté libre.

#### Scenario: Sillón ocupado y profesional distinto
- **WHEN** S1 está reservado para P1 de 10:00 a 10:30 y se solicita P2 en S1 de 10:15 a 10:45
- **THEN** se rechaza el alta por indisponibilidad del sillón

### Requirement: Intervalo mínimo inclusivo en ambos sentidos

El módulo SHALL aplicar el mismo intervalo explícito al profesional y al sillón, comparando reservas anteriores y posteriores. La separación exactamente igual al intervalo SHALL permitirse. El intervalo no tendrá valor implícito ni inicial de producción en este módulo.

#### Scenario: Borde exacto después de una reserva
- **WHEN** una reserva termina a las 10:30, el intervalo es 10 minutos y se solicita el mismo recurso desde las 10:40, sin otros conflictos
- **THEN** el módulo admite el alta

#### Scenario: Separación insuficiente después de una reserva
- **WHEN** una reserva termina a las 10:30, el intervalo es 10 minutos y se solicita el mismo recurso desde las 10:39
- **THEN** el módulo rechaza el alta

#### Scenario: Borde exacto antes de una reserva
- **WHEN** una reserva comienza a las 11:00, el intervalo es 10 minutos y el turno solicitado para el mismo recurso finaliza a las 10:50, sin otros conflictos
- **THEN** el módulo admite el alta

#### Scenario: Separación insuficiente antes de una reserva
- **WHEN** una reserva comienza a las 11:00, el intervalo es 10 minutos y el turno solicitado para el mismo recurso finaliza a las 10:51
- **THEN** el módulo rechaza el alta

### Requirement: Validación independiente del orden y de recursos ajenos

El módulo SHALL evaluar todas las reservas ocupantes recibidas sin depender de su orden. Las reservas que no comparten ni profesional ni sillón SHALL carecer de efecto sobre la disponibilidad del turno solicitado.

#### Scenario: Solapamiento permitido entre recursos distintos
- **WHEN** existe una reserva P1/S1 de 10:00 a 10:30 y se solicita P2/S2 en el mismo período, sin otras reservas
- **THEN** el módulo admite el alta

#### Scenario: Conflicto en una colección no ordenada
- **WHEN** se recibe una colección no ordenada con reservas ajenas y una reserva incompatible del profesional o sillón solicitado
- **THEN** el módulo rechaza el alta, independientemente de la posición de esa reserva

### Requirement: Resultado aislado y sin modificación de las reservas

El módulo SHALL recibir explícitamente la colección de reservas que ocupan recursos y el intervalo. Una respuesta exitosa SHALL devolver los datos del turno calculado; un rechazo SHALL distinguir entrada inválida de indisponibilidad. Ningún resultado SHALL modificar la colección recibida ni guardar una reserva.

#### Scenario: La creación no persiste ni modifica entradas
- **WHEN** se valida una solicitud contra una colección de reservas y se obtiene un resultado exitoso
- **THEN** se devuelve un turno calculado y la colección original permanece sin cambios

#### Scenario: El rechazo no altera la agenda recibida
- **WHEN** una solicitud se rechaza por conflicto
- **THEN** no se devuelve un turno exitoso y la colección de reservas permanece sin cambios
