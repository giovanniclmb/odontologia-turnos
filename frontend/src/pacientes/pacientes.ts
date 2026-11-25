import { http } from "../api/client.js";
import type { Paciente, CrearPacienteRequest, ActualizarPacienteRequest, Profesional } from "../api/types.js";

export async function buscarPacientes(codigo?: string, texto?: string): Promise<Paciente[]> {
  const params = new URLSearchParams();
  if (codigo) params.set("codigo", codigo);
  if (texto) params.set("texto", texto);
  const query = params.toString();
  return http.get<Paciente[]>(`/pacientes${query ? "?" + query : ""}`);
}

export async function crearPaciente(data: CrearPacienteRequest): Promise<Paciente> {
  return http.post<Paciente>("/pacientes", data);
}

export async function consultarPaciente(id: string): Promise<Paciente> {
  return http.get<Paciente>(`/pacientes/${id}`);
}

export async function actualizarPaciente(id: string, data: ActualizarPacienteRequest): Promise<Paciente> {
  return http.put<Paciente>(`/pacientes/${id}`, data);
}

export async function asignarProfesional(pacienteId: string, profesionalId: string): Promise<void> {
  await http.post(`/pacientes/${pacienteId}/profesionales`, { profesionalId });
}

export async function desasignarProfesional(pacienteId: string, profesionalId: string): Promise<void> {
  await http.delete(`/pacientes/${pacienteId}/profesionales/${profesionalId}`);
}

export async function listarProfesionales(): Promise<Profesional[]> {
  return http.get<Profesional[]>("/recursos/profesionales");
}