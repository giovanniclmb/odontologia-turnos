package ar.edu.utn.odontologia.agenda;

import java.time.Instant;
import java.util.UUID;

/** Un horario de turno que corresponde recordar. */
public record TurnoPorRecordar(UUID turnoId, Instant inicio) {}
