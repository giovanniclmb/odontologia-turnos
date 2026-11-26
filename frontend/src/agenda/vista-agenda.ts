import { boton, buscarElemento, nodo, type Documento } from "../dom/nodo.js";
import { entrada, seleccion } from "../dom/campos.js";
import { diasDelPeriodo, fechaEnSede, instanteDesdeSede, limitesDePeriodo, sumarDias, type Vista } from "../tiempo/mendoza.js";
import { alerta, aviso, cargando, encabezado, falloConReintento } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { mostrarCancelacion, mostrarReprogramacion, resumenTurno } from "./edicion-turno.js";
import { mensajeDeError, mensajeSinIntervalo } from "./mensajes.js";
import { componerDias, fechaCorta, type Dia, type Elemento } from "./modelo.js";
import type { RutaAgenda } from "./ruta.js";
import type { Catalogos, ResultadoEdicion, Sillon, TurnoActualizado, TurnoAgenda } from "./tipos.js";

const NOMBRES_DIA = ["Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"];

function nombreDia(fecha: string): string {
  return NOMBRES_DIA[new Date(`${fecha}T00:00:00Z`).getUTCDay()] ?? "";
}

function plural(cantidad: number, singular: string): string {
  return `${cantidad} ${singular}${cantidad === 1 ? "" : "s"}`;
}

function cabecera(contexto: Contexto, ruta: RutaAgenda): HTMLElement {
  const { doc } = contexto;
  const acciones: HTMLButtonElement[] = [];
  if (contexto.usuario.rol === "recepcion") {
    const pendientes = boton(doc, "Confirmaciones", "btn-secondary", () => contexto.navegar({ ...ruta, seccion: "agenda/confirmaciones" }));
    pendientes.id = "agenda-confirmaciones";
    acciones.push(pendientes);
    const bloquear = boton(doc, "Bloquear período", "btn-secondary", () => contexto.navegar({ ...ruta, seccion: "agenda/bloqueo" }));
    bloquear.id = "agenda-bloquear";
    acciones.push(bloquear);
  }
  const nuevo = boton(doc, "+ Nuevo turno", "btn-primary", () => contexto.navegar({ ...ruta, seccion: "agenda/nuevo" }));
  nuevo.id = "agenda-nuevo";
  acciones.push(nuevo);
  const subtitulo = ruta.vista === "dia"
    ? "Hora de Mendoza. Duración efectiva de cada turno."
    : "Hora de Mendoza. Semana de lunes a domingo.";
  return encabezado(doc, "Agenda por sillón", subtitulo, acciones);
}

/**
 * Al tipear, cada dígito puede formar una fecha válida: navegar en ese momento destruiría el campo.
 * Con teclado se aplica al salir o con Enter; elegida en el calendario, de inmediato.
 */
function alElegirFecha(control: HTMLInputElement, actual: string, aplicar: (fecha: string) => void): void {
  let tecleando = false;
  let aplicada = actual;
  const confirmar = () => {
    const elegida = control.value;
    if (elegida === aplicada || instanteDesdeSede(elegida, "00:00") === null) return;
    aplicada = elegida;
    aplicar(elegida);
  };
  control.addEventListener("keydown", (evento) => {
    if (evento.key === "Enter") confirmar();
    else if (evento.key !== "Tab") tecleando = true;
  });
  control.addEventListener("pointerdown", () => { tecleando = false; });
  control.addEventListener("change", () => { if (!tecleando) confirmar(); });
  control.addEventListener("blur", confirmar);
}

