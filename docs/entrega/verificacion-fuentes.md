# Verificación de fuentes

La consigna pide que los integrantes **abran y comprueben personalmente** al menos cinco fuentes citadas, de distintos tipos, y anoten si lo que afirma el informe está en esa URL.

**Estado: 5 fuentes revisadas por cuatro integrantes**, según los resultados que Giovanni Colombo transmitió el 25/11/2026 ([planilla](#planilla-personal)). La segunda comprobación del agente se registra aparte: no reemplaza esa revisión ni debe presentarse como trabajo del equipo. Para el video se pueden exponer las diferencias aclarando quién las encontró y qué comprobó cada integrante.

## Segunda comprobación del agente (25/11/2026)

Se intentó abrir las ocho fuentes que el relevamiento inicial (hecho con Codex) había listado; siete respondieron y AgendaPro devolvió HTTP 403. El texto publicado ese día se contrastó con el informe; el informe detalla en su [sección correspondiente](../discovery/informe-discovery.md#segunda-comprobación-del-agente-25112026) las cinco que revisó el equipo, y las correcciones de las otras tres constan en la tabla siguiente. No se crearon cuentas, no se enviaron formularios ni se copiaron reseñas o datos de pacientes.

## Correcciones aplicadas

El 25/11/2026, ante la pregunta de si corregir en el informe las diferencias halladas, la respuesta fue «Sí, corregilas». Se aplicaron en las fichas, la matriz B, la sección C y la sección de verificación del informe. Se aplicaron antes de la revisión personal y no consta que esta haya confirmado cada una.

| N.º | Fuente | Decía el informe | Se observó | Corrección |
| --- | --- | --- | --- | --- |
| 1 | Dentalink, manual | No prueba disponibilidad conjunta; asignación simultánea de sillón y profesional: No evidenciado | La sección plegada «Cruce con Recursos» declara que la agenda online valida profesional y recurso. No habla de sillón ni de concurrencia | Ficha 01 y sección C. La matriz no cambia |
| 2 | DentApp, inicio | Importe, prueba y frecuencia de backup: No evidenciado | Publica precios, 30 días de prueba y backups diarios | Ficha 02; cuatro celdas de S y E; total de 1,275 a 1,475 |
| 3 | DentApp, inicio | Recordatorios por WhatsApp, sin distinguir planes | Email en Profesional; WhatsApp desde Clínica Plus. API y webhooks en Empresarial | Ficha 02. «API pública» sigue como No evidenciado: no consta acceso público |
| 4 | Clinic Cloud, tarifas | Adopción: No evidenciado | Declara más de 3.000 consultas y clínicas en España y exhibe ISO/IEC 27001 | Ficha 09 |
| 5 | Dentify, portada | No usar como vigente el anual de $25.000 | La modalidad anual publica $25.000 y $38.000 por mes | Ficha 03 |
| 6 | Dentidesk, términos | Posible cargo por odontólogo adicional | Dice que generará un cargo según el plan | Ficha 06 |
| 7 | tab32, precios | Prueba de 14 días; migración aparte | Pide email y tarjeta; migración desde $1,250; la exportación incluye el ledger | Ficha 15 |
| 8 | Odontoly, producto | Sin límite de usuarios consignado | Dos usuarios y hasta cuatro dispositivos | Ficha 05 |
| 9 | AgendaPro, reserva | Servicios con duraciones y botón Agendar | HTTP 403 | Anotado en la ficha 08; sin cambiar celdas |

No consta si cada diferencia existía el 17/11/2026 o apareció después. En Dentalink, la página está fechada el 12/06/2026 y la sección está plegada.

**Errores del propio agente.** En su primera pasada informó que el adicional de 10 EUR de Clinic Cloud no se localizaba y que Dentify no mostraba el importe anual. Ambas observaciones eran falsas: el primero está en un texto emergente y el segundo aparece al elegir «Anual». Lo detectó al repetir la revisión antes de corregir el informe.

## Planilla personal

Resultados transmitidos por Giovanni Colombo el 25/11/2026. No consta la fecha de cada apertura. «Hallazgo» recoge lo informado por quien revisó; «Corrección y contraste», lo que se cambió y lo que comprobó después el agente. Son cinco tipos de fuente distintos.

| Tipo | Fuente y URL | Integrante | Hallazgo | Corrección y contraste |
| --- | --- | --- | --- | --- |
| Sitio oficial | [DentApp](https://www.dentapp.ar/software-odontologico-argentina) | Giovanni Colombo | Sin diferencias | Sin cambios |
| Página de precios | [tab32](https://tab32.com/pricing/) | Giovanni Colombo | Diferencias de precio según el servicio; para el servicio dental, un precio más alto, cercano a $400 | Importe observado confirmado: $427 ([detalle](#tab32-importe-observado)). Se agregaron a la ficha 15 los cargos por servicio; el precio base no cambia |
| Centro de ayuda | [Dentalink](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online) | Sofia Martinez | Sin diferencias con los datos obtenidos | Sin cambios |
| Términos de uso | [Dentidesk](https://app.dentidesk.com/terminos.php) | Fabiano Ramos | La prueba es de 15 días, no de 30; después empiezan los cargos | La ficha 06 ya indicaba 15 días. El agente reabrió los términos: 15 días de prueba (2.2 y 12.1) y 30 días corridos para pedir la exportación tras el término (7.6). Se aclaró la diferencia en el informe |
| Interfaz pública | [AgendaPro](https://agendapro.com/site/ar/odontologiaor/239004) | Luciano Izquierdo Cafici | Sin diferencias, luego de simular una reserva en la app, que consideró bastante buena | Sin cambios en las celdas. Giovanni Colombo confirmó el 25/11 que Luciano detuvo la simulación antes de confirmar la reserva. El agente había recibido HTTP 403 ese día |

## tab32: importe observado

El 25/11/2026 Giovanni Colombo confirmó que el importe cercano a $400 era **$427**. El agente había reabierto la [página de precios](https://tab32.com/pricing/): $427 es la estimación mensual de **Open Dental**, otro producto, en un comparador con valores iniciales de 15 turnos por día, 70 % de pacientes asegurados y 48 semanas al año. El mismo comparador estima $179 por mes para tab32, que anuncia su sistema desde $95. La página suma aparte cargos por uso y paquetes de agentes de IA. El agente tampoco localizó un plan de tab32 cercano a $400 en el inicio ni en otras diez páginas del sitio.

Los cambios al informe se hacen desde [compilar_investigacion.py](../discovery/compilar_investigacion.py) y [competidores.json](../discovery/competidores.json), regenerando el PDF. Los cambios hechos solo en el Markdown se pierden al volver a generarlo.
