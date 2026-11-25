/** Validación de las respuestas de la API en el límite: nada entra sin comprobar su forma. */
import type { Profesional } from "../api/types.js";
import type { Bloqueo, EstadoTurno, PacienteElegible, Prestacion, Sillon, TurnoAgenda } from "./tipos.js";

export class RespuestaInvalidaError extends Error {
  constructor(detalle: string) {
    super(`Respuesta inválida del servidor: ${detalle}`);
    this.name = "RespuestaInvalidaError";
  }
}

type Objeto = Record<string, unknown>;

function esObjeto(valor: unknown): valor is Objeto {
  return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function objeto(valor: unknown): Objeto {
  if (!esObjeto(valor)) throw new RespuestaInvalidaError("se esperaba un objeto");
  return valor;
}

function lista<T>(valor: unknown, leer: (elemento: Objeto) => T): T[] {
  if (!Array.isArray(valor)) throw new RespuestaInvalidaError("se esperaba una lista");
  return valor.map((elemento: unknown) => leer(objeto(elemento)));
}

function texto(origen: Objeto, campo: string): string {
  const valor = origen[campo];
  if (typeof valor !== "string") throw new RespuestaInvalidaError(campo);
  return valor;
}

function textoONulo(origen: Objeto, campo: string): string | null {
  return origen[campo] === null ? null : texto(origen, campo);
}

function instante(origen: Objeto, campo: string): string {
  const valor = texto(origen, campo);
  if (Number.isNaN(Date.parse(valor))) throw new RespuestaInvalidaError(campo);
  return valor;
}

function booleano(origen: Objeto, campo: string): boolean {
  const valor = origen[campo];
  if (typeof valor !== "boolean") throw new RespuestaInvalidaError(campo);
  return valor;
}

function enteroPositivo(origen: Objeto, campo: string): number {
  const valor = origen[campo];
  if (typeof valor !== "number" || !Number.isInteger(valor) || valor <= 0) throw new RespuestaInvalidaError(campo);
  return valor;
}

function estado(origen: Objeto): EstadoTurno {
  const valor = origen["estado"];
  if (valor === "PENDIENTE" || valor === "CONFIRMADO" || valor === "CANCELADO") return valor;
  throw new RespuestaInvalidaError("estado");
}

export function leerTurnos(valor: unknown): TurnoAgenda[] {
  return lista(valor, (turno) => ({
    id: texto(turno, "id"),
    pacienteNombre: texto(turno, "pacienteNombre"),
    pacienteApellido: texto(turno, "pacienteApellido"),
    profesionalId: texto(turno, "profesionalId"),
    sillonId: texto(turno, "sillonId"),
    prestacionId: texto(turno, "prestacionId"),
    inicio: instante(turno, "inicio"),
    fin: instante(turno, "fin"),
    duracionMinutos: enteroPositivo(turno, "duracionMinutos"),
    estado: estado(turno),
    version: texto(turno, "version")
  }));
}

export function leerBloqueos(valor: unknown): Bloqueo[] {
  return lista(valor, (bloqueo) => ({
    id: texto(bloqueo, "id"),
    inicio: instante(bloqueo, "inicio"),
    fin: instante(bloqueo, "fin"),
    sillonId: textoONulo(bloqueo, "sillonId"),
    profesionalId: textoONulo(bloqueo, "profesionalId")
  }));
}

export function leerRecursos(valor: unknown): (Sillon & Profesional)[] {
  return lista(valor, (recurso) => ({
    id: texto(recurso, "id"),
    codigo: texto(recurso, "codigo"),
    activo: booleano(recurso, "activo")
  }));
}

export function leerPrestaciones(valor: unknown): Prestacion[] {
  return lista(valor, (prestacion) => ({
    id: texto(prestacion, "id"),
    codigo: texto(prestacion, "codigo"),
    duracionReferenciaMinutos: prestacion["duracionReferenciaMinutos"] === null
      ? null : enteroPositivo(prestacion, "duracionReferenciaMinutos")
  }));
}

export function leerPacientes(valor: unknown): PacienteElegible[] {
  return lista(valor, (paciente) => ({
    id: texto(paciente, "id"),
    codigo: texto(paciente, "codigo"),
    nombre: texto(paciente, "nombre"),
    apellido: texto(paciente, "apellido")
  }));
}

/** null representa la sede sin configurar. */
export function leerIntervalo(valor: unknown): number | null {
  const respuesta = objeto(valor);
  return respuesta["minutos"] === null ? null : enteroPositivo(respuesta, "minutos");
}
