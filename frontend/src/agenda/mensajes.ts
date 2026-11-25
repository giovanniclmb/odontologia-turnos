import type { Rol } from "./tipos.js";
import { RespuestaInvalidaError } from "./validadores.js";

function codigoDe(error: unknown): string | null {
  if (typeof error !== "object" || error === null || !("codigo" in error)) return null;
  return typeof error.codigo === "string" ? error.codigo : null;
}

export function mensajeSinIntervalo(rol: Rol): string {
  const accion = rol === "recepcion" ? "Configúrelo en Configuración." : "Solicítelo a recepción.";
  return `Falta configurar el intervalo entre turnos. ${accion}`;
}

/** Traduce errores a mensajes para la persona usuaria, sin detalles internos. */
export function mensajeDeError(error: unknown, rol: Rol): string {
  if (error instanceof RespuestaInvalidaError) return "El servidor envió una respuesta inesperada.";
  if (error instanceof TypeError) return "No se pudo conectar con el servidor.";
  switch (codigoDe(error)) {
    case "horario_no_disponible":
      return "El horario solicitado no está disponible. Revise profesional, sillón u horario.";
    case "bloqueo_afecta_turno":
      return "El bloqueo afecta a un turno existente. No se creó.";
    case "intervalo_sin_configurar":
      return mensajeSinIntervalo(rol);
    case "autorizacion_denegada":
      return "No tiene permiso para esta operación.";
    case "no_autenticado":
      return "Su sesión venció. Cierre sesión y vuelva a ingresar.";
    case "solicitud_invalida":
      return error instanceof Error ? error.message : "Solicitud inválida.";
    default:
      return "No se pudo completar la operación.";
  }
}
