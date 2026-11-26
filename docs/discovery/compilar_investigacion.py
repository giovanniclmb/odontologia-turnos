"""Compila fuentes y, si existen, las decisiones explícitamente aprobadas."""
from pathlib import Path
import json, runpy, re
ROOT=Path(__file__).resolve().parent
DATA=json.loads((ROOT/'competidores.json').read_text())
PRODUCTS=DATA['products']
SLUG=runpy.run_path(str(Path.home()/'.agents/skills/web-scraper/scripts/slugify.py'))['slugify']
GROUPS=[
 ('T','Turnos y automatización',25,['Vista diaria/semanal','Agenda por profesional','Agenda por sillón/box','Reserva online','Recordatorios']),
 ('C','Clínica odontológica',20,['Historia clínica','Odontograma','Periodontograma','Imágenes','Planes de tratamiento']),
 ('I','Integraciones locales y WhatsApp',15,['WhatsApp clínico','Google Calendar','Mercado Pago','ARCA operativa','API pública']),
 ('A','Administración y cobros',15,['Caja','Cobros','Facturación general','Obras sociales argentinas','Reportes']),
 ('P','Experiencia del paciente',10,['Reserva autónoma','Confirmación','Cancelación','Reprogramación','Lista de espera']),
 ('S','Seguridad y portabilidad',10,['Roles/permisos','Auditoría','Respaldos','Cifrado con alcance claro','Exportación con alcance claro']),
 ('E','Precio y adopción',5,['Tarifa aplicable publicada','Moneda identificada','Unidad de cobro','Prueba accesible declarada','Extras identificados'])]
LABEL={'C':'Declarado','D':'Documentado','N':'No evidenciado'}
VALUE={'C':0.5,'D':1,'N':0}
def table(headers,rows):
 def cell(v):return str(v).replace('|','/').replace('\n',' ')
 return '\n'.join(['| '+' | '.join(map(cell,headers))+' |','|'+'|'.join(['---']*len(headers))+'|']+['| '+' | '.join(map(cell,r))+' |' for r in rows])+'\n'
def ref(i,j,p):
 title,url=p['sources'][j-1]
 return f'[F{i:02}.{j} {title}]({url})'
for i,p in enumerate(PRODUCTS,1):
 refs='; '.join(ref(i,j,p) for j in range(1,len(p['sources'])+1))
 rechecked=' Parte de ellas se volvió a revisar el 25/11/2026.' if p.get('rechecks') else ''
 note=f'''# {p['name']}

- **URL**: {p['sources'][0][1]}
- **Fecha de investigación**: {p['date']}

## Problema que resuelve

Centralización de agenda y gestión de pacientes según el posicionamiento del proveedor; alcance: {p['segment']}.

## Usuarios objetivo

{p['segment']}. Proveedor: {p['provider']}. País: {p['country']}. Modalidad: {p['mode']}.

## Features detectadas

- Agenda: {p['agenda']}
- Paciente y automatización: {p['digital']}
- Clínica: {p['clinical']}
- Administración: {p['admin']}
- Integraciones: {p['integration']}
- Seguridad y portabilidad: {p['security']}

## Pricing

{p['price']}

## Posicionamiento

{p['mode']} para {p['segment'].lower()}. No se adopta el posicionamiento comercial como validación de resultados.

## Diferenciadores

Aspectos observables para contrastar: {p['limits']}

## Notas del investigador

{p['adoption']}

Fuentes consultadas: {refs}. Todas consultadas el {p['date']}.{rechecked} Declaraciones comerciales no equivalen a una prueba independiente. Documentación e interfaz pública acreditan lo publicado, no el comportamiento de una instalación real. No se crearon cuentas ni se enviaron formularios.
'''
 (ROOT/'sources'/f'{SLUG(p["name"])}.md').write_text(note)

