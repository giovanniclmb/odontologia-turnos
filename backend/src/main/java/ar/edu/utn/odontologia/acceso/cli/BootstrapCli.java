package ar.edu.utn.odontologia.acceso.cli;

import ar.edu.utn.odontologia.OdontologiaApplication;
import ar.edu.utn.odontologia.acceso.DatosBootstrap;
import ar.edu.utn.odontologia.acceso.DatosPersonales;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioBootstrap;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Console;

public final class BootstrapCli {
    private BootstrapCli() {
    }

    public static void main(String[] args) {
        String nombreUsuario = variable("BOOTSTRAP_NOMBRE_USUARIO");
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            nombreUsuario = leer("Nombre de usuario: ");
        }
        String contrasena = variable("BOOTSTRAP_CONTRASENA");
        if (contrasena == null || contrasena.isBlank()) {
            contrasena = leerOculto("Contrasena: ");
        }
        String rolTexto = variable("BOOTSTRAP_ROL");
        if (rolTexto == null || rolTexto.isBlank()) {
            rolTexto = leer("Rol (recepcion/odontologo): ");
        }
        Rol rol = Rol.desdeCodigo(rolTexto.trim());

        try (ConfigurableApplicationContext contexto = new SpringApplicationBuilder(OdontologiaApplication.class)
                .web(WebApplicationType.NONE)
                .run(concatenar(args, "--spring.main.web-application-type=none"))) {
            ServicioBootstrap bootstrap = contexto.getBean(ServicioBootstrap.class);
            DatosBootstrap datos = new DatosBootstrap(nombreUsuario.trim(), contrasena, rol,
                    new DatosPersonales(null, nombreUsuario.trim(), null), null);
            Usuario usuario = bootstrap.bootstrapInicial(datos);
            System.out.println("Bootstrap creado: usuario=" + usuario.getNombreUsuario() + " id=" + usuario.getId()
                    + " rol=" + usuario.getRol().codigo());
        }
    }

    private static String[] concatenar(String[] args, String extra) {
        String[] resultado = new String[(args == null ? 0 : args.length) + 1];
        if (args != null) {
            System.arraycopy(args, 0, resultado, 0, args.length);
        }
        resultado[resultado.length - 1] = extra;
        return resultado;
    }

    private static String variable(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(nombre);
        }
        return valor;
    }

    private static String leer(String prompt) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("No hay consola disponible; use variables de entorno");
        }
        return console.readLine(prompt);
    }

    private static String leerOculto(String prompt) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("No hay consola disponible; use variables de entorno");
        }
        char[] chars = console.readPassword(prompt);
        return chars == null ? "" : new String(chars);
    }
}