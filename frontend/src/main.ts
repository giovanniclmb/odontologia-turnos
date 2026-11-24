import { currentUser, login, logout } from "./auth/auth.js";
import { getToken } from "./api/client.js";
import { buscarPacientes, crearPaciente, consultarPaciente, actualizarPaciente, asignarProfesional, desasignarProfesional, listarProfesionales } from "./pacientes/pacientes.js";
import { consultarFicha, actualizarAntecedentes, agregarNota, editarNota } from "./ficha/ficha.js";
import type { Paciente, Profesional, Ficha } from "./api/types.js";
import { ApiError } from "./api/client.js";

function el<T extends HTMLElement>(id: string): T {
  const node = document.getElementById(id);
  if (!node) throw new Error(`Elemento no encontrado: ${id}`);
  return node as T;
}

function mostrarMensaje(contenedor: HTMLElement, mensaje: string, error = false): void {
  contenedor.textContent = mensaje;
  contenedor.className = error ? "mensaje error" : "mensaje";
}

function esRecepcion(): boolean {
  return currentUser()?.rol === "recepcion";
}

function renderLogin(): void {
  const root = el<HTMLDivElement>("app");
  root.innerHTML = `
    <section class="tarjeta">
      <h1>Iniciar sesión</h1>
      <form id="form-login">
        <label>Usuario <input id="usuario" required></label>
        <label>Contraseña <input id="contrasena" type="password" required></label>
        <button type="submit">Ingresar</button>
      </form>
      <p id="login-mensaje" class="mensaje"></p>
    </section>
  `;
  const form = el<HTMLFormElement>("form-login");
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const usuario = (el<HTMLInputElement>("usuario")).value;
    const contrasena = (el<HTMLInputElement>("contrasena")).value;
    const mensaje = el<HTMLParagraphElement>("login-mensaje");
    try {
      await login(usuario, contrasena);
      renderApp();
    } catch (error) {
      mostrarMensaje(mensaje, "Credenciales inválidas", true);
    }
  });
}

async function renderApp(): Promise<void> {
  const root = el<HTMLDivElement>("app");
  const user = currentUser();
  root.innerHTML = `
    <header>
      <h1>Pacientes y ficha clínica</h1>
      <div>
        <span>${user?.nombreUsuario ?? ""} (${user?.rol ?? ""})</span>
        <button id="btn-logout">Cerrar sesión</button>
      </div>
    </header>
    <section class="tarjeta">
      <h2>Búsqueda</h2>
      <form id="form-busqueda">
        <input id="busqueda-codigo" placeholder="Código exacto">
        <input id="busqueda-texto" placeholder="Nombre o apellido">
        <button type="submit">Buscar</button>
      </form>
    </section>
    <section class="tarjeta">
      <h2>Alta de paciente</h2>
      <form id="form-alta">
        <input id="alta-codigo" placeholder="Código" required>
        <input id="alta-nombre" placeholder="Nombre" required>
        <input id="alta-apellido" placeholder="Apellido" required>
        <input id="alta-email" placeholder="Email" required>
        <input id="alta-telefono" placeholder="Teléfono">
        <button type="submit">Crear</button>
      </form>
      <p id="alta-mensaje" class="mensaje"></p>
    </section>
    <section class="tarjeta">
      <h2>Listado</h2>
      <ul id="lista-pacientes"></ul>
    </section>
    <section class="tarjeta" id="detalle" hidden></section>
  `;
  el<HTMLButtonElement>("btn-logout").addEventListener("click", async () => {
    await logout();
    renderLogin();
  });
  const formBusqueda = el<HTMLFormElement>("form-busqueda");
  formBusqueda.addEventListener("submit", async (event) => {
    event.preventDefault();
    const codigo = (el<HTMLInputElement>("busqueda-codigo")).value.trim();
    const texto = (el<HTMLInputElement>("busqueda-texto")).value.trim();
    await cargarListado(codigo || undefined, texto || undefined);
  });
  const formAlta = el<HTMLFormElement>("form-alta");
  formAlta.addEventListener("submit", async (event) => {
    event.preventDefault();
    const mensaje = el<HTMLParagraphElement>("alta-mensaje");
    try {
      await crearPaciente({
        codigo: (el<HTMLInputElement>("alta-codigo")).value.trim(),
        nombre: (el<HTMLInputElement>("alta-nombre")).value.trim(),
        apellido: (el<HTMLInputElement>("alta-apellido")).value.trim(),
        email: (el<HTMLInputElement>("alta-email")).value.trim(),
        telefono: (el<HTMLInputElement>("alta-telefono")).value.trim() || null
      });
      mostrarMensaje(mensaje, "Paciente creado");
      formAlta.reset();
      await cargarListado();
    } catch (error) {
      mostrarMensaje(mensaje, (error as Error).message, true);
    }
  });
  await cargarListado();
}

async function cargarListado(codigo?: string, texto?: string): Promise<void> {
  const lista = el<HTMLUListElement>("lista-pacientes");
  lista.innerHTML = "";
  try {
    const pacientes = await buscarPacientes(codigo, texto);
    for (const p of pacientes) {
      const item = document.createElement("li");
      item.textContent = `${p.codigo} - ${p.nombre} ${p.apellido} (${p.email})`;
      const btn = document.createElement("button");
      btn.textContent = "Ver detalle";
      btn.addEventListener("click", () => void verDetalle(p.id));
      item.appendChild(btn);
      lista.appendChild(item);
    }
  } catch (error) {
    lista.textContent = (error as Error).message;
  }
}

