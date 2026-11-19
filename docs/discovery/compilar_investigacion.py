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

Fuentes consultadas: {refs}. Todas consultadas el {p['date']}. Declaraciones comerciales no equivalen a una prueba independiente. Documentación e interfaz pública acreditan lo publicado, no el comportamiento de una instalación real. No se crearon cuentas ni se enviaron formularios.
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

## A Comparación de sistemas

### Identificación y pertinencia

'''
rows=[]
for i,p in enumerate(PRODUCTS,1):
 rows.append([f'{i}. {p["name"]}',p['provider'],p['country'],p['segment'],p['mode'],ref(i,1,p),p['date']])
md=intro+table(['Sistema','Proveedor','País evidenciado','Segmento','Modalidad','URL oficial','Consulta'],rows)
md+='''
La oferta argentina encabeza la tabla. Dentidesk y Odonto.me aportan referencias en español; AgendaPro tiene oferta y reservas públicas en Argentina. Clinic Cloud y XDentalCloud son referencias de módulos y condiciones; Open Dental, Dentrix, Curve, CareStack y tab32 aportan patrones internacionales cuya comercialización y localización argentina no se presumen.

### Fichas comparables y trazabilidad

Las fuentes F01 a F15 se enumeran completas al final. Las fichas complementan las matrices; una capacidad pendiente de prueba no se convierte en requisito del producto que construiremos.

'''
for i,p in enumerate(PRODUCTS,1):
 md+=f'### {i:02} {p["name"]}\n\n'
 md+='Fuentes: '+ '; '.join(ref(i,j,p) for j in range(1,len(p['sources'])+1))+'.\n\n'
 for key,title in [('agenda','Agenda y recursos'),('digital','Turnos digitales y automatización'),('clinical','Clínica'),('admin','Administración'),('integration','Integraciones'),('security','Seguridad y exportación'),('price','Modelo comercial'),('limits','Fortalezas y límites de la evidencia'),('adoption','Adopción')]:
  md+=f'- **{title}**: {p[key]}\n'
 md+='\n'
md+='''## B Matriz provisional de puntuación

Se aplican los pesos de la consigna. Para que el cálculo sea auditable sin probar productos ni elegir un ganador, esta versión mide **cobertura de evidencia pública**, no calidad real, seguridad efectiva, relación precio/valor ni conveniencia de compra. La interpretación y cualquier ranking comercial deben revisarse con el usuario. Los resultados no eligen el MVP.

Cada dimensión tiene cinco criterios. Por criterio: Documentado = 1 punto; Declarado = 0,5; No evidenciado = 0. La suma de cada dimensión va de 0 a 5. Una función futura no suma como disponible. La falta de evidencia reduce la puntuación sin afirmar que la función no existe. La columna de precio mide transparencia y condiciones de adopción, no asequibilidad comparada entre monedas.

Total ponderado = T × 0,25 + C × 0,20 + I × 0,15 + A × 0,15 + P × 0,10 + S × 0,10 + E × 0,05. Se calcula sin redondear los componentes y se muestran tres decimales. Las matrices detalladas de criterios explican cada componente. No mezclar planes o países para concluir que todo está incluido en una misma tarifa.

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

### Funcionalidades frecuentes en la muestra

La mayoría de las ofertas dentales consultadas presenta agenda, historia y odontograma; varias añaden imágenes, presupuestos, caja y reportes. Dentalink, Dentidesk, XDentalCloud, Dentrix, CareStack y tab32 publican reserva o comunicación digital. Esto es un patrón descriptivo de la muestra, no una estimación estadística del mercado argentino.

### Diferencias observables

Odontoly declara operación local-first y revela límites de cifrado y acceso remoto; contrasta con las ofertas cloud de DentApp, Dentify, Dentidesk y Curve. DentApp publica Google Calendar y MercadoPago; Dentalink presenta obras sociales y operación clínica. Open Dental ofrece manuales detallados sobre operatorios y bloqueos. Dentidesk explicita condiciones de exportación y cierre. La relevancia de cada diferencia depende del consultorio que el usuario decida atender.

Las promesas de IA, reducción de ausencias, mayor facturación o facilidad de uso siguen siendo afirmaciones comerciales hasta contar con demostraciones y mediciones comparables. No se atribuyen ahorros ni resultados clínicos a partir de testimonios.

