# Spec Delta

## ADDED Requirements

### Requirement: Consulta autorizada de bloqueos
El backend SHALL exigir `bloqueo:consultar` y devolver los bloqueos que intersectan el período semiabierto pedido para la vista de un sillón, ordenados por inicio. Recepción SHALL recibir todos los del sitio con sus recursos.

#### Scenario: Recepción consulta el período
- **WHEN** recepción consulta un período con bloqueos de distintos sillones y profesionales
- **THEN** recibe todos los que lo intersectan, con sus recursos

#### Scenario: Consulta sin identidad o con período inválido
- **WHEN** falta la identidad, falta un extremo o el fin no es posterior al inicio
- **THEN** se rechaza la consulta sin devolver bloqueos

### Requirement: Alcance de bloqueos para odontología
Odontología SHALL recibir solo los bloqueos que afectan al sillón consultado o a su profesional vinculado. Cada resultado SHALL indicar el período y cuáles de esos dos recursos afecta, sin identificar a otros profesionales ni a otros sillones.

#### Scenario: Bloqueo del sillón con otro profesional
- **WHEN** un bloqueo afecta al sillón consultado y a otro profesional
- **THEN** odontología recibe el período y la afectación del sillón, sin la referencia del otro profesional

#### Scenario: Bloqueo del propio profesional en otro sillón
- **WHEN** un bloqueo afecta a su profesional y a un sillón distinto del consultado
- **THEN** odontología recibe el período y la afectación de su profesional, sin la referencia de ese sillón

#### Scenario: Bloqueo ajeno
- **WHEN** un bloqueo no afecta al sillón consultado ni a su profesional
- **THEN** odontología no lo recibe

#### Scenario: Odontología sin profesional vinculado
- **WHEN** la identidad de odontología no tiene profesional vinculado
- **THEN** recibe solo la afectación del sillón consultado
