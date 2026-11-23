# Design

## Context

Ver [proposal.md](proposal.md) y las specs delta de pacientes/ficha y acceso. El backend usa Spring Boot no web, JPA separado del dominio, Flyway y PostgreSQL; la tabla `odontologia.paciente` ya contiene UUID estable y `codigo` con unicidad exacta. C-03 aporta `Identidad`, `Permiso` y autorización a nivel de servicio; C-05 aporta profesionales persistidos y la relación usuario-profesional. No existe transporte HTTP.

## Goals / Non-Goals

**Goals:**

- Extender el paciente persistido existente sin reemplazar su identidad ni `codigo`.
- Aplicar permisos y alcance por profesional antes de leer o modificar un registro.
- Persistir la ficha diferida y sus notas con restricciones relacionales y sin permisos SQL de borrado.
- Definir contratos Java de servicio/DTO que puedan consumirse sin transporte.

**Non-Goals:**

- HTTP, autenticación por token en transporte, interfaz, odontograma, auditoría/versionado de cambios, papelera o eliminación lógica de pacientes/notas.
- Filtros avanzados, paginación o políticas de duplicidad de email.

## Decisions

### Modelo relacional y migración

Crear `V5__pacientes_y_ficha_clinica.sql`, sujeto a confirmar la secuencia global con Giovanni al integrar. No modificar V1–V4 ni cambiar UUID o códigos existentes.

- Ampliar `odontologia.paciente` con `nombre text NOT NULL`, `apellido text NOT NULL`, `email text NOT NULL` y `telefono text NULL`. Mantener el UUID y `codigo` existentes. El email no tendrá unicidad; la aplicación validará formato. Constraints SQL rechazarán texto vacío en nombre, apellido y email, mientras que la validación del formato email queda en el servicio.
- Crear `odontologia.paciente_profesional` con `paciente_id`, `profesional_id` y `activo boolean NOT NULL`. La clave primaria será el par paciente/profesional, con FK a las tablas existentes. Una asignación nueva inserta activa; quitarla actualiza `activo=false`; una reasignación la reactiva. Este estado conserva solo la asignación vigente y no pretende ser auditoría histórica. Recepción es la única que puede modificarla.
- Crear `odontologia.ficha_clinica` con `paciente_id` como PK/FK y `antecedentes text NULL`. La fila se crea de forma transaccional al registrar los primeros antecedentes o la primera nota. Si nace por una nota, antecedentes queda nulo. La unicidad de `paciente_id` impide fichas duplicadas.
- Crear `odontologia.nota_clinica` con UUID generado por la aplicación, `paciente_id`, `autor_usuario_id`, `texto text NOT NULL` y `creada_en timestamptz NOT NULL`. FKs a ficha/paciente y usuario; constraint de texto no vacío. Autor y fecha son inmutables al editar el contenido.
- Conceder a `odonto_app` solo SELECT/INSERT/UPDATE sobre estas tablas/columnas; no conceder DELETE. La baja de asignación se implementa mediante UPDATE del estado activo. Las altas de ficha y nota se confirman en una transacción.

Se mantiene el patrón de C-02: UUID generado por Hibernate/aplicación, FK y constraints en PostgreSQL, `ddl-auto=validate`, migración como fuente del esquema. Las pruebas verificarán la migración desde V4 con pacientes existentes, conservando UUID/código; los registros heredados requieren completar los nuevos datos obligatorios antes de migrar. No hay datos clínicos reales ni valores de relleno: la migración debe identificar y rechazar filas legacy incompletas sin alterar los registros confirmados; acordar la precondición de V5 en Apply con base limpia de pacientes o datos ficticios completos.

### Contratos de servicio y DTO

Sin controladores ni rutas HTTP. Contratos propuestos en el paquete `ar.edu.utn.odontologia.pacientes`:

- `ServicioPacientes.crear(CrearPaciente)`, `actualizarDatos(UUID, ActualizarDatosPaciente)`, `consultar(UUID)` y `buscar(FiltroBusquedaPaciente)`; los DTO de lectura serán `PacienteResumen` y `PacienteDetalle`.
- `ServicioAsignacionPacientes.asignar(UUID pacienteId, UUID profesionalId)` y `desasignar(UUID pacienteId, UUID profesionalId)`. Solo recepción. El conjunto admite cero o más asignaciones.
- `ServicioFichaClinica.consultar(UUID pacienteId)`, `actualizarAntecedentes(UUID pacienteId, String texto)`, `agregarNota(UUID pacienteId, CrearNotaClinica)` y `editarNota(UUID pacienteId, UUID notaId, EditarNotaClinica)`; la lectura devuelve `FichaClinicaVista` y notas con `autorId` y `creadaEn`.
- Los DTO de alta/edición de datos administrativos contienen `codigo` (solo alta), `nombre`, `apellido`, `email` y `telefono`. `FiltroBusquedaPaciente` admite código exacto y coincidencia parcial de nombre/apellido sin distinguir mayúsculas. Los resultados de búsqueda no incluyen datos clínicos.
- El usuario autor y la fecha de creación de una nota se obtienen de `Identidad` y reloj del backend, nunca del DTO entrante. El contrato del reloj se inyecta para que las pruebas sean deterministas.

