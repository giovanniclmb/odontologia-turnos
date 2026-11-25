# Protocolo de inspección por etapa

Recopilación de lo que consta en el [estado de la fundación](../../.active-orchestrator-state.json) y la [bitácora](../bitacora.md). Las frases entre comillas están copiadas de esos registros. Donde el motivo no quedó escrito dice «No consta»: solo quien decidió puede aportarlo.

## Fundación (etapas 1 a 5)

| Fase | Registro de la decisión | Lectura | Motivo registrado |
| --- | --- | --- | --- |
| Inicialización de OpenSpec | «Continuar»; «Usuario: Vamos con Discovery.» | Continuar | No consta |
| Discovery inicial | «Continuar: apruebo el resumen y generar los documentos» | Continuar | No consta |
| Ampliación de Discovery | El usuario agregó paciente, email, lista de espera y bloqueos, y cambió el límite de cancelación de 24 a 48 horas; «Apruebo el resumen y actualizar los documentos» | Cambio de alcance antes de aprobar | El cambio de plazo dejó el recordatorio fuera de término y llevó a enviar el enlace al crear el turno. El motivo de las 48 horas no consta |
| Verificación de fuentes de Discovery, 25/11 | El agente halló diferencias en una segunda comprobación; ante la pregunta de si corregirlas: «Sí, corregilas» | **Ajustar** | [Afirmaciones desactualizadas o incompletas](verificacion-fuentes.md#correcciones-aplicadas). Las halló el agente, no una revisión personal |
| Base de conocimiento | «Continuar»; «Usuario: Vamos con el siguiente paso» (el estado conserva `pending_user_review`) | Continuar | No consta |
| Roadmap | «Continuar»; «Usuario: Vamos con el siguiente paso» | Continuar | No consta. El agente corrigió dos precisiones al verificar |
| Seguimiento, `find-skill` y registro | Frases de avance a la fase siguiente | Sin elección Continuar o Ajustar rotulada | No consta |
| Instrucciones de agentes | RA-01 a RA-03: «Aprobar esta regla». RA-04: «Quiero ajustarla», con el texto sobre commits autónomos y push solo por pedido | **Ajustar** en RA-04 | El texto aprobado define la nueva regla; el porqué no consta |

## Change académico (C-01)

| Momento | Qué consta | Fuente |
| --- | --- | --- |
| Antes de redactar | Elección de C-01; separación mínima inclusiva antes y después; dominio aislado con intervalo explícito; Java 25, Maven y JUnit | Memorias 45 a 48 ([Engram](engram.md)) y KB 10 |
| Propose | Artefactos creados y pendientes de revisión | [Instantánea](../evidencias/c01-propuesta-verificacion.json) |
| Revisión del Propose | **No consta un «Ajustar».** `proposal.md` y `spec.md` archivados tienen el mismo SHA-256 que en la instantánea previa a la revisión; `design.md` y `tasks.md` cambiaron después, con la aprobación y el avance registrados | Instantánea y archivo del change |
| Apply | «El usuario aprobó la propuesta y su aplicación explícitamente» | [Diseño](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/design.md) |
| Verify y Archive | 55 pruebas en verde; archivo pedido por el usuario | [Verify](../../openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/evidence/verify.md) y bitácora |

## Correcciones registradas en otros changes

No pertenecen al change académico. Sirven si se quiere mostrar una corrección posterior a una revisión, diciendo a qué change corresponde.

| Change | Qué se corrigió | Fuente |
| --- | --- | --- |
| C-18 | La revisión del PR 44 halló tres defectos en el enlace de cancelación; Giovanni eligió que siga vigente al confirmar el turno | [Propuesta de correcciones](../../openspec/changes/archive/2026-11-25-c-18-fixes-hallazgos-pr44/proposal.md) |
| C-11 | Correcciones y verificación ampliada de la interfaz | [Evidencia](../evidencias/c11-correcciones/verify.md) |
| C-03 | Correcciones de credenciales del Issue 30 | [Evidencia](../evidencias/c03-pendientes/verify.md) |
