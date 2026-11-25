import assert from "node:assert/strict";
import { test } from "node:test";
import { diasDelPeriodo, fechaEnSede, horaEnSede, instanteDesdeSede, limitesDePeriodo, sumarDias } from "../dist/tiempo/mendoza.js";

test("interpreta fecha y hora cargadas como hora de Mendoza", () => {
  assert.equal(instanteDesdeSede("2026-11-26", "09:00")?.toISOString(), "2026-11-26T12:00:00.000Z");
  assert.equal(instanteDesdeSede("2026-12-31", "23:30")?.toISOString(), "2027-01-01T02:30:00.000Z");
});

test("muestra los instantes en Mendoza aunque cambie el día respecto de UTC", () => {
  const instante = new Date("2026-11-26T02:30:00Z");
  assert.equal(fechaEnSede(instante), "2026-11-25");
  assert.equal(horaEnSede(instante), "23:30");
  assert.equal(horaEnSede(new Date("2026-11-26T03:00:00Z")), "00:00");
});

test("rechaza fechas y horas inexistentes o mal formadas", () => {
  for (const [fecha, hora] of [["2026-02-30", "09:00"], ["2026-11-26", "24:00"], ["2026-11-26", "09:60"], ["", "09:00"], ["2026-11-26", ""], ["26/11/2026", "09:00"]]) {
    assert.equal(instanteDesdeSede(fecha, hora), null, `${fecha} ${hora}`);
  }
});

test("suma días cruzando meses y años", () => {
  assert.equal(sumarDias("2026-11-30", 1), "2026-12-01");
  assert.equal(sumarDias("2027-01-01", -1), "2026-12-31");
  assert.equal(sumarDias("2028-02-28", 1), "2028-02-29");
});

test("la semana va de lunes a domingo desde cualquiera de sus días", () => {
  const semana = ["2026-11-23", "2026-11-24", "2026-11-25", "2026-11-26", "2026-11-27", "2026-11-28", "2026-11-29"];
  for (const fecha of ["2026-11-23", "2026-11-25", "2026-11-29"]) {
    assert.deepEqual(diasDelPeriodo("semana", fecha), semana);
  }
  assert.deepEqual(diasDelPeriodo("dia", "2026-11-25"), ["2026-11-25"]);
});

test("los límites del período son medianoches de Mendoza con fin excluido", () => {
  assert.deepEqual(limitesDePeriodo("dia", "2026-11-25"), { desde: "2026-11-25T03:00:00.000Z", hasta: "2026-11-26T03:00:00.000Z" });
  assert.deepEqual(limitesDePeriodo("semana", "2026-11-25"), { desde: "2026-11-23T03:00:00.000Z", hasta: "2026-11-30T03:00:00.000Z" });
});

test("los años fuera del rango admitido se rechazan sin lanzar errores", () => {
  for (const fecha of ["0000-01-01", "0001-01-01", "0026-11-26", "1000-01-01", "1969-12-31", "9998-01-01", "9999-12-31"]) {
    assert.equal(instanteDesdeSede(fecha, "09:00"), null, fecha);
  }
  assert.ok(instanteDesdeSede("1970-01-02", "00:00"));
  assert.ok(limitesDePeriodo("semana", "9997-12-31").hasta);
  assert.ok(limitesDePeriodo("semana", "1970-01-01").desde);
});
