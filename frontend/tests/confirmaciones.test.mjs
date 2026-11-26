import assert from "node:assert/strict";
import { test } from "node:test";
import { leerRuta } from "../dist/agenda/ruta.js";
import { montarConfirmaciones } from "../dist/agenda/confirmaciones.js";
import { crearSeccion } from "../dist/agenda/seccion.js";
import { apiFalsa, contexto, pendiente, rechazo, SILLON } from "./soporte/contexto.mjs";
import { alertas, boton, documentoFalso, esperar, Nodo, porId, todos } from "./soporte/dom-falso.mjs";

const ruta = { seccion: "agenda/confirmaciones", vista: "dia", fecha: "2026-11-25", sillonId: SILLON };
const turno = { id: "t1", pacienteNombre: "Ana", pacienteApellido: "Ficticia", profesionalId: "p1", sillonId: SILLON, prestacionId: "r1", inicio: "2026-11-27T02:15:00Z", fin: "2026-11-27T02:45:00Z", duracionMinutos: 30, estado: "PENDIENTE", version: "v1" };

test("solo recepción puede abrir el seguimiento y ver los datos en hora de Mendoza", async () => {
  assert.equal(leerRuta("#agenda/confirmaciones", "recepcion", "2026-11-25").seccion, "agenda/confirmaciones");
  assert.equal(leerRuta("#agenda/confirmaciones", "odontologo", "2026-11-25").seccion, "agenda");
  const api = apiFalsa({ pendientes: async () => [turno] });
  const ctx = contexto({ api: api.api });
  await montarConfirmaciones(ctx, ruta);
  assert.match(ctx.raiz.textContent, /Ana Ficticia/);
  assert.match(ctx.raiz.textContent, /26\/11\/2026/);
  assert.match(ctx.raiz.textContent, /23:15/);
  assert.match(ctx.raiz.textContent, /PRO-01/);
  assert.match(ctx.raiz.textContent, /SIL-01/);
  assert.match(ctx.raiz.textContent, /CONTROL/);
  assert.match(boton(ctx.raiz, "Registrar confirmación").getAttribute("aria-label"), /Ana Ficticia/);
  const denegado = contexto({ api: api.api, rol: "odontologo" });
  await montarConfirmaciones(denegado, ruta);
  assert.equal(api.de("pendientes").length, 1);
});

test("conserva identificadores de recursos que no aparecen en los catálogos", async () => {
  const ctx = contexto({ api: apiFalsa({ pendientes: async () => [{ ...turno, sillonId: "fuera-catalogo", profesionalId: "ex-profesional" }] }).api });
  await montarConfirmaciones(ctx, ruta);
  assert.match(ctx.raiz.textContent, /fuera-catalogo/);
  assert.match(ctx.raiz.textContent, /ex-profesional/);
});

test("muestra carga, vacío y error con reintento", async () => {
  const carga = pendiente();
  const api = apiFalsa({ pendientes: () => carga.promesa });
  const ctx = contexto({ api: api.api });
  const montaje = montarConfirmaciones(ctx, ruta);
  assert.match(ctx.raiz.textContent, /Cargando/);
  carga.resolver([]); await montaje;
  assert.match(ctx.raiz.textContent, /No hay turnos pendientes/);
  const fallida = contexto({ api: apiFalsa({ pendientes: async () => { throw new TypeError(); } }).api });
  await montarConfirmaciones(fallida, ruta);
  assert.match(fallida.raiz.textContent, /No se pudo cargar/);
  assert.ok(boton(fallida.raiz, "Reintentar"));
});

