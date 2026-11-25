# Backend — dominio y base de persistencia

C-01 aporta las reglas puras de agenda. C-02 añade Spring Boot no-web, referencias persistidas y migraciones. C-03 aporta el acceso interno, C-04 la ficha clínica, C-05 la configuración de recursos y C-15 los bloqueos. C-06 conecta el alta y la consulta de turnos a PostgreSQL. C-11 incorpora el servidor HTTP para acceso interno, pacientes y ficha clínica.

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

## Transporte de email (C-17)

Variables de entorno opcionales para arranque y obligatorias al enviar: `RESEND_API_KEY`, `EMAIL_FROM`, `EMAIL_TEST_ALLOWLIST` (lista separada por comas), `EMAIL_MAX_RETRIES` (default 2) y `EMAIL_TIMEOUT` (default `10s`). Los secretos no se versionan; `.env` está ignorado por Git. El puerto neutro es `comunicaciones.CorreoSalida`; el adaptador Resend usa `java.net.http.HttpClient` y `email_envio` registra cada intento con idempotencia por `(tipo, turno, destinatario)`.

## Desarrollo local

1. Copiar `.env.example` de la raíz a `.env` y definir las tres contraseñas privadas: DB_BOOTSTRAP_PASSWORD, DB_PASSWORD y DB_MIGRATION_PASSWORD. `.env` está ignorado por Git; no publicar ni imprimir sus valores.
2. Validar sin volcar credenciales: `docker compose config --quiet`.
3. Iniciar solo la base: `docker compose up -d db --wait`.
4. Exportar al proceso Java DB_URL, DB_USER, DB_PASSWORD, DB_MIGRATION_USER y DB_MIGRATION_PASSWORD con los valores correspondientes. Spring no lee `.env` automáticamente; Compose sí lo usa. El usuario ordinario debe ser odonto_app y el migrador odonto_migrator; sus contraseñas deben coincidir con las del bootstrap.
5. Ejecutar `mvn -f backend/pom.xml spring-boot:run` desde la raíz, o `java -jar backend/target/agenda-dominio-0.1.0-SNAPSHOT.jar` después de clean verify.

La URL de ejemplo es jdbc:postgresql://127.0.0.1:5433/odontologia_dev. DB_PORT permite cambiar el puerto de Compose; ajustar también DB_URL. No poner user, password ni service en la URL: se rechazan, incluso como claves codificadas, para evitar que sobreescriban las credenciales separadas. Los parámetros de transporte como connectTimeout y socketTimeout siguen permitidos.

Desde C-11, el proceso principal valida conexión, migraciones y entidades y permanece escuchando HTTP en el puerto 8080. El CLI de bootstrap conserva su ejecución no-web. El contenedor PostgreSQL permanece disponible con su volumen. No utiliza el Postgres.app del host. El nombre de proyecto predeterminado es odontologia-turnos; para varias copias en una misma máquina, acordar un nombre Compose y puerto distintos y usarlo consistentemente.

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

## Cuenta y acceso del paciente — C-16

`ServicioCuentasPaciente.registrar(DatosRegistroCuentaPaciente)` crea una cuenta por email y contraseña, aplica la política de credenciales vigente y guarda el hash bcrypt; la identidad inicial queda `PENDIENTE`. El email de la cuenta es único entre cuentas, pero no se usa para asociarla con una ficha ni exige verificación por correo.

`ServicioCuentasPaciente.autenticar(CredencialesCuentaPaciente)` permite ingresar a una cuenta pendiente y devuelve `IdentidadCuentaPaciente`, separada de `Identidad` y los roles internos. Las credenciales incorrectas o un email no reconocido producen el mismo `AutenticacionFallidaException` genérico.

Recepción confirma el vínculo con `ServicioCuentasPaciente.confirmarVinculo(Identidad, VinculacionCuentaPaciente)`. La operación exige una identidad interna de recepción y una ficha existente. Cada ficha admite como máximo una cuenta; el usuario no puede confirmar ni cambiar su propio vínculo. Al vincular, el siguiente ingreso devuelve el identificador de paciente y el estado `VINCULADA`.

Los consumidores de lista deben usar `AutorizadorCuentaPaciente`: `exigirPacienteVinculado` devuelve el paciente autenticado o deniega el acceso, y `exigirPropiedad` permite continuar solo si la inscripción pertenece a ese paciente. La identidad de paciente no se acepta en servicios clínicos, administrativos ni de agenda. C-16 no incorpora ciclo de inscripción, rutas HTTP, recuperación ni transporte de sesión/token.

V10 crea `odontologia.cuenta_paciente` con email único, hash no vacío y referencia nullable/única a `paciente`; `odonto_app` tiene `SELECT`, `INSERT` y `UPDATE`, sin `DELETE`. Se conservan V8 de reprogramación y V9 de cancelación. `CuentasPacienteMigracionIT` verifica las restricciones y la actualización desde V9 preservando pacientes e historial con PostgreSQL real; `AccesoPersistenciaIT` verifica mapeo JPA y vinculación manual.

