/** Piezas compartidas por las vistas de agenda y configuración. */
import { boton, nodo, type Documento } from "../dom/nodo.js";

export function encabezado(doc: Documento, titulo: string, subtitulo: string, acciones: readonly Node[] = []): HTMLElement {
  return nodo(doc, "section", { clase: "page-header" }, [
    nodo(doc, "div", {}, [
      nodo(doc, "h1", { texto: titulo, atributos: { tabindex: "-1" } }),
      nodo(doc, "p", { texto: subtitulo })
    ]),
    nodo(doc, "div", { clase: "header-actions" }, acciones)
  ]);
}

export function alerta(doc: Documento, texto: string): HTMLParagraphElement {
  return nodo(doc, "p", { clase: "alert", texto, atributos: { role: "alert" } });
}

export function aviso(doc: Documento, texto: string): HTMLParagraphElement {
  return nodo(doc, "p", { clase: "notice", texto });
}

export function cargando(doc: Documento, texto: string): HTMLElement {
  return nodo(doc, "section", { clase: "card", atributos: { role: "status" } }, [
    nodo(doc, "div", { clase: "skeleton", atributos: { "aria-hidden": "true" } }),
    nodo(doc, "span", { clase: "hint", texto })
  ]);
}

export function falloConReintento(doc: Documento, texto: string, reintentar: () => void): HTMLElement {
  return nodo(doc, "section", { clase: "card stack" }, [
    alerta(doc, texto),
    nodo(doc, "div", {}, [boton(doc, "Reintentar", "btn-secondary", reintentar)])
  ]);
}

/** Zona de mensajes que se actualiza sin volver a dibujar el formulario. */
export function zonaDeError(doc: Documento): { nodo: HTMLDivElement; mostrar(texto: string): void; limpiar(): void } {
  const zona = nodo(doc, "div", { clase: "span-2" });
  return {
    nodo: zona,
    mostrar: (texto) => zona.replaceChildren(alerta(doc, texto)),
    limpiar: () => zona.replaceChildren()
  };
}
