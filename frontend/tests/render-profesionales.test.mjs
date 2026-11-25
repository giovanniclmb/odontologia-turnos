import assert from "node:assert/strict";
import { test } from "node:test";
import { renderOpcionesProfesionales, renderProfesionalesAsignados } from "../dist/dom/render-profesionales.js";

test("renderiza códigos y valores de profesionales como texto HTML inerte", () => {
  const html = renderOpcionesProfesionales([
    { id: 'profesional" autofocus onfocus="alert(1)', codigo: '<img src=x onerror=alert(1)>' }
  ]);

  assert.equal(
    html,
    '<option value="profesional&quot; autofocus onfocus=&quot;alert(1)">&lt;img src=x onerror=alert(1)&gt;</option>'
  );
});

test("renderiza los códigos de profesionales asignados como texto HTML inerte", () => {
  const html = renderProfesionalesAsignados(["profesional-1"], [
    { id: "profesional-1", codigo: '<img src=x onerror="alert(1)">', activo: true }
  ]);

  assert.equal(html, "&lt;img src=x onerror=&quot;alert(1)&quot;&gt;");
});