function barra(contexto: Contexto, ruta: RutaAgenda, sillones: readonly Sillon[]): HTMLElement {
  const { doc } = contexto;
  const ir = (cambios: Partial<RutaAgenda>) => contexto.navegar({ ...ruta, ...cambios });
  const sillon = seleccion(doc, "agenda-sillon", "Sillón",
    sillones.map((s) => ({ valor: s.id, texto: s.activo ? s.codigo : `${s.codigo} (inactivo)` })), ruta.sillonId ?? "");
  sillon.control.addEventListener("change", () => ir({ sillonId: sillon.control.value }));
  const fecha = entrada(doc, "agenda-fecha", ruta.vista === "dia" ? "Fecha" : "Fecha dentro de la semana", "date", ruta.fecha);
  alElegirFecha(fecha.control, ruta.fecha, (elegida) => ir({ fecha: elegida }));
  const salto = ruta.vista === "dia" ? 1 : 7;
  const control = (id: string, texto: string, cambios: Partial<RutaAgenda>) => {
    const elemento = boton(doc, texto, "btn-secondary", () => ir(cambios));
    elemento.id = id;
    return elemento;
  };
  const vista = (id: string, texto: string, valor: Vista) => {
    const elemento = control(id, texto, { vista: valor });
    elemento.setAttribute("aria-pressed", String(ruta.vista === valor));
    return elemento;
  };
  return nodo(doc, "section", { clase: "card", atributos: { "aria-label": "Período y sillón" } }, [
    nodo(doc, "div", { clase: "toolbar" }, [
      sillon.contenedor,
      fecha.contenedor,
      nodo(doc, "div", { clase: "stepper", atributos: { role: "group", "aria-label": "Cambiar período" } }, [
        control("agenda-anterior", "Anterior", { fecha: sumarDias(ruta.fecha, -salto) }),
        control("agenda-hoy", "Hoy", { fecha: contexto.hoy }),
        control("agenda-siguiente", "Siguiente", { fecha: sumarDias(ruta.fecha, salto) })
      ]),
      nodo(doc, "div", { clase: "segmented", atributos: { role: "group", "aria-label": "Vista" } }, [
        vista("agenda-vista-dia", "Día", "dia"),
        vista("agenda-vista-semana", "Semana", "semana")
      ])
    ])
  ]);
}

type Acciones = (elemento: Elemento, fecha: string) => HTMLElement | null;

function elementoDeLista(doc: Documento, elemento: Elemento, conDuracionJunto: boolean, acciones: HTMLElement | null): HTMLLIElement {
  const clase = elemento.estado === "bloqueo" ? "slot is-block" : elemento.estado === "cancelado" ? "slot is-cancelled" : "slot";
  const insignia: Record<Elemento["estado"], string> = {
    pendiente: "badge-pending", confirmado: "badge-confirmed", cancelado: "badge-cancelled", bloqueo: "badge-block"
  };
  const horario = conDuracionJunto && elemento.tipo === "turno" ? `${elemento.horario} · ${elemento.duracion}` : elemento.horario;
  return nodo(doc, "li", { clase }, [
    nodo(doc, "div", { clase: "slot-time", texto: horario },
      conDuracionJunto ? [] : [nodo(doc, "small", { texto: elemento.duracion })]),
    nodo(doc, "div", { clase: "slot-info" }, [
      nodo(doc, "strong", { texto: elemento.titulo }),
      nodo(doc, "small", { texto: elemento.detalle })
    ]),
    nodo(doc, "span", { clase: `badge ${insignia[elemento.estado]}`, texto: elemento.etiqueta }),
    ...(acciones === null ? [] : [acciones])
  ]);
}

function lista(doc: Documento, dia: Dia, semanal: boolean, acciones: Acciones): HTMLUListElement {
  return nodo(doc, "ul", { clase: "day-list" },
    dia.elementos.map((elemento) => elementoDeLista(doc, elemento, semanal, acciones(elemento, dia.fecha))));
}

