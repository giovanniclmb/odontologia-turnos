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