### Vacíos de evidencia para el mercado argentino

En las páginas consultadas no suele estar descrita la transacción que evita simultáneamente la ocupación del profesional y del sillón. Tampoco hay evidencia suficiente para comparar de forma uniforme auditoría, exportación completa y restauración. Una integración fiscal extranjera no prueba ARCA; listar obras sociales no prueba comunicación electrónica con ellas. Estos son vacíos de información pública de esta muestra, no una afirmación de que ningún proveedor los resuelva.

Los precios mezclan profesional, usuario, sede, anualidad, extras, mensajes y migración. Las páginas de AgendaPro requieren distinguir categorías; Clinic Cloud requiere distinguir módulos. La moneda no identificada de Odonto.me y los importes equivalentes anuales de XDentalCloud impiden comparar automáticamente el costo total.

### Oportunidades que deben validarse

- Evidenciar disponibilidad conjunta de profesional y sillón con reglas visibles y pruebas de concurrencia.
- Mostrar al consultorio qué es solicitud, confirmación, cancelación y reprogramación, con trazabilidad de cambios.
- Explicar costos de mensajes, integraciones y portabilidad antes de contratar.
- Facilitar recuperación de cancelaciones o lista de espera, si las entrevistas confirman esa prioridad.
- Limitar los datos solicitados en una reserva y separar permisos administrativos y clínicos.

Estas son hipótesis de oportunidad; no tienen validación de demanda ni disposición a pagar. La selección de funciones en la sección D procede de decisiones explícitas del usuario y no constituye validación comercial de estas hipótesis.

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

### Comprobaciones realizadas por el agente

La consigna exige que los integrantes comprueben personalmente al menos cinco fuentes. Las comprobaciones siguientes son automatizadas por el agente y **no sustituyen esa revisión humana**. La columna de revisión personal queda pendiente; no se atribuye trabajo no realizado a los integrantes.

'''
checks=[
 ['Sitio oficial: DentApp','Proveedor argentino, nube e integraciones locales','La página específica identifica Codilium S.A.S. y publica Google Calendar/MercadoPago. No se verificó inscripción registral.','https://www.dentapp.ar/software-odontologico-argentina'],
 ['Precios: tab32','El buscador mostraba $125/mes','La apertura vigente mostró desde $95/mes; se descartó el precio indexado anterior y se registraron cargos variables.','https://tab32.com/pricing/'],
 ['Ayuda: Dentalink','Reserva mediante enlace y configuración profesional','El manual explica enlace, permisos, bloques y profesionales habilitados. No prueba disponibilidad conjunta de sillón y profesional.','https://ayuda.softwaredentalink.com/es/articles/9493139-configura-tu-agenda-online'],
 ['Términos: Dentidesk','Exportación de datos','Licencia activa: Excel estándar sin costo; después del término: hasta 30 días y posible cargo. No se redujo todo a exportación gratuita.','https://app.dentidesk.com/terminos.php'],
 ['Interfaz pública: AgendaPro','Reserva y duración variable por servicio','Se observaron servicios con diferentes duraciones y botón Agendar; no se completó reserva ni se copiaron reseñas o datos de pacientes.','https://agendapro.com/site/ar/odontologiaor/239004'],
 ['Precios: Clinic Cloud','La extracción inicial señalaba promoción de dos meses','La apertura posterior mostró 50% durante tres meses. Se priorizó la página abierta y se aclaró que Max incluye odontograma.','https://clinic-cloud.com/tarifas'],
 ['Sitio y FAQ: Dentify','País, cifrado y precio anual de extracción inicial','La portada actual declara Hecho en Argentina, nube cifrada y $30.000/$45.000 mensuales. Se retiró el anual no corroborado y no se inventó código de moneda.','https://www.dentify.com.ar/'],
 ['Producto: Odontoly','WhatsApp y ARCA podrían parecer disponibles por figurar en catálogo','El sitio los identifica como complementos próximos. También distingue backups cifrados de base de trabajo aún sin cifrado por la app.','https://odontoly.com/']]
md+=table(['Tipo y fuente','Afirmación contrastada','Hallazgo y corrección','URL','Revisión personal'],[[*r[:3],f'[Abrir]({r[3]})','Pendiente del integrante'] for r in checks])+'\n'
md+='''### Fuentes no accesibles y exclusiones

