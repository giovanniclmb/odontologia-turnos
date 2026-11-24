# Backend — dominio y base de persistencia

C-01 aporta las reglas puras de agenda. C-02 añade Spring Boot no-web, referencias persistidas y migraciones. C-03 aporta el acceso interno, C-04 la ficha clínica, C-05 la configuración de recursos y C-15 los bloqueos. C-06 conecta el alta y la consulta de turnos a PostgreSQL.

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
- Bootstrap del primer usuario (una sola vez por técnico), sin HTTP: `mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.main-class=ar.edu.utn.odontologia.acceso.cli.BootstrapCli` con DB_URL, DB_USER, DB_PASSWORD, DB_MIGRATION_USER y DB_MIGRATION_PASSWORD exportadas. Variables opcionales: BOOTSTRAP_NOMBRE_USUARIO, BOOTSTRAP_CONTRASENA y BOOTSTRAP_ROL; si faltan, se solicitan por consola (contraseña oculta). El proceso inicializa el contexto Spring/JPA, ejecuta la migración y llama a ServicioBootstrap.bootstrapInicial; un segundo intento se rechaza.

Cambiar `.env` no cambia credenciales de un volumen ya inicializado. Ante un error, revisar credenciales y diagnóstico; no borrar volúmenes, ejecutar Flyway clean/repair ni modificar una base ajena como solución automática. Detener el servicio sin borrar datos es distinto de eliminar su volumen; cualquier operación destructiva requiere alcance y autorización propios.

## Modelo y migraciones

El esquema odontologia contiene paciente, profesional, sillon, prestacion, usuario, token_acceso e historial_contrasena. usuario tiene id uuid, nombre_usuario text obligatorio no vacío y único, hash_credencial text, rol CHECK (recepcion/odontologo), activo, profesional_ref uuid opaco y creado_en. token_acceso tiene id uuid, usuario_id FK, hash_token, creado_en, expira_en y revocado. Hibernate genera el UUID en la aplicación; PostgreSQL no tiene default de generación. odonto_app tiene SELECT/INSERT/UPDATE sobre usuario y token_acceso, sin DELETE.

Las entidades y los repositorios JPA están separados del dominio. La agenda persistida usa `agenda.persistencia` y expone sus datos mediante DTO del servicio.

V1__referencias_base.sql crea las referencias y permisos. V2__acceso_y_permisos.sql crea usuario y token_acceso con sus constraints y permisos. Flyway es el único gestor de estructura; Hibernate usa validate y la inicialización SQL alternativa está desactivada. Flyway valida checksum y rechaza también migraciones futuras ausentes del binario; no hay baseline automático, clean ni out-of-order.

Versiones globales consecutivas: Giovanni coordina el próximo número según el orden real de integración. Una migración aplicada a una base de trabajo compartida no se renumera ni edita; se corrige con otra versión. Los tests de desarrollo usan contenedores nuevos al iterar V1 y nunca reparan el historial de una base persistente.

### C-05 — Recursos, prestaciones e intervalo

V4 agrega el intervalo único de sede, inicialmente sin configurar, el estado operativo de profesionales y sillones y la duración de referencia de prestaciones. Conserva los UUID y códigos de V1. Un recurso heredado sin estado se considera no disponible. Una prestación heredada sin duración sigue consultable y recepción puede establecer su duración por el servicio. La FK de profesional valida los vínculos existentes al migrar. Antes de aplicar V4 en una base persistente, comprobar que las referencias no nulas a profesionales correspondan a filas existentes.

Las operaciones están disponibles en `ServicioConfiguracionSede`, `ServicioRecursosAgenda` y `ServicioVinculacionProfesional`. Pasan la identidad interna autenticada a los servicios. Recepción puede configurar, dar de alta recursos, ajustar duraciones, crear usuarios internos y vincularlos a profesionales. Odontología puede consultar la configuración y los catálogos. La disponibilidad mínima devuelve el intervalo vigente cuando el profesional y el sillón existen y están activos. El dominio de agenda sigue recibiendo un intervalo explícito por invocación.

### C-06 — Turnos persistidos

`ServicioAgenda.crear(identidad, solicitud)` recibe los UUID de paciente, profesional, sillón y prestación, un inicio `Instant` y un ajuste opcional en minutos. Valida el permiso y el alcance de la identidad autenticada. Recepción puede operar en toda la sede. Odontología puede crear para su profesional vinculado y pacientes con asignación activa. El servicio devuelve el turno en estado `PENDIENTE` o informa entrada o referencia inválida, falta de configuración, acceso denegado o conflicto de disponibilidad.

`ServicioAgenda.consultarAgendaSillon(identidad, sillonId, inicio, fin)` devuelve los turnos que intersectan el período semiabierto, ordenados por inicio. Odontología ve los turnos de su profesional. El alta verifica la ocupación completa de ambos recursos y devuelve un conflicto genérico cuando el horario está ocupado.

V7 agrega `odontologia.turno` con cuatro claves foráneas, UUID generado en la aplicación y permisos `SELECT`/`INSERT` para `odonto_app`. El servicio normaliza el inicio a microsegundos y conserva el fin y la duración efectiva. El alta valida y guarda bajo los locks compartidos de C-15 en `READ COMMITTED`. El intervalo se aplica entre turnos y la contigüidad con un bloqueo está permitida. Las pruebas de integración ejercitan ambos servicios con su almacenamiento real. [AgendaPersistidaIT](src/test/java/ar/edu/utn/odontologia/persistencia/AgendaPersistidaIT.java)

### C-07 — Reprogramación de turnos

`ServicioAgenda.reprogramar(identidad, solicitud)` recibe el UUID del turno, su versión esperada y el inicio, profesional y sillón de destino. Alta y consulta incluyen la versión vigente en `TurnoRegistrado`. El operador usa ese valor como token opaco y vuelve a consultar si el servicio informa una versión desactualizada.

