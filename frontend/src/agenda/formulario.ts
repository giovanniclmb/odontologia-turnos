/** Envío único y errores sin perder lo ingresado. */
import { boton, nodo, type Documento } from "../dom/nodo.js";
import { zonaDeError } from "./comunes.js";

export interface Formulario {
  nodo: HTMLFormElement;
  error: ReturnType<typeof zonaDeError>;
  confirmar: HTMLButtonElement;
}

export interface OpcionesFormulario {
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
  let enviando = false;
  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    if (enviando || confirmar.disabled) return;
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
    enviando = true;
    confirmar.disabled = true;
    void preparado()
      .catch((fallo: unknown) => error.mostrar(opciones.traducir(fallo)))
      .finally(() => {
        enviando = false;
        confirmar.disabled = false;
      });
  });
  return { nodo: formulario, error, confirmar };
}
