# Tasks

## 1. Persistencia del token de cancelación

- [x] 1.1 Crear la migración y la representación persistida del token de cancelación, guardando solo su hash SHA-256, el identificador del turno, la versión vigente al emitir y un indicador de consumo; verificar que la migración aplica en PostgreSQL de prueba y que no se almacena el token en claro.
- [x] 1.2 Implementar el acceso transaccional a tokens por hash y por turno, con bloqueo de fila para concurrencia; verificar con pruebas de repositorio que una segunda inserción o consumo concurrente no duplica el registro.

## 2. Emisión del token

- [x] 2.1 Implementar la generación del token opaco aleatorio y su hash, y la emisión solo cuando el inicio del turno sea posterior al instante de emisión más 48 horas; verificar con pruebas unitarias el límite inclusivo y la no emisión cuando la ventana está cerrada.
- [x] 2.2 Invalidar los tokens existentes del turno al reprogramar y emitir uno nuevo solo si el turno reprogramado cumple la ventana de 48 horas; verificar con pruebas que un token anterior deja de ser válido tras reprogramar.
- [x] 2.3 Dejar el token emitido disponible para C-19 sin enviar email desde C-18; verificar que ningún componente de C-18 invoque transporte de email.

## 3. Validación, consumo y cancelación por token

- [x] 3.1 Implementar la validación del token por hash, incluyendo existencia, no consumo, no expiración, estado `PENDIENTE` o `CONFIRMADO`, inicio no alcanzado y corte inclusivo de 48 horas; verificar con pruebas unitarias cada condición de rechazo.
- [x] 3.2 Integrar la cancelación por token con la cancelación transaccional existente de `ServicioAgenda`, consumiendo el token en la misma transacción y liberando el horario; verificar con pruebas de integración PostgreSQL que cancelar por token deja el turno `CANCELADO` y libera recursos.
- [x] 3.3 Garantizar el máximo de una cancelación exitosa por token ante solicitudes repetidas o concurrentes, sin revelar datos del turno; verificar con pruebas de concurrencia que solo una operación tiene éxito y las demás son rechazadas genéricamente.

## 4. Contrato HTTP

- [x] 4.1 Implementar la validación no mutante por `GET` y la cancelación por `POST` con acción explícita del paciente; verificar que `GET` no cambia estado ni consumo y que `POST` ejecuta la cancelación atómica.
- [x] 4.2 Definir respuestas genéricas para token ausente, inválido, expirado, consumido o no elegible, sin filtrar existencia, estado ni datos clínicos; verificar con pruebas HTTP los códigos y cuerpos acordados.

## 5. Verificación de integración

- [x] 5.1 Ejecutar la suite backend y las pruebas de migración, concurrencia y cancelación por token; verificar que no hay regresiones en `agenda-persistida` ni en la cancelación interna.
- [x] 5.2 Revisar que la autorización proviene solo del token y que no se acepta identidad interna, cookie de sesión ni identificador de turno como credencial; verificar con pruebas negativas de bypass.

## Workflow follow-up

- Archivar el change mediante OpenSpec después de que la revisión y las pruebas requeridas estén satisfechas.
- Verificar el resultado archivado y actualizar el estado de C-18 en `CHANGES.md` solo cuando corresponda.
