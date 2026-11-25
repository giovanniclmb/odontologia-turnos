# Spec Delta

## MODIFIED Requirements

### Requirement: Inicio de sesión y sesión autenticada en la interfaz
El sistema SHALL permitir iniciar sesión con credenciales válidas y operar con la sesión que el navegador conserva en una cookie inaccesible para JavaScript. La interfaz SHALL NOT guardar el token de sesión, el token anti-CSRF ni la identidad en almacenamiento persistente del navegador. Las credenciales inválidas SHALL mostrar un rechazo genérico.

#### Scenario: Login válido
- **WHEN** un usuario ingresa credenciales válidas
- **THEN** el sistema abre la sesión y habilita las operaciones autorizadas

#### Scenario: Login inválido
- **WHEN** un usuario ingresa credenciales inválidas
- **THEN** el sistema muestra un rechazo genérico y no abre sesión

#### Scenario: Cierre de sesión
- **WHEN** el usuario cierra sesión y el servidor confirma el cierre o indica que la sesión ya no era válida
- **THEN** la interfaz descarta identidad y token anti-CSRF y vuelve a la pantalla de login

#### Scenario: Cierre no confirmado
- **WHEN** el cierre de sesión falla sin que el servidor lo confirme
- **THEN** la interfaz informa que la sesión sigue abierta y permite reintentar

#### Scenario: Recarga con sesión vigente
- **WHEN** el usuario recarga la página o abre otra pestaña con la sesión vigente
- **THEN** la interfaz recupera identidad y rol desde el servidor sin pedir credenciales

#### Scenario: Sesión vencida
- **WHEN** una operación o el arranque reciben una respuesta de no autenticado
- **THEN** la interfaz descarta la identidad y muestra la pantalla de login

#### Scenario: Sesión reemplazada en otra pestaña
- **WHEN** una escritura se rechaza porque el token anti-CSRF ya no corresponde a la sesión
- **THEN** la interfaz vuelve a consultar la sesión, muestra la identidad vigente y no reenvía la escritura

#### Scenario: Sin credenciales en el almacenamiento
- **WHEN** la interfaz arranca en un navegador que conserva token o identidad de la versión anterior
- **THEN** los elimina y no los utiliza
