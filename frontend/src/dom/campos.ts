/** Campos de formulario con su etiqueta asociada. */
import { nodo, type Documento } from "./nodo.js";

export interface Campo<T extends HTMLElement> {
  contenedor: HTMLDivElement;
  rotulo: HTMLLabelElement;
  control: T;
  ayuda: HTMLSpanElement;
}

function envolver<T extends HTMLElement>(doc: Documento, id: string, etiqueta: string, control: T, ayudaInicial: string): Campo<T> {
  control.id = id;
  const rotulo = nodo(doc, "label", { texto: etiqueta, atributos: { for: id } });
  const ayuda = nodo(doc, "span", { clase: "hint", texto: ayudaInicial, atributos: { id: `${id}-ayuda` } });
  control.setAttribute("aria-describedby", `${id}-ayuda`);
  return { contenedor: nodo(doc, "div", { clase: "field" }, [rotulo, control, ayuda]), rotulo, control, ayuda };
}

export function entrada(doc: Documento, id: string, etiqueta: string, tipo: string, valor = "", ayuda = ""): Campo<HTMLInputElement> {
  const control = nodo(doc, "input", { clase: "input" });
  control.type = tipo;
  control.value = valor;
  return envolver(doc, id, etiqueta, control, ayuda);
}

export interface Opcion {
  valor: string;
  texto: string;
}

export function cargarOpciones(doc: Documento, control: HTMLSelectElement, opciones: readonly Opcion[], elegido = ""): void {
  control.replaceChildren(...opciones.map(({ valor, texto }) => {
    const opcion = nodo(doc, "option", { texto });
    opcion.value = valor;
    return opcion;
  }));
  control.value = opciones.some((opcion) => opcion.valor === elegido) ? elegido : opciones[0]?.valor ?? "";
}

export function seleccion(doc: Documento, id: string, etiqueta: string, opciones: readonly Opcion[], elegido = "", ayuda = ""): Campo<HTMLSelectElement> {
  const control = nodo(doc, "select", { clase: "input" });
  cargarOpciones(doc, control, opciones, elegido);
  return envolver(doc, id, etiqueta, control, ayuda);
}