intro='''# Investigación de mercado de gestión odontológica en Argentina

Desarrollo de Software - Trabajo Global

Fecha de consulta: 17 de noviembre de 2026. Integrantes y datos de portada: pendientes de informar.

## Resumen ejecutivo

Se investigaron 15 soluciones con páginas oficiales abiertas, priorizando oferta argentina, luego soluciones latinoamericanas y referencias internacionales. La muestra cubre sistemas dentales y una plataforma multisector comercializada para odontología. El orden refleja pertinencia documental y geográfica, no participación de mercado ni calidad medida.

Agenda, ficha del paciente, odontograma y administración aparecen repetidamente en la oferta dental. La reserva por enlace y los recordatorios también están extendidos en las páginas revisadas. Por eso esas prestaciones, por sí solas, no acreditan una diferenciación comercial. Las capacidades de asignación simultánea de profesional y sillón, las condiciones de exportación y el costo completo requieren comprobaciones adicionales en buena parte de la muestra.

Se encontraron precios indexados distintos de las aperturas actuales, promociones cambiantes, módulos dependientes del plan y prestaciones anunciadas como próximas. El relevamiento conserva estas limitaciones. No se realizó una auditoría técnica, prueba de carga, compra ni demo con cuenta.

Este documento es el resultado de investigación previo a la entrevista. El usuario pidió responder las preguntas después del relevamiento. Segmento objetivo, roles, MVP, integraciones, reglas, presupuesto y stack siguen sin decidirse. No se creó la KB, el roadmap ni un change. La sección D se completará con las respuestas y la aprobación del usuario.

## Alcance y método

Se consultaron páginas puntuales, sin crawling masivo ni evasión de bloqueos. Solo se incorporó al núcleo comparativo información recuperada de fuentes oficiales; los extractos de buscador se usaron para localizar páginas, no como sustituto de una apertura. Si una información no fue demostrada en las fuentes examinadas, se escribió «No evidenciado». Eso significa desconocimiento en esta muestra, no ausencia en el producto.

Se distingue **Documentado** (manual con procedimiento, interfaz pública o condición textual verificable) de **Declarado** (afirmación comercial del proveedor). Ninguna etiqueta equivale a prueba operativa independiente o garantía de cumplimiento. El domicilio publicado no acredita por sí solo el origen histórico de la empresa; se marca la diferencia donde corresponde.

No se asume que WhatsApp para soporte, compartir un enlace, recordatorios semiautomáticos y WhatsApp Business API sean equivalentes. Tampoco se equiparan una solicitud pendiente y una reserva confirmada, imágenes almacenadas y radiología integrada, ni un reporte Excel y una exportación clínica completa.

**Datos no relevados de forma sistemática.** La consigna de investigación pide además datos que no integran la matriz ni se relevaron de manera uniforme: duración variable, bloqueos y sobreturnos; chatbot y redes sociales; seguimiento de ausentes, recuperación de pacientes, controles periódicos y campañas; anamnesis; señas; sitio web, radiología y firma digital; sucursales, soporte y capacitación; privacidad y adecuación a la normativa argentina; plan gratuito; y una síntesis de fortalezas y diferenciales por sistema. Algunas fichas los mencionan. Cuando una ficha no dice nada sobre uno de ellos, no se relevó: no debe leerse como «No evidenciado» ni como ausencia. Las referencias tampoco incluyen videos oficiales, marketplaces ni reseñas verificadas.

## A Comparación de sistemas

### Identificación y pertinencia

'''
rows=[]
for i,p in enumerate(PRODUCTS,1):
 rows.append([f'{i}. {p["name"]}',p['provider'],p['country'],p['segment'],p['mode'],ref(i,1,p),p['date']])
md=intro+table(['Sistema','Proveedor','País evidenciado','Segmento','Modalidad','URL oficial','Consulta'],rows)
md+='''
La oferta argentina encabeza la tabla. Dentidesk y Odonto.me aportan referencias en español; AgendaPro tiene oferta y reservas públicas en Argentina. Clinic Cloud y XDentalCloud son referencias de módulos y condiciones; Open Dental, Dentrix, Curve, CareStack y tab32 aportan patrones internacionales cuya comercialización y localización argentina no se presumen.

'''
md+='''### Funciones principales

«Documentado» corresponde a funcionalidad comprobada en documentación, interfaz pública o condición textual; «Declarado», a afirmación comercial del proveedor. Los valores son los de la matriz B. Las fuentes se consultaron el 17/11/2026; las consultas posteriores constan en las referencias.

'''
def feature(p,group,name):
 index=[g[0] for g in GROUPS].index(group)
 return LABEL[p['criteria'][index][GROUPS[index][3].index(name)]]
FEATURES=[('T','Reserva online'),('T','Recordatorios'),('T','Agenda por sillón/box'),('C','Historia clínica')]
md+=table(['Sistema']+[n for _,n in FEATURES]+['Fuentes'],[[p['name']]+[feature(p,g,n) for g,n in FEATURES]+[' '.join(f'[F{i:02}.{j}]({url})' for j,(_,url) in enumerate(p['sources'],1))] for i,p in enumerate(PRODUCTS,1)])
md+='''
### Fichas comparables y trazabilidad

Las fuentes F01 a F15 se enumeran completas al final. Las fichas complementan las matrices; una capacidad pendiente de prueba no se convierte en requisito del producto que construiremos.

'''
for i,p in enumerate(PRODUCTS,1):
 md+=f'### {i:02} {p["name"]}\n\n'
 md+='Fuentes: '+ '; '.join(ref(i,j,p) for j in range(1,len(p['sources'])+1))+'.\n\n'
 for key,title in [('agenda','Agenda y recursos'),('digital','Turnos digitales y automatización'),('clinical','Clínica'),('admin','Administración'),('integration','Integraciones'),('security','Seguridad y exportación'),('price','Modelo comercial'),('limits','Fortalezas y límites de la evidencia'),('adoption','Adopción')]:
  md+=f'- **{title}**: {p[key]}\n'
 md+='\n'
