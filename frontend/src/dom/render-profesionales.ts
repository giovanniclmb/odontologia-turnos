import type { Profesional } from "../api/types.js";

export function renderOpcionesProfesionales(profesionales: readonly Profesional[]): string {
  return profesionales.map((profesional) =>
    `<option value="${escapeHtml(profesional.id)}">${escapeHtml(profesional.codigo)}</option>`
  ).join("");
}

export function renderProfesionalesAsignados(ids: readonly string[], profesionales: readonly Profesional[]): string {
  const codigoPorId = new Map(profesionales.map((profesional) => [profesional.id, profesional.codigo]));
  const asignados = ids.map((id) => codigoPorId.get(id) ?? "Profesional no disponible").join(", ") || "Sin asignaciones";
  return escapeHtml(asignados);
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
