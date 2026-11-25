export interface LoginRequest {
  nombreUsuario: string;
  contrasena: string;
}

export interface LoginResponse {
  token: string;
  usuarioId: string;
  nombreUsuario: string;
  rol: "recepcion" | "odontologo";
  profesionalRef: string | null;
}

export interface Paciente {
  id: string;
  codigo: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
  profesionalesAsignados: string[];
}

export interface CrearPacienteRequest {
  codigo: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
}

export interface ActualizarPacienteRequest {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
}

export interface Profesional {
  id: string;
  codigo: string;
  activo: boolean;
}

export interface Nota {
  id: string;
  autorId: string;
  creadaEn: string;
  texto: string;
}

export interface Ficha {
  pacienteId: string;
  antecedentes: string | null;
  notas: Nota[];
}

export interface ApiError {
  codigo: string;
  mensaje: string;
}