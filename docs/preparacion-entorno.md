# Preparación del entorno

Fecha: 17 de noviembre de 2026. Alcance: etapa 0 de la consigna DOCX. Por decisión explícita del usuario, el agente de trabajo es Codex.

## Revisión de la consigna

El trabajo requiere preparar herramientas, fundar el proyecto con Active Orchestrator, investigar al menos 15 competidores con fuentes verificables, crear una base de conocimiento y roadmap, completar un único change OPSX con pruebas y archivo, y mostrar memoria entre sesiones mediante Engram. La preparación no implica aprobar ni ejecutar las etapas posteriores.

El Discovery es obligatorio. El stack de la aplicación se elige después de investigar. Las etapas usan el protocolo Continuar / Ajustar / Parar acá; no se usarán datos reales de pacientes. La reflexión final debe ser escrita por los integrantes.

## Herramientas verificadas

| Herramienta | Versión observada | Resultado |
| --- | --- | --- |
| Git | 2.54.0 | Ya disponible; repositorio inicializado en master, sin commits |
| Node.js | 24.19.0 | Ya disponible mediante nvm |
| npm | 12.0.2 | Ya disponible |
| Codex CLI | 0.167.0-alpha.4.1 | Ya disponible; la comprobación aislada dentro del sandbox advirtió que no pudo crear alias de PATH |
| Go | 1.27.4 darwin/arm64 | Instalado con Homebrew |
| OpenSpec | 2.1.0 | Instalado por Active Stack |
| Engram | 3.7.2 | Instalado; inicialización MCP y mem_save comprobados |
| Active Stack | e2ee756-local-npm-fix | Compilación local para macOS Apple Silicon, Full para Codex |

Se comprobó que config.toml es TOML válido y contiene los MCP engram, context7 y playwright. Engram fue probado mediante initialize y tools/call mem_save por stdio; la memoria de preparación quedó guardada como observación 1. Posteriormente los tres servidores respondieron a initialize y tools/list desde clientes MCP independientes. El resultado está en evidencias/mcp-verificacion.json y es reproducible con evidencias/verificar-mcp.py. La comprobación no demuestra carga en este chat ni ejecución del navegador de Playwright.

Se comprobó la presencia de SKILL.md para active-orchestrator, discovery-research, web-scraper, kb-creator, roadmap-generator, agent-instruction, skill-registry, find-skill, skill-creator, sdd-orchestrator, design-system, active-delivery y active-roadmap. Las habilidades globales están en ~/.agents/skills; la configuración global de Codex está en ~/.codex. El instalador toma respaldos en ~/.active-stack.

## Compilación e incidencias

Fuente oficial: https://github.com/Group-Active-IA/active-stack, commit e2ee756fb218c92845e4e68fa9731a8969f188e8. Clonado en /private/tmp/odonto-active-stack.

Se ejecutó build.sh. Su matriz solo genera Windows y Linux, por lo que se añadió una compilación nativa mediante go build. No se modificó la matriz original.

El primer intento para Claude, anterior a la aclaración del usuario, falló al solicitar sudo para instalar OpenSpec; informó rollback exitoso. Engram permaneció instalado como herramienta compartida. El registro se conserva en evidencias/active-stack-full.log.

El modo headless construía PlatformProfile solo con el sistema operativo y dejaba NpmWritable falso. En consecuencia solicitaba sudo aunque el prefijo npm estuviera dentro del directorio del usuario. La copia temporal fue corregida para reutilizar esa detección; no se cambió la política de permisos. Una prueba que simula un prefijo npm del usuario falló antes de corregir y pasó después. El parche y la prueba se conservan en evidencias/active-stack-npm-local.patch y evidencias/active-stack-npm-local-test.go.txt.

Los paquetes headless y system pasaron sus pruebas. La suite completa go test ./... no pasó: fallaron TestWindowsBundleThemeContainsMinimalInstallerPages, TestCheckout_RepetitionsStartIdenticalAndRestore, TestEvaluate_ReviewDispositionsAndMechanism, TestEvaluate_ReferenceAcceptedDefectiveRejected, TestDoc_InventoryReproducesGolden y TestDownloadBinary_APIError. No se corrigieron esos casos ajenos al alcance de preparación. La salida completa está en evidencias/active-stack-tests.log.

La instalación definitiva se ejecutó con install --agent codex --mode full --tier balanceado --yes --lang es. Terminó con código 0 y sin harnesses degradados en el registro. Su resumen imprime cero comprobaciones individuales; por eso se verificaron por separado versiones, habilidades, TOML y Engram. Registro: evidencias/active-stack-codex-full.log.

La revisión automática rechazó iniciar el instalador interactivo sin destino explícito. No se usó ese camino; la instalación definitiva fijó agente y modo en los argumentos.

## Pendientes para continuar

- Abrir una nueva sesión de Codex y comprobar que expone las habilidades y los tres MCP configurados.
- Capturar la instalación finalizada y la lista de harnesses para la evidencia académica. El control de pantalla bloquea Codex y Terminal; el navegador automatizado también rechazó abrir archivos locales. No se generaron capturas ni se simularon. Para mostrar la evidencia real en Terminal, desde la raíz del proyecto, ejecutar por separado `bash docs/evidencias/mostrar-evidencia.sh instalacion`, `bash docs/evidencias/mostrar-evidencia.sh harnesses` y `bash docs/evidencias/mostrar-evidencia.sh mcp`. Capturar cada salida con Cmd+Shift+4 y guardar las imágenes en docs/evidencias. La primera muestra el registro de una instalación ya finalizada, no una nueva ejecución del instalador. Actualización del 25/11/2026: las salidas de instalación y harnesses constan en [una captura](evidencias/captura-instalador.png).
- Invocar $active-orchestrator para iniciar la etapa 1 y revisar su propuesta de openspec init para Codex. No se ejecutó openspec init ni se creó una estructura OpenSpec manualmente.
- Continuar con Discovery obligatorio y revisar cada etapa antes de avanzar.

La inicialización de Engram vinculó esta carpeta al proyecto «integrador odonto» en .engram/config.json. No se creó un remoto Git ni se hicieron commits.
