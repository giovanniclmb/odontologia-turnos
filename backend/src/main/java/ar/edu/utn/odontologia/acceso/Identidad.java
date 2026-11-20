package ar.edu.utn.odontologia.acceso;

import java.util.UUID;

public record Identidad(UUID usuarioId, String nombreUsuario, Rol rol, UUID profesionalRef) {
}
