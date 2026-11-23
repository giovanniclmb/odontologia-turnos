# Spec Delta

## Purpose

Gestionar los datos administrativos mínimos de cada paciente y proteger el acceso a su ficha clínica sencilla, compuesta por antecedentes y notas, según el rol y la asignación profesional vigente.

## ADDED Requirements

### Requirement: Datos administrativos mínimos del paciente
El sistema SHALL conservar el UUID y `codigo` existentes del paciente, junto con nombre, apellido y email obligatorios, y teléfono opcional. El email SHALL tener formato válido y SHALL NOT ser único. Nombre, apellido y email SHALL contener texto no vacío. La unicidad y validación exacta del `codigo` SHALL conservar las reglas de referencias existentes.

#### Scenario: Alta válida de paciente
- **WHEN** recepción u odontología crea un paciente con código, nombre, apellido y email válidos, y opcionalmente un teléfono
- **THEN** el sistema conserva esos datos con el UUID estable del paciente

#### Scenario: Datos obligatorios inválidos
- **WHEN** se intenta crear o editar un paciente con nombre, apellido o email vacío, o con email de formato inválido
- **THEN** el sistema rechaza la operación sin cambiar los datos confirmados

#### Scenario: Email compartido
- **WHEN** dos pacientes válidos utilizan el mismo email
- **THEN** el sistema permite guardar ambos pacientes

#### Scenario: Código duplicado
- **WHEN** se intenta crear un paciente con un `codigo` ya utilizado por otro paciente
- **THEN** el sistema rechaza el alta y conserva el paciente existente

### Requirement: Gestión administrativa de pacientes por rol
El sistema SHALL permitir a recepción y odontología crear, consultar y editar datos administrativos de pacientes con `paciente:datos-admin`. Recepción SHALL tener alcance sobre todos los pacientes del sitio; odontología SHALL tener alcance únicamente sobre pacientes asignados a su profesional. La creación por odontología SHALL dejar al paciente sin asignación hasta que recepción lo asigne. El sistema SHALL NOT ofrecer eliminación de pacientes.

#### Scenario: Recepción gestiona cualquier paciente
- **WHEN** recepción crea, consulta o edita datos administrativos de un paciente del sitio
- **THEN** el sistema permite la operación

#### Scenario: Odontología gestiona paciente asignado
- **WHEN** un odontólogo crea, consulta o edita datos administrativos de un paciente dentro de su alcance asignado
- **THEN** el sistema permite la operación

#### Scenario: Odontología crea paciente sin asignación
- **WHEN** un odontólogo crea un paciente nuevo
- **THEN** el paciente queda sin profesionales asignados y el odontólogo no obtiene acceso posterior a sus datos hasta que recepción lo asigne

#### Scenario: Odontología accede a paciente no asignado
- **WHEN** un odontólogo consulta o edita datos administrativos de un paciente no asignado a su profesional
- **THEN** el sistema deniega la operación en el backend

#### Scenario: Operación administrativa sin permiso
- **WHEN** una identidad sin `paciente:datos-admin` intenta crear o editar datos administrativos
- **THEN** el sistema deniega la operación en el backend

#### Scenario: Eliminación no habilitada
- **WHEN** una identidad intenta eliminar un paciente
- **THEN** el sistema deniega la operación y conserva el paciente

### Requirement: Búsqueda básica con alcance por rol
El sistema SHALL permitir listar y buscar pacientes por código, nombre o apellido. Los resultados SHALL incluir datos administrativos básicos sin contenido clínico y SHALL respetar el alcance por registro: global para recepción y limitado a pacientes asignados para odontología.

#### Scenario: Recepción busca pacientes del sitio
- **WHEN** recepción lista o busca pacientes por código, nombre o apellido
- **THEN** obtiene coincidencias del sitio con datos administrativos básicos y sin antecedentes ni notas

#### Scenario: Odontología busca pacientes asignados
- **WHEN** un odontólogo lista o busca pacientes por código, nombre o apellido
- **THEN** obtiene solo coincidencias asignadas a su profesional y sin antecedentes ni notas

#### Scenario: Búsqueda fuera de alcance
- **WHEN** un odontólogo busca un paciente que no está asignado a su profesional
- **THEN** el paciente no aparece en los resultados

### Requirement: Asignación explícita de pacientes a profesionales
El sistema SHALL permitir que recepción asigne a un paciente cero o más profesionales existentes y que modifique o quite esas asignaciones. Las asignaciones SHALL determinar el alcance de odontología sobre los datos administrativos y clínicos del paciente. Odontología SHALL NOT administrar asignaciones.

#### Scenario: Recepción asigna profesionales
- **WHEN** recepción asigna uno o más profesionales existentes a un paciente
- **THEN** el sistema conserva las asignaciones y cada profesional asignado obtiene el alcance correspondiente

#### Scenario: Recepción quita una asignación
- **WHEN** recepción quita la asignación de un profesional a un paciente
- **THEN** ese profesional pierde el acceso al paciente y los demás profesionales asignados conservan su alcance