function periodo(contexto: Contexto, ruta: RutaAgenda, dias: readonly Dia[], sillon: string, acciones: Acciones): HTMLElement {
  const { doc } = contexto;
  const primero = dias[0];
  const ultimo = dias[dias.length - 1];
  const anio = (ultimo?.fecha ?? ruta.fecha).slice(0, 4);
  const titulo = ruta.vista === "dia"
    ? `${nombreDia(ruta.fecha)} ${fechaCorta(ruta.fecha)}/${anio} · ${sillon}`
    : `Semana ${fechaCorta(primero?.fecha ?? ruta.fecha)} – ${fechaCorta(ultimo?.fecha ?? ruta.fecha)}/${anio} · ${sillon}`;
  const titulos = nodo(doc, "div", {}, [nodo(doc, "h2", { clase: "card-title", texto: titulo })]);
  const encabezadoPeriodo = nodo(doc, "div", { clase: "card-header" }, [titulos]);
  const tarjeta = (contenido: Node) => nodo(doc, "section", { clase: "card" }, [encabezadoPeriodo, contenido]);
  if (dias.every((dia) => dia.elementos.length === 0)) {
    return tarjeta(nodo(doc, "p", {
      clase: "empty-state", texto: "No hay turnos ni bloqueos en este período para el sillón elegido.", atributos: { role: "status" }
    }));
  }
  if (ruta.vista === "dia" && primero !== undefined) {
    titulos.append(nodo(doc, "p", {
      clase: "card-subtitle", texto: `${plural(primero.turnos, "turno")} y ${plural(primero.bloqueos, "bloqueo")}`
    }));
    return tarjeta(lista(doc, primero, false, acciones));
  }
  return tarjeta(nodo(doc, "div", { clase: "week" }, dias.map((dia) => nodo(doc, "section", { clase: "week-day" }, [
    nodo(doc, "h3", { texto: `${nombreDia(dia.fecha).slice(0, 3)} ${fechaCorta(dia.fecha)}` }),
    dia.elementos.length === 0 ? nodo(doc, "p", { clase: "week-empty", texto: "Sin actividad visible" }) : lista(doc, dia, true, acciones)
  ]))));
}

function resultadoEdicion(original: TurnoAgenda, actualizado: TurnoActualizado, catalogos: Catalogos): ResultadoEdicion {
  const destino = resumenTurno(actualizado, catalogos);
  if (actualizado.estado === "CANCELADO") {
    return { texto: `Turno cancelado. Se liberaron el sillón y el horario del profesional. ${destino}.`, destino: null };
  }
  return original.version === actualizado.version
    ? { texto: "Se mantuvo el horario original.", destino: null }
    : { texto: `Turno reprogramado para ${destino}.`, destino: actualizado };
}

function mostrarResultado(contexto: Contexto, ruta: RutaAgenda, resultado: ResultadoEdicion): HTMLElement {
  const { doc } = contexto;
  const { destino } = resultado;
  const aviso = nodo(doc, "p", {
    texto: resultado.texto, atributos: { id: "agenda-resultado", role: "status", tabindex: "-1" }
  });
  const contenido: Node[] = [aviso];
  const { desde, hasta } = limitesDePeriodo(ruta.vista, ruta.fecha);
  if (destino !== null && (destino.sillonId !== ruta.sillonId
    || Date.parse(destino.inicio) >= Date.parse(hasta) || Date.parse(destino.fin) <= Date.parse(desde))) {
    contenido.push(nodo(doc, "div", {}, [boton(doc, "Ver destino", "btn-secondary", () => contexto.navegar({
      ...ruta, sillonId: destino.sillonId, fecha: fechaEnSede(new Date(destino.inicio))
    }))]));
  }
  return nodo(doc, "div", { clase: "notice stack" }, contenido);
}

