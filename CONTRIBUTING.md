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
5. Integrar mediante PR cuando `backend-verify` termine correctamente, la rama esté actualizada respecto de main y Giovanni apruebe. Un cambio posterior invalida la aprobación anterior y requiere nueva revisión.

No asumir que tener escritura permite hacer push directo a main. La protección exige PR, aprobación del propietario de código y CI; no permite force push ni borrar main. No se exige historial lineal: los merges preservan el trabajo integrado como ya ocurrió con C-01.

## PRs de Giovanni y excepción administrativa

GitHub no permite al autor aprobar su propio PR. Por decisión del usuario, los PRs cuyo autor sea Giovanni mantienen CI obligatorio como política del equipo y requieren una excepción explícita documentada al integrar. Giovanni conserva la capacidad administrativa de omitir la protección; no se aplica la regla a administradores para que esa excepción sea posible.

La excepción no se ejecuta automáticamente ni se interpreta como permiso para omitir un CI fallido. Registrar en el PR: motivo, autorización explícita y enlace al CI correcto del commit que se integra. Un agente debe solicitar esa autorización para el PR concreto antes de usar bypass. Para PRs de colaboradores, Giovanni aprueba normalmente; no usar bypass para sustituir esa revisión.

Limitación técnica: el bypass administrativo puede omitir más controles que la sola aprobación. Su uso está acotado por este procedimiento; GitHub no restringe esa capacidad únicamente a PRs propios en esta configuración.

## CI y cambios compartidos

El workflow CI ejecuta `mvn --batch-mode --no-transfer-progress -f backend/pom.xml clean verify` con Temurin 25 en pushes a main y PRs hacia main, incluidos los que solo modifican documentación, para que el check requerido siempre tenga resultado. Las Actions están fijadas por SHA y el token del workflow solo tiene lectura de contenidos.

CODEOWNERS asigna a Giovanni la revisión de todos los archivos, según la política elegida. No adjudica a Giovanni la implementación de todo el backlog ni modifica las asignaciones nominales aprobadas. Coordinar cambios en build, contratos, instrucciones, configuración y futuras migraciones para evitar ediciones simultáneas incompatibles.

## Datos, permisos y evidencia

Usar pacientes y datos clínicos ficticios. No publicar secretos ni credenciales. Mantener las reglas aprobadas de AGENTS.md y la KB. Las verificaciones de software no reemplazan la revisión personal de fuentes ni la reflexión académica de los integrantes.

Los agentes pueden compilar, probar y crear commits dentro del trabajo autorizado; cada nuevo push requiere un pedido explícito del usuario. La autorización para publicar esta configuración inicial no constituye un permiso permanente.

El reparto nominal de los 25 changes está aprobado y registrado en CHANGES.md y el plan de colaboración. La elección y creación de Issues/Projects continúa pendiente; esta aprobación no publica tareas remotas.

Fuentes: [protección de ramas](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches) y [CODEOWNERS](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners).