Doctoralia PRO Argentina devolvió 403 en sus páginas de agenda/precio; no se afirmó un precio a partir de resultados indexados. DentalTap tuvo timeouts/502 en producto y precios; se localizó un manual, pero no se lo usó para rellenar prestaciones no corroboradas. Se sustituyeron esos candidatos en el núcleo por Dentidesk y Odonto.me. DriCloud se exploró como software médico general y se reemplazó por XDentalCloud, sin trasladar funcionalidades entre productos. No se usaron mecanismos de evasión, cuentas personales ni contacto comercial.

No se verificaron reseñas externas como parte de este corte. Las cifras de adopción que aparecen están atribuidas al proveedor. Ausencia de reseñas verificadas no implica ausencia de clientes.

## Preguntas pendientes de la entrevista

Los once puntos se completarán con el usuario: problema prioritario, usuarios/roles, casos de uso, validación de competidores, imprescindibles, opcionales, reglas de negocio, integraciones, restricciones, riesgos y preguntas abiertas. Se preguntará mediante menú en rondas de hasta cinco preguntas. Ninguna opción preseleccionada equivale a una respuesta enviada.

Pendientes concretos: segmento y número de sedes; profesionales/sillones; dolor principal; quién agenda; reserva directa o solicitud; datos mínimos; estados y transiciones; duración y cancelación; convivencia con WhatsApp; plazo, integrantes y presupuesto; forma de entrega; selección de referencias y verificación personal de fuentes.

No se generó discovery.md con respuestas inferidas. Discovery permanece en progreso y necesita la entrevista y la aprobación explícita del resumen antes de pasar a KB.

## Referencias completas

Todas las referencias siguientes fueron consultadas el 2026-11-17. Las rutas enlazadas son fuentes primarias abiertas; las limitaciones se describen en sus fichas. Las capacidades comerciales y las condiciones pueden cambiar.

'''
for i,p in enumerate(PRODUCTS,1):
 for j,(title,url) in enumerate(p['sources'],1):
  md+=f'- **F{i:02}.{j} - {p["name"]}: {title}**. [{url}]({url}). Consulta: {p["date"]}.\n'
approved = ROOT/'decisiones-aprobadas.json'
if approved.exists():
 decisions=json.loads(approved.read_text())
 assert decisions['status']=='approved_with_explicit_open_questions'
 md=md.replace('Fecha de consulta: 17 de noviembre de 2026. Integrantes y datos de portada: pendientes de informar.', 'Fecha de consulta y aprobación: 17 de noviembre de 2026. Equipo de cuatro integrantes con autorización docente informada por el usuario; nombres pendientes.')
 old='Este documento es el resultado de investigación previo a la entrevista. El usuario pidió responder las preguntas después del relevamiento. Segmento objetivo, roles, MVP, integraciones, reglas, presupuesto y stack siguen sin decidirse. No se creó la KB, el roadmap ni un change. La sección D se completará con las respuestas y la aprobación del usuario.'
 checklist=(ROOT/'discovery.md').read_text()
 def checklist_section(number):
  match=re.search(rf'^## {number}\. [^\n]+\n(.*?)(?=^## |\Z)',checklist,flags=re.S|re.M)
  if not match:
   raise ValueError(f'Falta sección {number} del checklist aprobado')
  return match.group(1).strip()
 # El alcance del informe se deriva del checklist aprobado; regenerarlo no
 # puede restaurar la exclusión histórica de paciente, email o lista de espera.
 new='Tras la investigación y las preguntas por menú, el usuario aprobó el resumen de once puntos y su ampliación (revisión 2). El MVP incluye agenda de sillón diaria/semanal, bloqueos por recepción, ficha clínica sencilla, disponibilidad conjunta de profesional y sillón y confirmaciones manuales. El paciente cancela por enlace hasta 48 horas antes y gestiona con cuenta la lista de espera; recepción asigna los huecos. Se envía email al crear con enlace y recordatorio 24 horas antes, sin opción de cancelar cuando el plazo venció. Se requieren envíos reales a direcciones de prueba del equipo mediante proveedor gratuito aún por definir. El stack elegido es HTML/CSS/TypeScript vanilla, Java Spring Boot y PostgreSQL; entrega 25/11/2026 y presupuesto externo cero. El intervalo inicial, los detalles técnicos y la revisión personal de cinco fuentes siguen pendientes. Discovery, KB y roadmap son documentación de planificación: no se implementó código ni se seleccionó un change.'
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
 section+='\nNo se contactó a proveedores ni se crearon cuentas. Las demos y comprobaciones personales siguen como trabajo posterior; no se presentan como realizadas.\n\n### Tres referencias de experiencia de usuario elegidas\n\n'
 ux={'Dentalink':'Configuración y presentación de opciones de agenda en documentación oficial.','AgendaPro':'Interfaz pública con servicios, duraciones y condiciones de reserva.','Dentrix':'Flujo documentado de motivo, profesional y horario; artículo histórico acompañado por página vigente.'}
 section+=table(['Referencia','Qué estudiar'],[[n,ux[n]] for n in decisions['ux_references']])
 section+='''
