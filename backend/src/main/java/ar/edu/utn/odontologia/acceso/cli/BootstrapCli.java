package ar.edu.utn.odontologia.acceso.cli;

import ar.edu.utn.odontologia.acceso.Rol;
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
        // El ensamblado real de repositorios/JPA se realiza en C-06/C-10; aqui solo se documenta el flujo.
        System.out.println("Bootstrap solicitado para usuario=" + nombreUsuario + " rol=" + rol.codigo());
        System.out.println("Requiere repositorios JPA inicializados por la aplicacion; sin transporte HTTP.");
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
