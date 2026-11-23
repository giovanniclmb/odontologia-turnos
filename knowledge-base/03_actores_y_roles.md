# Actores y roles

## Actores del sistema

| Actor | Descripción | Interacción aprobada |
| --- | --- | --- |
| Recepción/administración | Equipo que coordina atención y seguimiento | Agenda, bloqueos, asignación de huecos, pacientes y confirmaciones. Lectura clínica y administración de asignaciones paciente-profesional. |
| Odontólogo | Profesional que atiende pacientes | Opera el sistema; gestiona datos administrativos de pacientes dentro de su alcance y lee/edita la ficha clínica de pacientes asignados. |
| Paciente | Usuario que recibe la atención | Recibe emails de turnos y avisos, cancela por enlace y gestiona su inscripción mediante una cuenta. |

Los cuatro integrantes del trabajo académico no equivalen a cuatro usuarios del consultorio. Tampoco se crea un rol de superadministrador a partir de la palabra administración.

## RBAC y matriz de permisos

Esta tabla registra requisitos de acceso, no una implementación de autenticación ni una política completa. Para pacientes, el alcance aprobado es global para recepción y limitado a los pacientes asignados al profesional de odontología. No se infiere acceso clínico global a ningún odontólogo.

| Recurso u operación | Recepción/administración | Odontólogos | Pacientes |
| --- | --- | --- | --- |
| Crear turnos internos | Aprobado como equipo | Aprobado como equipo | Fuera del MVP |
| Reprogramar | Aprobado como equipo | Aprobado como equipo | Fuera del MVP |
| Cancelar | Aprobado como equipo | Aprobado como equipo | Su turno, por enlace hasta 48 horas antes |
| Consultar agenda de sillón | Aprobado para el equipo | Aprobado para el equipo | Acceso no definido |
| Bloquear sillón y/o profesional por período | Aprobado si no afecta turnos existentes | No definido específicamente | Fuera del MVP |
| Gestionar inscripción en lista de espera | No definido específicamente | No definido específicamente | Propia, mediante cuenta |
| Confirmar quién toma un hueco y asignarlo | Aprobado | No definido específicamente | Fuera del MVP |
| Leer ficha clínica | Aprobado; cualquier paciente del sitio | Aprobado; solo pacientes asignados al profesional | Acceso no definido |
| Buscar/listar datos administrativos de pacientes | Aprobado; todo el sitio | Aprobado; solo pacientes asignados al profesional | Acceso no definido |
| Editar antecedentes | No permitido | Aprobado; solo pacientes asignados al profesional | No permitido |
| Crear notas clínicas | No permitido | Aprobado; solo pacientes asignados al profesional | No permitido |
| Editar nota clínica | No permitido | Solo el odontólogo autor, mientras siga asignado al paciente | No permitido |
| Registrar confirmaciones manuales | Aprobado | No definido específicamente | Fuera del MVP |
| Consultar listado por confirmar | Aprobado | No definido específicamente | Fuera del MVP |
| Crear/consultar/editar datos administrativos del paciente | Aprobado; alcance global del sitio | Aprobado; solo pacientes asignados, salvo el alta que deja al paciente sin asignación | Acceso no definido |
| Asignar o quitar profesionales de un paciente | Aprobado | No permitido | Acceso no definido |
| Eliminar pacientes, fichas o notas | No permitido en C-04 | No permitido en C-04 | No definido |
| Configurar intervalo, prestaciones o recursos | Pendiente | Pendiente | No definido |
| Crear usuarios o asignar permisos | Pendiente | Pendiente | No definido |

La palabra gestionar no concede permisos de eliminación ni administración de cuentas. El alcance de pacientes de C-04 está resuelto en PA-05; las operaciones de otros recursos y cuentas continúan sujetas a PA-05 y PA-14 de [preguntas abiertas](10_preguntas_abiertas.md).

## Acceso del paciente y del equipo

El acceso interno del equipo está implementado según C-03. El enlace del email permite cancelar el turno asociado dentro del plazo. La cuenta permite gestionar la inscripción propia en lista de espera. Siguen pendientes el registro y la vinculación de cuentas de paciente y sus permisos por registro, además de la protección y vigencia del enlace (PA-16 y PA-17).

## Regla clínica que debe verificarse

Recepción puede leer la ficha de cualquier paciente del sitio pero no modificarla. Los odontólogos leen y editan antecedentes de pacientes asignados; solo el odontólogo autor puede editar el contenido de una nota, mientras mantenga la asignación. Ningún perfil recibe permiso de eliminación por inferencia. La autorización se comprueba en el backend, además de cualquier control de interfaz.

La comprobación de permisos necesita alcanzar el punto real de modificación de datos, además de la interfaz. Esto es un criterio de verificación de la regla aprobada, no la selección de un mecanismo de seguridad.

## Fuentes y navegación

[Discovery aprobado](../docs/discovery/discovery.md), puntos 2 y 7; [respuestas estructuradas](../docs/discovery/decisiones-aprobadas.json), `roles` y `use_cases`. Consultar RN-CL-01, RN-CL-02, RN-AG-09, RN-AG-10, RN-PA-01, RN-PA-02 y RN-LE-03. [Reglas de negocio](05_reglas_de_negocio.md) · [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md)
