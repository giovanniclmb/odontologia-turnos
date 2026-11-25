import assert from "node:assert/strict";
import { test } from "node:test";
import { componerDias } from "../dist/agenda/modelo.js";

const catalogos = {
  sillones: [{ id: "s1", codigo: "SIL-01", activo: true }],
  profesionales: [{ id: "p1", codigo: "PRO-01", activo: true }],
  prestaciones: [{ id: "r1", codigo: "CONTROL", duracionReferenciaMinutos: 30 }]
};
const turno = (id, inicio, fin, estado = "PENDIENTE") => ({
  id, pacienteNombre: "Ana", pacienteApellido: "Ficticia", profesionalId: "p1", sillonId: "s1", prestacionId: "r1",
  inicio, fin, duracionMinutos: (Date.parse(fin) - Date.parse(inicio)) / 60000, estado, version: "0"
});
const bloqueo = (id, inicio, fin, sillonId = "s1", profesionalId = null) => ({ id, inicio, fin, sillonId, profesionalId });
const componer = (opciones) => componerDias({ dias: ["2026-11-25"], rol: "recepcion", catalogos, turnos: [], bloqueos: [], ...opciones });

test("ordena turnos y bloqueos por inicio con horario, duración y detalle", () => {
  const [dia] = componer({
    turnos: [turno("tarde", "2026-11-25T18:00:00Z", "2026-11-25T19:00:00Z", "CONFIRMADO"), turno("temprano", "2026-11-25T12:00:00Z", "2026-11-25T12:30:00Z")],
    bloqueos: [bloqueo("medio", "2026-11-25T16:00:00Z", "2026-11-25T17:00:00Z", "s1", "p1")]
  });
  assert.deepEqual(dia.elementos.map((e) => e.id), ["temprano", "medio", "tarde"]);
  assert.deepEqual(dia.elementos[0], { id: "temprano", tipo: "turno", horario: "09:00 – 09:30", duracion: "30 min",
    titulo: "Ana Ficticia", detalle: "Profesional PRO-01 · CONTROL", estado: "pendiente", etiqueta: "Pendiente" });
  assert.equal(dia.elementos[1].detalle, "Sillón SIL-01 · Profesional PRO-01");
  assert.equal(dia.elementos[1].etiqueta, "No disponible");
  assert.equal(dia.turnos, 2);
  assert.equal(dia.bloqueos, 1);
});

test("identifica los turnos cancelados", () => {
  const [dia] = componer({ turnos: [turno("c", "2026-11-25T14:00:00Z", "2026-11-25T15:00:00Z", "CANCELADO")] });
  assert.equal(dia.elementos[0].estado, "cancelado");
  assert.equal(dia.elementos[0].etiqueta, "Cancelado");
});

test("repite en cada día un período que cruza la medianoche y respeta el fin excluido", () => {
  const dias = componer({
    dias: ["2026-11-27", "2026-11-28", "2026-11-29"],
    bloqueos: [bloqueo("largo", "2026-11-27T15:00:00Z", "2026-11-29T03:00:00Z")]
  });
  assert.deepEqual(dias.map((d) => d.elementos.length), [1, 1, 0]);
  assert.equal(dias[0].elementos[0].horario, "Desde 27/11 12:00 hasta 29/11 00:00");
});

test("un bloqueo que termina a medianoche se muestra dentro de su día", () => {
  const [dia] = componer({ bloqueos: [bloqueo("noche", "2026-11-26T01:00:00Z", "2026-11-26T03:00:00Z")] });
  assert.equal(dia.elementos[0].horario, "22:00 – 00:00");
});

test("a odontología le describe la afectación sin códigos ajenos", () => {
  const [dia] = componer({
    rol: "odontologo",
    bloqueos: [
      bloqueo("a", "2026-11-25T12:00:00Z", "2026-11-25T13:00:00Z", "s1", null),
      bloqueo("b", "2026-11-25T14:00:00Z", "2026-11-25T15:00:00Z", null, "p1"),
      bloqueo("c", "2026-11-25T16:00:00Z", "2026-11-25T17:00:00Z", "s1", "p1")
    ]
  });
  assert.deepEqual(dia.elementos.map((e) => e.detalle),
    ["Afecta este sillón", "Afecta su agenda profesional", "Afecta este sillón y su agenda profesional"]);
});
