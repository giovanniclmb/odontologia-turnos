# Verificación de fuentes

La consigna pide que los integrantes **abran y comprueben personalmente** al menos cinco fuentes citadas, de distintos tipos, y anoten si lo que afirma el informe está en esa URL.

**Estado: 0 fuentes verificadas personalmente.** Lo que sigue incluye una segunda comprobación hecha por el agente, que no reemplaza esa tarea ni debe presentarse como revisión del equipo. Para el video se pueden exponer las diferencias aclarando quién las encontró y qué comprobó cada integrante.

## Segunda comprobación del agente (25/11/2026)

Se intentó abrir las ocho fuentes que el relevamiento inicial (hecho con Codex) había listado; siete respondieron y AgendaPro devolvió HTTP 403. El texto publicado ese día se contrastó con el informe; el detalle por fuente está en su [sección correspondiente](../discovery/informe-discovery.md#segunda-comprobación-del-agente-25112026). No se crearon cuentas, no se enviaron formularios ni se copiaron reseñas o datos de pacientes.

## Diferencias que requieren decisión

El informe conserva sus fichas, matrices y puntajes. Para cada fila, el equipo decide Continuar o Ajustar.

| N.º | Fuente | Afirmación afectada | Diferencia observada | Dónde impacta | Decisión |
| --- | --- | --- | --- | --- | --- |
| 1 | Dentalink, manual | El manual no prueba disponibilidad conjunta; asignación simultánea de sillón y profesional: No evidenciado | La sección plegada «Cruce con Recursos» declara que se valida la disponibilidad de profesional y recurso en la agenda online. No habla de sillón ni de concurrencia | Ficha 01, verificación de fuentes, sección C y revisar la matriz T | Pendiente |
| 2 | DentApp, inicio | Importe, prueba y frecuencia de backup: No evidenciado | Hoy publica precios, 30 días de prueba y backups diarios | Ficha 02 y matrices S y E | Pendiente |
| 3 | DentApp, inicio | Recordatorios por WhatsApp, sin distinguir planes | Email en el plan Profesional; WhatsApp desde Clínica Plus. También publica API y webhooks en Empresarial | Ficha 02 y matriz I | Pendiente |
| 4 | Clinic Cloud, tarifas | Adopción: No evidenciado; adicional de 10 EUR por agenda | Declara más de 3.000 consultas y clínicas en España; el adicional no se localizó | Ficha 09 | Pendiente |
| 5 | Dentify, portada | Se descartó un importe anual no corroborado | Ofrece modalidad anual con dos meses gratis, sin importe visible | Ficha 03 | Pendiente |
| 6 | Dentidesk, términos | Posible cargo por odontólogo adicional | El texto dice que generará un cargo según el plan | Ficha 06 | Pendiente |
| 7 | AgendaPro, reserva | Servicios con duraciones y botón Agendar | La página respondió HTTP 403; no se pudo recomprobar | Verificación de fuentes | Pendiente |

No consta si cada diferencia existía el 17/11/2026 o apareció después. En Dentalink, la página está fechada el 12/06/2026 y la sección está plegada.

## Planilla personal

Completar solo después de abrir la URL. Alcanzan cinco filas de tipos distintos. En «Hallazgo» anotar qué se vio; en «Corrección», qué se cambió en el informe o por qué no se cambió.

| Tipo | Fuente y URL | Qué buscar | Integrante | Fecha | Hallazgo | Corrección |
| --- | --- | --- | --- | --- | --- | --- |
| Sitio oficial | [DentApp](https://www.dentapp.ar/software-odontologico-argentina) y [sus planes](https://www.dentapp.ar/) | Proveedor, integraciones, precios, prueba y backups | | | | |
| Página de precios | [tab32](https://tab32.com/pricing/) | Importe, prueba y cargos por uso | | | | |
| Centro de ayuda | [Dentalink](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online) | Desplegar «Cruce con Recursos» | | | | |
| Términos de uso | [Dentidesk](https://app.dentidesk.com/terminos.php) | Puntos 2.2, 2.4, 2.8 y 7.5 a 7.6 | | | | |
| Interfaz pública | [AgendaPro](https://agendapro.com/site/ar/odontologiaor/239004) | Servicios y duraciones; anotar si responde 403 | | | | |
| Página de precios | [Clinic Cloud](https://clinic-cloud.com/tarifas) | Importes, descuento, odontograma y agendas | | | | |
| Sitio y preguntas frecuentes | [Dentify](https://www.dentify.com.ar/) | País, cifrado, importes y modalidad anual | | | | |
| Producto | [Odontoly](https://odontoly.com/) | Complementos «próximamente» y alcance del cifrado | | | | |

Al completar las filas, actualizar la sección «Verificación de fuentes» desde [compilar_investigacion.py](../discovery/compilar_investigacion.py), regenerar el PDF y revisarlo. Los cambios hechos solo en el Markdown se pierden al volver a generarlo.
