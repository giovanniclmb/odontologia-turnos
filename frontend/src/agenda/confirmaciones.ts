import { boton, buscarElemento, nodo } from "../dom/nodo.js";
import { fechaEnSede, horaEnSede } from "../tiempo/mendoza.js";
import { alerta, cargando, encabezado, falloConReintento } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { codigoDe, mensajeDeError, resultadoEdicionIncierto } from "./mensajes.js";
import type { RutaAgenda } from "./ruta.js";
import type { Catalogos, Rol, TurnoAgenda } from "./tipos.js";

const ID_ACTUALIZAR = "confirmaciones-actualizar";
const ID_RESULTADO = "confirmaciones-resultado";
const CLASE_AVISO = "alert";
const PEDIR_ACTUALIZAR = "Actualice la lista antes de volver a intentarlo.";

interface Opciones {
  resultado?: string | undefined;
  /** Qué recibe el foco al terminar de dibujar. */
  foco?: "actualizar" | "aviso" | undefined;
}

/** La vista montada en una raíz, para entregarle los envíos que terminan después de redibujar. */
interface Vista {
  resultado: string | undefined;
  vigente(): boolean;
  recargar(opciones?: Opciones): void;
}

// Conserva el bloqueo también si se sale de la vista mientras se espera la respuesta.
const bloqueados = new Set<string>();
const enviando = new Set<string>();
const generaciones = new WeakMap<HTMLElement, number>();
const vistas = new WeakMap<HTMLElement, Vista>();
/** Fallos por turno: se vuelven a mostrar al redibujar, hasta que se pida actualizar la lista. */
const fallos = new WeakMap<HTMLElement, Map<string, string>>();

export async function montarConfirmaciones(
  contexto: Contexto, ruta: RutaAgenda, opciones: Opciones = {}
): Promise<void> {
  const { doc, raiz, api, usuario } = contexto;
  const generacion = (generaciones.get(raiz) ?? 0) + 1;
  generaciones.set(raiz, generacion);
  const vigente = () => contexto.vigente() && generaciones.get(raiz) === generacion;
  const volver = boton(doc, "Volver a agenda", "btn-secondary", () => contexto.navegar({ ...ruta, seccion: "agenda" }));
  const cabecera = () => encabezado(doc, "Turnos por confirmar", "Seguimiento manual de los turnos pendientes de toda la sede.", [volver]);
  if (usuario.rol !== "recepcion") {
    raiz.replaceChildren(cabecera(), alerta(doc, "No tiene permiso para esta operación."));
    return;
  }
  const recargar = (siguientes?: Opciones) => void montarConfirmaciones(contexto, ruta, siguientes);
  vistas.set(raiz, { resultado: opciones.resultado, vigente, recargar });
  const consultables = [...bloqueados].filter((id) => !enviando.has(id));
  const previos = () => [cabecera(), ...mensajesPrevios(contexto, opciones.resultado)];
  raiz.replaceChildren(...previos(), cargando(doc, "Cargando pendientes…"));
  try {
    const [turnos, sillones, profesionales, prestaciones] = await Promise.all([
      api.pendientes(), api.sillones(), api.profesionales(), api.prestaciones()
    ]);
    if (!vigente()) return;
    for (const id of consultables) if (!enviando.has(id)) bloqueados.delete(id);
    const catalogos: Catalogos = { sillones, profesionales, prestaciones };
    const filas = turnos.map((turno) => fila(contexto, turno, catalogos, vigente));
    raiz.replaceChildren(...previos(), listado(contexto, filas, recargar));
    enfocar(raiz, opciones);
  } catch (error) {
    if (!vigente()) return;
    const reintentar = () => recargar({ resultado: opciones.resultado, foco: "actualizar" });
    const fallo = falloConReintento(doc, `No se pudo cargar la lista. ${mensajeDeError(error, usuario.rol)}`, reintentar);
    raiz.replaceChildren(...previos(), fallo);
    if (opciones.foco === "actualizar") buscarElemento(fallo, (elemento) => elemento.tagName === "BUTTON")?.focus();
    else enfocar(raiz, opciones);
  }
}

/** Resultado del último registro y fallos todavía sin revisar. */
function mensajesPrevios(contexto: Contexto, resultado: string | undefined): HTMLElement[] {
  const { doc, raiz } = contexto;
  const exito = resultado === undefined ? [] : [nodo(doc, "p", {
    clase: "notice", texto: resultado, atributos: { id: ID_RESULTADO, role: "status", tabindex: "-1" }
  })];
  return [...exito, ...[...(fallos.get(raiz)?.values() ?? [])].map((texto) => avisoDeFallo(contexto, texto))];
}

function avisoDeFallo(contexto: Contexto, texto: string): HTMLParagraphElement {
  const aviso = alerta(contexto.doc, texto);
  aviso.setAttribute("tabindex", "-1");
  return aviso;
}

function enfocar(raiz: HTMLElement, { resultado, foco }: Opciones): void {
  if (foco === "aviso") buscarElemento(raiz, (elemento) => elemento.className === CLASE_AVISO)?.focus();
  else if (resultado !== undefined) buscarElemento(raiz, (elemento) => elemento.id === ID_RESULTADO)?.focus();
  else if (foco === "actualizar") buscarElemento(raiz, (elemento) => elemento.id === ID_ACTUALIZAR)?.focus();
}

