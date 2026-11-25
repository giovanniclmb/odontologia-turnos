# Spec Delta

## MODIFIED Requirements

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

### Requirement: Reintentos con backoff corto

El sistema SHALL reintentar un envío fallido hasta el número configurado de reintentos, con backoff corto, antes de marcarlo como fallido, reenviando cada intento con la misma clave de idempotencia.

#### Scenario: Recuperación en reintento
- **WHEN** el primer intento falla y un reintento posterior tiene éxito
- **THEN** el resultado final es exitoso y se registra la cantidad de intentos

#### Scenario: Agotamiento de reintentos
- **WHEN** todos los intentos fallan
- **THEN** el estado final es fallido y no se realizan más intentos