test("registra una vez, actualiza el listado y muestra el resultado", async () => {
  let consultas = 0;
  const envio = pendiente();
  const api = apiFalsa({ pendientes: async () => ++consultas === 1 ? [turno] : [], confirmarTurno: () => envio.promesa });
  const ctx = contexto({ api: api.api });
  await montarConfirmaciones(ctx, ruta);
  const confirmar = boton(ctx.raiz, "Registrar confirmación");
  confirmar.emitir("click"); confirmar.emitir("click");
  assert.equal(api.de("confirmarTurno").length, 1);
  assert.deepEqual(api.de("confirmarTurno")[0], ["t1", "v1"]);
  envio.resolver({ ...turno, estado: "CONFIRMADO", version: "v2" });
  await esperar(); await esperar();
  assert.match(ctx.raiz.textContent, /confirmación.*registrada/i);
  assert.match(ctx.raiz.textContent, /No hay turnos pendientes/);
  assert.equal(porId(ctx.raiz, "confirmaciones-resultado").enfocado, true);
});

test("resultado incierto y versión obsoleta bloquean el reenvío hasta consultar otra vez", async () => {
  for (const fallo of [async () => { throw new TypeError("red"); }, rechazo("version_turno_desactualizada")]) {
    const api = apiFalsa({ pendientes: async () => [turno], confirmarTurno: fallo });
    const ctx = contexto({ api: api.api });
    await montarConfirmaciones(ctx, ruta);
    let control = boton(ctx.raiz, "Registrar confirmación");
    control.emitir("click"); await esperar();
    assert.equal(control.disabled, true);
    assert.match(ctx.raiz.textContent, /Actualice la lista/);
    control.emitir("click"); assert.equal(api.de("confirmarTurno").length, 1);
    boton(ctx.raiz, "Actualizar lista").emitir("click"); await esperar();
    control = boton(ctx.raiz, "Registrar confirmación");
    assert.equal(control.disabled, false);
  }
});

test("un registro guardado con consulta fallida conserva el éxito y reintenta solo la lectura", async () => {
  let consultas = 0;
  const api = apiFalsa({ pendientes: async () => { if (++consultas === 2) throw new TypeError(); return consultas === 1 ? [turno] : []; },
    confirmarTurno: async () => ({ ...turno, estado: "CONFIRMADO" }) });
  const ctx = contexto({ api: api.api });
  await montarConfirmaciones(ctx, ruta);
  boton(ctx.raiz, "Registrar confirmación").emitir("click"); await esperar(); await esperar();
  assert.match(ctx.raiz.textContent, /confirmación fue registrada/i);
  assert.match(ctx.raiz.textContent, /No se pudo cargar/);
  boton(ctx.raiz, "Reintentar").emitir("click"); await esperar();
  assert.equal(api.de("confirmarTurno").length, 1);
  assert.match(ctx.raiz.textContent, /No hay turnos pendientes/);
});

test("no escribe el resultado de un envío en una vista abandonada", async () => {
  const envio = pendiente();
  let vigente = true;
  const ctx = contexto({ api: apiFalsa({ pendientes: async () => [turno], confirmarTurno: () => envio.promesa }).api });
  ctx.vigente = () => vigente;
  await montarConfirmaciones(ctx, ruta);
  boton(ctx.raiz, "Registrar confirmación").emitir("click");
  vigente = false;
  ctx.raiz.textContent = "Otra vista";
  envio.resolver({ ...turno, estado: "CONFIRMADO" }); await esperar();
  assert.equal(ctx.raiz.textContent, "Otra vista");
});

test("un rechazo sin conflicto permite corregir o reintentar", async () => {
  const ctx = contexto({ api: apiFalsa({ pendientes: async () => [turno], confirmarTurno: rechazo("autorizacion_denegada", 403) }).api });
  await montarConfirmaciones(ctx, ruta);
  const control = boton(ctx.raiz, "Registrar confirmación");
  control.emitir("click"); await esperar();
  assert.match(ctx.raiz.textContent, /No tiene permiso/);
  assert.equal(control.disabled, false);
});