Estas referencias no autorizan copiar una interfaz ni agregar autorreserva general al MVP. La participación del paciente se limita a lo aprobado en esta revisión. La política visual se mantiene diferida hasta la fase correspondiente.

### MVP aprobado y funcionalidades imprescindibles

'''
 section+=checklist_section(5)+'\n\n### Usuarios y reglas de la revisión aprobada\n\n'+checklist_section(2)+'\n\n'+checklist_section(7)
 section+='\n\n### Diferenciadores buscados dentro del alcance aprobado\n\nSe priorizan reglas verificables sobre profesional y sillón, intervalo configurable y separación entre lectura administrativa y edición clínica. Son objetivos de calidad, no exclusividad comercial demostrada. La reducción de ausencias y huecos mediante emails y lista de espera todavía requiere medición.\n\n### Funcionalidades para etapas posteriores\n\n'
 section+='\n'.join('- '+v+'.' for v in decisions['out_of_mvp'])
 section+='''

No se asigna prioridad ni compromiso a esas ampliaciones. El MVP define el backlog, mientras que la entrega académica debe llevar un único change por el ciclo completo. Todavía no se seleccionó ese change.
'''
 section+='\n### Integraciones\n\n'+checklist_section(8)+'\n\n### Restricciones\n\n'+checklist_section(9)+'\n\n### Riesgos\n\n'+checklist_section(10)+'\n\n'
 md=re.sub(r'## D Decisiones pendientes del usuario\n.*?(?=## Verificación de fuentes)',lambda _:section,md,flags=re.S)
 questions='''## Preguntas abiertas tras la aprobación

El checklist completo de once puntos y las decisiones aprobadas están en discovery.md y decisiones-aprobadas.json. La aprobación conserva estos pendientes y no autoriza resolverlos por inferencia:

'''
 questions+='\n'.join('- '+v+'.' for v in decisions['open_questions'])
 questions+='\n\nDiscovery, KB y roadmap se consolidan con la revisión 2 aprobada. El primer change todavía no fue elegido y no se inició implementación, se seleccionó proveedor o se enviaron emails. La revisión personal de al menos cinco fuentes sigue pendiente y debe registrarse con hallazgos reales de los integrantes.\n\n'
 md=re.sub(r'## Preguntas pendientes de la entrevista\n.*?(?=## Referencias completas)',lambda _:questions,md,flags=re.S)
(ROOT/'informe-discovery.md').write_text(md)
(ROOT/'competidores.json').write_text(json.dumps(DATA,ensure_ascii=False,indent=2)+'\n')
(ROOT/'matriz-puntuacion.json').write_text(json.dumps({'method':'Cobertura provisional de evidencia; D=1 C=0.5 N=0, cinco criterios por dimensión','weights':{g[0]:g[2] for g in GROUPS},'criteria':{g[0]:g[3] for g in GROUPS},'scores':[{'product':p['name'],**p['scores'],'weighted_total':p['weighted_total']} for p in PRODUCTS]},ensure_ascii=False,indent=2)+'\n')
print(f'Informe: {len(PRODUCTS)} productos, {sum(len(p["sources"]) for p in PRODUCTS)} referencias, {len(md.split())} palabras.')