La operación mueve un turno pendiente cuyo inicio todavía es futuro a otro inicio futuro, conservando el mismo paciente, prestación y duración efectiva. Recepción puede elegir otro profesional. Odontología puede cambiar horario y sillón de sus propios turnos de pacientes asignados, conservando el profesional. Un destino idéntico tras normalizar el inicio devuelve el turno vigente y conserva su versión. Un rechazo conserva la asignación que ya estaba confirmada.

V8 inicializa la versión de turnos existentes y nuevos. El rol de aplicación recibe `UPDATE` solo sobre profesional, sillón, inicio, fin y versión. Se bloquea la fila del turno y después se adquieren juntos los recursos de origen y destino mediante C-15. La actualización y el incremento de versión se confirman en la misma transacción. [ReprogramacionPersistidaIT](src/test/java/ar/edu/utn/odontologia/persistencia/ReprogramacionPersistidaIT.java)

### C-08 — Cancelación interna de turnos

`ServicioAgenda.cancelar(Identidad, SolicitudCancelacion)` recibe el UUID del turno y su versión opaca esperada. Exige `turno:cancelar`. Recepción puede operar en toda la sede y odontología solo puede cancelar sus propios turnos de pacientes con asignación activa. El turno debe estar `PENDIENTE` y su inicio debe seguir siendo futuro tras adquirir los locks. Una versión anterior pide volver a consultar. Una solicitud con versión vigente sobre un turno ya cancelado se rechaza explícitamente.

El `TurnoRegistrado` devuelto conserva la asignación y la duración, con estado `CANCELADO` y una versión nueva. El registro sigue visible en la agenda por sillón dentro del alcance de la consulta y deja de ocupar el profesional y el sillón al confirmar la transacción. Si el llamador abre una transacción exterior, usa `READ COMMITTED`, entra sin locks previos de agenda y respeta el orden fila → recursos durante toda la transacción. En ese caso el resultado es provisional hasta el commit exterior.

V9 admite `PENDIENTE` y `CANCELADO` y suma únicamente el permiso de `UPDATE` sobre `estado` para `odonto_app`. La cancelación se persiste actualizando `estado` y `version` en la misma escritura. [CancelacionPersistidaIT](src/test/java/ar/edu/utn/odontologia/persistencia/CancelacionPersistidaIT.java)

## Dominio C-01

`new Agenda().crear(solicitud, reservas, intervalo)` devuelve Exito(Turno), EntradaInvalida(motivo) o NoDisponible(recurso). Solicitud incluye referencias opacas, prestación con duración positiva, inicio Instant y ajuste Duration opcional; null en el ajuste usa la duración de la prestación. No consulta catálogos ni persiste un turno.

El intervalo positivo es explícito en cada llamada. Se admite exactamente el intervalo antes y después para profesional y sillón, sin duplicarlo. Se evalúan todas las reservas ocupantes válidas sin asumir orden; las ajenas no interfieren. Las entradas son inmutables. Los diez minutos de los fixtures no son configuración de producción.

Las pruebas puras de C-01 verifican el cálculo con reservas provistas por el llamador. La exclusión concurrente de la agenda persistida se comprueba en C-06 con PostgreSQL y transacciones independientes. Los datos de prueba son ficticios.


## Correcciones de C-03 — Issue #30 (20/11/2026)

Contrato aprobado por Giovanni: un usuario activo cambia solo su propia contraseña mediante `ServicioCambioContrasena.cambiarContrasena(identidad, actual, nueva)`, confirmando la vigente. No recibe un usuario destino, no añade endpoints, recuperación ni permisos de gestión de cuentas ajenas. El estado activo se vuelve a comprobar en la base; presentar una identidad anterior no permite cambiar una cuenta desactivada.

Se rechazan la contraseña vigente y las cuatro anteriores (cinco en total). V3__historial_contrasena.sql registra hashes previos, con UUID generado en aplicación y secuencia por usuario; una transacción con bloqueo de fila serializa validación, historial y actualización. Un rechazo o fallo de persistencia revierte el cambio completo. La clave vigente cuenta incluso para usuarios anteriores a V3; no se inventan contraseñas históricas inexistentes. El historial se conserva, mientras la consulta de validación toma solo las últimas cuatro previas; la secuencia determina el orden sin depender de empates de reloj.

El usuario ordinario tiene SELECT/INSERT sobre el historial, sin UPDATE/DELETE. La unicidad usuario/secuencia sirve también como índice para la lectura ordenada. El servicio aplica la política existente a los datos disponibles: nombre de usuario; el modelo actual no almacena nombre personal ni email. El alcance no implementa todavía emisión/revocación de tokens, transportes ni recuperación.

BootstrapCliIT ejecuta el CLI en un JVM hijo con entorno vacío y una allowlist del fixture; fuerza `classpath:/application.yaml`, acota memoria, tiempo y salida, y conserva la configuración original del proceso padre. La regresión mantiene valores Spring centinela inaccesibles en el padre. ProcesoAisladoTest verifica que el entorno del hijo contiene exclusivamente la allowlist, sin mostrar valores heredados en diagnósticos.

BootstrapMavenIT comprueba el comando `spring-boot:run` documentado arriba, seleccionando explícitamente el CLI con `spring-boot.run.main-class`, contra PostgreSQL temporal y también el segundo intento rechazado. Usa settings vacíos, repositorio Maven local y modo offline; no lee settings/credenciales del usuario ni descarga dependencias desde el proceso hijo. El Maven de la ejecución principal debe estar disponible en PATH; la prueba se verifica en macOS y en el CI Linux del proyecto.
