# Verify

Fecha: 25/11/2026. Java 25.0.4, PostgreSQL 18.7 (Testcontainers). Sin envíos reales: las pruebas usan un transporte falso o un servidor local.

## Resultado

| Comprobación | Resultado |
| --- | --- |
| `mvn -f backend/pom.xml clean verify` | 155 pruebas unitarias y 326 de integración, sin fallos |
| `openspec validate recordatorio-automatico-24-horas --strict` | Válido |
| `openspec validate --specs --strict` | 13 de 13 |
| `python3 -m unittest discover -s scripts/tests` | 9 pruebas, sin fallos |

No se ejecutaron las pruebas del frontend: el change no lo modifica.

## TDD

Se observó el fallo antes de implementar: columna y migración ausentes (6 errores); consulta vacía (9 de 10); mensaje vacío (1); envío sin implementar (10 de 12); programación sin registrar (2 de 3 y 1 unitaria).

## Revisión independiente

Sin defectos bloqueantes en producción. Hallazgos y resolución:

| Hallazgo | Resolución |
| --- | --- |
| Cuatro pruebas esperaban 13 migraciones (bloqueaba `clean verify`) | Actualizadas a 14 |
| El valor por defecto apagado no quedaba probado | `ActivacionRecordatorioTest` lee la configuración sin resolver |
| El aviso de cierre no quedaba probado | `CierreRecordatorioIT` cierra la aplicación con un envío en curso |
| El truncado a microsegundos no tenía prueba | Caso agregado en `HorarioFijadoIT` |
| El apagado en pruebas dependía del perfil de envío real | Movido a la configuración base de failsafe; comprobado en el POM efectivo con y sin `-Denvio.real=true` |
| La prueba de activación no demostraba que la revisión funcionara | `CierreRecordatorioIT` exige un envío al arrancar |
| Dos inicios del mismo segundo compartían clave | El tipo usa el inicio completo; prueba con fallo observado |
| El tiempo de espera podía no cubrir el cuerpo de la respuesta | Confirmado; defecto previo de C-17, fuera de este change |

Se alteró temporalmente la implementación para comprobar que las pruebas nuevas detectan el valor por defecto encendido, la falta del aviso de cierre y la falta de truncado. La misma comprobación mostró que la espera de apagado del programador que se había agregado era redundante, porque Spring ya espera la tarea en curso, y se quitó. Las correcciones no tuvieron una segunda revisión independiente.

## Límites conocidos

- Cada horario tiene un solo intento: un registro `failed` o `pending` impide repetirlo, también si faltaban credenciales o si el proceso se cortó.
- Activar el envío manda en la primera revisión los recordatorios de todos los turnos de las próximas 24 horas.
- Un cambio del turno durante los reintentos no detiene el mensaje en curso.
- Sin el arreglo del transporte de C-17, una respuesta que no termina detiene la revisión hasta reiniciar.
- La aplicación apagada durante todo el período deja el turno sin recordatorio.
- Un turno anterior a la migración fijado con poca anticipación puede recibir un recordatorio.
- La migración usa la versión 14; el change abierto de C-21 deberá numerar la suya a continuación.

## Pendiente

- Envío real a una dirección de prueba del equipo, con autorización expresa (tarea 5.3). La [prueba manual](../../../docs/evidencias/c20-envio-real/prueba-real-runbook.md) está preparada: el ensayo con servidor local y la validación previa de la configuración pasaron, sin envíos.
- Archivo en OpenSpec, publicación e integración.
