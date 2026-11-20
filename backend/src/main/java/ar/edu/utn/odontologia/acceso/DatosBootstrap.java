package ar.edu.utn.odontologia.acceso;

import java.util.UUID;

public record DatosBootstrap(String nombreUsuario, String contrasena, Rol rol, DatosPersonales datosPersonales, UUID profesionalRef) {
}
