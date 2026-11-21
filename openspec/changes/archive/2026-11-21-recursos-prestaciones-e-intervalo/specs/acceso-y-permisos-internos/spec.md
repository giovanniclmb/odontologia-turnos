# Spec Delta

## MODIFIED Requirements

### Requirement: Catálogo de permisos por operación
El sistema SHALL definir los siguientes permisos nombrados para operaciones del equipo: `turno:crear`, `turno:reprogramar`, `turno:cancelar`, `agenda-sillon:consultar`, `bloqueo:crear`, `ficha:leer`, `ficha:editar`, `confirmacion:registrar`, `pendientes:consultar`, `paciente:datos-admin`, `config:intervalo`, `config:recursos`, `usuario:crear/asignar` y `paciente:eliminar`. Cada recurso protegido SHALL verificar el permiso correspondiente antes de ejecutar la operación.

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
