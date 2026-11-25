# Spec Delta

## ADDED Requirements

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