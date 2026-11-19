# Arquitectura propuesta

## Estado de la propuesta

Este archivo conserva la propuesta inicial y sus límites. Las decisiones posteriores aprobadas para C-01 y C-02 se registran en los bloques correspondientes; C-02 tiene implementación y Verify local, pendiente de archivo e integración. Los puntos de otros changes siguen siendo propuestas o preguntas abiertas, no decisiones inferidas.

## Patrones aplicados o pendientes

| Aspecto | Base aprobada | Estado de definición |
| --- | --- | --- |
| Presentación | HTML/CSS/TypeScript vanilla | Tecnología aprobada; estructura, navegación y build pendientes |
| Lógica de aplicación | Java Spring Boot | Tecnología aprobada; paquetes, capas y patrones pendientes |
| Persistencia | PostgreSQL | Tecnología aprobada; modelo físico y acceso a datos pendientes |
| Consistencia de agenda | Profesional y sillón sin solapamiento, con intervalo | Invariante aprobado; mecanismo de concurrencia pendiente |
| Autorización clínica | Recepción lee; odontólogos editan | Regla aprobada; autenticación y aplicación técnica pendientes |
| Acceso del paciente | Cancelación por enlace; cuenta para lista de espera | Registro, vinculación, permisos y protección del enlace pendientes |
| Comunicaciones | Email de creación, recordatorio y aviso de huecos | Proveedor gratuito y mecanismo de envío/programación pendientes |
| Disponibilidad | Vistas diaria/semanal y bloqueos por recepción | Reglas aprobadas; límites y concurrencia por especificar |

Separar responsabilidades visuales, de dominio y de persistencia es una propuesta de lectura del stack, no una adopción de arquitectura hexagonal, microservicios, repositorios o bibliotecas concretas.

## Estructura de directorios

Estructura documental actual relevante:

```text
docs/discovery/       fuentes, informe y decisiones aprobadas
knowledge-base/       diez documentos canónicos e índice
openspec/             estructura creada por el CLI
```

Para C-01 se aprobó `backend/` con estructura Maven, Java 25 y JUnit. El dominio puro no incluye Spring Boot ni persistencia. La ubicación del frontend y migraciones, y la versión Spring Boot, siguen pendientes para los changes correspondientes.

## Consistencia de turnos

La implementación deberá comprobar disponibilidad de profesional y sillón, duración e intervalo también ante solicitudes simultáneas. Una lectura de disponibilidad en pantalla no demuestra que el guardado proteja ambos recursos.

No se seleccionó una solución de locking, nivel de aislamiento, constraint, extensión PostgreSQL o estrategia de reintentos. El spec y el diseño del change correspondiente deberán mostrar cómo se verifica la regla. No se escribieron índices ni DDL en esta KB.

La cancelación libera inmediatamente el horario. La reprogramación conserva prestación y duración; su resultado preciso ante un fallo sigue abierto, sin prometer aquí atomicidad, compensaciones o pérdida del turno anterior como decisiones aprobadas.

Los bloqueos de recepción afectan sillón y/o profesional por período y se rechazan si afectan turnos existentes. Crear o reprogramar debe considerar estos bloqueos. La lista avisa a interesados compatibles; recepción asigna el hueco tras validar disponibilidad. El aviso no reserva ni adjudica automáticamente.

## Email y lista de espera

El email de creación entrega el enlace para cancelar hasta 48 horas antes del turno. El recordatorio se envía 24 horas antes y no ofrece cancelación cuando venció el plazo. La cuenta del paciente permite gestionar la inscripción con prestación, rango de fechas y franjas horarias, con profesional opcional.

Se requieren envíos reales a direcciones de prueba del equipo mediante una opción gratuita por definir. Proveedor, integración, programación, duplicados, reintentos y resultado de fallos quedan abiertos en PA-15 y PA-18. No se selecciona SMTP, API, cola ni scheduler. Las altas cercanas al turno y el efecto de reprogramar o cancelar sobre enlaces y emails deberán especificarse. Compatibilidad exacta de huecos y avisos cuando ya están ocupados: PA-20 y PA-21.

## Seguridad

