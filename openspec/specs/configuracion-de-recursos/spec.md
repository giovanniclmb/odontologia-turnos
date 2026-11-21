# configuracion-de-recursos Specification

## Purpose

Proveer a la agenda de una única sede los recursos, las duraciones de referencia y el intervalo común que necesita para validar nuevas asignaciones.

## Requirements

### Requirement: Intervalo único de la sede
El sistema SHALL permitir consultar y establecer un único intervalo persistido para la sede en minutos enteros positivos, sin valor inicial ni límites de negocio adicionales. El nuevo valor SHALL regir desde su confirmación para las validaciones futuras.

#### Scenario: Sede sin configurar
- **WHEN** se consulta el intervalo en una sede que aún no lo configuró
- **THEN** se obtiene el estado sin configurar y la validación de disponibilidad mínima indica que falta el intervalo

#### Scenario: Configuración y modificación del intervalo
- **WHEN** recepción establece un valor válido y luego lo sustituye por otro valor válido
- **THEN** las consultas y validaciones posteriores usan el último valor confirmado y los turnos anteriores conservan sus datos

#### Scenario: Valores inválidos
- **WHEN** recepción intenta establecer un valor nulo, cero, negativo o fraccionario
- **THEN** la operación se rechaza y se conserva la configuración previa

### Requirement: Alta y consulta de profesionales y sillones
El sistema SHALL permitir dar de alta y consultar profesionales y sillones persistidos con referencia estable, código y estado operativo. Los recursos SHALL incorporarse por operación, sin precargar cantidades fijas.

#### Scenario: Alta y consulta de recursos
- **WHEN** recepción da de alta un profesional y un sillón con códigos válidos
- **THEN** ambos pueden consultarse por sus referencias incluso tras reiniciar

#### Scenario: Código de recurso duplicado
- **WHEN** se intenta dar de alta un recurso con un código existente en su catálogo
- **THEN** el alta se rechaza y el registro previo se conserva

### Requirement: Disponibilidad mínima de recursos
La validación de disponibilidad mínima SHALL requerir un profesional existente y activo, un sillón existente y activo y un intervalo de sede configurado. Este criterio cubre la precondición de RN-AG-02 y RN-AG-04.

#### Scenario: Precondiciones satisfechas
- **WHEN** existen ambos recursos activos y hay un intervalo configurado
- **THEN** la validación mínima acepta los recursos y proporciona el intervalo vigente

#### Scenario: Recurso ausente o inactivo
- **WHEN** se omite un recurso obligatorio o cualquiera de las referencias corresponde a un recurso inexistente o inactivo
- **THEN** la validación mínima rechaza la asignación

### Requirement: Catálogo de prestaciones y duración de referencia
El sistema SHALL permitir dar de alta y consultar prestaciones y ajustar su duración de referencia en minutos enteros positivos. El catálogo SHALL iniciar vacío. El ajuste SHALL afectar las referencias consultadas posteriormente y conservar las duraciones ya registradas en turnos.

#### Scenario: Catálogo inicial y alta
- **WHEN** el catálogo se consulta antes de la primera alta y luego recepción registra una prestación válida
- **THEN** la primera consulta se encuentra vacía y la siguiente devuelve la prestación con su duración de referencia

#### Scenario: Ajuste válido
- **WHEN** recepción modifica la duración de referencia de una prestación existente
- **THEN** el valor actualizado queda disponible para futuras altas de turnos y se conserva la duración elegida de turnos anteriores

#### Scenario: Duración inválida
- **WHEN** recepción propone una duración nula, cero, negativa o fraccionaria
- **THEN** el alta o el ajuste se rechaza sin persistir el valor inválido
