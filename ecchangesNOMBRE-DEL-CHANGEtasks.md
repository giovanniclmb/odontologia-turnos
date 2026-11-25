[1mdiff --git a/CHANGES.md b/CHANGES.md[m
[1mindex bfb8344..9652889 100644[m
[1m--- a/CHANGES.md[m
[1m+++ b/CHANGES.md[m
[36m@@ -366,10 +366,10 @@[m [mGiovanni aprobó el reparto el 19/11/2026. Las ondas preservan las dependencias[m
   - [Preguntas abiertas](knowledge-base/10_preguntas_abiertas.md)[m
 [m
 ### [C-21] `inscripcion-y-preferencias-de-espera`[m
[31m-- **Estado**: `[ ]` pendiente[m
[32m+[m[32m- **Estado**: implementado y verificado localmente. Migración V12, titularidad, vigencia y concurrencia verificadas con PostgreSQL 18.7. Archivo OpenSpec pendiente. [Tareas y evidencia](openspec/changes/c-21-inscripcion-y-preferencias-de-espera/tasks.md)[m
 - **Responsable**: Sofia[m
 - **Scope**: Gestión backend de inscripción propia para US-014 y RN-LE-01.[m
[31m-  - Resolver PA-16/PA-20: ciclo y vigencia de inscripción, operaciones autorizadas y criterios de compatibilidad. No inventar vencimientos, prioridad o cantidad de inscripciones.[m
[32m+[m[32m  - C-16 define la autorización por vínculo y titularidad. Para C-21 se aprobó crear, consultar, editar y dar de baja inscripciones propias, con una vigente por prestación y fin de vigencia al finalizar el rango de fechas o al darla de baja. C-22 resuelve la compatibilidad exacta de huecos y la asignación.[m
   - Implementar el contrato y representación aprobados para prestación, rango de fechas y franjas horarias, con profesional opcional; proteger cada operación mediante C-16.[m
   - Tests: preferencias válidas e inválidas según spec, profesional omitido y acceso propio/ajeno; mantener separada la inscripción de una reserva.[m
 - **Dependencias**: C-16, C-05[m
[1mdiff --git a/backend/src/main/java/ar/edu/utn/odontologia/referencias/persistencia/PacienteReferencias.java b/backend/src/main/java/ar/edu/utn/odontologia/referencias/persistencia/PacienteReferencias.java[m
[1mindex 65ff5f8..3fa075e 100644[m
[1m--- a/backend/src/main/java/ar/edu/utn/odontologia/referencias/persistencia/PacienteReferencias.java[m
[1m+++ b/backend/src/main/java/ar/edu/utn/odontologia/referencias/persistencia/PacienteReferencias.java[m
[36m@@ -1,11 +1,18 @@[m
 package ar.edu.utn.odontologia.referencias.persistencia;[m
 [m
[32m+[m[32mimport jakarta.persistence.LockModeType;[m
 import java.util.List;[m
[32m+[m[32mimport java.util.Optional;[m
 import java.util.UUID;[m
[32m+[m[32mimport org.springframework.data.jpa.repository.Lock;[m
 import org.springframework.data.jpa.repository.Query;[m
 import org.springframework.data.repository.query.Param;[m
 [m
 public interface PacienteReferencias extends Referencias<PacienteReferencia> {[m
[32m+[m[32m    @Lock(LockModeType.PESSIMISTIC_WRITE)[m
[32m+[m[32m    @Query("select p from PacienteReferencia p where p.id = :id")[m
[32m+[m[32m    Optional<PacienteReferencia> buscarParaModificarEspera(@Param("id") UUID id);[m
[32m+[m
     @Query("""[m
             select p from PacienteReferencia p[m
             where (coalesce(:codigo, '') = '' or p.codigo = :codigo)[m
[1mdiff --git a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/AccesoPersistenciaIT.java b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/AccesoPersistenciaIT.java[m
[1mindex 8bf83fe..fd6283b 100644[m
[1m--- a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/AccesoPersistenciaIT.java[m
[1m+++ b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/AccesoPersistenciaIT.java[m
[36m@@ -292,7 +292,7 @@[m [mclass AccesoPersistenciaIT {[m
     void migracionNoSeReaplica() throws Exception {[m
         try (var c = db.migrationConnection(); var q = c.createStatement(); var r = q.executeQuery("select count(*) from odontologia.flyway_schema_history where success")) {[m
             assertTrue(r.next());[m
[31m-            assertEquals(11, r.getInt(1));[m
[32m+[m[32m            assertEquals(12, r.getInt(1));[m
         }[m
     }[m
 }[m
[1mdiff --git a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/ArranqueIT.java b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/ArranqueIT.java[m
[1mindex c836990..9af6154 100644[m
[1m--- a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/ArranqueIT.java[m
[1m+++ b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/ArranqueIT.java[m
[36m@@ -134,14 +134,15 @@[m [mclass ArranqueIT {[m
             Files.copy(Path.of("src/main/resources/db/migration/V9__cancelacion_de_turnos.sql"),temporal.resolve("V9__cancelacion_de_turnos.sql"));[m
             Files.copy(Path.of("src/main/resources/db/migration/V10__cuentas_paciente.sql"),temporal.resolve("V10__cuentas_paciente.sql"));[m
             Files.copy(Path.of("src/main/resources/db/migration/V11__email_envio.sql"),temporal.resolve("V11__email_envio.sql"));[m
[31m-            Files.writeString(temporal.resolve("V12__futura.sql"),"CREATE TABLE odontologia.futura (id uuid PRIMARY KEY); SELECT 1;\n");[m
[32m+[m[32m            Files.copy(Path.of("src/main/resources/db/migration/V12__inscripciones_lista_de_espera.sql"),temporal.resolve("V12__inscripciones_lista_de_espera.sql"));[m
[32m+[m[32m            Files.writeString(temporal.resolve("V13__futura.sql"),"CREATE TABLE odontologia.futura (id uuid PRIMARY KEY); SELECT 1;\n");[m
             try(var c=abrir(aislada,Map.of("spring.flyway.locations","filesystem:"+temporal))){}[m
             var error=assertThrows(RuntimeException.class,()->{try(var c=abrir(aislada,Map.of())){} });[m
             String mensaje=cadena(error).toLowerCase(Locale.ROOT);[m
             assertTrue(mensaje.contains("migration"));[m
[31m-            assertTrue(mensaje.contains("12"), "El rechazo debe corresponder a la version futura 12: " + mensaje);[m
[32m+[m[32m            assertTrue(mensaje.contains("13"), "El rechazo debe corresponder a la version futura 13: " + mensaje);[m
             try(var c=aislada.migrationConnection();var q=c.createStatement();var result=q.executeQuery("select count(*) from odontologia.flyway_schema_history where success")){[m
[31m-                assertTrue(result.next());assertEquals(12,result.getInt(1));[m
[32m+[m[32m                assertTrue(result.next());assertEquals(13,result.getInt(1));[m
             }[m
         }[m
     }[m
[1mdiff --git a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/CuentasPacienteMigracionIT.java b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/CuentasPacienteMigracionIT.java[m
[1mindex 135b491..6fe6b3e 100644[m
[1m--- a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/CuentasPacienteMigracionIT.java[m
[1m+++ b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/CuentasPacienteMigracionIT.java[m
[36m@@ -55,7 +55,7 @@[m [mclass CuentasPacienteMigracionIT {[m
     @Test[m
     void v10PermiteCuentaPendienteYProtegeEmailVinculoYPropiedad() throws Exception {[m
         try (var db = PostgresFixture.start()) {[m
[31m-            assertEquals(11, migrador(db).load().migrate().migrationsExecuted);[m
[32m+[m[32m            assertEquals(12, migrador(db).load().migrate().migrationsExecuted);[m
             UUID pacienteA = UUID.randomUUID();[m
             UUID pacienteB = UUID.randomUUID();[m
             insertarPaciente(db, pacienteA, "PACIENTE-CUENTA-A", "compartido@example.invalid");[m
[1mdiff --git a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/RecursosMigracionIT.java b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/RecursosMigracionIT.java[m
[1mindex 4abf330..b7be88d 100644[m
[1m--- a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/RecursosMigracionIT.java[m
[1m+++ b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/RecursosMigracionIT.java[m
[36m@@ -12,7 +12,7 @@[m [mclass RecursosMigracionIT {[m
     @Test[m
     void unaInstalacionNuevaNoSiembraRecursosNiPrestaciones() throws Exception {[m
         try (var db = PostgresFixture.start()) {[m
[31m-            assertEquals(11, migrador(db).load().migrate().migrationsExecuted);[m
[32m+[m[32m            assertEquals(12, migrador(db).load().migrate().migrationsExecuted);[m
             try (var con = db.appConnection(); var sql = con.createStatement();[m
                  var resultado = sql.executeQuery("""[m
                          select (select count(*) from odontologia.profesional)[m
[1mdiff --git a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/SchemaIT.java b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/SchemaIT.java[m
[1mindex 91a3ef5..e5f89ca 100644[m
[1m--- a/backend/src/test/java/ar/edu/utn/odontologia/persistencia/SchemaIT.java[m
[1m+++ b/backend/src/test/java/ar/edu/utn/odontologia/persistencia/SchemaIT.java[m
[36m@@ -28,7 +28,7 @@[m [mclass SchemaIT {[m
             assertTrue(rs.next()); assertEquals(14, rs.getInt(1));[m
         }[m
         assertEquals(0, f.migrate().migrationsExecuted);[m
[31m-        assertEquals(11, f.info().applied().length);[m
[32m+[m[32m        assertEquals(12, f.info().applied().length);[m
     }[m
 [m
     @ParameterizedTest @ValueSource(strings={"paciente","profesional","sillon","prestacion"})[m
[1mdiff --git a/knowledge-base/10_preguntas_abiertas.md b/knowledge-base/10_preguntas_abiertas.md[m
[1mindex 3765027..824b4b4 100644[m
[1m--- a/knowledge-base/10_preguntas_abiertas.md[m
[1m+++ b/knowledge-base/10_preguntas_abiertas.md[m
[36m@@ -36,7 +36,7 @@[m [mLa columna de consulta identifica a quién le correspondería resolver cada preg[m
 | PA-17 | Alta | ¿Cómo se evalúa el instante exacto del corte de 48 horas, incluyendo la referencia horaria aplicable? ¿Qué vigencia y condiciones de uso tiene el enlace y qué ocurre si se reutiliza, vence o el turno cambia? | Cancelación por enlace | Usuario y equipo |[m
 | PA-18 | Alta | ¿Cómo se tratan el email de alta y el recordatorio ante reprogramación, cancelación o creación dentro de las ventanas de 48 o 24 horas? ¿Qué tratamiento reciben los envíos duplicados o fallidos en cada tipo de email? | Programación y entrega de email | Usuario y equipo |[m
 | PA-19 | Alta | ¿Quién puede quitar o cambiar un bloqueo, con qué condiciones y qué ocurre con la disponibilidad resultante? ¿Cómo se validan límites de períodos y solicitudes simultáneas de bloqueo y turno? | Agenda y bloqueos | Usuario y equipo |[m
[31m-| PA-20 | Alta | ¿Cómo se determina la compatibilidad del hueco con preferencias, duración, recursos e intervalo? ¿Qué operaciones y vigencia tiene la inscripción, cómo decide recepción entre interesados y cómo se revalida la disponibilidad al asignar? | Lista de espera y agenda | Usuario y equipo |[m
[32m+[m[32m| PA-20 | Alta | C-21 tiene aprobadas las operaciones de alta, consulta, edición y baja propias, una inscripción vigente por prestación y el fin de vigencia al finalizar el rango de fechas o al darla de baja. Para C-22 queda pendiente cómo se determina la compatibilidad con preferencias, duración, recursos e intervalo, cómo decide recepción entre interesados y cómo se revalida la disponibilidad al asignar. | Lista de espera y agenda | Usuario y equipo |[m
 | PA-21 | Alta | ¿Qué ocurre con quienes recibieron un aviso si el hueco se ocupa antes de la asignación? ¿Qué información se muestra o comunica y cómo se tratan avisos pendientes o repetidos? | Disponibilidad y avisos de lista de espera | Usuario y equipo |[m
 [m
 [Ampliación aprobada y sus pendientes de especificación](../docs/discovery/ampliacion-paciente.md#pendientes-de-especificación-que-no-se-resuelven-por-inferencia)[m
