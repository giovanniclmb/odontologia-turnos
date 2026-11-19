# Backend — dominio y base de persistencia

C-01 aporta las reglas puras de agenda; C-02 añade Spring Boot no-web, referencias persistidas y migraciones. Todavía no hay endpoints, login, ficha clínica, turnos persistidos ni UI.

## Requisitos y versiones

- JDK 25 y Maven 3.9.16 como entorno de referencia.
- Docker con daemon accesible y Docker Compose v2 o posterior.
- PostgreSQL oficial 18.7 para desarrollo y Testcontainers.
- Spring Boot 4.1.2 coordina Hibernate 7.4.13.Final, Flyway 12.4.0, driver PostgreSQL 42.7.14, Testcontainers 2.0.5 y JUnit Jupiter 6.0.3. Las 55 pruebas de C-01 se conservan; su antiguo pin de JUnit se sustituyó por el BOM para alinear las dependencias.
- Compiler, Surefire y Failsafe fijados en pom.xml. No se usan características preview.

Clonar no instala herramientas. La primera ejecución descarga dependencias Maven e imágenes públicas y requiere conexión. Comprobar `java -version`, `mvn -version` y el preflight del repositorio.

## Pruebas

Desde la raíz:

```sh
mvn -f backend/pom.xml test
./scripts/check-docker.sh
mvn -f backend/pom.xml clean verify
```

`test` ejecuta las unitarias del dominio sin PostgreSQL. `clean verify` construye el JAR y ejecuta además las IT contra PostgreSQL temporal real. Falla si Docker no está disponible o no se descubren IT; no usa H2, mocks de base ni omisiones por daemon ausente. Los reportes están en backend/target/surefire-reports y backend/target/failsafe-reports.

Las pruebas no leen la conexión de desarrollo: aíslan el Environment de Spring, fuerzan configuración de classpath y suministran sus credenciales temporales. Las pruebas negativas usan destinos centinela inaccesibles y V2/checksums solo en recursos temporales. El fixture cierra únicamente sus contenedores.

## Desarrollo local

1. Copiar `.env.example` de la raíz a `.env` y definir las tres contraseñas privadas: DB_BOOTSTRAP_PASSWORD, DB_PASSWORD y DB_MIGRATION_PASSWORD. `.env` está ignorado por Git; no publicar ni imprimir sus valores.
2. Validar sin volcar credenciales: `docker compose config --quiet`.
3. Iniciar solo la base: `docker compose up -d db --wait`.
4. Exportar al proceso Java DB_URL, DB_USER, DB_PASSWORD, DB_MIGRATION_USER y DB_MIGRATION_PASSWORD con los valores correspondientes. Spring no lee `.env` automáticamente; Compose sí lo usa. El usuario ordinario debe ser odonto_app y el migrador odonto_migrator; sus contraseñas deben coincidir con las del bootstrap.
5. Ejecutar `mvn -f backend/pom.xml spring-boot:run` desde la raíz, o `java -jar backend/target/agenda-dominio-0.1.0-SNAPSHOT.jar` después de clean verify.

La URL de ejemplo es jdbc:postgresql://127.0.0.1:5433/odontologia_dev. DB_PORT permite cambiar el puerto de Compose; ajustar también DB_URL. No poner user, password ni service en la URL: se rechazan, incluso como claves codificadas, para evitar que sobreescriban las credenciales separadas. Los parámetros de transporte como connectTimeout y socketTimeout siguen permitidos.

El proceso valida conexión, migraciones y entidades y termina normalmente: no queda un servidor HTTP escuchando. El contenedor PostgreSQL sí permanece disponible con su volumen. No utiliza el Postgres.app del host. El nombre de proyecto predeterminado es odontologia-turnos; para varias copias en una misma máquina, acordar un nombre Compose y puerto distintos y usarlo consistentemente.

## Bootstrap y privilegios

El script docker/postgres/10-bootstrap.sh se monta de solo lectura y se ejecuta únicamente al inicializar un directorio de datos vacío. El volumen de PostgreSQL 18 se monta en /var/lib/postgresql. El administrador odonto_bootstrap solo prepara la base, roles y esquema; no es una conexión de Spring.

- odonto_migrator: dueño del esquema odontologia, sin SUPERUSER, CREATEDB ni CREATEROLE.
- odonto_app: USAGE en el esquema y SELECT/INSERT/UPDATE sobre las cuatro referencias, sin DDL ni DELETE.
- Flyway usa conexión separada de JPA. El arranque valida las identidades efectivas antes de abrir conexiones.

Cambiar `.env` no cambia credenciales de un volumen ya inicializado. Ante un error, revisar credenciales y diagnóstico; no borrar volúmenes, ejecutar Flyway clean/repair ni modificar una base ajena como solución automática. Detener el servicio sin borrar datos es distinto de eliminar su volumen; cualquier operación destructiva requiere alcance y autorización propios.

## Modelo y migraciones

El esquema odontologia contiene paciente, profesional, sillon y prestacion. Cada tabla tiene id uuid y codigo text obligatorio, no vacío ni formado solo por espacios comunes y único por tabla. La comparación es exacta, sin normalización. El mismo código puede existir en tipos distintos. Hibernate genera el UUID en la aplicación; PostgreSQL no tiene default de generación.

No hay columnas clínicas, cuentas, duración de prestación, intervalo ni tabla de turnos. Esas ampliaciones pertenecen a los changes siguientes. Las entidades y repositorios JPA están separados del paquete agenda y no se exponen como contratos HTTP.

V1__referencias_base.sql crea las tablas y permisos. Flyway es el único gestor de estructura; Hibernate usa validate y la inicialización SQL alternativa está desactivada. Flyway valida checksum y rechaza también migraciones futuras ausentes del binario; no hay baseline automático, clean ni out-of-order.

Versiones globales consecutivas: Giovanni coordina el próximo número según el orden real de integración. Una migración aplicada a una base de trabajo compartida no se renumera ni edita; se corrige con otra versión. Los tests de desarrollo usan contenedores nuevos al iterar V1 y nunca reparan el historial de una base persistente.

## Dominio C-01

`new Agenda().crear(solicitud, reservas, intervalo)` devuelve Exito(Turno), EntradaInvalida(motivo) o NoDisponible(recurso). Solicitud incluye referencias opacas, prestación con duración positiva, inicio Instant y ajuste Duration opcional; null en el ajuste usa la duración de la prestación. No consulta catálogos ni persiste un turno.

El intervalo positivo es explícito en cada llamada. Se admite exactamente el intervalo antes y después para profesional y sillón, sin duplicarlo. Se evalúan todas las reservas ocupantes válidas sin asumir orden; las ajenas no interfieren. Las entradas son inmutables. Los diez minutos de los fixtures no son configuración de producción.

C-02 no conecta aún la creación de turnos con estas tablas ni garantiza exclusión concurrente: C-06 deberá validar y persistir con garantías frente a cambios de la agenda. La evidencia de C-02 está en su change OpenSpec; los datos utilizados son ficticios.
