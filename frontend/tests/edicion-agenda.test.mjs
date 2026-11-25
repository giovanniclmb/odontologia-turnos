import assert from "node:assert/strict";
import { test } from "node:test";
import { montarAgenda } from "../dist/agenda/vista-agenda.js";
import { crearSeccion } from "../dist/agenda/seccion.js";
import { RespuestaInvalidaError } from "../dist/agenda/validadores.js";
import { apiFalsa, contexto, pendiente, rechazo, OTRO_SILLON, SILLON } from "./soporte/contexto.mjs";
import { alertas, boton, formulario, porId, todos, esperar } from "./soporte/dom-falso.mjs";

const ruta = { seccion: "agenda", vista: "dia", fecha: "2099-11-26", sillonId: SILLON };
const turno = {
  id: "t1", pacienteNombre: "Ana", pacienteApellido: "Ejemplo", profesionalId: "p1",
  sillonId: SILLON, prestacionId: "r1", inicio: "2099-11-26T12:00:12.123456Z",
  fin: "2099-11-26T12:45:12.123456Z", duracionMinutos: 45, estado: "PENDIENTE", version: "opaca/=1"
};
const reprogramaciones = (ctx) => todos(ctx.raiz, (n) => n.tag === "button" && n.textContent === "Reprogramar");

test("el resumen y el aviso conservan códigos de recurso sin duplicar el rótulo", async () => {
  let actual = turno;
  const datos = apiFalsa({
    sillones: async () => [{ id: SILLON, codigo: "Sillón 1", activo: true }],
    profesionales: async () => [{ id: "p1", codigo: "PRO-01", activo: true }],
    turnos: async () => [actual],
    cancelarTurno: async () => actual = { ...turno, estado: "CANCELADO", version: "nueva" }
  });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Cancelar turno").emitir("click");
  const dialogo = todos(ctx.raiz, (n) => n.tag === "dialog")[0];
  assert.match(dialogo.textContent, /Profesional PRO-01.*Sillón 1/);
  assert.doesNotMatch(dialogo.textContent, /Sillón Sillón 1/);
  boton(ctx.raiz, "Confirmar cancelación").emitir("click");
  await esperar();
  const aviso = porId(ctx.raiz, "agenda-resultado");
  assert.match(aviso.textContent, /Turno cancelado.*Profesional PRO-01.*Sillón 1/);
  assert.doesNotMatch(aviso.textContent, /Sillón Sillón 1/);
});

test("ofrece acciones nativas en día y semana para turnos futuros elegibles de recepción", async () => {
  const datos = apiFalsa({ turnos: async () => [
    turno,
    { ...turno, id: "confirmado", estado: "CONFIRMADO" },
    { ...turno, id: "cancelado", estado: "CANCELADO" }
  ] });
  for (const vista of ["dia", "semana"]) {
    const ctx = contexto({ api: datos.api });
    await montarAgenda(ctx, { ...ruta, vista });
    assert.equal(reprogramaciones(ctx).length, 2);
    assert.ok(reprogramaciones(ctx).every((control) => control.type === "button"
      && control.getAttribute("tabindex") === null));
    assert.equal(todos(ctx.raiz, (n) => n.tag === "button" && n.textContent === "Cancelar turno").length, 2);
  }
});

test("odontología puede operar sobre pendientes propios futuros pero no sobre confirmados ni turnos iniciados", async () => {
  const futuros = apiFalsa({ turnos: async () => [
    turno, { ...turno, id: "confirmado", estado: "CONFIRMADO" }
  ] });
  const ctx = contexto({ api: futuros.api, rol: "odontologo", profesionalRef: "p1" });
  await montarAgenda(ctx, ruta);
  assert.equal(reprogramaciones(ctx).length, 1);
  const pasados = apiFalsa({ turnos: async () => [{
    ...turno, inicio: "2020-11-26T12:00:00Z", fin: "2020-11-26T12:45:00Z"
  }] });
  const anterior = contexto({ api: pasados.api });
  await montarAgenda(anterior, { ...ruta, fecha: "2020-11-26" });
  assert.equal(reprogramaciones(anterior).length, 0);
  assert.equal(boton(anterior.raiz, "Cancelar turno"), undefined);
});

test("reprograma en hora de Mendoza y conserva prestación, duración y versión", async () => {
  const datos = apiFalsa({
    turnos: async () => [turno],
    reprogramarTurno: async () => ({
      ...turno, sillonId: OTRO_SILLON, profesionalId: "p2", inicio: "2099-11-27T02:00:00Z",
      fin: "2099-11-27T02:45:00Z", version: "nueva"
    })
  });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Reprogramar").emitir("click");
  assert.match(ctx.raiz.textContent, /Ana Ejemplo.*CONTROL.*45 minutos/);
  assert.equal(porId(ctx.raiz, "reprogramar-hora").value, "09:00");
  porId(ctx.raiz, "reprogramar-hora").value = "23:00";
  porId(ctx.raiz, "reprogramar-sillon").value = OTRO_SILLON;
  porId(ctx.raiz, "reprogramar-profesional").value = "p2";
  formulario(ctx.raiz).emitir("submit");
  await esperar();
  assert.deepEqual(datos.de("reprogramarTurno"), [["t1", {
    versionEsperada: "opaca/=1", inicio: "2099-11-27T02:00:00.000Z", profesionalId: "p2", sillonId: OTRO_SILLON
  }]]);
});