md+='''## B Matriz de puntuación

Se aplican los pesos de la consigna. Para que el cálculo sea auditable sin probar productos ni elegir un ganador, esta versión mide **cobertura de evidencia pública**, no calidad real, seguridad efectiva, relación precio/valor ni conveniencia de compra. La interpretación y cualquier ranking comercial requieren revisión antes de usarse para decidir. Los resultados no eligen el MVP.

Cada dimensión tiene cinco criterios. Por criterio: Documentado = 1 punto; Declarado = 0,5; No evidenciado = 0. La suma de cada dimensión va de 0 a 5. Una función futura no suma como disponible. La falta de evidencia reduce la puntuación sin afirmar que la función no existe. La columna de precio mide transparencia y condiciones de adopción, no asequibilidad comparada entre monedas.

Total ponderado = T × 0,25 + C × 0,20 + I × 0,15 + A × 0,15 + P × 0,10 + S × 0,10 + E × 0,05. Se calcula sin redondear los componentes y se muestran tres decimales. Las matrices detalladas de criterios explican cada componente. No mezclar planes o países para concluir que todo está incluido en una misma tarifa.

Tras la segunda comprobación del 25/11/2026 cambiaron cuatro celdas de DentApp (respaldos, tarifa publicada, unidad de cobro y prueba), y su total pasó de 1,275 a 1,475. La validación de recursos documentada por Dentalink no suma en «Agenda por sillón/box» porque el manual no identifica el recurso como sillón.

'''
summary=[]
for p in PRODUCTS:
 vals=[sum(VALUE[k] for k in seq) for seq in p['criteria']]
 total=sum(v*g[2]/100 for v,g in zip(vals,GROUPS))
 p['scores']={g[0]:v for g,v in zip(GROUPS,vals)};p['weighted_total']=round(total,3)
 summary.append([p['name']]+[f'{v:.1f}' for v in vals]+[f'{total:.3f}'])
md+=table(['Sistema','T 25%','C 20%','I 15%','A 15%','P 10%','S 10%','E 5%','Total /5'],summary)+'\n'
for idx,(key,name,weight,criteria) in enumerate(GROUPS):
 md+=f'### {key} {name}\n\nPeso: {weight} %. Las fuentes de cada fila son las mismas referencias F del producto correspondiente.\n\n'
 md+=table(['Sistema']+criteria,[[p['name']]+[LABEL[v] for v in p['criteria'][idx]] for p in PRODUCTS])+'\n'
md+='''### Aspectos de agenda que exigen una demostración específica

La matriz anterior no sustituye una prueba de concurrencia. Al demostrar un producto deberían ensayarse duración por prestación, bloqueo de horario, sobreturno autorizado, profesional en dos sillones, un sillón con dos profesionales y citas contiguas. No se ejecutaron estas pruebas en proveedores. En la ficha, «No evidenciado» sobre concurrencia nunca significa que el proveedor carezca del control.

## C Análisis competitivo

Las cifras indican en cuántos de los 15 sistemas figura cada función como Documentado o Declarado en la matriz B. Describen la evidencia pública de esta muestra: no son una estimación estadística del mercado argentino ni prueban que la función opere. Se excluye la dimensión de precio y adopción, que mide transparencia comercial.

'''
coverage=[]
for index,(key,_,_,criteria) in enumerate(GROUPS):
 if key=='E':continue
 for position,name in enumerate(criteria):
  marks=[(p['name'],p['criteria'][index][position]) for p in PRODUCTS]
  coverage.append({'name':name,'documented':[n for n,m in marks if m=='D'],'declared':[n for n,m in marks if m=='C']})
