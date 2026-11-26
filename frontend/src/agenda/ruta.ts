/** El estado de la vista vive en el hash de la URL. */
import { instanteDesdeSede, type Vista } from "../tiempo/mendoza.js";
import type { Rol } from "./tipos.js";

export type SeccionAgenda = "agenda" | "agenda/nuevo" | "agenda/bloqueo" | "agenda/confirmaciones";

export interface RutaAgenda {
  seccion: SeccionAgenda;
  vista: Vista;
  fecha: string;
  sillonId: string | null;
}

export type Ruta = RutaAgenda | { seccion: "pacientes" } | { seccion: "configuracion" };

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function seccionDe(nombre: string, rol: Rol): Ruta["seccion"] {
  const recepcion = rol === "recepcion";
  switch (nombre) {
    case "agenda":
    case "agenda/nuevo":
      return nombre;
    // La restricción es de presentación: el backend vuelve a autorizar cada operación.
    case "agenda/bloqueo":
    case "agenda/confirmaciones":
      return recepcion ? nombre : "agenda";
    case "configuracion":
      return recepcion ? nombre : "agenda";
    default:
      return "pacientes";
  }
}

export function leerRuta(hash: string, rol: Rol, hoy: string): Ruta {
  const [nombre = "", consulta = ""] = hash.replace(/^#/, "").split("?", 2);
  const seccion = seccionDe(nombre, rol);
  if (seccion === "pacientes" || seccion === "configuracion") return { seccion };
  const parametros = new URLSearchParams(consulta);
  const fecha = parametros.get("fecha") ?? "";
  const sillon = parametros.get("sillon") ?? "";
  return {
    seccion,
    vista: parametros.get("vista") === "semana" ? "semana" : "dia",
    fecha: instanteDesdeSede(fecha, "00:00") === null ? hoy : fecha,
    sillonId: UUID.test(sillon) ? sillon : null
  };
}

export function escribirRuta(ruta: Ruta): string {
  if (ruta.seccion === "pacientes" || ruta.seccion === "configuracion") return `#${ruta.seccion}`;
  const parametros = new URLSearchParams({ vista: ruta.vista, fecha: ruta.fecha });
  if (ruta.sillonId !== null) parametros.set("sillon", ruta.sillonId);
  return `#${ruta.seccion}?${parametros.toString()}`;
}
