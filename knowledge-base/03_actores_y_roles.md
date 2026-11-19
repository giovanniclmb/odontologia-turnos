# Actores y roles

## Actores del sistema

| Actor | Descripción | Interacción aprobada |
| --- | --- | --- |
| Recepción/administración | Equipo que coordina atención y seguimiento | Agenda, bloqueos, asignación de huecos, pacientes y confirmaciones. Lectura clínica. |
| Odontólogo | Profesional que atiende pacientes | Opera el sistema; lectura y edición de antecedentes/notas |
| Paciente | Usuario que recibe la atención | Recibe emails de turnos y avisos, cancela por enlace y gestiona su inscripción mediante una cuenta. |

Los cuatro integrantes del trabajo académico no equivalen a cuatro usuarios del consultorio. Tampoco se crea un rol de superadministrador a partir de la palabra administración.

## RBAC y matriz de permisos

Esta tabla registra requisitos de acceso, no una implementación de autenticación ni una política completa. Solo se consideran permitidas las operaciones aprobadas y dentro del alcance de pacientes/recursos que se defina. No se infiere acceso clínico global a cualquier paciente.

| Recurso u operación | Recepción/administración | Odontólogos | Pacientes |
| --- | --- | --- | --- |
| Crear turnos internos | Aprobado como equipo | Aprobado como equipo | Fuera del MVP |
| Reprogramar | Aprobado como equipo | Aprobado como equipo | Fuera del MVP |
| Cancelar | Aprobado como equipo | Aprobado como equipo | Su turno, por enlace hasta 48 horas antes |
| Consultar agenda de sillón | Aprobado para el equipo | Aprobado para el equipo | Acceso no definido |
| Bloquear sillón y/o profesional por período | Aprobado si no afecta turnos existentes | No definido específicamente | Fuera del MVP |
| Gestionar inscripción en lista de espera | No definido específicamente | No definido específicamente | Propia, mediante cuenta |
| Confirmar quién toma un hueco y asignarlo | Aprobado | No definido específicamente | Fuera del MVP |
| Leer ficha clínica | Aprobado; alcance de pacientes pendiente | Aprobado; alcance de pacientes pendiente | Acceso no definido |
| Editar antecedentes y notas | No permitido | Aprobado | No permitido por la regla de edición exclusiva |
| Registrar confirmaciones manuales | Aprobado | No definido específicamente | Fuera del MVP |
| Consultar listado por confirmar | Aprobado | No definido específicamente | Fuera del MVP |
| Gestionar datos administrativos del paciente | Incluido a nivel de equipo; detalle pendiente | Incluido a nivel de equipo; detalle pendiente | Acceso no definido |
| Eliminar pacientes, fichas o notas | Pendiente; no se otorga por inferencia | Pendiente; no se otorga por inferencia | No definido |
| Configurar intervalo, prestaciones o recursos | Pendiente | Pendiente | No definido |
| Crear usuarios o asignar permisos | Pendiente | Pendiente | No definido |

La palabra gestionar no define por sí sola permisos de eliminación o de administración de cuentas. El catálogo completo de operaciones se resolverá en PA-05 y PA-14 de [preguntas abiertas](10_preguntas_abiertas.md).

## Acceso del paciente y del equipo

El enlace del email permite cancelar el turno asociado dentro del plazo. La cuenta permite gestionar la inscripción propia en lista de espera. Quedan por definir el ingreso del equipo, el registro y la vinculación de cuentas, sus permisos por registro y la protección y vigencia del enlace (PA-05, PA-16 y PA-17).

## Regla clínica que debe verificarse

Recepción puede leer la ficha pero no modificar antecedentes o notas. Solo los odontólogos pueden editar la información clínica. La forma técnica de identificar al usuario y aplicar esa restricción no está elegida; no se presupone JWT, sesiones, un proveedor externo ni roles de base de datos.

La comprobación de permisos necesita alcanzar el punto real de modificación de datos, además de la interfaz. Esto es un criterio de verificación de la regla aprobada, no la selección de un mecanismo de seguridad.

## Fuentes y navegación

[Discovery aprobado](../docs/discovery/discovery.md), puntos 2 y 7; [respuestas estructuradas](../docs/discovery/decisiones-aprobadas.json), `roles` y `use_cases`. Consultar RN-CL-01, RN-CL-02, RN-AG-09, RN-AG-10, RN-PA-01, RN-PA-02 y RN-LE-03. [Reglas de negocio](05_reglas_de_negocio.md) · [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md)
