# Tasks

Responsable: Fabi. Gobernanza: CRITICO. Planificación autorizada; Apply requiere pedido posterior del usuario. Cada tarea se acota a una sesión orientativa menor de dos horas. [Diseño](design.md) · [Spec](specs/acceso-y-permisos-internos/spec.md).

## 1. Modelo de usuario y migración

- [x] 1.1 Escribir prueba de arranque que observe la ausencia de la tabla `usuario` en una base limpia; añadir la migración Flyway con `usuario` y `token_acceso` (siguiente número global coordinado por Giovanni); verificar GREEN contra PostgreSQL real y que V1 de C-02 no se reaplica.
- [x] 1.2 Escribir pruebas SQL directas de constraints: rol CHECK (`recepcion`/`odontologo`), `nombre_usuario` NOT NULL/UNIQUE/no vacío, FK de `token_acceso.usuario_id`; verificar rechazo y conservación de datos confirmados.
- [x] 1.3 Implementar entidades JPA y repositorios mínimos separados del dominio para `usuario` y `token_acceso`; verificar guardar/recuperar con UUID generado en aplicación y `ddl-auto=validate` en verde.
- [x] 1.4 Documentar la convención de migración y permisos (`odonto_app` SELECT/INSERT/UPDATE) en README sin secretos; verificar enlaces y comandos.

## 2. Hashing y política de credenciales

- [x] 2.1 Implementar hash bcrypt (costo 12, sal embebida) y verificación; prueba TS-6 (hash ≠ texto plano, algoritmo y costo); verificar GREEN.
- [x] 2.2 Implementar política mínima de contraseña (longitud >=12, mayúscula/minúscula/número, sin datos personales, no reutilización de las últimas N con N=5 por defecto); pruebas de rechazo para cada caso; verificar GREEN.
- [x] 2.3 Verificar que los logs de autenticación y gestión de credenciales no contienen credenciales ni hashes (prueba de captura de log); corregir cualquier salida.

## 3. Autenticación y autorización a nivel de servicio

- [x] 3.1 Implementar `ServicioAutenticacion` (credenciales válidas → identidad con rol; inválidas/desconocido/inactivo → rechazo genérico); pruebas TS-1 y TS-2; verificar mismo error genérico en todos los casos de rechazo.
- [x] 3.2 Implementar `ServicioAutorizacion` con permisos nombrados (`turno:crear`, `turno:reprogramar`, `turno:cancelar`, `agenda-sillon:consultar`, `bloqueo:crear`, `ficha:leer`, `ficha:editar`, `confirmacion:registrar`, `pendientes:consultar`, `paciente:datos-admin`, `config:intervalo`, `usuario:crear/asignar`, `paciente:eliminar`); pruebas TS-3 y TS-4 (permitido/denegado/anónimo).
- [x] 3.3 Implementar alcance por registro (recepción global; odontólogo solo vinculados) con deny-by-default para no vinculados; prueba TS-7; verificar rechazo fuera de alcance.
- [x] 3.4 Implementar separación clínica (recepción lee ficha; odontólogo lee y edita; recepción no edita) en backend; pruebas de autorización efectiva; verificar con PostgreSQL real, no mocks.
- [x] 3.5 Exponer contrato reutilizable de identidad/permisos para C-04, C-05 y C-06 (sin transporte); pruebas de consumo simulado; verificar que no depende de HTTP.

## 4. Bootstrap controlado

- [x] 4.1 Implementar `ServicioBootstrap` idempotente (crea primer usuario si no existe; rechaza segundo intento); prueba TS-5; verificar rechazo de creación por usuario sin permiso.
- [x] 4.2 Implementar entrypoint/CLI de técnico con entrada segura (prompt o variable de entorno no versionada); documentar en `.env.example` solo nombres/placeholders; verificar ausencia de secretos versionados.
- [x] 4.3 Verificar que no existe rol de superadministrador ni permisos globales por inferencia; prueba de catálogo de roles.

## 5. Token opaco persistido (modelo)

- [x] 5.1 Implementar entidad/repositorio `token_acceso` (hash del token, expiración, revocación) sin emisión/transporte; prueba de persistencia contra PostgreSQL real; verificar que el token en claro no se almacena.

## 6. Integración y verificación

- [x] 6.1 Ejecutar pruebas de integración (Failsafe) contra PostgreSQL real para autenticación, autorización, alcance y bootstrap; verificar que no se usan mocks como evidencia de autorización efectiva.
- [x] 6.2 Ejecutar `mvn clean verify` con Docker disponible; registrar unitarias e IT; verificar preflight de Docker y que Failsafe no omite IT. (Review correction: mvn test = 77 unitarias OK; mvn clean verify = 71 IT OK, BUILD SUCCESS.)
- [x] 6.3 Ejecutar `openspec validate acceso-y-permisos-internos --strict` y `git diff --check`; conciliar README/KB/CHANGES con decisiones y evidencia; verificar enlaces.

## Workflow follow-up

- Presentar el resultado y límites antes de cerrar el change; las pruebas no sustituyen evidencia humana académica.
- Archivar mediante CLI y skill cuando el usuario pida finalizar, sincronizando la capacidad principal y comprobando el archivo.
- Hacer commits atómicos dentro del trabajo autorizado. Publicar y abrir/integrar PR únicamente con pedido expreso de push/integración.
- Mantener Issue #4 y dependencias como seguimiento del trabajo real; la planificación no equivale a implementación ni desbloquea código de C-04 antes de integrar C-03.
- Documentar preguntas abiertas remanentes (PA-05 parcial) sin resolverlas por inferencia.

## Correcciones de review PR #29 (CRITICAL, sin nuevo alcance)

- [x] P1 BootstrapCli conectado a `ServicioBootstrap` + repositorios reales (Usuario, TokenAcceso) mediante contexto Spring; contraseña leída y usada; sin HTTP. Comando documentado en backend/README.md. IT: `BootstrapPersistenciaIT` (creación persistente del primer usuario, autenticación posterior exitosa, segundo bootstrap rechazado).
- [x] P2 Concurrencia de bootstrap serializada con `pg_advisory_xact_lock` en transacción (`@Transactional`); IT `BootstrapConcurrenciaIT` con dos hilos: exactamente una creación exitosa.
- [x] P2 `AccesoPersistenciaIT.tokenEnClaroNoSeAlmacena` reemplazada por aserción efectiva sobre `hash_token`; agregadas IT de autenticación, autorización, alcance por registro y bootstrap contra PostgreSQL real (`AccesoPersistenciaIT`, `BootstrapPersistenciaIT`).
- [x] P2 `ArranqueIT.rechazaHistorialDeUnaVersionFutura` corregido: copia real de V1 y V2, V3 temporal ausente del binario, rechazo correspondiente a la versión 3, historial sin repair/clean (3 migraciones exitosas).
- Pendiente humano: revisión personal/académica y evidencia humana no se declaran resueltas por pruebas verdes. PA-05 sigue parcial sin resolver.