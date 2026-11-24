const BASE_URL = ((): string => {
  const override = (window as { __API_BASE_URL__?: string }).__API_BASE_URL__;
  if (override) return override.replace(/\/$/, "");
  if (window.location.port === "5173") {
    return "http://localhost:8080";
  }
  return "";
})();

export function getToken(): string | null {
  return localStorage.getItem("token");
}

export function setToken(token: string | null): void {
  if (token === null) {
    localStorage.removeItem("token");
  } else {
    localStorage.setItem("token", token);
  }
}

export class ApiError extends Error {
  codigo: string;
  status: number;
  constructor(status: number, codigo: string, mensaje: string) {
    super(mensaje);
    this.status = status;
    this.codigo = codigo;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers ?? {});
  headers.set("Accept", "application/json");
  if (init.body !== undefined && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const token = getToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(`${BASE_URL}${path}`, { ...init, headers });
  if (!response.ok) {
    let codigo = "error";
    let mensaje = response.statusText;
    try {
      const body = (await response.json()) as { codigo?: string; mensaje?: string };
      if (body.codigo) codigo = body.codigo;
      if (body.mensaje) mensaje = body.mensaje;
    } catch {
      // ignore
    }
    throw new ApiError(response.status, codigo, mensaje);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const http = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PUT", body: body === undefined ? undefined : JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" })
};