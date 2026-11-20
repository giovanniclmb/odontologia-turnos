package ar.edu.utn.odontologia.acceso;

import java.util.UUID;

/**
 * Contrato (sin implementacion efectiva en C-03) para vincular un usuario interno
 * con una referencia opaca de profesional. La implementacion efectiva se realiza en C-05.
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
