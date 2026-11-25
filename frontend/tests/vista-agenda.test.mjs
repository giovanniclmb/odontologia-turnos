import assert from "node:assert/strict";
import { test } from "node:test";
import { montarAgenda } from "../dist/agenda/vista-agenda.js";
import { alertas, boton, esperar, porId } from "./soporte/dom-falso.mjs";
import { apiFalsa, contexto, pendiente, rechazo, SILLON, OTRO_SILLON } from "./soporte/contexto.mjs";

const ruta = (cambios = {}) => ({ seccion: "agenda", vista: "dia", fecha: "2026-11-25", sillonId: SILLON, ...cambios });
const turno = (cambios = {}) => ({
  id: "t1", pacienteNombre: "Ana", pacienteApellido: "Ficticia", profesionalId: "p1", sillonId: SILLON, prestacionId: "r1",
  inicio: "2026-11-25T12:00:00Z", fin: "2026-11-25T12:30:00Z", duracionMinutos: 30, estado: "PENDIENTE", version: "0", ...cambios
});

test("muestra turnos y bloqueos del período y sillón elegidos como texto inerte", async () => {
  const { api, de } = apiFalsa({
    turnos: async () => [turno({ pacienteNombre: "<img src=x onerror=alert(1)>" }), turno({ id: "t2", estado: "CANCELADO", inicio: "2026-11-25T14:00:00Z", fin: "2026-11-25T15:00:00Z" })],
    bloqueos: async () => [{ id: "b1", inicio: "2026-11-25T16:00:00Z", fin: "2026-11-25T17:00:00Z", sillonId: SILLON, profesionalId: "p2" }]
  });
  const ctx = contexto({ api });
  await montarAgenda(ctx, ruta());
  const texto = ctx.raiz.textContent;
  for (const esperado of ["<img src=x onerror=alert(1)> Ficticia", "09:00 – 09:30", "30 min", "Profesional PRO-01 · CONTROL", "Pendiente", "Cancelado", "Sillón SIL-01 · Profesional PRO-02", "Miércoles 25/11/2026 · SIL-01", "2 turnos y 1 bloqueo"]) {
    assert.ok(texto.includes(esperado), esperado);
  }
  assert.deepEqual(de("turnos"), [[SILLON, "2026-11-25T03:00:00.000Z", "2026-11-26T03:00:00.000Z"]]);
  assert.deepEqual(de("bloqueos"), de("turnos"));
});

test("la semana consulta de lunes a domingo e indica los días sin actividad", async () => {
  const { api, de } = apiFalsa({ turnos: async () => [turno()] });
  const ctx = contexto({ api });
  await montarAgenda(ctx, ruta({ vista: "semana" }));
  assert.deepEqual(de("turnos"), [[SILLON, "2026-11-23T03:00:00.000Z", "2026-11-30T03:00:00.000Z"]]);
  assert.ok(ctx.raiz.textContent.includes("Semana 23/11 – 29/11/2026 · SIL-01"));
  assert.equal(ctx.raiz.textContent.split("Sin actividad visible").length - 1, 6);
});

test("indica la carga y después el período sin actividad", async () => {
  const espera = pendiente();
  const ctx = contexto({ api: apiFalsa({ turnos: () => espera.promesa }).api });
  const montaje = montarAgenda(ctx, ruta());
  await esperar();
  assert.ok(ctx.raiz.textContent.includes("Cargando agenda…"));
  espera.resolver([]);
  await montaje;
  assert.ok(ctx.raiz.textContent.includes("No hay turnos ni bloqueos en este período para el sillón elegido."));
});

test("ante un fallo permite reintentar", async () => {
  let intentos = 0;
  const ctx = contexto({ api: apiFalsa({ turnos: async () => { if (++intentos === 1) throw new TypeError("Failed to fetch"); return [turno()]; } }).api });
  await montarAgenda(ctx, ruta());
  assert.deepEqual(alertas(ctx.raiz), ["No se pudo cargar la agenda. No se pudo conectar con el servidor."]);
  boton(ctx.raiz, "Reintentar").emitir("click");
  await esperar();
  assert.ok(ctx.raiz.textContent.includes("Ana Ficticia"));
});

test("una respuesta atrasada no reemplaza la vista vigente", async () => {
  const espera = pendiente();
  let vigente = true;
  const ctx = contexto({ api: apiFalsa({ turnos: () => espera.promesa }).api });
  ctx.vigente = () => vigente;
  const montaje = montarAgenda(ctx, ruta());
  await esperar();
  vigente = false;
  ctx.raiz.textContent = "otra vista";
  espera.resolver([turno()]);
  await montaje;
  assert.equal(ctx.raiz.textContent, "otra vista");
});

