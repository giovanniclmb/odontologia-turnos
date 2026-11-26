const estado = new URLSearchParams(location.search).get("estado") || "listado";
const contenido = document.getElementById("contenido");
const aviso = document.getElementById("aviso");
const actualizar = document.getElementById("actualizar");
const turnos = [
  { fecha: "Jueves 26 de noviembre", hora: "09:00–09:45", paciente: "Ana Ejemplo", detalle: "Control odontológico · Dra. Soto · Sillón 1" },
  { fecha: "Viernes 27 de noviembre", hora: "10:30–11:00", paciente: "Bruno Ejemplo", detalle: "Consulta odontológica · Dr. Ríos · Sillón 2" }
];
function mostrarLista(datos) {
  contenido.replaceChildren();
  if (!datos.length) { contenido.innerHTML = '<p class="seguimiento-vacio">No hay turnos pendientes de confirmación.</p>'; return; }
  const lista = document.createElement("ul"); lista.className = "seguimiento-lista";
  datos.forEach((turno) => {
    const fila = document.createElement("li"); fila.className = "seguimiento-turno";
    fila.innerHTML = `<div><div class="seguimiento-fecha">${turno.fecha}</div><div>${turno.hora}</div></div><div><strong>${turno.paciente}</strong><p class="seguimiento-detalle">${turno.detalle}</p></div>`;
    const boton = document.createElement("button"); boton.className = "btn btn-primary"; boton.type = "button"; boton.textContent = "Registrar confirmación"; boton.setAttribute("aria-label", `Registrar confirmación de ${turno.paciente}`);
    boton.addEventListener("click", () => { aviso.innerHTML = '<div class="seguimiento-mensaje exito" role="status">Confirmación registrada. La lista está actualizada.</div>'; mostrarLista(datos.filter((t) => t !== turno)); });
    fila.append(boton); lista.append(fila);
  });
  contenido.append(lista);
}
actualizar.addEventListener("click", () => location.assign("?estado=listado"));
if (estado === "vacio") mostrarLista([]);
else if (estado === "error") contenido.innerHTML = '<div class="seguimiento-mensaje" role="alert">No se pudo cargar la lista. Use Actualizar lista para volver a intentarlo.</div>';
else {
  mostrarLista(estado === "guardado" ? turnos.slice(1) : turnos);
  if (estado === "conflicto" || estado === "incierto") {
    aviso.innerHTML = `<div class="seguimiento-mensaje" role="alert">${estado === "conflicto" ? "El turno cambió desde la última consulta." : "No se pudo saber si la confirmación se guardó."} Actualice la lista antes de volver a intentarlo.</div>`;
    contenido.querySelectorAll("button").forEach((boton) => { boton.disabled = true; });
  }
  if (estado === "guardado") aviso.innerHTML = '<div class="seguimiento-mensaje exito" role="status">Confirmación registrada. La lista está actualizada.</div>';
}
