# Proposal

## Why

C-05 prepara los datos que necesita la agenda persistida. C-02 aporta referencias mínimas y C-03 aporta identidad y permisos, pero todavía falta administrar los recursos y la configuración de la sede.

## What Changes

- Persistir un intervalo único para la sede. Arranca sin configurar, acepta minutos enteros positivos y cada ajuste rige para las validaciones futuras.
- Dar de alta y consultar profesionales y sillones sin cantidades fijas. La disponibilidad mínima combina la existencia y el estado de ambos recursos con la configuración del intervalo.
- Partir de un catálogo de prestaciones vacío y permitir altas, consultas y ajustes de la duración de referencia.
- Reservar las escrituras de configuración y la creación y vinculación de usuarios internos a recepción/administración. Odontología puede consultar los recursos y la configuración.
- Hacer efectiva la vinculación usuario-profesional definida en C-03. Mantener los bloqueos en C-15 y la creación de turnos persistidos en C-06.

## Capabilities

### New Capabilities

- `configuracion-de-recursos` — profesionales, sillones, prestaciones e intervalo común de sede.

### Modified Capabilities

- `acceso-y-permisos-internos` — permiso de configuración de recursos, asignación a recepción de los permisos aprobados y vinculación efectiva usuario-profesional.

## Impact

Backend Java/Spring, migraciones Flyway y pruebas con PostgreSQL. Los contratos son de servicio. El alcance cubre US-005 y aporta los datos requeridos por RN-AG-02, RN-AG-04 y RN-AG-05.
