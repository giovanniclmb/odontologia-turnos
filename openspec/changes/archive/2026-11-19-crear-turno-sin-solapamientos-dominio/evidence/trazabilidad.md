# Trazabilidad de C-01

Spec: [disponibilidad-de-turnos](../specs/disponibilidad-de-turnos/spec.md). Pruebas: `backend/src/test/java/ar/edu/utn/odontologia/agenda/AgendaTest.java`.

| Escenario del spec | Prueba |
| --- | --- |
| Recursos y referencias presentes | creaConDuracionDePrestacionYConservaReferencias |
| Falta un recurso obligatorio | rechazaEntradaInvalida (profesional/sillón nulos, vacíos o blancos) |
| Falta una referencia administrativa necesaria | rechazaEntradaInvalida (paciente/prestación) |
| Se utiliza la duración de la prestación | creaConDuracionDePrestacionYConservaReferencias |
| Se respeta el ajuste explícito | usaAjusteExplicitoDeDuracion |
| Duración efectiva inválida | rechazaEntradaInvalida (referencia nula/cero/negativa y ajuste no positivo) |
| Profesional ocupado y sillón distinto | rechazaSolapamiento (PROFESIONAL) |
| Ajuste que provoca un conflicto | ajusteDeDuracionPuedeGenerarConflicto |
| Sillón ocupado y profesional distinto | rechazaSolapamiento (SILLON) |
| Borde exacto después de una reserva | respetaIntervaloInclusivoAntesYDespues (fin 09:50, inicio solicitado 10:00) |
| Separación insuficiente después de una reserva | respetaIntervaloInclusivoAntesYDespues (fin 09:51) |
| Borde exacto antes de una reserva | respetaIntervaloInclusivoAntesYDespues (inicio 10:40, fin solicitado 10:30) |
| Separación insuficiente antes de una reserva | respetaIntervaloInclusivoAntesYDespues (inicio 10:39) |
| Solapamiento permitido entre recursos distintos | permiteMismaHoraConRecursosDistintos |
| Conflicto en una colección no ordenada | encuentraConflictoEnColeccionNoOrdenada |
| La creación no persiste ni modifica entradas | noModificaReservasAlAceptarNiRechazar (éxito) |
| El rechazo no altera la agenda recibida | noModificaReservasAlAceptarNiRechazar (rechazo) |

Casos adicionales: períodos iguales, contención total, recurso AMBOS, intervalo de 15 minutos sin constante oculta, desbordamiento temporal y reserva malformada ajena posterior a un conflicto. Las pruebas no usan mocks ni base de datos.
