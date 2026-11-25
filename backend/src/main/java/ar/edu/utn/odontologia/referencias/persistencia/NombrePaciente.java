package ar.edu.utn.odontologia.referencias.persistencia;

import java.util.UUID;

public record NombrePaciente(UUID id, String nombre, String apellido) {}