test("informa que faltan sillones sin consultar la agenda", async () => {
  const { api, de } = apiFalsa({ sillones: async () => [] });
  const ctx = contexto({ api });
  await montarAgenda(ctx, ruta({ sillonId: null }));
  assert.ok(ctx.raiz.textContent.includes("No hay sillones disponibles."));
  assert.equal(de("turnos").length, 0);
});

test("sin sillón en la dirección elige el primero y lo conserva en la URL", async () => {
  const ctx = contexto();
  await montarAgenda(ctx, ruta({ sillonId: null }));
  assert.deepEqual(ctx.reemplazos, [ruta()]);
});

test("cambiar sillón, fecha, período o vista actualiza la dirección", async () => {
  const ctx = contexto();
  await montarAgenda(ctx, ruta({ vista: "semana" }));
  boton(ctx.raiz, "Siguiente").emitir("click");
  boton(ctx.raiz, "Anterior").emitir("click");
  boton(ctx.raiz, "Día").emitir("click");
  const sillon = porId(ctx.raiz, "agenda-sillon");
  sillon.value = OTRO_SILLON;
  sillon.emitir("change");
  const fecha = porId(ctx.raiz, "agenda-fecha");
  fecha.value = "2026-12-01";
  fecha.emitir("change");
  assert.deepEqual(ctx.navegaciones, [
    ruta({ vista: "semana", fecha: "2026-12-02" }), ruta({ vista: "semana", fecha: "2026-11-18" }), ruta(),
    ruta({ vista: "semana", sillonId: OTRO_SILLON }), ruta({ vista: "semana", fecha: "2026-12-01" })
  ]);
});

test("recupera el foco del control utilizado", async () => {
  const ctx = contexto();
  ctx.foco = "agenda-siguiente";
  await montarAgenda(ctx, ruta());
  assert.equal(porId(ctx.raiz, "agenda-siguiente").enfocado, true);
});

test("solo recepción ve la acción de bloquear y odontología recibe la aclaración de alcance", async () => {
  const recepcion = contexto();
  await montarAgenda(recepcion, ruta());
  assert.ok(boton(recepcion.raiz, "Bloquear período"));
  boton(recepcion.raiz, "+ Nuevo turno").emitir("click");
  assert.equal(recepcion.navegaciones[0].seccion, "agenda/nuevo");
  const odontologia = contexto({ rol: "odontologo", profesionalRef: "p1" });
  await montarAgenda(odontologia, ruta());
  assert.equal(boton(odontologia.raiz, "Bloquear período"), undefined);
  assert.ok(odontologia.raiz.textContent.includes("Ve únicamente sus turnos."));
});

test("advierte el intervalo sin configurar según el rol", async () => {
  for (const [rol, texto] of [["recepcion", "Configúrelo en Configuración."], ["odontologo", "Solicítelo a recepción."]]) {
    const ctx = contexto({ api: apiFalsa({ intervalo: async () => null }).api, rol });
    await montarAgenda(ctx, ruta());
    assert.ok(alertas(ctx.raiz).some((alerta) => alerta.includes(texto)), rol);
  }
});

test("no expone detalles internos de un error inesperado", async () => {
  const ctx = contexto({ api: apiFalsa({ turnos: rechazo("error", 500, "SQLException en odontologia.turno") }).api });
  await montarAgenda(ctx, ruta());
  assert.ok(!ctx.raiz.textContent.includes("SQLException"));
});

test("al tipear la fecha espera a salir del campo para no perder dígitos", async () => {
  const ctx = contexto();
  await montarAgenda(ctx, ruta());
  const fecha = porId(ctx.raiz, "agenda-fecha");
  fecha.emitir("keydown", { key: "1" });
  fecha.value = "2026-11-01";
  fecha.emitir("change");
  assert.deepEqual(ctx.navegaciones, []);
  fecha.emitir("keydown", { key: "5" });
  fecha.value = "2026-11-15";
  fecha.emitir("change");
  fecha.emitir("blur");
  fecha.emitir("blur");
  assert.deepEqual(ctx.navegaciones, [ruta({ fecha: "2026-11-15" })]);
});

test("Enter aplica la fecha tipeada y el calendario la aplica al elegirla", async () => {
  const tipeada = contexto();
  await montarAgenda(tipeada, ruta());
  const campo = porId(tipeada.raiz, "agenda-fecha");
  campo.emitir("keydown", { key: "2" });
  campo.value = "2026-12-02";
  campo.emitir("change");
  campo.emitir("keydown", { key: "Enter" });
  assert.deepEqual(tipeada.navegaciones, [ruta({ fecha: "2026-12-02" })]);
  const elegida = contexto();
  await montarAgenda(elegida, ruta());
  const otro = porId(elegida.raiz, "agenda-fecha");
  otro.emitir("keydown", { key: " " });
  otro.emitir("pointerdown");
  otro.value = "2026-12-03";
  otro.emitir("change");
  assert.deepEqual(elegida.navegaciones, [ruta({ fecha: "2026-12-03" })]);
});
