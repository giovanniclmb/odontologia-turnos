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
El sistema SHALL definir los siguientes permisos nombrados para operaciones del equipo: `turno:crear`, `turno:reprogramar`, `turno:cancelar`, `agenda-sillon:consultar`, `bloqueo:crear`, `bloqueo:consultar`, `ficha:leer`, `ficha:editar`, `confirmacion:registrar`, `pendientes:consultar`, `paciente:datos-admin`, `config:intervalo`, `config:recursos`, `usuario:crear/asignar` y `paciente:eliminar`. Cada recurso protegido SHALL verificar el permiso correspondiente antes de ejecutar la operación. El permiso `paciente:datos-admin` SHALL estar concedido a recepción y odontología, sujeto al alcance por registro definido para cada rol; `paciente:eliminar` SHALL permanecer denegado. Los permisos `pendientes:consultar` y `confirmacion:registrar` SHALL concederse exclusivamente a recepción. El permiso `bloqueo:consultar` SHALL concederse a recepción y odontología, con el alcance por rol de la consulta de bloqueos.

#### Scenario: Permiso concedido
- **WHEN** una identidad con el permiso requerido solicita la operación protegida
- **THEN** el sistema permite la ejecución de la operación

#### Scenario: Permiso denegado
- **WHEN** una identidad sin el permiso requerido solicita la operación protegida
- **THEN** el sistema rechaza la operación con un error de autorización

#### Scenario: Acceso anónimo denegado
- **WHEN** una solicitud sin identidad intenta acceder a un recurso protegido
- **THEN** el sistema rechaza el acceso

#### Scenario: Escrituras habilitadas para recepción
- **WHEN** una identidad de recepción ejecuta una escritura válida del intervalo con `config:intervalo`, del catálogo o recursos con `config:recursos`, o crea o vincula un usuario con `usuario:crear/asignar`
- **THEN** el servicio permite la operación con las validaciones vigentes

#### Scenario: Consulta y escritura según rol
- **WHEN** una identidad de odontología consulta la configuración o los recursos, o intenta ejecutar alguna de las escrituras administrativas de C-05
- **THEN** se permite la consulta y se deniegan la escritura, la creación de usuarios y la vinculación

#### Scenario: Gestión administrativa del paciente permitida
- **WHEN** recepción o un odontólogo solicita crear o editar datos administrativos de un paciente dentro del alcance por registro de su rol
- **THEN** el servicio permite la operación con `paciente:datos-admin`

#### Scenario: Gestión administrativa del paciente fuera de alcance
- **WHEN** un odontólogo solicita consultar o editar datos administrativos de un paciente no asignado a su profesional
- **THEN** el sistema deniega la operación en el backend

#### Scenario: Eliminación del paciente denegada
- **WHEN** una identidad de recepción u odontología solicita eliminar un paciente
- **THEN** el sistema deniega la operación

#### Scenario: Permisos de seguimiento manual
- **WHEN** se evalúan los permisos de listado de pendientes y registro manual de confirmación
- **THEN** recepción tiene ambos permisos y odontología carece de ambos

#### Scenario: Permisos de bloqueos
- **WHEN** se evalúan los permisos de alta y consulta de bloqueos
- **THEN** recepción tiene ambos y odontología solo `bloqueo:consultar`

### Requirement: Separación clínica entre recepción y odontólogo
El sistema SHALL permitir que recepción lea la ficha clínica de cualquier paciente del sitio y que odontología lea y edite antecedentes de pacientes asignados a su profesional. Solo el odontólogo autor SHALL poder editar una nota clínica, y SHALL estar asignado al paciente al momento de la edición. El sistema SHALL denegar a recepción toda edición clínica. Estas restricciones SHALL verificarse en el backend, no solo en la interfaz.

#### Scenario: Recepción lee ficha
- **WHEN** una identidad con rol `recepcion` solicita la lectura de una ficha clínica del sitio
- **THEN** el sistema permite la lectura

#### Scenario: Odontólogo edita ficha
- **WHEN** una identidad con rol `odontologo` solicita leer o editar antecedentes de un paciente asignado a su profesional
- **THEN** el sistema permite la operación

#### Scenario: Autor asignado edita nota
- **WHEN** un odontólogo asignado solicita editar una nota clínica de la que es autor
- **THEN** el sistema permite la edición y conserva el autor y fecha de creación originales

#### Scenario: Otro odontólogo asignado no edita nota ajena
- **WHEN** un odontólogo asignado solicita editar una nota clínica cuyo autor es otro usuario
- **THEN** el sistema rechaza la edición

#### Scenario: Odontólogo no asignado no lee ni edita
- **WHEN** una identidad con rol `odontologo` solicita leer o editar la ficha de un paciente no asignado a su profesional
- **THEN** el sistema rechaza la operación

#### Scenario: Recepción no edita ficha
- **WHEN** una identidad con rol `recepcion` solicita crear o editar antecedentes o notas
- **THEN** el sistema rechaza la edición

### Requirement: Alcance por registro de pacientes y turnos
El sistema SHALL otorgar a recepción lectura global de pacientes y turnos del sitio. El sistema SHALL otorgar a odontología acceso a datos administrativos y clínicos solo de pacientes asignados explícitamente a su profesional, y acceso a turnos solo cuando le correspondan según la vinculación profesional definida por contrato. Como única excepción, la agenda SHALL mostrar a odontología nombre y apellido del paciente de sus propios turnos aunque no tenga asignación activa. La asignación de pacientes SHALL ser administrada por recepción. El sistema SHALL NOT asumir acceso clínico global para ningún rol.

