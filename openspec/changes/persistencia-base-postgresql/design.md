# Design

## Context

Ver [proposal.md](proposal.md) y el [spec](specs/persistencia-de-referencias/spec.md). C-01 está integrado y archivado; su dominio puro tiene 55 pruebas. El backend es un único módulo Maven Java 25. CI ya ejecuta clean verify en Ubuntu con Java 25. La única capacidad principal existente es disponibilidad-de-turnos y no cambia en C-02.

Giovanni confirmó el 19/11/2026: Boot 4.1.2, PostgreSQL 18.7, JPA/Hibernate separado del dominio, Flyway SQL, Compose para desarrollo, Testcontainers para pruebas, cuatro referencias UUID/código y versiones globales consecutivas coordinadas al integrar. Se usa la rama codex/c-02-persistencia en el worktree aislado existente; no se crea otro worktree ni se modifica Postgres.app, que tiene un servidor activo en el host.

Approval: pending. Las decisiones del menú permiten redactar este plan; no equivalen a aprobar un diseño todavía no presentado ni a comenzar Apply.

## Goals / Non-Goals

**Goals:** arrancar un contexto Spring Boot con esquema comprobado, persistir referencias mínimas con restricciones reales, separar privilegios y reproducir el entorno en el equipo y CI.

**Non-Goals:** introducir capas o módulos Maven adicionales, aplicar anotaciones ORM al dominio de C-01, exponer HTTP, autenticar usuarios, decidir campos clínicos o crear entidades de turno. No se atribuye a los tests de C-02 exclusión concurrente de agenda.

## Decisions

### 1. Módulo y dependencias

Mantener backend/ y Java 25/Maven. Incorporar la gestión de dependencias de Boot 4.1.2, sus componentes de arranque, JPA, soporte Flyway/PostgreSQL y pruebas. Las versiones de Hibernate, driver PostgreSQL, Flyway y Testcontainers se coordinan con el BOM de Boot y se registran mediante dependency:tree en Apply. No seleccionar snapshots ni imponer versiones externas sin comprobar compatibilidad. Si alguna incompatibilidad exige cambiar la versión base aprobada, consultar al usuario.

No añadir starter web ni servidor. La aplicación se configura como no-web: valida infraestructura y puede cerrar normalmente al finalizar el proceso de arranque; no se presenta como servidor HTTP disponible. Registrar Agenda mediante configuración externa como bean reutilizable sin cambiar su clase ni sus records. Las pruebas comprueban el contexto y preservan los 55 casos unitarios sin PostgreSQL.

Alternativas descartadas por elección del usuario: JdbcTemplate/Spring Data JDBC, otra versión Java y trasladar la base a un servicio externo. No se crea Dockerfile del backend: el alcance aprobado usa Compose solo para PostgreSQL y ejecuta Java mediante Maven, no dockeriza la aplicación.

### 2. Cuatro referencias con modelo propio de persistencia

Paquetes propuestos: ar.edu.utn.odontologia para arranque; referencias.persistencia para entidades y repositorios. Las entidades nunca se exponen mediante endpoints ni se convierten en los records del dominio. No hacen falta DTOs HTTP en este change porque no existe ese contrato.

Esquema PostgreSQL del proyecto: odontologia. Tablas paciente, profesional, sillon y prestacion, cada una con dos columnas:

| Columna | Tipo y restricción |
| --- | --- |
| id | uuid, clave primaria, sin default de generación en PostgreSQL |
| codigo | text, NOT NULL, UNIQUE por tabla y CHECK de código no vacío ni compuesto solo de espacios comunes |

La unicidad compara el valor exacto: no hay lower(), recorte del dato guardado ni política de nombres clínicos. Un mismo código entre tipos está permitido. Las tablas todavía no tienen relaciones, campos clínicos, duración, estado, timestamps ni configuración de intervalo; esos datos se añaden en los changes responsables. Esta base no es un CRUD completo de pacientes o recursos.

El proveedor JPA genera UUID en la aplicación al persistir, sin extensiones de PostgreSQL ni defaults SQL. Para estas pequeñas referencias se acepta la localidad de índice menos favorable de UUID aleatorio como trade-off frente a bigint; no se añade una biblioteca solo para UUID ordenables. UUID es el tipo Java y SQL; la representación textual para el dominio se definirá al conectar el caso de uso posterior. Repositorios exponen únicamente las operaciones necesarias para guardar y recuperar referencias, sin diseñar una API de negocio futura.

