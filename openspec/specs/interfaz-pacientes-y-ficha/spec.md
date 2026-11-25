# interfaz-pacientes-y-ficha Specification

## Purpose

Permitir a recepción y odontología operar pacientes y la ficha clínica desde una interfaz web vanilla, con autenticación por token opaco, capa HTTP compartida y gating por rol, sin introducir frameworks de UI.

## Requirements

### Requirement: Inicio de sesión y sesión autenticada en la interfaz
El sistema SHALL permitir iniciar sesión con credenciales válidas y conservar un token opaco para autenticar las operaciones posteriores. Las credenciales inválidas SHALL mostrar un rechazo genérico. El cierre de sesión SHALL descartar el token.

#### Scenario: Login válido
- **WHEN** un usuario ingresa credenciales válidas
- **THEN** el sistema obtiene un token opaco y habilita las operaciones autorizadas

#### Scenario: Login inválido
- **WHEN** un usuario ingresa credenciales inválidas
- **THEN** el sistema muestra un rechazo genérico y no conserva token

#### Scenario: Cierre de sesión
- **WHEN** el usuario cierra sesión
- **THEN** el sistema descarta el token y vuelve a la pantalla de login

### Requirement: Listado y búsqueda de pacientes en la interfaz
El sistema SHALL mostrar pacientes con datos administrativos básicos según el alcance del rol, permitiendo búsqueda por código exacto y coincidencia parcial de nombre o apellido. Los resultados SHALL excluir contenido clínico.

#### Scenario: Recepción lista pacientes del sitio
- **WHEN** recepción abre el listado
- **THEN** el sistema muestra pacientes del sitio con datos administrativos básicos

#### Scenario: Odontología lista pacientes asignados
- **WHEN** un odontólogo abre el listado
- **THEN** el sistema muestra solo pacientes asignados a su profesional

#### Scenario: Búsqueda parcial
- **WHEN** el usuario busca por texto parcial de nombre o apellido
- **THEN** el sistema muestra coincidencias dentro del alcance del rol

### Requirement: Alta de paciente desde la interfaz
El sistema SHALL permitir crear un paciente con código, nombre, apellido y email obligatorios y teléfono opcional. La creación por odontología SHALL dejar al paciente sin asignación.

#### Scenario: Alta válida por recepción
- **WHEN** recepción crea un paciente con datos válidos
- **THEN** el sistema conserva el paciente y lo muestra en el listado

#### Scenario: Alta por odontología sin asignación
- **WHEN** un odontólogo crea un paciente nuevo
- **THEN** el paciente queda sin profesionales asignados

#### Scenario: Datos inválidos
- **WHEN** el usuario intenta crear un paciente con datos obligatorios inválidos
- **THEN** el sistema muestra el error y no crea el paciente

### Requirement: Edición de datos administrativos desde la interfaz
El sistema SHALL permitir editar nombre, apellido, email y teléfono de un paciente dentro del alcance del rol. El código no SHALL ser editable.

#### Scenario: Edición válida
- **WHEN** un usuario autorizado edita datos administrativos válidos
- **THEN** el sistema conserva los cambios y los muestra

#### Scenario: Edición fuera de alcance
- **WHEN** un odontólogo edita un paciente no asignado
- **THEN** el sistema deniega la operación y muestra el error

### Requirement: Detalle de paciente en la interfaz
El sistema SHALL mostrar el detalle administrativo de un paciente autorizado, incluyendo sus profesionales asignados.

#### Scenario: Detalle autorizado
- **WHEN** el usuario consulta el detalle de un paciente en su alcance
- **THEN** el sistema muestra los datos administrativos y las asignaciones

#### Scenario: Detalle no autorizado
- **WHEN** un odontólogo consulta el detalle de un paciente no asignado
- **THEN** el sistema deniega la operación

### Requirement: Asignación de profesionales desde la interfaz
El sistema SHALL permitir a recepción asignar o quitar profesionales a un paciente. La interfaz SHALL ocultar esta operación a odontología.

