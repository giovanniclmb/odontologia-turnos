import { boton, nodo } from "../dom/nodo.js";
import { cargarOpciones, entrada, seleccion, type Campo } from "../dom/campos.js";
import type { Profesional } from "../api/types.js";
import { instanteDesdeSede } from "../tiempo/mendoza.js";
import { aviso, cargando, encabezado, falloConReintento } from "./comunes.js";
import type { Contexto } from "./contexto.js";
import { formularioUnico, type Formulario } from "./formulario.js";
import { mensajeDeError } from "./mensajes.js";
import type { RutaAgenda } from "./ruta.js";
import type { Prestacion, Sillon } from "./tipos.js";

interface CatalogosAlta {
  sillones: Sillon[];
  profesionales: Profesional[];
  prestaciones: Prestacion[];
}

const ENTERO_POSITIVO = /^[1-9]\d{0,8}$/;
const elegir = { valor: "", texto: "Seleccione…" };
const opcionesDe = (catalogo: readonly { id: string; codigo: string }[]) =>
  [elegir, ...catalogo.map((elemento) => ({ valor: elemento.id, texto: elemento.codigo }))];

function cabecera(contexto: Contexto, ruta: RutaAgenda): HTMLElement {
  return encabezado(contexto.doc, "Nuevo turno", "El turno se crea como pendiente de confirmación.",
    [boton(contexto.doc, "Volver a la agenda", "btn-secondary", () => contexto.navegar({ ...ruta, seccion: "agenda" }))]);
}

function buscadorDePacientes(contexto: Contexto, mostrarError: (texto: string) => void) {
  const { doc, api, usuario } = contexto;
  const texto = entrada(doc, "turno-buscar", "Buscar paciente", "search", "", usuario.rol === "recepcion"
    ? "Busque por código exacto, o por parte del nombre o del apellido."
    : "Solo pacientes asignados a su profesional.");
  const paciente = seleccion(doc, "turno-paciente", "Paciente *", [{ valor: "", texto: "Busque un paciente" }]);
  let busqueda = 0;
  const buscar = async () => {
    const consulta = texto.control.value.trim();
    if (consulta === "") {
      mostrarError("Ingrese un código, nombre o apellido para buscar.");
      return;
    }
    const propia = ++busqueda;
    paciente.ayuda.textContent = "Buscando…";
    try {
      const encontrados = await api.buscarPacientes(consulta);
      if (propia !== busqueda || !contexto.vigente()) return;
      cargarOpciones(doc, paciente.control, [
        { valor: "", texto: encontrados.length === 0 ? "Sin coincidencias" : "Seleccione un paciente" },
        ...encontrados.map((p) => ({ valor: p.id, texto: `${p.nombre} ${p.apellido} · ${p.codigo}` }))
      ], paciente.control.value);
      paciente.ayuda.textContent = `${encontrados.length} coincidencia(s).`;
    } catch (error) {
      if (propia !== busqueda || !contexto.vigente()) return;
      paciente.ayuda.textContent = "";
      mostrarError(mensajeDeError(error, usuario.rol));
    }
  };
  texto.control.addEventListener("keydown", (evento) => {
    if (evento.key !== "Enter") return;
    evento.preventDefault();
    void buscar();
  });
  texto.contenedor.className = "field span-2";
  texto.contenedor.replaceChildren(texto.rotulo, nodo(doc, "div", { clase: "inline-search" }, [
    texto.control, boton(doc, "Buscar", "btn-secondary", () => void buscar())
  ]), texto.ayuda);
  paciente.contenedor.className = "field span-2";
  return { nodos: [texto.contenedor, paciente.contenedor], paciente };
}

function campoProfesional(contexto: Contexto, activos: readonly Profesional[]) {
  const { doc, usuario } = contexto;
  if (usuario.rol === "recepcion") {
    const campo = seleccion(doc, "turno-profesional", "Profesional *", opcionesDe(activos));
    return { campo, valor: () => campo.control.value, disponible: activos.length > 0 };
  }
  const propio = activos.find((p) => p.id === usuario.profesionalRef);
  const campo: Campo<HTMLInputElement> = entrada(doc, "turno-profesional", "Profesional", "text",
    propio?.codigo ?? "", "Su profesional vinculado.");
  campo.control.readOnly = true;
  return { campo, valor: () => propio?.id ?? "", disponible: propio !== undefined };
}

