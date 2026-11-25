/** Fechas y horas de la sede, con independencia de la zona del navegador. */
export type Vista = "dia" | "semana";

const ZONA_SEDE = "America/Argentina/Mendoza";
const FORMATO = new Intl.DateTimeFormat("en-US", {
  timeZone: ZONA_SEDE, hourCycle: "h23",
  year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit"
});
const MS_POR_DIA = 86_400_000;
// La agenda no opera fuera de este rango; el margen superior permite calcular el período siguiente.
const ANIO_MINIMO = 1970;
const ANIO_MAXIMO = 9997;

function partes(instante: Date): { fecha: string; hora: string } {
  const valores = new Map(FORMATO.formatToParts(instante).map((parte) => [parte.type, parte.value]));
  return {
    fecha: `${(valores.get("year") ?? "").padStart(4, "0")}-${valores.get("month")}-${valores.get("day")}`,
    hora: `${valores.get("hour")}:${valores.get("minute")}`
  };
}

export function fechaEnSede(instante: Date): string {
  return partes(instante).fecha;
}

export function horaEnSede(instante: Date): string {
  return partes(instante).hora;
}

/** Devuelve null si la fecha u hora no existen, no respetan AAAA-MM-DD y HH:MM o exceden el rango admitido. */
export function instanteDesdeSede(fecha: string, hora: string): Date | null {
  const anio = Number(fecha.slice(0, 4));
  return anio < ANIO_MINIMO || anio > ANIO_MAXIMO ? null : convertir(fecha, hora);
}

/** Sin límite de rango: la semana de una fecha admitida puede incluir días contiguos que lo exceden. */
function convertir(fecha: string, hora: string): Date | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha) || !/^\d{2}:\d{2}$/.test(hora)) return null;
  const comoUtc = Date.parse(`${fecha}T${hora}:00Z`);
  if (Number.isNaN(comoUtc)) return null;
  // El desplazamiento surge de la base de zonas: no se fija en el código.
  const vista = partes(new Date(comoUtc));
  const desplazamiento = Date.parse(`${vista.fecha}T${vista.hora}:00Z`) - comoUtc;
  const instante = new Date(comoUtc - desplazamiento);
  const resultado = partes(instante);
  return resultado.fecha === fecha && resultado.hora === hora ? instante : null;
}

export function sumarDias(fecha: string, dias: number): string {
  return new Date(Date.parse(`${fecha}T00:00:00Z`) + dias * MS_POR_DIA).toISOString().slice(0, 10);
}

/** La semana va de lunes a domingo. */
export function diasDelPeriodo(vista: Vista, fecha: string): string[] {
  if (vista === "dia") return [fecha];
  const desdeLunes = (new Date(`${fecha}T00:00:00Z`).getUTCDay() + 6) % 7;
  const lunes = sumarDias(fecha, -desdeLunes);
  return Array.from({ length: 7 }, (_, indice) => sumarDias(lunes, indice));
}

export function inicioDelDia(fecha: string): Date {
  const instante = convertir(fecha, "00:00");
  if (instante === null) throw new RangeError(`Fecha inválida: ${fecha}`);
  return instante;
}

/** Período semiabierto que cubre la vista. */
export function limitesDePeriodo(vista: Vista, fecha: string): { desde: string; hasta: string } {
  const dias = diasDelPeriodo(vista, fecha);
  const primero = dias[0] ?? fecha;
  return {
    desde: inicioDelDia(primero).toISOString(),
    hasta: inicioDelDia(sumarDias(primero, dias.length)).toISOString()
  };
}
