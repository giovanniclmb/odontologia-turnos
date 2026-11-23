# Spec Delta

## MODIFIED Requirements

### Requirement: Catálogo de permisos por operación
El sistema SHALL definir los siguientes permisos nombrados para operaciones del equipo: `turno:crear`, `turno:reprogramar`, `turno:cancelar`, `agenda-sillon:consultar`, `bloqueo:crear`, `ficha:leer`, `ficha:editar`, `confirmacion:registrar`, `pendientes:consultar`, `paciente:datos-admin`, `config:intervalo`, `config:recursos`, `usuario:crear/asignar` y `paciente:eliminar`. Cada recurso protegido SHALL verificar el permiso correspondiente antes de ejecutar la operación. El permiso `paciente:datos-admin` SHALL estar concedido a recepción y odontología, sujeto al alcance por registro definido para cada rol; `paciente:eliminar` SHALL permanecer denegado.

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
El sistema SHALL otorgar a recepción lectura global de pacientes y turnos del sitio. El sistema SHALL otorgar a odontología acceso a datos administrativos y clínicos solo de pacientes asignados explícitamente a su profesional, y acceso a turnos solo cuando le correspondan según la vinculación profesional definida por contrato. La asignación de pacientes SHALL ser administrada por recepción. El sistema SHALL NOT asumir acceso clínico global para ningún rol.

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