def total(f):return len(f['documented'])+len(f['declared'])
STANDARD_MIN,RARE_MAX=10,4
standard=sorted((f for f in coverage if total(f)>=STANDARD_MIN),key=total,reverse=True)
rare=sorted((f for f in coverage if total(f)<=RARE_MAX),key=total)
md+=f'''### Funcionalidades estándar de mercado

Se consideran estándar en la muestra las funciones presentes en al menos {STANDARD_MIN} de los 15 sistemas. Predominan las declaraciones comerciales: la columna «Documentado» muestra cuántos casos tienen respaldo más allá de la afirmación. Ofrecer estas funciones no diferencia por sí solo a un producto.

'''
md+=table(['Función','Sistemas (de 15)','Documentado'],[[f['name'],total(f),len(f['documented'])] for f in standard])
md+=f'''
### Funcionalidades diferenciadoras reales

Se exige que la función sea poco frecuente ({RARE_MAX} sistemas o menos) y que al menos un caso esté documentado. Una afirmación comercial aislada no alcanza. La rareza refleja lo publicado en las fuentes y no prueba que los demás sistemas carezcan de la función.

'''
md+=table(['Función','Documentado en','Declarado en'],[[f['name'],', '.join(f['documented']),', '.join(f['declared']) or 'Ninguno'] for f in rare if f['documented']])
md+='''
Fuera de los criterios de la matriz, Dentalink documenta que su agenda online valida a la vez al profesional y al recurso asociado al servicio; Dentidesk explicita condiciones de exportación y cierre; Open Dental ofrece manuales detallados sobre operatorios y bloqueos, y Odontoly declara operación local-first con límites de cifrado y acceso remoto. La relevancia de cada diferencia depende del consultorio que se decida atender.

Las promesas de IA, reducción de ausencias, mayor facturación o facilidad de uso siguen siendo afirmaciones comerciales hasta contar con demostraciones y mediciones comparables. No se atribuyen ahorros ni resultados clínicos a partir de testimonios.

'''
md+=f'''### Vacíos frecuentes del mercado argentino

Funciones evidenciadas en {RARE_MAX} sistemas o menos. La muestra incluye ofertas internacionales y un valor bajo, incluso cero, expresa falta de información pública: no demuestra que ningún proveedor lo resuelva.

'''
md+=table(['Función','Sistemas (de 15)','Documentado'],[[f['name'],total(f),len(f['documented'])] for f in rare])
md+='''
En las páginas consultadas no suele estar descrita la transacción que evita simultáneamente la ocupación del profesional y del sillón. Una integración fiscal extranjera no prueba ARCA; listar obras sociales no prueba comunicación electrónica con ellas. Los precios mezclan profesional, usuario, sede, anualidad, extras, mensajes y migración: las páginas de AgendaPro requieren distinguir categorías, Clinic Cloud requiere distinguir módulos, y la moneda no identificada de Odonto.me y los importes equivalentes anuales de XDentalCloud impiden comparar automáticamente el costo total.

'''
count={f['name']:total(f) for f in coverage}
md+=f'''### Oportunidades de innovación

- Evidenciar disponibilidad conjunta de profesional y sillón con reglas visibles y pruebas de concurrencia (agenda por sillón/box: {count['Agenda por sillón/box']} de 15).
- Mostrar al consultorio qué es solicitud, confirmación, cancelación y reprogramación, con trazabilidad de cambios (cancelación: {count['Cancelación']}; reprogramación: {count['Reprogramación']}; auditoría: {count['Auditoría']}).
- Explicar costos de mensajes, integraciones y portabilidad antes de contratar (exportación con alcance claro: {count['Exportación con alcance claro']}).
- Facilitar la recuperación de cancelaciones mediante lista de espera, si las entrevistas confirman esa prioridad (lista de espera: {count['Lista de espera']}).
- Limitar los datos solicitados en una reserva y separar permisos administrativos y clínicos.

Estas son hipótesis de oportunidad; no tienen validación de demanda ni disposición a pagar. La selección de funciones en la sección D procede de decisiones expresas posteriores a la investigación y no constituye validación comercial de estas hipótesis.

## D Decisiones pendientes del usuario

### Competidores para demo

La consigna pide elegir cinco. La selección final sigue pendiente. La investigación ofrece estos enfoques para construir la selección en el menú: oferta local e integraciones (Dentalink, DentApp, Dentify), agenda y portabilidad documentadas (Dentidesk, Open Dental), clínica y múltiples recursos (XDentalCloud), experiencia del paciente (AgendaPro, Dentrix, CareStack). Se pueden combinar o sustituir; no se contactó a ninguno ni se abrió una cuenta.

### Referencias de experiencia de usuario

La consigna pide tres. Todavía no están seleccionadas. Hay evidencia útil para revisar el recorrido de reserva en Dentalink, AgendaPro y Dentrix, la vinculación cita-presupuesto en Clinic Cloud y la diferencia solicitud/reserva directa en CareStack. Son alternativas de estudio, no una política visual aprobada ni una interfaz a copiar.

### Opciones de alcance del MVP para la entrevista

- Agenda interna: pacientes mínimos, profesionales, prestaciones y gestión de turnos por recepción. Falta decidir recursos, duración y reglas.
- Agenda con participación del paciente: agregar solicitud o reserva autónoma, según el control que el consultorio necesite. Implica decidir confirmación, cancelación y límites.
- Gestión clínica ampliada: agregar historia/odontograma, pagos o integraciones solo si son imprescindibles para el segmento elegido y el plazo permite verificarlos.

No se eligió ninguna opción. La separación definitiva entre imprescindibles, diferenciadores y etapas posteriores se completará con las respuestas. WhatsApp, ARCA, MercadoPago, autoservicio y módulos clínicos no se consideran obligatorios por defecto. El stack tampoco está decidido. La consigna permite implementar una API o módulo de dominio sin interfaz.

## Verificación de fuentes

### Revisión personal del equipo

Cuatro integrantes revisaron cinco fuentes de distintos tipos. Giovanni Colombo transmitió los resultados el 25/11/2026; no consta la fecha de cada apertura. «Qué afirmaba» resume lo contrastado; «Qué encontró» recoge lo informado por quien revisó, y «Corrección y contraste» indica qué cambió y qué comprobó después el agente.

'''
personal=[
 ['Giovanni Colombo','Sitio oficial: [DentApp](https://www.dentapp.ar/software-odontologico-argentina)','Proveedor Codilium S.A.S., nube y publicación de Google Calendar y MercadoPago.','Sin diferencias.','Sin cambios.'],
 ['Giovanni Colombo','Precios: [tab32](https://tab32.com/pricing/)','Precio desde $95 por mes, con cargos variables aparte.','Diferencias de precio según el servicio; para el servicio dental, un precio más alto, cercano a $400.','Ficha 15: se agregaron los cargos por servicio. Giovanni Colombo confirmó el 25/11 que el importe observado era $427. En el comparador de la página, esa cifra es el costo mensual estimado de Open Dental; para tab32 estima $179 y su precio publicado sigue desde $95. El precio base no cambia.'],
 ['Sofia Martinez','Centro de ayuda: [Dentalink](https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online)','Enlace de reserva, permisos, bloques y profesionales habilitados; desde la corrección del 25/11, validación conjunta de profesional y recurso.','Sin diferencias con los datos obtenidos.','Sin cambios.'],
 ['Fabiano Ramos','Términos de uso: [Dentidesk](https://app.dentidesk.com/terminos.php)','Excel estándar sin costo con licencia activa; tras el término, exportación solicitable hasta 30 días con posible cargo. Ficha 06: prueba de 15 días.','La prueba es de 15 días, no de 30; después empiezan los cargos.','La ficha 06 ya indicaba 15 días (punto 2.2). Los 30 días son el plazo para pedir la exportación tras el término (punto 7.6). Se aclaró la diferencia y se agregó que luego de la prueba se requiere suscripción.'],
 ['Luciano Izquierdo Cafici','Interfaz pública: [AgendaPro](https://agendapro.com/site/ar/odontologiaor/239004)','Reserva pública con servicios de distinta duración y botón Agendar.','Sin diferencias, luego de simular una reserva en la app, que consideró bastante buena.','Sin cambios en las celdas. Giovanni Colombo confirmó que la simulación se detuvo antes de confirmar la reserva. El agente había recibido HTTP 403 ese día.']]
