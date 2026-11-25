# Verify — C-19 email-al-crear-turno

Fecha: 25/11/2026. Base: `main` en `5eaba41`, rama `feature/c-19-email-al-crear-turno`.

## Estado

Implementado y verificado localmente con proveedor simulado. **La evidencia con envío real está pendiente de autorización expresa (tarea 4.3)**, por lo que el change no está completo ni archivado.

## Fallo observado antes de implementar

Con esqueletos sin comportamiento y PostgreSQL real, la ejecución focalizada mostró 9 fallos o errores unitarios y 23 de integración: texto y enlace vacíos, cola sin límite, motivo con la respuesta del proveedor, ningún aviso publicado, evento con el token en texto y ningún email enviado. Algunas comprobaciones negativas y el motivo de una falla de red ya pasaban: son guardas, no evidencia de rojo.

Tras la revisión, las pruebas nuevas de representación en texto y de motivo en el registro fallaron antes de corregir.

## Verificación final

| Comprobación | Resultado |
| --- | --- |
| `mvn -f backend/pom.xml clean verify` | 151 unitarias y 292 de integración, sin fallos, errores ni omisiones; PostgreSQL 18.7 con Testcontainers |
| Unitarias y las integraciones de envío, vigencia y aviso, 5 ejecuciones adicionales | 151 y 27 sin fallos en cada una |
| `npm test` en `frontend/` (sin cambios de interfaz) | 81 sin fallos |
| `python3 -m unittest discover -s scripts/tests` | 9 sin fallos |
| `openspec validate email-al-crear-turno --strict` | Válido |

`EnvioEmailTurnoIT` recorre agenda, adaptador real, registro en PostgreSQL y un servidor HTTP local en lugar de Resend. **No se envió ningún correo real.**

## Preparación del envío real

`EnvioTurnoRealManual` crea un turno ficticio en una base temporal y deja que la aplicación envíe su email; la selección automática no lo ejecuta. El ensayo con servidor local pasó. Las validaciones previas pasaron con valores ficticios, y rechazan una lista con más de una dirección. No se cargó la configuración privada ni se contactó a Resend. Procedimiento: [prueba real](../../../docs/evidencias/c19-envio-real/prueba-real-runbook.md).

Al prepararlo se observó que el aislamiento de las integraciones agregado en este change vaciaba también la configuración de las pruebas manuales, incluida la de C-17. Ahora se desactiva solo con `-Denvio.real=true`; con esa opción las validaciones pasan y sin ella siguen aisladas. La suite completa posterior dio 151 unitarias y 292 de integración sin fallos. Este ajuste y la herramienta no pasaron por revisión independiente.

## Revisión independiente

Un agente sin el contexto de la implementación revisó en solo lectura, sin ejecutar Maven. En el código de producción no halló bloqueantes: sin fuga de token o datos, cruce entre pacientes, ciclos de bloqueos ni vía de envío real desde pruebas o demostraciones. Halló un bloqueante en una prueba: `EjecutorEmailTest` era intermitente, reproducido con una réplica fuera de Maven. Se corrigió junto con la redacción del README y el diseño, el motivo en el registro, la representación en texto del mensaje y la solicitud, dos pruebas débiles y `.env.example`.

## Pendientes

- Envío real a una dirección de prueba autorizada y su evidencia, sin credenciales ni direcciones. El remitente de prueba de Resend solo entrega a la dirección de la cuenta.
- La página de cancelación corresponde a C-23; hasta entonces el enlace no cancela desde el navegador.
- Un aviso se pierde si el proceso cae, si la cola está llena o si el cierre supera la espera; un envío interrumpido puede quedar `pending`.
- La vigencia se comprueba antes del primer intento; un cambio durante los reintentos no detiene el mensaje.
- El descarte por cola llena no identifica el turno.
- Sin enlace, un cambio solo de recurso o de ida y vuelta antes del envío puede producir dos mensajes con la misma fecha.
- La URL base admite `http` hacia cualquier host, como indica la spec.
- El ejecutor de email es el único declarado: un `@Async` futuro sin calificador lo usaría.
- Las integraciones ejecutadas fuera de Maven no vacían las variables de email del entorno.
- Preexistentes de C-17: el adaptador no escapa todos los caracteres de control del JSON, no restaura una interrupción y puede dejar `pending` si falla la persistencia tras un envío aceptado.
- `CHANGES.md` en `main` todavía muestra a Fabi como responsable.
