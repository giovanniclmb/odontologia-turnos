# C-11 — Correcciones de revisión de PR #35

Fecha: 24/11/2026 (America/Mendoza).

## Alcance y autorización

Giovanni pidió corregir C-11 con un subagente Luna de razonamiento medio y cerrar el archivo e integración después de verificar. Worker: `gpt-6-luna`, `medium`; la sesión principal coordina y revisa de forma independiente. Se conserva a Fabi como responsable del change y autor del PR.

La rama de corrección parte de `0dff9ec` y recibió `main` en `a2426b4` sin conflictos. Solo C-11: HTML seguro, interfaz de pacientes/ficha y sus verificaciones. No se modifican migraciones, reglas de agenda, identidad ni permisos de dominio.

C-11 ya estaba archivado en `openspec/changes/archive/2026-11-23-interfaz-pacientes-y-ficha/`. `openspec list --json` confirmó que no hay changes activos. Esta corrección conserva ese archivo y añade evidencia posterior; no crea un archivo duplicado ni afirma que la verificación histórica incompleta fuera suficiente.

## Correcciones

- Los códigos de profesionales se escapan al construir las opciones y al mostrar los profesionales asignados. Se verifican los dos caminos, incluidos caracteres que podrían cerrar etiquetas o atributos.
- La lista de pacientes utiliza celdas con `textContent`. Se conserva el escape de datos administrativos, antecedentes, notas y errores.
- La interfaz reutiliza tokens y marca provisional de la revisión 1 aprobada. Listado, alta y detalle se muestran según la acción, con controles correspondientes al rol y autoría real.
- Los UUID de profesionales se resuelven a sus códigos del catálogo existente. No se añaden datos o estados al modelo de pacientes.
- La carga fallida de ficha se distingue de una ficha todavía no creada y permite reintentar. El detalle conserva salida al listado durante carga o error.
- Se corrigen las rutas relativas de nueve HTML de mockups que quedaron rotas al archivarlos. Los tokens, recursos y diseño aprobado se conservan.
- `frontend-verify` añade compilación y pruebas a CI con Node 24 y acciones fijadas por SHA.

## Pruebas

| Comprobación | Evidencia observada |
| --- | --- |
| API real focalizada | `mvn -f backend/pom.xml -Dit.test=ApiPacientesIT clean verify`: 114 unitarias y 5 IT, sin fallos, errores ni omisiones. |
| Backend completo integrado | `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify`: 114 unitarias + 181 IT = **295**, cero fallos, errores u omisiones; 2 min 28 s. |
| Contratos HTTP | Roles `recepcion`/`odontologo`, login inválido 401, CORS permitido/rechazado, alcance de pacientes, recepción sin edición clínica, rechazo de nota ajena, nota vacía y revocación por logout. Filtro, controladores, servicios y PostgreSQL reales. |
| Regresiones de HTML | `npm test`: compilación TypeScript estricta y **2/2** pruebas `node:test` correctas, comprobadas también por la sesión principal. Worker observó el fallo esperado antes de corregir selector y asignados. |
| Roadmap | `python3 -m unittest discover -s scripts/tests -v`: **9/9** correctas. |
| OpenSpec | `openspec validate --specs`: 9/9 correctas. Permanece el aviso histórico de texto de requisito mayor a 500 caracteres en acceso interno. |

Los logs completos de ejecución local están en `/tmp/odonto-c11-http.log`, `/tmp/odonto-c11-full-verify.log` y `/tmp/odonto-c11-roadmap-tests.log`; son temporales. Este documento conserva comandos, resultados y límites. Las primeras comprobaciones de frontend fallaron por `tsc` no instalado; `npm ci` instaló la dependencia ya fijada en el lockfile.

## Verificación visual e interactiva

Sistema y mockups: revisión 1, aprobación previamente registrada. Se verifica con API sobre PostgreSQL desechable y usuarios/pacientes totalmente ficticios de `ApiC11Fixture`; no se usa una base compartida ni se envían emails.

Estado: **verificación completada con diferencias menores documentadas**, sin bloqueantes detectados en el alcance revisado. Worker y sesión principal comprobaron los flujos; la sesión principal repitió login inválido/válido, edición de nota propia, alta y edición administrativa, asignar/quitar, errores y ambos caminos de códigos hostiles.