export async function montarAgenda(contexto: Contexto, ruta: RutaAgenda, resultado?: ResultadoEdicion): Promise<void> {
  const { doc, raiz, api, usuario } = contexto;
  raiz.replaceChildren(cabecera(contexto, ruta), cargando(doc, "Cargando agenda…"));
  try {
    const [sillones, profesionales, prestaciones, intervalo] = await Promise.all(
      [api.sillones(), api.profesionales(), api.prestaciones(), api.intervalo()]);
    if (!contexto.vigente()) return;
    const sillon = sillones.find((s) => s.id === ruta.sillonId) ?? sillones.find((s) => s.activo) ?? sillones[0];
    if (sillon === undefined) {
      raiz.replaceChildren(cabecera(contexto, ruta),
        ...(resultado === undefined ? [] : [mostrarResultado(contexto, ruta, resultado)]),
        aviso(doc, "No hay sillones disponibles. Sin ellos no se puede consultar la agenda ni crear turnos."));
      return;
    }
    const efectiva: RutaAgenda = { ...ruta, sillonId: sillon.id };
    if (sillon.id !== ruta.sillonId) contexto.reemplazar(efectiva);
    const { desde, hasta } = limitesDePeriodo(ruta.vista, ruta.fecha);
    const [turnos, bloqueos] = await Promise.all(
      [api.turnos(sillon.id, desde, hasta), api.bloqueos(sillon.id, desde, hasta)]);
    if (!contexto.vigente()) return;
    const catalogos: Catalogos = { sillones, profesionales, prestaciones };
    const dias = componerDias({ dias: diasDelPeriodo(ruta.vista, ruta.fecha), rol: usuario.rol, catalogos, turnos, bloqueos });
    const contenido = [cabecera(contexto, efectiva), barra(contexto, efectiva, sillones)];
    if (resultado !== undefined) contenido.push(mostrarResultado(contexto, efectiva, resultado));
    if (intervalo === null) contenido.push(alerta(doc, mensajeSinIntervalo(usuario.rol)));
    if (usuario.rol === "odontologo") {
      contenido.push(aviso(doc, "Ve únicamente sus turnos. Un horario sin turnos visibles puede no estar disponible."));
    }
    const editables = new Map(turnos.filter((turno) => Date.parse(turno.inicio) > Date.now() && (
      usuario.rol === "recepcion" ? turno.estado !== "CANCELADO"
        : turno.estado === "PENDIENTE" && turno.profesionalId === usuario.profesionalRef
    )).map((turno) => [turno.id, turno]));
    const acciones: Acciones = (elemento, fecha) => {
      const turno = elemento.tipo === "turno" ? editables.get(elemento.id) : undefined;
      if (turno === undefined) return null;
      const accion = (texto: string, abrir: typeof mostrarReprogramacion) => {
        const control = boton(doc, texto, "btn-secondary", () => {
          if (!contexto.vigente()) return;
          let recargaObligatoria = false;
          const recargar = () => void montarAgenda({ ...contexto, foco: control.id }, efectiva);
          abrir({
            contexto, turno, catalogos,
            volver: () => {
              if (!contexto.vigente()) return;
              if (recargaObligatoria) {
                recargar();
                return;
              }
              raiz.replaceChildren(...contenido);
              control.focus();
            },
            recargar,
            exigirRecarga: () => { recargaObligatoria = true; },
            completada: (actualizado) => {
              const resultado = resultadoEdicion(turno, actualizado, catalogos);
              if (contexto.vigente()) void montarAgenda({ ...contexto, foco: control.id }, efectiva, resultado);
              else contexto.conservarEdicion?.(resultado);
            }
          });
        });
        control.id = `${texto === "Reprogramar" ? "reprogramar" : "cancelar"}-${turno.id}-${fecha}`;
        const verbo = texto === "Reprogramar" ? "Reprogramar turno" : texto;
        control.setAttribute("aria-label", `${verbo} de ${elemento.titulo} (${elemento.horario})`);
        return control;
      };
      return nodo(doc, "div", { clase: "turno-acciones" }, [
        accion("Reprogramar", mostrarReprogramacion), accion("Cancelar turno", mostrarCancelacion)
      ]);
    };
    contenido.push(periodo(contexto, efectiva, dias, sillon.codigo, acciones));
    raiz.replaceChildren(...contenido);
    if (contexto.foco !== null) {
      const foco = buscarElemento(raiz, (elemento) => elemento.id === contexto.foco);
      (foco ?? buscarElemento(raiz, (elemento) => elemento.id === "agenda-resultado"))?.focus();
    }
  } catch (error) {
    if (!contexto.vigente()) return;
    const texto = resultado === undefined ? "No se pudo cargar la agenda." : "No se pudo actualizar la agenda.";
    raiz.replaceChildren(cabecera(contexto, ruta),
      ...(resultado === undefined ? [] : [mostrarResultado(contexto, ruta, resultado)]),
      falloConReintento(doc, `${texto} ${mensajeDeError(error, usuario.rol)}`,
        () => void montarAgenda(contexto, ruta, resultado)));
    if (resultado !== undefined) buscarElemento(raiz, (elemento) => elemento.id === "agenda-resultado")?.focus();
  }
}
