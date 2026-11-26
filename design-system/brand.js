// Nombre de la aplicación (ver brand.md).
const BRAND_NAME = "sonrisa.app";

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