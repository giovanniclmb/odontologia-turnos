package ar.edu.utn.odontologia.persistencia;

import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import ar.edu.utn.odontologia.pacientes.ServicioPacientes;
import ar.edu.utn.odontologia.recursos.ServicioRecursosAgenda;
import java.util.concurrent.CountDownLatch;

/**
 * Solo pruebas: API en el puerto 8080 sobre PostgreSQL desechable, con los recursos ficticios que la interfaz de
 * agenda necesita y no permite crear. El intervalo queda sin configurar para recorrer también ese estado.
 */
public final class ApiC10Demo {
    private ApiC10Demo() {}

    public static void main(String[] args) throws InterruptedException {
        var fixture = new ApiC11Fixture(8080);
        Runtime.getRuntime().addShutdownHook(new Thread(fixture::close));
        var recepcion = fixture.context.getBean(ServicioAutenticacion.class)
                .autenticar("recepcion-demo", ApiC11Fixture.CLAVE);
        var recursos = fixture.context.getBean(ServicioRecursosAgenda.class);
        recursos.altaSillon(recepcion, "SIL-01", true);
        recursos.altaSillon(recepcion, "SIL-02", true);
        recursos.altaPrestacion(recepcion, "CONTROL", 30);
        recursos.altaPrestacion(recepcion, "TRATAMIENTO", 60);
        fixture.context.getBean(ServicioPacientes.class).crear(recepcion, new ServicioPacientes.CrearPaciente(
                "PAC-DEMO-2", "Bruno", "Muestra", "bruno@example.invalid", null));
        System.out.println("C10_DEMO_URL=" + fixture.base);
        new CountDownLatch(1).await();
    }
}
