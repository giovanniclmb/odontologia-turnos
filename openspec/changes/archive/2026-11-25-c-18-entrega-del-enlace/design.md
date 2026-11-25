# Design

## Context

Ver [proposal.md](proposal.md). `ServicioCancelacionPorEnlace.emitir` devuelve el token en claro y `ServicioAgenda` lo descarta. C-19 todavía no existe.

## Goals / Non-Goals

**Goals:**

- Dejar un punto de consumo para C-19 sin acoplar la agenda al email.

**Non-Goals:**

- Enviar email, plantillas, destinatarios, reintentos o persistir la entrega.

## Decisions

### Evento de aplicación `EnlaceCancelacionEmitido`

`emitir` publica el evento dentro de la transacción. El consumidor usa `@TransactionalEventListener` en la fase posterior al commit, que es su valor por defecto.

| Alternativa | Motivo para descartarla |
| --- | --- |
| C-19 emite al enviar | Descartada por el usuario: cambia la emisión aprobada |
| Dejar solo registrado el pendiente | Descartada por el usuario: no permite entregar el enlace |
| Devolver el token en el resultado del turno | Podría terminar en una respuesta HTTP |
| Puerto propio con implementación vacía | Código sin uso hasta C-19 y sincronización manual |

El `toString` del evento oculta el token.

## Risks / Trade-offs

- [Un consumidor con `@EventListener` recibiría el token antes del commit] → el contrato queda documentado en el record; C-19 debe usar el oyente transaccional.
- [Si el proceso cae entre el commit y el consumo, el enlace se pierde] → entrega sin persistencia, coherente con el transporte «best-effort» de C-17.
- [El oyente corre en el hilo y antes de liberar la conexión de la operación] → un envío síncrono demoraría la respuesta.
- [Spring registra completa la excepción de un oyente; si incluye el cuerpo del email o la URL, filtra el token] → C-19 debe capturar sus errores y registrar solo el turno.
- [Usar una transacción nueva en el mismo hilo pide una segunda conexión mientras se retiene la primera, con un pool de 3] → C-19 debe consumir de forma asíncrona, con transacción propia en ese hilo; inferencia no reproducida.
- [Un token entregado puede quedar reemplazado antes del envío] → C-19 debe comprobar su validez antes de enviar; el token viejo ya se rechaza.
- [Sin consumidor, el evento se descarta] → comportamiento esperado hasta C-19.

## Migration Plan

Sin cambios de esquema. Revertir el commit restituye el comportamiento anterior.
