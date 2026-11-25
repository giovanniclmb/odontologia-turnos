# Verificación de fuentes

La consigna pide que los integrantes **abran y comprueben personalmente** al menos cinco fuentes citadas, de distintos tipos, y anoten si lo que afirma el informe está en esa URL.

**Estado: 0 fuentes verificadas personalmente.** Lo que sigue incluye una segunda comprobación hecha por el agente, que no reemplaza esa tarea ni debe presentarse como revisión del equipo. Para el video se pueden exponer las diferencias aclarando quién las encontró y qué comprobó cada integrante.

## Segunda comprobación del agente (25/11/2026)

Se intentó abrir las ocho fuentes que el relevamiento inicial (hecho con Codex) había listado; siete respondieron y AgendaPro devolvió HTTP 403. El texto publicado ese día se contrastó con el informe; el detalle por fuente está en su [sección correspondiente](../discovery/informe-discovery.md#segunda-comprobación-del-agente-25112026). No se crearon cuentas, no se enviaron formularios ni se copiaron reseñas o datos de pacientes.

## Correcciones aplicadas

El 25/11/2026, ante la pregunta de si corregir en el informe las diferencias halladas, la respuesta fue «Sí, corregilas». Se aplicaron en las fichas, la matriz B, la sección C y la sección de verificación del informe. Quien revise personalmente puede confirmarlas o volver a ajustarlas.

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

Completar solo después de abrir la URL. Alcanzan cinco filas de tipos distintos. En «Hallazgo» anotar qué se vio; en «Corrección», qué se cambió en el informe o por qué no se cambió.

| Tipo | Fuente y URL | Qué buscar | Integrante | Fecha | Hallazgo | Corrección |
| --- | --- | --- | --- | --- | --- | --- |
| Sitio oficial | [DentApp](https://www.dentapp.ar/software-odontologico-argentina) y [sus planes](https://www.dentapp.ar/) | Proveedor, integraciones, precios, prueba y backups | | | | |
| Página de precios | [tab32](https://tab32.com/pricing/) | Importe, prueba y cargos por uso | | | | |
| Centro de ayuda | [Dentalink](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online) | Desplegar «Cruce con Recursos» | | | | |
| Términos de uso | [Dentidesk](https://app.dentidesk.com/terminos.php) | Puntos 2.2, 2.4, 2.8 y 7.5 a 7.6 | | | | |
| Interfaz pública | [AgendaPro](https://agendapro.com/site/ar/odontologiaor/239004) | Servicios y duraciones; anotar si responde 403 | | | | |
| Página de precios | [Clinic Cloud](https://clinic-cloud.com/tarifas) | Importes, descuento, odontograma y el texto emergente de «Agendas» | | | | |
| Sitio y preguntas frecuentes | [Dentify](https://www.dentify.com.ar/) | País, cifrado e importes; elegir «Anual» | | | | |
| Producto | [Odontoly](https://odontoly.com/) | Complementos «próximamente» y alcance del cifrado | | | | |

Al completar las filas, actualizar la sección «Verificación de fuentes» desde [compilar_investigacion.py](../discovery/compilar_investigacion.py), regenerar el PDF y revisarlo. Los cambios hechos solo en el Markdown se pierden al volver a generarlo.
