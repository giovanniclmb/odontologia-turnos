# Tasks

Responsable: Giovanni. Gobernanza: CRITICO. Plan y Apply aprobados expresamente el 19/11/2026; avance observado en las casillas. Cada tarea se acota a una sesión orientativa menor de dos horas; dividir si aparece complejidad adicional, sin recortar el spec. [Diseño](design.md) · [Spec](specs/persistencia-de-referencias/spec.md).

## 1. Base de arranque y build

- [x] 1.1 Confirmar rama aislada, árbol de trabajo y C-01 integrado; ejecutar las 55 pruebas actuales y registrar la línea base sin PostgreSQL antes de cambiar el build.
- [x] 1.2 Incorporar Boot 4.1.2, gestión compatible de JPA/Flyway/PostgreSQL/Testcontainers y Failsafe al módulo Java 25; verificar dependency:tree, ausencia de snapshots y que mvn test mantiene las 55 pruebas sin Docker. Registrar versiones efectivas en README.

## 2. Entorno PostgreSQL y credenciales

- [x] 2.1 Crear compose.yaml para PostgreSQL 18.7 con healthcheck, volumen y puerto local configurable, más .env.example sin secretos; comprobar docker compose config --quiet con variables de prueba y registrar que no apunta al Postgres.app del host.
- [x] 2.2 Preparar el fixture PostgreSQL 18.7 temporal de Testcontainers con puerto aleatorio, sin leer conexiones de desarrollo ni depender del .env del equipo; dejarlo disponible para las pruebas del bootstrap, migraciones y JPA, y comprobar el aislamiento de configuración sin tratar un fallo de Docker como RED.
- [x] 2.3 Escribir pruebas reales del bootstrap antes de implementarlo y separar administrador inicial, migrador y usuario ordinario; implementar script y propiedades, verificar roles sin SUPERUSER/CREATEDB/CREATEROLE y destino/identidad de ambas conexiones. Documentar inicialización única, credenciales y ausencia de limpieza automática.

## 3. Migración y referencias

- [x] 3.1 Escribir pruebas que observen la ausencia del esquema requerido, añadir V1__referencias_base.sql con las cuatro tablas y sus permisos, y verificar RED/GREEN contra PostgreSQL real con Flyway. Documentar la convención global consecutiva, out-of-order=false y clean deshabilitado.
- [x] 3.2 Escribir pruebas SQL directas de PK, código obligatorio y unicidad por tipo, incluidos mismo código entre tipos y conservación de datos confirmados ante rechazo; implementar o corregir restricciones hasta GREEN y registrar casos y SQLStates observados sin secretos.
- [x] 3.3 Escribir pruebas de guardar/recuperar cada referencia en transacciones/lecturas nuevas con UUID generado en aplicación; implementar entidades JPA y repositorios mínimos separados de Agenda, verificar GREEN con ddl-auto=validate y documentar el modelo de dos columnas por tipo.
- [x] 3.4 Probar en PostgreSQL real que el usuario ordinario guarda/lee referencias pero no puede crear tablas, mientras las tablas y migraciones pertenecen al migrador no superusuario; comprobar GREEN sin conectar JPA como administrador y añadir evidencia de permisos.
- [x] 3.5 Con las referencias y roles disponibles, escribir prueba de arranque completo no-web y bean Agenda, observar el fallo por comportamiento faltante e implementar el arranque/configuración externa mínimos; verificar GREEN y documentar que no hay servidor HTTP ni cambios al dominio.

## 4. Arranques repetidos y fallos

- [x] 4.1 Escribir y ejecutar prueba de dos contextos sucesivos sobre la misma base, verificando referencias conservadas y una sola aplicación de V1; corregir por TDD cualquier recreación o reaplicación y documentar el resultado observado.
- [x] 4.2 Probar checksum alterado mediante recurso temporal de prueba, propiedades obligatorias ausentes, credenciales incorrectas y destino inaccesible con espera acotada; verificar fallo explícito, sin repair/clean, fallback de base o contraseñas expuestas, registrando RED/GREEN aplicables y diagnóstico.
- [x] 4.3 Verificar que el preflight de Docker falla de forma reproducible cuando no puede acceder al daemon, sin detener el Docker del usuario ni modificar datos; comprobar que Failsafe no omite IT y documentar los requisitos de mvn test frente a clean verify.

## 5. CI y verificación del conjunto

- [x] 5.1 Mantener el check backend-verify y añadir comprobación explícita de Docker antes de clean verify en CI; ejecutar ese flujo local con el entorno aprobado y registrar unitarias e IT sin omisiones, conservando permisos de lectura y política de revisión existentes.
- [x] 5.2 Ejecutar clean verify completo y revisar reportes unitarios/IT, trazabilidad de todos los escenarios y logs sin secretos; registrar Verify con entorno, comandos, resultados y límites. Si no se ejecuta CI remoto por no haber pedido de push, declararlo pendiente sin inventar resultado.
- [x] 5.3 Revisar esquema, privilegios, separación del dominio y manejo de errores; corregir hallazgos con TDD y repetir comprobaciones afectadas. Registrar si la revisión fue de la misma sesión o independiente, sin afirmar un modelo no disponible.
- [x] 5.4 Ejecutar openspec validate persistencia-base-postgresql --strict y git diff --check; conciliar README/KB/CHANGES con decisiones y evidencia de C-02 sin alterar dependencias ni cerrar PA fuera de alcance. Verificar enlaces y scripts documentados.

## Workflow follow-up

- Presentar el resultado y los límites antes de cerrar el change; las pruebas no sustituyen evidencia humana académica.
- Archivar mediante CLI y skill cuando el usuario pida finalizar, sincronizando la capacidad principal y comprobando el archivo.
- Hacer commits atómicos dentro del trabajo autorizado. Publicar y abrir/integrar PR únicamente dentro del pedido correspondiente de push/integración; la exención de revisión de Giovanni no publica cambios por sí sola.
- Mantener el Issue #3 y sus dependencias como seguimiento del trabajo real; la planificación completa no equivale a implementación terminada ni desbloquea código de C-03 antes de integrar C-02.
