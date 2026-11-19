# Revisión independiente de C-02

Usuario autorizó Reviewer Astra / medium por indisponibilidad de Luna. El Worker de infraestructura usó Sol / medium. El Reviewer trabajó en lectura, sin editar ni ejecutar Maven. El Integrator ejecutó las verificaciones combinadas.

## Hallazgos y correcciones

- P1: propiedades Spring externas podían desviar las IT hacia otro destino. Se reprodujo con propiedades de proceso que apuntan a una URL centinela inaccesible. Se aislaron Environment y ubicación de configuración en las IT. Las variables del entorno de desarrollo no se leen.
- P2: la conexión ordinaria podía reutilizar identidad del migrador. Se reprodujo y se verifican ambas identidades efectivas antes de abrir conexiones; se rechaza también bootstrap y overrides Spring peligrosos.
- P2: Flyway aceptaba por defecto migraciones futuras. Se reprodujo con V2 en un recurso temporal aislado y se vació ignore-migration-patterns. El binario V1 ahora rechaza ese historial sin repararlo.
- P2: credenciales en la URL JDBC prevalecían sobre las propiedades verificadas. Cinco regresiones reprodujeron el defecto. Se rechazan parámetros user/password/service y userinfo antes de conectar, con claves decodificadas y comprobación adicional mediante el parser pgJDBC. El diagnóstico no incluye la URL ni sus valores.

Los siete primeros fallos están en 13-red-review-regressions.log y 14-red-migration-identity.log; después pasaron 17 casos. Los cinco de URL están en 16-red-url-credentials.log; después pasaron las 22 pruebas de ArranqueIT en 17-green-url-credentials.log. El Reviewer confirmó cierre y ausencia de otros hallazgos en la corrección revisada. Alcance: bootstrap, Compose, modelo, migración, configuración y pruebas de C-02; no es una auditoría exhaustiva ni garantía de ausencia de defectos.
