const controles = Object.fromEntries(
  [...document.querySelectorAll("[id]")].map((nodo) => [nodo.id, nodo]),
);
const parametros = new URLSearchParams(location.search);
const odontologia = parametros.get("rol") === "odontologia";
const turno = {
  fecha: "2026-11-26",
  hora: "09:00",
  profesional: "Dra. Soto",
  sillon: "Sillón 1",
  estado: odontologia ? "Pendiente" : "Confirmado",
};
const filtro = { fecha: turno.fecha, sillon: turno.sillon };
const formatoFecha = new Intl.DateTimeFormat("es-AR", {
  dateStyle: "full",
  timeZone: "UTC",
});

function rangoHorario() {
  const [hora, minuto] = turno.hora.split(":").map(Number);
  const fin = hora * 60 + minuto + 45;
  const horaFin = String(Math.floor(fin / 60) % 24).padStart(2, "0");
  const minutoFin = String(fin % 60).padStart(2, "0");
  return `${turno.hora}–${horaFin}:${minutoFin}`;
}

function fechaTexto(fecha) {
  return formatoFecha.format(new Date(`${fecha}T12:00:00Z`));
}

function resumen() {
  return `${fechaTexto(turno.fecha)}, ${rangoHorario()} · ${turno.profesional} · ${turno.sillon}`;
}

function informar(texto) {
  controles.resultado.hidden = !texto;
  controles.resultado.textContent = texto;
}

function pintarAgenda() {
  const visible = turno.fecha === filtro.fecha && turno.sillon === filtro.sillon;
  controles["agenda-dia"].textContent = `${fechaTexto(filtro.fecha)} · ${filtro.sillon} · Hora de Mendoza`;
  controles.turno.hidden = !visible;
  controles["otro-turno"].hidden = filtro.fecha !== "2026-11-26" || filtro.sillon !== "Sillón 1";
  controles.destino.hidden = visible;
  controles.horario.textContent = rangoHorario();
  controles["turno-detalles"].textContent = `Control odontológico · ${turno.profesional}`;
  const cancelado = turno.estado === "Cancelado";
  const color = cancelado ? "cancelled" : turno.estado === "Pendiente" ? "pending" : "confirmed";
  controles.estado.className = `badge badge-${color}`;
  controles.estado.textContent = turno.estado;
  controles.turno.classList.toggle("is-cancelled", cancelado);
  controles.acciones.hidden = cancelado;
}

function abrirAgenda(foco = "agenda-titulo") {
  pintarAgenda();
  controles.reprogramar.hidden = true;
  controles.agenda.hidden = false;
  controles[foco].focus();
}

function abrirFormulario() {
  controles.origen.textContent = resumen();
  for (const campo of ["fecha", "hora", "profesional", "sillon"]) {
    controles[campo].value = turno[campo];
  }
  controles.conflicto.hidden = true;
  controles.desactualizado.hidden = true;
  controles.agenda.hidden = true;
  controles.reprogramar.hidden = false;
  controles["reprogramar-titulo"].focus();
}

function abrirConfirmacion() {
  controles["cancelar-detalles"].textContent = resumen();
  controles.confirmacion.returnValue = "mantener";
  controles.confirmacion.showModal();
}

controles["abrir-reprogramar"].addEventListener("click", abrirFormulario);
controles["abrir-cancelar"].addEventListener("click", abrirConfirmacion);
controles.volver.addEventListener("click", () => abrirAgenda("abrir-reprogramar"));
controles.recargar.addEventListener("click", () => abrirAgenda());
controles["ver-destino"].addEventListener("click", () => {
  filtro.fecha = turno.fecha;
  filtro.sillon = turno.sillon;
  abrirAgenda();
});

controles.formulario.addEventListener("submit", (evento) => {
  evento.preventDefault();
  const campos = ["fecha", "hora", "profesional", "sillon"];
  const destino = Object.fromEntries(campos.map((campo) => [campo, controles[campo].value]));
  if (destino.hora === "10:00" && destino.fecha === "2026-11-26" &&
    destino.profesional === "Dra. Soto" && destino.sillon === "Sillón 1") {
    controles.conflicto.hidden = false;
    controles.conflicto.focus();
    return;
  }
  if (campos.some((campo) => turno[campo] !== destino[campo])) {
    Object.assign(turno, destino, { estado: "Pendiente" });
    informar(`Turno reprogramado para el ${resumen()}.`);
  } else {
    informar("Se mantuvo el horario original.");
  }
  abrirAgenda();
});

function cancelarTurno() {
  turno.estado = "Cancelado";
  informar(`Turno cancelado. Se liberaron el sillón y el horario del profesional el ${resumen()}.`);
  abrirAgenda();
}

controles.confirmacion.addEventListener("close", () => {
  if (controles.confirmacion.returnValue === "cancelar") cancelarTurno();
});

controles.operador.textContent = odontologia ? "Odontología" : "Recepción";
controles.profesional.disabled = odontologia;
pintarAgenda();

switch (parametros.get("vista")) {
  case "reprogramar":
    abrirFormulario();
    break;
  case "conflicto":
    abrirFormulario();
    controles.hora.value = "10:00";
    controles.conflicto.hidden = false;
    break;
  case "version":
    abrirFormulario();
    controles.desactualizado.hidden = false;
    break;
  case "confirmar":
    abrirConfirmacion();
    break;
  case "cancelado":
    cancelarTurno();
    break;
}
