import { entrada, seleccion } from "../dom/campos.js";
import { boton, buscarElemento, nodo } from "../dom/nodo.js";
import { fechaEnSede, horaEnSede, instanteDesdeSede } from "../tiempo/mendoza.js";
import { alerta, encabezado } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { codigoDe, mensajeErrorEdicion, resultadoEdicionIncierto } from "./mensajes.js";
import type { Catalogos, TurnoActualizado, TurnoAgenda } from "./tipos.js";

interface Edicion {
  contexto: Contexto;
  turno: TurnoAgenda;
  catalogos: Catalogos;
  volver(): void;
  recargar(): void;
  exigirRecarga(): void;
  completada(turno: TurnoActualizado): void;
}

function codigo(catalogo: readonly { id: string; codigo: string }[], id: string): string {
  return catalogo.find((recurso) => recurso.id === id)?.codigo ?? "No disponible";
}

function paciente(turno: TurnoAgenda): string {
  return `${turno.pacienteNombre} ${turno.pacienteApellido}`;
}

function periodoTurno(turno: TurnoActualizado): string {
  const inicio = new Date(turno.inicio);
  const fin = new Date(turno.fin);
  const fecha = fechaEnSede(inicio);
  const finVisible = fechaEnSede(fin) === fecha ? horaEnSede(fin) : `${fechaEnSede(fin)} ${horaEnSede(fin)}`;
  return `${fecha}, ${horaEnSede(inicio)}–${finVisible}`;
}

export function resumenTurno(turno: TurnoActualizado, catalogos: Catalogos): string {
  const sillon = codigo(catalogos.sillones, turno.sillonId);
  const profesional = codigo(catalogos.profesionales, turno.profesionalId);
  const rotuloSillon = /^sill[oó]n\b/i.test(sillon) ? sillon : `Sillón ${sillon}`;
  const rotuloProfesional = /^profesional\b/i.test(profesional) ? profesional : `Profesional ${profesional}`;
  return `${periodoTurno(turno)} (Mendoza) · ${rotuloProfesional} · ${rotuloSillon}`;
}

function enviarDesde(
  edicion: Edicion, confirmar: HTMLButtonElement, errores: HTMLElement,
  operacion: () => Promise<TurnoActualizado>, marcarEnvio?: (enCurso: boolean) => void
): void {
  const { contexto, turno } = edicion;
  const { doc, usuario } = contexto;
  if (confirmar.disabled || !contexto.vigente()) return;
  errores.replaceChildren();
  let recargarPrimero = false;
  const envio = contexto.enviar(`editar-turno:${turno.id}`, async () => {
    const resultado = await operacion();
    edicion.completada(resultado);
  }, (error) => mensajeErrorEdicion(error, usuario.rol));
  if (envio === null) {
    errores.replaceChildren(alerta(doc, "Hay una edición en curso para este turno. Espere su resultado."));
    return;
  }
  confirmar.disabled = true;
  marcarEnvio?.(true);
  void envio.catch((fallo: unknown) => {
    if (!contexto.vigente()) return;
    const codigo = codigoDe(fallo);
    recargarPrimero = codigo === "version_turno_desactualizada" || resultadoEdicionIncierto(fallo);
    const aviso = alerta(doc, mensajeErrorEdicion(fallo, usuario.rol));
    aviso.setAttribute("tabindex", "-1");
    errores.replaceChildren(aviso);
    if (recargarPrimero) {
      edicion.exigirRecarga();
      errores.append(boton(doc, "Recargar agenda", "btn-secondary", edicion.recargar));
    }
    aviso.focus();
  }).finally(() => {
    confirmar.disabled = recargarPrimero;
    marcarEnvio?.(false);
  });
}

