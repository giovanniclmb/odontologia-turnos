# Design

## Context

Ver [proposal.md](proposal.md) para motivación y alcance. C-01 no tiene dependencias; el repositorio carece de build y código de producto. El inventario de specs está vacío. El trabajo previo de colaboración en README y docs se conserva sin incorporarlo a la implementación de este change.

El 19/11/2026 Giovanni eligió C-01 a su cargo y aprobó Java 25, Maven y JUnit en `backend/`; intervalo explícito, reservas ocupantes como entrada, borde inclusivo antes y después para ambos recursos y aplazamiento de los estados y valor inicial de producción. El entorno observado ejecuta Maven 3.9.16 con JDK 25.0.4. `/usr/libexec/java_home` no reconoce el JDK Homebrew; los comandos de Java y Maven sí lo utilizan. No se modificarán archivos globales para resolver esa diferencia.

Este diseño introduce el primer módulo y dependencias de prueba, por lo que se incluye antes de Apply. Approval: approved (19/11/2026, by user). El usuario aprobó la propuesta y su aplicación explícitamente. El módulo está implementado y verificado; no está archivado ni integrado en main.

## Goals / Non-Goals

**Goals:** dominio puro, determinista, sin efectos laterales; contrato pequeño para reutilizarlo al añadir persistencia; escenario → prueba trazable y build reproducible documentado.

**Non-Goals:** decidir identidades físicas o esquema SQL, representar estados futuros, administrar bloqueos y horarios laborales, elegir versiones de Spring Boot o asegurar transacciones. La devolución de un turno no lo incorpora a una agenda persistida.

## Decisions

### Build y dependencias

Un único proyecto Maven en `backend/`, distribución estándar `src/main/java` y `src/test/java`, compilación con `release=25` sin características preview. Maven 3.9.16 es la referencia observada para reproducir la ejecución. Se fijarán versiones explícitas de Compiler Plugin, Surefire y JUnit Jupiter en el POM, sin rangos ni versiones dinámicas; el README registrará los valores efectivos usados en Apply.

Se propone JUnit Jupiter 6.1.3, cuya documentación oficial exige Java 17 o superior y es compatible con la base Java aprobada. Las versiones concretas de plugins se fijarán a releases publicadas compatibles comprobadas antes del primer build y quedarán registradas en la evidencia. Elegirlas es un detalle de build; si la compatibilidad exige cambiar Java/Maven/JUnit o el alcance, consultar al usuario.

No incorporar dependencias Spring, ORM, mocks de base o procesadores de código para este dominio. Alternativas consideradas: Gradle requiere otro build no elegido; Spring Boot añadiría infraestructura ajena al change; un script de assertions propio prescindiría del JUnit acordado. Un wrapper puede evaluarse después; los comandos de este change usarán el Maven instalado, sin afirmar que el clon instala herramientas.

