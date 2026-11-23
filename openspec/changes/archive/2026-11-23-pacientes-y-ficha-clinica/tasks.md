# Tasks

## 1. Precondiciones y persistencia

- [x] 1.1 Antes de Apply, comprobar que C-03 está integrado en `main`; verificar sin exponer filas si `odontologia.paciente` contiene datos y detener el trabajo si la migración requiere completar datos obligatorios no disponibles.
- [x] 1.2 Escribir primero IT PostgreSQL que validen la migración desde V4, preservación de UUID/código, rechazo atómico de pacientes legacy incompatibles, constraints/FKs y grants sin DELETE; observar los fallos esperados antes de implementar V5.
- [x] 1.3 Implementar `V5__pacientes_y_ficha_clinica.sql` y el modelo JPA de paciente ampliado, asignaciones, ficha y notas; ejecutar las IT y verificar identidad estable, integridad y permisos SQL con PostgreSQL Testcontainers.

## 2. Gestión administrativa y asignaciones

- [x] 2.1 Escribir pruebas unitarias de validación y autorización para alta/edición/búsqueda, emails compartidos, creación por odontología sin asignación y alcance global/asignado; observar los fallos esperados antes del servicio.
- [x] 2.2 Conceder `paciente:datos-admin` a ambos roles en la autorización C-03; implementar DTO y servicios de alta, consulta, actualización y búsqueda por código exacto o nombre/apellido parcial sin distinguir mayúsculas; ejecutar pruebas unitarias y verificar que los resultados excluyen datos clínicos y respetan el alcance por rol.
- [x] 2.3 Escribir pruebas de asignación para recepción, rechazo a odontología, profesional inexistente, desasignación y reactivación; implementar el servicio con estado activo y verificar permisos en backend y persistencia PostgreSQL.

## 3. Ficha, antecedentes y notas clínicas

- [x] 3.1 Escribir pruebas unitarias para creación diferida de ficha, lectura por recepción y odontología asignada, denegación fuera de alcance y edición clínica denegada a recepción; observar los fallos antes de implementar.
- [x] 3.2 Implementar lectura/edición de antecedentes y registrar que una ficha nace con los primeros antecedentes; ejecutar pruebas de servicio y verificar que recepción no puede mutar antecedentes.
- [x] 3.3 Escribir pruebas de autoría de notas: contenido obligatorio, autor/fecha del backend, edición por autor asignado, rechazo a otro odontólogo/recepción y ausencia de eliminación; implementar alta/edición y ejecutar la suite unitaria.
- [x] 3.4 Añadir IT PostgreSQL para notas, ficha diferida, FKs, concurrencia al crear el primer contenido y conservación de autor/fecha; ejecutar contra Testcontainers y comprobar que no se usan mocks como evidencia de integridad.

## 4. Integración y evidencia

- [x] 4.1 Ejecutar `mvn -f backend/pom.xml test` y `mvn -f backend/pom.xml clean verify`; conservar resultados reales de pruebas unitarias e IT y resolver cualquier regresión de C-03/C-05.
- [x] 4.2 Ejecutar `openspec validate pacientes-y-ficha-clinica --strict` y revisar que las specs, el contrato de servicio, las migraciones y los criterios de autorización coinciden; registrar Verify y evidencia sin datos reales de pacientes.

## Workflow follow-up

- Vincular el PR hacia `main`, comprobar CI y obtener la revisión requerida según `CONTRIBUTING.md`.
- Integrar solo con las dependencias y aprobaciones de gobernanza cumplidas.
- Archivar el change mediante el flujo OpenSpec y verificar el estado archivado antes de marcar C-04 completado.
