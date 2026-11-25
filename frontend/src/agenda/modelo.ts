/** Compone lo que se muestra en cada día, sin tocar el DOM. */
import { fechaEnSede, horaEnSede, inicioDelDia, sumarDias } from "../tiempo/mendoza.js";
import type { Bloqueo, Catalogos, EstadoTurno, Rol, TurnoAgenda } from "./tipos.js";

export type EstadoElemento = "pendiente" | "confirmado" | "cancelado" | "bloqueo";

export interface Elemento {
  id: string;
  tipo: "turno" | "bloqueo";
  horario: string;
  duracion: string;
  titulo: string;
  detalle: string;
  estado: EstadoElemento;
  etiqueta: string;
}

export interface Dia {
  fecha: string;
  elementos: Elemento[];
  turnos: number;
  bloqueos: number;
}

export interface EntradaDias {
  dias: readonly string[];
  rol: Rol;
  catalogos: Catalogos;
  turnos: readonly TurnoAgenda[];
  bloqueos: readonly Bloqueo[];
}

const ESTADOS: Record<EstadoTurno, { estado: EstadoElemento; etiqueta: string }> = {
  PENDIENTE: { estado: "pendiente", etiqueta: "Pendiente" },
  CONFIRMADO: { estado: "confirmado", etiqueta: "Confirmado" },
  CANCELADO: { estado: "cancelado", etiqueta: "Cancelado" }
};

export function fechaCorta(fecha: string): string {
  const [, mes, dia] = fecha.split("-");
  return `${dia}/${mes}`;
}

function horario(inicio: Date, fin: Date): string {
  const fechaInicio = fechaEnSede(inicio);
  // Terminar exactamente a medianoche sigue perteneciendo al día de inicio.
  if (fin.getTime() <= inicioDelDia(sumarDias(fechaInicio, 1)).getTime()) {
    return `${horaEnSede(inicio)} – ${horaEnSede(fin)}`;
  }
  return `Desde ${fechaCorta(fechaInicio)} ${horaEnSede(inicio)} hasta ${fechaCorta(fechaEnSede(fin))} ${horaEnSede(fin)}`;
}

function codigo(catalogo: readonly { id: string; codigo: string }[], id: string): string {
  return catalogo.find((elemento) => elemento.id === id)?.codigo ?? "no disponible";
}

function detalleBloqueo(bloqueo: Bloqueo, rol: Rol, catalogos: Catalogos): string {
  if (rol === "odontologo") {
    if (bloqueo.sillonId !== null && bloqueo.profesionalId !== null) return "Afecta este sillón y su agenda profesional";
    return bloqueo.sillonId !== null ? "Afecta este sillón" : "Afecta su agenda profesional";
  }
  const recursos: string[] = [];
  if (bloqueo.sillonId !== null) recursos.push(`Sillón ${codigo(catalogos.sillones, bloqueo.sillonId)}`);
  if (bloqueo.profesionalId !== null) recursos.push(`Profesional ${codigo(catalogos.profesionales, bloqueo.profesionalId)}`);
  return recursos.join(" · ");
}

export function componerDias(entrada: EntradaDias): Dia[] {
  const { rol, catalogos } = entrada;
  const periodos = [
    ...entrada.turnos.map((turno) => ({
      inicio: new Date(turno.inicio),
      fin: new Date(turno.fin),
      elemento: {
        id: turno.id,
        tipo: "turno" as const,
        duracion: `${turno.duracionMinutos} min`,
        titulo: `${turno.pacienteNombre} ${turno.pacienteApellido}`,
        detalle: `Profesional ${codigo(catalogos.profesionales, turno.profesionalId)} · ${codigo(catalogos.prestaciones, turno.prestacionId)}`,
        ...ESTADOS[turno.estado]
      }
    })),
    ...entrada.bloqueos.map((bloqueo) => ({
      inicio: new Date(bloqueo.inicio),
      fin: new Date(bloqueo.fin),
      elemento: {
        id: bloqueo.id,
        tipo: "bloqueo" as const,
        duracion: `${Math.round((Date.parse(bloqueo.fin) - Date.parse(bloqueo.inicio)) / 60_000)} min`,
        titulo: "Bloqueo",
        detalle: detalleBloqueo(bloqueo, rol, catalogos),
        estado: "bloqueo" as const,
        etiqueta: "No disponible"
      }
    }))
  ].sort((a, b) => a.inicio.getTime() - b.inicio.getTime());

  return entrada.dias.map((fecha) => {
    const desde = inicioDelDia(fecha).getTime();
    const hasta = inicioDelDia(sumarDias(fecha, 1)).getTime();
    const elementos = periodos
      .filter((periodo) => periodo.inicio.getTime() < hasta && periodo.fin.getTime() > desde)
      .map((periodo) => ({ ...periodo.elemento, horario: horario(periodo.inicio, periodo.fin) }));
    return {
      fecha,
      elementos,
      turnos: elementos.filter((elemento) => elemento.tipo === "turno").length,
      bloqueos: elementos.filter((elemento) => elemento.tipo === "bloqueo").length
    };
  });
}