Fuentes consultadas el 19/11/2026: [JUnit 6.1.3](https://docs.junit.org/6.1.3/overview.html), [soporte Maven de JUnit](https://docs.junit.org/6.1.3/running-tests/build-support.html), [objetivo Java en Maven](https://maven.apache.org/guides/mini/guide-java-target.html) y [Surefire](https://maven.apache.org/surefire/maven-surefire-plugin/). Context7 devolvió ejemplos de JUnit 5 anteriores; se contrastó el sitio oficial antes de proponer la versión.

### Modelo mínimo y frontera de entrada

Paquete propuesto `ar.edu.utn.odontologia.agenda`. Objetos inmutables para referencias opacas no vacías, prestación con duración de referencia, solicitud con ajuste opcional, reserva ocupante y turno calculado. Referencias de paciente/profesional/sillón/prestación se comparan por valor; no hay generación de identidad persistente, alta en catálogos ni datos clínicos. El nombre concreto de clases queda a implementación manteniendo el contrato del spec.

Representar inicio y fin como `Instant` y duraciones como `Duration`, evitando confundir una hora local con un instante en el dominio. Los ejemplos del spec asumen todos los horarios en una misma referencia; los fixtures convertirán explícitamente a UTC sin elegir la zona horaria del consultorio. La conversión de horarios del usuario corresponde al futuro adaptador. No consultar reloj ni imponer restricciones de pasado o jornada laboral que siguen sin acordarse.

Para el alcance aprobado, invocar el módulo con intervalo positivo explícito y reservas ocupantes válidas. No hay default de cero, minutos iniciales ni política de cambios de configuración. El contrato de producción sobre límites de configuración y estados sigue fuera de C-01. Los argumentos obligatorios faltantes y las duraciones efectivas no positivas se rechazan como entradas inválidas; el ajuste ausente usa la duración de referencia. Las referencias y períodos de las reservas también deben ser válidos para evitar una aceptación a partir de datos malformados.

### Disponibilidad simétrica y sin doble intervalo

Para cada reserva que comparta profesional o sillón, se admite separación si el fin de la solicitud más el intervalo es anterior o igual al inicio de la reserva, o si el fin de la reserva más el intervalo es anterior o igual al inicio de la solicitud. En cualquier otro caso existe conflicto. La comparación incluye igualdad y se aplica igual a ambos recursos.

No expandir simultáneamente los dos períodos por ambos extremos: eso duplicaría el intervalo requerido. Recorrer toda la colección sin asumir orden cronológico; las reservas de otros recursos no impiden el alta. Complejidad lineal respecto de las reservas recibidas, suficiente para el módulo aislado sin inventar volumen de producción.

Probar además variantes de contención total, períodos iguales y ajuste de duración, porque un algoritmo que comprueba solo el inicio podría omitir conflictos. Una reserva puede afectar ambos recursos; basta rechazar de forma determinista, sin imponer prioridad de mensajes administrativos.

### Resultado y errores

Devolver un resultado de dominio que distinga éxito con turno calculado, entrada inválida e indisponibilidad con el recurso afectado cuando sea identificable. Sin códigos HTTP ni mensajes de UI. Validar antes de calcular y no mutar las entradas; cualquier desbordamiento temporal o duración no representable será entrada inválida, no un éxito parcial. El spec no exige acumular todos los errores.

### TDD y verificación

Ejecutar primero pruebas que fallen por la ausencia del comportamiento esperado, añadir la implementación mínima y registrar el resultado GREEN; repetir por regla, sin presentar como RED una descarga fallida o un error de entorno. Incluir escenarios del spec y parámetros para ambos recursos y sentidos del intervalo.

Comandos previstos desde la raíz, disponibles únicamente después de Apply:

- `mvn -f backend/pom.xml test` para las pruebas unitarias.
- `mvn -f backend/pom.xml clean verify` para comprobar el estado integrado del módulo.
- `openspec validate crear-turno-sin-solapamientos-dominio --strict` para verificar artefactos.

Mantener un mapa escenario/prueba y evidencia RED/GREEN/Verify en el change. Verificar que Surefire descubre pruebas y que no se omiten. Ningún test de este change demuestra exclusión concurrente entre procesos ni integridad de PostgreSQL.

## Risks / Trade-offs

- [Reservas incompletas o seleccionadas incorrectamente por el llamador] → Documentar la precondición; el filtrado por estado y la lectura consistente se implementarán donde corresponda, no aquí.
- [La agenda cambia después de calcular el turno] → C-06 debe revalidar y persistir con garantías concurrentes; este módulo no ofrece una reserva atómica.
- [Java 25 no está en otro equipo] → Documentar JDK y Maven requeridos; no bajar silenciosamente la versión elegida.
- [Confundir fixtures de 10 minutos con configuración inicial] → Declararlos datos de prueba y exigir intervalo explícito en cada caso.
- [Artefactos marcados done se interpretan como implementación aprobada] → Mantener tareas sin completar y pedir revisión/aplicación explícita.

## Migration Plan

No hay datos ni código de producto que migrar. Apply añadirá el módulo de forma aislada y documentará sus comandos. Antes de escribir código comprobará aislamiento de la rama según la skill aplicable y preservará los cambios documentales previos. No se instalarán herramientas globales ni se ejecutarán servicios. Si se decide retirar la implementación, se hará mediante un cambio revisable que conserve documentación y evidencia, sin reset destructivo.

## Open Questions

No quedan preguntas bloqueantes dentro del alcance descrito. Fuera de este change permanecen: valor inicial y administración del intervalo, ciclo de estados, horarios y bloqueos, identidades físicas, modelo PostgreSQL y versión Spring Boot. PA-13 está respondida por la elección expresa de C-01; las demás PA solo están acotadas para esta unidad.

## UI Mockups

- Decision: no (no UI)
- Fecha: 19/11/2026.
- Este change no modifica páginas, componentes ni interacciones visuales; la política visual del proyecto permanece diferida.