export function mostrarReprogramacion(edicion: Edicion): void {
  const { contexto, turno, catalogos } = edicion;
  const { doc, raiz, api, usuario } = contexto;
  const fechaOriginal = fechaEnSede(new Date(turno.inicio));
  const horaOriginal = horaEnSede(new Date(turno.inicio));
  const fecha = entrada(doc, "reprogramar-fecha", "Nueva fecha", "date", fechaOriginal);
  const hora = entrada(doc, "reprogramar-hora", "Nueva hora de inicio (Mendoza)", "time", horaOriginal);
  const profesionalFijo = usuario.rol === "odontologo";
  const opcionesActivas = (recursos: readonly { id: string; codigo: string; activo: boolean }[], elegido: string) => [
    ...(!recursos.some((recurso) => recurso.activo && recurso.id === elegido)
      ? [{ valor: "", texto: "Seleccione…" }] : []),
    ...recursos.filter((recurso) => recurso.activo).map((recurso) => ({ valor: recurso.id, texto: recurso.codigo }))
  ];
  const profesional = seleccion(doc, "reprogramar-profesional", "Profesional", profesionalFijo
    ? [{ valor: turno.profesionalId, texto: codigo(catalogos.profesionales, turno.profesionalId) }]
    : opcionesActivas(catalogos.profesionales, turno.profesionalId), turno.profesionalId);
  profesional.control.disabled = profesionalFijo;
  const sillon = seleccion(doc, "reprogramar-sillon", "Sillón",
    opcionesActivas(catalogos.sillones, turno.sillonId), turno.sillonId);

  const errores = nodo(doc, "div", { clase: "span-2 errores-edicion" });
  const confirmar = nodo(doc, "button", { clase: "btn btn-primary", texto: "Guardar reprogramación" });
  confirmar.type = "submit";
  const volver = boton(doc, "Volver a la agenda", "btn-secondary", edicion.volver);
  const form = nodo(doc, "form", { clase: "patient-form", atributos: { novalidate: "" } }, [
    errores, fecha.contenedor, hora.contenedor, profesional.contenedor, sillon.contenedor,
    nodo(doc, "div", { clase: "form-actions" }, [volver, confirmar])
  ]);
  form.addEventListener("submit", (evento) => {
    evento.preventDefault();
    if (confirmar.disabled) return;
    const fechaDestino = fecha.control.value;
    const horaDestino = hora.control.value;
    const inicio = fechaDestino === fechaOriginal && horaDestino === horaOriginal
      ? turno.inicio : instanteDesdeSede(fechaDestino, horaDestino)?.toISOString();
    const profesionalId = profesionalFijo ? turno.profesionalId : profesional.control.value;
    const sillonId = sillon.control.value;
    if (inicio === undefined || !profesionalId || !sillonId) {
      errores.replaceChildren(alerta(doc, "Ingrese una fecha y hora válidas y elija los recursos del turno."));
      return;
    }
    enviarDesde(edicion, confirmar, errores, () => api.reprogramarTurno(turno.id, {
      versionEsperada: turno.version, inicio, profesionalId, sillonId
    }));
  });

  const dato = (etiqueta: string, valor: string) => nodo(doc, "div", {}, [
    nodo(doc, "dt", { texto: etiqueta }), nodo(doc, "dd", { texto: valor })
  ]);
  raiz.replaceChildren(encabezado(doc, "Reprogramar turno", "Elija el nuevo horario y los recursos del turno."),
    nodo(doc, "section", { clase: "card" }, [
      nodo(doc, "h2", { clase: "card-title", texto: `Turno de ${paciente(turno)}` }),
      nodo(doc, "p", { clase: "card-subtitle", texto: resumenTurno(turno, catalogos) }),
      nodo(doc, "dl", { clase: "turno-datos" }, [
        dato("Prestación", codigo(catalogos.prestaciones, turno.prestacionId)),
        dato("Duración", `${turno.duracionMinutos} minutos`)
      ]),
      form
    ]));
  buscarElemento(raiz, (elemento) => elemento.tagName === "H1")?.focus();
}

export function mostrarCancelacion(edicion: Edicion): void {
  const { contexto, turno, catalogos } = edicion;
  const { doc, raiz, api } = contexto;
  const dialogo = nodo(doc, "dialog", { clase: "confirmar-cancelacion", atributos: {
    "aria-labelledby": "confirmar-cancelacion-titulo", "aria-describedby": "confirmar-cancelacion-descripcion"
  } });
  dialogo.addEventListener("close", () => edicion.volver(), { once: true });
  const mantener = boton(doc, "Mantener turno", "btn-secondary", () => dialogo.close());
  mantener.setAttribute("autofocus", "");
  dialogo.addEventListener("cancel", (evento) => { if (mantener.disabled) evento.preventDefault(); });
  const errores = nodo(doc, "div", { clase: "errores-edicion" });
  const estado = nodo(doc, "p", { clase: "hint", atributos: { role: "status", tabindex: "-1" } });
  const confirmar = boton(doc, "Confirmar cancelación", "btn-danger", () => {
    enviarDesde(edicion, confirmar, errores, () => api.cancelarTurno(turno.id, turno.version), (enCurso) => {
      mantener.disabled = enCurso;
      estado.textContent = enCurso ? "Cancelando turno…" : "";
      if (enCurso) estado.focus();
    });
  });
  dialogo.append(nodo(doc, "div", { clase: "stack" }, [
    nodo(doc, "h2", { clase: "card-title", texto: `¿Cancelar el turno de ${paciente(turno)}?`,
      atributos: { id: "confirmar-cancelacion-titulo" } }),
    nodo(doc, "p", { texto: "El turno quedará registrado como cancelado y liberará los recursos que ocupa.",
      atributos: { id: "confirmar-cancelacion-descripcion" } }),
    nodo(doc, "p", { clase: "card-subtitle", texto: resumenTurno(turno, catalogos) }),
    errores,
    estado,
    nodo(doc, "div", { clase: "header-actions confirmar-acciones" }, [mantener, confirmar])
  ]));
  raiz.append(dialogo);
  dialogo.showModal();
  mantener.focus();
}
