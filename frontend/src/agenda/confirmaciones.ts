import { boton, buscarElemento, nodo } from "../dom/nodo.js";
import { fechaEnSede, horaEnSede } from "../tiempo/mendoza.js";
import { alerta, cargando, encabezado, falloConReintento } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { codigoDe, mensajeDeError, resultadoEdicionIncierto } from "./mensajes.js";
import type { RutaAgenda } from "./ruta.js";
import type { Catalogos, TurnoAgenda } from "./tipos.js";

// Conserva el bloqueo también si se sale de la vista mientras se espera la respuesta.
const bloqueados = new Set<string>();
const enviando = new Set<string>();
const generaciones = new WeakMap<HTMLElement, number>();

export async function montarConfirmaciones(contexto: Contexto, ruta: RutaAgenda, resultado?: string): Promise<void> {
  const { doc, raiz, api, usuario } = contexto;
  const generacion = (generaciones.get(raiz) ?? 0) + 1;
  generaciones.set(raiz, generacion);
  const vigente = () => contexto.vigente() && generaciones.get(raiz) === generacion;
  const consultables = [...bloqueados].filter((id) => !enviando.has(id));
  const volver = boton(doc, "Volver a agenda", "btn-secondary", () => contexto.navegar({ ...ruta, seccion: "agenda" }));
  const cabecera = () => encabezado(doc, "Turnos por confirmar", "Seguimiento manual de los turnos pendientes de toda la sede.", [volver]);
  if (usuario.rol !== "recepcion") {
    raiz.replaceChildren(cabecera(), alerta(doc, "No tiene permiso para esta operación."));
    return;
  }
  raiz.replaceChildren(cabecera(), ...(resultado ? [nodo(doc, "p", { clase: "notice", texto: resultado, atributos: { role: "status" } })] : []), cargando(doc, "Cargando pendientes…"));
  try {
    const [turnos, sillones, profesionales, prestaciones] = await Promise.all([
      api.pendientes(), api.sillones(), api.profesionales(), api.prestaciones()
    ]);
    if (!vigente()) return;
    for (const id of consultables) if (!enviando.has(id)) bloqueados.delete(id);
    const catalogos: Catalogos = { sillones, profesionales, prestaciones };
    const recargar = (mensaje?: string) => void montarConfirmaciones(contexto, ruta, mensaje);
    const contenido: Node[] = [cabecera()];
    if (resultado) contenido.push(nodo(doc, "p", { clase: "notice", texto: resultado, atributos: { id: "confirmaciones-resultado", role: "status", tabindex: "-1" } }));
    const actualizar = boton(doc, "Actualizar lista", "btn-secondary", () => recargar());
    const titulo = nodo(doc, "div", { clase: "card-header" }, [nodo(doc, "div", {}, [
      nodo(doc, "h2", { clase: "card-title", texto: "Pendientes" }),
      nodo(doc, "p", { clase: "card-subtitle", texto: "Ordenados por fecha y hora de inicio · Hora de Mendoza" })
    ]), actualizar]);
    const filas = turnos.map((turno) => fila(contexto, turno, catalogos, recargar));
    contenido.push(nodo(doc, "section", { clase: "card" }, [titulo, filas.length === 0
      ? nodo(doc, "p", { clase: "empty-state", texto: "No hay turnos pendientes de confirmación.", atributos: { role: "status" } })
      : nodo(doc, "ul", { clase: "seguimiento-lista" }, filas)]));
    raiz.replaceChildren(...contenido);
    if (resultado) buscarElemento(raiz, (elemento) => elemento.id === "confirmaciones-resultado")?.focus();
  } catch (error) {
    if (!vigente()) return;
    raiz.replaceChildren(cabecera(), ...(resultado ? [nodo(doc, "p", { clase: "notice", texto: resultado, atributos: { id: "confirmaciones-resultado", role: "status", tabindex: "-1" } })] : []),
      falloConReintento(doc, `No se pudo cargar la lista. ${mensajeDeError(error, usuario.rol)}`, () => void montarConfirmaciones(contexto, ruta, resultado)));
    if (resultado) buscarElemento(raiz, (elemento) => elemento.id === "confirmaciones-resultado")?.focus();
  }
}

function fila(contexto: Contexto, turno: TurnoAgenda, catalogos: Catalogos, recargar: (mensaje?: string) => void): HTMLLIElement {
  const { doc } = contexto;
  const codigo = (items: readonly { id: string; codigo: string }[], id: string) => items.find((item) => item.id === id)?.codigo ?? id;
  const fecha = fechaEnSede(new Date(turno.inicio)).split("-").reverse().join("/");
  const hora = horaEnSede(new Date(turno.inicio));
  const mensaje = nodo(doc, "div", {});
  const control = boton(doc, "Registrar confirmación", "btn-primary", () => {
    if (bloqueados.has(turno.id) || enviando.has(turno.id)) return;
    enviando.add(turno.id);
    control.disabled = true;
    const envio = contexto.enviar(`editar-turno:${turno.id}`, async () => {
      try {
        await contexto.api.confirmarTurno(turno.id, turno.version);
        bloqueados.add(turno.id);
        if (contexto.vigente()) recargar("La confirmación fue registrada.");
      } catch (error) {
        const recarga = resultadoEdicionIncierto(error) || codigoDe(error) === "version_turno_desactualizada" || codigoDe(error) === "solicitud_invalida";
        if (recarga) bloqueados.add(turno.id);
        if (contexto.vigente()) {
          const texto = resultadoEdicionIncierto(error)
            ? "No se pudo confirmar si se registró. Actualice la lista antes de volver a intentarlo."
            : `${mensajeDeError(error, contexto.usuario.rol)}${recarga ? " Actualice la lista antes de volver a intentarlo." : ""}`;
          mensaje.replaceChildren(alerta(doc, texto));
          control.disabled = recarga;
        }
        if (!contexto.vigente()) throw error;
      } finally { enviando.delete(turno.id); }
    });
    if (envio === null) { enviando.delete(turno.id); control.disabled = true; }
    else void envio.catch(() => {});
  });
  control.disabled = bloqueados.has(turno.id) || enviando.has(turno.id);
  control.setAttribute("aria-label", `Registrar confirmación de ${turno.pacienteNombre} ${turno.pacienteApellido}, ${fecha} ${hora}`);
  const fin = horaEnSede(new Date(turno.fin));
  return nodo(doc, "li", { clase: "seguimiento-turno" }, [
    nodo(doc, "div", { clase: "slot-time" }, [nodo(doc, "strong", { texto: fecha }), nodo(doc, "div", { texto: `${hora}–${fin}` })]),
    nodo(doc, "div", { clase: "slot-info" }, [nodo(doc, "strong", { texto: `${turno.pacienteNombre} ${turno.pacienteApellido}` }),
      nodo(doc, "small", { texto: `${codigo(catalogos.profesionales, turno.profesionalId)} · ${codigo(catalogos.sillones, turno.sillonId)} · ${codigo(catalogos.prestaciones, turno.prestacionId)}` }), mensaje]), control
  ]);
}
