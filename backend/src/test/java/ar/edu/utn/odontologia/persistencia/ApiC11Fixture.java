package ar.edu.utn.odontologia.persistencia;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.*;
import ar.edu.utn.odontologia.pacientes.*;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

/** Solo pruebas: API real sobre PostgreSQL desechable y datos totalmente ficticios. */
public final class ApiC11Fixture implements AutoCloseable {
    public static final String CLAVE = "PruebaSegura986!";
    private final PostgresFixture db;
    final ConfigurableApplicationContext context;
    final UUID profesional;
    final UUID paciente;
    final UUID notaAjena;
    final String base;

    public ApiC11Fixture() {
        this(0);
    }

    ApiC11Fixture(int puerto) {
        db = PostgresFixture.start();
        ConfigurableApplicationContext started = null;
        try {
            var properties = new HashMap<String, Object>(db.properties());
            properties.put("server.address", "127.0.0.1");
            properties.put("server.port", Integer.toString(puerto));
            properties.put("spring.main.web-application-type", "servlet");
            var env = new StandardEnvironment();
            env.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            env.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            env.getPropertySources().addFirst(new MapPropertySource("c11Temporal", properties));
            started = new SpringApplicationBuilder(OdontologiaApplication.class)
                    .web(WebApplicationType.SERVLET).environment(env)
                    .run("--spring.config.location=classpath:/application.yaml");
            context = started;
            base = "http://127.0.0.1:" + context.getEnvironment().getProperty("local.server.port");
            var bootstrap = context.getBean(ServicioBootstrap.class);
            var auth = context.getBean(ServicioAutenticacion.class);
            bootstrap.bootstrapInicial(new DatosBootstrap("recepcion-demo", CLAVE, Rol.RECEPCION, null, null));
            var recepcion = auth.autenticar("recepcion-demo", CLAVE);
            var recursos = context.getBean(ServicioRecursosAgenda.class);
            profesional = recursos.altaProfesional(recepcion, "PRO-DEMO-1", true).id();
            var segundo = recursos.altaProfesional(recepcion, "PRO-DEMO-2", true).id();
            bootstrap.crearUsuario(recepcion, new DatosBootstrap("odonto-demo", CLAVE, Rol.ODONTOLOGO, null, profesional));
            bootstrap.crearUsuario(recepcion, new DatosBootstrap("colega-demo", CLAVE, Rol.ODONTOLOGO, null, segundo));
            var pacientes = context.getBean(ServicioPacientes.class);
            paciente = pacientes.crear(recepcion, new ServicioPacientes.CrearPaciente(
                    "PAC-DEMO-1", "Ana", "Ejemplo", "ana@example.invalid", "555-0100")).id();
            var asignaciones = context.getBean(ServicioAsignacionPacientes.class);
            asignaciones.asignar(recepcion, paciente, profesional);
            asignaciones.asignar(recepcion, paciente, segundo);
            var clinica = context.getBean(ServicioFichaClinica.class);
            var odonto = auth.autenticar("odonto-demo", CLAVE);
            clinica.actualizarAntecedentes(odonto, paciente, "Antecedentes ficticios para pruebas.");
            clinica.agregarNota(odonto, paciente, new ServicioFichaClinica.CrearNotaClinica("Nota propia ficticia."));
            notaAjena = clinica.agregarNota(auth.autenticar("colega-demo", CLAVE), paciente,
                    new ServicioFichaClinica.CrearNotaClinica("Nota de otro profesional ficticio.")).id();
        } catch (RuntimeException | Error failure) {
            if (started != null) started.close();
            db.close();
            throw failure;
        }
    }

    @Override public void close() { try { context.close(); } finally { db.close(); } }

    /** Mantiene únicamente el fixture temporal vivo para verificar la UI; Ctrl-C lo cierra. */
    public static void main(String[] args) throws InterruptedException {
        var fixture = new ApiC11Fixture();
        Runtime.getRuntime().addShutdownHook(new Thread(fixture::close));
        System.out.println("C11_DEMO_URL=" + fixture.base);
        System.out.println("C11_DEMO_PACIENTE=" + fixture.paciente);
        new CountDownLatch(1).await();
    }
}
