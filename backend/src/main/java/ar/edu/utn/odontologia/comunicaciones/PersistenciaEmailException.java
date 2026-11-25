package ar.edu.utn.odontologia.comunicaciones;

/**
 * Excepcion lanzada cuando el adaptador de email no puede persistir el registro de envio.
 * No se contacta al proveedor externo ante este fallo.
 */
public class PersistenciaEmailException extends RuntimeException {
    public PersistenciaEmailException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}