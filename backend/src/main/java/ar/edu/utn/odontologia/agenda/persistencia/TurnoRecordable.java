package ar.edu.utn.odontologia.agenda.persistencia;

import java.time.Instant;
import java.util.UUID;

/** Datos para decidir si un horario se recuerda. {@code horarioFijadoEn} es nulo en turnos anteriores a C-20. */
public record TurnoRecordable(UUID turnoId, UUID pacienteId, Instant inicio, Instant horarioFijadoEn) {}