async function verDetalle(pacienteId: string): Promise<void> {
  const detalle = el<HTMLElement>("detalle");
  detalle.hidden = false;
  detalle.innerHTML = "<p>Cargando...</p>";
  try {
    const paciente = await consultarPaciente(pacienteId);
    const ficha = await consultarFicha(pacienteId).catch(() => null);
    const profesionales = esRecepcion() ? await listarProfesionales().catch(() => []) : [];
    detalle.innerHTML = renderDetalle(paciente, ficha, profesionales);
    adjuntarEventosDetalle(paciente, profesionales);
  } catch (error) {
    detalle.innerHTML = `<p class="mensaje error">${(error as Error).message}</p>`;
  }
}

function renderDetalle(paciente: Paciente, ficha: Ficha | null, profesionales: Profesional[]): string {
  const asignados = paciente.profesionalesAsignados.join(", ") || "sin asignaciones";
  const recepcion = esRecepcion();
  const notasHtml = (ficha?.notas ?? []).map((n) => `
    <li>
      <p>${n.texto}</p>
      <small>Autor: ${n.autorId} - ${n.creadaEn}</small>
      ${!recepcion ? `<button data-editar-nota="${n.id}">Editar</button>` : ""}
    </li>
  `).join("");
  return `
    <h2>Detalle</h2>
    <form id="form-editar-paciente">
      <input id="edit-nombre" value="${escape(paciente.nombre)}" required>
      <input id="edit-apellido" value="${escape(paciente.apellido)}" required>
      <input id="edit-email" value="${escape(paciente.email)}" required>
      <input id="edit-telefono" value="${escape(paciente.telefono ?? "")}">
      <button type="submit">Guardar datos</button>
    </form>
    <p>Asignaciones: ${asignados}</p>
    ${recepcion ? `
      <section>
        <h3>Asignaciones</h3>
        <select id="select-profesional">
          ${profesionales.map((p) => `<option value="${p.id}">${p.codigo}</option>`).join("")}
        </select>
        <button id="btn-asignar">Asignar</button>
        <button id="btn-desasignar">Quitar</button>
      </section>
    ` : ""}
    <section>
      <h3>Ficha clínica</h3>
      ${recepcion ? "" : `
        <textarea id="antecedentes">${escape(ficha?.antecedentes ?? "")}</textarea>
        <button id="btn-guardar-antecedentes">Guardar antecedentes</button>
      `}
      <p>${ficha?.antecedentes ?? "Sin antecedentes"}</p>
      <h4>Notas</h4>
      <ul>${notasHtml}</ul>
      ${recepcion ? "" : `
        <textarea id="nueva-nota"></textarea>
        <button id="btn-agregar-nota">Agregar nota</button>
      `}
    </section>
    <p id="detalle-mensaje" class="mensaje"></p>
  `;
}

function adjuntarEventosDetalle(paciente: Paciente, profesionales: Profesional[]): void {
  const mensaje = el<HTMLParagraphElement>("detalle-mensaje");
  const formEditar = document.getElementById("form-editar-paciente") as HTMLFormElement | null;
  formEditar?.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      await actualizarPaciente(paciente.id, {
        nombre: (el<HTMLInputElement>("edit-nombre")).value.trim(),
        apellido: (el<HTMLInputElement>("edit-apellido")).value.trim(),
        email: (el<HTMLInputElement>("edit-email")).value.trim(),
        telefono: (el<HTMLInputElement>("edit-telefono")).value.trim() || null
      });
      mostrarMensaje(mensaje, "Datos actualizados");
      await verDetalle(paciente.id);
    } catch (error) {
      mostrarMensaje(mensaje, (error as Error).message, true);
    }
  });

  if (esRecepcion()) {
    document.getElementById("btn-asignar")?.addEventListener("click", async () => {
      const select = el<HTMLSelectElement>("select-profesional");
      try {
        await asignarProfesional(paciente.id, select.value);
        mostrarMensaje(mensaje, "Profesional asignado");
        await verDetalle(paciente.id);
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
    document.getElementById("btn-desasignar")?.addEventListener("click", async () => {
      const select = el<HTMLSelectElement>("select-profesional");
      try {
        await desasignarProfesional(paciente.id, select.value);
        mostrarMensaje(mensaje, "Asignación quitada");
        await verDetalle(paciente.id);
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
  } else {
    document.getElementById("btn-guardar-antecedentes")?.addEventListener("click", async () => {
      const texto = (el<HTMLTextAreaElement>("antecedentes")).value;
      try {
        await actualizarAntecedentes(paciente.id, texto);
        mostrarMensaje(mensaje, "Antecedentes guardados");
        await verDetalle(paciente.id);
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
    document.getElementById("btn-agregar-nota")?.addEventListener("click", async () => {
      const texto = (el<HTMLTextAreaElement>("nueva-nota")).value.trim();
      try {
        await agregarNota(paciente.id, texto);
        mostrarMensaje(mensaje, "Nota agregada");
        await verDetalle(paciente.id);
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
    document.querySelectorAll<HTMLButtonElement>("[data-editar-nota]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const notaId = btn.dataset.editarNota;
        if (!notaId) return;
        const nuevo = prompt("Nuevo contenido");
        if (nuevo === null) return;
        try {
          await editarNota(paciente.id, notaId, nuevo);
          mostrarMensaje(mensaje, "Nota actualizada");
          await verDetalle(paciente.id);
        } catch (error) {
          mostrarMensaje(mensaje, (error as Error).message, true);
        }
      });
    });
  }
}

function escape(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

window.addEventListener("DOMContentLoaded", () => {
  if (currentUser() && getToken()) {
    void renderApp();
  } else {
    renderLogin();
  }
});