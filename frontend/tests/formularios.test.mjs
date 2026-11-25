import assert from "node:assert/strict";
import { test } from "node:test";
import { montarAltaTurno } from "../dist/agenda/formulario-turno.js";
import { montarAltaBloqueo } from "../dist/agenda/formulario-bloqueo.js";
import { montarConfiguracion } from "../dist/configuracion/vista.js";
import { alertas, boton, esperar, formulario, porId } from "./soporte/dom-falso.mjs";
import { apiFalsa, contexto, pendiente, rechazo, SILLON } from "./soporte/contexto.mjs";

const ruta = (seccion) => ({ seccion, vista: "dia", fecha: "2026-11-26", sillonId: SILLON });
const escribir = (ctx, valores) => {
  for (const [id, valor] of Object.entries(valores)) {
    const campo = porId(ctx.raiz, id);
    campo.value = valor;
    campo.emitir("change");
  }
};
const enviar = async (ctx) => { formulario(ctx.raiz).emitir("submit"); await esperar(); };

async function altaCompleta(opciones = {}) {
  const falsa = apiFalsa(opciones.api);
  const ctx = contexto({ api: falsa.api, rol: opciones.rol, profesionalRef: opciones.profesionalRef });
  await montarAltaTurno(ctx, ruta("agenda/nuevo"));
  escribir(ctx, { "turno-buscar": "Ficticia" });
  boton(ctx.raiz, "Buscar").emitir("click");
  await esperar();
  escribir(ctx, { "turno-paciente": "c1", "turno-prestacion": "r1", "turno-hora": "09:00" });
  if (opciones.rol !== "odontologo") escribir(ctx, { "turno-profesional": "p1" });
  return { ctx, ...falsa };
}

test("crea el turno con la hora de Mendoza y vuelve a la agenda de ese día", async () => {
  const { ctx, de } = await altaCompleta();
  await enviar(ctx);
  assert.deepEqual(de("crearTurno"), [[{ pacienteId: "c1", profesionalId: "p1", sillonId: SILLON, prestacionId: "r1", inicio: "2026-11-26T12:00:00.000Z", duracionAjustadaMinutos: 30 }]]);
  assert.deepEqual(ctx.navegaciones, [ruta("agenda")]);
});

test("propone la duración de referencia y envía el ajuste", async () => {
  const { ctx, de } = await altaCompleta();
  assert.equal(porId(ctx.raiz, "turno-duracion").value, "30");
  escribir(ctx, { "turno-prestacion": "r2" });
  assert.equal(porId(ctx.raiz, "turno-duracion").value, "60");
  escribir(ctx, { "turno-duracion": "45" });
  await enviar(ctx);
  assert.equal(de("crearTurno")[0][0].duracionAjustadaMinutos, 45);
});

test("ante un rechazo muestra el motivo y conserva lo ingresado", async () => {
  const { ctx } = await altaCompleta({ api: { crearTurno: rechazo("horario_no_disponible") } });
  await enviar(ctx);
  assert.deepEqual(alertas(ctx.raiz), ["El horario solicitado no está disponible. Revise profesional, sillón u horario."]);
  assert.equal(porId(ctx.raiz, "turno-paciente").value, "c1");
  assert.equal(porId(ctx.raiz, "turno-hora").value, "09:00");
  assert.equal(boton(ctx.raiz, "Crear turno").disabled, false);
  assert.deepEqual(ctx.navegaciones, []);
});

test("impide reenviar un alta en curso", async () => {
  const espera = pendiente();
  const { ctx, de } = await altaCompleta({ api: { crearTurno: () => espera.promesa } });
  await enviar(ctx);
  assert.equal(boton(ctx.raiz, "Crear turno").disabled, true);
  await enviar(ctx);
  assert.equal(de("crearTurno").length, 1);
  espera.resolver();
});

test("no envía datos incompletos ni duraciones que no sean enteros positivos", async () => {
  for (const cambio of [{ "turno-paciente": "" }, { "turno-hora": "" }, { "turno-duracion": "0" }, { "turno-duracion": "7.5" }]) {
    const { ctx, de } = await altaCompleta();
    escribir(ctx, cambio);
    await enviar(ctx);
    assert.equal(de("crearTurno").length, 0, JSON.stringify(cambio));
    assert.equal(alertas(ctx.raiz).length, 1);
  }
});

test("odontología opera solo con su profesional", async () => {
  const { ctx, de } = await altaCompleta({ rol: "odontologo", profesionalRef: "p2" });
  const profesional = porId(ctx.raiz, "turno-profesional");
  assert.equal(profesional.readOnly, true);
  assert.equal(profesional.value, "PRO-02");
  await enviar(ctx);
  assert.equal(de("crearTurno")[0][0].profesionalId, "p2");
});

test("informa los catálogos que faltan y no permite confirmar", async () => {
  const { api, de } = apiFalsa({ prestaciones: async () => [{ id: "r1", codigo: "SIN", duracionReferenciaMinutos: null }], profesionales: async () => [] });
  const ctx = contexto({ api });
  await montarAltaTurno(ctx, ruta("agenda/nuevo"));
  assert.ok(ctx.raiz.textContent.includes("Faltan datos para crear turnos: profesionales activos, prestaciones con duración."));
  assert.equal(boton(ctx.raiz, "Crear turno").disabled, true);
  await enviar(ctx);
  assert.equal(de("crearTurno").length, 0);
});

