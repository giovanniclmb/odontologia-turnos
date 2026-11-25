// Nombre de trabajo provisional del consultorio.
// Reemplazar esta constante cuando se defina el nombre comercial real.
const BRAND_NAME = "Consultorio odontológico (nombre por definir)";

function applyBrandName() {
  document.querySelectorAll("[data-brand-name]").forEach(function (element) {
    element.textContent = BRAND_NAME;
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", applyBrandName, { once: true });
} else {
  applyBrandName();
}