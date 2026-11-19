# acceso-y-permisos-internos Specification

## Purpose

Establecer la identidad, autenticación y autorización del equipo del consultorio a nivel de servicio, con roles y permisos por operación y por registro, hashing de credenciales, política de contraseñas y bootstrap controlado, sin transporte HTTP, para proteger los recursos clínicos y administrativos de los changes posteriores.

## Requirements

### Requirement: Modelo de usuario interno con identificador estable
El sistema SHALL representar a cada miembro del equipo como un usuario interno con un identificador estable, una credencial verificable, un rol de `recepcion` o `odontologo`, y un estado activo o inactivo. El sistema SHALL NOT crear un rol de superadministrador ni otorgar permisos globales por inferencia.

#### Scenario: Creación de usuario válido
- **WHEN** se crea un usuario interno con identificador, credencial, rol válido y estado activo
- **THEN** el usuario queda registrado y su identificador se conserva estable para referencias posteriores

#### Scenario: Rol inválido rechazado
- **WHEN** se intenta crear un usuario con un rol distinto de `recepcion` u `odontologo`
- **THEN** el sistema rechaza la creación y no registra el usuario

#### Scenario: Sin superadministrador
- **WHEN** se consulta el catálogo de roles disponibles
- **THEN** solo existen `recepcion` y `odontologo`; no existe un rol de superadministrador

### Requirement: Autenticación con rechazo genérico
El sistema SHALL autenticar a un usuario interno a partir de su identificador y credencial. Las credenciales válidas SHALL producir una identidad con el rol correspondiente. Las credenciales inválidas, el usuario desconocido o el usuario inactivo SHALL producir un rechazo genérico que no revele cuál de los factores falló.

#### Scenario: Login válido de recepción
- **WHEN** un usuario activo con rol `recepcion` presenta credenciales correctas
- **THEN** el sistema devuelve una identidad con rol `recepcion`

#### Scenario: Login válido de odontólogo
- **WHEN** un usuario activo con rol `odontologo` presenta credenciales correctas
- **THEN** el sistema devuelve una identidad con rol `odontologo`

#### Scenario: Rechazo por contraseña incorrecta
- **WHEN** un usuario activo presenta una contraseña incorrecta
- **THEN** el sistema rechaza la autenticación con un error genérico

#### Scenario: Rechazo por usuario desconocido
- **WHEN** se presenta un identificador que no existe
- **THEN** el sistema rechaza la autenticación con el mismo error genérico que para contraseña incorrecta

#### Scenario: Rechazo por usuario inactivo
- **WHEN** un usuario inactivo presenta credenciales correctas
- **THEN** el sistema rechaza la autenticación con el mismo error genérico

### Requirement: Catálogo de permisos por operación
El sistema SHALL definir los siguientes permisos nombrados para operaciones del equipo: `turno:crear`, `turno:reprogramar`, `turno:cancelar`, `agenda-sillon:consultar`, `bloqueo:crear`, `ficha:leer`, `ficha:editar`, `confirmacion:registrar`, `pendientes:consultar`, `paciente:datos-admin`, `config:intervalo`, `usuario:crear/asignar` y `paciente:eliminar`. Cada recurso protegido SHALL verificar el permiso correspondiente antes de ejecutar la operación.

#### Scenario: Permiso concedido
- **WHEN** una identidad con el permiso requerido solicita la operación protegida
- **THEN** el sistema permite la ejecución de la operación

#### Scenario: Permiso denegado
- **WHEN** una identidad sin el permiso requerido solicita la operación protegida
- **THEN** el sistema rechaza la operación con un error de autorización

#### Scenario: Acceso anónimo denegado
- **WHEN** una solicitud sin identidad intenta acceder a un recurso protegido
- **THEN** el sistema rechaza el acceso

### Requirement: Separación clínica entre recepción y odontólogo
El sistema SHALL permitir que recepción lea la ficha clínica y que el odontólogo lea y edite antecedentes y notas. El sistema SHALL denegar la edición de la ficha clínica a recepción. Esta restricción SHALL verificarse en el backend, no solo en la interfaz.

#### Scenario: Recepción lee ficha
- **WHEN** una identidad con rol `recepcion` solicita la lectura de una ficha clínica
- **THEN** el sistema permite la lectura

#### Scenario: Odontólogo edita ficha
- **WHEN** una identidad con rol `odontologo` solicita la edición de antecedentes o notas
- **THEN** el sistema permite la edición

#### Scenario: Recepción no edita ficha
- **WHEN** una identidad con rol `recepcion` solicita la edición de antecedentes o notas
- **THEN** el sistema rechaza la edición

### Requirement: Alcance por registro de pacientes y turnos
El sistema SHALL otorgar a recepción lectura global de pacientes y turnos del sitio. El sistema SHALL otorgar al odontólogo acceso solo a los pacientes y turnos que le correspondan según la vinculación profesional definida por contrato. El sistema SHALL NOT asumir acceso clínico global para ningún rol.

#### Scenario: Recepción consulta cualquier paciente
- **WHEN** una identidad con rol `recepcion` consulta un paciente del sitio
- **THEN** el sistema permite la consulta