### C-11 — Verificación HTTP y entorno temporal para la interfaz

`ApiPacientesIT` levanta el servidor en loopback y puerto aleatorio sobre PostgreSQL 18.7 desechable. Comprueba el contrato de roles del login, rechazo 401, preflight CORS, permisos clínicos, alcance administrativo y revocación real al cerrar sesión. Se ejecuta con `mvn -f backend/pom.xml clean verify` junto con el resto de las pruebas.

Para reproducir la comprobación visual sin tocar la base de desarrollo, desde `backend/`:

```sh
mvn test-compile dependency:build-classpath -Dmdep.outputFile=/tmp/odonto-c11-classpath
java -cp "target/test-classes:target/classes:$(cat /tmp/odonto-c11-classpath)" ar.edu.utn.odontologia.persistencia.ApiC11Fixture
```

El fixture imprime `C11_DEMO_URL` con su puerto aleatorio. Crea únicamente datos ficticios y tres usuarios de prueba: `recepcion-demo`, `odonto-demo` y `colega-demo`, con contraseña pública de prueba `PruebaSegura986!`. Nunca usar estas credenciales en una base persistente. El fixture está en código de pruebas y no se empaqueta en el JAR de la aplicación. Ctrl-C cierra la API y su contenedor temporal. Configurar la URL del cliente con `window.__API_BASE_URL__` antes de cargar su módulo; el origen permitido de desarrollo es `http://localhost:5173`.

### C-10 — HTTP de agenda, bloqueos, intervalo y catálogos

Todas las rutas requieren `Authorization: Bearer <token>`; sin identidad responden `401 no_autenticado`, con las cabeceras CORS si el origen está permitido. Una falla interna al resolver el token responde `500 error_interno` con `correlacion`; el detalle queda solo en el registro del servidor. El backend aplica permiso y alcance en cada operación.

| Método y ruta | Uso | Acceso |
| --- | --- | --- |
| `GET /agenda/sillones/{sillonId}/turnos?desde&hasta` | Turnos del sillón con nombre y apellido del paciente | Recepción: todos; odontología: los de su profesional |
| `POST /agenda/turnos` | Alta (`201`): `pacienteId`, `profesionalId`, `sillonId`, `prestacionId`, `inicio` y `duracionAjustadaMinutos` opcional | Según alcance de C-06 |
| `GET /agenda/bloqueos?sillonId&desde&hasta` | Bloqueos del período | Recepción: todos; odontología: los del sillón consultado o de su profesional, con las demás referencias en `null` |
| `POST /agenda/bloqueos` | Alta (`201`): `inicio`, `fin` y `sillonId`, `profesionalId` o ambos | Recepción |
| `GET /sede/intervalo` | `{ "minutos": número o null }` | Equipo |
| `PUT /sede/intervalo` | Establece `minutos`, entero positivo | Recepción |
| `GET /recursos/sillones`, `GET /recursos/prestaciones` | Catálogos | Equipo |

Los instantes usan ISO 8601 con zona, entre los años 1 y 9998; un alta cuyo fin superaría el año 9999 se rechaza. No hay una duración máxima de negocio definida. Las consultas aceptan períodos semiabiertos de hasta 7 días. Los errores devuelven `{codigo, mensaje}`:

| Estado y código | Caso |
| --- | --- |
| `400 solicitud_invalida` | Dato ausente, mal formado, decimal donde se pide un entero o período inválido |
| `403 autorizacion_denegada` | Sin permiso o fuera de alcance |
| `404 solicitud_invalida` | Referencia inexistente |
| `409 horario_no_disponible` | Choque por profesional, sillón, intervalo o bloqueo, sin identificar la causa |
| `409 bloqueo_afecta_turno` | El bloqueo alcanza un turno ocupante |
| `409 intervalo_sin_configurar` | Alta sin intervalo de sede |

`ApiAgendaIT` verifica este contrato y `LecturasAgendaIT` el alcance de las lecturas, ambas sobre PostgreSQL real.

Para recorrer la interfaz de agenda sin tocar la base de desarrollo, con el puerto 8080 libre, desde `backend/`:

```sh
mvn test-compile dependency:build-classpath -Dmdep.outputFile=target/demo-classpath
java -cp "target/test-classes:target/classes:$(cat target/demo-classpath)" ar.edu.utn.odontologia.persistencia.ApiC10Demo
```

`ApiC10Demo` suma al fixture de C-11 los sillones `SIL-01` y `SIL-02`, las prestaciones `CONTROL` y `TRATAMIENTO` y un paciente sin asignación, todos ficticios. El intervalo queda sin configurar. Usa los mismos usuarios de prueba, nunca en una base persistente. Ctrl-C cierra la API y su contenedor.
