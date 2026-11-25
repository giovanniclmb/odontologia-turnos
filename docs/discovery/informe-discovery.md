# Investigación de mercado de gestión odontológica en Argentina

Desarrollo de Software - Trabajo Global

Integrantes: Giovanni Colombo, Sofia Martinez, Luciano Izquierdo Cafici y Fabiano Ramos.

Fecha de entrega: 25 de noviembre de 2026. Consulta de fuentes y aprobación del Discovery: 17 de noviembre de 2026.

Equipo de cuatro integrantes con autorización docente informada por el equipo.

<!-- salto-de-pagina -->

## Resumen ejecutivo

Se investigaron 15 soluciones con páginas oficiales abiertas, priorizando oferta argentina, luego soluciones latinoamericanas y referencias internacionales. La muestra cubre sistemas dentales y una plataforma multisector comercializada para odontología. El orden refleja pertinencia documental y geográfica, no participación de mercado ni calidad medida.

Agenda, ficha del paciente, odontograma y administración aparecen repetidamente en la oferta dental. La reserva por enlace y los recordatorios también están extendidos en las páginas revisadas. Por eso esas prestaciones, por sí solas, no acreditan una diferenciación comercial. Las capacidades de asignación simultánea de profesional y sillón, las condiciones de exportación y el costo completo requieren comprobaciones adicionales en buena parte de la muestra.

Se encontraron precios indexados distintos de las aperturas actuales, promociones cambiantes, módulos dependientes del plan y prestaciones anunciadas como próximas. El relevamiento conserva estas limitaciones. No se realizó una auditoría técnica, prueba de carga, compra ni demo con cuenta.

Tras la investigación y las preguntas por menú, el usuario aprobó el resumen de once puntos y su ampliación (revisión 2). El MVP incluye agenda de sillón diaria/semanal, bloqueos por recepción, ficha clínica sencilla, disponibilidad conjunta de profesional y sillón y confirmaciones manuales. El paciente cancela por enlace hasta 48 horas antes y gestiona con cuenta la lista de espera; recepción asigna los huecos. Se envía email al crear con enlace y recordatorio 24 horas antes, sin opción de cancelar cuando el plazo venció. Se requieren envíos reales a direcciones de prueba del equipo mediante proveedor gratuito aún por definir. El stack elegido es HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL; entrega 25/11/2026 y presupuesto externo cero. A ese corte seguían pendientes el intervalo inicial, los detalles técnicos y la revisión personal de cinco fuentes. Este informe es un corte al cierre del Discovery (revisión 2, consolidada el 19/11/2026): a esa fecha no se había implementado código ni seleccionado un change. El estado posterior consta en README.md y CHANGES.md; la revisión personal y la segunda comprobación del agente, informadas el 25/11/2026, figuran en «Verificación de fuentes».

## Alcance y método

Se consultaron páginas puntuales, sin crawling masivo ni evasión de bloqueos. Solo se incorporó al núcleo comparativo información recuperada de fuentes oficiales; los extractos de buscador se usaron para localizar páginas, no como sustituto de una apertura. Si una información no fue demostrada en las fuentes examinadas, se escribió «No evidenciado». Eso significa desconocimiento en esta muestra, no ausencia en el producto.

Se distingue **Documentado** (manual con procedimiento, interfaz pública o condición textual verificable) de **Declarado** (afirmación comercial del proveedor). Ninguna etiqueta equivale a prueba operativa independiente o garantía de cumplimiento. El domicilio publicado no acredita por sí solo el origen histórico de la empresa; se marca la diferencia donde corresponde.

No se asume que WhatsApp para soporte, compartir un enlace, recordatorios semiautomáticos y WhatsApp Business API sean equivalentes. Tampoco se equiparan una solicitud pendiente y una reserva confirmada, imágenes almacenadas y radiología integrada, ni un reporte Excel y una exportación clínica completa.

## A Comparación de sistemas

### Identificación y pertinencia

