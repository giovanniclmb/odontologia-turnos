# Memoria del agente (Engram)

## Qué hay guardado

| Almacén | Contenido observado el 25/11/2026 |
| --- | --- |
| Local, en el equipo de Giovanni, proyecto `integrador odonto` | 120 memorias entre el 17 y el 25/11 (marcas en UTC): 42 decisiones, 31 de configuración, 26 hallazgos, 10 correcciones, 9 de arquitectura y 2 preferencias |
| Exportado al repositorio, [`.engram/`](../../.engram/manifest.json) | Un bloque creado por Fabi con 88 memorias. Su numeración no coincide necesariamente con la local |

## Consulta real

Ejecutada el 25/11/2026 desde una sesión de Claude Code distinta de las que guardaron las memorias, con Engram 3.7.2:

```sh
engram search "C-01" --project "integrador odonto" --limit 8
```

Devolvió ocho resultados. Los que corresponden al change académico:

| Memoria | Fecha local | Título |
| --- | --- | --- |
| 45 | 19/11 03:51 | Seleccionó C-01 a cargo de Giovanni |
| 46 | 19/11 03:51 | Confirmó borde inclusivo del intervalo en C-01 |
| 47 | 19/11 03:52 | Aprobó alcance aislado de dominio para C-01 |
| 48 | 19/11 04:01 | Acordó Java 25 y preparó propuesta C-01 |
| 49 | 19/11 04:31 | Implementó y verificó C-01 con 55 pruebas |
| 50 | 19/11 10:39 | Archivó C-01 y lo integró en main |

Las otras dos son la 63 (C-02) y la 151 (traspaso a Claude Code).

## Cómo evitó empezar de cero

- La memoria 46 conserva que la separación exacta al intervalo se admite y que **los 10 minutos eran un ejemplo, no un valor de producción**. Una sesión nueva que solo viera las pruebas podría tomar ese número como configuración.
- La memoria 47 conserva por qué C-01 recibe únicamente reservas ocupantes y no define estados: así no se reabre ni se da por resuelta la pregunta global.
- La memoria 45 distingue haber elegido el change de haber aprobado su implementación.

## Para el video

Cada integrante que presente esta parte debe ejecutar la consulta en pantalla y explicarlo con sus palabras. Se hace en el equipo que tiene el almacén local; en otro, los resultados pueden diferir. Revisar la pantalla antes de grabar: las memorias contienen rutas locales.