Todos los servicios reciben `Identidad` explícitamente o consumen el contrato C-03. Exigir `paciente:datos-admin` o `ficha:leer`/`ficha:editar` antes de operar. Recepción puede acceder globalmente. Para odontología, validar que `Identidad.profesionalRef` coincida con una asignación activa del paciente. La creación administrativa no requiere asignación; un odontólogo creador no recibe acceso posterior por ese hecho. La autorización se comprueba en la operación de servicio y vuelve a aplicarse antes de persistir la mutación, no en la UI.

La edición de antecedentes admite a cualquier odontólogo asignado. Para notas, además de la asignación vigente, comparar `nota.autor_usuario_id` con `Identidad.usuarioId`; rechazar sin modificar si no coincide. Recepción puede leer la ficha, pero no invocar mutaciones clínicas. Ningún DTO ni servicio ofrece eliminación.

### Validación y búsqueda

- Nombre, apellido y email se validan como no nulos/no vacíos; email pasa una validación sintáctica en el servicio y no es único. El código conserva la restricción exacta existente. Teléfono es texto opcional y no se impone formato regional.
- Nota requiere texto no vacío. Antecedentes son texto libre; la ficha nace cuando se guarda por primera vez un antecedente o nota.
- La búsqueda acepta código exacto y coincidencia parcial de nombre/apellido sin distinguir mayúsculas; filtra por alcance antes de devolver resultados. Los DTO de listado incluyen solo datos administrativos.

### Pruebas

Aplicar TDD a reglas de validación y autorización. Pruebas unitarias cubrirán roles, asignación activa/inactiva, creación sin asignar, recepción global, denegación fuera de alcance, autoría de notas y ausencia de eliminación. Tests de integración con Testcontainers PostgreSQL comprobarán V5, constraints/FKs, persistencia de relaciones, creación diferida de ficha, notas fechadas y grants de `odonto_app`. No usar mocks para afirmar integridad de PostgreSQL.

## Risks / Trade-offs

- [La tabla paciente puede tener referencias legacy sin datos administrativos] → V5 comprueba que no existan filas antes de añadir los campos obligatorios; si hay filas, falla sin modificarlas y requiere una migración de datos acordada con información ficticia/autorizada, nunca valores inferidos.
- [Desactivar y reactivar una asignación no conserva historia] → el change solo requiere el alcance vigente y no adopta auditoría; no borrar filas ni afirmar trazabilidad histórica.
- [Una carrera puede crear ficha dos veces] → PK paciente en `ficha_clinica` y transacción; resolver conflicto único y releer la fila confirmada.
- [La autorización de una nota depende simultáneamente de asignación y autoría] → comprobar ambos criterios en backend y cubrir las combinaciones con pruebas de servicio.
- [Email válido en sintaxis no asegura entrega] → entrega y proveedor pertenecen a changes de email; C-04 solo guarda el dato.

## Migration Plan

1. Confirmar que C-03 está integrado en `main`, comprobar sin exponer filas si la tabla paciente está vacía y coordinar el número de Flyway. Si contiene pacientes, detener Apply y acordar una migración de datos antes de ejecutar V5.
2. Ejecutar V5 en PostgreSQL temporal y verificar que conserva la estructura de UUID/código, crea el nuevo modelo y falla de forma atómica si existen filas legacy incompatibles.
3. Aplicar servicios/DTO con TDD y pruebas de autorización antes de cerrar las operaciones de persistencia.
4. Verificar migración, constraints, permisos SQL y escenarios clínicos con PostgreSQL Testcontainers; conservar evidencia ficticia.
5. PR hacia `main`, CI y revisión según `CONTRIBUTING.md`; registrar Verify y archivar solo tras cumplir el flujo OpenSpec.

Si V5 ya se aplicó en una base compartida, cualquier corrección será una migración posterior; no editar V5 ni ejecutar `clean`, `repair` o borrar volúmenes como rollback.

