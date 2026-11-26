# Odontología Turnos

Proyecto integrador de Desarrollo de Software de UTN: sistema de gestión de turnos para un consultorio de una sede con varios odontólogos.

**Estado al 25/11/2026:** C-01 a C-13 y C-15 a C-21 están implementados, archivados en OpenSpec e integrados en `main`. Siguen pendientes C-14 y C-22 a C-25. No hay despliegue productivo. El detalle por item está en [CHANGES.md](CHANGES.md).

El change académico que recorre el ciclo completo es **C-01, crear un turno sin solapamientos**: [propuesta, diseño, tareas y evidencia](openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos-dominio/proposal.md) y [spec vigente](openspec/specs/disponibilidad-de-turnos/spec.md). La entrega y sus pendientes se siguen en [docs/entrega](docs/entrega/README.md).

## Qué se puede usar hoy

| Área | API HTTP | Interfaz web |
| --- | --- | --- |
| Ingreso y salida del equipo | Sí | Sí |
| Pacientes, asignaciones y ficha clínica | Sí | Sí |
| Agenda diaria y semanal, alta de turnos y bloqueos | Sí | Sí |
| Reprogramación y cancelación por el equipo | Sí | Sí |
| Intervalo de la sede | Sí | Sí, solo recepción |
| Cancelación del paciente por enlace | Sí | Pendiente (C-23) |
| Confirmaciones manuales | Sí | Sí, solo recepción |
| Cuenta del paciente y lista de espera | Pendiente; existen los servicios (C-16 y C-21) | Pendiente (C-24) |
| Avisos de huecos y asignación | Pendiente (C-22) | Pendiente (C-25) |

El email al crear o reprogramar un turno (C-19) y el recordatorio de 24 horas (C-20, apagado por defecto) usan Resend y solo envían a las direcciones de prueba configuradas.

## Alcance aprobado

- Agenda diaria y semanal por sillón, con profesional y sillón obligatorios, duración variable e intervalo global configurable.
- Bloqueos por recepción de sillón y/o profesional; se rechazan si afectan turnos existentes.
- Gestión de pacientes y ficha sencilla con antecedentes y notas. Recepción puede leerla; solo odontólogos la editan.
- Creación y reprogramación de turnos por el equipo, cancelación y confirmaciones manuales.
- Email al crear el turno con enlace para cancelar hasta 48 horas antes; recordatorio automático 24 horas antes, sin opción de cancelar cuando venció el plazo.
- Cuenta del paciente para gestionar su inscripción en lista de espera y sus preferencias. Avisos automáticos por email; recepción confirma quién toma cada hueco. El aviso no reserva el horario.

La autorreserva general, WhatsApp automático, odontograma, pagos y facturación están fuera del MVP. El presupuesto externo es cero. Los ejemplos, pruebas y capturas usan datos clínicos ficticios.

## Stack

| Capa | Tecnología |
| --- | --- |
| Frontend | HTML, CSS y TypeScript vanilla en modo estricto |
| Backend | Java 25 y Spring Boot 4.1.2, Maven, JPA/Hibernate y Flyway |
| Base de datos | PostgreSQL 18.7: Docker Compose en desarrollo y Testcontainers en pruebas |

## Requisitos

| Herramienta | Para qué |
| --- | --- |
| Git | Clonar el repositorio |
| JDK 25 y Maven 3.9.16 (referencia) | Compilar y probar el backend |
| Docker con el daemon iniciado | Pruebas de integración con PostgreSQL real |
| Node 24 y npm | Compilar y probar el frontend |
| Python 3 | Pruebas de la asignación de Issues y servidor local de la interfaz |

La primera ejecución necesita conexión: descarga dependencias Maven y npm e imágenes públicas de Docker. Clonar no instala herramientas. Para las pruebas no hace falta `.env` ni credenciales. Los comandos están verificados en macOS; la integración continua ejecuta las suites completas en Linux.

## Clonar y ejecutar las pruebas

```sh
git clone https://github.com/giovanniclmb/odontologia-turnos.git
cd odontologia-turnos
```

Solo el change académico (C-01, no necesita Docker):

```sh
mvn -f backend/pom.xml test -Dtest=AgendaTest
```

Backend completo, unitarias e integración. Requiere libre el puerto 8080, porque una prueba arranca ahí el JAR; si la aplicación de demostración está abierta, detenerla antes.

```sh
./scripts/check-docker.sh
mvn -f backend/pom.xml clean verify
```

Frontend:

```sh
npm --prefix frontend ci
npm --prefix frontend test
```

Asignación de Issues:

```sh
python3 -m unittest discover -s scripts/tests
```

Resultados observados y entorno: [verificación en un clon limpio](docs/entrega/verificacion-clon-limpio.md).

## Ver la aplicación en local

Usa datos ficticios y una base temporal, sin tocar la de desarrollo. Requiere Docker iniciado y los puertos 8080 y 5173 libres. Los comandos son para macOS o Linux.

1. Desde la raíz, compilar la interfaz:

   ```sh
   npm --prefix frontend ci
   npm --prefix frontend run build
   ```

