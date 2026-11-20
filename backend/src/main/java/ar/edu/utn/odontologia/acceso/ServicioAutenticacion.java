package ar.edu.utn.odontologia.acceso;

import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import org.springframework.stereotype.Service;

@Service
public class ServicioAutenticacion {
    private final Usuarios usuarios;
    private final ServicioCredenciales credenciales;

    public ServicioAutenticacion(Usuarios usuarios, ServicioCredenciales credenciales) {
        this.usuarios = usuarios;
        this.credenciales = credenciales;
    }

    public Identidad autenticar(String nombreUsuario, String contrasena) {
        Usuario usuario = (nombreUsuario == null) ? null : usuarios.findByNombreUsuario(nombreUsuario).orElse(null);
        if (usuario == null || !usuario.isActivo()) {
            throw new AutenticacionFallidaException();
        }
        if (!credenciales.verificar(contrasena, usuario.getHashCredencial())) {
            throw new AutenticacionFallidaException();
        }
        return new Identidad(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol(), usuario.getProfesionalRef());
    }
}
