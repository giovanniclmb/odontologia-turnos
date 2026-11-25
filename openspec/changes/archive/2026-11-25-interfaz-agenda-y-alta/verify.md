# Verify — C-10 interfaz-agenda-y-alta

Fecha: 25/11/2026. Rama `feature/c-10-interfaz-agenda-y-alta`.

## Pruebas automáticas

| Comprobación | Resultado |
| --- | --- |
| Línea base en `main` antes de implementar | 128 unitarias y 195 de integración sin fallos |
| `./scripts/check-docker.sh` | Correcto |
| `mvn -f backend/pom.xml clean verify` | 129 unitarias y 216 de integración, sin fallos, errores ni omisiones; PostgreSQL 18.7 con Testcontainers |
| `npm test` en `frontend/` | 65 pruebas, sin fallos. Antes de las correcciones de la revisión, las 53 de entonces se repitieron con `TZ=Asia/Tokyo` y `TZ=Europe/Madrid` |
| `python3 -m unittest discover -s scripts/tests` | 9 pruebas correctas |
| `openspec validate interfaz-agenda-y-alta --strict` | Válido antes de archivar |
| `openspec archive` y `openspec validate --specs` | Archivado como `2026-11-25-interfaz-agenda-y-alta`; 11 de 11 especificaciones válidas. Con `--strict` quedan dos avisos de longitud en `acceso-y-permisos-internos`; el de alcance por registro surge de la excepción agregada |

TDD: las pruebas nuevas se observaron fallar antes de implementar, contra esqueletos o rutas inexistentes (permiso desconocido; 7 de lecturas; 11 de HTTP; 20 de lógica, 23 de vistas y 3 del controlador de sección en el frontend). Algunas pruebas negativas ya pasaban contra el esqueleto porque este no hacía nada; quedan como regresión.

## Recorrido con la API real

API temporal `ApiC10Demo` sobre PostgreSQL desechable, interfaz en `http://localhost:5173` y datos ficticios.

| Recorrido | Resultado |
| --- | --- |
| Recepción: agenda sin intervalo | Aviso de configuración faltante y período vacío |
| Recepción: intervalo `7,5` y luego `15` | Error junto al campo; después «Vigente: 15 minutos» |
| Recepción: alta a las 09:00 | Turno pendiente visible en la agenda |
| Recepción: alta a las 09:15, mismo profesional y sillón | Rechazo de disponibilidad con los datos conservados |
| Recepción: bloqueo sobre el turno | Rechazo sin creación |
| Recepción: bloqueos de sillón y profesional, y solo de profesional | Creados y visibles |
| Recepción: vista semanal y recarga por dirección | Lunes a domingo; se conservan vista, fecha y sillón |
| Odontología: `#configuracion` | Redirige a la agenda; sin enlace ni acción de bloqueo |
| Odontología: turno propio de paciente sin asignación | Muestra nombre y apellido |
| Odontología: bloqueos | Solo describe la afectación del sillón o de su agenda |
| Odontología: alta | Profesional fijo; la búsqueda no devuelve el paciente sin asignación |
| Teclado | Ingreso, intervalo, apertura del alta y búsqueda de paciente; el foco pasa al título al cambiar de sección |

El recorrido por teclado fue parcial: los selectores y horarios del alta y del bloqueo se completaron con la automatización del navegador.

Después de las correcciones de la revisión se repitió una prueba de humo en Chromium contra la API real: intervalo, alta, bloqueo rechazado, bloqueo creado y cierre de sesión, sin excepciones de JavaScript.

## Revisión visual

Capturas de lo implementado en `mockups/verify/`: 14 imágenes a 390×844 y 1440×900, sin desbordamiento horizontal ni excepciones de JavaScript.

Corregido durante el recorrido: error del intervalo junto al campo, botón de búsqueda en línea, resumen del día bajo su título y título del documento al cerrar sesión. Las capturas son anteriores a un ajuste que quita el espacio de la zona de errores vacía en los formularios y a las correcciones de la revisión.

