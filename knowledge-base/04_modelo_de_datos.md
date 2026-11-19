# Modelo de datos

## Dominios

- **Pacientes y ficha clínica.** Identificar al paciente en la operación del consultorio y conservar antecedentes y notas.
- **Agenda.** Registrar turnos por prestación y asignarlos a un profesional y un sillón o box de la sede.
- **Disponibilidad.** Representar bloqueos de sillones, profesionales o ambos por período.
- **Acceso y comunicaciones del paciente.** Relacionar cuentas con pacientes, enlaces con turnos y emails con sus destinatarios.
- **Lista de espera.** Registrar preferencias, relacionar huecos con avisos y representar la asignación que realiza recepción.
- **Equipo y configuración.** Distinguir al personal del consultorio y representar el intervalo común entre turnos.

## Relaciones conceptuales

El diagrama representa asociaciones del dominio. Su dirección expresa cómo se lee la asociación y las cardinalidades quedan abiertas, salvo la asignación obligatoria de un profesional y un sillón a cada turno.

```mermaid
flowchart LR
    paciente[Paciente] -->|tiene información en| ficha[Ficha clínica]
    ficha -->|incluye| antecedentes[Antecedentes]
    ficha -->|incluye| notas[Notas clínicas]
    turno[Turno] -->|corresponde a| paciente
    turno -->|reserva| profesional[Profesional]
    turno -->|reserva| sillon[Sillón o box]
    turno -->|toma la duración de referencia de| prestacion[Prestación]
    turno -->|respeta el intervalo de| configuracion[Configuración de sede]
    bloqueo[Bloqueo por período] -->|puede afectar a| profesional
    bloqueo -->|puede afectar a| sillon
    cuenta[Cuenta de paciente] -->|corresponde a| paciente
    cuenta -->|permite gestionar| inscripcion[Inscripción y preferencias]
    inscripcion -->|indica| prestacion
    inscripcion -->|puede indicar| profesional
    enlace[Enlace de cancelación] -->|se refiere al| turno
    correo[Email de turno] -->|se refiere al| turno
    correo -->|se dirige al| paciente
    aviso[Aviso por email] -->|comunica un| hueco[Hueco disponible]
    aviso -->|corresponde a una| inscripcion
```

## Entidades y conceptos

| Concepto | Información respaldada por el relevamiento | Pendiente de especificación |
| --- | --- | --- |
| Paciente | La gestión de pacientes y la consulta de sus datos forman parte del MVP. El turno corresponde a la atención de un paciente. | Campos mínimos, identificador y operaciones de gestión (PA-05 a PA-07). |
| Ficha clínica, antecedentes y notas | La ficha cubre antecedentes y notas. Recepción puede leerla, y los odontólogos pueden leerla y editarla. | Contenido de cada componente y relaciones exactas entre paciente, ficha, antecedentes y notas (PA-06 y PA-07). |
| Profesional y usuario interno | El profesional atiende turnos como recurso obligatorio. El personal opera con capacidades distintas según sea recepción/administración u odontólogo. | Relación entre cuenta y profesional, identificadores, autenticación y alcance de permisos (PA-05 y PA-07). |
| Sillón o box | Recurso físico obligatorio de la única sede. Su ocupación debe comprobarse en la agenda. | Cantidad, identificador, atributos y reglas de disponibilidad (PA-04, PA-07 y PA-08). |
| Prestación | Aporta la duración de referencia al crear un turno. Reprogramar conserva la prestación y la duración del turno. | Campos del catálogo, forma de administrarlo y relación con los turnos guardados (PA-07 y PA-14). |
| Turno | Fecha, hora, profesional, sillón o box, prestación y duración efectiva son datos de la reserva. La confirmación manual y la cancelación tienen efectos en el flujo de agenda. | Identificador, estados, transiciones y representación de las confirmaciones (PA-02 y PA-07). |
| Configuración de sede | Hay una única sede y un único intervalo configurable común. El valor inicial registrado en las fuentes es `null` (pendiente). | Minutos iniciales, límites, permisos y aplicación de cambios en la configuración (PA-01). |
| Bloqueo de recursos | Período y sillón, profesional o ambos. Se rechaza si afecta turnos existentes. | Límites de período, modificación o desbloqueo y permisos detallados (PA-04 y PA-19). |
| Cuenta de paciente | Se usa para gestionar la inscripción propia. | Registro, verificación, vínculo con paciente y alcance (PA-16). |
| Enlace de cancelación | Se refiere a un turno y se envía en el email de alta. La cancelación tiene un plazo de hasta 48 horas antes. | Vigencia, protección, reutilización y efecto de los cambios del turno (PA-17 y PA-18). |
| Email de turno | Destinatario y turno asociado para el email de alta y el recordatorio. | Verificación de destinatarios, proveedor, programación, registro de entrega y duplicados (PA-15 y PA-18). |
| Inscripción en lista de espera | Prestación, rango de fechas y franjas horarias, con profesional opcional. | Ciclo de la inscripción y criterios exactos para vincularla con huecos (PA-16 y PA-20). |
| Hueco y aviso | El aviso comunica un hueco liberado a interesados; recepción confirma quién toma el hueco y realiza la asignación. | Representación del hueco, relación con preferencias y turnos, y manejo de huecos ocupados (PA-20 y PA-21). |

## Reglas que condicionan el modelo

- Un alta o reprogramación debe validar la disponibilidad del profesional y del sillón o box, incluidos sus bloqueos, además del intervalo de la sede.
- La duración efectiva debe estar disponible al reprogramar, incluso si se ajustó en el alta. Cómo representarla en persistencia queda abierto.
- La cancelación libera el horario de inmediato. Qué estado y qué representación se emplean queda abierto.
- El esquema físico de PostgreSQL, los identificadores, las cardinalidades restantes, las restricciones de base de datos y los índices se definirán al especificar el cambio correspondiente (PA-07).

## Datos iniciales

La sede única forma parte del alcance aprobado. La cantidad de profesionales y sillones, las prestaciones iniciales y los minutos del intervalo siguen pendientes (PA-01, PA-08 y PA-14). Los ejemplos, pruebas y capturas utilizan pacientes ficticios. Para demostrar el envío real de emails se usarán las direcciones de prueba indicadas por el equipo.

Fuentes y referencias. [Ampliación aprobada](../docs/discovery/ampliacion-paciente.md) · [Discovery, secciones 2, 3, 5, 7 y 9](../docs/discovery/discovery.md) · [Decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json) · [Reglas de negocio](05_reglas_de_negocio.md) · [Preguntas abiertas](10_preguntas_abiertas.md)