#### Scenario: Recepción consulta cualquier paciente
- **WHEN** una identidad con rol `recepcion` consulta un paciente del sitio
- **THEN** el sistema permite la consulta

#### Scenario: Odontólogo consulta su paciente
- **WHEN** una identidad con rol `odontologo` consulta un paciente asignado a su profesional
- **THEN** el sistema permite la consulta

#### Scenario: Odontólogo fuera de alcance
- **WHEN** una identidad con rol `odontologo` consulta un paciente no asignado a su profesional
- **THEN** el sistema rechaza el acceso

#### Scenario: Odontología no administra asignaciones
- **WHEN** una identidad con rol `odontologo` intenta asignar o quitar un profesional de un paciente
- **THEN** el sistema rechaza la operación

#### Scenario: Nombre en un turno propio sin asignación activa
- **WHEN** un odontólogo consulta en la agenda un turno de su profesional cuyo paciente ya no está asignado
- **THEN** obtiene nombre y apellido del paciente, y el acceso a sus demás datos administrativos y clínicos sigue denegado

### Requirement: Credenciales protegidas con hash y política mínima
El sistema SHALL almacenar la credencial como un hash (bcrypt con costo 12 o argon2) con sal por usuario, nunca en texto plano. El sistema SHALL aplicar una política mínima de contraseña: longitud de al menos 12 caracteres, presencia de mayúscula, minúscula y número, ausencia de datos personales, y no reutilización de la vigente y las cuatro anteriores (cinco en total). El sistema SHALL NOT registrar credenciales en logs ni versionar secretos.

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
- **WHEN** se propone una contraseña igual a la vigente o una de las cuatro anteriores usadas por el usuario
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
El sistema SHALL implementar la asignación de una referencia opaca a un profesional existente para un usuario interno. La operación SHALL exigir `usuario:crear/asignar` y persistir el vínculo con integridad referencial. La referencia SHALL conservar su carácter opcional para usuarios sin vínculo.

#### Scenario: Contrato documentado
- **WHEN** se consulta el contrato de vinculación usuario-profesional
- **THEN** se describe la referencia opaca al profesional y el permiso requerido para asignarla

#### Scenario: Sin implementación en C-03
- **WHEN** se revisa el alcance histórico de C-03
- **THEN** se identifica que C-03 definió el contrato y C-05 incorpora la vinculación efectiva

#### Scenario: Vinculación efectiva
- **WHEN** recepción vincula un usuario existente con un profesional existente
- **THEN** se persiste la referencia y la identidad obtenida en una nueva autenticación refleja el vínculo

#### Scenario: Referencia inexistente
- **WHEN** se intenta vincular un usuario inexistente o se indica un profesional inexistente
- **THEN** se rechaza la operación sin cambiar vínculos previamente registrados

#### Scenario: Usuario sin profesional
- **WHEN** se crea un usuario válido sin vínculo con un profesional
- **THEN** se conserva la posibilidad de autenticar al usuario con la referencia ausente

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

### Requirement: Cambio propio transaccional de contraseña
Un usuario interno activo SHALL cambiar únicamente su propia contraseña presentando la vigente. El servicio SHALL volver a verificar su estado y actualizar credencial e historial atómicamente. El historial SHALL sobrevivir al reinicio. Este contrato SHALL NOT habilitar recuperación, cambios ajenos ni transporte HTTP.

#### Scenario: Cambio propio y conservación del historial
- **WHEN** un usuario activo confirma su contraseña actual y presenta una nueva que cumple la política
- **THEN** se actualiza su credencial y se conserva la anterior como hash en el historial, incluso tras reiniciar

#### Scenario: Rechazo sin cambios parciales
- **WHEN** la contraseña actual es incorrecta, el usuario está inactivo, la nueva viola la política o falla la persistencia
- **THEN** se rechaza la operación sin modificar la credencial ni agregar un historial confirmado

#### Scenario: Solicitudes concurrentes
- **WHEN** dos solicitudes intentan cambiar la misma cuenta con la misma contraseña actual
- **THEN** solo una prospera y la otra vuelve a verificar la credencial vigente antes de rechazarla

### Requirement: Autenticación por token opaco
El sistema SHALL emitir un token opaco al autenticar credenciales válidas y resolver la identidad a partir del token enviado en el encabezado Authorization. El sistema SHALL NOT usar cookies ni sesiones de transporte.

#### Scenario: Login exitoso emite token
- **WHEN** un usuario envía credenciales válidas al endpoint de login
- **THEN** el sistema devuelve un token opaco asociado a la identidad

#### Scenario: Token inválido o ausente
- **WHEN** una solicitud protegida no incluye un token válido
- **THEN** el sistema rechaza la solicitud sin revelar detalles

#### Scenario: Cierre de sesión revoca token
- **WHEN** el usuario cierra sesión
- **THEN** el sistema revoca el token y las solicitudes posteriores con ese token son rechazadas

### Requirement: Transporte HTTP justificado para autenticación y operaciones
El sistema SHALL permitir transporte HTTP para autenticación y operaciones de C-11, manteniendo la autorización y el alcance a nivel de servicio.

#### Scenario: Endpoints protegidos por permiso
- **WHEN** se invoca un endpoint protegido
- **THEN** el sistema verifica el permiso correspondiente antes de ejecutar la operación

#### Scenario: Autorización se mantiene en backend
- **WHEN** el cliente intenta omitir el gating de la interfaz
- **THEN** el backend aplica las mismas reglas y deniega lo no autorizado