### 3. Esquema gestionado exclusivamente por Flyway

Primera migración prevista: backend/src/main/resources/db/migration/V1__referencias_base.sql. Creará las cuatro tablas, restricciones y permisos de datos necesarios en el esquema preparado. Hibernate usa ddl-auto=validate; no create, create-drop o update. No se mezcla schema.sql/data.sql con Flyway ni se insertan ejemplos permanentemente al arrancar la aplicación.

Flyway valida checksums, conserva su historial y mantiene out-of-order=false, clean deshabilitado y sin baseline automático sobre una base desconocida. La aplicación falla si el esquema o el historial no coincide; no ejecuta reparación silenciosa.

Convención aprobada: versiones globales consecutivas. Cada autor prepara su migración; Giovanni reserva/confirma el próximo número según el orden real de integración. Antes de integrar, comparar main y renumerar únicamente migraciones que aún no se aplicaron. Una vez aplicada, no editarla ni renumerarla: corregir con la siguiente migración. El ID del change se registra en el nombre descriptivo o documentación, no determina la precedencia de versión.

### 4. Bootstrap y privilegios separados

Compose crea una base dedicada de desarrollo, por ejemplo odontologia_dev. Bootstrap dentro del contenedor prepara el esquema odontologia y los roles odonto_migrator y odonto_app. El administrador solo se usa en ese bootstrap. Ningún rol de aplicación o migración tendrá SUPERUSER, CREATEDB o CREATEROLE; el migrador posee únicamente el esquema del proyecto y el usuario ordinario recibe USAGE y SELECT/INSERT/UPDATE sobre las cuatro referencias, sin DDL. No se configura DELETE hasta que una operación posterior lo requiera y se acuerde.

Spring utiliza credenciales ordinarias para JPA y credenciales separadas para Flyway. Los scripts no interpolan contraseñas como SQL crudo: utilizarán el mecanismo de quoting de parámetros de psql. No se guarda configuración privada en el repositorio ni se imprime en logs.

.env.example documentará solo nombres y placeholders: DB_URL, DB_USER, DB_PASSWORD, DB_MIGRATION_USER, DB_MIGRATION_PASSWORD y las variables del bootstrap/puerto necesarias. Las credenciales locales de Compose se configuran en .env ignorado; Spring recibe su configuración mediante variables o un mecanismo de importación explícito documentado. No se asume que Spring lea .env automáticamente. La ausencia de variables obligatorias falla de forma explícita. No se usan contraseñas reales ni una contraseña compartida del equipo en ejemplos.

### 5. Compose de desarrollo acotado

compose.yaml contiene solo PostgreSQL oficial 18.7 con etiqueta completa fijada, healthcheck pg_isready, volumen dedicado y puerto configurable limitado a 127.0.0.1. Se propone 5433 para evitar el Postgres.app observado en 5432. No añadir container_name fijo. El proyecto/volumen debe distinguirse de otros proyectos para no conectarse o interferir con una base ajena.

Para PostgreSQL 18, comprobar y respetar el layout del contenedor oficial y montar el volumen en /var/lib/postgresql. Los scripts de inicialización corren únicamente con el directorio vacío; cambiar .env no altera por sí solo usuarios o passwords de un volumen existente. Documentar esta limitación y fallar con diagnóstico ante discrepancias, sin recomendar borrar volúmenes como solución automática.

Comandos previstos: docker compose config --quiet, docker compose up -d db y arranque Maven con el entorno cargado como se documente en README. No incluir en la guía una limpieza destructiva de datos; cualquier futura eliminación de volumen exige su propio alcance y consentimiento. Estas operaciones se ejecutan en Apply, no durante planificación.

### 6. Pruebas reales y ciclo de verificación

Conservar *Test para las pruebas unitarias de C-01 y añadir *IT con Failsafe para PostgreSQL. mvn test sigue ejecutando el dominio sin necesitar Docker; mvn clean verify ejecuta todo y falla si no hay Docker o no se descubren IT. No usar disabledWithoutDocker, H2, perfiles que omitan IT en CI ni mocks como evidencia de integridad.

