# Asignación automática de Issues

Diseño aprobado por Giovanni el 19/11/2026. Esta configuración se prepara localmente; solo se activa al publicar e integrar el workflow en la rama predeterminada del repositorio. No se creó una automatización de Codex ni un monitoreo que dependa de una computadora local.

## Comportamiento

[assign-roadmap.yml](../.github/workflows/assign-roadmap.yml) comprueba cada hora, en el minuto 17, los colaboradores con escritura. GitHub puede demorar una ejecución programada; no se promete detección instantánea. Las ejecuciones programadas de repositorios públicos pueden deshabilitarse tras 60 días de inactividad.

El asignador lee responsables de las fichas de [CHANGES.md](../CHANGES.md) y sus usuarios de la tabla Equipo en [CONTRIBUTING.md](../CONTRIBUTING.md). Encuentra Issues abiertos mediante el marcador `<!-- odontologia-change:C-XX -->` utilizado al crearlos. No depende de `.atl/`, que no está versionado, ni supone que el número de Issue coincide con el change.

- Si el integrante todavía no tiene acceso activo con escritura, deja sus Issues pendientes.
- Asigna únicamente Issues sin responsables. Conserva las asignaciones existentes, aunque difieran del plan; una reasignación manual corresponde al líder.
- Omite PRs, Issues cerrados y marcadores ajenos al reparto. Repetir la ejecución no duplica asignaciones.
- Rechaza responsables desconocidos y marcadores duplicados antes de escribir. Un error de API termina la ejecución con fallo, sin interpretarlo como invitación pendiente; escrituras anteriores confirmadas no se revierten y el próximo intento puede continuar.
- Relee cada Issue antes de asignar para respetar cambios manuales recientes y usa la operación de añadir responsable, sin reemplazar otros. GitHub no ofrece una asignación condicional atómica: si una persona asigna entre esa última lectura y el POST, pueden quedar ambos responsables; el bot nunca elimina al asignado manualmente.
- No modifica dependencias, estados, etiquetas, invitaciones ni permisos del equipo. Tener un Issue asignado no autoriza implementar sin dependencias integradas y aprobación del alcance.

Cada ejecución deja un resumen en Actions. La concurrencia serializa este workflow. El token efímero del repositorio tiene `contents: read` e `issues: write`; no se añade un PAT ni se habilitan permisos de administración. No se ejecuta con escritura desde eventos de PR.

## Ejecución manual y pruebas

En GitHub: Actions → Asignar Issues del roadmap → Run workflow. `dry_run` está activado por defecto: muestra qué haría sin escribir. Desactivarlo aplica las asignaciones permitidas. Para detener el proceso, deshabilitar este workflow desde Actions.

Desde una copia local, con Python 3 y GitHub CLI autenticado:

```sh
python3 -m unittest discover -s scripts/tests -v
python3 scripts/assign_roadmap.py --repo giovanniclmb/odontologia-turnos
```

El segundo comando solo consulta y simula. Agregar `--apply` permite escribir; no usarlo para una verificación de lectura. En Actions, `GITHUB_REPOSITORY` identifica el repositorio y `GH_TOKEN` recibe el token efímero. CI ejecuta las pruebas del asignador sin credenciales ni acceso a GitHub; el workflow horario también las ejecuta antes de aplicar.

Verificación local del 19/11/2026: nueve pruebas correctas tras RED por asignador ausente. Simulación real sin escrituras: doce Issues pendientes para Fabi/Sofia y once con asignación existente conservada. Ambos workflows pasan actionlint 1.7.7 (sintaxis y expresiones; sin shellcheck/pyflakes). El token de Actions y la ejecución programada solo pueden comprobarse al publicar; no se presentan como verificados por pruebas locales.

Fuentes: [ejecución programada](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#schedule), [token efímero](https://docs.github.com/en/actions/concepts/security/github_token), [colaboradores](https://docs.github.com/en/rest/collaborators/collaborators#list-repository-collaborators) y [añadir responsables](https://docs.github.com/en/rest/issues/assignees#add-assignees-to-an-issue).