test("odontología mantiene profesional e instante exacto al conservar el horario original", async () => {
  const confirmado = { ...turno, estado: "CONFIRMADO" };
  const datos = apiFalsa({ turnos: async () => [turno], reprogramarTurno: async () => turno });
  const ctx = contexto({ api: datos.api, rol: "odontologo", profesionalRef: "p1" });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Reprogramar").emitir("click");
  assert.equal(porId(ctx.raiz, "reprogramar-profesional").disabled, true);
  formulario(ctx.raiz).emitir("submit");
  await esperar();
  assert.deepEqual(datos.de("reprogramarTurno"), [["t1", {
    versionEsperada: turno.version, inicio: turno.inicio, profesionalId: "p1", sillonId: SILLON
  }]]);
  assert.match(ctx.raiz.textContent, /Se mantuvo el horario original/);

  const recepcion = apiFalsa({ turnos: async () => [confirmado], reprogramarTurno: async () => confirmado });
  const ctxRecepcion = contexto({ api: recepcion.api });
  await montarAgenda(ctxRecepcion, ruta);
  boton(ctxRecepcion.raiz, "Reprogramar").emitir("click");
  formulario(ctxRecepcion.raiz).emitir("submit");
  await esperar();
  assert.match(ctxRecepcion.raiz.textContent, /Confirmado/);
  assert.match(ctxRecepcion.raiz.textContent, /Se mantuvo el horario original/);
});

test("un conflicto conserva los datos de destino para corregirlos", async () => {
  const datos = apiFalsa({ turnos: async () => [turno], reprogramarTurno: rechazo("horario_no_disponible") });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Reprogramar").emitir("click");
  porId(ctx.raiz, "reprogramar-hora").value = "10:00";
  formulario(ctx.raiz).emitir("submit");
  await esperar();
  assert.match(alertas(ctx.raiz).join(" "), /horario solicitado no está disponible/);
  assert.equal(porId(ctx.raiz, "reprogramar-hora").value, "10:00");
  assert.equal(datos.de("turnos").length, 1);
});

test("mantener el turno restaura el foco y confirmar lo deja visible como cancelado", async () => {
  let actual = turno;
  const datos = apiFalsa({
    turnos: async () => [actual],
    cancelarTurno: async () => actual = { ...turno, estado: "CANCELADO", version: "nueva" }
  });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  const origen = boton(ctx.raiz, "Cancelar turno");
  origen.emitir("click");
  assert.ok(boton(ctx.raiz, "Mantener turno").enfocado);
  boton(ctx.raiz, "Mantener turno").emitir("click");
  assert.equal(datos.de("cancelarTurno").length, 0);
  assert.ok(origen.enfocado);
  origen.emitir("click");
  boton(ctx.raiz, "Confirmar cancelación").emitir("click");
  await esperar();
  assert.deepEqual(datos.de("cancelarTurno"), [["t1", "opaca/=1"]]);
  assert.match(ctx.raiz.textContent, /Turno cancelado.*liberaron.*Cancelado/);
  assert.equal(boton(ctx.raiz, "Cancelar turno"), undefined);
});

test("muestra el destino de la edición sin cambiar filtros hasta solicitar verlo", async () => {
  const actualizado = { ...turno, version: "nueva", sillonId: OTRO_SILLON };
  let movido = false;
  const datos = apiFalsa({ turnos: async () => movido ? [] : [turno], reprogramarTurno: async () => {
    movido = true;
    return actualizado;
  } });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Reprogramar").emitir("click");
  porId(ctx.raiz, "reprogramar-sillon").value = OTRO_SILLON;
  formulario(ctx.raiz).emitir("submit");
  await esperar();
  assert.match(ctx.raiz.textContent, /Turno reprogramado.*SIL-02/);
  assert.ok(todos(ctx.raiz, (n) => n.getAttribute("role") === "status" && n.textContent.includes("Turno reprogramado"))[0].enfocado);
  assert.equal(ctx.navegaciones.length, 0);
  assert.ok(datos.de("turnos").every(([sillon]) => sillon === SILLON));
  boton(ctx.raiz, "Ver destino").emitir("click");
  assert.deepEqual(ctx.navegaciones, [{ ...ruta, sillonId: OTRO_SILLON }]);
});

test("un fallo de red o una respuesta inválida deja incierto el resultado y exige recarga", async () => {
  for (const fallo of [
    new TypeError("red"), new RespuestaInvalidaError("turno"), new SyntaxError("JSON inválido"),
    Object.assign(new Error("gateway"), { status: 503, codigo: "error" })
  ]) {
    const datos = apiFalsa({
      turnos: async () => [turno],
      reprogramarTurno: async () => { throw fallo; }
    });
    const ctx = contexto({ api: datos.api });
    await montarAgenda(ctx, ruta);
    boton(ctx.raiz, "Reprogramar").emitir("click");
    formulario(ctx.raiz).emitir("submit");
    await esperar();
    assert.match(alertas(ctx.raiz).join(" "), /No se pudo confirmar si la edición se guardó/);
    assert.ok(boton(ctx.raiz, "Recargar agenda"));
    formulario(ctx.raiz).emitir("submit");
    assert.equal(datos.de("reprogramarTurno").length, 1);
  }
});

