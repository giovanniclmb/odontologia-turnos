# transporte-de-email Specification

## Purpose

Proveer un canal real de email gratuito, configurable y auditable, compartido por los tipos de correo del sistema, con deduplicación, reintentos y registro de resultado sin interrumpir la operación de negocio.

## Requirements

### Requirement: Configuración de transporte sin secretos versionados
El sistema SHALL leer la configuración de transporte de email desde el entorno y SHALL no requerir secretos versionados en el repositorio.

#### Scenario: Configuración presente
- **WHEN** las variables de entorno requeridas para enviar están definidas
- **THEN** el transporte queda habilitado y puede intentar envíos

#### Scenario: Configuración ausente al enviar
- **WHEN** falta una variable obligatoria para enviar
- **THEN** el intento se rechaza, se registra el fallo y no se expone el secreto

### Requirement: Restricción a destinatarios de prueba
El sistema SHALL enviar correos reales únicamente a direcciones incluidas en la allowlist configurada.

#### Scenario: Destinatario permitido
- **WHEN** el destinatario está en la allowlist
- **THEN** el envío puede realizarse

#### Scenario: Destinatario no permitido
- **WHEN** el destinatario no está en la allowlist
- **THEN** el sistema rechaza el envío sin contactar al proveedor y registra el rechazo

### Requirement: Resultado de envío observable
El sistema SHALL devolver un resultado que indique éxito o fallo y, en caso de fallo, el motivo.

#### Scenario: Envío exitoso
- **WHEN** el proveedor acepta el mensaje
- **THEN** el resultado es exitoso

#### Scenario: Envío fallido
- **WHEN** el proveedor rechaza o no responde dentro del tiempo configurado
- **THEN** el resultado es fallido con el motivo registrado

### Requirement: Reintentos con backoff corto

El sistema SHALL reintentar un envío fallido hasta el número configurado de reintentos, con backoff corto, antes de marcarlo como fallido, reenviando cada intento con la misma clave de idempotencia.

#### Scenario: Recuperación en reintento
- **WHEN** el primer intento falla y un reintento posterior tiene éxito
- **THEN** el resultado final es exitoso y se registra la cantidad de intentos

#### Scenario: Agotamiento de reintentos
- **WHEN** todos los intentos fallan
- **THEN** el estado final es fallido y no se realizan más intentos

### Requirement: Deduplicación por idempotencia

El sistema SHALL garantizar la adquisición atómica de la clave de idempotencia y SHALL no enviar un correo duplicado cuando ya existe un registro exitoso para la misma clave de idempotencia (tipo, turno, destinatario).

#### Scenario: Clave ya enviada
- **WHEN** se solicita un envío cuya clave ya tiene estado exitoso
- **THEN** el sistema no contacta al proveedor y devuelve el resultado previo

#### Scenario: Clave nueva
- **WHEN** no existe registro previo para la clave
- **THEN** el sistema realiza el envío y registra el resultado

#### Scenario: Condición de carrera concurrente
- **WHEN** dos envíos concurrentes intentan adquirir la misma clave de idempotencia
- **THEN** el sistema recupera el registro existente y devuelve su estado sin contactar al proveedor

#### Scenario: Fallo de persistencia
- **WHEN** la persistencia del registro de envío falla por una causa distinta a la violación de unicidad
- **THEN** el sistema devuelve un fallo controlado sin contactar al proveedor

### Requirement: Registro auditable de envíos
El sistema SHALL registrar cada intento de envío con estado, cantidad de intentos, marcas de tiempo y error cuando corresponda.

#### Scenario: Registro tras intento
- **WHEN** finaliza un intento de envío
- **THEN** existe un registro consultable con el estado resultante

### Requirement: Transporte no interrumpe la operación de negocio
El fallo del transporte SHALL no propagarse como excepción no controlada al llamador; el llamador recibirá el resultado del envío.

#### Scenario: Fallo controlado
- **WHEN** el envío falla por cualquier causa
- **THEN** la operación del llamador continúa y recibe el resultado fallido

### Requirement: Sustituibilidad del transporte en pruebas
El sistema SHALL permitir sustituir el transporte por una implementación falsa en pruebas, manteniendo el mismo contrato de resultado.

#### Scenario: Uso de transporte falso
- **WHEN** se configura el transporte falso en un entorno de prueba
- **THEN** los envíos se registran con el mismo contrato de resultado sin contactar al proveedor

### Requirement: Motivo de fallo sin contenido del mensaje
El motivo de fallo que el sistema registra o devuelve SHALL limitarse a la categoría del error y al estado del proveedor. SHALL NOT incluir la respuesta del proveedor, el cuerpo ni enlaces del mensaje.

#### Scenario: Rechazo del proveedor con detalle
- **WHEN** el proveedor rechaza el envío y su respuesta repite parte del mensaje
- **THEN** el motivo registrado contiene solo el estado devuelto
