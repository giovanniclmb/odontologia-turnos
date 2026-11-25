import assert from "node:assert/strict";
import { test } from "node:test";

/** Cliente HTTP real; solo se sustituye la red y el almacenamiento del navegador. */
const pedidos = [];
let responder = () => [];
globalThis.window = { location: { port: "5173" } };
globalThis.localStorage = { getItem: () => "token-ficticio", setItem() {}, removeItem() {} };
globalThis.fetch = async (url, init) => {
  pedidos.push({ url, metodo: init.method, cuerpo: init.body, autorizacion: init.headers.get("Authorization") });
  return new Response(JSON.stringify(responder(url)), { status: 200, headers: { "Content-Type": "application/json" } });
};
const { apiAgenda } = await import("../dist/agenda/api.js");
const BASE = "http://localhost:8080";

test("usa las rutas, métodos y campos del contrato", async () => {
  responder = (url) => (url.includes("intervalo") ? { minutos: 15 } : []);
  pedidos.length = 0;
  await apiAgenda.sillones();
  await apiAgenda.profesionales();
  await apiAgenda.prestaciones();
  await apiAgenda.intervalo();
  await apiAgenda.establecerIntervalo(15);
  await apiAgenda.turnos("s1", "2026-11-25T03:00:00.000Z", "2026-11-26T03:00:00.000Z");
  await apiAgenda.bloqueos("s1", "2026-11-25T03:00:00.000Z", "2026-11-26T03:00:00.000Z");
  await apiAgenda.crearTurno({ pacienteId: "c1" });
  await apiAgenda.crearBloqueo({ sillonId: "s1" });
  const periodo = "desde=2026-11-25T03%3A00%3A00.000Z&hasta=2026-11-26T03%3A00%3A00.000Z";
  assert.deepEqual(pedidos.map((p) => `${p.metodo} ${p.url.replace(BASE, "")}`), [
    "GET /recursos/sillones", "GET /recursos/profesionales", "GET /recursos/prestaciones",
    "GET /sede/intervalo", "PUT /sede/intervalo",
    `GET /agenda/sillones/s1/turnos?${periodo}`, `GET /agenda/bloqueos?sillonId=s1&${periodo}`,
    "POST /agenda/turnos", "POST /agenda/bloqueos"
  ]);
  assert.equal(pedidos[4].cuerpo, '{"minutos":15}');
  assert.ok(pedidos.every((p) => p.autorizacion === "Bearer token-ficticio"));
});

test("busca por código y por nombre y une los resultados sin repetir", async () => {
  const paciente = (id) => ({ id, codigo: id, nombre: "Ana", apellido: "Ficticia" });
  responder = (url) => (url.includes("codigo=") ? [paciente("c1")] : [paciente("c1"), paciente("c2")]);
  pedidos.length = 0;
  const encontrados = await apiAgenda.buscarPacientes("Ana");
  assert.deepEqual(encontrados.map((p) => p.id), ["c1", "c2"]);
  assert.deepEqual(pedidos.map((p) => p.url.replace(BASE, "")), ["/pacientes?codigo=Ana", "/pacientes?texto=Ana"]);
});

const turnoEditado = {
  id: "turno/1", profesionalId: "p1",
  sillonId: "s1", prestacionId: "r1", inicio: "2026-11-27T12:00:00.123456Z",
  fin: "2026-11-27T12:45:00.123456Z", duracionMinutos: 45, estado: "PENDIENTE", version: "nueva/=?"
};

test("reprograma con la versión opaca y recibe el turno actualizado", async () => {
  pedidos.length = 0;
  responder = () => turnoEditado;
  const datos = { versionEsperada: "vieja?=/", profesionalId: "p1", sillonId: "s1", inicio: turnoEditado.inicio };
  assert.deepEqual(await apiAgenda.reprogramarTurno(turnoEditado.id, datos), turnoEditado);
  assert.equal(pedidos[0].url, BASE + "/agenda/turnos/turno%2F1/reprogramacion");
  assert.equal(pedidos[0].metodo, "POST");
  assert.deepEqual(JSON.parse(pedidos[0].cuerpo), datos);
});

test("cancela con solo la versión del turno y valida la respuesta", async () => {
  pedidos.length = 0;
  const cancelado = { ...turnoEditado, estado: "CANCELADO", version: "cancelada/=? " };
  responder = () => cancelado;
  assert.deepEqual(await apiAgenda.cancelarTurno(turnoEditado.id, turnoEditado.version), cancelado);
  assert.equal(pedidos[0].url, BASE + "/agenda/turnos/turno%2F1/cancelacion");
  assert.equal(pedidos[0].metodo, "POST");
  assert.deepEqual(JSON.parse(pedidos[0].cuerpo), { versionEsperada: turnoEditado.version });
});

test("rechaza respuestas de edición con versión o estado malformados", async () => {
  responder = () => ({ ...turnoEditado, version: 17 });
  await assert.rejects(apiAgenda.reprogramarTurno(turnoEditado.id, {}), { name: "RespuestaInvalidaError" });
  responder = () => ({ ...turnoEditado, estado: "DESCONOCIDO" });
  await assert.rejects(apiAgenda.cancelarTurno(turnoEditado.id, "original"), { name: "RespuestaInvalidaError" });
});
