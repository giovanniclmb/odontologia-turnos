# Design

## Context

Ver [proposal.md](proposal.md). `ServicioCancelacionPorEnlace.emitir` publica `EnlaceCancelacionEmitido(turnoId, token)` solo si la ventana está abierta. `CorreoSalida` envía texto plano, filtra por lista de prueba, deduplica por tipo, turno y destinatario, registra en `email_envio` y reintenta. No hay ejecución asíncrona configurada y el pool tiene 3 conexiones.

## Goals / Non-Goals

**Goals:**

- Conectar agenda y transporte sin acoplar la agenda al email.
- Que ningún fallo de email afecte un turno ni exponga el token.

**Non-Goals:**

- Cola persistente, reenvíos, vista de fallos, HTML, recordatorios y página de cancelación.

## Decisions

### Un evento por alta o reprogramación

`ServicioAgenda` publica `TurnoProgramado(turnoId, version, inicio, motivo, token)` dentro de la transacción; `token` es nulo con la ventana cerrada y su texto lo oculta. Reemplaza a `EnlaceCancelacionEmitido`, y `emitir` deja de publicar.

| Alternativa | Motivo para descartarla |
| --- | --- |
| Mantener el evento y agregar otro sin token | Dos avisos para una misma operación que el consumidor debería correlacionar |
| Que el controlador invoque el envío | Se perderían las altas hechas por el servicio y la garantía posterior al commit |

### Consumo asíncrono y acotado

El oyente usa `@TransactionalEventListener` y `@Async` sobre un ejecutor propio de un hilo y cola de 100. Un hilo limita el uso de conexiones y mantiene el orden de los avisos. Con la cola llena el aviso se descarta y se registra, en lugar de bloquear la solicitud.

### Vigencia antes de enviar

El módulo de agenda expone una consulta de solo lectura, sin bloqueos: turno no cancelado, mismo inicio y, si el aviso trae token, que su hash sea el registrado y no esté consumido. No compara la versión, porque confirmar la incrementa sin cambiar el mensaje.

### Deduplicación

Tipo `turno-alta` y `turno-reprogramacion-v<versión>`. Usa la clave vigente de C-17 sin migraciones.

### Configuración y composición

`email.cancelacion-url-base` desde `EMAIL_CANCELACION_URL_BASE`, `http` o `https` y sin fragmento propio. Se valida al usarla y no al arrancar, para no exigir configuración de email en entornos que no envían. La composición es una función sin dependencias, probada con unidades.

### Motivo de fallo

El adaptador registra `HTTP <estado>` o el tipo de excepción, sin cuerpo ni mensaje del proveedor.

## Risks / Trade-offs

- [Un proceso que cae entre el commit y el envío pierde el email] → aceptado con «solo registrar»; no queda registro.
- [Un envío fallido ocupa su clave y no se puede repetir] → aceptado; una reprogramación genera uno nuevo.
- [El turno puede cambiar entre la comprobación y la llamada al proveedor] → ventana mínima aceptada; un enlace viejo se rechaza.
- [Sin página de cancelación el enlace no funciona en el navegador] → C-23 debe leer el token del fragmento y usar `POST`.
- [El remitente de prueba de Resend solo entrega a la dirección de la cuenta] → condiciona la evidencia real; confirmar antes de enviar.
- [Un entorno con configuración real envía al crear turnos de pacientes cuya dirección esté en la lista] → las pruebas usan transporte falso y direcciones ficticias.

## Migration Plan

Sin cambios de esquema. Revertir el cambio restituye el evento anterior y deja de enviar.
