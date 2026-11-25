package ar.edu.utn.odontologia.agenda.persistencia;

import java.time.Instant;
import java.util.UUID;

public record TokenCancelacionLeido(UUID id, UUID turnoId, String hash, long version, boolean consumido,
                                    Instant creadoEn) {}