/** Envío único y errores sin perder lo ingresado. */
import { boton, nodo, type Documento } from "../dom/nodo.js";
import { zonaDeError } from "./comunes.js";
import type { Contexto } from "./contexto.js";

export interface Formulario {
  nodo: HTMLFormElement;
  error: ReturnType<typeof zonaDeError>;
  confirmar: HTMLButtonElement;
}

export interface OpcionesFormulario {
  /** Identifica la operación para impedir repetirla mientras está en curso. */
  clave: string;
  enviarUnaVez: Contexto["enviar"];
  campos: readonly Node[];
  textoConfirmar: string;
  alCancelar?: () => void;
  /** Devuelve un mensaje si los datos no permiten enviar, o null si la vista ya lo mostró. */
  enviar(): string | null | (() => Promise<void>);
  traducir(error: unknown): string;
}

export function formularioUnico(doc: Documento, opciones: OpcionesFormulario): Formulario {
  const error = zonaDeError(doc);
  const confirmar = nodo(doc, "button", { clase: "btn btn-primary", texto: opciones.textoConfirmar });
  confirmar.type = "submit";
  const acciones: Node[] = opciones.alCancelar === undefined
    ? [confirmar] : [boton(doc, "Cancelar", "btn-secondary", opciones.alCancelar), confirmar];
  const formulario = nodo(doc, "form", { clase: "patient-form", atributos: { novalidate: "" } },
    [error.nodo, ...opciones.campos, nodo(doc, "div", { clase: "form-actions" }, acciones)]);
  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    if (confirmar.disabled) return;
    const preparado = opciones.enviar();
    if (preparado === null) {
      error.limpiar();
      return;
    }
    if (typeof preparado === "string") {
      error.mostrar(preparado);
      return;
    }
    error.limpiar();
    const envio = opciones.enviarUnaVez(opciones.clave, preparado);
    if (envio === null) {
      error.mostrar("Hay un envío en curso. Espere su resultado antes de enviar otro.");
      return;
    }
    confirmar.disabled = true;
    void envio
      .catch((fallo: unknown) => error.mostrar(opciones.traducir(fallo)))
      .finally(() => {
        confirmar.disabled = false;
      });
  });
  return { nodo: formulario, error, confirmar };
}
