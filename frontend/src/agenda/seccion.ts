/** Monta la vista de cada ruta y descarta lo que llegue de navegaciones anteriores. */
import { montarConfiguracion } from "../configuracion/vista.js";
import { buscarElemento } from "../dom/nodo.js";
import { alerta } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { montarAltaBloqueo } from "./formulario-bloqueo.js";
import { montarAltaTurno } from "./formulario-turno.js";
import { mensajeDeError } from "./mensajes.js";
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
  let actual: RutaDeSeccion | null = null;
  const enCurso = new Set<string>();

  async function mostrar(ruta: RutaDeSeccion): Promise<void> {
    const propia = ++generacion;
    const cambiaSeccion = actual?.seccion !== ruta.seccion;
    actual = ruta;
    const { doc, raiz, api, usuario, navegar, reemplazar } = dependencias;
    const vigente = () => propia === generacion;
    await montar({
      doc, raiz, api, usuario, navegar, reemplazar, vigente, enviar,
      hoy: dependencias.hoy(),
      foco: cambiaSeccion ? null : dependencias.elementoActivo()
    }, ruta);
    if (cambiaSeccion && vigente()) buscarElemento(raiz, (elemento) => elemento.tagName === "H1")?.focus();
  }

  /** El resultado de un envío cuya vista ya no está se refleja en la que se esté mostrando. */
  function enviar(clave: string, operacion: () => Promise<void>): Promise<void> | null {
    if (enCurso.has(clave)) return null;
    enCurso.add(clave);
    const origen = generacion;
    return operacion().then(
      () => {
        enCurso.delete(clave);
        if (origen !== generacion && actual?.seccion === "agenda") void mostrar(actual);
      },
      (error: unknown) => {
        enCurso.delete(clave);
        if (origen === generacion) throw error;
        if (actual !== null) informarEnvioFallido(error);
      }
    );
  }

  function informarEnvioFallido(error: unknown): void {
    const { doc, raiz, usuario } = dependencias;
    const texto = `El envío anterior no se completó. ${mensajeDeError(error, usuario.rol)}`;
    raiz.replaceChildren(alerta(doc, texto), ...Array.from(raiz.children));
  }

  return {
    mostrar,
    cerrar(): void {
      generacion++;
      actual = null;
      dependencias.raiz.replaceChildren();
    }
  };
}
