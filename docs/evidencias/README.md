# Evidencias de la fundación

Esta carpeta conserva verificaciones, salidas de herramientas y utilidades de reproducción de la preparación del entorno y la documentación. Un registro debe leerse con su fecha y alcance: una comprobación de un MCP o del PDF no demuestra que la aplicación esté implementada.

## Archivos versionados

- Verificaciones finales de Discovery, KB, roadmap, recomendaciones, registro de skills e instrucciones de agentes.
- Evidencia de la vinculación del repositorio y su preparación para versionado.
- Logs históricos, parche y utilidades citados en la preparación del entorno, para mantener trazabilidad de sus incidencias reales.

Algunas evidencias contienen rutas absolutas, versiones y hashes de la máquina donde se produjeron. Se conservan como datos históricos; no deben interpretarse como configuración portable ni como verificaciones realizadas por otro integrante.

Los logs y parches históricos conservan su formato exacto. `.gitattributes` los excluye del control de espacios de Git para no alterar salidas de herramientas o líneas de contexto de un parche; el resto de los archivos de texto mantiene ese control. PDF y DOCX se tratan como binarios.

## Archivos locales

Los respaldos `antes-*`, `estado-antes-*`, `discovery-antes-consolidacion` y `ampliacion-paciente-antes-*` están excluidos de Git y se mantienen en disco. Son snapshots intermedios, no el estado vigente. El historial Git conserva los cambios posteriores al primer versionado.

El [registro de skills](../../.atl/skill-registry.md) se versiona desde el 25/11/2026 porque la consigna lo exige; el resto de `.atl/` sigue ignorado. Para trabajar desde otro entorno debe regenerarse con `skill-registry`; no atribuir las skills globales de una máquina a todos los integrantes.

## Pendientes

El 25/11/2026 se incorporaron la [captura de instalación y harnesses](captura-instalador.png), que muestra las salidas de `mostrar-evidencia.sh`, y la [revisión personal de cinco fuentes](../entrega/verificacion-fuentes.md#planilla-personal). La reflexión continúa pendiente. Los logs de instalación y las comprobaciones automatizadas no sustituyen las acciones de los integrantes.
