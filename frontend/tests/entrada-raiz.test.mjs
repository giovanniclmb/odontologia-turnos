import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";
import { test } from "node:test";

const raiz = new URL("../../", import.meta.url);

test("la raíz del servidor de desarrollo abre la aplicación", () => {
  const entrada = new URL("index.html", raiz);
  assert.ok(existsSync(entrada), "falta index.html en la raíz del repositorio");

  const destino = /http-equiv="refresh" content="0; url=([^"]+)"/.exec(readFileSync(entrada, "utf8"))?.[1];

  assert.equal(destino, "frontend/public/index.html");
  assert.ok(existsSync(new URL(destino, raiz)), "el destino de la redirección no existe");
});
