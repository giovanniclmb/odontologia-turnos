# Colaborar en Odontología Turnos

## Equipo

| Integrante | GitHub | Responsabilidad confirmada |
| --- | --- | --- |
| Giovanni | [@giovanniclmb](https://github.com/giovanniclmb) | Propietario, coordinación, base y lógica central de agenda |
| Fabi | [@FabianoRamos05](https://github.com/FabianoRamos05) | Colaborador con escritura; changes asignados en el plan aprobado |
| Luciano | [@Dreamboy2006](https://github.com/Dreamboy2006) | Colaborador con escritura; changes asignados en el plan aprobado |
| Sofia | [@jowy0](https://github.com/jowy0) | Colaboradora con escritura; changes asignados en el plan aprobado |

Los colaboradores deben aceptar la invitación antes de poder escribir. Una invitación pendiente no equivale a acceso activo. Giovanni conserva la administración del repositorio personal; no se otorga administración a los demás.

## Preparar el entorno

Clonar el repositorio, leer AGENTS.md, la KB, CHANGES.md y [el plan de colaboración](docs/plan-colaboracion.md). Instalar JDK 25 y Maven; [backend/README.md](backend/README.md) explica el módulo y sus límites. Regenerar localmente el registro de skills según las herramientas realmente disponibles; `.atl/` no se publica ni se copia como configuración global.

Desde la raíz:

```sh
mvn -f backend/pom.xml test
mvn -f backend/pom.xml clean verify
```

## Trabajar por change

1. Preparar propuestas y contratos en paralelo. Implementar únicamente cuando todas las dependencias del change estén integradas en main y su alcance esté aprobado.
2. Actualizar main local y crear una rama por change. Para Codex, usar `codex/c-02-persistencia` como ejemplo; el ID y el nombre deben corresponder al change real.
3. Aplicar TDD a las reglas de negocio. Crear commits atómicos por tarea con mensajes concretos que expliquen propósito y resultado.
4. Abrir un PR hacia main con la plantilla; incluir dependencias, verificación y evidencia. Solicitar revisión a Giovanni; CODEOWNERS también la solicita automáticamente al marcar listo un PR.
5. Integrar mediante PR cuando `backend-verify` termine correctamente, la rama esté actualizada respecto de main y, si el autor es otro integrante, Giovanni apruebe. Los PRs de Giovanni no requieren aprobación de revisión. En los PRs de colaboradores, un cambio posterior invalida la aprobación anterior y requiere nueva revisión.

No asumir que tener escritura permite hacer push directo a main. Para colaboradores, la protección exige PR, aprobación de Giovanni y CI; no permite force push ni borrar main. Giovanni conserva la exención administrativa de revisión descrita abajo. No se exige historial lineal: los merges preservan el trabajo integrado como ya ocurrió con C-01.

## Cambios de Giovanni sin aprobación de revisión

Por decisión expresa del líder el 19/11/2026, los PRs de Giovanni no requieren autoaprobación, aprobación de otro integrante ni autorización adicional de excepción para cada PR. Si la tarea autoriza integrar su cambio y el CI del commit está correcto, el agente puede usar la facultad administrativa necesaria sin volver a pedir permiso de revisión.

Se mantienen PR para trazabilidad, CI correcto, dependencias del roadmap y reglas del proyecto. Los PRs de Fabi, Luciano y Sofia sí requieren la aprobación de Giovanni. Esta exención de revisión no elimina la regla de pedir explícitamente cada push al usuario, ni autoriza por sí sola un merge que no forme parte de la tarea solicitada.

Implementación en GitHub: `enforce_admins=false` permite al propietario integrar sin satisfacer la aprobación requerida por CODEOWNERS. Los demás colaboradores no tienen administración y quedan sujetos a revisión. Limitación técnica: la capacidad administrativa puede omitir también otros controles; el procedimiento del equipo conserva CI correcto y no autoriza saltarse pruebas fallidas.

## CI y cambios compartidos

El workflow CI ejecuta `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify` con Temurin 25 en pushes a main y PRs hacia main, incluidos los que solo modifican documentación, para que el check requerido siempre tenga resultado. Las Actions están fijadas por SHA. CI conserva su token con lectura de contenidos; el workflow separado de asignación solicita además escritura de Issues para aplicar el reparto aprobado.

CODEOWNERS asigna a Giovanni la revisión de todos los archivos, según la política elegida. No adjudica a Giovanni la implementación de todo el backlog ni modifica las asignaciones nominales aprobadas. Coordinar cambios en build, contratos, instrucciones, configuración y futuras migraciones para evitar ediciones simultáneas incompatibles.

## Datos, permisos y evidencia

Usar pacientes y datos clínicos ficticios. No publicar secretos ni credenciales. Mantener las reglas aprobadas de AGENTS.md y la KB. Las verificaciones de software no reemplazan la revisión personal de fuentes ni la reflexión académica de los integrantes.

Los agentes pueden compilar, probar y crear commits dentro del trabajo autorizado; cada nuevo push requiere un pedido explícito del usuario. La autorización para publicar esta configuración inicial no constituye un permiso permanente.

El reparto nominal de los 25 changes está aprobado y registrado en CHANGES.md y el plan de colaboración. Los Issues C-02 a C-25 ya existen en GitHub; Projects no fue configurado. La [automatización de asignaciones](docs/automatizacion-asignaciones.md) aplica el reparto a Issues sin responsable cuando el integrante tiene acceso activo con escritura. Su preparación local no la activa: requiere integrar el workflow en la rama predeterminada. Una asignación no habilita implementar antes de cumplir las dependencias y aprobaciones.

Fuentes: [protección de ramas](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches) y [CODEOWNERS](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners).
