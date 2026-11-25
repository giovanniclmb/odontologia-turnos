/** Construye nodos con texto plano: ningún dato se interpreta como HTML. */
export type Documento = Pick<Document, "createElement">;

export interface OpcionesNodo {
  clase?: string;
  texto?: string;
  atributos?: Readonly<Record<string, string>>;
}

export function nodo<K extends keyof HTMLElementTagNameMap>(
  doc: Documento, etiqueta: K, opciones: OpcionesNodo = {}, hijos: readonly Node[] = []
): HTMLElementTagNameMap[K] {
  const elemento = doc.createElement(etiqueta);
  if (opciones.clase !== undefined) elemento.className = opciones.clase;
  if (opciones.texto !== undefined) elemento.textContent = opciones.texto;
  for (const [nombre, valor] of Object.entries(opciones.atributos ?? {})) elemento.setAttribute(nombre, valor);
  if (hijos.length > 0) elemento.append(...hijos);
  return elemento;
}

/** Primer descendiente que cumple la condición, sin depender de selectores globales. */
export function buscarElemento(raiz: Element, condicion: (elemento: Element) => boolean): HTMLElement | null {
  const pendientes: Element[] = Array.from(raiz.children);
  for (let indice = 0; indice < pendientes.length; indice++) {
    const actual = pendientes[indice];
    if (actual === undefined) continue;
    if (condicion(actual) && esEnfocable(actual)) return actual;
    pendientes.push(...Array.from(actual.children));
  }
  return null;
}

function esEnfocable(elemento: Element): elemento is HTMLElement {
  return "focus" in elemento;
}

export function boton(doc: Documento, texto: string, clase: string, alPulsar: () => void): HTMLButtonElement {
  const elemento = nodo(doc, "button", { texto, clase: `btn ${clase}` });
  elemento.type = "button";
  elemento.addEventListener("click", alPulsar);
  return elemento;
}
