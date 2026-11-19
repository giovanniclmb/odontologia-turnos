# Design

## Context

Ver [proposal.md](proposal.md) y [spec](specs/acceso-y-permisos-internos/spec.md). C-02 está integrado y archivado: backend Maven/Java 25, Spring Boot 4.1.2, JPA/Hibernate separado del dominio, Flyway SQL, PostgreSQL 18.7, roles separados (`odontologia` schema, `odonto_migrator`, `odonto_app`), cuatro referencias con UUID generado en aplicación. No hay HTTP, usuarios ni permisos aún. Este change agrega identidad y autorización a nivel de servicio sobre esa base, sin exponer transporte.

## Goals / Non-Goals

**Goals:** modelo de usuario y permisos verificables en backend; autenticación con rechazo genérico; autorización por operación nombrada y por registro; hashing bcrypt (costo 12) con sal por usuario y política de contraseña; bootstrap controlado de primer usuario; contrato reutilizable para C-04/C-05/C-06; modelo de token opaco persistido para el mecanismo aprobado, sin transporte.

**Non-Goals:** no HTTP/endpoints/cookies/headers/sesiones; no UI; no implementación de vinculación usuario-profesional (solo contrato); no asignación definitiva de permisos pendientes en KB (deny-by-default); no proveedor de email, despliegue ni CI nuevo; no modificar referencias de C-02 ni el dominio de C-01.

## Decisions

### 1. Mecanismo de autenticación aprobado: token opaco persistido
Se adopta el mecanismo aprobado en explore: token opaco persistido en base de datos, consumido mediante encabezado `Authorization` (Option C). En C-03 se modela la persistencia del token y la identidad resultante a nivel de servicio; el transporte (cookie vs header), endpoints y ciclo de vida HTTP se definen en C-06/C-10. Alternativas descartadas: JWT y sesiones en memoria, por decisión explícita del usuario y por evitar estado no revocable ni secretos en cliente.

### 2. Modelo de usuario en el esquema `odontologia`
Se agrega una tabla `usuario` con:
- `id` uuid, clave primaria, generado en aplicación (patrón C-02).
- `nombre_usuario` text, NOT NULL, UNIQUE, código no vacío (patrón de código de referencia).
- `hash_credencial` text, NOT NULL (bcrypt costo 12, sal embebida en el hash).
- `rol` text, NOT NULL, CHECK en (`recepcion`, `odontologo`).
- `activo` boolean, NOT NULL, default true.
- `profesional_ref` uuid, NULL (referencia opaca al profesional; FK/constraints efectivos en C-05).
- `creado_en` timestamptz, NOT NULL.

No se almacena sal separada: bcrypt genera y embebe una sal por hash, satisfaciendo el requisito de sal por usuario sin columna adicional. Alternativa de columna `salt` separada descartada por redundancia y mayor superficie de error.

### 3. Permisos derivados del rol, con deny-by-default para pendientes
Los permisos se representan como constantes nombradas (`turno:crear`, etc.) y se resuelven a partir del rol mediante un mapeo en código. Las operaciones marcadas como "pendiente" en KB 03 (eliminar pacientes, configurar intervalo, crear/asignar usuarios, bloqueos por odontólogo, etc.) se dejan deny-by-default hasta aprobación expresa del usuario; no se otorgan por inferencia. Alternativa de tabla `permiso`/`usuario_permiso` descartada en este change por complejidad y porque el alcance aprobado no requiere grants dinámicos; si C-05/C-06 los necesitan, se amplía entonces.

### 4. Hashing y política de contraseña
Se usa bcrypt con costo 12 (o argon2 si el equipo lo aprueba en Apply) con sal por usuario embebida. La política mínima se valida en el servicio antes de persistir: longitud >=12, mayúscula, minúscula, número, sin datos personales (nombre/usuario/email del usuario), sin reutilización de las últimas N (N acordado en Apply, default 5 si no se define). Las credenciales nunca se registran en logs ni se versionan. Alternativa de validación solo en UI descartada por RA-03 (la regla debe verificarse en backend).

