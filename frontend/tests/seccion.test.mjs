import assert from "node:assert/strict";
import { test } from "node:test";
import { crearSeccion } from "../dist/agenda/seccion.js";
import { documentoFalso, esperar, Nodo, porId, todos } from "./soporte/dom-falso.mjs";
import { apiFalsa, pendiente, SILLON } from "./soporte/contexto.mjs";

const agenda = (fecha = "2026-11-25") => ({ seccion: "agenda", vista: "dia", fecha, sillonId: SILLON });

function crear(api = apiFalsa().api) {
  const estado = { activo: null };
  const raiz = new Nodo("main");
  const seccion = crearSeccion({
    doc: documentoFalso(), raiz, api, usuario: { rol: "recepcion", profesionalRef: null },
    hoy: () => "2026-11-25", navegar() {}, reemplazar() {}, elementoActivo: () => estado.activo
  });
  const titulo = () => todos(raiz, (n) => n.tag === "h1")[0];
  return { seccion, raiz, estado, titulo };
}

test("monta cada sección y lleva el foco al título al entrar", async () => {
  const { seccion, titulo } = crear();
  await seccion.mostrar(agenda());
  assert.equal(titulo().textContent, "Agenda por sillón");
  assert.equal(titulo().enfocado, true);
  await seccion.mostrar({ seccion: "configuracion" });
  assert.equal(titulo().textContent, "Configuración");
  assert.equal(titulo().enfocado, true);
});

test("dentro de la misma sección devuelve el foco al control usado", async () => {
  const { seccion, raiz, estado, titulo } = crear();
  await seccion.mostrar(agenda());
  estado.activo = "agenda-siguiente";
  await seccion.mostrar(agenda("2026-11-26"));
  assert.equal(porId(raiz, "agenda-siguiente").enfocado, true);
  assert.equal(titulo().enfocado, false);
});

test("descarta la respuesta de una navegación anterior", async () => {
  const lenta = pendiente();
  let consultas = 0;
  const { seccion, titulo } = crear(apiFalsa({ turnos: () => (++consultas === 1 ? lenta.promesa : Promise.resolve([])) }).api);
  const primera = seccion.mostrar(agenda());
  await esperar();
  await seccion.mostrar({ seccion: "configuracion" });
  lenta.resolver([]);
  await primera;
  assert.equal(titulo().textContent, "Configuración");
});

test("al cerrar vacía la sección y descarta lo pendiente", async () => {
  const lenta = pendiente();
  const { seccion, raiz } = crear(apiFalsa({ turnos: () => lenta.promesa }).api);
  const montaje = seccion.mostrar(agenda());
  await esperar();
  seccion.cerrar();
  lenta.resolver([]);
  await montaje;
  assert.equal(raiz.textContent, "");
});
