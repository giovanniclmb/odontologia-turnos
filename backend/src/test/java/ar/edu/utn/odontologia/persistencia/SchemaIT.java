package ar.edu.utn.odontologia.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SchemaIT {
    private static PostgresFixture db;

    @BeforeAll static void start() { db = PostgresFixture.start(); }
    @AfterAll static void stop() { if (db != null) db.close(); }

    private Flyway flyway() {
        return Flyway.configure().dataSource(db.jdbcUrl(), "odonto_migrator", db.migrationPassword())
                .schemas("odontologia").createSchemas(false).cleanDisabled(true).outOfOrder(false).load();
    }

    @Test void aplicaOnceMigracionesYCatorceTablasUnaSolaVez() throws Exception {
        var f = flyway();
        f.migrate();
        try (var c = db.migrationConnection(); var st = c.createStatement()) {
            var rs = st.executeQuery("select count(*) from information_schema.tables where table_schema='odontologia' and table_name in ('paciente','profesional','sillon','prestacion','usuario','token_acceso','historial_contrasena','configuracion_sede','paciente_profesional','ficha_clinica','nota_clinica','bloqueo_recurso','turno','email_envio')");
            assertTrue(rs.next()); assertEquals(14, rs.getInt(1));
        }
        assertEquals(0, f.migrate().migrationsExecuted);
        assertEquals(12, f.info().applied().length);
    }

    @ParameterizedTest @ValueSource(strings={"paciente","profesional","sillon","prestacion"})
    void propietarioEsMigradorNoSuperusuario(String tabla) throws Exception {
        flyway().migrate();
        try (var c = db.appConnection(); var q = c.prepareStatement("select tableowner from pg_tables where schemaname='odontologia' and tablename=?")) {
            q.setString(1,tabla);var r=q.executeQuery();assertTrue(r.next());assertEquals("odonto_migrator",r.getString(1));
            var role = c.createStatement().executeQuery("select current_user, rolsuper, rolcreatedb, rolcreaterole from pg_roles where rolname=current_user");
            assertTrue(role.next());assertEquals("odonto_app",role.getString(1));assertFalse(role.getBoolean(2));assertFalse(role.getBoolean(3));assertFalse(role.getBoolean(4));
            var error=assertThrows(SQLException.class,()->c.createStatement().execute("create table odontologia.no_autorizada(id integer)"));
            assertEquals("42501",error.getSQLState());
        }
    }

    @ParameterizedTest @CsvSource({
        "paciente,null-id,23502","paciente,duplicate-id,23505","paciente,null-code,23502","paciente,empty-code,23514","paciente,spaces-code,23514","paciente,duplicate-code,23505",
        "profesional,null-id,23502","profesional,duplicate-id,23505","profesional,null-code,23502","profesional,empty-code,23514","profesional,spaces-code,23514","profesional,duplicate-code,23505",
        "sillon,null-id,23502","sillon,duplicate-id,23505","sillon,null-code,23502","sillon,empty-code,23514","sillon,spaces-code,23514","sillon,duplicate-code,23505",
        "prestacion,null-id,23502","prestacion,duplicate-id,23505","prestacion,null-code,23502","prestacion,empty-code,23514","prestacion,spaces-code,23514","prestacion,duplicate-code,23505"})
    void restriccionesRealesConservanDatosConfirmados(String tabla,String caso,String estado) throws Exception {
        flyway().migrate();
        var id=UUID.randomUUID();var codigo="CONFIRMADO-"+id;
        try(var c=db.appConnection()) {
            insertar(c,tabla,id,codigo); c.setAutoCommit(false);
            UUID segundo=caso.equals("null-id")?null:caso.equals("duplicate-id")?id:UUID.randomUUID();
            String texto=switch(caso) {case "null-code"->null;case "empty-code"->"";case "spaces-code"->"   ";case "duplicate-code"->codigo;default->"OTRO-"+id;};
            var error=assertThrows(SQLException.class,()->insertar(c,tabla,segundo,texto));
            assertEquals(estado,error.getSQLState());c.rollback();
            try(var q=c.prepareStatement("select codigo from odontologia."+tabla+" where id=?")){
                q.setObject(1,id);var rs=q.executeQuery();assertTrue(rs.next());assertEquals(codigo,rs.getString(1));
            }
        }
    }

    @Test void permiteCodigoEntreTiposYNoNormaliza() throws Exception {
        flyway().migrate();String codigo="MISMO-"+UUID.randomUUID();
        try(var c=db.appConnection()) {
            for(String tabla:List.of("paciente","profesional","sillon","prestacion")) insertar(c,tabla,UUID.randomUUID(),codigo);
            insertar(c,"paciente",UUID.randomUUID(),codigo.toLowerCase(Locale.ROOT));
            insertar(c,"paciente",UUID.randomUUID()," "+codigo+" ");
        }
    }

    private static void insertar(Connection c,String tabla,UUID id,String codigo) throws SQLException {
        if(!List.of("paciente","profesional","sillon","prestacion").contains(tabla))throw new IllegalArgumentException("tabla desconocida");
        if(tabla.equals("paciente")) {
            try(var q=c.prepareStatement("insert into odontologia.paciente(id,codigo,nombre,apellido,email) values (?,?,?,?,?)")) {
                q.setObject(1,id);q.setString(2,codigo);q.setString(3,"Nombre ficticio");q.setString(4,"Apellido ficticio");q.setString(5,"schema@example.invalid");q.executeUpdate();
            }
        } else {
            try(var q=c.prepareStatement("insert into odontologia."+tabla+"(id,codigo) values (?,?)")) {q.setObject(1,id);q.setString(2,codigo);q.executeUpdate();}
        }
    }
}
