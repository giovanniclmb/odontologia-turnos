# QA de navegador de C-13

Fecha: 25/11/2026. La evidencia se completó en varios intentos; el seguimiento de la tarea queda a cargo de la coordinación.

## Entorno comprobado

Se compiló el frontend mediante la suite automatizada (92/92 pruebas). Se inició una copia aislada del fixture `ApiC10Demo` con Spring Boot y PostgreSQL 18.7 desechable de Testcontainers, y un servidor estático en loopback en el puerto 5173. El fixture alcanzó `C10_DEMO_URL=http://127.0.0.1:8080`. El acceso al socket de Docker requirió ejecución fuera del sandbox. No se usó la base de desarrollo.

## Bloqueo y alcance de la evidencia

El inventario de Computer Use no ofreció navegadores conectados. Los intentos de abrir `iab` y `chrome` respondieron `Browser is not available`; seleccionar la ventana existente de Chrome respondió `Computer Use was not approved to use Google Chrome`. Se respetó la restricción y no se intentó eludirla. No hubo interacción con la aplicación, ni capturas, ni comprobación visual de 390 × 844 o 1440 × 900, foco, teclado, errores o comparación con el mockup. Las pruebas automatizadas no sustituyen esas verificaciones.

En un segundo intento, tras habilitarse Chrome, se comprobó que la ventana respondía y se prepararon dos turnos ficticios en un fixture nuevo. Al volver a seleccionar la ventana, la revisión automática rechazó la acción por riesgo inaceptable: la lectura de la ventana incluiría contenido privado ajeno a C-13, incluido un PDF académico y otras pestañas. El rechazo indicó expresamente no eludirlo. No se abrió ni modificó ninguna pestaña ni se recorrió la interfaz. Para continuar se necesita una sesión de navegador dedicada a QA, sin contenido privado ajeno y con acceso autorizado.

## Tercer intento: ventana dedicada

Con una ventana dedicada autorizada se levantó un fixture nuevo de PostgreSQL 18.7 y se crearon dos turnos ficticios en sillones y profesionales distintos. El servidor estático requirió ejecución fuera del sandbox para que Chrome accediera al loopback. El recorrido observado en la aplicación y API reales fue:

| Comprobación | Resultado observado |
| --- | --- |
| Listado | Recepción vio ambos turnos, nombres ficticios, recursos, prestación y horario 09:00–09:30 de Mendoza. |
| Viewports | DevTools emuló 390 × 844 y 1440 × 900. Se observaron la disposición apilada y la disposición de escritorio respectivamente; la vista se ajustó a la ventana de herramientas. |
| Foco y teclado | Al abrir la vista, el título recibió el foco. Tres pulsaciones de Tab llegaron a Registrar confirmación y Enter ejecutó el registro. El aviso de éxito recibió foco y el turno desapareció del listado. |
| Conflicto | Se confirmó el segundo turno mediante una llamada concurrente a la API de la misma base temporal. La interfaz rechazó la versión anterior, mostró el mensaje y deshabilitó la acción. Actualizar lista produjo el estado vacío. |

En una ventana intermedia de 894 píxeles con zoom del navegador al 110 %, la primera observación mostró el botón recortado por desborde horizontal. La grilla exigía columnas mínimas de 8 y 12 rem más la acción, pero solo pasaba a una columna por ancho de viewport y no por el ancho disponible junto a la barra lateral. Se cambió a una consulta del ancho del contenedor; al recargar, el botón quedó visible. La comprobación RED/GREEN fue visual, sin prueba automatizada de geometría. La suite frontend posterior pasó 92/92. No se guardaron capturas de esta sesión.

Después de ingresar con el usuario ficticio de odontología, la revisión automática rechazó la siguiente captura por riesgo inaceptable: mostraría el aviso de Google Password Manager con datos privados de la cuenta, ajenos a la QA. La revisión indicó no eludir el rechazo; se detuvo el recorrido. Por eso siguen pendientes la comprobación visual de la restricción para odontología y los estados de error de red y resultado incierto. La tarea 3.2 permanece abierta.

## Cuarto intento: ventana de incógnito

El usuario abrió una ventana de incógnito dedicada. Con un fixture PostgreSQL 18.7 nuevo se completaron los casos que habían quedado pendientes:

- Como `odonto-demo`, acceder directamente a `#agenda/confirmaciones` mostró la agenda, sin el botón de confirmaciones ni listado de seguimiento.
- Como `recepcion-demo`, se cargó un turno ficticio y se activó Offline en DevTools. Al intentar registrar, la solicitud falló con `ERR_INTERNET_DISCONNECTED`; la interfaz indicó que no podía saber si se había registrado y deshabilitó el botón. Este caso simula una falla de conexión antes de recibir una respuesta; no demuestra por sí solo una respuesta perdida después de guardar.
- Con Offline aún activo, Actualizar lista mostró «No se pudo cargar la lista» y ofreció Reintentar. Se restableció No throttling y Reintentar recuperó el turno desde la API con el botón habilitado. La vista no reenvió automáticamente el registro.

Las observaciones visuales de los intentos tercero y cuarto se contrastaron con el mockup aprobado. No se guardaron capturas. Las pruebas automatizadas complementan el caso de respuesta incierta y recarga fallida.

## Reproducción cuando el navegador esté disponible

1. Con Docker y Java 25 o superior, arrancar el fixture `ApiC10Demo` siguiendo las instrucciones de `backend/README.md`. Este crea una base PostgreSQL temporal y usuarios exclusivamente ficticios. No usar las credenciales de prueba en una base persistente.
2. Compilar el frontend (`npm --prefix frontend run build`) y servir la raíz del repositorio en `http://localhost:5173`, como indica `frontend/README.md`. Abrir `/frontend/public/index.html` en un navegador autorizado.
3. Ingresar como recepción con los datos del fixture, configurar el intervalo y crear al menos dos turnos futuros ficticios. Abrir Confirmaciones y comparar listado y vacío con el mockup aprobado. Registrar un turno y comprobar que desaparece tras recargar.
4. Comprobar los estados de error y conflicto con condiciones controladas, el foco y la navegación por teclado, la exclusión de odontología y los dos viewports especificados. Registrar capturas con solo datos ficticios y resultados observados.

## Limpieza

Se detuvieron el servidor estático y el proceso Java del fixture. No quedaron procesos escuchando en los puertos 5173 y 8080. Una consulta a Docker no mostró el contenedor PostgreSQL creado por este intento; el fixture empleó Ryuk para su limpieza. La copia de trabajo aislada de QA quedó fuera del repositorio.

En el segundo intento se detuvieron nuevamente el servidor estático y el fixture tras el rechazo; los dos turnos existían solo en el PostgreSQL temporal.

En el tercer intento se detuvieron el servidor estático y el proceso Java del fixture después del rechazo. Los turnos eran exclusivamente ficticios y estaban en la base temporal.

En el cuarto intento se restableció No throttling en DevTools y se cerraron los servicios temporales al terminar.
