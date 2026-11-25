import { boton, nodo } from "../dom/nodo.js";
import { entrada, seleccion } from "../dom/campos.js";
import { instanteDesdeSede } from "../tiempo/mendoza.js";
import { aviso, cargando, encabezado, falloConReintento } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { formularioUnico } from "./formulario.js";
import { mensajeDeError } from "./mensajes.js";
import type { RutaAgenda } from "./ruta.js";

const ninguno = { valor: "", texto: "Ninguno" };
const opcionesDe = (catalogo: readonly { id: string; codigo: string }[]) =>
  [ninguno, ...catalogo.map((elemento) => ({ valor: elemento.id, texto: elemento.codigo }))];

function cabecera(contexto: Contexto, ruta: RutaAgenda): HTMLElement {
  return encabezado(contexto.doc, "Bloquear período",
    "Elija un sillón, un profesional o ambos. Un bloqueo no se puede modificar ni quitar por ahora.",
    [boton(contexto.doc, "Volver a la agenda", "btn-secondary", () => contexto.navegar({ ...ruta, seccion: "agenda" }))]);
}

export async function montarAltaBloqueo(contexto: Contexto, ruta: RutaAgenda): Promise<void> {
  const { doc, raiz, api, usuario } = contexto;
  raiz.replaceChildren(cabecera(contexto, ruta), cargando(doc, "Cargando recursos…"));
  try {
    const [sillones, profesionales] = await Promise.all([api.sillones(), api.profesionales()]);
    if (!contexto.vigente()) return;
    const sillon = seleccion(doc, "bloqueo-sillon", "Sillón", opcionesDe(sillones), ruta.sillonId ?? "");
    const profesional = seleccion(doc, "bloqueo-profesional", "Profesional", opcionesDe(profesionales));
    const desdeFecha = entrada(doc, "bloqueo-desde-fecha", "Fecha *", "date", ruta.fecha);
    const desdeHora = entrada(doc, "bloqueo-desde-hora", "Hora *", "time");
    const hastaFecha = entrada(doc, "bloqueo-hasta-fecha", "Fecha *", "date", ruta.fecha);
    const hastaHora = entrada(doc, "bloqueo-hasta-hora", "Hora *", "time");
    const grupo = (titulo: string, campos: readonly Node[]) =>
      nodo(doc, "fieldset", {}, [nodo(doc, "legend", { texto: titulo }), ...campos]);
    const formulario = formularioUnico(doc, {
      campos: [
        grupo("Recursos (al menos uno)", [sillon.contenedor, profesional.contenedor]),
        grupo("Desde (hora de Mendoza)", [desdeFecha.contenedor, desdeHora.contenedor]),
        grupo("Hasta (no incluido)", [hastaFecha.contenedor, hastaHora.contenedor])
      ],
      textoConfirmar: "Crear bloqueo",
      alCancelar: () => contexto.navegar({ ...ruta, seccion: "agenda" }),
      traducir: (error) => mensajeDeError(error, usuario.rol),
      enviar: () => {
        const sillonId = sillon.control.value || null;
        const profesionalId = profesional.control.value || null;
        if (sillonId === null && profesionalId === null) return "Elija al menos un sillón o un profesional.";
        const inicio = instanteDesdeSede(desdeFecha.control.value, desdeHora.control.value);
        const fin = instanteDesdeSede(hastaFecha.control.value, hastaHora.control.value);
        if (inicio === null || fin === null) return "Complete fecha y hora de inicio y de fin.";
        if (fin.getTime() <= inicio.getTime()) return "El fin debe ser posterior al inicio.";
        const dia = desdeFecha.control.value;
        return async () => {
          await api.crearBloqueo({ sillonId, profesionalId, inicio: inicio.toISOString(), fin: fin.toISOString() });
          if (contexto.vigente()) {
            contexto.navegar({ ...ruta, seccion: "agenda", fecha: dia, sillonId: sillonId ?? ruta.sillonId });
          }
        };
      }
    });
    const sinRecursos = sillones.length === 0 && profesionales.length === 0;
    formulario.confirmar.disabled = sinRecursos;
    raiz.replaceChildren(cabecera(contexto, ruta),
      ...(sinRecursos ? [aviso(doc, "No hay sillones ni profesionales para bloquear.")] : []),
      nodo(doc, "section", { clase: "card" }, [formulario.nodo]));
  } catch (error) {
    if (!contexto.vigente()) return;
    raiz.replaceChildren(cabecera(contexto, ruta), falloConReintento(doc, mensajeDeError(error, usuario.rol),
      () => void montarAltaBloqueo(contexto, ruta)));
  }
}