| Sistema | Proveedor | País evidenciado | Segmento | Modalidad | URL oficial | Consulta |
|---|---|---|---|---|---|---|
| 1. Dentalink | Dentalink; entidad legal no evidenciada | No evidenciado | Consultorios y clínicas dentales con oferta argentina | Gestión web; alojamiento contractual no evidenciado | [F01.1 Sitio argentino](https://web-ar.softwaredentalink.com/) | 2026-11-17 |
| 2. DentApp | Codilium S.A.S. | Argentina, declarado por el proveedor | Independientes, clínicas y redes según plan | Nube en navegador | [F02.1 Producto y país](https://www.dentapp.ar/software-odontologico-argentina) | 2026-11-17 |
| 3. Dentify | Dentify; entidad legal no evidenciada | Argentina, declarado en el sitio | Odontólogo independiente y clínica multiprofesional | SaaS en navegador | [F03.1 Producto, FAQ y precios](https://www.dentify.com.ar/) | 2026-11-17 |
| 4. OdontoApp | Sovil | No evidenciado; ofrece servicio en Argentina | Odontólogos y consultorios latinoamericanos | Nube declarada en política de privacidad | [F04.1 Portada parcial](https://odontoapp.com.ar/) | 2026-11-17 |
| 5. Odontoly | Odontoly; entidad legal no evidenciada | Argentina, declarado por el sitio | Consultorios odontológicos y equipos pequeños | Aplicación local-first; acceso remoto condicionado | [F05.1 Producto, restricciones y precios](https://odontoly.com/) | 2026-11-17 |
| 6. Dentidesk | Servicios Tecnológicos e Informáticos Dentales Dentidesk Chile SpA | Chile, identidad en términos oficiales | Consultorios, clínicas y múltiples sucursales | SaaS dental | [F06.1 Características](https://www.dentidesk.cl/dentidesk/feature) | 2026-11-17 |
| 7. Odonto.me | No evidenciado | No evidenciado | Consultorios y equipos con capacidad técnica en autohospedado | Pro online o código abierto autohospedado | [F07.1 Producto y planes](https://odonto.me/?locale=es) | 2026-11-17 |
| 8. AgendaPro | AgendaPro Inc. según pie de página | No evidenciado | Agenda multisector; oferta para dentistas en Argentina | Plataforma online | [F08.1 Oferta dental](https://agendapro.com/es/dental/software-odontologico) | 2026-11-17 |
| 9. Clinic Cloud | Clinic Cloud; entidad legal no evidenciada | No evidenciado | Clínicas generales con módulo odontológico | SaaS clínico | [F09.1 Tarifas y módulos](https://clinic-cloud.com/tarifas) | 2026-11-17 |
| 10. XDentalCloud | Massive Bionics LLC | EE.UU., declarado en FAQ de precios; no confundir mercado servido con país | Consultorios, clínicas y múltiples sedes | SaaS dental en Microsoft Azure, según proveedor | [F10.1 Producto](https://xdentalcloud.com/) | 2026-11-17 |
| 11. Open Dental | Open Dental Software | EE.UU., domicilio en Salem, Oregon | Consultorios y clínicas; referencia de dominio internacional | Self-hosted; cloud publicado para EE.UU. | [F11.1 Agenda por operatorios](https://www.opendental.com/manual/operatories.html) | 2026-11-17 |
| 12. Dentrix | Henry Schein One | No evidenciado en las páginas seleccionadas | Consultorios y grupos dentales; referencia internacional | PMS con Patient Engage conectado; modalidad de despliegue contractual no evidenciada | [F12.1 Producto](https://www.dentrix.com/) | 2026-11-17 |
| 13. Curve Dental | CD Newco, LLC | EE.UU., domicilio publicado en Alpharetta, Georgia | Consultorios y clínicas multiubicación | SaaS en navegador; Mac, PC y Chromebook | [F13.1 Producto, FAQ, precio y domicilio](https://www.curvedental.com/) | 2026-11-17 |
| 14. CareStack | Good Methods Global Inc. | No evidenciado en las páginas seleccionadas | Consultorios, grupos/DSO y odontología móvil | SaaS dental | [F14.1 Plataforma y certificaciones declaradas](https://carestack.com/) | 2026-11-17 |
| 15. tab32 | tab32; entidad legal no evidenciada | EE.UU., fundación y sede en Sacramento declaradas | Alpine para práctica individual; Summit para grupos | SaaS dental en Google Cloud | [F15.1 Producto](https://tab32.com/) | 2026-11-17 |

La oferta argentina encabeza la tabla. Dentidesk y Odonto.me aportan referencias en español; AgendaPro tiene oferta y reservas públicas en Argentina. Clinic Cloud y XDentalCloud son referencias de módulos y condiciones; Open Dental, Dentrix, Curve, CareStack y tab32 aportan patrones internacionales cuya comercialización y localización argentina no se presumen.

### Fichas comparables y trazabilidad

Las fuentes F01 a F15 se enumeran completas al final. Las fichas complementan las matrices; una capacidad pendiente de prueba no se convierte en requisito del producto que construiremos.

### 01 Dentalink

Fuentes: [F01.1 Sitio argentino](https://web-ar.softwaredentalink.com/); [F01.2 Manual de agenda online](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online).

- **Agenda y recursos**: El manual documenta enlace de agenda, profesionales habilitados, bloques y permisos. La sección «Cruce con Recursos» documenta cómo asociar recursos a un servicio y afirma que la agenda online valida que el profesional y el recurso estén disponibles en el mismo horario (consulta del 25/11/2026). Identificación de ese recurso como sillón y control de concurrencia: No evidenciado.
- **Turnos digitales y automatización**: Reserva por enlace documentada. Declara confirmaciones por WhatsApp/email, campañas y seguimiento de citas anuladas o ausentes. Compartir enlace por WhatsApp no demuestra WhatsApp Business API.
- **Clínica**: Declara historia, odontograma, periodontograma, imágenes, firma y planes de tratamiento.
- **Administración**: Declara caja, gastos, liquidación a odontólogos, KPI, reportes Excel y obras sociales.
- **Integraciones**: WhatsApp y obras sociales declarados; Mercado Pago, ARCA y API pública: No evidenciado.
- **Seguridad y exportación**: Permisos de agenda documentados. Exportación de reportes Excel declarada; no demuestra exportación integral de historias e imágenes. Cifrado, backup y auditoría: No evidenciado en las páginas examinadas.
- **Modelo comercial**: No evidenciado: importe, moneda, prueba y cargos aplicables en las páginas consultadas.
- **Fortalezas y límites de la evidencia**: No se probó el producto. No confundir gestión de convenios con integración electrónica con una obra social.
- **Adopción**: No evidenciado

### 02 DentApp

Fuentes: [F02.1 Producto y país](https://www.dentapp.ar/software-odontologico-argentina); [F02.2 Funcionalidades y planes](https://www.dentapp.ar/).

- **Agenda y recursos**: Declara agenda por profesional con Google Calendar y estados. Vistas, sillones, duración por prestación y bloqueo de solapamientos simultáneos: No evidenciado.
- **Turnos digitales y automatización**: Declara recordatorios automáticos por correo en el plan Profesional y por WhatsApp desde Clínica Plus (consulta del 25/11/2026); reserva autónoma, lista de espera y reprogramación por paciente: No evidenciado.
- **Clínica**: Declara historia, odontograma FDI, imágenes, recetas, consentimientos y presupuestos. Periodontograma: No evidenciado.
- **Administración**: Declara caja, cobros, facturas, presupuestos y reportes. Integración fiscal ARCA: No evidenciado.
- **Integraciones**: Declara Google Calendar, MercadoPago y RCTA. El plan Empresarial menciona API y webhooks e integraciones a medida con obras sociales y prepagas (consulta del 25/11/2026); acceso público a la API: No evidenciado. Inscripción ReNaPDiS declarada, sin contraste registral independiente.
- **Seguridad y exportación**: Declara roles admin/doctor/secretaría, documentos cifrados, cifrado en tránsito y en reposo y backups diarios (consulta del 25/11/2026). Exportación completa y auditoría: No evidenciado.
- **Modelo comercial**: Publica por mes, con IVA 21 % incluido: Profesional $24.900 (1 profesional), Clínica Plus $54.900 (hasta 5) y Clínica Pro $74.900 (hasta 10); Empresarial a medida. 30 días de prueba sin cargo (consulta del 25/11/2026). Código de moneda: No evidenciado explícitamente.
- **Fortalezas y límites de la evidencia**: El soporte por WhatsApp y un recordatorio automático son capacidades diferentes. No se validó habilitación normativa ni receta electrónica real.
- **Adopción**: No evidenciado

### 03 Dentify

Fuentes: [F03.1 Producto, FAQ y precios](https://www.dentify.com.ar/); [F03.2 Página de consultorio](https://www.dentify.com.ar/software-consultorio-odontologico).

- **Agenda y recursos**: Declara vistas diarias/semanales, filtro por profesional, prácticas y prevención de superposición. Sillón y control concurrente de reservas: No evidenciado.
- **Turnos digitales y automatización**: Estados de turno declarados. Reserva online y recordatorios por WhatsApp: No evidenciado; WhatsApp publicado para soporte no equivale a recordatorios.
- **Clínica**: Declara historias, odontograma, imágenes, recetas y evoluciones. Periodontograma: No evidenciado.
- **Administración**: Declara ingresos, cobros, egresos, balances y obras sociales. Emisión fiscal integrada: No evidenciado.
- **Integraciones**: Obras sociales declaradas; Mercado Pago, Google Calendar, ARCA y API: No evidenciado.
- **Seguridad y exportación**: FAQ declara nube cifrada y acceso de profesionales invitados. Exportación integral, auditoría y política de backup verificable: No evidenciado.
- **Modelo comercial**: Página abierta: en modalidad mensual, Básico $30.000/mes y Clínicas $45.000/mes; en modalidad anual, $25.000/mes ($300.000 por año) y $38.000/mes ($456.000 por año), anunciada con «2 meses gratis» (consulta del 25/11/2026). Prueba 14 días sin tarjeta. Código de moneda e impuestos: No evidenciado explícitamente; no inferir ARS solo por el dominio.
- **Fortalezas y límites de la evidencia**: Hay una demostración ilustrada en la portada, sin prueba operativa. Se corrigió la ausencia de evidencia de país y cifrado al leer la portada actual. El pie indica «Desarrollado por IB Digital», sin acreditar entidad legal.
- **Adopción**: No evidenciado

### 04 OdontoApp

Fuentes: [F04.1 Portada parcial](https://odontoapp.com.ar/); [F04.2 Política de privacidad](https://odontoapp.com.ar/privacidad).

- **Agenda y recursos**: Declara agenda digital. Vistas, profesionales, sillones, duración y control concurrente: No evidenciado.
- **Turnos digitales y automatización**: Declara recordatorios automáticos. Política explica WhatsApp configurable por el profesional; API oficial de Meta y reserva online: No evidenciado.
- **Clínica**: Declara historia y odontograma; política incluye imágenes y radiografías. Periodontograma y planes detallados: No evidenciado.
- **Administración**: Política menciona facturación; caja, medios de cobro, integración fiscal y métricas: No evidenciado.
- **Integraciones**: WhatsApp declarado; Mercado Pago, Google Calendar, ARCA y API: No evidenciado.
- **Seguridad y exportación**: Política declara cifrado en tránsito y control de acceso. Declara adecuación a Ley 25.326; no es auditoría ni certificación de cumplimiento. Backup, auditoría y exportación: No evidenciado.
- **Modelo comercial**: No evidenciado: precios y condiciones, porque la portada recuperada es parcial.
- **Fortalezas y límites de la evidencia**: No completar información de planes desde un fragmento indexado. No se relaciona automáticamente con marcas similares o con un pie residual de otra web.
- **Adopción**: La portada declara más de 100 odontólogos y clínicas; cifra comercial, no usuarios activos verificados.

### 05 Odontoly

Fuentes: [F05.1 Producto, restricciones y precios](https://odontoly.com/).

- **Agenda y recursos**: Declara agenda compartida y solicitudes de reserva. Vistas diarias/semanales y control simultáneo sillón/profesional: No evidenciado.
- **Turnos digitales y automatización**: Declara solicitudes por enlace revisadas por el equipo. WhatsApp Business y SMS figuran próximos, no disponibles.
- **Clínica**: Declara historia, evoluciones, odontograma, fotografías y presupuestos. Visor RX avanzado figura próximo; no confundir almacenamiento con visor.
- **Administración**: Declara caja, deudas y pagos ARS/USD con cotización manual. ARCA y cobros automatizados figuran próximos.
- **Integraciones**: Mercado Pago, Google Calendar y API: No evidenciado. WhatsApp Business y ARCA son anuncios futuros.
- **Seguridad y exportación**: Declara cuentas/permisos y copias completas cifradas. Aclara que la base de trabajo aún no está cifrada por la app. Auditoría y exportación interoperable: No evidenciado.
- **Modelo comercial**: Referencia publicada USD 149,99/mes sin permanencia; el sitio pide confirmar precio final, moneda e impuestos. No se evidenció prueba gratuita. El plan incluye dos usuarios y hasta cuatro dispositivos autorizados (consulta del 25/11/2026).
- **Fortalezas y límites de la evidencia**: Mac Apple Silicon; Windows en desarrollo. Seguir durante un corte exige sesión y autorización vigentes; el acceso remoto requiere equipo principal encendido. No asumir operación offline absoluta.
- **Adopción**: No evidenciado

### 06 Dentidesk

Fuentes: [F06.1 Características](https://www.dentidesk.cl/dentidesk/feature); [F06.2 Términos de uso](https://app.dentidesk.com/terminos.php); [F06.3 Ayuda roles y profesionales](https://dentidesk.freshdesk.com/support/solutions/articles/150000201446--c%C3%B3mo-agregar-doctores-y-personal-a-mi-cl%C3%ADnica-en-dentidesk-).

- **Agenda y recursos**: Agenda por doctor, sucursal y perfiles documentada en ayuda. Prevención simultánea por sillón y profesional: No evidenciado.
- **Turnos digitales y automatización**: Declara botón web de reserva, confirmación/anulación por email y recordatorios de control. WhatsApp: No evidenciado.
- **Clínica**: Declara historia/anamnesis, odontograma, fichas de periodoncia e imágenes; ficha periodontal no demuestra un periodontograma gráfico.
- **Administración**: Declara presupuestos por pieza, egresos, inventario, liquidaciones y reportes. Facturación argentina y obras sociales: No evidenciado.
- **Integraciones**: Integraciones argentinas, WhatsApp API, Google Calendar y API pública: No evidenciado.
- **Seguridad y exportación**: Ayuda documenta permisos. Términos contemplan Excel estándar sin costo con licencia activa; tras el término, solicitud de exportación hasta 30 días corridos y posible cargo. No demuestra exportación de todos los archivos clínicos.
- **Modelo comercial**: Precio: No evidenciado. Prueba de 15 días y, al finalizar, suscripción según el plan elegido; los términos indican que cada cuenta de odontólogo adicional generará un cargo según el plan contratado. Otros formatos de extracción pueden tener costo.
- **Fortalezas y límites de la evidencia**: No confundir gratuidad de exportación con licencia activa con extracción posterior al cierre. No atribuir usuarios de la plataforma U-DESK al producto dental.
- **Adopción**: No evidenciado

### 07 Odonto.me

Fuentes: [F07.1 Producto y planes](https://odonto.me/?locale=es).

- **Agenda y recursos**: Declara agendas múltiples y citas por dentista. Sillones, vistas y bloqueo de solapamientos: No evidenciado.
- **Turnos digitales y automatización**: Declara recordatorios y solicitud de reseñas automáticos. Enlace WhatsApp dirige a comunidad, no demuestra mensajería clínica; autorreserva: No evidenciado.
- **Clínica**: Declara odontograma, notas y expedientes. Periodontograma, imágenes y planes detallados: No evidenciado.
- **Administración**: Declara cargos, pagos online y saldos; facturación fiscal, obras sociales y reportes financieros detallados: No evidenciado.
- **Integraciones**: Mercado Pago, Google Calendar, ARCA, API y WhatsApp clínico: No evidenciado.
- **Seguridad y exportación**: En autohospedado, actualizaciones y respaldos quedan a cargo del usuario. Roles, auditoría, cifrado y exportación: No evidenciado.
- **Modelo comercial**: Pro: $6 mensual y prueba 30 días sin tarjeta. Moneda e impuestos: No evidenciado. Código abierto gratis en servidor propio; hosting y mantenimiento no están incluidos por esa afirmación.
- **Fortalezas y límites de la evidencia**: No deducir USD del símbolo $. Ni la imagen de producto ni el enlace de código acreditan una instalación probada.
- **Adopción**: No evidenciado

### 08 AgendaPro

Fuentes: [F08.1 Oferta dental](https://agendapro.com/es/dental/software-odontologico); [F08.2 Planes Argentina](https://agendapro.com/ar/planes); [F08.3 Interfaz pública de reserva](https://agendapro.com/site/ar/odontologiaor/239004).

- **Agenda y recursos**: Declara agendas por profesional; interfaz pública muestra servicios y duración. Sillones y bloqueo transaccional concurrente: No evidenciado.
- **Turnos digitales y automatización**: Reserva pública documentada; declara recordatorios por email y WhatsApp. Los detalles dependen de categoría/plan; no extrapolar el agente IA de Belleza a Salud. La interfaz pública muestra condiciones de cancelación y cambios de un consultorio; no son reglas adoptadas para nuestro producto.
- **Clínica**: Declara fichas personalizables e imágenes en su oferta dental; la cobertura de consentimientos depende de categoría y plan. Odontograma y periodontograma: No evidenciado.
- **Administración**: Caja, comisiones, inventario y reportes según plan; facturación electrónica argentina figura próxima. Obras sociales integradas: No evidenciado.
- **Integraciones**: Mercado Pago y API con cargo US$200/mes declarados. WhatsApp tiene cupos/costos Meta y condiciones de categoría. ARCA operativa: No evidenciado.
- **Seguridad y exportación**: Seguridad específica, auditoría y exportación integral: No evidenciado en las páginas examinadas.
- **Modelo comercial**: La página abierta muestra precios de Belleza/Barbería en ARS con IVA. Tarifa aplicable a odontología: No evidenciado; no usar $7.900 o $15.900 como precio de Salud. API US$200/mes es un adicional, no la suscripción dental.
- **Fortalezas y límites de la evidencia**: La reserva se observó sin completarla. No confundir confirmación de una interfaz con prueba de integridad del backend. Precio indexado antiguo descartado. El 25/11/2026 la interfaz de reserva respondió HTTP 403; el agente no pudo recomprobar la observación del 17/11. En la revisión personal informada ese día, Luciano Izquierdo Cafici no halló diferencias.
- **Adopción**: No evidenciado

### 09 Clinic Cloud

Fuentes: [F09.1 Tarifas y módulos](https://clinic-cloud.com/tarifas); [F09.2 Ayuda presupuesto y odontograma](https://clinic-cloud.com/faq/agenda-y-comunicaciones/presupuestos).

- **Agenda y recursos**: Declara múltiples agendas según plan. La ayuda vincula citas y prestaciones del presupuesto. Recurso sillón concurrente: No evidenciado.
- **Turnos digitales y automatización**: Declara reserva 24/7 por Doctoralia y recordatorios. WhatsApp de Doctoralia Pro tiene condiciones propias; no atribuirlo como incluido automáticamente.
- **Clínica**: Historia clínica y odontograma en Max declarados; ayuda documenta presupuesto desde odontograma. Periodontograma: No evidenciado.
- **Administración**: Caja, presupuestos, liquidaciones y reportes; facturación TicketBAI/VeriFactu, que no prueba facturación ARCA.
- **Integraciones**: Doctoralia declarado. Mercado Pago, obras sociales argentinas, Google Calendar y API pública: No evidenciado.
- **Seguridad y exportación**: Permisos por niveles declarados. La página de tarifas exhibe ISO/IEC 27001, sin certificado inspeccionado (consulta del 25/11/2026). Exportación, auditoría, cifrado y política de backup: No evidenciado en las fuentes examinadas.
- **Modelo comercial**: Mini 29, Pro 49 y Max 79 EUR/mes + IVA; Enterprise a convenir. Odontograma en Max. Apertura actual muestra 50% de descuento durante 3 meses, no 2. Agendas adicionales +10 EUR según límite del plan.
- **Fortalezas y límites de la evidencia**: No comparar Mini como si incluyera odontograma. Precios y promoción están sujetos a contratación; no se abrió cuenta ni aceptó términos.
- **Adopción**: Declara más de 3.000 consultas y clínicas en España (consulta del 25/11/2026); cifra comercial no verificada externamente.

### 10 XDentalCloud

Fuentes: [F10.1 Producto](https://xdentalcloud.com/); [F10.2 Precios](https://xdentalcloud.com/precios/); [F10.3 Agenda](https://xdentalcloud.com/agenda-dental/); [F10.4 Seguridad](https://xdentalcloud.com/seguridad/); [F10.5 Empresa](https://xdentalcloud.com/empresa/).

- **Agenda y recursos**: Declara agenda multi-sillón en tiempo real y citas por consulta. El bloqueo concurrente por profesional y sillón requiere demostración: No evidenciado.
- **Turnos digitales y automatización**: Declara reserva 24h, confirmación automática y recordatorios email/SMS/push. WhatsApp operativo para Argentina: No evidenciado.
- **Clínica**: Declara historia dental, odontograma, periodontograma, imágenes y planes de tratamiento.
- **Administración**: Declara facturación, arqueos, presupuestos, liquidaciones y estadísticas. Integración ARCA: No evidenciado; normativa de otros países no la sustituye.
- **Integraciones**: Declara Google Calendar, MercadoPago y API; disponibilidad local y condiciones a confirmar. WhatsApp API para Argentina: No evidenciado.
- **Seguridad y exportación**: Declara cifrado, respaldo diario, control por IP y cumplimiento RGPD/HIPAA; no es auditoría independiente ni certificación argentina. Exportación integral y auditoría: No evidenciado.
- **Modelo comercial**: Esencial publicado 42 EUR/mes equivalente anual 510 EUR; home indica USD49 para América. Los importes equivalentes no coinciden exactamente al multiplicar por 12; no recalcular ni elegir moneda final. Extras portal/CRM y otros según plan. La apertura vigente ofrece demo con datos de ejemplo; duración de prueba contractual: No evidenciado.
- **Fortalezas y límites de la evidencia**: Se analiza XDentalCloud, no se trasladan capacidades desde DriCloud por compartir tecnología. Cotización argentina y detalle fiscal pendientes.
- **Adopción**: No evidenciado

### 11 Open Dental

Fuentes: [F11.1 Agenda por operatorios](https://www.opendental.com/manual/operatories.html); [F11.2 Bloqueos](https://www.opendental.com/manual/blockouts.html); [F11.3 Tarifas](https://opendental.com/site/fees.html); [F11.4 Historia clínica](https://www.opendental.com/manual/chart.html); [F11.5 Contacto](https://www.opendental.com/site/contact.html); [F11.6 Agenda general](https://www.opendental.com/manual/appointments.html).

- **Agenda y recursos**: Manual documenta operatorios, profesionales, vistas, bloqueos y manejo de citas. Permitir superposición es configurable: no atribuir una prohibición universal.
- **Turnos digitales y automatización**: Web Sched y recordatorios tienen servicios y cargos publicados. La reserva online puede ser adicional; WhatsApp: No evidenciado.
- **Clínica**: Manual documenta historia gráfica, periodontograma, recetas y tratamiento. Interoperabilidad radiográfica argentina: No evidenciado.
- **Administración**: Publica licencia/soporte y servicios. Integración fiscal argentina, obras sociales argentinas y medios de cobro locales: No evidenciado en las fuentes relevadas.
- **Integraciones**: Mercado Pago, Google Calendar, ARCA, API pública y WhatsApp: No evidenciado en esta muestra de fuentes.
- **Seguridad y exportación**: Permiso de bloqueos documentado. No se evaluaron cifrado, exportación, auditoría ni restauración; son No evidenciado en este relevamiento.
- **Modelo comercial**: Otros países: USD89/mes por sede hasta 3 profesionales, contrato 12 meses; desde el cuarto, USD20/profesional según condiciones. Servicios web y conversión pueden costar aparte. Elegibilidad argentina final a confirmar.
- **Fortalezas y límites de la evidencia**: La documentación permite entender restricciones concretas; no constituye una prueba de configuración aplicada ni de disponibilidad comercial argentina.
- **Adopción**: No evidenciado

### 12 Dentrix

Fuentes: [F12.1 Producto](https://www.dentrix.com/); [F12.2 Patient Engage](https://www.dentrix.com/dental-solutions/marketing-and-patient-experience/dentrix-patient-engage-suite/); [F12.3 Flujo documentado de reserva](https://magazine.dentrix.com/online-booking-from-the-patients-perspective-2/).

- **Agenda y recursos**: Artículo oficial documenta selección de motivo, profesional, horario y operatorio disponible. No demuestra bloqueo simultáneo en todas las configuraciones.
- **Turnos digitales y automatización**: Reserva 24/7, recordatorios y reactivación email/texto; formularios y firma declarados. WhatsApp API: No evidenciado.
- **Clínica**: Declara charting e imágenes. Periodontograma y planes específicos en la muestra: No evidenciado.
- **Administración**: Declara seguros, facturación, cobros y analítica multiubicación, orientados a su mercado; no atribuir obras sociales argentinas.
- **Integraciones**: API Exchange declarado, no equivale a API pública sin contrato. ARCA, Mercado Pago y Google Calendar: No evidenciado.
- **Seguridad y exportación**: Declara servicios de ciberseguridad/backup; roles, auditoría, cifrado y exportación integral: No evidenciado en las fuentes seleccionadas.
- **Modelo comercial**: No evidenciado: monto total, moneda, prueba y adicionales aplicables. Solicitud comercial sin completar.
- **Fortalezas y límites de la evidencia**: Artículo demostrativo de 2021: corrobora flujo histórico y se complementa con página vigente; no garantiza que la interfaz actual sea idéntica.
- **Adopción**: No evidenciado

### 13 Curve Dental

Fuentes: [F13.1 Producto, FAQ, precio y domicilio](https://www.curvedental.com/).

- **Agenda y recursos**: Declara scheduling y operación multiubicación. Vistas y asignación simultánea sillón/profesional: No evidenciado.
- **Turnos digitales y automatización**: Declara online booking, recordatorios, recare y comunicación. WhatsApp y lista de espera: No evidenciado.
- **Clínica**: Declara charting, imágenes, perio y planes de tratamiento.
- **Administración**: Declara facturación, cobros, Curve Pay, membresías y reportes. Obras sociales y ARCA: No evidenciado.
- **Integraciones**: Mercado Pago, Google Calendar, API pública y WhatsApp clínico: No evidenciado.
- **Seguridad y exportación**: Declara infraestructura/controles HIPAA. No se revisó auditoría independiente, permisos detallados, exportación o frecuencia de backup.
- **Modelo comercial**: Cotización según tamaño y funciones. Importe, moneda, prueba y cargos: No evidenciado en la página examinada.
- **Fortalezas y límites de la evidencia**: El sitio advierte funciones limitadas a EE.UU.; no presentar como disponible para contratar en Argentina sin confirmación.
- **Adopción**: Declara 100.000+ usuarios activos y 6.000+ sedes en EE.UU./Canadá; cifras del proveedor, no verificadas externamente.

### 14 CareStack

Fuentes: [F14.1 Plataforma y certificaciones declaradas](https://carestack.com/); [F14.2 Reserva online](https://carestack.com/dental-software/features/online-scheduling).

- **Agenda y recursos**: Declara agenda multiubicación y filtros de profesional/tratamiento; control concurrente de sillones: No evidenciado.
- **Turnos digitales y automatización**: Documenta diferencia entre solicitud que confirma la clínica y reserva directa; enlaces por sitio/portal. Confirma por llamada/texto/email. Agentes de recall y relleno de cancelaciones figuran próximos.
- **Clínica**: Declara charting general, periodontal, imágenes y planes de tratamiento.
- **Administración**: Declara cobros, pagos, seguros, liquidación y analítica. Mercado argentino y obras sociales locales: No evidenciado.
- **Integraciones**: Integraciones sanitarias declaradas; WhatsApp, Mercado Pago, ARCA, Google Calendar y API pública en la muestra: No evidenciado.
- **Seguridad y exportación**: Declara backups, HIPAA, SOC 2 Type 2 e ISO27001:2022; no se inspeccionaron certificados ni auditoría independiente. Exportación: No evidenciado.
- **Modelo comercial**: No evidenciado: importe, moneda, prueba o cargos en las páginas examinadas.
- **Fortalezas y límites de la evidencia**: No convertir presencia de un agente futuro en automatización disponible. Solicitud de turno y reserva confirmada son comportamientos distintos.
- **Adopción**: Declara 3.000+ prácticas; cifra comercial no validada como clientes argentinos.

### 15 tab32

Fuentes: [F15.1 Producto](https://tab32.com/); [F15.2 Tarifa vigente abierta](https://tab32.com/pricing/); [F15.3 Empresa y seguridad declarada](https://tab32.com/about/).

- **Agenda y recursos**: Declara agenda por columnas de operatorio y lista de espera. Control concurrente de profesional/sillón: No evidenciado.
- **Turnos digitales y automatización**: Declara reserva online, recordatorios, confirmaciones y seguimiento de ausentes. IA y comunicación con cargos separados según servicio.
- **Clínica**: Declara odontograma, imágenes, perio y planes de tratamiento.
- **Administración**: Declara facturación, claims, pagos y reportes. ARCA/obras sociales argentinas: No evidenciado.
- **Integraciones**: API para Summit declarada; no se validó acceso público. WhatsApp, Google Calendar y Mercado Pago: No evidenciado.
- **Seguridad y exportación**: Declara Google Cloud, cifrado, backups automáticos, HIPAA/SOC2 y una exportación gratuita al salir que incluye el ledger. No se auditó cumplimiento.
- **Modelo comercial**: Página abierta vigente: desde $95/mes, usuarios/profesionales ilimitados; prueba 14 días, que pide email y tarjeta (consulta del 25/11/2026). Sitio empresarial identifica dólares en cargos, pero moneda contractual argentina no confirmada. Claims $0,25, adjuntos $0,50 por claim, elegibilidad $0,75; migración publicada desde $1,250 y otros cargos separados. La misma página cobra aparte por servicio: voz $0,20 por minuto, asistente de IA por tokens, registro de pagos 1,25 % y agentes de IA a $25 por mes cada uno, $325 los catorce o $349 con agentes propios; mensajes y llamadas $0,05 (consulta del 25/11/2026). Snippet $125 descartado.
- **Fortalezas y límites de la evidencia**: La búsqueda indexada y apertura mostraron planes distintos; se priorizó apertura. Ledger no se migra según FAQ; no confundir migración con exportación. Producto orientado a EE.UU.; disponibilidad argentina sin confirmar.
- **Adopción**: No evidenciado

## B Matriz provisional de puntuación

Se aplican los pesos de la consigna. Para que el cálculo sea auditable sin probar productos ni elegir un ganador, esta versión mide **cobertura de evidencia pública**, no calidad real, seguridad efectiva, relación precio/valor ni conveniencia de compra. La interpretación y cualquier ranking comercial deben revisarse con el usuario. Los resultados no eligen el MVP.

Cada dimensión tiene cinco criterios. Por criterio: Documentado = 1 punto; Declarado = 0,5; No evidenciado = 0. La suma de cada dimensión va de 0 a 5. Una función futura no suma como disponible. La falta de evidencia reduce la puntuación sin afirmar que la función no existe. La columna de precio mide transparencia y condiciones de adopción, no asequibilidad comparada entre monedas.

Total ponderado = T × 0,25 + C × 0,20 + I × 0,15 + A × 0,15 + P × 0,10 + S × 0,10 + E × 0,05. Se calcula sin redondear los componentes y se muestran tres decimales. Las matrices detalladas de criterios explican cada componente. No mezclar planes o países para concluir que todo está incluido en una misma tarifa.

Tras la segunda comprobación del 25/11/2026 cambiaron cuatro celdas de DentApp (respaldos, tarifa publicada, unidad de cobro y prueba), y su total pasó de 1,275 a 1,475. La validación de recursos documentada por Dentalink no suma en «Agenda por sillón/box» porque el manual no identifica el recurso como sillón.

| Sistema | T 25% | C 20% | I 15% | A 15% | P 10% | S 10% | E 5% | Total /5 |
|---|---|---|---|---|---|---|---|---|
| Dentalink | 2.5 | 2.5 | 0.5 | 2.0 | 1.5 | 1.5 | 0.0 | 1.800 |
| DentApp | 1.0 | 2.0 | 1.5 | 2.0 | 0.0 | 1.5 | 3.0 | 1.475 |
| Dentify | 1.0 | 1.5 | 0.0 | 2.0 | 0.0 | 1.0 | 3.0 | 1.100 |
| OdontoApp | 0.5 | 1.5 | 0.5 | 0.5 | 0.0 | 1.0 | 0.0 | 0.675 |
| Odontoly | 0.5 | 1.5 | 0.0 | 1.0 | 0.5 | 1.0 | 3.0 | 0.875 |
| Dentidesk | 2.0 | 2.0 | 0.0 | 1.5 | 1.5 | 2.0 | 2.0 | 1.575 |
| Odonto.me | 1.0 | 1.0 | 0.0 | 0.5 | 0.0 | 0.0 | 3.0 | 0.675 |
| AgendaPro | 2.0 | 1.0 | 1.5 | 1.5 | 3.0 | 0.0 | 1.5 | 1.525 |
| Clinic Cloud | 1.5 | 3.0 | 0.0 | 2.0 | 0.5 | 0.5 | 4.5 | 1.600 |
| XDentalCloud | 1.5 | 2.5 | 1.5 | 2.0 | 1.0 | 1.5 | 2.0 | 1.750 |
| Open Dental | 4.0 | 5.0 | 0.0 | 0.0 | 0.5 | 1.0 | 4.0 | 2.350 |
| Dentrix | 3.5 | 1.5 | 0.0 | 1.5 | 1.5 | 0.5 | 0.0 | 1.600 |
| Curve Dental | 1.0 | 2.5 | 0.0 | 1.5 | 0.5 | 0.5 | 0.0 | 1.075 |
| CareStack | 2.0 | 2.5 | 0.0 | 2.0 | 1.5 | 1.0 | 0.0 | 1.550 |
| tab32 | 1.5 | 2.5 | 0.0 | 1.5 | 1.5 | 2.0 | 4.0 | 1.650 |

### T Turnos y automatización

Peso: 25 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | Vista diaria/semanal | Agenda por profesional | Agenda por sillón/box | Reserva online | Recordatorios |
|---|---|---|---|---|---|
| Dentalink | No evidenciado | Documentado | No evidenciado | Documentado | Declarado |
| DentApp | No evidenciado | Declarado | No evidenciado | No evidenciado | Declarado |
| Dentify | Declarado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| OdontoApp | No evidenciado | No evidenciado | No evidenciado | No evidenciado | Declarado |
| Odontoly | No evidenciado | No evidenciado | No evidenciado | Declarado | No evidenciado |
| Dentidesk | No evidenciado | Documentado | No evidenciado | Declarado | Declarado |
| Odonto.me | No evidenciado | Declarado | No evidenciado | No evidenciado | Declarado |
| AgendaPro | No evidenciado | Declarado | No evidenciado | Documentado | Declarado |
| Clinic Cloud | No evidenciado | Declarado | No evidenciado | Declarado | Declarado |
| XDentalCloud | No evidenciado | No evidenciado | Declarado | Declarado | Declarado |
| Open Dental | Documentado | Documentado | Documentado | Declarado | Declarado |
| Dentrix | No evidenciado | Documentado | Documentado | Documentado | Declarado |
| Curve Dental | No evidenciado | No evidenciado | No evidenciado | Declarado | Declarado |
| CareStack | No evidenciado | Declarado | No evidenciado | Documentado | Declarado |
| tab32 | No evidenciado | No evidenciado | Declarado | Declarado | Declarado |

### C Clínica odontológica

Peso: 20 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | Historia clínica | Odontograma | Periodontograma | Imágenes | Planes de tratamiento |
|---|---|---|---|---|---|
| Dentalink | Declarado | Declarado | Declarado | Declarado | Declarado |
| DentApp | Declarado | Declarado | No evidenciado | Declarado | Declarado |
| Dentify | Declarado | Declarado | No evidenciado | Declarado | No evidenciado |
| OdontoApp | Declarado | Declarado | No evidenciado | Declarado | No evidenciado |
| Odontoly | Declarado | Declarado | No evidenciado | Declarado | No evidenciado |
| Dentidesk | Declarado | Declarado | No evidenciado | Declarado | Declarado |
| Odonto.me | Declarado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| AgendaPro | Declarado | No evidenciado | No evidenciado | Declarado | No evidenciado |
| Clinic Cloud | Declarado | Documentado | No evidenciado | Declarado | Documentado |
| XDentalCloud | Declarado | Declarado | Declarado | Declarado | Declarado |
| Open Dental | Documentado | Documentado | Documentado | Documentado | Documentado |
| Dentrix | Declarado | Declarado | No evidenciado | Declarado | No evidenciado |
| Curve Dental | Declarado | Declarado | Declarado | Declarado | Declarado |
| CareStack | Declarado | Declarado | Declarado | Declarado | Declarado |
| tab32 | Declarado | Declarado | Declarado | Declarado | Declarado |

### I Integraciones locales y WhatsApp

Peso: 15 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | WhatsApp clínico | Google Calendar | Mercado Pago | ARCA operativa | API pública |
|---|---|---|---|---|---|
| Dentalink | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| DentApp | Declarado | Declarado | Declarado | No evidenciado | No evidenciado |
| Dentify | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| OdontoApp | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Odontoly | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentidesk | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Odonto.me | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| AgendaPro | Declarado | No evidenciado | Declarado | No evidenciado | Declarado |
| Clinic Cloud | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| XDentalCloud | No evidenciado | Declarado | Declarado | No evidenciado | Declarado |
| Open Dental | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentrix | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Curve Dental | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| CareStack | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| tab32 | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |

### A Administración y cobros

Peso: 15 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | Caja | Cobros | Facturación general | Obras sociales argentinas | Reportes |
|---|---|---|---|---|---|
| Dentalink | Declarado | Declarado | No evidenciado | Declarado | Declarado |
| DentApp | Declarado | Declarado | Declarado | No evidenciado | Declarado |
| Dentify | Declarado | Declarado | No evidenciado | Declarado | Declarado |
| OdontoApp | No evidenciado | No evidenciado | Declarado | No evidenciado | No evidenciado |
| Odontoly | Declarado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| Dentidesk | Declarado | Declarado | No evidenciado | No evidenciado | Declarado |
| Odonto.me | No evidenciado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| AgendaPro | Declarado | Declarado | No evidenciado | No evidenciado | Declarado |
| Clinic Cloud | Declarado | Declarado | Declarado | No evidenciado | Declarado |
| XDentalCloud | Declarado | Declarado | Declarado | No evidenciado | Declarado |
| Open Dental | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentrix | No evidenciado | Declarado | Declarado | No evidenciado | Declarado |
| Curve Dental | No evidenciado | Declarado | Declarado | No evidenciado | Declarado |
| CareStack | Declarado | Declarado | Declarado | No evidenciado | Declarado |
| tab32 | No evidenciado | Declarado | Declarado | No evidenciado | Declarado |

### P Experiencia del paciente

Peso: 10 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | Reserva autónoma | Confirmación | Cancelación | Reprogramación | Lista de espera |
|---|---|---|---|---|---|
| Dentalink | Documentado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| DentApp | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentify | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| OdontoApp | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Odontoly | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentidesk | Declarado | Declarado | Declarado | No evidenciado | No evidenciado |
| Odonto.me | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| AgendaPro | Documentado | Declarado | Documentado | Declarado | No evidenciado |
| Clinic Cloud | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| XDentalCloud | Declarado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| Open Dental | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentrix | Documentado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| Curve Dental | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| CareStack | Documentado | Declarado | No evidenciado | No evidenciado | No evidenciado |
| tab32 | Declarado | Declarado | No evidenciado | No evidenciado | Declarado |

### S Seguridad y portabilidad

Peso: 10 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | Roles/permisos | Auditoría | Respaldos | Cifrado con alcance claro | Exportación con alcance claro |
|---|---|---|---|---|---|
| Dentalink | Documentado | No evidenciado | No evidenciado | No evidenciado | Declarado |
| DentApp | Declarado | No evidenciado | Declarado | Declarado | No evidenciado |
| Dentify | Declarado | No evidenciado | No evidenciado | Declarado | No evidenciado |
| OdontoApp | Declarado | No evidenciado | No evidenciado | Declarado | No evidenciado |
| Odontoly | Declarado | No evidenciado | Declarado | No evidenciado | No evidenciado |
| Dentidesk | Documentado | No evidenciado | No evidenciado | No evidenciado | Documentado |
| Odonto.me | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| AgendaPro | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Clinic Cloud | Declarado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| XDentalCloud | Declarado | No evidenciado | Declarado | Declarado | No evidenciado |
| Open Dental | Documentado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Dentrix | No evidenciado | No evidenciado | Declarado | No evidenciado | No evidenciado |
| Curve Dental | No evidenciado | No evidenciado | No evidenciado | Declarado | No evidenciado |
| CareStack | No evidenciado | No evidenciado | Declarado | Declarado | No evidenciado |
| tab32 | Declarado | No evidenciado | Declarado | Declarado | Declarado |

### E Precio y adopción

Peso: 5 %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.

| Sistema | Tarifa aplicable publicada | Moneda identificada | Unidad de cobro | Prueba accesible declarada | Extras identificados |
|---|---|---|---|---|---|
| Dentalink | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| DentApp | Documentado | No evidenciado | Documentado | Documentado | No evidenciado |
| Dentify | Documentado | No evidenciado | Documentado | Documentado | No evidenciado |
| OdontoApp | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Odontoly | Documentado | Documentado | Documentado | No evidenciado | No evidenciado |
| Dentidesk | No evidenciado | No evidenciado | No evidenciado | Documentado | Documentado |
| Odonto.me | Documentado | No evidenciado | Documentado | Documentado | No evidenciado |
| AgendaPro | No evidenciado | No evidenciado | No evidenciado | Declarado | Documentado |
| Clinic Cloud | Documentado | Documentado | Documentado | Declarado | Documentado |
| XDentalCloud | Documentado | No evidenciado | No evidenciado | Declarado | Declarado |
| Open Dental | Documentado | Documentado | Documentado | No evidenciado | Documentado |
| Dentrix | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| Curve Dental | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| CareStack | No evidenciado | No evidenciado | No evidenciado | No evidenciado | No evidenciado |
| tab32 | Documentado | No evidenciado | Documentado | Documentado | Documentado |

### Aspectos de agenda que exigen una demostración específica

La matriz anterior no sustituye una prueba de concurrencia. Al demostrar un producto deberían ensayarse duración por prestación, bloqueo de horario, sobreturno autorizado, profesional en dos sillones, un sillón con dos profesionales y citas contiguas. No se ejecutaron estas pruebas en proveedores. En la ficha, «No evidenciado» sobre concurrencia nunca significa que el proveedor carezca del control.

## C Análisis competitivo

### Funcionalidades frecuentes en la muestra

La mayoría de las ofertas dentales consultadas presenta agenda, historia y odontograma; varias añaden imágenes, presupuestos, caja y reportes. Dentalink, Dentidesk, XDentalCloud, Dentrix, CareStack y tab32 publican reserva o comunicación digital. Esto es un patrón descriptivo de la muestra, no una estimación estadística del mercado argentino.

### Diferencias observables

Odontoly declara operación local-first y revela límites de cifrado y acceso remoto; contrasta con las ofertas cloud de DentApp, Dentify, Dentidesk y Curve. DentApp publica Google Calendar y MercadoPago; Dentalink presenta obras sociales y operación clínica. Open Dental ofrece manuales detallados sobre operatorios y bloqueos. Dentidesk explicita condiciones de exportación y cierre. La relevancia de cada diferencia depende del consultorio que el usuario decida atender.

Las promesas de IA, reducción de ausencias, mayor facturación o facilidad de uso siguen siendo afirmaciones comerciales hasta contar con demostraciones y mediciones comparables. No se atribuyen ahorros ni resultados clínicos a partir de testimonios.

### Vacíos de evidencia para el mercado argentino

En las páginas consultadas no suele estar descrita la transacción que evita simultáneamente la ocupación del profesional y del sillón. Dentalink documenta que su agenda online valida a la vez al profesional y al recurso asociado al servicio, sin identificar ese recurso como sillón ni describir el control de concurrencia. Tampoco hay evidencia suficiente para comparar de forma uniforme auditoría, exportación completa y restauración. Una integración fiscal extranjera no prueba ARCA; listar obras sociales no prueba comunicación electrónica con ellas. Estos son vacíos de información pública de esta muestra, no una afirmación de que ningún proveedor los resuelva.

Los precios mezclan profesional, usuario, sede, anualidad, extras, mensajes y migración. Las páginas de AgendaPro requieren distinguir categorías; Clinic Cloud requiere distinguir módulos. La moneda no identificada de Odonto.me y los importes equivalentes anuales de XDentalCloud impiden comparar automáticamente el costo total.

### Oportunidades que deben validarse

- Evidenciar disponibilidad conjunta de profesional y sillón con reglas visibles y pruebas de concurrencia.
- Mostrar al consultorio qué es solicitud, confirmación, cancelación y reprogramación, con trazabilidad de cambios.
- Explicar costos de mensajes, integraciones y portabilidad antes de contratar.
- Facilitar recuperación de cancelaciones o lista de espera, si las entrevistas confirman esa prioridad.
- Limitar los datos solicitados en una reserva y separar permisos administrativos y clínicos.

Estas son hipótesis de oportunidad; no tienen validación de demanda ni disposición a pagar. La selección de funciones en la sección D procede de decisiones explícitas del usuario y no constituye validación comercial de estas hipótesis.

## D Recomendación y alcance aprobados por el usuario

Las elecciones siguientes fueron realizadas por el usuario después de la investigación y aprobadas mediante menú. Son decisiones del proyecto, no una recomendación de compra ni una clasificación de calidad de los proveedores.

### Cinco competidores seleccionados para análisis o demo posterior

| Competidor elegido | Objetivo de contraste |
|---|---|
| Dentalink | Contrastar operación dental y confirmaciones; el manual público permite examinar configuración de agenda. |
| DentApp | Examinar propuesta argentina, roles y las integraciones locales declaradas sin incorporarlas automáticamente al MVP. |
| Dentify | Contrastar agenda de consultorio y ficha sencilla en una oferta que se declara argentina. |
| Dentidesk | Examinar separación de perfiles y condiciones de exportación documentadas. |
| AgendaPro | Examinar claridad del recorrido público de reserva y condiciones por servicio; la autorreserva no se incorpora al MVP. |

No se contactó a proveedores ni se crearon cuentas. Las demos siguen como trabajo posterior y no se presentan como realizadas; la revisión personal de fuentes figura en «Verificación de fuentes».

### Tres referencias de experiencia de usuario elegidas

| Referencia | Qué estudiar |
|---|---|
| Dentalink | Configuración y presentación de opciones de agenda en documentación oficial. |
| AgendaPro | Interfaz pública con servicios, duraciones y condiciones de reserva. |
| Dentrix | Flujo documentado de motivo, profesional y horario; artículo histórico acompañado por página vigente. |

Estas referencias no autorizan copiar una interfaz ni agregar autorreserva general al MVP. La participación del paciente se limita a lo aprobado en esta revisión. La política visual se mantiene diferida hasta la fase correspondiente.

### MVP aprobado y funcionalidades imprescindibles

- Gestión de pacientes y ficha clínica sencilla con antecedentes y notas.
- Agenda por sillón para varios odontólogos en una sede, con vista diaria y semanal, duración variable, bloqueos y control de solapamientos.
- Creación de turnos con profesional y sillón/box obligatorios y disponibles.
- Duración definida por prestación, ajustable por el equipo al crear el turno.
- Intervalo configurable común para toda la sede, cuyo valor inicial sigue pendiente.
- Reprogramación y cancelación de turnos con las reglas aprobadas.
- Listado de turnos por confirmar y registro de confirmaciones manuales.
- Separación del acceso clínico: lectura para recepción y edición solo para odontólogos.
- Email al crear el turno con enlace para cancelar; recordatorio automático 24 horas antes, sin opción de cancelar cuando el plazo venció.
- Cancelación del paciente por enlace hasta 48 horas antes del turno.
- Cuenta para gestionar la inscripción en lista de espera y aviso automático de huecos por email, con asignación final por recepción.

Estas funciones definen el MVP del roadmap; no significan que todas se implementen en el único primer change requerido por la consigna. La elección de ese change se realizará después del roadmap.

### Usuarios y reglas de la revisión aprobada

- Recepción/administración: gestiona la agenda, registra confirmaciones y realiza el seguimiento manual. Puede leer la ficha clínica.
- Odontólogos: operan el sistema y pueden leer y editar los antecedentes y notas clínicas. Solo ellos pueden editar la información clínica.
- Paciente: recibe emails, cancela su turno desde un enlace y gestiona con cuenta su inscripción en la lista de espera. La creación general de turnos y la asignación de huecos siguen a cargo del equipo; no hay autorreserva general. El mecanismo técnico de la cuenta y los permisos detallados siguen pendientes.

La cantidad concreta de profesionales y sillones está pendiente. El mecanismo de autenticación y el detalle de permisos por operación se especificarán antes de implementar; no se inventan aquí roles adicionales con acceso total.

- Todo turno requiere profesional y sillón/box; ninguno puede estar ocupado por otro turno durante el horario reservado.
- Entre turnos debe respetarse un intervalo configurable único para toda la sede. No se considera cero ni se fija una cantidad de minutos por defecto en este documento.
- La duración se obtiene de la prestación, pero el equipo puede ajustarla al crear el turno.
- Al reprogramar pueden cambiar fecha, hora, profesional y sillón, siempre validando la disponibilidad. Se conservan prestación y duración.
- Al cancelar se libera inmediatamente el horario, sin esperar una liberación manual posterior.
- El equipo crea todos los turnos del MVP. La confirmación se registra y sigue manualmente por recepción.
- Recepción puede leer la ficha clínica; solo los odontólogos editan antecedentes y notas.
- Recepción bloquea sillón y/o profesional por período; se rechaza el bloqueo si afecta turnos existentes.
- El paciente cancela mediante enlace hasta 48 horas antes. Recibe el enlace en el email de creación; el recordatorio se envía 24 horas antes y no ofrece cancelación si el plazo venció. El instante exacto del límite y los turnos creados dentro de esos plazos se especificarán antes de implementar.
- La lista de espera registra prestación, rango de fechas y franjas horarias, con profesional opcional. Al liberarse un hueco se avisa por email a los interesados; recepción confirma quién lo toma, sin reserva ni adjudicación automática.

El valor inicial del intervalo, los estados exactos y el comportamiento preciso ante una reprogramación fallida quedan abiertos para especificación. No se admiten sobreturnos por inferencia: la regla aprobada impide ocupaciones simultáneas de los recursos.

### Diferenciadores buscados dentro del alcance aprobado

Se priorizan:

- Reglas verificables sobre profesional y sillón.
- Intervalo configurable común para la sede.
- Separación entre lectura administrativa y edición clínica.

Son objetivos de calidad, no exclusividad comercial demostrada. La reducción de ausencias y huecos mediante emails y lista de espera todavía requiere medición.

### Funcionalidades para etapas posteriores

- Autorreserva general del paciente.
- Adjudicación automática de huecos.
- WhatsApp automático.
- Odontograma.
- Pagos.
- Facturación.

No se asigna prioridad ni compromiso a esas ampliaciones. El MVP define el backlog, mientras que la entrega académica debe llevar un único change por el ciclo completo. Todavía no se seleccionó ese change.

### Integraciones

El email es una integración obligatoria del MVP revisado: deben demostrarse envíos reales a direcciones de prueba del equipo con una opción gratuita aún por definir. No se elige proveedor ni se envían correos durante esta consolidación. El seguimiento manual de confirmaciones se conserva junto a los emails automáticos; no implica WhatsApp. Google Calendar, pagos y servicios fiscales no se incorporan por defecto.

PostgreSQL es parte del stack elegido por el usuario y no un servicio externo contratado. La ubicación y el modo de ejecución de la base de datos siguen sin decidirse.

### Restricciones

- Entrega: 25 de noviembre de 2026.
- Equipo: cuatro integrantes, cantidad que puede variar. El usuario informó autorización docente para superar el máximo de tres de la consigna; no se atribuye una verificación documental independiente a esa declaración.
- Presupuesto para servicios externos: cero.
- Frontend: HTML, CSS y TypeScript vanilla.
- Backend: Java con Spring Boot.
- Base de datos: PostgreSQL.
- Todos los ejemplos, pruebas y capturas deben usar datos clínicos ficticios, como exige la consigna. Para la demostración de email se usarán direcciones de prueba indicadas por el equipo.
- El trabajo académico requiere un único change completo, especificado, probado y archivado; no una implementación apresurada de todo el MVP.

Las versiones concretas, el despliegue y el reparto nominal de tareas no están definidos. La selección de estas tecnologías fue explícita del usuario; no se presenta como conclusión obligada del análisis competitivo.

### Riesgos

Los riesgos que preocupan especialmente al usuario son el manejo incorrecto de datos clínicos/permisos y los errores de solapamiento o reprogramación. No estableció un orden entre esos dos riesgos. El plazo también preocupa, aunque menos; su confianza en llegar no constituye una garantía de ejecución.

El control de agenda necesita escenarios verificables sobre ambos recursos y el intervalo. La separación de permisos necesita comprobar que recepción no pueda modificar información clínica. La efectividad del seguimiento manual para reducir inasistencias todavía no fue medida con usuarios reales.

La ampliación añade riesgos técnicos de acceso indebido por cuenta/enlace, emails fallidos o duplicados, límites del proveedor gratuito y avisos de huecos ya ocupados. Son riesgos identificados, no un nuevo orden de preocupación elegido por el usuario.

Persisten límites de investigación: declaraciones comerciales no auditadas, precios y planes cambiantes y condiciones de portabilidad incompletas. Las capacidades no evidenciadas de los competidores siguen como preguntas, no se convierten en supuestas ausencias.

## Verificación de fuentes

### Revisión personal del equipo

Cuatro integrantes revisaron cinco fuentes de distintos tipos. Giovanni Colombo transmitió los resultados el 25/11/2026; no consta la fecha de cada apertura. «Hallazgo» recoge lo informado por quien revisó. En «Corrección y contraste» se indica qué cambió y qué comprobó después el agente.

| Integrante | Tipo y fuente | Hallazgo | Corrección y contraste |
|---|---|---|---|
| Giovanni Colombo | Sitio oficial: [DentApp](https://www.dentapp.ar/software-odontologico-argentina) | Sin diferencias. | Sin cambios. |
| Giovanni Colombo | Precios: [tab32](https://tab32.com/pricing/) | Diferencias de precio según el servicio; para el servicio dental, un precio más alto, cercano a $400. | Ficha 15: se agregaron los cargos por servicio. Giovanni Colombo confirmó el 25/11 que el importe observado era $427. En el comparador de la página, esa cifra es el costo mensual estimado de Open Dental; para tab32 estima $179 y su precio publicado sigue desde $95. El precio base no cambia. |
| Sofia Martinez | Centro de ayuda: [Dentalink](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online) | Sin diferencias con los datos obtenidos. | Sin cambios. |
| Fabiano Ramos | Términos de uso: [Dentidesk](https://app.dentidesk.com/terminos.php) | La prueba es de 15 días, no de 30; después empiezan los cargos. | La ficha 06 ya indicaba 15 días (punto 2.2). Los 30 días son el plazo para pedir la exportación tras el término (punto 7.6). Se aclaró la diferencia y se agregó que luego de la prueba se requiere suscripción. |
| Luciano Izquierdo Cafici | Interfaz pública: [AgendaPro](https://agendapro.com/site/ar/odontologiaor/239004) | Sin diferencias, luego de simular una reserva en la app, que consideró bastante buena. | Sin cambios en las celdas. El agente había recibido HTTP 403 ese día. |

### Comprobaciones realizadas por el agente

Las comprobaciones siguientes son automatizadas y se muestran aparte de la revisión personal, a la que **no sustituyen**.

| Tipo y fuente | Afirmación contrastada | Hallazgo y corrección | URL | Revisión personal |
|---|---|---|---|---|
| Sitio oficial: DentApp | Proveedor argentino, nube e integraciones locales | La página específica identifica Codilium S.A.S. y publica Google Calendar/MercadoPago. No se verificó inscripción registral. | [Abrir](https://www.dentapp.ar/software-odontologico-argentina) | Giovanni Colombo: sin diferencias |
| Precios: tab32 | El buscador mostraba $125/mes | La apertura vigente mostró desde $95/mes; se descartó el precio indexado anterior y se registraron cargos variables. | [Abrir](https://tab32.com/pricing/) | Giovanni Colombo: diferencia de precio |
| Ayuda: Dentalink | Reserva mediante enlace y configuración profesional | El manual explica enlace, permisos, bloques y profesionales habilitados. No prueba disponibilidad conjunta de sillón y profesional. Corregido el 25/11: ver la segunda comprobación. | [Abrir](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online) | Sofia Martinez: sin diferencias |
| Términos: Dentidesk | Exportación de datos | Licencia activa: Excel estándar sin costo; después del término: solicitud de exportación hasta 30 días corridos y posible cargo. Ese plazo no es la prueba gratuita, que dura 15 días. No se redujo todo a exportación gratuita. | [Abrir](https://app.dentidesk.com/terminos.php) | Fabiano Ramos: prueba de 15 días |
| Interfaz pública: AgendaPro | Reserva y duración variable por servicio | Se observaron servicios con diferentes duraciones y botón Agendar; no se completó reserva ni se copiaron reseñas o datos de pacientes. | [Abrir](https://agendapro.com/site/ar/odontologiaor/239004) | Luciano Izquierdo Cafici: sin diferencias |
| Precios: Clinic Cloud | La extracción inicial señalaba promoción de dos meses | La apertura posterior mostró 50% durante tres meses. Se priorizó la página abierta y se aclaró que Max incluye odontograma. | [Abrir](https://clinic-cloud.com/tarifas) | Sin revisión personal |
| Sitio y FAQ: Dentify | País, cifrado y precio anual de extracción inicial | La portada actual declara Hecho en Argentina, nube cifrada y $30.000/$45.000 mensuales. Se retiró el anual no corroborado y no se inventó código de moneda. Corregido el 25/11: la modalidad anual sí está publicada. | [Abrir](https://www.dentify.com.ar/) | Sin revisión personal |
| Producto: Odontoly | WhatsApp y ARCA podrían parecer disponibles por figurar en catálogo | El sitio los identifica como complementos próximos. También distingue backups cifrados de base de trabajo aún sin cifrado por la app. | [Abrir](https://odontoly.com/) | Sin revisión personal |

### Segunda comprobación del agente (25/11/2026)

El agente intentó volver a abrir en un navegador las ocho fuentes anteriores y contrastó con este informe el texto publicado ese día en las siete que respondieron. No sustituye la revisión personal. Por decisión del 25/11/2026 (Ajustar), las diferencias se incorporaron a las fichas, la matriz B y la sección C. Una diferencia puede deberse a una omisión del relevamiento o a un cambio de la página posterior al 17/11/2026; salvo indicación, no se distingue cuál.

En su primera pasada de ese día el agente no vio el adicional de Clinic Cloud, publicado en un texto emergente, ni los importes anuales de Dentify, visibles al cambiar un selector. Una segunda pasada corrigió ambas observaciones.

| Fuente | Resultado | Diferencia o detalle | Corrección aplicada |
|---|---|---|---|
| DentApp | Coinciden Codilium S.A.S., nube, Google Calendar, MercadoPago, roles y ReNaPDiS declarado. | Publica 30 días de prueba y precios mensuales con IVA ($24.900, $54.900 y $74.900; Empresarial a medida), recordatorios según plan, API y webhooks en Empresarial y «backups diarios». | Ficha 02 y cuatro celdas de la matriz B. |
| tab32 | Coinciden desde $95/mes, usuarios ilimitados, prueba de 14 días, cargos por claim, adjuntos y elegibilidad, y que los ledgers no se migran. | La prueba pide email y tarjeta; la migración se publica desde $1,250; la exportación gratuita al salir incluye el ledger. | Ficha 15. |
| Dentalink | Coinciden el enlace de reserva, los permisos y los bloqueos de horario. | El manual (fechado el 12/06/2026) tiene una sección plegada, «Cruce con Recursos», que declara que la agenda online valida que el profesional y el recurso asociado al servicio estén disponibles en el mismo horario. No identifica el recurso como sillón ni describe control de concurrencia. | Ficha 01 y sección C. La matriz B no cambia. |
| Dentidesk | Coinciden Excel estándar sin costo con licencia activa, solicitud hasta 30 días después del término con posible cargo y prueba de 15 días. | Cada cuenta de odontólogo adicional «generará un cargo según el plan contratado»; el informe lo expresaba como posible. | Ficha 06. |
| AgendaPro | No se pudo comprobar. | La interfaz pública respondió HTTP 403. No se reintentó ni se evadió el bloqueo. | Se anota en la ficha 08; las celdas conservan la observación del 17/11. |
| Clinic Cloud | Coinciden 29, 49 y 79 EUR más IVA, 50 % durante tres meses, 10 EUR por agenda extra, odontograma en Max y reserva por Doctoralia. | Declara más de 3.000 consultas y clínicas en España y exhibe ISO/IEC 27001. | Ficha 09: adopción y sello declarados. |
| Dentify | Coinciden $30.000 y $45.000 mensuales, 14 días sin tarjeta, «Hecho en Argentina» y nube cifrada. | La modalidad anual publica $25.000 y $38.000 por mes ($300.000 y $456.000 por año); el informe había descartado el anual. El pie indica «Desarrollado por IB Digital». | Ficha 03. |
| Odontoly | Coinciden USD 149,99 sin permanencia; WhatsApp Business, SMS, ARCA y visor RX como próximos; copias cifradas y base de trabajo sin cifrar; Windows en desarrollo. | El plan publica además dos usuarios y hasta cuatro dispositivos autorizados. | Ficha 05. |

### Fuentes no accesibles y exclusiones

Doctoralia PRO Argentina devolvió 403 en sus páginas de agenda/precio; no se afirmó un precio a partir de resultados indexados. DentalTap tuvo timeouts/502 en producto y precios; se localizó un manual, pero no se lo usó para rellenar prestaciones no corroboradas. Se sustituyeron esos candidatos en el núcleo por Dentidesk y Odonto.me. DriCloud se exploró como software médico general y se reemplazó por XDentalCloud, sin trasladar funcionalidades entre productos. No se usaron mecanismos de evasión, cuentas personales ni contacto comercial.

No se verificaron reseñas externas como parte de este corte. Las cifras de adopción que aparecen están atribuidas al proveedor. Ausencia de reseñas verificadas no implica ausencia de clientes.

## Preguntas abiertas tras la aprobación

El checklist completo de once puntos y las decisiones aprobadas están en discovery.md y decisiones-aprobadas.json. La aprobación conserva estos pendientes y no autoriza resolverlos por inferencia:

- Minutos iniciales del intervalo global.
- Cantidad de profesionales y sillones/boxes.
- Estados exactos del turno y transiciones.
- Campos mínimos de paciente y ficha clínica.
- Comportamiento exacto ante una reprogramación fallida.
- Nombres de integrantes y reparto de trabajo.
- Versiones concretas de Java, Spring Boot, PostgreSQL y herramientas de frontend.
- Revisión personal de al menos cinco fuentes por integrantes. Informada el 25/11/2026.
- Proveedor gratuito de email, límites y configuración de direcciones de prueba.
- Registro y vinculación de la cuenta con el paciente y permisos sobre datos propios.
- Seguridad, vigencia y uso de enlaces; tratamiento exacto del límite de 48 horas.
- Turnos creados dentro de las 48 o 24 horas; efectos de reprogramación y cancelación sobre emails.
- Duplicados, fallos, reintentos y constancia de envío.
- Gestión de desbloqueos y límites temporales de bloqueos.
- Compatibilidad exacta de huecos con duración, recursos, intervalo y preferencias; disponibilidad al asignar y comunicación si ya fue ocupado.

Discovery, KB y roadmap se consolidan con la revisión 2 aprobada. El primer change todavía no fue elegido y no se inició implementación, se seleccionó proveedor o se enviaron emails. La revisión personal de cinco fuentes, pendiente al consolidar, se informó el 25/11/2026 y figura en «Verificación de fuentes».

## Referencias completas

Todas las referencias siguientes fueron consultadas el 2026-11-17; las que indican una nueva consulta se volvieron a abrir, o se intentó abrirlas, en esa segunda fecha. Las rutas enlazadas son fuentes primarias abiertas; las limitaciones se describen en sus fichas. Las capacidades comerciales y las condiciones pueden cambiar.

- **F01.1 - Dentalink: Sitio argentino**. [https://web-ar.softwaredentalink.com/](https://web-ar.softwaredentalink.com/). Consulta: 2026-11-17.
- **F01.2 - Dentalink: Manual de agenda online**. [https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F02.1 - DentApp: Producto y país**. [https://www.dentapp.ar/software-odontologico-argentina](https://www.dentapp.ar/software-odontologico-argentina). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F02.2 - DentApp: Funcionalidades y planes**. [https://www.dentapp.ar/](https://www.dentapp.ar/). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F03.1 - Dentify: Producto, FAQ y precios**. [https://www.dentify.com.ar/](https://www.dentify.com.ar/). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F03.2 - Dentify: Página de consultorio**. [https://www.dentify.com.ar/software-consultorio-odontologico](https://www.dentify.com.ar/software-consultorio-odontologico). Consulta: 2026-11-17.
- **F04.1 - OdontoApp: Portada parcial**. [https://odontoapp.com.ar/](https://odontoapp.com.ar/). Consulta: 2026-11-17.
- **F04.2 - OdontoApp: Política de privacidad**. [https://odontoapp.com.ar/privacidad](https://odontoapp.com.ar/privacidad). Consulta: 2026-11-17.
- **F05.1 - Odontoly: Producto, restricciones y precios**. [https://odontoly.com/](https://odontoly.com/). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F06.1 - Dentidesk: Características**. [https://www.dentidesk.cl/dentidesk/feature](https://www.dentidesk.cl/dentidesk/feature). Consulta: 2026-11-17.
- **F06.2 - Dentidesk: Términos de uso**. [https://app.dentidesk.com/terminos.php](https://app.dentidesk.com/terminos.php). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F06.3 - Dentidesk: Ayuda roles y profesionales**. [https://dentidesk.freshdesk.com/support/solutions/articles/150000201446--c%C3%B3mo-agregar-doctores-y-personal-a-mi-cl%C3%ADnica-en-dentidesk-](https://dentidesk.freshdesk.com/support/solutions/articles/150000201446--c%C3%B3mo-agregar-doctores-y-personal-a-mi-cl%C3%ADnica-en-dentidesk-). Consulta: 2026-11-17.
- **F07.1 - Odonto.me: Producto y planes**. [https://odonto.me/?locale=es](https://odonto.me/?locale=es). Consulta: 2026-11-17.
- **F08.1 - AgendaPro: Oferta dental**. [https://agendapro.com/es/dental/software-odontologico](https://agendapro.com/es/dental/software-odontologico). Consulta: 2026-11-17.
- **F08.2 - AgendaPro: Planes Argentina**. [https://agendapro.com/ar/planes](https://agendapro.com/ar/planes). Consulta: 2026-11-17.
- **F08.3 - AgendaPro: Interfaz pública de reserva**. [https://agendapro.com/site/ar/odontologiaor/239004](https://agendapro.com/site/ar/odontologiaor/239004). Consulta: 2026-11-17. Nueva consulta: 2026-11-25 (respondió HTTP 403).
- **F09.1 - Clinic Cloud: Tarifas y módulos**. [https://clinic-cloud.com/tarifas](https://clinic-cloud.com/tarifas). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F09.2 - Clinic Cloud: Ayuda presupuesto y odontograma**. [https://clinic-cloud.com/faq/agenda-y-comunicaciones/presupuestos](https://clinic-cloud.com/faq/agenda-y-comunicaciones/presupuestos). Consulta: 2026-11-17.
- **F10.1 - XDentalCloud: Producto**. [https://xdentalcloud.com/](https://xdentalcloud.com/). Consulta: 2026-11-17.
- **F10.2 - XDentalCloud: Precios**. [https://xdentalcloud.com/precios/](https://xdentalcloud.com/precios/). Consulta: 2026-11-17.
- **F10.3 - XDentalCloud: Agenda**. [https://xdentalcloud.com/agenda-dental/](https://xdentalcloud.com/agenda-dental/). Consulta: 2026-11-17.
- **F10.4 - XDentalCloud: Seguridad**. [https://xdentalcloud.com/seguridad/](https://xdentalcloud.com/seguridad/). Consulta: 2026-11-17.
- **F10.5 - XDentalCloud: Empresa**. [https://xdentalcloud.com/empresa/](https://xdentalcloud.com/empresa/). Consulta: 2026-11-17.
- **F11.1 - Open Dental: Agenda por operatorios**. [https://www.opendental.com/manual/operatories.html](https://www.opendental.com/manual/operatories.html). Consulta: 2026-11-17.
- **F11.2 - Open Dental: Bloqueos**. [https://www.opendental.com/manual/blockouts.html](https://www.opendental.com/manual/blockouts.html). Consulta: 2026-11-17.
- **F11.3 - Open Dental: Tarifas**. [https://opendental.com/site/fees.html](https://opendental.com/site/fees.html). Consulta: 2026-11-17.
- **F11.4 - Open Dental: Historia clínica**. [https://www.opendental.com/manual/chart.html](https://www.opendental.com/manual/chart.html). Consulta: 2026-11-17.
- **F11.5 - Open Dental: Contacto**. [https://www.opendental.com/site/contact.html](https://www.opendental.com/site/contact.html). Consulta: 2026-11-17.
- **F11.6 - Open Dental: Agenda general**. [https://www.opendental.com/manual/appointments.html](https://www.opendental.com/manual/appointments.html). Consulta: 2026-11-17.
- **F12.1 - Dentrix: Producto**. [https://www.dentrix.com/](https://www.dentrix.com/). Consulta: 2026-11-17.
- **F12.2 - Dentrix: Patient Engage**. [https://www.dentrix.com/dental-solutions/marketing-and-patient-experience/dentrix-patient-engage-suite/](https://www.dentrix.com/dental-solutions/marketing-and-patient-experience/dentrix-patient-engage-suite/). Consulta: 2026-11-17.
- **F12.3 - Dentrix: Flujo documentado de reserva**. [https://magazine.dentrix.com/online-booking-from-the-patients-perspective-2/](https://magazine.dentrix.com/online-booking-from-the-patients-perspective-2/). Consulta: 2026-11-17.
- **F13.1 - Curve Dental: Producto, FAQ, precio y domicilio**. [https://www.curvedental.com/](https://www.curvedental.com/). Consulta: 2026-11-17.
- **F14.1 - CareStack: Plataforma y certificaciones declaradas**. [https://carestack.com/](https://carestack.com/). Consulta: 2026-11-17.
- **F14.2 - CareStack: Reserva online**. [https://carestack.com/dental-software/features/online-scheduling](https://carestack.com/dental-software/features/online-scheduling). Consulta: 2026-11-17.
- **F15.1 - tab32: Producto**. [https://tab32.com/](https://tab32.com/). Consulta: 2026-11-17.
- **F15.2 - tab32: Tarifa vigente abierta**. [https://tab32.com/pricing/](https://tab32.com/pricing/). Consulta: 2026-11-17. Nueva consulta: 2026-11-25.
- **F15.3 - tab32: Empresa y seguridad declarada**. [https://tab32.com/about/](https://tab32.com/about/). Consulta: 2026-11-17.