test("una consulta iniciada durante el envío no habilita el reenvío con datos anteriores", async () => {
  const envio = pendiente();
  const consulta = pendiente();
  let numero = 0;
  const api = apiFalsa({ pendientes: () => ++numero === 1 ? Promise.resolve([turno]) : numero === 2 ? consulta.promesa : Promise.resolve([]),
    confirmarTurno: () => envio.promesa });
  const ctx = contexto({ api: api.api });
  await montarConfirmaciones(ctx, ruta);
  boton(ctx.raiz, "Registrar confirmación").emitir("click");
  boton(ctx.raiz, "Actualizar lista").emitir("click");
  envio.resolver({ ...turno, estado: "CONFIRMADO" }); await esperar();
  consulta.resolver([turno]); await esperar(); await esperar();
  assert.match(ctx.raiz.textContent, /No hay turnos pendientes/);
  assert.equal(api.de("confirmarTurno").length, 1);
});

const otro = { ...turno, id: "t2", pacienteNombre: "Bruno", inicio: "2026-11-27T12:00:00Z", fin: "2026-11-27T12:30:00Z" };
const agenda = { ...ruta, seccion: "agenda" };

/** Sección real: reproduce lo que ocurre al navegar mientras hay un envío en curso. */
function conSeccion(sustituciones) {
  const api = apiFalsa(sustituciones);
  const raiz = new Nodo("main");
  const seccion = crearSeccion({ doc: documentoFalso(), raiz, api: api.api, usuario: { rol: "recepcion", profesionalRef: null },
    hoy: () => "2026-11-25", navegar() {}, reemplazar() {}, elementoActivo: () => null });
  return { api, raiz, seccion };
}
const registrar = (raiz, nombre) => todos(raiz, (n) => n.tag === "button"
  && n.textContent === "Registrar confirmación" && n.getAttribute("aria-label").includes(nombre))[0];

test("al salir de la vista, un resultado incierto no se informa como envío no completado", async () => {
  const envio = pendiente();
  const { raiz, seccion } = conSeccion({ pendientes: async () => [turno], confirmarTurno: () => envio.promesa });
  await seccion.mostrar(ruta);
  registrar(raiz, "Ana").emitir("click");
  await seccion.mostrar(agenda);
  envio.rechazar(new TypeError("red")); await esperar(); await esperar();
  assert.doesNotMatch(raiz.textContent, /no se completó/i);
  assert.match(alertas(raiz).join(" "), /No se pudo saber si se registró la confirmación de Ana Ficticia/);
});

test("muestra los fallos aunque la lista se haya redibujado durante el envío", async () => {
  const envios = [pendiente(), pendiente(), pendiente()];
  const tercero = { ...turno, id: "t3", pacienteNombre: "Carla" };
  let confirmado = false;
  let numero = 0;
  const api = apiFalsa({ pendientes: async () => confirmado ? [turno, otro] : [turno, otro, tercero],
    confirmarTurno: () => envios[numero++].promesa });
  const ctx = contexto({ api: api.api });
  await montarConfirmaciones(ctx, ruta);
  for (const nombre of ["Ana", "Bruno", "Carla"]) registrar(ctx.raiz, nombre).emitir("click");
  confirmado = true;
  envios[2].resolver({ ...tercero, estado: "CONFIRMADO" }); await esperar(); await esperar();
  envios[0].rechazar(new TypeError("red"));
  envios[1].rechazar(Object.assign(new Error("x"), { codigo: "version_turno_desactualizada", status: 409 }));
  for (let vuelta = 0; vuelta < 4; vuelta++) await esperar();
  const avisos = alertas(ctx.raiz).join(" | ");
  assert.match(avisos, /Ana Ficticia/);
  assert.match(avisos, /Bruno Ficticia/);
  assert.match(ctx.raiz.textContent, /confirmación fue registrada/i);
  assert.equal(registrar(ctx.raiz, "Ana").disabled, false);
  boton(ctx.raiz, "Actualizar lista").emitir("click"); await esperar(); await esperar();
  assert.deepEqual(alertas(ctx.raiz), []);
});