2. Desde la raíz, iniciar la API de demostración y dejar la terminal abierta. Está lista cuando imprime `C10_DEMO_URL`.

   ```sh
   cd backend
   mvn test-compile dependency:build-classpath -Dmdep.outputFile=target/demo-classpath
   java -cp "target/test-classes:target/classes:$(cat target/demo-classpath)" ar.edu.utn.odontologia.persistencia.ApiC10Demo
   ```

3. En otra terminal, desde la raíz, servir la interfaz:

   ```sh
   python3 -m http.server 5173 --bind 127.0.0.1
   ```

4. Abrir <http://localhost:5173/> e ingresar con los [usuarios de prueba](backend/README.md#c-11--verificación-http-y-entorno-temporal-para-la-interfaz). Para dar turnos, ingresar como recepción y establecer primero el intervalo en Configuración.

Ctrl-C en cada terminal detiene los servicios; los datos de la demostración se pierden. Después de actualizar el código, detenerlos y repetir desde el primer paso.

El nombre comercial y el logo no están definidos: la interfaz muestra un [nombre y logo provisionales](design-system/brand.md). Para trabajar con una base persistente, ver [desarrollo local](backend/README.md#desarrollo-local).

## Documentación

| Fuente | Contenido |
| --- | --- |
| [Consigna](<docs/desarrollo-active-stack Global (1).docx>) | Requisitos académicos originales |
| [Entrega](docs/entrega/README.md) | Cotejo contra la consigna, evidencias y pendientes |
| [Discovery](docs/discovery/discovery.md) | Checklist y decisiones aprobadas |
| [Ampliación aprobada](docs/discovery/ampliacion-paciente.md) | Paciente, emails, lista de espera y bloqueos |
| [Informe de investigación](docs/discovery/informe-discovery.md) / [PDF](docs/discovery/informe-discovery.pdf) | Comparación de 15 sistemas, 38 referencias y verificación de fuentes |
| [Knowledge base](knowledge-base/README.md) | Diez documentos canónicos del dominio y la arquitectura |
| [Roadmap](CHANGES.md) | 25 items con dependencias, responsables y estado |
| [Plan de colaboración](docs/plan-colaboracion.md) | Equipo de cuatro, preparación paralela y reparto aprobado |
| [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md) | Pendientes y resoluciones acotadas por change |
| [Instrucciones de agentes](AGENTS.md) | Stack, reglas y roadmap para agentes; [CLAUDE.md](CLAUDE.md) es idéntico |
| [Bitácora](docs/bitacora.md) | Decisiones y verificaciones |
| [Evidencias](docs/evidencias/README.md) | Alcance y conservación de registros técnicos |

## Trabajar en el proyecto

1. Leer [AGENTS.md](AGENTS.md) y el [índice de la KB](knowledge-base/README.md).
2. Consultar [CHANGES.md](CHANGES.md), sus dependencias y las preguntas abiertas del item elegido.
3. Acordar el change, su alcance y los criterios de aceptación antes de implementar.
4. Usar el cliente y las herramientas disponibles en el entorno; el repositorio no instala configuraciones globales, MCP ni skills.

OpenSpec se configura en [openspec/config.yaml](openspec/config.yaml), con seis skills de proyecto en `.agents/skills/`; el CLI debe estar instalado para trabajar con ese flujo. `CLAUDE.md` conserva contenido idéntico a `AGENTS.md` por requisito académico. El [registro de skills versionado](.atl/skill-registry.md) describe el equipo donde se generó; regenerarlo en cada entorno.

Los scripts de Discovery son utilidades documentales, no pruebas del producto. [compilar_investigacion.py](docs/discovery/compilar_investigacion.py) usa Python y el helper global `web-scraper/scripts/slugify.py` de `~/.agents/skills/`. [generar_pdf.py](docs/discovery/generar_pdf.py) usa ReportLab; después de regenerar el PDF hay que revisarlo visualmente. Sin esas dependencias, los documentos ya generados se pueden consultar.

## Trabajo del equipo

Guía: [CONTRIBUTING.md](CONTRIBUTING.md). GitHub: Giovanni `@giovanniclmb`, Fabi `@FabianoRamos05`, Luciano `@Dreamboy2006` y Sofia `@jowy0`. Política aprobada: colaboradores con escritura; PR con aprobación de Giovanni y CI correcto; sus propios PRs están exentos de aprobación, con CI correcto y trazabilidad mediante PR.

El equipo está integrado por Giovanni (líder y propietario del repositorio), Sofia, Luciano y Fabi, con autorización docente informada para trabajar de a cuatro. El reparto de los 25 changes figura en el [plan de colaboración](docs/plan-colaboracion.md). La entrega es el 25/11/2026; completar el backlog no es requisito del único change académico.

Se permiten compilación, pruebas y commits autónomos dentro del alcance autorizado. Cada commit debe cubrir una tarea específica y tener un mensaje que explique el cambio y su propósito. **El push requiere un pedido explícito del usuario.**

Los integrantes [revisaron cinco fuentes](docs/entrega/verificacion-fuentes.md#planilla-personal), registradas el 25/11/2026. También están incorporadas la [captura de instalación](docs/evidencias/captura-instalador.png) y la [reflexión escrita](docs/entrega/reflexion.md). El video de defensa sigue pendiente. Las verificaciones del agente no reemplazan el trabajo de los integrantes.
