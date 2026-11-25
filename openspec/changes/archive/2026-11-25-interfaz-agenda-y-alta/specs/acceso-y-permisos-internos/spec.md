# Spec Delta

## MODIFIED Requirements

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
