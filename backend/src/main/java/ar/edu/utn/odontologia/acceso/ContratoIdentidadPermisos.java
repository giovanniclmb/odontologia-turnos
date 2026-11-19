package ar.edu.utn.odontologia.acceso;

/**
 * Contrato reutilizable para propagar identidad y permisos a nivel de servicio,
 * sin transporte HTTP ni endpoints.
 */
public interface ContratoIdentidadPermisos {
    Identidad identidadActual();

    boolean tienePermiso(Permiso permiso);

    void exigirPermiso(Permiso permiso);
}