function construir(contexto: Contexto, ruta: RutaAgenda, catalogos: CatalogosAlta): HTMLElement[] {
  const { doc, api, usuario } = contexto;
  const sillones = catalogos.sillones.filter((s) => s.activo);
  const prestaciones = catalogos.prestaciones.filter((p) => p.duracionReferenciaMinutos !== null);
  let formulario: Formulario | null = null;
  const buscador = buscadorDePacientes(contexto, (texto) => formulario?.error.mostrar(texto));
  const profesional = campoProfesional(contexto, catalogos.profesionales.filter((p) => p.activo));
  const sillon = seleccion(doc, "turno-sillon", "Sillón *", opcionesDe(sillones), ruta.sillonId ?? "");
  const prestacion = seleccion(doc, "turno-prestacion", "Prestación *", opcionesDe(prestaciones));
  const duracion = entrada(doc, "turno-duracion", "Duración (minutos) *", "text", "", "Elija una prestación.");
  duracion.control.setAttribute("inputmode", "numeric");
  prestacion.control.addEventListener("change", () => {
    const referencia = prestaciones.find((p) => p.id === prestacion.control.value)?.duracionReferenciaMinutos;
    duracion.control.value = referencia == null ? "" : String(referencia);
    duracion.ayuda.textContent = referencia == null
      ? "Elija una prestación." : `Referencia de la prestación: ${referencia}. Puede ajustarla.`;
  });
  const fecha = entrada(doc, "turno-fecha", "Fecha *", "date", ruta.fecha);
  const hora = entrada(doc, "turno-hora", "Hora de inicio (Mendoza) *", "time");

  formulario = formularioUnico(doc, {
    campos: [...buscador.nodos, profesional.campo.contenedor, sillon.contenedor, prestacion.contenedor,
      duracion.contenedor, fecha.contenedor, hora.contenedor],
    textoConfirmar: "Crear turno",
    alCancelar: () => contexto.navegar({ ...ruta, seccion: "agenda" }),
    traducir: (error) => mensajeDeError(error, usuario.rol),
    enviar: () => {
      const pacienteId = buscador.paciente.control.value;
      const profesionalId = profesional.valor();
      const sillonId = sillon.control.value;
      const prestacionId = prestacion.control.value;
      const inicio = instanteDesdeSede(fecha.control.value, hora.control.value);
      if (!pacienteId || !profesionalId || !sillonId || !prestacionId || inicio === null) {
        return "Complete paciente, profesional, sillón, prestación, fecha y hora.";
      }
      const minutos = duracion.control.value.trim();
      if (!ENTERO_POSITIVO.test(minutos)) return "La duración debe ser un número entero mayor que cero.";
      const dia = fecha.control.value;
      return async () => {
        await api.crearTurno({ pacienteId, profesionalId, sillonId, prestacionId,
          inicio: inicio.toISOString(), duracionAjustadaMinutos: Number(minutos) });
        if (contexto.vigente()) contexto.navegar({ ...ruta, seccion: "agenda", fecha: dia, sillonId });
      };
    }
  });

  const faltantes = [
    ...(profesional.disponible ? [] : [usuario.rol === "recepcion" ? "profesionales activos" : "un profesional activo vinculado a su usuario"]),
    ...(sillones.length > 0 ? [] : ["sillones activos"]),
    ...(prestaciones.length > 0 ? [] : ["prestaciones con duración"])
  ];
  formulario.confirmar.disabled = faltantes.length > 0;
  const avisos = faltantes.length === 0 ? [] : [aviso(doc, `Faltan datos para crear turnos: ${faltantes.join(", ")}.`)];
  return [...avisos, nodo(doc, "section", { clase: "card" }, [formulario.nodo])];
}

export async function montarAltaTurno(contexto: Contexto, ruta: RutaAgenda): Promise<void> {
  const { doc, raiz, api, usuario } = contexto;
  raiz.replaceChildren(cabecera(contexto, ruta), cargando(doc, "Cargando datos del turno…"));
  try {
    const [sillones, profesionales, prestaciones] = await Promise.all(
      [api.sillones(), api.profesionales(), api.prestaciones()]);
    if (!contexto.vigente()) return;
    raiz.replaceChildren(cabecera(contexto, ruta), ...construir(contexto, ruta, { sillones, profesionales, prestaciones }));
  } catch (error) {
    if (!contexto.vigente()) return;
    raiz.replaceChildren(cabecera(contexto, ruta), falloConReintento(doc, mensajeDeError(error, usuario.rol),
      () => void montarAltaTurno(contexto, ruta)));
  }
}
