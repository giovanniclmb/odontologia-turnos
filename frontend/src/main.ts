import { currentUser, login, logout } from "./auth/auth.js";
import { getToken } from "./api/client.js";
import { buscarPacientes, crearPaciente, consultarPaciente, actualizarPaciente, asignarProfesional, desasignarProfesional, listarProfesionales } from "./pacientes/pacientes.js";
import { consultarFicha, actualizarAntecedentes, agregarNota, editarNota } from "./ficha/ficha.js";
import type { Paciente, Profesional, Ficha } from "./api/types.js";
import { ApiError } from "./api/client.js";
import { renderOpcionesProfesionales, renderProfesionalesAsignados } from "./dom/render-profesionales.js";

function el<T extends HTMLElement>(id: string): T {
  const node = document.getElementById(id);
  if (!node) throw new Error(`Elemento no encontrado: ${id}`);
  return node as T;
}

function mostrarMensaje(contenedor: HTMLElement, mensaje: string, error = false): void {
  contenedor.textContent = mensaje;
  contenedor.className = error ? "mensaje error" : "mensaje";
}

function formatearFecha(fechaIso: string): string {
  const fecha = new Date(fechaIso);
  return Number.isNaN(fecha.getTime())
    ? fechaIso
    : new Intl.DateTimeFormat("es-AR", { dateStyle: "short", timeStyle: "short" }).format(fecha);
}

function aplicarNombreMarca(): void {
  (window as Window & { applyBrandName?: () => void }).applyBrandName?.();
}

function esRecepcion(): boolean {
  return currentUser()?.rol === "recepcion";
}

