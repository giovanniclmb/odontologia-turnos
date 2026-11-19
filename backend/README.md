# Dominio de agenda — C-01

Requiere JDK 25 y Maven; entorno de referencia: OpenJDK 25.0.4 y Maven 3.9.16. Plugins fijados: Compiler 3.16.0, Surefire 3.6.1; JUnit Jupiter 6.1.3. No usa características preview.

Desde la raíz del repositorio:

```sh
mvn -f backend/pom.xml test
mvn -f backend/pom.xml clean verify
```

La primera ejecución descarga dependencias públicas a la caché de Maven y necesita conexión. Clonar no instala Java ni Maven. En macOS comprobar `java -version` y `mvn -version`; no depender de que `java_home` detecte un JDK de Homebrew.

El módulo calcula un turno contra reservas ocupantes explícitas; no guarda datos ni ofrece exclusión concurrente. No incluye servidor, API, Spring Boot, PostgreSQL o interfaz.

## Contrato del dominio

`new Agenda().crear(solicitud, reservas, intervalo)` devuelve `Exito(Turno)`, `EntradaInvalida(motivo)` o `NoDisponible(recurso)`. El recurso se identifica como PROFESIONAL, SILLON o AMBOS para la reserva conflictiva detectada; no se acumulan todos los conflictos.

`Agenda.Solicitud` recibe referencias opacas no vacías de paciente/profesional/sillón, una `Prestacion(referencia, duracion)`, inicio `Instant` y ajuste `Duration` opcional (`null` significa usar duración de prestación). La referencia de la prestación y su duración positiva son obligatorias incluso con ajuste. No se consultan catálogos.

La lista de `Reserva(profesional, sillon, inicio, fin)` debe contener únicamente reservas que ocupan recursos, con recursos no vacíos y fin posterior al inicio. Se validan todas antes de evaluar conflictos. El intervalo positivo es obligatorio en cada invocación: no hay valor predeterminado. Los diez minutos de los tests son fixtures, no configuración de producción. Las entradas malformadas y los desbordamientos temporales se devuelven como EntradaInvalida.

Los valores son records inmutables y la lista recibida no se modifica. El turno devuelto conserva referencias, inicio, fin y duración efectiva; no tiene ID persistente. Los tests usan datos ficticios e instantes UTC. La zona horaria del consultorio, estados, catálogo, horarios laborales, bloqueos y límites de configuración no se deciden aquí.

## Disponibilidad

Se admite un turno si, para cada reserva que comparta profesional o sillón, su fin más el intervalo es menor o igual al inicio de la reserva, o el fin de la reserva más el intervalo es menor o igual al inicio del turno. La igualdad se permite y el intervalo no se duplica. Las reservas se recorren sin asumir orden. Complejidad lineal respecto de la cantidad de reservas recibidas.

El resultado se calcula contra una instantánea proporcionada por el llamador. Dos llamadas pueden devolver éxito para el mismo horario: C-06 deberá revalidar y persistir con garantías concurrentes. La lista recibida permanece intacta tanto al aceptar como al rechazar. La evidencia y el mapa de escenarios están en el change OpenSpec de C-01.
