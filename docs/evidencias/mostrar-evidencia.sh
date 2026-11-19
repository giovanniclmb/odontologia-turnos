#!/usr/bin/env bash
set -euo pipefail
evidence_dir="$(cd "$(dirname "$0")" && pwd)"
case "${1:-}" in
  instalacion)
    printf 'ACTIVE STACK FULL PARA CODEX\nRegistro de la instalación realizada; no es una reinstalación.\n\n'
    tail -n 16 "$evidence_dir/active-stack-codex-full.log"
    ;;
  harnesses)
    printf 'HARNESSES FULL PARA CODEX\nPasos completados según el registro de instalación:\n\n'
    awk '/✓ / {sub(/^  ✓ /, ""); if ($0 != "snapshot" && $0 != "self-install" && $0 != "verify-hook") print $0}' "$evidence_dir/active-stack-codex-full.log"
    ;;
  mcp)
    node -e 'const fs=require("fs");const r=JSON.parse(fs.readFileSync(process.argv[1],"utf8")); console.log("VERIFICACION MCP\n"+r.checked_at+"\n");for(const s of r.results)console.log(s.name+": "+s.status+" | "+(s.server?.version||"?")+" | "+s.tool_count+" herramientas");console.log("\n"+r.method);' "$evidence_dir/mcp-verificacion.json"
    ;;
  *)
    printf 'Uso: bash docs/evidencias/mostrar-evidencia.sh instalacion|harnesses|mcp\n'
    exit 1
    ;;
esac
