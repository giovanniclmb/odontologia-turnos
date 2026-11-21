package ar.edu.utn.odontologia.acceso;

import java.util.UUID;

/**
 * Contrato de acceso y datos para vincular un usuario interno con un profesional.
 */
public final class ContratoVinculacionUsuarioProfesional {
    private ContratoVinculacionUsuarioProfesional() {
    }

    public static final String PERMISO_REQUERIDO = Permiso.USUARIO_CREAR_ASIGNAR.codigo();

    public record Vinculacion(UUID usuarioId, UUID profesionalRef) {
    }

    public static void validarAsignacion(Identidad solicitante, ServicioAutorizacion autorizacion) {
        autorizacion.exigir(solicitante, Permiso.USUARIO_CREAR_ASIGNAR);
    }
}