test("conflictos de versión en ambos comandos requieren recarga y bloquean el reenvío", async () => {
  for (const cancelar of [false, true]) {
    const obsoleta = rechazo("version_turno_desactualizada");
    const datos = apiFalsa({
      turnos: async () => [turno], reprogramarTurno: obsoleta, cancelarTurno: obsoleta
    });
    const ctx = contexto({ api: datos.api });
    await montarAgenda(ctx, ruta);
    boton(ctx.raiz, cancelar ? "Cancelar turno" : "Reprogramar").emitir("click");
    const enviar = () => cancelar ? boton(ctx.raiz, "Confirmar cancelación").emitir("click")
      : formulario(ctx.raiz).emitir("submit");
    enviar();
    await esperar();
    assert.match(alertas(ctx.raiz).join(" "), /El turno cambió/);
    enviar();
    assert.equal(datos.de(cancelar ? "cancelarTurno" : "reprogramarTurno").length, 1);
    boton(ctx.raiz, "Recargar agenda").emitir("click");
    await esperar();
    assert.equal(datos.de("turnos").length, 2);
    assert.ok(boton(ctx.raiz, "Reprogramar"));
  }
});

test("un éxito con recarga fallida permite repetir solo la consulta", async () => {
  let lecturas = 0;
  const cancelado = { ...turno, estado: "CANCELADO", version: "nueva" };
  const datos = apiFalsa({
    turnos: async () => {
      lecturas++;
      if (lecturas === 2) throw new Error("falló conexión temporalmente");
      return [lecturas === 1 ? turno : cancelado];
    },
    cancelarTurno: async () => cancelado
  });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Cancelar turno").emitir("click");
  boton(ctx.raiz, "Confirmar cancelación").emitir("click");
  await esperar();
  assert.match(ctx.raiz.textContent, /Turno cancelado.*No se pudo (actualizar|cargar) la agenda/);
  boton(ctx.raiz, "Reintentar").emitir("click");
  await esperar();
  assert.equal(datos.de("cancelarTurno").length, 1);
  assert.equal(datos.de("turnos").length, 3);
  assert.match(ctx.raiz.textContent, /Cancelado/);
});

test("navegar durante un envío bloquea otra edición del mismo turno y conserva la vista vigente", async () => {
  const envio = pendiente();
  const datos = apiFalsa({
    turnos: async () => [turno], reprogramarTurno: () => envio.promesa, cancelarTurno: async () => turno
  });
  const ctx = contexto({ api: datos.api });
  const seccion = crearSeccion({ ...ctx, hoy: () => ruta.fecha, elementoActivo: () => null });
  await seccion.mostrar(ruta);
  boton(ctx.raiz, "Reprogramar").emitir("click");
  formulario(ctx.raiz).emitir("submit");
  const semanal = { ...ruta, vista: "semana" };
  await seccion.mostrar(semanal);
  boton(ctx.raiz, "Cancelar turno").emitir("click");
  boton(ctx.raiz, "Confirmar cancelación").emitir("click");
  assert.equal(datos.de("cancelarTurno").length, 0);
  assert.match(alertas(ctx.raiz).join(" "), /edición en curso/);
  boton(ctx.raiz, "Mantener turno").emitir("click");
  envio.resolver({ ...turno, version: "nueva", sillonId: OTRO_SILLON });
  await esperar();
  assert.match(ctx.raiz.textContent, /Semana de lunes a domingo/);
  assert.match(ctx.raiz.textContent, /Turno reprogramado/);
  assert.ok(boton(ctx.raiz, "Ver destino"));
  assert.equal(ctx.navegaciones.length, 0);
});

test("mientras se confirma la cancelación, el diálogo no ofrece desistir de un envío ya iniciado", async () => {
  const envio = pendiente();
  const datos = apiFalsa({ turnos: async () => [turno], cancelarTurno: () => envio.promesa });
  const ctx = contexto({ api: datos.api });
  await montarAgenda(ctx, ruta);
  boton(ctx.raiz, "Cancelar turno").emitir("click");
  boton(ctx.raiz, "Confirmar cancelación").emitir("click");
  const mantener = boton(ctx.raiz, "Mantener turno");
  assert.equal(mantener.disabled, true);
  const dialogo = todos(ctx.raiz, (nodo) => nodo.tag === "dialog")[0];
  let seQueda = false;
  dialogo.emitir("cancel", { preventDefault: () => { seQueda = true; } });
  assert.equal(seQueda, true);
  assert.equal(datos.de("cancelarTurno").length, 1);
  envio.resolver({ ...turno, estado: "CANCELADO", version: "nueva" });
  await esperar();
});