md+=table(['Integrante','Tipo y fuente','Qué afirmaba el informe','Qué encontró','Corrección y contraste'],personal)+'\n'
md+='''### Comprobaciones realizadas por el agente

Las comprobaciones siguientes son automatizadas y se muestran aparte de la revisión personal, a la que **no sustituyen**.

'''
checks=[
 ['Sitio oficial: DentApp','Proveedor argentino, nube e integraciones locales','La página específica identifica Codilium S.A.S. y publica Google Calendar/MercadoPago. No se verificó inscripción registral.','https://www.dentapp.ar/software-odontologico-argentina','Giovanni Colombo: sin diferencias'],
 ['Precios: tab32','El buscador mostraba $125/mes','La apertura vigente mostró desde $95/mes; se descartó el precio indexado anterior y se registraron cargos variables.','https://tab32.com/pricing/','Giovanni Colombo: diferencia de precio'],
 ['Ayuda: Dentalink','Reserva mediante enlace y configuración profesional','El manual explica enlace, permisos, bloques y profesionales habilitados. No prueba disponibilidad conjunta de sillón y profesional. Corregido el 25/11: ver la segunda comprobación.','https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online','Sofia Martinez: sin diferencias'],
 ['Términos: Dentidesk','Exportación de datos','Licencia activa: Excel estándar sin costo; después del término: solicitud de exportación hasta 30 días corridos y posible cargo. Ese plazo no es la prueba gratuita, que dura 15 días. No se redujo todo a exportación gratuita.','https://app.dentidesk.com/terminos.php','Fabiano Ramos: prueba de 15 días'],
 ['Interfaz pública: AgendaPro','Reserva y duración variable por servicio','Se observaron servicios con diferentes duraciones y botón Agendar; no se completó reserva ni se copiaron reseñas o datos de pacientes.','https://agendapro.com/site/ar/odontologiaor/239004','Luciano Izquierdo Cafici: sin diferencias']]
