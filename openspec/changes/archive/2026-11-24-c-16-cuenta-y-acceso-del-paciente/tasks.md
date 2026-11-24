# Tasks

## 1. Gates y contrato de identidad

- [x] 1.1 Verificados C-03 integrado mediante PR #29 (merge `39d4231`) y C-04 mediante PR #32 (merge `60d884c`), ambos ancestros de `origin/main`; autorización explícita para iniciar Apply recibida del usuario el 23/11/2026.
- [x] 1.2 Definidos DTO e interfaces de servicio para registro por email, ingreso, confirmación manual por recepción y estado pendiente/vinculado; compilación Maven verificada y contratos revisados contra `specs/cuentas-paciente/spec.md`, sin transporte HTTP ni recuperación.

## 2. Registro e ingreso de cuentas

- [x] 2.1 Implementada V8 con entidad/repositorio de cuenta separados; `CuentasPacienteMigracionIT` pasó en PostgreSQL 18.7 (incluidos unicidad/FK/pendiente), y `AccesoPersistenciaIT` pasó con 10/10.
- [x] 2.2 Implementados registro pendiente e ingreso por email con `ServicioCredenciales`; TDD: 5/5 pruebas unitarias pasaron para hash, login pendiente, rechazo genérico, email duplicado y contraseña inválida.
- [x] 2.3 La autenticación propaga identidad separada con estado coherente pendiente/vinculada; `IdentidadCuentaPacienteTest` pasó 4/4 y confirmó que los roles internos siguen siendo solo recepción y odontología.

## 3. Vinculación manual y autorización propia

- [x] 3.1 Implementada confirmación protegida por recepción; 5/5 pruebas unitarias pasaron y `AccesoPersistenciaIT` verificó en PostgreSQL vínculo manual, ingreso vinculado y rechazo de segunda cuenta por ficha (11/11 IT).
- [x] 3.2 Implementado `AutorizadorCuentaPaciente`; TDD: 4/4 pruebas pasaron para identidad propia, cuenta pendiente, propietario ajeno e identidad ausente, con denegación genérica.
- [x] 3.3 Verificado el límite por tipos: clínica, datos administrativos, agenda y asignación requieren `Identidad` interna; `AccesoPacienteNoClinicoTest` y `ServicioAutorizacionTest` pasaron (12/12), sin rol ni permiso paciente interno.

## 4. Integración y documentación

- [x] 4.1 Documentado en `backend/README.md` el contrato de registro, ingreso genérico, vínculo manual, autorización y límites, consistente con los DTO y specs; no incluye recuperación.
- [x] 4.2 Verificada la suite unitaria (114/114) y C-16 con PostgreSQL 18.7: `CuentasPacienteMigracionIT` 1/1 y `AccesoPersistenciaIT` 11/11. `clean verify` detectó expectativas previas de V7 obsoletas; se actualizaron conteo/tablas y fixture de historial futuro a V9. Los IT afectados pasaron luego: `ArranqueIT` 22/22, `RecursosMigracionIT` 2/2 y `SchemaIT` 30/30 (54/54). Solo se usaron datos ficticios.

## Workflow follow-up

- Vincular el PR, comprobar CI y revisión conforme a CONTRIBUTING.md.
- Registrar Verify con evidencia real y archivar mediante el flujo OpenSpec correspondiente después de aprobar la revisión.
