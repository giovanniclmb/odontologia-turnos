# Spec Delta

## Purpose

Proporcionar almacenamiento persistente e íntegro de las referencias mínimas de agenda y un arranque verificable sobre un esquema versionado. Esta base permitirá extender pacientes y recursos sin acoplar la lógica de disponibilidad a la infraestructura.

## ADDED Requirements

### Requirement: Referencias independientes con identidad estable

El backend SHALL persistir por separado referencias de paciente, profesional, sillón y prestación. Cada referencia SHALL tener un UUID generado por la aplicación y un código obligatorio. Recuperar la referencia SHALL conservar su UUID y código.

#### Scenario: Guardar y recuperar cada tipo
- **WHEN** se guarda una referencia ficticia de cada uno de los cuatro tipos y se recupera en una nueva operación de lectura
- **THEN** cada referencia conserva su UUID no nulo y código original

#### Scenario: Identidades diferentes en nuevas referencias
- **WHEN** se guardan dos referencias nuevas de un mismo tipo con códigos diferentes
- **THEN** sus UUID generados son distintos

### Requirement: Código único dentro de cada tipo

La base SHALL rechazar códigos duplicados dentro del mismo tipo de referencia. El mismo código SHALL poder existir en tipos distintos. La comparación del código será exacta, sin normalización de mayúsculas o espacios realizada por esta operación.

#### Scenario: Duplicado dentro del mismo tipo
- **WHEN** se intenta guardar otra referencia del mismo tipo con un código ya persistido
- **THEN** la base rechaza la segunda inserción y conserva la referencia original

#### Scenario: Código compartido por tipos diferentes
- **WHEN** se utiliza REF-001 para un paciente y también para un sillón
- **THEN** ambas referencias se persisten correctamente

### Requirement: Integridad de referencias en la base

La base SHALL impedir identificadores nulos, códigos nulos, vacíos o formados únicamente por espacios comunes, e identidades repetidas dentro de un mismo tipo. Los fallos de integridad SHALL dejar sin cambios los datos previamente confirmados.

#### Scenario: Inserción directa con datos inválidos
- **WHEN** una operación SQL de prueba intenta insertar UUID nulo o repetido, o un código nulo, vacío o de espacios en cualquiera de los cuatro tipos
- **THEN** PostgreSQL rechaza la operación mediante sus restricciones y conserva los datos confirmados

### Requirement: Conservación entre arranques

Las referencias confirmadas SHALL sobrevivir al cierre y nuevo arranque del backend contra la misma base. El arranque SHALL validar el esquema sin recrear, borrar ni reemplazar los datos existentes.

#### Scenario: Reiniciar sobre la misma base
- **WHEN** se persisten referencias, se cierra el backend y se abre un nuevo contexto contra la misma base
- **THEN** las referencias siguen disponibles con los mismos datos

### Requirement: Evolución versionada y validada del esquema

El backend SHALL aplicar la primera migración al arrancar sobre una base inicializada para el proyecto y registrar su ejecución. Arranques posteriores SHALL evitar reaplicarla. Una discrepancia de una migración ya aplicada SHALL impedir declarar el arranque exitoso.

#### Scenario: Primera migración en base sin tablas de aplicación
- **WHEN** el backend arranca por primera vez con la base y los roles de desarrollo preparados
- **THEN** aparecen las cuatro tablas de referencia y el registro de migración aplicada correctamente

#### Scenario: Segundo arranque sin cambios
- **WHEN** se reinicia el backend con el mismo conjunto de migraciones
- **THEN** no se reaplica la migración y las referencias existentes se conservan

#### Scenario: Discrepancia de migración aplicada
- **WHEN** una prueba controlada presenta un checksum diferente para una migración ya aplicada
- **THEN** el arranque falla sin reparar el historial ni borrar las referencias existentes

### Requirement: Fallo explícito ante conexión o configuración inválida

El backend SHALL requerir configuración explícita de conexión y credenciales. Una configuración obligatoria ausente, credenciales inválidas o base inaccesible SHALL producir un fallo de arranque identificable, sin sustituir PostgreSQL por otra base ni revelar secretos en la evidencia.

#### Scenario: Configuración obligatoria ausente
- **WHEN** se intenta arrancar sin la URL o credenciales requeridas
- **THEN** el arranque falla y la documentación permite identificar el dato faltante

#### Scenario: Credenciales rechazadas
- **WHEN** se intenta arrancar con una credencial incorrecta para la base de prueba
- **THEN** el arranque falla sin acceder a otra base ni registrar la contraseña en la evidencia

#### Scenario: Base inaccesible
- **WHEN** la conexión de prueba apunta a un destino inaccesible con tiempo de espera acotado
- **THEN** se obtiene un fallo de arranque, no un resultado exitoso con almacenamiento alternativo

### Requirement: Separación de privilegios de operación y migración

Las operaciones ordinarias SHALL usar un usuario sin privilegios de superusuario ni capacidad de crear o borrar tablas. La migración SHALL ejecutarse mediante una identidad distinta con permisos limitados al esquema del proyecto. Las credenciales del administrador de bootstrap no se usarán como conexión ordinaria.

#### Scenario: Usuario ordinario limitado
- **WHEN** la prueba consulta la identidad activa de la conexión ordinaria e intenta crear una tabla en el esquema del proyecto
- **THEN** confirma que no es superusuario, la creación es rechazada y la lectura y escritura autorizadas de referencias funcionan

#### Scenario: Migraciones con identidad separada
- **WHEN** se comprueba la configuración efectiva y el propietario de las tablas creadas
- **THEN** la identidad de migración es distinta de la ordinaria y no es superusuario

### Requirement: Aislamiento de desarrollo y pruebas

Las pruebas de integración SHALL ejecutarse contra PostgreSQL real temporal, separado del entorno de desarrollo y de bases preexistentes del equipo. La verificación completa SHALL fallar si no puede ejecutar esas pruebas; no deberá omitirlas silenciosamente.

#### Scenario: Pruebas aisladas
- **WHEN** se ejecuta la suite de integración sin proporcionar una conexión de desarrollo
- **THEN** utiliza su PostgreSQL temporal y no modifica las referencias del entorno de desarrollo

#### Scenario: Entorno de contenedores no disponible
- **WHEN** el entorno requerido para crear la base temporal no está disponible
- **THEN** la verificación completa falla con diagnóstico del entorno y no informa pruebas de persistencia aprobadas u omitidas como evidencia válida

### Requirement: Dominio sin efectos de infraestructura ni exposición HTTP

La incorporación de persistencia SHALL conservar los resultados y reglas de disponibilidad de C-01. El arranque de esta unidad SHALL realizarse sin exponer endpoints HTTP, sin introducir cuentas de usuarios ni tablas de turnos.

#### Scenario: Regresión del dominio
- **WHEN** se ejecutan las 55 pruebas de C-01 junto con la verificación completa
- **THEN** mantienen sus resultados y pueden ejecutarse como pruebas unitarias sin necesitar una base de datos

#### Scenario: Arranque sin servidor web
- **WHEN** se arranca el contexto de C-02 con conexión válida
- **THEN** se inicializan persistencia y migraciones sin iniciar un servidor HTTP