Testcontainers inicia PostgreSQL 18.7 temporal con puerto aleatorio y datos ficticios. Un bootstrap controlado crea roles como en desarrollo; la conexión del contexto usa el usuario ordinario y Flyway el migrador, no el superusuario temporal. El administrador del contenedor se conserva solo para preparar y comprobar metadatos/privilegios durante tests. La configuración de prueba no lee DB_URL del desarrollo ni depende del .env de otro integrante.

Casos esenciales: guardar y recuperar cada tipo en transacciones/lecturas nuevas; UUID y códigos; duplicado y dato inválido rechazados por SQL real; contexto reiniciado sobre la misma base sin perder datos; V1 aplicada una sola vez; checksum alterado en una copia temporal de prueba, sin editar el recurso productivo, provoca fallo; mala configuración/conexión/credenciales falla con espera acotada; roles ordinario y migrador no superusuarios, DDL rechazado al usuario ordinario; contexto no-web y regresión de 55 pruebas.

Registrar RED real antes de la implementación correspondiente, GREEN por grupo y Verify con reportes unitarios e IT. Un error de descarga o daemon no es RED de comportamiento. El caso de Docker no disponible se verifica mediante el fallo reproducible del preflight documentado, sin detener el Docker del usuario. Los contextos y contenedores temporales se cierran como parte del fixture; no ejecutar limpiezas de recursos ajenos.

CI mantiene el check backend-verify y añade una comprobación explícita de Docker antes de clean verify. No modifica la política de revisión, exención de Giovanni ni permisos del token.

## Risks / Trade-offs

- [Credenciales o esquema ajenos] → Instancia Compose dedicada, roles separados y prueba sin conexión de desarrollo.
- [Bootstrap omitido en volumen existente] → Documentar inicialización única y validar precondiciones; no reparar ni borrar automáticamente.
- [Desorden de migraciones paralelas] → Próxima versión coordinada al integrar; no versiones derivadas de C-NN ni out-of-order.
- [Pruebas de integración omitidas] → Failsafe y preflight obligatorios en verify/CI, sin skip por Docker ausente.
- [Confundir base mínima con agenda persistida o autenticación] → Mantener tablas y contratos acotados; C-03 a C-06 amplían su alcance por separado.
- [Tiempo de descarga o indisponibilidad del registro] → Declarar red/daemon requeridos; registrar diagnóstico de entorno, sin reemplazar el PostgreSQL real por un mock.

## Migration Plan

Apply añadirá configuración, bootstrap y V1 a una base nueva dedicada. No migra datos existentes ni toca Postgres.app. Validará la configuración antes de iniciar recursos y documentará versiones y variables sin secretos.

Revisar y validar los artefactos antes de Apply. Después de implementar, ejecutar unitarias e IT, revisar la evidencia y los permisos, y solicitar el cierre/archivo por el flujo correspondiente. No hacer push sin pedido expreso. Si se necesita revertir código, hacerlo mediante un cambio revisable; una base con migraciones aplicadas se corrige con una migración nueva, sin Flyway clean, repair o borrado de volumen automático.

## Open Questions

No quedan elecciones bloqueantes en el alcance del plan. Permanecen fuera de C-02 los campos y operaciones de C-03/C-04/C-05, tabla y consistencia de turnos de C-06, y la configuración clínica/funcional pendiente de la KB. La compatibilidad fina de dependencias gestionadas se comprobará con el build; si exige cambiar una base aprobada, se consulta antes de proceder.

## UI Mockups

- Decision: no (no UI)
- Fecha: 19/11/2026.
- Política visual sin cambios: este change no crea ni modifica pantallas.

## Sources

Fuentes primarias consultadas el 19/11/2026: [requisitos de Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html), [JPA e inicialización en Boot 4.1.0](https://github.com/spring-projects/spring-boot/blob/v4.1.0/documentation/spring-boot-docs/src/docs/antora/modules/reference/pages/data/sql.adoc), [Testcontainers en Boot](https://github.com/spring-projects/spring-boot/blob/v4.1.0/documentation/spring-boot-docs/src/docs/antora/modules/reference/pages/testing/testcontainers.adoc), [versiones PostgreSQL](https://www.postgresql.org/support/versioning/) e [imagen oficial PostgreSQL](https://raw.githubusercontent.com/docker-library/docs/master/postgres/README.md). La documentación de la imagen y la página de versiones mostraron distinta actualización para PostgreSQL 19; se usa el 18.7 aprobado, presente en ambas, sin inferir otra versión.
