package ar.edu.utn.odontologia.agenda.bloqueos;

import java.util.UUID;

public record Bloqueo(UUID id, RecursosBloqueo recursos, PeriodoBloqueo periodo) {}
