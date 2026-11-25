# Tasks

## 1. Condiciones previas

- [x] 1.1 Registrar la aprobación expresa de Apply con su fecha en este change.
  - 25/11/2026: Giovanni aprobó por menú implementar C-19 según la propuesta. No autoriza envíos reales ni push.

## 2. Aviso desde la agenda

- [x] 2.1 Con prueba previa fallida y PostgreSQL real, publicar `TurnoProgramado` en alta y reprogramación en lugar del evento anterior; verificar commit, reversión, destino idéntico, confirmación, ventana cerrada y texto sin token.
- [x] 2.2 Con prueba previa fallida, exponer la consulta de vigencia; verificar cancelado, reprogramado, confirmado y enlace reemplazado.

## 3. Composición y envío

- [x] 3.1 Con pruebas unitarias previas fallidas, componer asunto y cuerpo; verificar Mendoza, fecha límite, enlace, ausencia de enlace, URL inválida y ausencia de datos no aprobados.
- [x] 3.2 Con prueba previa fallida y transporte falso, consumir el aviso de forma asíncrona; verificar destinatario, aislamiento, descarte, tipos de deduplicación, fallos y registro sin dirección ni token.
- [x] 3.3 Con prueba previa fallida, dejar el motivo de fallo sin respuesta del proveedor; verificar con las pruebas del adaptador.
- [x] 3.4 Documentar `EMAIL_CANCELACION_URL_BASE` y el comportamiento en `backend/README.md`; verificar siguiendo la configuración desde cero sin secretos.

## 4. Verificación

- [x] 4.1 Ejecutar `mvn -f backend/pom.xml clean verify`, `openspec validate email-al-crear-turno --strict` y las pruebas del roadmap; registrar en `verify.md`.
- [x] 4.2 Obtener revisión independiente de fugas del token, aislamiento de destinatarios y concurrencia.
- [x] 4.3 Con autorización expresa y direcciones de prueba del equipo, realizar un envío real y registrar la evidencia sin credenciales ni direcciones.

## Workflow follow-up

- Archivar con el CLI de OpenSpec, actualizar `CHANGES.md` y pedir autorización antes de publicar.
