# Design

## Context

El backend actual implementa autenticación y autorización internas a nivel de servicio, sin transporte HTTP. Sus roles son recepción y odontología; C-04 aporta la ficha de paciente y sus identificadores. La cuenta del paciente requiere una identidad separada y una relación explícita con esa ficha. Ver proposal.md para la motivación y `specs/cuentas-paciente/spec.md` para el contrato observable.

## Goals / Non-Goals

**Goals:**
- Propagar una identidad de paciente con estado de vinculación y referencia estable a la ficha una vez confirmada.
- Mantener las comprobaciones de propiedad en servicios backend para que un identificador de inscripción recibido no permita operar sobre otra persona.
- Permitir a recepción confirmar el vínculo contra una ficha existente.

**Non-Goals:**
- Incorporar un transporte de autenticación, rutas HTTP, JWT, sesiones o proveedor externo.
- Incorporar recuperación de cuenta, verificación por email, acceso clínico, operaciones de agenda o ciclo de vida de la inscripción.

## Decisions

### Separar las cuentas de paciente de los usuarios internos

La cuenta de paciente tendrá identidad y permisos propios, separados de los roles internos de recepción y odontología. Ingresará con email y contraseña; el email será único entre cuentas, sin requerir que sea único en las fichas ni usarlo para vincularlas. Se reutilizará el hash y la política de contraseña ya aprobados para usuarios internos, sin agregar `paciente` a los roles internos ni heredar sus permisos. Se descarta ampliar el usuario interno porque mezclaría el acceso clínico-administrativo con la identidad del paciente.

### Usar una vinculación explícita y pendiente por defecto

El registro crea la cuenta sin `pacienteRef`; el ingreso puede autenticar esa cuenta en estado pendiente, pero la autorización para operaciones de lista exige una referencia confirmada. Recepción realiza la vinculación mediante una operación protegida. La referencia a paciente es única y nullable: una ficha puede tener cero o una cuenta y una cuenta puede estar pendiente o vinculada a una ficha. No se usa coincidencia de email para seleccionar la ficha, dado que el email del paciente no es único.

El email no requiere una verificación por correo dentro de C-16: la confirmación manual de recepción establece el vínculo con la ficha. El ingreso fallido usa un rechazo genérico para no revelar si existe una cuenta para el email presentado.

### Hacer cumplir el alcance en el backend

Los servicios reciben una identidad de paciente autenticada y resuelven la inscripción junto con su paciente propietario. La autorización compara esa propiedad con el paciente vinculado antes de devolver o modificar datos. Se deniega por defecto cualquier operación no incluida en la capability.

### Mantener el contrato sin transporte HTTP

Las operaciones de registro, ingreso, confirmación de vínculo y autorización se definen como contratos de servicio/DTO, siguiendo el backend actual. JWT, sesión y proveedor quedan para un change de transporte posterior y no alteran la decisión de identidad ni propiedad.

### Coordinar persistencia después de C-04

La persistencia de cuenta referenciará el identificador estable del paciente definido por C-04. Su migración se agregará únicamente después de integrar C-04 y se coordinará con la secuencia Flyway al integrar; no se modifica ni se reescribe una migración ya aplicada.

## Risks / Trade-offs

- [Riesgo] Una identidad autenticada pendiente puede confundirse con una cuenta habilitada → incluir el estado pendiente en la identidad y exigir vínculo en cada operación de lista.
- [Riesgo] Una autorización delegada al frontend podría permitir operar sobre otra inscripción → resolver y comprobar la propiedad en el servicio backend para cada operación.
- [Riesgo] El vínculo podría apuntar a una ficha incorrecta → la confirmación debe seleccionar una ficha existente explícitamente y quedar bajo permiso de recepción; el email no se considera prueba de identidad.
- [Riesgo] La integración de C-04 puede cambiar el momento de incorporar la referencia → bloquear implementación y migración de C-16 hasta que C-04 esté integrada.
- [Trade-off] La revisión manual añade trabajo operativo y demora el acceso a la lista, pero evita vincular automáticamente fichas usando un dato no único.

## Migration Plan

1. No implementar ni aplicar migraciones hasta que C-03 y C-04 estén integrados y se cumplan las aprobaciones de gobernanza CRITICO.
2. Incorporar la representación de cuenta paciente y su referencia a paciente con una migración Flyway nueva, coordinada con el historial integrado.
3. Verificar el escenario pendiente/vinculado y la autorización de propiedad en pruebas con PostgreSQL real para la persistencia.
4. Si la migración aún no fue aplicada fuera de entornos descartables, revertir el cambio junto con la versión de aplicación. Si ya fue aplicada a una base compartida, no editarla ni eliminar datos; preparar una migración correctiva coordinada.

## Open Questions

- La selección de JWT, sesión o proveedor de identidad queda fuera de C-16 y deberá resolverse antes de implementar el transporte que lo requiera.
- La política y canal de recuperación de cuenta no están acordados; este change no expone una operación de recuperación.
