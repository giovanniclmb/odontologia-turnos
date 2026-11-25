import type { Profesional } from "../api/types.js";

export type EstadoTurno = "PENDIENTE" | "CONFIRMADO" | "CANCELADO";

export interface TurnoActualizado {
  id: string;
  profesionalId: string;
  sillonId: string;
  prestacionId: string;
  inicio: string;
  fin: string;
  duracionMinutos: number;
  estado: EstadoTurno;
  version: string;
}

export interface TurnoAgenda extends TurnoActualizado {
  pacienteNombre: string;
  pacienteApellido: string;
}

export interface ResultadoEdicion {
  texto: string;
  destino: { inicio: string; fin: string; sillonId: string } | null;
}

/** Las referencias que el rol no puede ver llegan en null. */
export interface Bloqueo {
  id: string;
  inicio: string;
  fin: string;
  sillonId: string | null;
  profesionalId: string | null;
}

export interface Sillon {
  id: string;
  codigo: string;
  activo: boolean;
}

export interface Prestacion {
  id: string;
  codigo: string;
  duracionReferenciaMinutos: number | null;
}

export interface PacienteElegible {
  id: string;
  codigo: string;
  nombre: string;
  apellido: string;
}

export interface Catalogos {
  sillones: readonly Sillon[];
  profesionales: readonly Profesional[];
  prestaciones: readonly Prestacion[];
}

export type Rol = "recepcion" | "odontologo";
