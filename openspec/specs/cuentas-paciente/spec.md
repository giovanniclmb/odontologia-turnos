# Cuentas de paciente Specification

## Purpose

Permite que un paciente cree una cuenta, ingrese mientras espera la confirmación de su vínculo y, una vez vinculado manualmente por recepción con su ficha, gestione únicamente sus propias inscripciones en lista de espera.

## Requirements

### Requirement: Registro de cuenta de paciente pendiente
El sistema SHALL permitir que una persona registre una cuenta de paciente identificada por un email único entre cuentas, en estado pendiente de vinculación, con una contraseña validada según la política vigente y almacenada como hash. El sistema SHALL NOT usar el email para asociar automáticamente la cuenta con una ficha.

#### Scenario: Registro pendiente aceptado
- **WHEN** una persona completa un registro de cuenta válido
- **THEN** el sistema crea una cuenta pendiente de vinculación y no la asocia automáticamente con una ficha por coincidencia de email

#### Scenario: Email de cuenta ya registrado
- **WHEN** una persona intenta registrar una cuenta con un email ya usado por otra cuenta
- **THEN** el sistema rechaza el registro y no crea una segunda cuenta con el mismo identificador

### Requirement: Ingreso de cuenta pendiente
El sistema SHALL autenticar las cuentas de paciente por email y contraseña. Las credenciales inválidas o no reconocidas SHALL producir un rechazo genérico. Una cuenta pendiente autenticada SHALL identificarse como pendiente, sin habilitar operaciones de lista de espera.

#### Scenario: Ingreso válido pendiente
- **WHEN** una cuenta pendiente presenta credenciales válidas
- **THEN** el sistema autentica la cuenta y devuelve una identidad sin vínculo de paciente ni permisos de inscripción

#### Scenario: Credenciales inválidas
- **WHEN** una cuenta de paciente presenta credenciales inválidas o no reconocidas
- **THEN** el sistema rechaza el ingreso con un error genérico sin conceder una identidad autenticada ni revelar qué dato falló

### Requirement: Confirmación manual del vínculo por recepción
El sistema SHALL permitir que recepción confirme explícitamente el vínculo entre una cuenta registrada y una ficha existente. Una ficha SHALL estar vinculada a no más de una cuenta. El email coincidente SHALL NOT sustituir esta confirmación.

#### Scenario: Recepción vincula cuenta con ficha
- **WHEN** una identidad de recepción confirma el vínculo de una cuenta pendiente con una ficha de paciente existente
- **THEN** la cuenta queda vinculada a esa ficha y su identidad autenticada incluye la referencia del paciente

#### Scenario: Paciente ya vinculado a otra cuenta
- **WHEN** recepción intenta vincular una segunda cuenta con una ficha que ya tiene una cuenta vinculada
- **THEN** el sistema rechaza la operación y conserva el vínculo existente

#### Scenario: Paciente no confirma su propio vínculo
- **WHEN** una cuenta de paciente intenta confirmar o cambiar su propio vínculo con una ficha
- **THEN** el sistema deniega la operación y conserva el vínculo confirmado vigente, si existe

### Requirement: Operaciones de lista habilitadas solo tras vinculación
El sistema SHALL permitir operaciones de lista de espera a una cuenta autenticada únicamente cuando esté vinculada con una ficha de paciente.

#### Scenario: Cuenta vinculada gestiona inscripción propia
- **WHEN** una cuenta autenticada y vinculada solicita una operación de lista sobre una inscripción propia
- **THEN** el sistema permite la operación si cumple el contrato de inscripción vigente

#### Scenario: Cuenta pendiente intenta operar la lista
- **WHEN** una cuenta autenticada sin vínculo confirmado solicita una operación de lista de espera
- **THEN** el sistema deniega la operación sin exponer datos de inscripciones

### Requirement: Alcance propio de las inscripciones
El sistema SHALL autorizar a una cuenta de paciente a consultar o gestionar únicamente inscripciones asociadas al paciente con el que está vinculada.

#### Scenario: Acceso a inscripción propia
- **WHEN** una cuenta vinculada solicita consultar o gestionar una inscripción asociada a su paciente
- **THEN** el sistema permite la operación según el contrato de inscripción vigente

#### Scenario: Acceso a inscripción ajena
- **WHEN** una cuenta vinculada solicita consultar o gestionar una inscripción asociada a otro paciente
- **THEN** el sistema deniega la operación y no revela sus datos

### Requirement: Sin permisos clínicos ni de reserva
La identidad de paciente SHALL NOT conceder acceso a datos administrativos o clínicos de pacientes, operaciones internas de agenda ni creación o asignación de turnos.

#### Scenario: Cuenta intenta operación fuera de alcance
- **WHEN** una cuenta de paciente solicita leer una ficha clínica, operar la agenda o reservar/asignar un turno
- **THEN** el sistema deniega la operación