md+=table(['Tipo y fuente','Afirmación contrastada','Hallazgo y corrección','URL','Revisión personal'],[[*r[:3],f'[Abrir]({r[3]})',r[4]] for r in checks])+'\n'
md+='''### Segunda comprobación del agente (25/11/2026)

El agente intentó volver a abrir en un navegador las cinco fuentes anteriores y contrastó con este informe el texto publicado ese día en las cuatro que respondieron. No sustituye la revisión personal. Por decisión del 25/11/2026 (Ajustar), las diferencias se incorporaron a las fichas, la matriz B y la sección C. Una diferencia puede deberse a una omisión del relevamiento o a un cambio de la página posterior al 17/11/2026; salvo indicación, no se distingue cuál.

'''
second=[
 ['DentApp','Coinciden Codilium S.A.S., nube, Google Calendar, MercadoPago, roles y ReNaPDiS declarado.','Publica 30 días de prueba y precios mensuales con IVA ($24.900, $54.900 y $74.900; Empresarial a medida), recordatorios según plan, API y webhooks en Empresarial y «backups diarios».','Ficha 02 y cuatro celdas de la matriz B.'],
 ['tab32','Coinciden desde $95/mes, usuarios ilimitados, prueba de 14 días, cargos por claim, adjuntos y elegibilidad, y que los ledgers no se migran.','La prueba pide email y tarjeta; la migración se publica desde $1,250; la exportación gratuita al salir incluye el ledger.','Ficha 15.'],
 ['Dentalink','Coinciden el enlace de reserva, los permisos y los bloqueos de horario.','El manual (fechado el 12/06/2026) tiene una sección plegada, «Cruce con Recursos», que declara que la agenda online valida que el profesional y el recurso asociado al servicio estén disponibles en el mismo horario. No identifica el recurso como sillón ni describe control de concurrencia.','Ficha 01 y sección C. La matriz B no cambia.'],
 ['Dentidesk','Coinciden Excel estándar sin costo con licencia activa, solicitud hasta 30 días después del término con posible cargo y prueba de 15 días.','Cada cuenta de odontólogo adicional «generará un cargo según el plan contratado»; el informe lo expresaba como posible.','Ficha 06.'],
 ['AgendaPro','No se pudo comprobar.','La interfaz pública respondió HTTP 403. No se reintentó ni se evadió el bloqueo.','Se anota en la ficha 08; las celdas conservan la observación del 17/11.']]
md+=table(['Fuente','Resultado','Diferencia o detalle','Corrección aplicada'],second)+'\n'
md+='''### Fuentes no accesibles y exclusiones

Doctoralia PRO Argentina devolvió 403 en sus páginas de agenda/precio; no se afirmó un precio a partir de resultados indexados. DentalTap tuvo timeouts/502 en producto y precios; se localizó un manual, pero no se lo usó para rellenar prestaciones no corroboradas. Se sustituyeron esos candidatos en el núcleo por Dentidesk y Odonto.me. DriCloud se exploró como software médico general y se reemplazó por XDentalCloud, sin trasladar funcionalidades entre productos. No se usaron mecanismos de evasión, cuentas personales ni contacto comercial.

No se verificaron reseñas externas como parte de este corte. Las cifras de adopción que aparecen están atribuidas al proveedor. Ausencia de reseñas verificadas no implica ausencia de clientes.

## Preguntas pendientes de la entrevista

Los once puntos se completarán con el usuario: problema prioritario, usuarios/roles, casos de uso, validación de competidores, imprescindibles, opcionales, reglas de negocio, integraciones, restricciones, riesgos y preguntas abiertas. Se preguntará mediante menú en rondas de hasta cinco preguntas. Ninguna opción preseleccionada equivale a una respuesta enviada.

Pendientes concretos: segmento y número de sedes; profesionales/sillones; dolor principal; quién agenda; reserva directa o solicitud; datos mínimos; estados y transiciones; duración y cancelación; convivencia con WhatsApp; plazo, integrantes y presupuesto; forma de entrega; selección de referencias y verificación personal de fuentes.

No se generó discovery.md con respuestas inferidas. Discovery permanece en progreso y necesita la entrevista y la aprobación explícita del resumen antes de pasar a KB.

## Referencias completas

Todas las referencias siguientes fueron consultadas el 2026-11-17; las que indican una nueva consulta se volvieron a abrir, o se intentó abrirlas, en esa segunda fecha. Las rutas enlazadas son fuentes primarias abiertas; las limitaciones se describen en sus fichas. Las capacidades comerciales y las condiciones pueden cambiar.

'''
for i,p in enumerate(PRODUCTS,1):
 for j,(title,url) in enumerate(p['sources'],1):
  again=p.get('rechecks',{}).get(url)
  md+=f'- **F{i:02}.{j} - {p["name"]}: {title}**. [{url}]({url}). Consulta: {p["date"]}.'+(f' Nueva consulta: {again}.' if again else '')+'\n'