Diferencias respecto de los mockups aprobados:

- La vista semanal muestra «Profesional», código y prestación; el mockup mostraba solo el código.
- Los selectores del alta empiezan en «Seleccione…» para evitar reservas por defecto, y el bloqueo sin sillón en la dirección empieza en «Ninguno».
- La búsqueda de pacientes informa la cantidad de coincidencias.

## Revisión independiente

Un agente sin el contexto de la implementación revisó `main..HEAD` en modo de solo lectura. No encontró hallazgos bloqueantes, ni fallas de autorización, ni exposición de datos en los endpoints nuevos. Informó 8 sugerencias y 4 nits.

### Corregidos, con prueba previa

| Hallazgo | Corrección |
| --- | --- |
| Años fuera de rango provocaban `RangeError` | La fecha ingresada o de la dirección se admite entre 1970 y 9997; la agenda compone también bloqueos muy antiguos |
| Un alta podía terminar después del año 9999 y romper la consulta | El contrato HTTP admite inicios hasta 9998 y rechaza un fin posterior a 9999 |
| Reabrir el formulario permitía repetir un envío en curso; un éxito tardío navegaba tras cerrar sesión | Envío único por operación a nivel de sección; el resultado se refleja en la vista vigente y cerrar sesión la desmonta |
| Prestación inexistente respondía `400` | Responde `404`; sin duración sigue siendo `400` |
| Tipear la fecha podía perder dígitos | Con teclado se aplica al salir o con Enter; comprobado en Chromium con ambas salidas |
| El error de búsqueda persistía tras una búsqueda correcta | Se limpia |
| Sin pruebas del cliente de agenda ni de token inválido | Se agregaron |

### Sin corregir

| Hallazgo | Estado |
| --- | --- |
| No hay duración máxima de un turno: una duración enorme ocupa recursos durante años y C-10 no ofrece cancelar | Regla de dominio pendiente de decisión |
| A recepción, un bloqueo que no afecta al sillón consultado se muestra igual como «No disponible» | Cumple el spec y el mockup aprobado; ajuste visual pendiente de decisión |
| `404` de referencia inexistente comparte el código `solicitud_invalida` y se distingue por el estado | Se conserva el contrato de C-11 |
| Con datos inválidos, odontología recibe `400` antes que `403`, y sin token un cuerpo mal formado recibe `400` antes que `401` | No persiste ni expone datos |
| Cadenas vacías se interpretan como ausentes, `"15"` se acepta como número y los campos desconocidos se ignoran | La interfaz no los envía |
| `415`, `405` y `/recursos/profesionales` sin token no siguen el formato o estado de las rutas nuevas | Preexistente |
| El foco puede perderse durante envíos y cargas; los errores de alta y bloqueo no se asocian a cada campo | Mejora de accesibilidad pendiente |

## Correcciones sumadas antes de integrar

El 25/11/2026 el usuario pidió sumar a este PR las correcciones del `401` sin cabecera CORS, preexistente en C-11, que antes figuraba entre lo no corregido.

| Commit | Cambio |
| --- | --- |
| `29cd92c` | Un filtro CORS precede a los demás, de modo que el `401` por token inválido llega al origen permitido |
| `78e647c` | Una falla interna al resolver el token responde `500 error_interno` con un identificador de correlación, sin detalles internos |

Sobre el árbol combinado con `ba9b2a2`, `mvn -f backend/pom.xml clean verify` terminó con 129 pruebas unitarias y 219 de integración, sin fallos, errores ni omisiones. No cambian el código de la interfaz, las especificaciones ni el esquema. Esa ejecución no incluye una comprobación en navegador de la sesión vencida.

En otra sesión de trabajo, sobre una combinación temporal de `ba9b2a2` con `78e647c`, se informó una comprobación en navegador: con el token inválido de una sesión anterior, las consultas recibieron un `401` legible y la agenda mostró «Su sesión venció. Cierre sesión y vuelva a ingresar.». En el mismo recorrido, recepción guardó el intervalo y creó un turno.