### 5. Bootstrap controlado
Un proceso de bootstrap (comando/entrypoint de técnico) crea el primer usuario si no existe ninguno. Es idempotente: si ya hay usuarios, rechaza. Requiere datos por entrada segura (prompt o variable de entorno no versionada), no archivos con secretos. No crea rol superadmin. La identidad del técnico no se modela como usuario del sistema. Alternativa de migración SQL con usuario semilla descartada por versionar secretos.

### 6. Alcance por registro
Recepción: lectura global de pacientes/turnos del sitio. Odontólogo: solo pacientes/turnos vinculados a su `profesional_ref`. La vinculación efectiva se implementa en C-05; C-03 fija el contrato (`profesional_ref` opaco + permiso `usuario:crear/asignar` para asignarla). Mientras no haya vinculación, el odontólogo no obtiene acceso a pacientes (deny-by-default), evitando acceso clínico global por inferencia.

### 7. Token opaco persistido (modelo de datos, sin transporte)
Tabla `token_acceso`: `id` uuid PK, `usuario_id` uuid FK a `usuario`, `hash_token` text NOT NULL (el token en claro solo existe en el cliente; en DB se guarda su hash), `creado_en`, `expira_en`, `revocado` boolean. El ciclo de emisión/validación/revocación y el transporte se implementan en C-06/C-10. Esto prepara el mecanismo aprobado sin introducir HTTP.

### 8. Migración Flyway
Nueva migración SQL en `backend/src/main/resources/db/migration/`, con el siguiente número global coordinado por Giovanni al integrar (no se asume V2 si hay cambios intermedios). Crea `usuario` y `token_acceso` con constraints y permisos para `odonto_app` (SELECT/INSERT/UPDATE; DELETE solo si una operación posterior lo requiere y se acuerde). Hibernate `ddl-auto=validate`. Sin datos semilla.

### 9. Servicio de autenticación/autorización
Componentes a nivel de servicio (nombres ilustrativos, no contrato de API):
- `ServicioAutenticacion`: valida credenciales, devuelve identidad o rechazo genérico.
- `ServicioAutorizacion`: verifica permiso por operación y alcance por registro.
- `ServicioBootstrap`: crea primer usuario.
- `ServicioCredenciales`: hash/verificación y política.
Contrato de identidad/permisos reutilizable expuesto para C-04/C-05/C-06 sin transporte.

## Risks / Trade-offs

- [Permisos pendientes sin asignación] → deny-by-default; requiere aprobación explícita antes de habilitar; documentado en open questions.
- [Vinculación usuario-profesional diferida] → odontólogo sin acceso a pacientes hasta C-05; evita acceso global por inferencia.
- [Número de migración] → coordinar al integrar; no editar migraciones aplicadas.
- [Costo bcrypt 12] → mayor latencia en autenticación; aceptable para equipo pequeño; medir en Apply.
- [Token opaco modelado antes de transporte] → tabla sin uso hasta C-06; riesgo de deriva; mitigado por contrato documentado.

## Migration Plan

Apply agregará migración con `usuario` y `token_acceso`, servicio de autenticación/autorización, bootstrap CLI y pruebas. No migra datos existentes (no hay usuarios). Rollback: nueva migración correctiva; no Flyway clean ni borrado de volumen. No se toca Postgres.app.

## Open Questions

- PA-05 (parcial): qué rol puede crear/asignar usuarios tras el bootstrap; permisos de configuración de intervalo; eliminación de pacientes; bloqueos por odontólogo; alcance exacto de datos administrativos del paciente. Se documentan como pendientes; no se resuelven por inferencia.
- Valor de N para no reutilización de contraseñas (default propuesto 5).
- Costo bcrypt vs argon2: confirmar en Apply según soporte de dependencias.
- Política de expiración/revocación de tokens: definir en C-06/C-10.

## UI Mockups

- Decision: no (no UI)
- Fecha: 19/11/2026.
- Este change no crea ni modifica pantallas.

## Sources

KB: 03_actores_y_roles, 05_reglas_de_negocio (RN-CL-01/02), 08_arquitectura_propuesta (seguridad), 10_preguntas_abiertas (PA-05), 04_modelo_de_datos. CHANGES.md ficha C-03. AGENTS.md RA-01..RA-04. Decisiones aprobadas en explore (opaque token, bootstrap, scope, hash, vinculación).