# Proposal

## Why

C-02 dejó una base persistente de referencias, pero los recursos protegidos posteriores (pacientes, ficha, agenda, bloqueos) no tienen identidad ni autorización del equipo. Sin este contrato, C-04, C-05, C-06, C-11 y C-16 no pueden verificar quién accede ni qué puede hacer, y la regla clínica (recepción lee, odontólogos editan) quedaría solo en la interfaz. C-03 establece el modelo de usuario, credenciales, roles y permisos mínimos, con separación por registro, sin HTTP ni endpoints.

## What Changes

- Incorporar el modelo de usuario interno con identificador estable, credencial verificable (hash, no texto plano), rol (`recepcion` | `odontologo`) y estado activo/inactivo. Sin rol de superadministrador.
- Definir autenticación: credenciales válidas → identidad con rol; credenciales inválidas, usuario desconocido o inactivo → rechazo genérico sin revelar cuál falló.
- Definir autorización por operación conforme a la matriz de KB 03: `turno:crear`, `turno:reprogramar`, `turno:cancelar`, `agenda-sillon:consultar`, `bloqueo:crear`, `ficha:leer`, `ficha:editar`, `confirmacion:registrar`, `pendientes:consultar`, `paciente:datos-admin`, `config:intervalo`, `usuario:crear/asignar`, `paciente:eliminar`. Cada recurso protegido verifica permiso en backend.
- Establecer separación clínica verificable en backend: recepción lee ficha; odontólogo lee y edita; recepción no edita.
- Definir alcance por registro: recepción = lectura global de pacientes/turnos del sitio; odontólogo = solo los propios. Contrato de vinculación usuario-profesional definido aquí, implementación en C-05.
- Definir política de credenciales: bcrypt (costo 12) + sal por usuario, política mínima de contraseña (longitud >=12, mayúscula/minúscula/número, sin datos personales, sin reutilización de las últimas N), sin registro de credenciales, sin secretos versionados.
- Definir bootstrap controlado de primer usuario/rol mediante script/CLI de un técnico, una sola vez; sin superadmin.
- Definir contrato reutilizable de propagación de identidad/permisos para C-04, C-05 y C-06 (servicio-nivel, sin transporte HTTP).
- Documentar preguntas abiertas remanentes (PA-05 parcial) sin resolverlas por inferencia.

## Capabilities

### New Capabilities
- `acceso-y-permisos-internos`: identidad, autenticación, autorización por operación y por registro, hashing de credenciales, política de contraseñas y bootstrap controlado del equipo, a nivel de servicio sin HTTP.

### Modified Capabilities
Ninguna. `persistencia-de-referencias` no cambia sus requisitos; C-03 agrega un modelo propio de usuario/permisos sin modificar las referencias existentes.

## Impact

Responsable: Fabi. Dependencia C-02 integrada en main. Issue: [#4](https://github.com/giovanniclmb/odontologia-turnos/issues/4). Gobernanza CRITICO: esta propuesta crea artefactos de planificación únicamente; requiere revisión y pedido posterior de Apply antes de código. Bloquea downstream: C-04, C-05, C-06, C-11, C-16.

Archivos previstos para Apply (a acordar en design): modelo de usuario y permisos en backend, migración adicional de Flyway, servicio de autenticación/autorización, bootstrap CLI/script, pruebas unitarias y de integración contra PostgreSQL real. Fuera de alcance: HTTP/endpoints, transporte (cookie vs header), implementación de vinculación usuario-profesional (C-05), UI, email, despliegue productivo.

No se resuelve PA-05 en su totalidad; se documentan los puntos remanentes en el change. No se inventan endpoints ni roles no aprobados.