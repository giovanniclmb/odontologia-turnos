# Verificación en un clon limpio

Fecha: 25/11/2026, 18:29 (-03). Ejecutada por el agente en el equipo de Giovanni.

## Qué se probó

Se clonó la rama `docs/entrega-academica` en el commit `30f105d`, en una carpeta temporal sin `.env`, y se ejecutaron desde su raíz los comandos del [README](../../README.md#clonar-y-ejecutar-las-pruebas). El código de `backend/`, `frontend/` y `scripts/` es el de `main` en `6aa8796`; los commits posteriores a `30f105d` solo modifican documentación.

| Comando | Resultado |
| --- | --- |
| `mvn -f backend/pom.xml test -Dtest=AgendaTest` | 55 pruebas de C-01; sin fallos, errores ni omisiones |
| `./scripts/check-docker.sh` | Correcto |
| `mvn -f backend/pom.xml clean verify` | 161 unitarias y 331 de integración; sin fallos, errores ni omisiones; `BUILD SUCCESS` en 3 min 6 s |
| `npm --prefix frontend ci` | Correcto |
| `npm --prefix frontend test` | 82 pruebas; sin fallos ni omisiones |
| `python3 -m unittest discover -s scripts/tests` | 9 pruebas correctas |

## Entorno

| Herramienta | Versión |
| --- | --- |
| macOS | 27.0.1 |
| OpenJDK | 25.0.4 |
| Maven | 3.9.16 |
| Node | 24.19.0 |
| Python | 3.14.7 |
| Docker | 29.7.2 |

## Segunda corrida: `main` con C-13

Fecha: 25/11/2026, por la noche. Clon limpio y sin `.env` del commit `430f1b9`, en el mismo equipo y sin conexión.

| Comando | Resultado |
| --- | --- |
| `mvn -f backend/pom.xml test -Dtest=AgendaTest` | 55 pruebas de C-01; sin fallos, errores ni omisiones |
| `./scripts/check-docker.sh` | Correcto |
| `mvn -f backend/pom.xml clean verify`, excluyendo `ArranqueJarIT` | 161 unitarias y 335 de integración correctas; `BUILD SUCCESS` en 3 min 21 s |
| `npm --prefix frontend ci` | Correcto |
| `npm --prefix frontend test` | 100 pruebas; sin fallos ni omisiones |
| `python3 -m unittest discover -s scripts/tests` | 9 pruebas correctas |

`ArranqueJarIT` se excluyó con `-Dit.test='!ArranqueJarIT'` porque arranca el JAR en el puerto 8080, ocupado en ese momento por una demostración: **esa prueba no se verificó en esta corrida**. El filtro hace además que Failsafe repita las unitarias e incluya tres pruebas manuales de envío real, que quedaron omitidas porque no se definió su confirmación; no se envió ningún email.

La integración continua de GitHub sobre `430f1b9` figuraba correcta ese día.

Con el mismo código en la copia principal se siguieron los pasos de [Ver la aplicación en local](../../README.md#ver-la-aplicación-en-local): la API arrancó, la interfaz respondió y una ruta protegida pidió sesión. No se recorrieron las pantallas.

## Límites

- Maven y npm corrieron **sin conexión**, con las dependencias ya descargadas en ese equipo (`MAVEN_ARGS=-o` y `npm_config_offline=true`). No se probó la primera descarga en una máquina nueva.
- Es un solo equipo. La integración continua de `main` en `6aa8796` figuraba correcta en GitHub ese día.
- Las pruebas de integración usan PostgreSQL temporal. No se enviaron emails reales.
- No se repitió el recorrido manual de la interfaz.

La evidencia propia del change académico, con sus pasos RED y GREEN del 19/11, está en [Verify de C-01](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/evidence/verify.md).
