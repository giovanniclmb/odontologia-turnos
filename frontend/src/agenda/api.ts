/** Único punto de la agenda que conoce las rutas HTTP. */
import { http } from "../api/client.js";
import type { ApiAgenda } from "./contexto.js";
import { leerBloqueos, leerIntervalo, leerPacientes, leerPrestaciones, leerRecursos, leerTurno, leerTurnos, RespuestaInvalidaError } from "./validadores.js";
import type { PacienteElegible } from "./tipos.js";

const consulta = (parametros: Record<string, string>) => new URLSearchParams(parametros).toString();

async function buscarPacientes(texto: string): Promise<PacienteElegible[]> {
  // La API combina código y texto con "y": se consultan por separado y se unen.
  const [porCodigo, porNombre] = await Promise.all([
    http.get<unknown>(`/pacientes?${consulta({ codigo: texto })}`),
    http.get<unknown>(`/pacientes?${consulta({ texto })}`)
  ]);
  const unicos = new Map([...leerPacientes(porCodigo), ...leerPacientes(porNombre)].map((p) => [p.id, p]));
  return [...unicos.values()];
}

export const apiAgenda: ApiAgenda = {
  sillones: async () => leerRecursos(await http.get<unknown>("/recursos/sillones")),
  profesionales: async () => leerRecursos(await http.get<unknown>("/recursos/profesionales")),
  prestaciones: async () => leerPrestaciones(await http.get<unknown>("/recursos/prestaciones")),
  intervalo: async () => leerIntervalo(await http.get<unknown>("/sede/intervalo")),
  establecerIntervalo: async (minutos) => leerIntervalo(await http.put<unknown>("/sede/intervalo", { minutos })),
  turnos: async (sillonId, desde, hasta) => leerTurnos(
    await http.get<unknown>(`/agenda/sillones/${encodeURIComponent(sillonId)}/turnos?${consulta({ desde, hasta })}`)),
  pendientes: async () => leerTurnos(await http.get<unknown>("/agenda/turnos/pendientes")),
  confirmarTurno: async (id, versionEsperada) => {
    const turno = leerTurno(await http.post<unknown>(`/agenda/turnos/${encodeURIComponent(id)}/confirmacion`, { versionEsperada }));
    if (turno.estado !== "CONFIRMADO" || turno.id !== id) throw new RespuestaInvalidaError("confirmación");
    return turno;
  },
  bloqueos: async (sillonId, desde, hasta) => leerBloqueos(
    await http.get<unknown>(`/agenda/bloqueos?${consulta({ sillonId, desde, hasta })}`)),
  crearTurno: async (solicitud) => {
    await http.post<unknown>("/agenda/turnos", solicitud);
  },
  reprogramarTurno: async (id, solicitud) => leerTurno(
    await http.post<unknown>(`/agenda/turnos/${encodeURIComponent(id)}/reprogramacion`, solicitud)),
  cancelarTurno: async (id, versionEsperada) => leerTurno(
    await http.post<unknown>(`/agenda/turnos/${encodeURIComponent(id)}/cancelacion`, { versionEsperada })),
  crearBloqueo: async (solicitud) => {
    await http.post<unknown>("/agenda/bloqueos", solicitud);
  },
  buscarPacientes
};
