# Trazabilidad de C-02

Los 18 escenarios del [spec](../specs/persistencia-de-referencias/spec.md) se comprobaron con PostgreSQL real temporal. El detalle de conteos y hashes está en verification.json.

| Escenario | Prueba / evidencia |
| --- | --- |
| Guardar y recuperar cada tipo | ReferenciasIT.generaUuidYRecuperaEnOtraTransaccion, cuatro tipos |
| Identidades diferentes en nuevas referencias | ReferenciasIT.generaUuidYRecuperaEnOtraTransaccion |
| Duplicado dentro del mismo tipo | SchemaIT.restriccionesRealesConservanDatosConfirmados, duplicate-code |
| Código compartido por tipos diferentes | SchemaIT.permiteCodigoEntreTiposYNoNormaliza |
| Inserción directa con datos inválidos | SchemaIT.restriccionesRealesConservanDatosConfirmados, 24 casos SQLState |
| Reiniciar sobre la misma base | ReferenciasIT.conservaReferenciasEHistorialAlReiniciar |
| Primera migración en base sin tablas | SchemaIT.aplicaCuatroTablasUnaSolaVez |
| Segundo arranque sin cambios | SchemaIT.aplicaCuatroTablasUnaSolaVez y ReferenciasIT.conservaReferenciasEHistorialAlReiniciar |
| Discrepancia de migración aplicada | ArranqueIT.checksumAlteradoFallaSinRepararNiBorrarDatos |
| Configuración obligatoria ausente | ArranqueIT.rechazaConfiguracionAusente, cinco variables |
| Credenciales rechazadas | ArranqueIT.rechazaCredencialIncorrectaSinRevelarla, ambas conexiones |
| Base inaccesible | ArranqueIT.rechazaDestinoInaccesibleConEsperaAcotada |
| Usuario ordinario limitado | SchemaIT.propietarioEsMigradorNoSuperusuario y BootstrapIT |
| Migraciones con identidad separada | BootstrapIT.verificaIdentidadDestinoYPrivilegiosDeAmbasConexiones y SchemaIT |
| Pruebas aisladas | PostgresFixture, Environment aislado; ArranqueIT.ignoraPropiedadesSpringDelProcesoAnfitrion con URL centinela |
| Contenedores no disponibles | preflight real, sin daemon y sin docker en PATH: infra-docker.log; Failsafe sin omisiones en Verify |
| Regresión del dominio | AgendaTest: 55 pruebas sin PostgreSQL en test, y dentro de clean verify |
| Arranque sin servidor web | ArranqueIT.arrancaSinHttpYExponeAgenda y ArranqueJarIT.jarEjecutableArrancaYTerminaSinServidor |

Regresiones adicionales de revisión: migrador/administrador usados como identidad ordinaria, override Spring de identidad, administrador como migrador, historial V2 no presente en el binario y credenciales en URL JDBC incluso codificadas. No se fabricaron fallos para funciones de librería; los logs RED muestran el comportamiento faltante o el defecto reproducido.
