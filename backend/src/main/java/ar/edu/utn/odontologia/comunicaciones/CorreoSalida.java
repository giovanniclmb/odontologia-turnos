package ar.edu.utn.odontologia.comunicaciones;

/**
 * Puerto neutro de salida de correo. El negocio depende solo de este contrato;
 * el proveedor concreto (Resend) y el fake de pruebas son implementaciones.
 */
public interface CorreoSalida {
    ResultadoEnvio enviar(SolicitudEnvio solicitud);
}