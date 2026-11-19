# Verify — C-01

Fecha: 19/11/2026. Responsable: Giovanni. Plan aprobado por el usuario antes de Apply; trabajo en `codex/c-01-dominio`, worktree aislado autorizado. Estado: implementado y verificado; pendiente de Archive e integración a main. Sin push.

## Resultado observado

- `mvn -f backend/pom.xml validate`: correcto (preparación, no ejecución de tests).
- `mvn -f backend/pom.xml test`: 55 pruebas, cero fallos, errores u omisiones.
- `mvn -f backend/pom.xml clean verify`: 55 pruebas, cero fallos, errores u omisiones; JAR generado.
- OpenSpec y diff: ver registro JSON de verificación final.

Entorno: OpenJDK 25.0.4, Maven 3.9.16, JUnit Jupiter 6.1.3, Compiler Plugin 3.16.0 y Surefire 3.6.1. Los resultados se guardan en logs; los reportes Surefire originales quedan bajo target/ y no se versionan.

## Evidencia TDD

| Archivo | Resultado y propósito |
| --- | --- |
| 00-test-compilation.log | Incidencia inicial: inferencia genérica de assertInstanceOf con dos clases alternativas. Se corrigió con ramas explícitas; no cuenta como RED. |
| 01-red.log | Esqueleto sin comportamiento: 55 fallos de assertions, cero errores. |
| 02-green-entradas.log | 35 pruebas de entradas y duración correctas. |
| 03-red-conflictos.log | 6 fallos esperados de conflictos aún no implementados. |
| 04-green-conflictos.log | 7 pruebas de conflictos y recursos distintos correctas. |
| 05-red-intervalo.log | 8 casos, 4 fallos: todavía se admitían separaciones menores que el intervalo. |
| 06-green-suite.log | Suite completa de 55 pruebas correcta. |
| 07-clean-verify.log | Reconstrucción completa, 55 pruebas correctas y JAR. |

Las pruebas de ajuste, orden e inmutabilidad también fallaron en el RED inicial antes de implementar. No se fabricó un fallo posterior para ellas. La trazabilidad de los 17 escenarios se conserva en [trazabilidad.md](trazabilidad.md).

## Revisión de la sesión

Se revisaron fórmula simétrica, igualdad, ausencia de doble intervalo, recursos independientes y compartidos, períodos iguales y contención, cambio de duración, intervalos explícitos distintos, lista no ordenada, entradas inválidas, desbordamientos e inmutabilidad. No se encontraron defectos pendientes dentro del alcance. Es revisión de la misma sesión, no revisión independiente ni garantía de ausencia de errores; no se seleccionó un modelo sustituto para Luna.

## Límites

No se guarda el turno ni se obtiene exclusión concurrente; el llamador proporciona una instantánea válida de reservas ocupantes. El intervalo de 10 minutos es ficticio, no un valor inicial. Estados, permisos, horarios laborales, bloqueos, API, Spring Boot y PostgreSQL se definirán en changes posteriores. Las pruebas y Verify no completan la revisión personal de fuentes ni las reflexiones académicas.

Los documentos de colaboración que estaban sin commit en la carpeta original se conservaron allí; este worktree solo recibió la propuesta de C-01 y su evidencia. Los snapshots documentales anteriores mantienen su fecha e interpretación histórica; los hashes vigentes se registran en la verificación de este change.
