# Reflexión sobre el trabajo

Desarrollo de Software - Trabajo Global. 

Integrantes: Giovanni Colombo, Sofia Martinez, Luciano Izquierdo Cafici y Fabiano Ramos. 

## 1. ¿Qué información del Discovery resultó incorrecta y cómo la detectamos?

La información que resultó incorrecta o desactualizada es la marcada en las diferencias. Por ejemplo, el informe decía que el manual de Dentalink no probaba la disponibilidad conjunta de profesional y recurso, y sí la declara; en DentApp figuraban como no evidenciados los precios, la prueba y los respaldos que están publicados; y en Dentify se había descartado el precio anual publicado.

Esas diferencias las detectó la revisión de otro agente sobre la información relevada. Además, revisamos manualmente cinco fuentes: Giovanni revisó DentApp y tab32; Sofia, Dentalink; Fabi, Dentidesk; y Luciano, AgendaPro. En tab32 se marcó una diferencia de precio cercana a $400, que correspondía al costo estimado de otro producto, Open Dental. En Dentidesk se verificó que la prueba es de 15 días, como indicaba el informe; los 30 días son el plazo para pedir los datos después de la baja. Las otras tres revisiones no encontraron diferencias.

El propio agente también informó mal dos datos en su primera comprobación. Dijo que no encontraba el adicional de 10 EUR por agenda extra de Clinic Cloud, que estaba publicado en un texto emergente, y que Dentify no mostraba el importe anual, que aparece al elegir «Anual». Lo detectó al repetir la revisión antes de corregir el informe: en Clinic Cloud se mantuvo el dato y en Dentify se agregó el precio anual.

## 2. ¿En qué etapa usamos «Ajustar»?

Usamos «Ajustar» en ciertas reglas de negocio, sobre todo en los plazos para cancelar turnos, y también al definir qué tipos de email se iban a enviar a los pacientes.

En el Discovery, el límite de cancelación pasó de 24 a 48 horas. Con ese cambio, el recordatorio enviado 24 horas antes quedaba fuera de plazo, por lo que el enlace para cancelar pasó al email que se envía al crear el turno.

Si en esos casos hubiéramos elegido «Continuar», habrían quedado plazos solapados y sin sentido, que se contradicen con el plazo de cancelación.

## 3. Especificación aprobada frente a un prompt suelto

La diferencia que notamos al implementar con una especificación aprobada (SDD) en lugar de un prompt suelto está, en primer lugar, en el orden en que se trabaja, en el respeto a las reglas de negocio planteadas y, sobre todo, en el resultado esperado. Al trabajar así, uno sabe qué resultado va a obtener gracias a la prolijidad de un proceso de desarrollo con especificaciones.

## 4. ¿Qué parte de la fundación aportó más y cuál menos?

En primer lugar, la parte que más valor aportó al ciclo OPSX fueron las reglas, porque son la fundación del proyecto: sin ellas, el proyecto es un conjunto de cosas sin sentido que se pisan entre sí. Después, el roadmap: con él, el flujo de trabajo es mucho más ordenado y conciso para los integrantes, y permite avanzar de manera escalonada con los cambios y funcionalidades que se van agregando.

Por último, sin quitarle importancia, la base de conocimiento. Es la base para tomar decisiones y plantear buena parte de la primera estructura del proyecto.

## 5. Qué hizo cada integrante y en qué etapa

**Giovanni Colombo (líder):** condujo la fundación (etapas 1 a 5) y el change académico C-01 (etapa 6). Implementó además C-02, C-06, C-07, C-08, C-10, C-19 y C-20, coordinó la integración de los cambios, revisó las fuentes de DentApp y tab32 y dirigió la documentación de la entrega.

**Sofia Martinez:** C-04 (pacientes y ficha clínica), C-09 (confirmaciones manuales), C-16 (cuenta del paciente) y C-21 (lista de espera). Revisó la fuente de Dentalink.

**Luciano Izquierdo Cafici:** C-05 (recursos e intervalo), C-15 (bloqueos), C-12 (reprogramación y cancelación en la interfaz) y C-13 (confirmaciones en la interfaz). Revisó la fuente de AgendaPro.

**Fabiano Ramos:** C-03 (acceso y permisos), C-11 (interfaz de pacientes), C-17 (transporte de email) y C-18 (cancelación por enlace). Exportó memorias de Engram al repositorio y revisó la fuente de Dentidesk.

## Uso de inteligencia artificial

Usamos agentes de IA durante el trabajo. La fundación se hizo con Codex y parte del trabajo posterior con Claude Code y OpenCode, que también participó en revisiones, correcciones y documentación de la entrega. Se usaron agentes para realizar features, revisión de PRS, integraciones y tests. Tambiénn usamos distintos agentes con diferentes efforts de acuerdo a las tareas, dando cuenta que no es necesario efforts altos para ciertas tareas. Trabajamos con el MCP de Github para todo lo relacionado al repositorio también.