approved = ROOT/'decisiones-aprobadas.json'
if approved.exists():
 decisions=json.loads(approved.read_text())
 assert decisions['status']=='approved_with_explicit_open_questions'
 md=md.replace('Fecha de consulta: 17 de noviembre de 2026. Integrantes y datos de portada: pendientes de informar.', 'Integrantes: Giovanni Colombo, Sofia Martinez, Luciano Izquierdo Cafici y Fabiano Ramos.\n\nFecha de entrega: 25 de noviembre de 2026. Consulta de fuentes y aprobación del Discovery: 17 de noviembre de 2026.\n\n<!-- salto-de-pagina -->')
 old='Este documento es el resultado de investigación previo a la entrevista. El usuario pidió responder las preguntas después del relevamiento. Segmento objetivo, roles, MVP, integraciones, reglas, presupuesto y stack siguen sin decidirse. No se creó la KB, el roadmap ni un change. La sección D se completará con las respuestas y la aprobación del usuario.'
 checklist=(ROOT/'discovery.md').read_text()
 def checklist_section(number):
  match=re.search(rf'^## {number}\. [^\n]+\n(.*?)(?=^## |\Z)',checklist,flags=re.S|re.M)
  if not match:
   raise ValueError(f'Falta sección {number} del checklist aprobado')
  return match.group(1).strip()
 # El alcance del informe se deriva del checklist aprobado; regenerarlo no
 # puede restaurar la exclusión histórica de paciente, email o lista de espera.
 new='Tras la investigación y las preguntas por menú, el usuario aprobó el resumen de once puntos y su ampliación (revisión 2). El MVP incluye agenda de sillón diaria/semanal, bloqueos por recepción, ficha clínica sencilla, disponibilidad conjunta de profesional y sillón y confirmaciones manuales. El paciente cancela por enlace hasta 48 horas antes y gestiona con cuenta la lista de espera; recepción asigna los huecos. Se envía email al crear con enlace y recordatorio 24 horas antes, sin opción de cancelar cuando el plazo venció. Se requieren envíos reales a direcciones de prueba del equipo mediante proveedor gratuito aún por definir. El stack elegido es HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL; entrega 25/11/2026 y presupuesto externo cero. A ese corte seguían pendientes el intervalo inicial, los detalles técnicos y la revisión personal de cinco fuentes. Este informe es un corte al cierre del Discovery (revisión 2, consolidada el 19/11/2026): a esa fecha no se había implementado código ni seleccionado un change. El estado posterior consta en README.md y CHANGES.md; la revisión personal y la segunda comprobación del agente, informadas el 25/11/2026, figuran en «Verificación de fuentes».'
 assert old in md
 md=md.replace(old,new)
 section='''## D Recomendación y alcance aprobados por el usuario

Las elecciones siguientes fueron realizadas por el usuario después de la investigación y aprobadas mediante menú. Son decisiones del proyecto, no una recomendación de compra ni una clasificación de calidad de los proveedores.

### Cinco competidores seleccionados para análisis o demo posterior

'''
 rationales={
  'Dentalink':'Contrastar operación dental y confirmaciones; el manual público permite examinar configuración de agenda.',
  'DentApp':'Examinar propuesta argentina, roles y las integraciones locales declaradas sin incorporarlas automáticamente al MVP.',
  'Dentify':'Contrastar agenda de consultorio y ficha sencilla en una oferta que se declara argentina.',
  'Dentidesk':'Examinar separación de perfiles y condiciones de exportación documentadas.',
  'AgendaPro':'Examinar claridad del recorrido público de reserva y condiciones por servicio; la autorreserva no se incorpora al MVP.'}
 section+=table(['Competidor elegido','Objetivo de contraste'],[[n,rationales[n]] for n in decisions['demo_shortlist']])
 section+='\nNo se contactó a proveedores ni se crearon cuentas. Las demos siguen como trabajo posterior y no se presentan como realizadas; la revisión personal de fuentes figura en «Verificación de fuentes».\n\n### Tres referencias de experiencia de usuario elegidas\n\n'
 ux={'Dentalink':'Configuración y presentación de opciones de agenda en documentación oficial.','AgendaPro':'Interfaz pública con servicios, duraciones y condiciones de reserva.','Dentrix':'Flujo documentado de motivo, profesional y horario; artículo histórico acompañado por página vigente.'}
 section+=table(['Referencia','Qué estudiar'],[[n,ux[n]] for n in decisions['ux_references']])
 section+='''
Estas referencias no autorizan copiar una interfaz ni agregar autorreserva general al MVP. La participación del paciente se limita a lo aprobado en esta revisión. La política visual se mantiene diferida hasta la fase correspondiente.

### MVP aprobado y funcionalidades imprescindibles

'''
 section+=checklist_section(5)+'\n\n### Usuarios y reglas de la revisión aprobada\n\n'+checklist_section(2)+'\n\n'+checklist_section(7)
 section+='\n\n### Diferenciadores buscados dentro del alcance aprobado\n\nSe priorizan:\n\n- Reglas verificables sobre profesional y sillón.\n- Intervalo configurable común para la sede.\n- Separación entre lectura administrativa y edición clínica.\n\nSon objetivos de calidad, no exclusividad comercial demostrada. La reducción de ausencias y huecos mediante emails y lista de espera todavía requiere medición.\n\n### Funcionalidades para etapas posteriores\n\n'
 section+='\n'.join('- '+v+'.' for v in decisions['out_of_mvp'])
 section+='''

No se asigna prioridad ni compromiso a esas ampliaciones. El MVP define el backlog, mientras que la entrega académica debe llevar un único change por el ciclo completo. Todavía no se seleccionó ese change.
'''
 # La línea del equipo queda solo en discovery.md, a pedido del usuario.
 restrictions,removed=re.subn(r'^- Equipo:[^\n]*\n','',checklist_section(9),flags=re.M)
 assert removed==1
 section+='\n### Integraciones\n\n'+checklist_section(8)+'\n\n### Restricciones\n\n'+restrictions+'\n\n### Riesgos\n\n'+checklist_section(10)+'\n\n'
 md=re.sub(r'## D Decisiones pendientes del usuario\n.*?(?=## Verificación de fuentes)',lambda _:section,md,flags=re.S)
 questions='''## Preguntas abiertas tras la aprobación

El checklist completo de once puntos y las decisiones aprobadas están en discovery.md y decisiones-aprobadas.json. La aprobación conserva estos pendientes y no autoriza resolverlos por inferencia:

'''
 questions+='\n'.join('- '+v+'.' for v in decisions['open_questions'])
 questions+='\n\nDiscovery, KB y roadmap se consolidan con la revisión 2 aprobada. El primer change todavía no fue elegido y no se inició implementación, se seleccionó proveedor o se enviaron emails. La revisión personal de cinco fuentes, pendiente al consolidar, se informó el 25/11/2026 y figura en «Verificación de fuentes».\n\n'
 reviewed='- Revisión personal de al menos cinco fuentes por integrantes.'
 assert reviewed in questions
 questions=questions.replace(reviewed,reviewed+' Informada el 25/11/2026.')
 team='- Nombres de integrantes y reparto de trabajo.'
 assert team in questions
 questions=questions.replace(team,team+' Resuelto después del corte: los nombres constan en la portada y el reparto en el plan de colaboración.')
 md=re.sub(r'## Preguntas pendientes de la entrevista\n.*?(?=## Referencias completas)',lambda _:questions,md,flags=re.S)