#### Scenario: Odontólogo consulta su paciente
- **WHEN** una identidad con rol `odontologo` consulta un paciente vinculado a su profesional
- **THEN** el sistema permite la consulta

#### Scenario: Odontólogo fuera de alcance
- **WHEN** una identidad con rol `odontologo` consulta un paciente no vinculado a su profesional
- **THEN** el sistema rechaza el acceso

### Requirement: Credenciales protegidas con hash y política mínima
El sistema SHALL almacenar la credencial como un hash (bcrypt con costo 12 o argon2) con sal por usuario, nunca en texto plano. El sistema SHALL aplicar una política mínima de contraseña: longitud de al menos 12 caracteres, presencia de mayúscula, minúscula y número, ausencia de datos personales, y no reutilización de las últimas N contraseñas. El sistema SHALL NOT registrar credenciales en logs ni versionar secretos.

#### Scenario: Hash distinto del texto plano
- **WHEN** se crea o actualiza la credencial de un usuario
- **THEN** el valor persistido es un hash verificable y no coincide con el texto plano de la contraseña

#### Scenario: Verificación de algoritmo y costo
- **WHEN** se verifica una credencial válida contra su hash
- **THEN** la verificación usa bcrypt con costo 12 (o argon2) y sal por usuario

#### Scenario: Contraseña corta rechazada
- **WHEN** se propone una contraseña de menos de 12 caracteres
- **THEN** el sistema rechaza la credencial

#### Scenario: Contraseña sin complejidad rechazada
- **WHEN** se propone una contraseña que carece de mayúscula, minúscula o número
- **THEN** el sistema rechaza la credencial

#### Scenario: Reutilización rechazada
- **WHEN** se propone una contraseña igual a una de las últimas N usadas por el usuario
- **THEN** el sistema rechaza la credencial

#### Scenario: Sin credenciales en logs
- **WHEN** se ejecutan operaciones de autenticación o gestión de credenciales
- **THEN** los logs no contienen credenciales en texto plano ni hashes reutilizables

### Requirement: Bootstrap controlado de primer usuario
El sistema SHALL permitir la creación del primer usuario con rol mediante un proceso de bootstrap controlado y documentado, ejecutable una sola vez por un técnico. Una segunda creación de usuario administrativo sin permiso SHALL ser rechazada. El bootstrap SHALL NOT requerir ni versionar secretos.

#### Scenario: Bootstrap inicial exitoso
- **WHEN** un técnico ejecuta el bootstrap con datos válidos para crear el primer usuario
- **THEN** el sistema crea el usuario con el rol indicado

#### Scenario: Segundo bootstrap denegado
- **WHEN** se intenta ejecutar el bootstrap cuando ya existe un usuario
- **THEN** el sistema rechaza la operación

#### Scenario: Creación sin permiso denegada
- **WHEN** un usuario sin el permiso `usuario:crear/asignar` intenta crear otro usuario
- **THEN** el sistema rechaza la creación

### Requirement: Contrato de vinculación usuario-profesional
El sistema SHALL definir el contrato para vincular un usuario interno con una referencia opaca de profesional, incluyendo el permiso `usuario:crear/asignar` como requisito para asignarla. La implementación efectiva de la vinculación SHALL realizarse en C-05; C-03 solo fija el contrato.

#### Scenario: Contrato documentado
- **WHEN** se consulta el contrato de vinculación usuario-profesional
- **THEN** se describe la referencia opaca al profesional y el permiso requerido para asignarla

#### Scenario: Sin implementación en C-03
- **WHEN** se ejecuta el alcance de C-03
- **THEN** no se implementa la vinculación efectiva; solo se deja el contrato para C-05

### Requirement: Propagación reutilizable de identidad y permisos
El sistema SHALL exponer un contrato reutilizable para propagar la identidad autenticada y sus permisos a los changes C-04, C-05 y C-06, a nivel de servicio, sin transporte HTTP ni endpoints.

#### Scenario: Consumo por C-04
- **WHEN** C-04 necesita verificar permisos sobre ficha clínica
- **THEN** consume el contrato de identidad/permisos definido en C-03

#### Scenario: Consumo por C-05
- **WHEN** C-05 necesita verificar permisos sobre recursos y configuración
- **THEN** consume el contrato de identidad/permisos definido en C-03

#### Scenario: Consumo por C-06
- **WHEN** C-06 necesita verificar permisos sobre creación de turnos
- **THEN** consume el contrato de identidad/permisos definido en C-03

### Requirement: Sin transporte HTTP en este change
El sistema SHALL NOT introducir endpoints HTTP, cookies, headers de transporte ni mecanismos de sesión en C-03. La autenticación y autorización se resuelven a nivel de servicio. Si se introduce transporte, SHALL estar justificado y aprobado.

#### Scenario: Ausencia de endpoints
- **WHEN** se revisa el alcance de C-03
- **THEN** no existen endpoints HTTP ni transporte de credenciales

#### Scenario: Transporte solo con aprobación
- **WHEN** se propone introducir transporte HTTP
- **THEN** requiere justificación y aprobación explícita documentada