import { http } from "../api/client.js";
import type { Ficha } from "../api/types.js";

export async function consultarFicha(pacienteId: string): Promise<Ficha> {
  return http.get<Ficha>(`/pacientes/${pacienteId}/ficha`);
}

export async function actualizarAntecedentes(pacienteId: string, antecedentes: string): Promise<Ficha> {
  return http.put<Ficha>(`/pacientes/${pacienteId}/ficha/antecedentes`, { antecedentes });
}

export async function agregarNota(pacienteId: string, texto: string): Promise<void> {
  await http.post(`/pacientes/${pacienteId}/ficha/notas`, { texto });
}

export async function editarNota(pacienteId: string, notaId: string, texto: string): Promise<void> {
  await http.put(`/pacientes/${pacienteId}/ficha/notas/${notaId}`, { texto });
}