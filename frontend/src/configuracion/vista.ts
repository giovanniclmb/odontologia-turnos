import { nodo } from "../dom/nodo.js";
import { entrada } from "../dom/campos.js";
import { aviso, cargando, encabezado, falloConReintento } from "../agenda/comunes.js";
import type { Contexto } from "../agenda/contexto.js";
import { formularioUnico } from "../agenda/formulario.js";
import { mensajeDeError } from "../agenda/mensajes.js";

const ENTERO = /^\d+$/;
const MAXIMO_ENTERO = 2_147_483_647;
const INVALIDO = "Ingrese un número entero mayor que cero. Se conserva el valor vigente.";
const AYUDA = "Número entero mayor que cero.";

function tarjeta(contexto: Contexto, inicial: number | null): HTMLElement {
  const { doc, api, usuario } = contexto;
  const vigente = nodo(doc, "p", { clase: "current-value" });
  const sinConfigurar = aviso(doc, "No se pueden crear turnos hasta configurar el intervalo.");
  const zonaAviso = nodo(doc, "div");
  const mostrarVigente = (minutos: number | null) => {
    vigente.textContent = minutos === null ? "Sin configurar" : `Vigente: ${minutos} ${minutos === 1 ? "minuto" : "minutos"}`;
    zonaAviso.replaceChildren(...(minutos === null ? [sinConfigurar] : []));
  };
  mostrarVigente(inicial);
  const resultado = nodo(doc, "p", { clase: "mensaje", atributos: { role: "status" } });
  const minutos = entrada(doc, "intervalo-minutos", "Minutos *", "text", "", AYUDA);
  minutos.control.setAttribute("inputmode", "numeric");
  const marcarCampo = (valido: boolean) => {
    minutos.control.setAttribute("aria-invalid", String(!valido));
    minutos.control.className = valido ? "input" : "input is-error";
    minutos.ayuda.className = valido ? "hint" : "field-error";
    minutos.ayuda.textContent = valido ? AYUDA : INVALIDO;
    if (valido) minutos.ayuda.removeAttribute("role");
    else minutos.ayuda.setAttribute("role", "alert");
  };
  const formulario = formularioUnico(doc, {
    campos: [minutos.contenedor],
    textoConfirmar: "Guardar intervalo",
    traducir: (error) => mensajeDeError(error, usuario.rol),
    enviar: () => {
      resultado.textContent = "";
      const texto = minutos.control.value.trim();
      const valor = Number(texto);
      const valido = ENTERO.test(texto) && valor > 0 && valor <= MAXIMO_ENTERO;
      marcarCampo(valido);
      if (!valido) return null;
      return async () => {
        mostrarVigente(await api.establecerIntervalo(valor));
        minutos.control.value = "";
        resultado.textContent = "Intervalo guardado.";
      };
    }
  });
  return nodo(doc, "section", { clase: "card" }, [
    nodo(doc, "div", { clase: "card-header" }, [nodo(doc, "div", {}, [
      nodo(doc, "h2", { clase: "card-title", texto: "Intervalo entre turnos" }),
      nodo(doc, "p", { clase: "card-subtitle",
        texto: "Se aplica antes y después, a profesional y sillón. El cambio rige para las próximas validaciones." })
    ])]),
    vigente, zonaAviso, formulario.nodo, resultado
  ]);
}

export async function montarConfiguracion(contexto: Contexto): Promise<void> {
  const { doc, raiz, api, usuario } = contexto;
  const cabecera = () => encabezado(doc, "Configuración", "Reglas comunes de la sede.");
  raiz.replaceChildren(cabecera(), cargando(doc, "Cargando configuración…"));
  try {
    const intervalo = await api.intervalo();
    if (!contexto.vigente()) return;
    raiz.replaceChildren(cabecera(), tarjeta(contexto, intervalo));
  } catch (error) {
    if (!contexto.vigente()) return;
    raiz.replaceChildren(cabecera(), falloConReintento(doc, mensajeDeError(error, usuario.rol),
      () => void montarConfiguracion(contexto)));
  }
}