function renderLogin(): void {
  const root = el<HTMLDivElement>("app");
  root.innerHTML = `
    <section class="login-card card" aria-labelledby="login-title">
      <div class="brand login-brand">
        <img src="../../design-system/assets/logo.svg" alt="Logo provisional" width="32" height="32">
        <div><strong data-brand-name></strong><small>Acceso al sistema</small></div>
      </div>
      <h1 id="login-title">Iniciar sesión</h1>
      <form id="form-login" class="form-stack">
        <div class="field"><label for="usuario">Usuario</label><input class="input" id="usuario" autocomplete="username" required></div>
        <div class="field"><label for="contrasena">Contraseña</label><input class="input" id="contrasena" type="password" autocomplete="current-password" required></div>
        <button type="submit" class="btn btn-primary">Ingresar</button>
      </form>
      <p id="login-mensaje" class="mensaje" role="alert"></p>
    </section>
  `;
  aplicarNombreMarca();
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
    <div class="app-shell">
      <aside class="sidebar" aria-label="Navegación principal">
        <div class="brand"><img src="../../design-system/assets/logo.svg" alt="Logo provisional"><strong data-brand-name></strong></div>
        <nav class="nav" aria-label="Secciones">
          <a href="#pacientes" id="nav-pacientes" aria-current="page">Pacientes</a>
          <span aria-disabled="true">Agenda</span>
          <span aria-disabled="true">Ficha clínica</span>
          <span aria-disabled="true">Lista de espera</span>
          <span aria-disabled="true">Configuración</span>
        </nav>
      </aside>
      <div class="main">
      <header class="topbar">
        <span class="topbar-title">Pacientes</span>
        <div class="topbar-user">
        <span>${escape(user?.nombreUsuario ?? "")} (${escape(user?.rol ?? "")})</span>
        <button id="btn-logout" class="btn btn-secondary">Cerrar sesión</button>
        </div>
      </header>
      <main class="content" id="pacientes">
        <div id="vista-lista" class="content-view">
          <section class="page-header">
            <div><h1>Pacientes</h1><p>Datos administrativos básicos. Sin contenido clínico.</p></div>
            <button id="btn-nuevo-paciente" class="btn btn-primary" type="button">+ Nuevo paciente</button>
          </section>
          <section class="card">
            <div class="card-header"><div><h2 class="card-title">Búsqueda</h2><p class="card-subtitle">Busque por código exacto o por nombre y apellido.</p></div></div>
            <form id="form-busqueda" class="search-form">
              <div class="field"><label for="busqueda-codigo">Código exacto</label><input class="input" id="busqueda-codigo" placeholder="Ej.: P-001"></div>
              <div class="field"><label for="busqueda-texto">Nombre o apellido</label><input class="input" id="busqueda-texto" placeholder="Buscar paciente"></div>
              <button type="submit" class="btn btn-secondary">Buscar</button>
            </form>
          </section>
          <section class="card" aria-labelledby="listado-titulo">
            <div class="card-header"><div><h2 id="listado-titulo" class="card-title">Listado de pacientes</h2><p class="card-subtitle">Pacientes disponibles según su perfil y asignaciones.</p></div></div>
            <div class="table-wrap"><table><thead><tr><th>Código</th><th>Nombre</th><th>Apellido</th><th>Email</th><th>Profesionales</th><th>Acciones</th></tr></thead><tbody id="lista-pacientes"></tbody></table></div>
            <p id="estado-listado" class="empty-state" role="status" hidden></p>
          </section>
        </div>
        <div id="vista-alta" class="content-view" hidden>
          <section class="page-header">
            <div><h1>Nuevo paciente</h1><p>Código, nombre, apellido y email obligatorios; teléfono opcional.</p></div>
            <button id="btn-cancelar-alta" class="btn btn-secondary" type="button">Volver al listado</button>
          </section>
          <section class="card" aria-labelledby="alta-titulo">
            <div class="card-header"><div><h2 id="alta-titulo" class="card-title">Datos administrativos</h2><p class="card-subtitle">El alta por odontología queda sin asignación automática.</p></div></div>
            <form id="form-alta" class="patient-form">
              <div class="field"><label for="alta-codigo">Código *</label><input class="input" id="alta-codigo" required></div>
              <div class="field"><label for="alta-nombre">Nombre *</label><input class="input" id="alta-nombre" required></div>
              <div class="field"><label for="alta-apellido">Apellido *</label><input class="input" id="alta-apellido" required></div>
              <div class="field"><label for="alta-email">Email *</label><input class="input" id="alta-email" type="email" required></div>
              <div class="field"><label for="alta-telefono">Teléfono</label><input class="input" id="alta-telefono" type="tel"></div>
              <div class="form-actions"><button type="submit" class="btn btn-primary">Guardar paciente</button></div>
            </form>
            <p id="alta-mensaje" class="mensaje" role="status"></p>
          </section>
        </div>
        <section class="card detail-card" id="detalle" hidden></section>
      </main>
      </div>
    </div>
  `;
  aplicarNombreMarca();
  el<HTMLButtonElement>("btn-logout").addEventListener("click", async () => {
    await logout();
    renderLogin();
  });
  el<HTMLAnchorElement>("nav-pacientes").addEventListener("click", (event) => {
    event.preventDefault();
    mostrarListado();
  });
  el<HTMLButtonElement>("btn-nuevo-paciente").addEventListener("click", mostrarAlta);
  el<HTMLButtonElement>("btn-cancelar-alta").addEventListener("click", mostrarListado);
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
  const estado = el<HTMLParagraphElement>("estado-listado");
  lista.innerHTML = "";
  estado.hidden = true;
  try {
    const [pacientes, profesionales] = await Promise.all([
      buscarPacientes(codigo, texto),
      listarProfesionales().catch(() => [])
    ]);
    const codigoPorProfesional = new Map(profesionales.map((profesional) => [profesional.id, profesional.codigo]));
    if (pacientes.length === 0) {
      estado.textContent = "No se encontraron pacientes con ese criterio. Pruebe otra búsqueda o cree un paciente.";
      estado.hidden = false;
    }
    for (const p of pacientes) {
      const item = document.createElement("tr");
      const profesionalesAsignados = p.profesionalesAsignados
        .map((id) => codigoPorProfesional.get(id) ?? "Profesional no disponible")
        .join(", ") || "Sin asignar";
      for (const value of [p.codigo, p.nombre, p.apellido, p.email, profesionalesAsignados]) {
        const cell = document.createElement("td");
        cell.textContent = value;
        item.appendChild(cell);
      }
      const actions = document.createElement("td");
      const btn = document.createElement("button");
      btn.textContent = "Ver detalle";
      btn.className = "btn btn-ghost btn-sm";
      btn.addEventListener("click", () => void verDetalle(p.id));
      actions.appendChild(btn);
      item.appendChild(actions);
      lista.appendChild(item);
    }
  } catch (error) {
    estado.textContent = (error as Error).message;
    estado.hidden = false;
  }
}

async function verDetalle(pacienteId: string): Promise<void> {
  const detalle = el<HTMLElement>("detalle");
  el<HTMLElement>("vista-lista").hidden = true;
  el<HTMLElement>("vista-alta").hidden = true;
  detalle.hidden = false;
  detalle.innerHTML = renderEstadoDetalle("Cargando detalle del paciente…");
  document.getElementById("btn-volver-listado")?.addEventListener("click", mostrarListado);
  try {
    const paciente = await consultarPaciente(pacienteId);
    let ficha: Ficha | null = null;
    let fichaError = false;
    try {
      ficha = await consultarFicha(pacienteId);
    } catch (error) {
      if (!(error instanceof ApiError) || error.status !== 404) fichaError = true;
    }
    const profesionales = await listarProfesionales().catch(() => []);
    detalle.innerHTML = renderDetalle(paciente, ficha, profesionales, fichaError);
    adjuntarEventosDetalle(paciente, profesionales);
  } catch (error) {
    detalle.innerHTML = renderEstadoDetalle((error as Error).message, true);
    document.getElementById("btn-volver-listado")?.addEventListener("click", mostrarListado);
  }
}

function renderEstadoDetalle(mensaje: string, error = false): string {
  return `
    <header class="detail-header">
      <div><h2>Detalle del paciente</h2></div>
      <button id="btn-volver-listado" class="btn btn-secondary" type="button">Volver</button>
    </header>
    <p class="mensaje${error ? " error" : ""}" role="${error ? "alert" : "status"}">${escape(mensaje)}</p>
  `;
}

function renderDetalle(paciente: Paciente, ficha: Ficha | null, profesionales: Profesional[], fichaError: boolean): string {
  const asignados = renderProfesionalesAsignados(paciente.profesionalesAsignados, profesionales);
  const recepcion = esRecepcion();
  const usuario = currentUser();
  const notasHtml = (ficha?.notas ?? []).map((n) => `
    <li>
      <p>${escape(n.texto)}</p>
      <small>Autor: ${escape(n.autorId === usuario?.usuarioId ? `${usuario.nombreUsuario} (${n.autorId})` : `Otro profesional (${n.autorId})`)} · ${escape(formatearFecha(n.creadaEn))}</small>
      ${!recepcion && n.autorId === usuario?.usuarioId ? `<button class="btn btn-ghost btn-sm" data-editar-nota="${escape(n.id)}">Editar</button>` : ""}
    </li>
  `).join("");
  return `
    <header class="detail-header">
      <div><h2>${escape(paciente.nombre)} ${escape(paciente.apellido)}</h2><p class="card-subtitle">Código ${escape(paciente.codigo)} · Detalle administrativo y ficha clínica.</p></div>
      <button id="btn-volver-listado" class="btn btn-secondary" type="button">Volver</button>
    </header>
    <section class="detail-section">
      <h3>Datos administrativos</h3>
      <form id="form-editar-paciente" class="detail-form">
        <div class="field"><label for="edit-nombre">Nombre</label><input class="input" id="edit-nombre" value="${escape(paciente.nombre)}" required></div>
        <div class="field"><label for="edit-apellido">Apellido</label><input class="input" id="edit-apellido" value="${escape(paciente.apellido)}" required></div>
        <div class="field"><label for="edit-email">Email</label><input class="input" id="edit-email" type="email" value="${escape(paciente.email)}" required></div>
        <div class="field"><label for="edit-telefono">Teléfono</label><input class="input" id="edit-telefono" value="${escape(paciente.telefono ?? "")}"></div>
        <div class="form-actions"><button class="btn btn-primary" type="submit">Guardar datos</button></div>
      </form>
    </section>
    <section class="detail-section">
      <h3>Profesionales asignados</h3>
      <p>${asignados}</p>
    ${recepcion ? `
        <div class="assignment-actions">
        <label for="select-profesional">Profesional</label>
        <select id="select-profesional" class="select">
          ${renderOpcionesProfesionales(profesionales)}
        </select>
        <button id="btn-asignar" class="btn btn-secondary" type="button">Asignar</button>
        <button id="btn-desasignar" class="btn btn-ghost" type="button">Quitar</button>
        </div>
    ` : ""}
    </section>
    <section class="detail-section clinical-section">
      <h3>Ficha clínica</h3>
      ${fichaError ? `
        <p class="mensaje error" role="alert">No se pudo cargar la ficha clínica. Reintente la consulta.</p>
        <button id="btn-reintentar-ficha" class="btn btn-secondary" type="button">Reintentar</button>
      ` : recepcion ? "" : `
        <label for="antecedentes">Antecedentes</label>
        <textarea class="input" id="antecedentes">${escape(ficha?.antecedentes ?? "")}</textarea>
        <button id="btn-guardar-antecedentes" class="btn btn-primary" type="button">Guardar antecedentes</button>
      `}
      ${fichaError ? "" : `<p class="antecedentes-lectura">${escape(ficha?.antecedentes ?? "Sin antecedentes")}</p>
        <h4>Notas</h4><ul>${notasHtml}</ul>`}
      ${fichaError || recepcion ? "" : `
        <label for="nueva-nota">Nueva nota</label>
        <textarea class="input" id="nueva-nota"></textarea>
        <button id="btn-agregar-nota" class="btn btn-primary" type="button">Agregar nota</button>
      `}
    </section>
    <p id="detalle-mensaje" class="mensaje" role="status"></p>
  `;
}

function mostrarAlta(): void {
  el<HTMLElement>("vista-lista").hidden = true;
  el<HTMLElement>("vista-alta").hidden = false;
  el<HTMLElement>("detalle").hidden = true;
  el<HTMLInputElement>("alta-codigo").focus();
}

function mostrarListado(): void {
  el<HTMLElement>("vista-lista").hidden = false;
  el<HTMLElement>("vista-alta").hidden = true;
  el<HTMLElement>("detalle").hidden = true;
}

function adjuntarEventosDetalle(paciente: Paciente, profesionales: Profesional[]): void {
  const mensaje = el<HTMLParagraphElement>("detalle-mensaje");
  const formEditar = document.getElementById("form-editar-paciente") as HTMLFormElement | null;
  document.getElementById("btn-volver-listado")?.addEventListener("click", mostrarListado);
  document.getElementById("btn-reintentar-ficha")?.addEventListener("click", () => void verDetalle(paciente.id));
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
      mostrarMensaje(el<HTMLParagraphElement>("detalle-mensaje"), "Datos actualizados");
    } catch (error) {
      mostrarMensaje(mensaje, (error as Error).message, true);
    }
  });

  if (esRecepcion()) {
    document.getElementById("btn-asignar")?.addEventListener("click", async () => {
      const select = el<HTMLSelectElement>("select-profesional");
      try {
        await asignarProfesional(paciente.id, select.value);
        await verDetalle(paciente.id);
        mostrarMensaje(el<HTMLParagraphElement>("detalle-mensaje"), "Profesional asignado");
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
    document.getElementById("btn-desasignar")?.addEventListener("click", async () => {
      const select = el<HTMLSelectElement>("select-profesional");
      try {
        await desasignarProfesional(paciente.id, select.value);
        await verDetalle(paciente.id);
        mostrarMensaje(el<HTMLParagraphElement>("detalle-mensaje"), "Asignación quitada");
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
  } else {
    document.getElementById("btn-guardar-antecedentes")?.addEventListener("click", async () => {
      const texto = (el<HTMLTextAreaElement>("antecedentes")).value;
      try {
        await actualizarAntecedentes(paciente.id, texto);
        await verDetalle(paciente.id);
        mostrarMensaje(el<HTMLParagraphElement>("detalle-mensaje"), "Antecedentes guardados");
      } catch (error) {
        mostrarMensaje(mensaje, (error as Error).message, true);
      }
    });
    document.getElementById("btn-agregar-nota")?.addEventListener("click", async () => {
      const texto = (el<HTMLTextAreaElement>("nueva-nota")).value.trim();
      try {
        await agregarNota(paciente.id, texto);
        await verDetalle(paciente.id);
        mostrarMensaje(el<HTMLParagraphElement>("detalle-mensaje"), "Nota agregada");
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
          await verDetalle(paciente.id);
          mostrarMensaje(el<HTMLParagraphElement>("detalle-mensaje"), "Nota actualizada");
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
