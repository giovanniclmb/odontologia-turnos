# Tasks

## 1. Transporte HTTP y autenticación (backend)

- [x] 1.1 Agregar `spring-boot-starter-web` y ajustar `application.yaml` a `web-application-type: servlet`; verificar que la aplicación arranca con Testcontainers.
- [x] 1.2 Implementar filtro/interceptor de token opaco que resuelva `Identidad` desde `Authorization: Bearer`; verificar con prueba de integración que un token válido autentica y uno inválido es rechazado.
- [x] 1.3 Implementar controlador de login (`POST /auth/login`) emitiendo token opaco persistido en `token_acceso`; verificar login válido/ inválido y expiración.
- [x] 1.4 Implementar endpoint de logout que revoca el token; verificar que el token revocado es rechazado.
- [x] 1.5 Implementar manejo de errores centralizado (400/403/404/409) con JSON estable; verificar respuestas de error en pruebas de integración.

## 2. Controladores de pacientes (backend)

- [x] 2.1 Implementar controlador de pacientes (listar/buscar, crear, consultar, actualizar) delegando en `ServicioPacientes`; verificar permisos y alcance por rol vía Testcontainers.
- [x] 2.2 Implementar controlador de asignación de profesionales (asignar/desasignar) delegando en `ServicioAsignacionPacientes`; verificar que solo recepción opera y odontología es denegada.
- [x] 2.3 Implementar controlador de ficha clínica (consultar, actualizar antecedentes, agregar/editar nota) delegando en `ServicioFichaClinica`; verificar autoría y alcance.

## 3. Base de frontend (estructura y tooling)

- [x] 3.1 Crear directorio `frontend/` con `tsconfig.json` (`strict: true`), estructura `src/` y `public/`, y build con `tsc`; verificar compilación sin errores.
- [x] 3.2 Implementar capa HTTP compartida (cliente fetch con token en `Authorization`, manejo de errores, tipos TS para DTO); verificar compilación y prueba manual de login.
- [x] 3.3 Implementar autenticación en frontend (login, logout, persistencia de token); verificar flujo manual de sesión.

## 4. Interfaz de pacientes (US-001)

- [x] 4.1 Implementar listado y búsqueda de pacientes con datos administrativos básicos; verificar filtrado por código y texto parcial.
- [x] 4.2 Implementar alta de paciente con validación de campos obligatorios; verificar creación y error de datos inválidos.
- [x] 4.3 Implementar edición de datos administrativos (sin código); verificar actualización y denegación fuera de alcance.
- [x] 4.4 Implementar detalle de paciente con asignaciones; verificar visualización autorizada y denegada.
- [x] 4.5 Implementar gestión de asignaciones de profesionales (recepción); verificar asignar/quitar y ocultamiento para odontología.

## 5. Interfaz de ficha clínica (US-002/US-003)

- [x] 5.1 Implementar visualización de antecedentes y notas; verificar recepción solo lectura y odontología con edición.
- [x] 5.2 Implementar edición de antecedentes por odontólogo asignado (creación diferida de ficha); verificar creación y actualización.
- [x] 5.3 Implementar gestión de notas (agregar, editar propia, denegar ajena, nota vacía); verificar autoría y fecha.
- [x] 5.4 Aplicar gating por rol en la interfaz para ocultar acciones no autorizadas; verificar que el backend sigue denegando operaciones por fuera de la UI.

## 6. Verificación y coordinación

- [x] 6.1 Ejecutar TDD completo (unitarias de validación/autorización + integración Testcontainers de controladores); verificar suite verde.
- [x] 6.2 Verificar manualmente la UI (login, pacientes, ficha) en desktop y mobile; registrar resultados.
- [x] 6.3 Documentar contratos HTTP consumidos por C-10 y actualizar CHANGES.md con estado; verificar enlace y coherencia.

## Workflow follow-up

- Política visual resuelta: system-and-mockups, fundación aprobada (Rev 1) y mockups de C-11 aprobados (2026-11-23).
- Archivar el change tras cumplir revisión y verificación.