#### Scenario: Paciente sin profesionales asignados
- **WHEN** un paciente todavía no tiene asignaciones profesionales
- **THEN** recepción conserva su acceso y ningún odontólogo obtiene acceso por inferencia

#### Scenario: Odontología no puede cambiar asignaciones
- **WHEN** un odontólogo intenta asignar o quitar un profesional de un paciente
- **THEN** el sistema deniega la operación en el backend

#### Scenario: Referencia profesional inexistente
- **WHEN** recepción intenta asignar un profesional inexistente
- **THEN** el sistema rechaza la operación sin cambiar las asignaciones confirmadas

### Requirement: Ficha clínica sencilla y creación diferida
La ficha SHALL contener antecedentes en un texto editable y notas clínicas independientes. El sistema SHALL crear la ficha al guardar los primeros antecedentes o la primera nota, y SHALL mantener como máximo una ficha por paciente.

#### Scenario: Primeros antecedentes
- **WHEN** se guardan antecedentes para un paciente sin ficha
- **THEN** el sistema crea una ficha asociada al paciente y conserva los antecedentes

#### Scenario: Primera nota
- **WHEN** se guarda una nota para un paciente sin ficha
- **THEN** el sistema crea una ficha asociada al paciente y conserva la nota

#### Scenario: Ficha única
- **WHEN** se guardan antecedentes o notas posteriores para un paciente que ya tiene ficha
- **THEN** el sistema actualiza la ficha existente o agrega una nota a ella sin crear otra ficha

### Requirement: Lectura clínica según rol y alcance
El sistema SHALL permitir a recepción leer la ficha clínica de cualquier paciente del sitio y a odontología leerla solo cuando su profesional esté asignado al paciente. La respuesta clínica SHALL incluir antecedentes y notas del paciente autorizado.

#### Scenario: Recepción lee ficha
- **WHEN** recepción consulta la ficha de un paciente del sitio
- **THEN** el sistema devuelve antecedentes y notas

#### Scenario: Odontología asignada lee ficha
- **WHEN** un odontólogo consulta la ficha de un paciente asignado a su profesional
- **THEN** el sistema devuelve antecedentes y notas

#### Scenario: Odontología no asignada lee ficha
- **WHEN** un odontólogo consulta la ficha de un paciente no asignado a su profesional
- **THEN** el sistema deniega la lectura en el backend

### Requirement: Edición de antecedentes y notas
El sistema SHALL permitir a odontología asignada editar los antecedentes y agregar notas con contenido no vacío. Recepción SHALL poder leer, pero no modificar, contenido clínico. Cada nota SHALL registrar su autor y fecha de creación en el backend; solo su autor SHALL poder editarla. El sistema SHALL NOT ofrecer eliminación de antecedentes, fichas ni notas.

#### Scenario: Odontología asignada edita antecedentes
- **WHEN** un odontólogo asignado guarda o actualiza los antecedentes de un paciente
- **THEN** el sistema conserva el texto actualizado en la ficha

#### Scenario: Recepción no edita antecedentes
- **WHEN** recepción intenta crear o modificar antecedentes
- **THEN** el sistema deniega la operación en el backend y conserva el contenido confirmado

#### Scenario: Odontología asignada agrega nota
- **WHEN** un odontólogo asignado registra una nota con contenido no vacío
- **THEN** el sistema guarda la nota con el usuario autor y fecha de creación asignados por el backend

#### Scenario: Autor edita su nota
- **WHEN** el odontólogo autor, aún asignado al paciente, edita el contenido de su nota
- **THEN** el sistema guarda el contenido actualizado y conserva el autor y la fecha de creación originales

#### Scenario: Otro odontólogo no edita la nota
- **WHEN** otro odontólogo asignado intenta editar una nota cuyo autor es distinto
- **THEN** el sistema deniega la edición y conserva el contenido confirmado

#### Scenario: Recepción no edita notas
- **WHEN** recepción intenta crear o editar una nota
- **THEN** el sistema deniega la operación en el backend

#### Scenario: Nota vacía o eliminación clínica
- **WHEN** una identidad intenta guardar una nota vacía o eliminar una nota, antecedentes o ficha
- **THEN** el sistema deniega la operación y conserva el contenido confirmado

### Requirement: Contratos de servicio sin transporte HTTP
El sistema SHALL exponer operaciones y DTO de servicio para gestión administrativa, búsqueda, asignaciones y ficha clínica. Este change SHALL NOT introducir rutas HTTP, controladores, cookies, headers de transporte ni mecanismos de sesión.

#### Scenario: Operaciones backend disponibles
- **WHEN** un consumidor backend invoca una operación autorizada con su DTO válido
- **THEN** el servicio devuelve el resultado contractual y aplica las validaciones y permisos definidos

#### Scenario: Sin rutas HTTP
- **WHEN** se revisa el backend tras este change
- **THEN** las operaciones se mantienen a nivel de servicio y no existen rutas HTTP nuevas