function listado(contexto: Contexto, filas: readonly HTMLLIElement[], recargar: Vista["recargar"]): HTMLElement {
  const { doc, raiz } = contexto;
  const actualizar = boton(doc, "Actualizar lista", "btn-secondary", () => {
    fallos.delete(raiz);
    recargar({ foco: "actualizar" });
  });
  actualizar.id = ID_ACTUALIZAR;
  const titulo = nodo(doc, "div", { clase: "card-header" }, [nodo(doc, "div", {}, [
    nodo(doc, "h2", { clase: "card-title", texto: "Pendientes" }),
    nodo(doc, "p", { clase: "card-subtitle", texto: "Ordenados por fecha y hora de inicio · Hora de Mendoza" })
  ]), actualizar]);
  return nodo(doc, "section", { clase: "card" }, [titulo, filas.length === 0
    ? nodo(doc, "p", { clase: "empty-state", texto: "No hay turnos pendientes de confirmación.", atributos: { role: "status" } })
    : nodo(doc, "ul", { clase: "seguimiento-lista" }, filas)]);
}

function conPunto(texto: string): string {
  return /[.!?]$/.test(texto) ? texto : `${texto}.`;
}

function motivo(error: unknown, rol: Rol): string {
  return codigoDe(error) === "version_turno_desactualizada"
    ? "El turno cambió desde la última consulta."
    : conPunto(mensajeDeError(error, rol));
}

/** Un resultado incierto o un turno que cambió obligan a consultar antes de reenviar. */
function exigeConsultar(error: unknown): boolean {
  const codigo = codigoDe(error);
  return resultadoEdicionIncierto(error) || codigo === "version_turno_desactualizada" || codigo === "solicitud_invalida";
}

function textoEnFila(error: unknown, rol: Rol): string {
  if (resultadoEdicionIncierto(error)) return `No se pudo confirmar si se registró. ${PEDIR_ACTUALIZAR}`;
  return exigeConsultar(error) ? `${motivo(error, rol)} ${PEDIR_ACTUALIZAR}` : motivo(error, rol);
}

/** Texto que identifica el turno porque se muestra lejos de su fila. */
function textoFueraDeFila(error: unknown, rol: Rol, referencia: string): string {
  return resultadoEdicionIncierto(error)
    ? `No se pudo saber si se registró la confirmación de ${referencia}. Revise la lista antes de volver a intentarlo.`
    : `No se registró la confirmación de ${referencia}. ${motivo(error, rol)}`;
}

function fila(contexto: Contexto, turno: TurnoAgenda, catalogos: Catalogos, filaVigente: () => boolean): HTMLLIElement {
  const { doc, raiz, usuario } = contexto;
  const codigo = (items: readonly { id: string; codigo: string }[], id: string) => items.find((item) => item.id === id)?.codigo ?? id;
  const paciente = `${turno.pacienteNombre} ${turno.pacienteApellido}`;
  const fecha = fechaEnSede(new Date(turno.inicio)).split("-").reverse().join("/");
  const hora = horaEnSede(new Date(turno.inicio));
  const referencia = `${paciente} (${fecha} ${hora})`;
  const mensaje = nodo(doc, "div", {});
  const mostrar = (texto: string) => {
    const aviso = avisoDeFallo(contexto, texto);
    mensaje.replaceChildren(aviso);
    aviso.focus();
  };

  const alTerminar = () => {
    const vista = vistas.get(raiz);
    if (vista?.vigente()) vista.recargar({ resultado: "La confirmación fue registrada." });
  };
  /** Muestra el fallo donde esté la persona; si ya no está en Confirmaciones, lo propaga a la sección. */
  const alFallar = (error: unknown) => {
    const vista = vistas.get(raiz);
    if (!vista?.vigente()) throw error;
    const propios = fallos.get(raiz) ?? new Map<string, string>();
    fallos.set(raiz, propios.set(turno.id, textoFueraDeFila(error, usuario.rol, referencia)));
    if (!filaVigente()) return vista.recargar({ resultado: vista.resultado, foco: "aviso" });
    mostrar(textoEnFila(error, usuario.rol));
    control.disabled = exigeConsultar(error);
  };

  const control = boton(doc, "Registrar confirmación", "btn-primary", () => {
    if (bloqueados.has(turno.id) || enviando.has(turno.id)) return;
    enviando.add(turno.id);
    control.disabled = true;
    mensaje.replaceChildren();
    fallos.get(raiz)?.delete(turno.id);
    const envio = contexto.enviar(`editar-turno:${turno.id}`, async () => {
      try {
        await contexto.api.confirmarTurno(turno.id, turno.version);
      } catch (error) {
        enviando.delete(turno.id);
        if (exigeConsultar(error)) bloqueados.add(turno.id);
        return alFallar(error);
      }
      enviando.delete(turno.id);
      bloqueados.add(turno.id);
      alTerminar();
    }, (error) => textoFueraDeFila(error, usuario.rol, referencia));
    if (envio !== null) return void envio.catch(() => {});
    enviando.delete(turno.id);
    control.disabled = false;
    mostrar("Hay una edición en curso para este turno. Espere su resultado.");
  });
  control.disabled = bloqueados.has(turno.id) || enviando.has(turno.id);
  control.setAttribute("aria-label", `Registrar confirmación de ${paciente}, ${fecha} ${hora}`);
  const fin = horaEnSede(new Date(turno.fin));
  return nodo(doc, "li", { clase: "seguimiento-turno" }, [
    nodo(doc, "div", { clase: "slot-time" }, [nodo(doc, "strong", { texto: fecha }), nodo(doc, "div", { texto: `${hora}–${fin}` })]),
    nodo(doc, "div", { clase: "slot-info" }, [nodo(doc, "strong", { texto: paciente }),
      nodo(doc, "small", { texto: `${codigo(catalogos.profesionales, turno.profesionalId)} · ${codigo(catalogos.sillones, turno.sillonId)} · ${codigo(catalogos.prestaciones, turno.prestacionId)}` }), mensaje]), control
  ]);
}
