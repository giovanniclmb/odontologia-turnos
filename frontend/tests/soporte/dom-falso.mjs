/** DOM mínimo para probar las vistas sin dependencias. Asignar innerHTML falla a propósito. */
export class Nodo {
  constructor(tag) {
    Object.assign(this, { tag, children: [], attributes: {}, listeners: {}, className: "", value: "", disabled: false, readOnly: false, id: "", type: "", enfocado: false });
    this.propio = "";
  }
  get tagName() { return this.tag.toUpperCase(); }
  set textContent(valor) { this.children = []; this.propio = String(valor); }
  get textContent() { return this.propio + this.children.map((hijo) => hijo.textContent).join(" "); }
  set innerHTML(_valor) { throw new Error("innerHTML no está permitido"); }
  setAttribute(nombre, valor) {
    this.attributes[nombre] = String(valor);
    if (nombre === "id") this.id = String(valor);
  }
  getAttribute(nombre) { return this.attributes[nombre] ?? null; }
  removeAttribute(nombre) { delete this.attributes[nombre]; }
  append(...nodos) { this.children.push(...nodos); }
  replaceChildren(...nodos) { this.propio = ""; this.children = [...nodos]; }
  addEventListener(tipo, oyente) { (this.listeners[tipo] ??= []).push(oyente); }
  focus() { this.enfocado = true; }
  showModal() { this.open = true; }
  close() { this.open = false; this.emitir("close"); }
  emitir(tipo, evento = {}) {
    for (const oyente of this.listeners[tipo] ?? []) oyente({ preventDefault() {}, ...evento });
  }
}

export const documentoFalso = () => ({ createElement: (tag) => new Nodo(tag) });
export const todos = (raiz, condicion) => [
  ...(condicion(raiz) ? [raiz] : []),
  ...raiz.children.flatMap((hijo) => todos(hijo, condicion))
];
export const porId = (raiz, id) => todos(raiz, (n) => n.id === id)[0];
export const boton = (raiz, texto) => todos(raiz, (n) => n.tag === "button" && n.textContent === texto)[0];
export const formulario = (raiz) => todos(raiz, (n) => n.tag === "form")[0];
export const alertas = (raiz) => todos(raiz, (n) => n.getAttribute("role") === "alert" && n.textContent !== "").map((n) => n.textContent);
export const esperar = () => new Promise((resolve) => setImmediate(resolve));
