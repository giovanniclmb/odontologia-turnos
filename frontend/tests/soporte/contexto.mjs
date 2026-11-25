import { documentoFalso, Nodo } from "./dom-falso.mjs";

export const SILLON = "9d6f71bb-4e97-4d16-96f9-184168cb5c0f";
export const OTRO_SILLON = "1d6f71bb-4e97-4d16-96f9-184168cb5c0f";

export const catalogos = () => ({
  sillones: [{ id: SILLON, codigo: "SIL-01", activo: true }, { id: OTRO_SILLON, codigo: "SIL-02", activo: true }],
  profesionales: [{ id: "p1", codigo: "PRO-01", activo: true }, { id: "p2", codigo: "PRO-02", activo: true }],
  prestaciones: [{ id: "r1", codigo: "CONTROL", duracionReferenciaMinutos: 30 }, { id: "r2", codigo: "TRATAMIENTO", duracionReferenciaMinutos: 60 }]
});

/** API falsa en el límite de red: registra las llamadas y permite sustituir cada respuesta. */
export function apiFalsa(sustituciones = {}) {
  const datos = catalogos();
  const llamadas = [];
  const base = {
    sillones: async () => datos.sillones,
    profesionales: async () => datos.profesionales,
    prestaciones: async () => datos.prestaciones,
    intervalo: async () => 10,
    establecerIntervalo: async (minutos) => minutos,
    turnos: async () => [],
    bloqueos: async () => [],
    crearTurno: async () => {},
    crearBloqueo: async () => {},
    buscarPacientes: async () => [{ id: "c1", codigo: "P-001", nombre: "Ana", apellido: "Ficticia" }]
  };
  const api = {};
  for (const [nombre, funcion] of Object.entries({ ...base, ...sustituciones })) {
    api[nombre] = (...argumentos) => { llamadas.push([nombre, ...argumentos]); return funcion(...argumentos); };
  }
  return { api, llamadas, de: (nombre) => llamadas.filter(([llamada]) => llamada === nombre).map(([, ...resto]) => resto) };
}

export function contexto({ api = apiFalsa().api, rol = "recepcion", profesionalRef = null } = {}) {
  const navegaciones = [];
  const reemplazos = [];
  const enCurso = new Set();
  return {
    doc: documentoFalso(),
    raiz: new Nodo("main"),
    api,
    usuario: { rol, profesionalRef },
    hoy: "2026-11-25",
    foco: null,
    navegar: (ruta) => navegaciones.push(ruta),
    reemplazar: (ruta) => reemplazos.push(ruta),
    vigente: () => true,
    enviar: (clave, operacion) => {
      if (enCurso.has(clave)) return null;
      enCurso.add(clave);
      return operacion().finally(() => enCurso.delete(clave));
    },
    navegaciones,
    reemplazos
  };
}

export const rechazo = (codigo, status = 409, mensaje = "detalle") => async () => {
  throw Object.assign(new Error(mensaje), { codigo, status });
};

export function pendiente() {
  let resolver;
  const promesa = new Promise((resolve) => { resolver = resolve; });
  return { promesa, resolver };
}