if approved.exists():
 # Redacción del informe; discovery.md conserva el registro aprobado.
 WORDING=[
  ('el usuario aprobó el resumen','se aprobó el resumen'),
  ('## D Recomendación y alcance aprobados por el usuario','## D Recomendación final y alcance aprobado'),
  ('Las elecciones siguientes fueron realizadas por el usuario después de la investigación y aprobadas mediante menú.','Las elecciones siguientes se hicieron después de la investigación y se aprobaron mediante menú.'),
  ('stack elegido por el usuario','stack elegido'),
  ('La selección de estas tecnologías fue explícita del usuario;','La selección de estas tecnologías fue expresa;'),
  ('Los riesgos que preocupan especialmente al usuario son','Las principales preocupaciones señaladas son'),
  ('No estableció un orden entre esos dos riesgos.','No se estableció un orden entre ambas.'),
  ('su confianza en llegar no constituye','la confianza en llegar no constituye'),
  ('no un nuevo orden de preocupación elegido por el usuario.','sin cambiar las preocupaciones señaladas.')]
 for old_text,new_text in WORDING:
  assert md.count(old_text)==1,old_text
  md=md.replace(old_text,new_text)
(ROOT/'informe-discovery.md').write_text(md)
(ROOT/'competidores.json').write_text(json.dumps(DATA,ensure_ascii=False,indent=2)+'\n')
(ROOT/'matriz-puntuacion.json').write_text(json.dumps({'method':'Cobertura de evidencia; D=1 C=0.5 N=0, cinco criterios por dimensión','weights':{g[0]:g[2] for g in GROUPS},'criteria':{g[0]:g[3] for g in GROUPS},'scores':[{'product':p['name'],**p['scores'],'weighted_total':p['weighted_total']} for p in PRODUCTS]},ensure_ascii=False,indent=2)+'\n')
print(f'Informe: {len(PRODUCTS)} productos, {sum(len(p["sources"]) for p in PRODUCTS)} referencias, {len(md.split())} palabras.')