test("crea un bloqueo de un solo recurso en hora de Mendoza", async () => {
  const { api, de } = apiFalsa();
  const ctx = contexto({ api });
  await montarAltaBloqueo(ctx, ruta("agenda/bloqueo"));
  escribir(ctx, { "bloqueo-desde-hora": "09:00", "bloqueo-hasta-hora": "10:00" });
  await enviar(ctx);
  assert.deepEqual(de("crearBloqueo"), [[{ sillonId: SILLON, profesionalId: null, inicio: "2026-11-26T12:00:00.000Z", fin: "2026-11-26T13:00:00.000Z" }]]);
  assert.deepEqual(ctx.navegaciones, [ruta("agenda")]);
});

test("el bloqueo exige un recurso y un fin posterior", async () => {
  for (const cambio of [{ "bloqueo-sillon": "" }, { "bloqueo-hasta-hora": "09:00" }, { "bloqueo-desde-hora": "" }]) {
    const { api, de } = apiFalsa();
    const ctx = contexto({ api });
    await montarAltaBloqueo(ctx, ruta("agenda/bloqueo"));
    escribir(ctx, { "bloqueo-desde-hora": "09:00", "bloqueo-hasta-hora": "10:00" });
    escribir(ctx, cambio);
    await enviar(ctx);
    assert.equal(de("crearBloqueo").length, 0, JSON.stringify(cambio));
    assert.equal(alertas(ctx.raiz).length, 1);
  }
});

test("el bloqueo rechazado conserva los datos y no se envía dos veces", async () => {
  const espera = pendiente();
  let intentos = 0;
  const { api, de } = apiFalsa({ crearBloqueo: async () => { intentos++; await espera.promesa; await rechazo("bloqueo_afecta_turno")(); } });
  const ctx = contexto({ api });
  await montarAltaBloqueo(ctx, ruta("agenda/bloqueo"));
  escribir(ctx, { "bloqueo-desde-hora": "09:00", "bloqueo-hasta-hora": "10:00" });
  await enviar(ctx);
  await enviar(ctx);
  espera.resolver();
  await esperar();
  assert.equal(de("crearBloqueo").length, 1);
  assert.deepEqual(alertas(ctx.raiz), ["El bloqueo afecta a un turno existente. No se creó."]);
  assert.equal(porId(ctx.raiz, "bloqueo-desde-hora").value, "09:00");
});

test("la configuración sin valor lo informa con el campo vacío y guarda un entero", async () => {
  const { api, de } = apiFalsa({ intervalo: async () => null });
  const ctx = contexto({ api });
  await montarConfiguracion(ctx);
  assert.ok(ctx.raiz.textContent.includes("Sin configurar"));
  assert.equal(porId(ctx.raiz, "intervalo-minutos").value, "");
  escribir(ctx, { "intervalo-minutos": "15" });
  await enviar(ctx);
  assert.deepEqual(de("establecerIntervalo"), [[15]]);
  assert.ok(ctx.raiz.textContent.includes("Vigente: 15 minutos"));
  assert.ok(ctx.raiz.textContent.includes("Intervalo guardado."));
});

test("la configuración rechaza valores inválidos y conserva el vigente", async () => {
  for (const valor of ["", "0", "-3", "7,5", "7.5", "1e3", "99999999999"]) {
    const { api, de } = apiFalsa();
    const ctx = contexto({ api });
    await montarConfiguracion(ctx);
    escribir(ctx, { "intervalo-minutos": valor });
    await enviar(ctx);
    assert.equal(de("establecerIntervalo").length, 0, valor);
    assert.ok(ctx.raiz.textContent.includes("Vigente: 10 minutos"));
    assert.equal(porId(ctx.raiz, "intervalo-minutos").getAttribute("aria-invalid"), "true");
  }
});

test("la configuración permite reintentar si no puede consultar", async () => {
  let intentos = 0;
  const ctx = contexto({ api: apiFalsa({ intervalo: async () => { if (++intentos === 1) throw new TypeError("sin red"); return 20; } }).api });
  await montarConfiguracion(ctx);
  boton(ctx.raiz, "Reintentar").emitir("click");
  await esperar();
  assert.ok(ctx.raiz.textContent.includes("Vigente: 20 minutos"));
});

test("el error del intervalo se muestra asociado al campo y se limpia al corregirlo", async () => {
  const ctx = contexto();
  await montarConfiguracion(ctx);
  escribir(ctx, { "intervalo-minutos": "7,5" });
  await enviar(ctx);
  const ayuda = porId(ctx.raiz, "intervalo-minutos-ayuda");
  assert.equal(ayuda.textContent, "Ingrese un número entero mayor que cero. Se conserva el valor vigente.");
  assert.equal(ayuda.className, "field-error");
  assert.deepEqual(alertas(ctx.raiz), [ayuda.textContent]);
  escribir(ctx, { "intervalo-minutos": "20" });
  await enviar(ctx);
  assert.equal(ayuda.textContent, "Número entero mayor que cero.");
  assert.equal(ayuda.className, "hint");
  assert.deepEqual(alertas(ctx.raiz), []);
});

test("una búsqueda correcta limpia el error de la anterior", async () => {
  let intentos = 0;
  const { api } = apiFalsa({ buscarPacientes: async () => { if (++intentos === 1) throw new TypeError("sin red"); return []; } });
  const ctx = contexto({ api });
  await montarAltaTurno(ctx, ruta("agenda/nuevo"));
  escribir(ctx, { "turno-buscar": "Ana" });
  boton(ctx.raiz, "Buscar").emitir("click");
  await esperar();
  assert.equal(alertas(ctx.raiz).length, 1);
  boton(ctx.raiz, "Buscar").emitir("click");
  await esperar();
  assert.deepEqual(alertas(ctx.raiz), []);
});
