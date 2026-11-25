# Design

## Context

Ver [proposal.md](proposal.md). Estado de `main` (a52847e):

- `CorreoSalida` envía una `SolicitudEnvio(tipo, turnoId, destinatario, asunto, cuerpo)`, hace sus reintentos y registra el resultado en `email_envio`, con clave única derivada de tipo, turno y destinatario. Un envío fallido o pendiente no se repite con la misma clave.
- `AvisoTurnoPorEmail` (C-19) envía el alta y la reprogramación en un ejecutor propio.
- `turno` no guarda cuándo se creó ni cuándo cambió su horario.
- No hay tareas programadas. Existe un `Clock` inyectable y el pool de conexiones es de 3.

## Goals / Non-Goals

**Goals:** cumplir la spec sin dependencias nuevas, con reloj controlable en pruebas y sin sostener transacciones durante el envío.

**Non-Goals:** cola persistente, reintentos posteriores, varias instancias coordinadas más allá de la clave única, cambios al transporte, al email de C-19 o a la interfaz.

## Decisions

### Revisión periódica

Una tarea de Spring (`@Scheduled` con espera fija de un minuto desde que termina la anterior, sin demora inicial) llama a un servicio de comunicaciones. La configuración que habilita la programación y registra la tarea existe solo con `email.recordatorio.activo=true` (`EMAIL_RECORDATORIO_ACTIVO`, `false` por defecto). El intervalo es una constante: nadie pidió configurarlo. Con un único hilo de programación no hay revisiones superpuestas.

El usuario descartó los temporizadores por turno y un proceso externo. Quartz se descarta porque agrega dependencia y tablas para una sola tarea.

El servicio es independiente de la tarea: las pruebas lo invocan directamente con un reloj controlado.

### Dato del horario fijado

Migración `V14__horario_fijado_del_turno.sql`: columna `turno.horario_fijado_en timestamptz NULL`, permiso de actualización de esa columna para la cuenta de aplicación e índice por `inicio`. La agenda la escribe en el alta y en la reprogramación que cambia el inicio, con el mismo instante posterior a los locks que usa para el enlace. `NULL` identifica turnos anteriores.

Alternativas descartadas: deducirlo de `token_cancelacion.creado_en` (no existe con menos de 48 horas y se reemplaza) o de `email_envio` (depende de que el email de alta haya llegado a intentarse).

Si la PR de C-21 se integra primero con la versión 14, esta migración se renumera antes de integrar. Flyway está configurado sin ejecución fuera de orden.

### Selección y envío

1. La agenda ofrece una lectura sin locks: turnos pendientes o confirmados con `ahora < inicio <= ahora + 24 h`, descartando los que tienen `horario_fijado_en > inicio - 24 h`. La ventana acota el resultado; no se pagina.
2. Comunicaciones descarta con una sola consulta los que ya tienen un registro en `email_envio` con el tipo del horario, sin importar el destinatario. Así un fallo no se reintenta ni se vuelve a avisar cada minuto, y un cambio de email no duplica.
3. Para cada turno restante vuelve a leerlo, comprueba estado, inicio y que no haya comenzado según el reloj, obtiene el paciente, compone el mensaje y llama a `CorreoSalida`.

El tipo es `turno-recordatorio-<inicio ISO-8601 completo>`, para distinguir inicios del mismo segundo. No usa la versión porque confirmar la incrementa sin cambiar el horario. Un turno que vuelve a un horario ya recordado queda excluido por la regla de anticipación.

Cada turno se procesa dentro de su propio manejo de errores y la revisión captura sus fallos. No hay transacción alrededor de la llamada al proveedor.

### Mensaje

Asunto «Recordatorio de tu turno». Cuerpo:

```
Hola, <nombre>:

Te recordamos tu turno del <dd/MM/yyyy> a las <HH:mm> (hora de Mendoza).

Ya venció el plazo para cancelarlo por enlace.
```

Reutiliza el formato horario de `MensajeTurno`; su representación textual oculta el cuerpo.

### Cierre de la aplicación

Un envío interrumpido quedaría fallido y no se repetiría. El cierre ordenado de Spring ya espera la tarea en curso hasta su límite por fase (30 segundos por defecto), así que no se agrega configuración. Avisado el cierre o interrumpido el hilo, la revisión no inicia otro; los restantes quedan sin registro para la próxima ejecución.

La revisión se registra solo en la aplicación web: el CLI de bootstrap no envía.

### Aislamiento de las pruebas

La configuración base de las pruebas de integración fija el recordatorio como desactivado, también cuando se desactiva el aislamiento de credenciales para un envío real.

## UI Mockups

Sin interfaz.

## Risks / Trade-offs

- [Al activar el envío salen de inmediato los recordatorios de todos los turnos de las próximas 24 horas] → apagado por defecto, lista de direcciones de prueba y aviso en el README.
- [Activado sin credenciales válidas, los turnos en ventana quedan fallidos y no se reintentan] → consecuencia de «solo registrar»; documentarlo.
- [Un corte entre el registro `pending` y el resultado deja el recordatorio sin reenvío] → mismo límite que C-17.
- [Un cambio durante los reintentos del envío puede dejar llegar el horario anterior] → la comprobación se hace justo antes; mismo límite que C-19.
- [Los envíos son secuenciales y un proveedor lento atrasa los siguientes] → volumen de un consultorio.
- [La aplicación apagada durante todo el período deja el turno sin recordatorio] → inherente a la ejecución local.
- [Un turno anterior fijado con poca anticipación puede recibir un recordatorio] → ocurre una sola vez, solo para turnos previos.

## Migration Plan

Migración aditiva. Para detener los envíos basta desactivar la configuración y reiniciar; la columna puede permanecer.
