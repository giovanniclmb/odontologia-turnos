import { http, setToken, getToken } from "../api/client.js";
import type { LoginRequest, LoginResponse } from "../api/types.js";

export function currentUser(): LoginResponse | null {
  const raw = localStorage.getItem("user");
  if (!raw) return null;
  try {
    return JSON.parse(raw) as LoginResponse;
  } catch {
    return null;
  }
}

export function setCurrentUser(user: LoginResponse | null): void {
  if (user === null) {
    localStorage.removeItem("user");
  } else {
    localStorage.setItem("user", JSON.stringify(user));
  }
}

export async function login(nombreUsuario: string, contrasena: string): Promise<LoginResponse> {
  const response = await http.post<LoginResponse>("/auth/login", {
    nombreUsuario,
    contrasena
  } as LoginRequest);
  setToken(response.token);
  setCurrentUser(response);
  return response;
}

export async function logout(): Promise<void> {
  try {
    await http.post<void>("/auth/logout");
  } catch {
    // ignore
  }
  setToken(null);
  setCurrentUser(null);
}