import assert from "node:assert/strict";
import { test } from "node:test";
import { crearSeccion } from "../dist/agenda/seccion.js";
import { alertas, documentoFalso, esperar, formulario, Nodo, porId, todos } from "./soporte/dom-falso.mjs";
import { apiFalsa, pendiente, SILLON } from "./soporte/contexto.mjs";

const agenda = (fecha = "2026-11-25") => ({ seccion: "agenda", vista: "dia", fecha, sillonId: SILLON });

function crear(api = apiFalsa().api, navegaciones = []) {
  const estado = { activo: null };
  const raiz = new Nodo("main");
  const seccion = crearSeccion({
    doc: documentoFalso(), raiz, api, usuario: { rol: "recepcion", profesionalRef: null },
    hoy: () => "2026-11-25", navegar: (ruta) => navegaciones.push(ruta), reemplazar() {}, elementoActivo: () => estado.activo
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

const bloqueo = { seccion: "agenda/bloqueo", vista: "dia", fecha: "2026-11-25", sillonId: SILLON };

function enviarBloqueo(raiz) {
  porId(raiz, "bloqueo-desde-hora").value = "09:00";
  porId(raiz, "bloqueo-hasta-hora").value = "10:00";
  formulario(raiz).emitir("submit");
}

test("volver a abrir el formulario no permite repetir un envío en curso", async () => {
  const espera = pendiente();
  const falsa = apiFalsa({ crearBloqueo: () => espera.promesa });
  const { seccion, raiz } = crear(falsa.api);
  await seccion.mostrar(bloqueo);
  enviarBloqueo(raiz);
  await seccion.mostrar(agenda());
  await seccion.mostrar(bloqueo);
  enviarBloqueo(raiz);
  await esperar();
  assert.equal(falsa.de("crearBloqueo").length, 1);
  assert.deepEqual(alertas(raiz), ["Hay un envío en curso. Espere su resultado antes de enviar otro."]);
  espera.resolver();
  await esperar();
  enviarBloqueo(raiz);
  await esperar();
  assert.equal(falsa.de("crearBloqueo").length, 2);
});

test("un envío que termina fuera de su formulario actualiza la agenda a la vista", async () => {
  const espera = pendiente();
  const falsa = apiFalsa({ crearBloqueo: () => espera.promesa });
  const { seccion, raiz } = crear(falsa.api);
  await seccion.mostrar(bloqueo);
  enviarBloqueo(raiz);
  await seccion.mostrar(agenda());
  const consultas = falsa.de("turnos").length;
  espera.resolver();
  await esperar();
  await esperar();
  assert.equal(falsa.de("turnos").length, consultas + 1);
});

test("un envío que falla fuera de su formulario se informa en la vista vigente", async () => {
  let fallar;
  const falsa = apiFalsa({ crearBloqueo: () => new Promise((_, reject) => { fallar = reject; }) });
  const { seccion, raiz } = crear(falsa.api);
  await seccion.mostrar(bloqueo);
  enviarBloqueo(raiz);
  await seccion.mostrar(agenda());
  fallar(Object.assign(new Error("x"), { codigo: "bloqueo_afecta_turno", status: 409 }));
  await esperar();
  assert.deepEqual(alertas(raiz), ["El envío anterior no se completó. El bloqueo afecta a un turno existente. No se creó."]);
  assert.ok(raiz.textContent.includes("Agenda por sillón"));
});

test("después de cerrar, un envío tardío no navega ni escribe en la pantalla", async () => {
  const espera = pendiente();
  const navegaciones = [];
  const { seccion, raiz } = crear(apiFalsa({ crearBloqueo: () => espera.promesa }).api, navegaciones);
  await seccion.mostrar(bloqueo);
  enviarBloqueo(raiz);
  seccion.cerrar();
  espera.resolver();
  await esperar();
  assert.deepEqual(navegaciones, []);
  assert.equal(raiz.textContent, "");
});