| Pantalla o estado | 1440×900 y 390×844 | Resultado |
| --- | --- | --- |
| Login normal | `capturas/login-{desktop,mobile}.png` | Formulario y marca provisional renderizados. |
| Login rechazado | `capturas/login-error-{desktop,mobile}.png` | 401 genérico, permite corregir credenciales. |
| Listado recepción | `capturas/pacientes-lista-recepcion-{desktop,mobile}.png` | Búsqueda real y códigos de profesionales. |
| Listado odontología | `capturas/pacientes-lista-odontologo-{desktop,mobile}.png` | Alcance asignado, sin gestión de asignaciones. |
| Alta | `capturas/paciente-alta-recepcion-{desktop,mobile}.png` | Alta ficticia real y búsqueda posterior. |
| Detalle, edición y asignaciones | `capturas/paciente-detalle-recepcion-{desktop,mobile}.png` | Edición administrativa y asignar/quitar comprobados; clínica de solo lectura. |
| Antecedentes y notas | `capturas/ficha-odontologo-{desktop,mobile}.png` | Tres notas: dos propias con Editar y una ajena sin ese control; edición propia real correcta. |
| Lista vacía | `capturas/listado-vacio-{desktop,mobile}.png` | Mensaje contextual, no una tabla con datos anteriores. |
| Carga y error de detalle | `capturas/detalle-{cargando,error}-{desktop,mobile}.png` | Siempre permite Volver. |
| Error de ficha y recuperación | `capturas/error-ficha-{desktop,mobile}.png` | Error explícito, sin falsa ficha vacía; Reintentar recupera la lectura real. |

Los estados de error 500 y la espera de carga se provocaron mediante interceptación local de respuestas en Playwright; los flujos positivos usaron el servidor y PostgreSQL reales. El rechazo de credenciales fue un 401 real. En la sesión principal hubo cero excepciones JavaScript; los tres errores de consola corresponden exactamente al 401 y los dos 500 provocados. Todos los HTML, CSS, módulos, marca y logo solicitados respondieron 200. En móvil, el documento midió 390 px para un viewport de 390 px; la tabla conserva desplazamiento interno.

**HTML seguro en navegador:** se interceptó el catálogo con un código que intentaba cerrar `option` y `select` e insertar una imagen con manejador. Resultado observado: una sola opción para ese profesional, texto literal también en asignados, cero elementos inyectados y ninguna ejecución. Worker guardó una nota y antecedente con HTML en el fixture real; la sesión principal confirmó su lectura literal por recepción, cero imágenes clínicas y ausencia de controles clínicos de edición. Captura adicional: `capturas/xss-profesionales-literal-desktop.png`.

### Comparación con referencias aprobadas

Se reutiliza revisión 1 sin modificar tokens, `brand.js` ni logo. Se contrastaron las referencias archivadas de login, listado, alta, detalle, ficha/antecedentes/notas y estados con las vistas contextuales implementadas; `capturas/referencia-pacientes-lista-desktop.png` conserva una referencia renderizada. La inspección incluyó las capturas finales en escritorio y móvil.

Diferencias menores que se mantienen explícitas:

- La búsqueda conserva campos separados de código exacto y nombre, según el contrato existente, en vez del único campo ilustrativo del mockup.
- No se muestran los estados ilustrativos Activo/Inactivo del paciente: no forman parte del modelo aprobado. Se muestran las asignaciones reales.
- Los ejemplos de mockup apilan variantes y estados; la aplicación presenta la variante aplicable al usuario autenticado. No incorpora un selector que permita cambiar de rol.
- Datos administrativos, antecedentes y notas se agrupan en el detalle del paciente. Conservan las operaciones y restricciones de las referencias; la altura depende del contenido y en móvil se apilan.
- La marca sigue siendo provisional. El catálogo proporciona códigos, no nombres personales de odontólogos. Las notas conservan su identificador de autor para distinguir autores ajenos; las propias añaden el usuario conectado. Las fechas usan la zona del navegador.

No se efectuó una comparación automática píxel a píxel ni se probó Safari/Firefox. La evidencia corresponde a Chromium/Playwright, los dos tamaños indicados y datos ficticios; no garantiza ausencia de otros defectos.

## Publicación e integración

Verificación local final completada y entornos temporales cerrados. El usuario pidió integrar después de corregir. La publicación y aprobación se contrastarán con el head final y los checks de GitHub; el PR #35 conserva el estado remoto y la trazabilidad del cierre de Issue #12. No interpretar esta evidencia local como prueba de un head posterior ni como integración remota ya realizada.
