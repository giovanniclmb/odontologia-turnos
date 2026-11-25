/** Monta la vista de cada ruta y descarta lo que llegue de navegaciones anteriores. */
import { montarConfiguracion } from "../configuracion/vista.js";
import { buscarElemento } from "../dom/nodo.js";
import type { Contexto } from "./contexto.js";
import { montarAltaBloqueo } from "./formulario-bloqueo.js";
import { montarAltaTurno } from "./formulario-turno.js";
import type { Ruta } from "./ruta.js";
import { montarAgenda } from "./vista-agenda.js";

export type RutaDeSeccion = Exclude<Ruta, { seccion: "pacientes" }>;

export interface Dependencias extends Pick<Contexto, "doc" | "raiz" | "api" | "usuario" | "navegar" | "reemplazar"> {
  hoy(): string;
  elementoActivo(): string | null;
}

function montar(contexto: Contexto, ruta: RutaDeSeccion): Promise<void> {
  switch (ruta.seccion) {
    case "agenda":
      return montarAgenda(contexto, ruta);
    case "agenda/nuevo":
      return montarAltaTurno(contexto, ruta);
    case "agenda/bloqueo":
      return montarAltaBloqueo(contexto, ruta);
    case "configuracion":
      return montarConfiguracion(contexto);
    default:
      return rutaNoContemplada(ruta);
  }
}

function rutaNoContemplada(ruta: never): never {
  throw new Error(`Ruta no contemplada: ${JSON.stringify(ruta)}`);
}

export function crearSeccion(dependencias: Dependencias) {
  let generacion = 0;
  let anterior: RutaDeSeccion["seccion"] | null = null;
  return {
    async mostrar(ruta: RutaDeSeccion): Promise<void> {
      const propia = ++generacion;
      const cambiaSeccion = anterior !== ruta.seccion;
      anterior = ruta.seccion;
      const { doc, raiz, api, usuario, navegar, reemplazar } = dependencias;
      const vigente = () => propia === generacion;
      await montar({
        doc, raiz, api, usuario, navegar, reemplazar, vigente,
        hoy: dependencias.hoy(),
        foco: cambiaSeccion ? null : dependencias.elementoActivo()
      }, ruta);
      if (cambiaSeccion && vigente()) buscarElemento(raiz, (elemento) => elemento.tagName === "H1")?.focus();
    },
    cerrar(): void {
      generacion++;
      anterior = null;
      dependencias.raiz.replaceChildren();
    }
  };
}