#### Scenario: Recepción asigna profesional
- **WHEN** recepción asigna un profesional existente
- **THEN** el sistema conserva la asignación y actualiza el alcance

#### Scenario: Recepción quita asignación
- **WHEN** recepción quita una asignación
- **THEN** el profesional pierde el acceso al paciente

#### Scenario: Odontología no gestiona asignaciones
- **WHEN** un odontólogo intenta cambiar asignaciones
- **THEN** la interfaz no ofrece la operación y el backend la deniega

### Requirement: Visualización de ficha clínica en la interfaz
El sistema SHALL mostrar antecedentes y notas de la ficha de un paciente autorizado. Recepción SHALL tener acceso de solo lectura.

#### Scenario: Recepción visualiza ficha
- **WHEN** recepción consulta la ficha de un paciente
- **THEN** el sistema muestra antecedentes y notas sin opción de edición

#### Scenario: Odontología asignada visualiza ficha
- **WHEN** un odontólogo asignado consulta la ficha
- **THEN** el sistema muestra antecedentes y notas con opciones de edición

#### Scenario: Odontología no asignada
- **WHEN** un odontólogo no asignado consulta la ficha
- **THEN** el sistema deniega la operación

### Requirement: Edición de antecedentes desde la interfaz
El sistema SHALL permitir a un odontólogo asignado guardar o actualizar los antecedentes de un paciente. La ficha SHALL crearse al guardar por primera vez.

#### Scenario: Guardar antecedentes por primera vez
- **WHEN** un odontólogo asignado guarda antecedentes para un paciente sin ficha
- **THEN** el sistema crea la ficha y conserva los antecedentes

#### Scenario: Actualizar antecedentes
- **WHEN** un odontólogo asignado actualiza antecedentes existentes
- **THEN** el sistema conserva el texto actualizado

#### Scenario: Recepción no edita antecedentes
- **WHEN** recepción intenta editar antecedentes
- **THEN** la interfaz no ofrece la operación y el backend la deniega

### Requirement: Gestión de notas clínicas desde la interfaz
El sistema SHALL permitir a un odontólogo asignado agregar notas con contenido no vacío y editar únicamente sus propias notas mientras permanezca asignado. Cada nota SHALL mostrar autor y fecha.

#### Scenario: Agregar nota
- **WHEN** un odontólogo asignado registra una nota con contenido válido
- **THEN** el sistema guarda la nota con autor y fecha y la muestra

#### Scenario: Editar nota propia
- **WHEN** el autor asignado edita su nota
- **THEN** el sistema guarda el contenido actualizado y conserva autor y fecha originales

#### Scenario: Editar nota ajena
- **WHEN** otro odontólogo asignado intenta editar una nota ajena
- **THEN** el sistema deniega la edición y conserva el contenido

#### Scenario: Nota vacía
- **WHEN** el usuario intenta guardar una nota vacía
- **THEN** el sistema rechaza la operación

### Requirement: Ausencia de eliminación en la interfaz de pacientes y ficha
El sistema SHALL NOT ofrecer eliminación de pacientes, fichas, antecedentes ni notas en la interfaz.

#### Scenario: Sin opción de eliminar
- **WHEN** el usuario opera pacientes o ficha
- **THEN** la interfaz no ofrece ninguna acción de eliminación

### Requirement: Gating por rol en la interfaz
El sistema SHALL aplicar el gating por rol en la interfaz para ocultar operaciones no autorizadas, sin sustituir la validación del backend.

#### Scenario: Recepción sin edición clínica
- **WHEN** recepción visualiza la ficha
- **THEN** la interfaz oculta las acciones de edición clínica

#### Scenario: Odontología sin gestión de asignaciones
- **WHEN** un odontólogo visualiza el detalle
- **THEN** la interfaz oculta la gestión de asignaciones

#### Scenario: Backend sigue validando
- **WHEN** una operación no autorizada se invoca por fuera de la interfaz
- **THEN** el backend la deniega igualmente