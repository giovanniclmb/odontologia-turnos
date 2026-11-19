# Tasks

Responsable: Giovanni. Plan y Apply aprobados por el usuario el 19/11/2026 para C-01, CRITICO. El avance observado se registra en las casillas. Fuentes: [spec](specs/disponibilidad-de-turnos/spec.md) y [diseño](design.md). Cada tarea se limita a una sesión orientativa inferior a dos horas; ajustar si la evidencia muestra otra necesidad.

## 1. Entorno mínimo del módulo

- [x] 1.1 Comprobar el aislamiento de trabajo con la skill aplicable, conservar los cambios documentales previos y confirmar que no hay dependencias pendientes de C-01; verificar con `git status` y la ficha del roadmap antes de editar código.
- [x] 1.2 Crear `backend/pom.xml` para Java 25 y JUnit Jupiter 6.1.3, con versiones explícitas de plugins compatibles y sin Spring ni persistencia; verificar `mvn -f backend/pom.xml validate` y registrar las versiones efectivas, sin presentar esta validación como ejecución de tests.
- [x] 1.3 Crear `backend/README.md` con requisitos JDK/Maven, comandos previstos y límites del módulo; verificar que rutas y comandos corresponden al POM y documentar la descarga inicial de dependencias sin modificar dotfiles.

## 2. Referencias, duración y resultado

- [x] 2.1 Escribir pruebas de referencias obligatorias, caso feliz sin reservas, duración por prestación y ajuste explícito; observar RED por comportamiento no implementado, implementar los objetos mínimos y comprobar GREEN con `mvn -f backend/pom.xml test`.
- [x] 2.2 Añadir con TDD el rechazo de entradas inválidas y la distinción entre fallo y éxito, incluida duración efectiva no positiva; verificar pruebas de rechazo y ausencia de turno exitoso, registrando RED/GREEN por regla.
- [x] 2.3 Documentar en `backend/README.md` la frontera de datos ficticios, ajuste opcional, intervalo explícito y reservas ocupantes válidas; verificar ejemplos contra las pruebas de este grupo y añadir su trazabilidad escenario/prueba a la evidencia del change.

## 3. Conflictos e intervalo

- [x] 3.1 Añadir primero pruebas de conflicto independiente de profesional y sillón, períodos iguales y contención; implementar la comprobación mínima y verificar RED/GREEN, incluyendo un caso simultáneo permitido con recursos distintos.
- [x] 3.2 Añadir pruebas parametrizadas del borde exacto y separación insuficiente antes y después para ambos recursos, con intervalo explícito de 10 minutos solo como fixture; implementar la comparación simétrica y verificar los ocho casos sin duplicar el intervalo.
- [x] 3.3 Cubrir mediante TDD el ajuste de duración que produce conflicto y el conflicto dentro de una colección no ordenada; verificar que todas las reservas relevantes se consideran y las ajenas no interfieren.
- [x] 3.4 Cubrir con pruebas la ausencia de mutación de las reservas tanto al aceptar como al rechazar; verificar GREEN, actualizar el mapa completo escenario/prueba y documentar en README que validar no persiste ni garantiza concurrencia.

## 4. Verificación integrada y entrega del change

- [x] 4.1 Ejecutar `mvn -f backend/pom.xml clean verify` y revisar los reportes Surefire; comprobar tests descubiertos, sin fallos ni omisiones, y que cada escenario del spec está cubierto. Registrar comando, entorno, resultado y limitaciones en evidencia Verify.
- [x] 4.2 Revisar la fórmula de intervalo, casos límite, inmutabilidad y límites del contrato; corregir hallazgos mediante TDD y volver a ejecutar las comprobaciones afectadas. Si participa un revisor independiente, registrar su alcance real; no afirmar revisión independiente si la realiza la misma sesión.
- [x] 4.3 Ejecutar `openspec validate crear-turno-sin-solapamientos-dominio --strict`, revisar el diff y reconciliar el estado local del roadmap y la documentación vigente con la selección de C-01; verificar que ningún estado afirme persistencia, UI, pruebas aún no ejecutadas o cierre de PA fuera de alcance.

## Workflow follow-up

- Presentar Verify y sus limitaciones al usuario antes del cierre. No dar por cumplidas la revisión personal de fuentes ni las reflexiones del equipo.
- Archivar mediante la skill y el CLI correspondientes cuando el usuario pida finalizar el change; sincronizar los specs según ese flujo y comprobar el resultado archivado.
- Conservar commits atómicos por tarea cuando corresponda. No hacer push sin pedido explícito.
