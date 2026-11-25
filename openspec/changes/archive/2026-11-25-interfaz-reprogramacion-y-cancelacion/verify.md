# Verify — C-12 interfaz-reprogramacion-y-cancelacion

Fecha de ejecución 25/11/2026.

## Pruebas automáticas

| Comprobación | Resultado |
| --- | --- |
| Línea base backend | 129 unitarias y 219 de integración, sin fallos |
| `mvn -B -f backend/pom.xml verify` | 129 unitarias y 229 de integración. 358 en total, sin fallos, errores ni omisiones |
| `ApiAgendaIT, AgendaPersistidaIT, ReprogramacionPersistidaIT, CancelacionPersistidaIT` | 99 de 99 pruebas focalizadas con PostgreSQL 18.7, incluidas 24 de HTTP de agenda |
| `npm --prefix frontend test` con `TZ=Asia/Tokyo` | 81 de 81 pruebas, incluidas 16 de C-12. Línea base de 65 |
| `openspec validate interfaz-reprogramacion-y-cancelacion --strict` | Válido |
| `git diff --check` | Sin problemas de espacios en blanco |

Backend ejecutado con Maven 3.9.9, JDK 26.0.2.1 con `--release 25` y PostgreSQL 18.7 mediante Testcontainers. Frontend ejecutado con Node 24.

## TDD Cycle Evidence

| Tarea | Archivo de prueba | Capa | Safety Net | RED | GREEN | TRIANGULACIÓN | REFACTOR |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1.1 | [ApiAgendaIT.java](../../../backend/src/test/java/ar/edu/utn/odontologia/persistencia/ApiAgendaIT.java) | HTTP y PostgreSQL | API 14/14 y agenda persistida 17/17 | 404 al reprogramar, 500 ante versión vieja y 200 ante fin fuera del contrato durante la ampliación | El primer movimiento pasó y la suite HTTP quedó en 24/24 | Seis casos. Movimiento de confirmado, destino idéntico, conflicto e intervalo, versión y solicitud, año límite y alcance de odontología | Reutilizó la proyección de salida vigente |
| 1.2 | [ApiAgendaIT.java](../../../backend/src/test/java/ar/edu/utn/odontologia/persistencia/ApiAgendaIT.java) | HTTP y PostgreSQL | API 14/14 y agenda persistida 17/17 | 404 al cancelar | 21/21 al incorporar el primer caso y 24/24 tras ampliar la cobertura | Cuatro casos. Confirmado y reutilización, versión y repetición, turno iniciado y solicitudes inválidas, alcance de odontología | Comparte proyección y validación de versión con reprogramación |
| 2.1 | [api-agenda.test.mjs](../../../frontend/tests/api-agenda.test.mjs) | Cliente HTTP | 65/65 frontend | Métodos ausentes, después rechazo de la respuesta real sin paciente | Contrato de comandos y validación correctos | Tres casos. Rutas y cuerpos, versión opaca y respuesta malformada | Reutilizó un único validador de respuesta para altas y ediciones |
| 2.2 | [edicion-agenda.test.mjs](../../../frontend/tests/edicion-agenda.test.mjs) | Vista | 65/65 frontend | Cero acciones donde correspondían dos | Acciones habilitadas en la agenda | Dos casos. Día y semana, rol de odontología y turnos iniciados | Usa las mismas acciones para día y semana |
| 2.3 | [edicion-agenda.test.mjs](../../../frontend/tests/edicion-agenda.test.mjs) | Formulario | 65/65 frontend | Acción y formulario ausentes | Campos precargados, envío válido y conflicto mostrado | Tres casos. Destino y versión desde UTC+9, instante original exacto y operación idéntica, conflicto con valores conservados | Reutilizó controles y conversión de fecha de C-10 |
| 2.4 | [edicion-agenda.test.mjs](../../../frontend/tests/edicion-agenda.test.mjs) | Diálogo | 65/65 frontend | Diálogo ausente y desistimiento habilitado durante envío | Desistimiento, envío y estado cancelado correctos | Dos casos. Desistimiento y cancelación, controles durante el envío | Reutilizó el envío compartido por turno |
| 2.5 | [edicion-agenda.test.mjs](../../../frontend/tests/edicion-agenda.test.mjs) | Sección | 65/65 frontend | Aviso y destino ausentes, resultado perdido tras navegación o consulta fallida, red con error genérico | Recarga y resultado conservados | Cinco casos. Destino y foco, versiones obsoletas, consulta fallida, navegación y resultado incierto. Un caso adicional para el prefijo repetido del sillón | Comparte el formateador de recursos entre resumen y aviso |
| 3.1 | [ApiAgendaIT.java](../../../backend/src/test/java/ar/edu/utn/odontologia/persistencia/ApiAgendaIT.java) | Integración | Línea base backend verde | Usa los RED HTTP de 1.1 y 1.2 | Flujo integrado verde con PostgreSQL 18.7 | Conflicto, reprogramación de confirmado, cancelación y coexistencia con una nueva reserva | Tarea de verificación |

## Recorrido con la API real

Edge en loopback y PostgreSQL 18.7 desechable, con la aplicación compilada y datos ficticios sembrados mediante `ApiC11Fixture` y los servicios de agenda. El navegador usó la zona `Asia/Tokyo` para comprobar la hora de Mendoza.

| Recorrido | Resultado |
| --- | --- |
| Recepción, turno confirmado a las 09:00 | La agenda diaria ofrece ambas acciones. Se abrió el formulario con Enter y el foco pasó al encabezado. Se conservaron la prestación y los 45 minutos efectivos. |
| Destino ocupado a las 10:00 | La API rechazó el cambio. Se mostró el aviso, permaneció el destino ingresado y, al volver, siguió el turno original a las 09:00 con el foco en Reprogramar. |
| Destino disponible a las 11:00 | La agenda mostró el nuevo horario hasta las 11:45 y estado pendiente. |
| Confirmación y desistimiento | El foco inicial fue a Mantener turno. Escape cerró la confirmación, mantuvo el turno pendiente y devolvió el foco a Cancelar turno. Tab y Enter permitieron confirmar en la segunda apertura. |
| Cancelación y nueva reserva | El registro permaneció visible como cancelado, sin acciones. La agenda informó la liberación y el alta posterior aceptó otra reserva para el mismo horario, sillón y profesional. |
| Odontología, vista semanal | El profesional quedó fijo. Al cambiar de sillón se conservaron los filtros y Ver destino abrió el turno en la agenda del sillón nuevo. |

Se pulsaron Enter, Tab y Escape mediante eventos de teclado del navegador. Los campos de hora, búsqueda y selección se completaron mediante automatización del DOM.

## Revisión visual

Ocho capturas de la aplicación en [mockups/verify](mockups/verify/), a 390×844 y 1440×900. El ancho del documento coincidió con el viewport y los diálogos quedaron dentro de sus límites. El recorrido no registró excepciones JavaScript. Se corrigió la repetición del prefijo del sillón cuando el código ya incluía la palabra Sillón.

La implementación muestra la cuenta de prueba y la acción de cerrar sesión en la barra real de C-10. El resumen usa fecha ISO, hora de Mendoza y los códigos devueltos por la API. La confirmación usa un diálogo nativo que toma foco en Mantener turno. El formulario conserva la disposición y los controles del mockup aprobado.
