import assert from "node:assert/strict";
import { test } from "node:test";
import { leerBloqueos, leerIntervalo, leerPacientes, leerPrestaciones, leerRecursos, leerTurnos, RespuestaInvalidaError } from "../dist/agenda/validadores.js";

const turno = {
  id: "t1", pacienteNombre: "Ana", pacienteApellido: "Ficticia", profesionalId: "p1", sillonId: "s1",
  prestacionId: "r1", inicio: "2026-11-25T12:00:00Z", fin: "2026-11-25T12:30:00Z", duracionMinutos: 30,
  estado: "PENDIENTE", version: "0"
};
const bloqueo = { id: "b1", inicio: "2026-11-25T16:00:00Z", fin: "2026-11-25T17:00:00Z", sillonId: "s1", profesionalId: null };

function rechaza(leer, valores) {
  for (const valor of valores) {
    assert.throws(() => leer(valor), RespuestaInvalidaError, JSON.stringify(valor));
  }
}

test("acepta turnos con el contrato esperado", () => {
  assert.deepEqual(leerTurnos([turno]), [turno]);
});

test("rechaza turnos incompletos, con tipos erróneos o estados desconocidos", () => {
  const { pacienteNombre, ...sinNombre } = turno;
  rechaza(leerTurnos, [null, {}, "texto", [null], [sinNombre], [{ ...turno, duracionMinutos: "30" }],
    [{ ...turno, estado: "ATENDIDO" }], [{ ...turno, inicio: "ayer" }]]);
});

test("acepta bloqueos con referencias recortadas y rechaza los mal formados", () => {
  assert.deepEqual(leerBloqueos([bloqueo]), [bloqueo]);
  rechaza(leerBloqueos, [{}, [{ ...bloqueo, fin: null }], [{ ...bloqueo, sillonId: 7 }]]);
});

test("valida catálogos de recursos y prestaciones", () => {
  assert.deepEqual(leerRecursos([{ id: "s1", codigo: "SIL-01", activo: true }]), [{ id: "s1", codigo: "SIL-01", activo: true }]);
  assert.deepEqual(leerPrestaciones([{ id: "r1", codigo: "CONTROL", duracionReferenciaMinutos: null }]),
    [{ id: "r1", codigo: "CONTROL", duracionReferenciaMinutos: null }]);
  rechaza(leerRecursos, [[{ id: "s1", codigo: "SIL-01" }], null]);
  rechaza(leerPrestaciones, [[{ id: "r1", codigo: "CONTROL", duracionReferenciaMinutos: 0 }]]);
});

test("del paciente conserva solo lo necesario para elegirlo", () => {
  const respuesta = [{ id: "c1", codigo: "P-001", nombre: "Ana", apellido: "Ficticia", email: "ana@example.invalid", telefono: null, profesionalesAsignados: [] }];
  assert.deepEqual(leerPacientes(respuesta), [{ id: "c1", codigo: "P-001", nombre: "Ana", apellido: "Ficticia" }]);
  rechaza(leerPacientes, [[{ id: "c1" }]]);
});

test("distingue el intervalo sin configurar de un valor inválido", () => {
  assert.equal(leerIntervalo({ minutos: null }), null);
  assert.equal(leerIntervalo({ minutos: 15 }), 15);
  rechaza(leerIntervalo, [{}, { minutos: 0 }, { minutos: 7.5 }, { minutos: "15" }, null]);
});