test("al volver a la vista durante el envío, refleja el éxito al terminar", async () => {
  const envio = pendiente();
  let guardado = false;
  const { raiz, seccion } = conSeccion({ pendientes: async () => guardado ? [] : [turno], confirmarTurno: () => envio.promesa });
  await seccion.mostrar(ruta);
  registrar(raiz, "Ana").emitir("click");
  await seccion.mostrar(agenda);
  await seccion.mostrar(ruta);
  guardado = true;
  envio.resolver({ ...turno, estado: "CONFIRMADO" }); await esperar(); await esperar();
  assert.match(raiz.textContent, /confirmación fue registrada/i);
  assert.match(raiz.textContent, /No hay turnos pendientes/);
});

test("al volver a la vista durante el envío, muestra el fallo y permite reintentar", async () => {
  const envio = pendiente();
  const { raiz, seccion } = conSeccion({ pendientes: async () => [turno], confirmarTurno: () => envio.promesa });
  await seccion.mostrar(ruta);
  registrar(raiz, "Ana").emitir("click");
  await seccion.mostrar(agenda);
  await seccion.mostrar(ruta);
  envio.rechazar(new TypeError("red")); for (let vuelta = 0; vuelta < 3; vuelta++) await esperar();
  assert.match(alertas(raiz).join(" "), /No se pudo saber si se registró la confirmación de Ana Ficticia/);
  assert.equal(registrar(raiz, "Ana").disabled, false);
});

test("explica que hay otra edición en curso sin trabar el botón", async () => {
  const api = apiFalsa({ pendientes: async () => [turno] });
  const ctx = contexto({ api: api.api });
  ctx.enviar = () => null;
  await montarConfirmaciones(ctx, ruta);
  const control = registrar(ctx.raiz, "Ana");
  control.emitir("click");
  assert.match(alertas(ctx.raiz).join(" "), /Hay una edición en curso para este turno/);
  assert.equal(control.disabled, false);
  assert.equal(api.de("confirmarTurno").length, 0);
});

test("redacta los rechazos con una sola indicación y puntuada", async () => {
  const casos = [
    [rechazo("version_turno_desactualizada"), "El turno cambió desde la última consulta. Actualice la lista antes de volver a intentarlo."],
    [rechazo("solicitud_invalida", 400, "El turno ya alcanzó su inicio"), "El turno ya alcanzó su inicio. Actualice la lista antes de volver a intentarlo."]
  ];
  for (const [confirmarTurno, esperado] of casos) {
    const ctx = contexto({ api: apiFalsa({ pendientes: async () => [{ ...turno, id: esperado }], confirmarTurno }).api });
    await montarConfirmaciones(ctx, ruta);
    registrar(ctx.raiz, "Ana").emitir("click"); await esperar();
    assert.deepEqual(alertas(ctx.raiz), [esperado]);
  }
});

test("conserva el foco al actualizar, reintentar y fallar un envío", async () => {
  let fallar = false;
  const ctx = contexto({ api: apiFalsa({ pendientes: async () => { if (fallar) throw new TypeError(); return [{ ...turno, id: "foco" }]; },
    confirmarTurno: rechazo("autorizacion_denegada", 403) }).api });
  await montarConfirmaciones(ctx, ruta);
  registrar(ctx.raiz, "Ana").emitir("click"); await esperar();
  assert.equal(todos(ctx.raiz, (n) => n.getAttribute("role") === "alert")[0].enfocado, true);
  boton(ctx.raiz, "Actualizar lista").emitir("click"); await esperar(); await esperar();
  assert.equal(boton(ctx.raiz, "Actualizar lista").enfocado, true);
  fallar = true;
  boton(ctx.raiz, "Actualizar lista").emitir("click"); await esperar(); await esperar();
  assert.equal(boton(ctx.raiz, "Reintentar").enfocado, true);
});
