# Tasks

## 1. Condiciones previas

- [x] 1.1 Registrar la aprobación expresa de Apply con su fecha en este change.
  - 25/11/2026: Giovanni aprobó implementar C-20 según la propuesta, incluidas sus interpretaciones. No autoriza envíos reales ni push.

## 2. Horario fijado

- [x] 2.1 Con prueba previa fallida y PostgreSQL real, agregar la migración y guardar el dato en el alta y en la reprogramación que cambia el inicio; verificar cambio sin mover el inicio, reversión, confirmación, cancelación y permisos de la cuenta de aplicación.
- [x] 2.2 Con prueba previa fallida y reloj controlado, exponer la consulta de turnos por recordar; verificar bordes de 24 horas, comienzo, estados, anticipación, turnos sin dato y relectura antes del envío.

## 3. Composición y envío

- [x] 3.1 Con prueba unitaria previa fallida, componer el recordatorio; verificar Mendoza, aclaración, ausencia de enlaces y de datos no aprobados.
- [x] 3.2 Con prueba previa fallida y transporte falso, procesar los turnos; verificar aislamiento, un solo intento por horario, fallos, cambio de email, reprogramación, descarte, agenda sin cambios y registro sin datos personales.
- [x] 3.3 Con prueba previa fallida y servidor local de prueba, verificar con el adaptador real que un fallo y una revisión repetida no generan otra llamada.

## 4. Programación y configuración

- [x] 4.1 Con prueba previa fallida, programar la revisión solo cuando está activada; verificar el valor por defecto, la ejecución al arrancar y que un error no detiene las siguientes.
- [x] 4.2 Fijar el recordatorio desactivado en las pruebas y documentar `EMAIL_RECORDATORIO_ACTIVO` en `.env.example` y `backend/README.md`.

## 5. Verificación

- [x] 5.1 Ejecutar `mvn -f backend/pom.xml clean verify`, `openspec validate recordatorio-automatico-24-horas --strict` y las pruebas del roadmap; registrar en `verify.md`.
- [x] 5.2 Obtener revisión independiente de duplicados, bordes temporales, concurrencia y datos en registros.
- [ ] 5.3 Con autorización expresa y direcciones de prueba del equipo, realizar un envío real y registrar la evidencia sin credenciales ni direcciones.
- [x] 5.4 Actualizar el flujo 9 de la KB y `CHANGES.md` con el estado verificado.

## Workflow follow-up

- Archivar con el CLI de OpenSpec y pedir autorización antes de publicar.
