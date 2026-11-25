# Tasks

Antes de Apply, registrar en [design.md](design.md) la aprobación de los mockups de C-12 conforme a la política visual vigente.

## 1. Adaptadores HTTP

- [x] 1.1 Reutilizar la identidad interna y el contrato de salida de agenda para exponer reprogramación. Verificar en el adaptador solicitud válida y sin cambios, destino incompatible, intervalo sin configurar, versión obsoleta, entrada inválida, sesión ausente y denegación genérica para odontología fuera de alcance.
- [x] 1.2 Exponer cancelación con versión opaca. Verificar en el adaptador el estado y la versión resultantes, cancelación repetida, turno iniciado, versión obsoleta, solicitud inválida, sesión ausente y denegación genérica para odontología fuera de alcance. Documentar ambas rutas y sus errores con el contrato de agenda.

## 2. Acciones en la agenda

- [x] 2.1 Ampliar el cliente de agenda y validar respuestas de edición. Probar URL y cuerpo de ambos comandos, versión opaca preservada, respuesta válida y respuesta malformada.
- [x] 2.2 Integrar accesos a Reprogramar y Cancelar turno en día y semana. Probar los estados e inicios aplicables por rol, ausencia de acciones para cancelados e iniciados y acceso por teclado.
- [x] 2.3 Integrar el formulario de reprogramación con datos conservados y controles según rol. Probar destino válido, selección desde otra zona horaria, horario original sin pérdida de precisión, profesional fijo en odontología, disponibilidad insuficiente y resultado sin cambios.
- [x] 2.4 Integrar la confirmación de cancelación y el resultado visible en agenda. Probar desistimiento sin solicitud y cancelación confirmada con turno histórico visible.
- [x] 2.5 Centralizar la recarga y los resultados de edición en la agenda existente. Probar edición duplicada bloqueada, versión obsoleta en ambos comandos, éxito con recarga fallida, navegación durante el envío y destino fuera de la vista. Documentar el uso de los controles en la guía de frontend.

## 3. Verificación integrada

- [x] 3.1 Ejecutar el flujo completo contra PostgreSQL real con datos ficticios. Verificar que un conflicto deja el turno y su ocupación intactos, que una reprogramación efectiva conserva prestación y duración y que una cancelación permite crear otra reserva válida sobre el horario liberado. Incluir recepción con confirmado y odontología dentro y fuera de alcance.
- [x] 3.2 Ejecutar las suites backend y frontend y la validación estricta de OpenSpec. Revisar los recorridos a 390×844 y 1440×900, foco, navegación por teclado y mensajes de estado. Registrar comandos, resultados y diferencias con los mockups aprobados.
