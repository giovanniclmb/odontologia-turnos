# Design

## Context

V1 de Flyway ya crea las referencias UUID y los códigos de profesional, sillón y prestación. V2 permite una referencia opcional a profesional desde el usuario interno. El catálogo de C-03 declara permisos de intervalo y de usuarios, aunque su matriz actual todavía no los otorga.

## Goals / Non-Goals

**Goals**

- Reutilizar los identificadores de C-02 y el contrato de identidad y permisos de C-03.
- Exponer operaciones de servicio transaccionales y un contrato de consulta para C-06.

**Non-Goals**

- La creación de turnos corresponde a C-06 y los bloqueos a C-15. Los horarios de sede, el transporte HTTP y el frontend quedan fuera de este change.

## Decisions

1. Extender las tablas existentes con V4. Conservar sus UUID y códigos. Incorporar estado para profesional y sillón y duración de referencia para prestación. Las altas del catálogo requieren duración positiva. Una referencia anterior sin duración queda consultable y requiere configurarla antes de usarla como fuente de duración. No completar sus datos por inferencia.
2. Persistir la sede con una fila de clave fija y un intervalo anulable al inicio. La clave y las restricciones de valores preservan una configuración única. Cada ajuste actualiza esa fila dentro de una transacción, con el bloqueo de fila del UPDATE de PostgreSQL. La validación consulta el valor vigente y distingue ausencia de configuración de recurso no disponible.
3. Usar los servicios y repositorios del backend con inyección por constructor y contratos de servicio separados de las entidades de persistencia. Las validaciones de configuración entregan a la agenda el intervalo y las referencias, sin alterar el algoritmo de dominio de C-01.
4. Autorizar antes de mutar. Reutilizar el catálogo de C-03, sumar `config:recursos` y conceder solo a recepción los tres permisos administrativos acordados. La autorización de `crearUsuario` conserva su comprobación existente y pasa a admitir a recepción. Las consultas requieren identidad interna autenticada.
5. Añadir en V4 la FK anulable `usuario.profesional_ref → profesional.id`. La operación de asignación valida las referencias y persiste el vínculo dentro de una transacción.

## Risks / Trade-offs

- Referencias heredadas incompletas → verificar la migración desde V3 con datos ficticios. Preservar los registros y tratar los estados no establecidos como no disponibles.
- Vínculos históricos huérfanos → detectar y revisar antes de desplegar V4. La migración debe fallar sin pérdida de datos si encuentra una referencia no válida.
- Cambio de matriz de acceso → cubrir los permisos de C-05 y ejecutar las pruebas de regresión de autenticación, bootstrap y permisos clínicos de C-03.

## Migration Plan

Ejecutar V4 como migración nueva y verificar instalaciones limpias y actualizaciones desde V3 en PostgreSQL real. Conservar el historial de Flyway. Si V4 falla durante la actualización, comprobar el rollback de la transacción y corregir la causa antes de reintentar. Si V4 ya se aplicó y se requiere revertir, preparar una migración correctiva que preserve los datos registrados.
