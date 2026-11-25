# Spec Delta

## MODIFIED Requirements

### Requirement: Autenticación por token opaco
El sistema SHALL emitir un token opaco al autenticar credenciales válidas, entregarlo solo en una cookie `HttpOnly`, `Secure` y `SameSite=Strict`, con vencimiento no posterior al del token, y resolver la identidad a partir de esa cookie. El sistema SHALL NOT incluir el token en el cuerpo de las respuestas ni aceptarlo en el encabezado `Authorization`.

#### Scenario: Login exitoso emite token
- **WHEN** un usuario envía credenciales válidas al endpoint de login
- **THEN** el sistema responde con la identidad y un token anti-CSRF, y establece el token opaco en la cookie sin incluirlo en el cuerpo

#### Scenario: Token inválido o ausente
- **WHEN** una solicitud protegida no incluye una cookie con un token válido
- **THEN** el sistema rechaza la solicitud sin revelar detalles

#### Scenario: Cookie inválida se descarta
- **WHEN** una solicitud distinta del login presenta una cookie con un token vencido, revocado o desconocido
- **THEN** el sistema la rechaza como no autenticada y ordena al navegador eliminar la cookie

#### Scenario: Cookie inválida no impide ingresar
- **WHEN** un usuario envía credenciales válidas al login mientras el navegador conserva una cookie inválida
- **THEN** el sistema autentica y reemplaza la cookie

#### Scenario: Encabezado Authorization sin efecto
- **WHEN** una solicitud protegida presenta un token vigente solo en el encabezado `Authorization`
- **THEN** el sistema la rechaza como no autenticada

#### Scenario: Cierre de sesión revoca token
- **WHEN** el usuario cierra sesión con su cookie y su token anti-CSRF
- **THEN** el sistema revoca el token, ordena eliminar la cookie y rechaza las solicitudes posteriores con ese token

## ADDED Requirements

### Requirement: Protección anti-CSRF de las operaciones con efecto
El sistema SHALL exigir, en toda solicitud autenticada que no sea de solo lectura, un token anti-CSRF ligado a la sesión y enviado en un encabezado propio. Si falta o no corresponde, SHALL rechazarla antes de ejecutar la operación. El login SHALL aceptar únicamente contenido JSON y rechazar orígenes no permitidos.

#### Scenario: Operación con token válido
- **WHEN** una identidad autenticada y autorizada envía una escritura con el token anti-CSRF de su sesión
- **THEN** el sistema ejecuta la operación

#### Scenario: Token ausente o ajeno
- **WHEN** una escritura autenticada llega sin token anti-CSRF o con el de otra sesión
- **THEN** el sistema responde `403` con el código `csrf_invalido` y no modifica datos ni revoca la sesión

#### Scenario: Lectura sin token anti-CSRF
- **WHEN** una identidad autenticada realiza una consulta sin token anti-CSRF
- **THEN** el sistema aplica la autorización vigente y responde la consulta

#### Scenario: Login desde un formulario ajeno
- **WHEN** se envía al login contenido que no es JSON o una solicitud desde un origen no permitido
- **THEN** el sistema la rechaza sin establecer la cookie

### Requirement: Consulta de la sesión vigente
El sistema SHALL ofrecer una consulta que, con una sesión válida, devuelva la identidad vigente y el token anti-CSRF sin incluir el token de sesión. Las respuestas de login y de esta consulta SHALL impedir su almacenamiento en caché.

#### Scenario: Sesión válida
- **WHEN** se consulta la sesión con una cookie válida
- **THEN** el sistema devuelve usuario, nombre de usuario, rol, profesional vinculado y token anti-CSRF

#### Scenario: Sin sesión
- **WHEN** se consulta la sesión sin cookie o con una inválida
- **THEN** el sistema responde como no autenticado sin revelar detalles

### Requirement: Origen explícito para solicitudes con credenciales
El sistema SHALL admitir solicitudes entre orígenes con credenciales únicamente desde una lista configurada de orígenes exactos y SHALL NOT combinar credenciales con un comodín de origen.

#### Scenario: Origen permitido
- **WHEN** la interfaz de un origen configurado consulta la API con credenciales
- **THEN** la respuesta habilita ese origen y las credenciales

#### Scenario: Origen no permitido
- **WHEN** un origen fuera de la lista solicita la API
- **THEN** la respuesta no lo habilita

#### Scenario: Comodín configurado
- **WHEN** se configura `*` como origen permitido
- **THEN** el sistema no atiende solicitudes con credenciales bajo esa configuración
