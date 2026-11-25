package ar.edu.utn.odontologia.espera;

import java.time.Instant;
import java.util.UUID;

public record InscripcionEspera(UUID id, PreferenciasEspera preferencias, VigenciaEspera vigencia, Instant bajaEn) {}