- Autenticación: mecanismo y ciclo de acceso pendientes; no se decide JWT o sesión por defecto.
- Autorización: conservar la separación clínica aprobada; permisos por operación y alcance de pacientes pendientes.
- Validación: campos, estados y límites necesitan definición; no se inventan formatos o reglas de identidad.
- Cuenta del paciente: registro, verificación, vinculación y autorización sobre su propia inscripción pendientes (PA-16); no se otorga acceso clínico por inferencia.
- Enlace de cancelación: protección, vigencia, reutilización y límite exacto de 48 horas pendientes (PA-17).
- Datos clínicos de prueba: exclusivamente ficticios según la consigna. Las direcciones de prueba para email serán las indicadas por el equipo. No cargar pacientes reales en repositorio, ejemplos o capturas.
- Secretos: no incluir credenciales en la KB ni en archivos versionados. Nombres y carga de configuración pendientes.
- Auditoría, cifrado, backup, restauración y retención: requisitos y diseño pendientes; las declaraciones de los competidores no constituyen una política del producto.

## Variables de entorno

No hay un contrato de configuración elegido. Esta lista identifica información por definir, sin fijar nombres, valores ni servicios:

| Configuración | Necesidad conocida | Nombre y ejemplo | Sensibilidad |
| --- | --- | --- | --- |
| Conexión PostgreSQL | Persistencia elegida | Pendiente | Puede contener credenciales; no publicar valores reales |
| Acceso del backend | Spring Boot elegido | Pendiente | Depende del dato |
| Dirección del backend para la interfaz | Interacción lógica entre capas | Pendiente | Depende del despliegue |
| Servicio de email y destinatarios de prueba | Envíos reales con presupuesto cero | Pendiente | Credenciales privadas; no versionar direcciones sin acuerdo del equipo |
| Intervalo de la sede | Regla funcional aprobada | Valor y almacenamiento pendientes | Configuración de negocio; no es un secreto por definición |

No se decide que el intervalo sea una variable de entorno: su persistencia y quién puede cambiarlo están abiertos.

## Operación y diseño visual

Presupuesto externo cero; email obligatorio mediante proveedor gratuito por definir. La base requiere un entorno de ejecución, pero Docker, hosting, CI, dominio y proveedores no están seleccionados. No se instala infraestructura en esta fase.

La política visual permanece diferida desde el plan de fundación. Generar esta KB no aprueba una identidad, mockup o diseño de pantalla. Cuando corresponda trabajo visual, se resolverá esa política mediante el flujo ya instalado.

## Fuentes y navegación

[Decisiones aprobadas](../docs/discovery/decisiones-aprobadas.json), `constraints`, `rules` y `open_questions`; [modelo conceptual](04_modelo_de_datos.md); PA-01 a PA-09 y PA-15 a PA-21 en [preguntas abiertas](10_preguntas_abiertas.md).

## Definición técnica acotada de C-02

Decisiones aprobadas el 19/11/2026: Boot 4.1.2, Java 25/Maven, JPA/Hibernate con entidades separadas del dominio, Flyway SQL y PostgreSQL 18.7. El esquema odontologia contiene únicamente paciente, profesional, sillon y prestacion con UUID generado en aplicación y codigo único por tipo. Las cuentas, campos clínicos, catálogos completos, intervalo y turnos se incorporan en sus changes.

Compose prepara la base local separada; el usuario aporta credenciales privadas. Testcontainers crea bases temporales y el Environment de las IT no hereda configuración Spring del anfitrión. Flyway usa migrador distinto del usuario ordinario, ambos sin superusuario; Hibernate valida el esquema. Versiones consecutivas coordinadas por Giovanni al integrar, sin out-of-order ni edición de migraciones ya aplicadas a bases compartidas. Credenciales en URL e identidades efectivas incorrectas se rechazan antes de conectar; historial futuro o checksum distinto bloquea el arranque.

C-02 está implementado y verificado en su rama, pendiente de archivo e integración. Su backend no-web no publica endpoints. Estas decisiones actualizan los pendientes técnicos anteriores únicamente para este alcance; no aprueban APIs, autenticación ni despliegue productivo. Consultar backend/README.md para las variables DB_* y los comandos comprobados.
