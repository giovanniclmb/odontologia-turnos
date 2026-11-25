import assert from "node:assert/strict";
import { test } from "node:test";
import { escribirRuta, leerRuta } from "../dist/agenda/ruta.js";

const HOY = "2026-11-25";
const SILLON = "9d6f71bb-4e97-4d16-96f9-184168cb5c0f";

test("sin dirección muestra pacientes y la agenda usa hoy en vista diaria", () => {
  assert.equal(leerRuta("", "recepcion", HOY).seccion, "pacientes");
  assert.deepEqual(leerRuta("#agenda", "recepcion", HOY), { seccion: "agenda", vista: "dia", fecha: HOY, sillonId: null });
});

test("conserva vista, fecha y sillón al escribir y volver a leer", () => {
  const ruta = { seccion: "agenda", vista: "semana", fecha: "2026-12-02", sillonId: SILLON };
  assert.equal(escribirRuta(ruta), `#agenda?vista=semana&fecha=2026-12-02&sillon=${SILLON}`);
  assert.deepEqual(leerRuta(escribirRuta(ruta), "odontologo", HOY), ruta);
  const alta = { ...ruta, seccion: "agenda/nuevo" };
  assert.deepEqual(leerRuta(escribirRuta(alta), "recepcion", HOY), alta);
});

test("descarta parámetros inválidos", () => {
  assert.deepEqual(leerRuta("#agenda?vista=mes&fecha=2026-02-30&sillon=<script>", "recepcion", HOY),
    { seccion: "agenda", vista: "dia", fecha: HOY, sillonId: null });
  assert.equal(leerRuta("#desconocida", "recepcion", HOY).seccion, "pacientes");
});

test("odontología no accede a bloqueo ni a configuración", () => {
  assert.equal(leerRuta("#configuracion", "recepcion", HOY).seccion, "configuracion");
  assert.equal(leerRuta("#agenda/bloqueo", "recepcion", HOY).seccion, "agenda/bloqueo");
  assert.equal(leerRuta("#configuracion", "odontologo", HOY).seccion, "agenda");
  assert.equal(leerRuta("#agenda/bloqueo", "odontologo", HOY).seccion, "agenda");
});
