package ar.edu.utn.odontologia.acceso;

import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioBootstrap {
    private final Usuarios usuarios;
    private final ServicioCredenciales credenciales;

    public ServicioBootstrap(Usuarios usuarios, ServicioCredenciales credenciales) {
        this.usuarios = usuarios;
        this.credenciales = credenciales;
    }

    @Transactional
    public Usuario bootstrapInicial(DatosBootstrap datos) {
        if (datos == null) {
            throw new IllegalArgumentException("datos nulos");
        }
        // Serializa check+insert entre procesos/conexiones; @Transactional solo no resuelve la carrera.
        usuarios.adquirirBloqueoBootstrap();
        if (!usuarios.findAll().isEmpty()) {
            throw new IllegalStateException("Bootstrap ya ejecutado");
        }
        DatosPersonales personales = datos.datosPersonales() == null
                ? new DatosPersonales(null, datos.nombreUsuario(), null)
                : datos.datosPersonales();
        credenciales.validarPolitica(datos.contrasena(), personales);
        String hash = credenciales.hash(datos.contrasena());
        Usuario usuario = new Usuario(datos.nombreUsuario(), hash, datos.rol(), true, datos.profesionalRef());
        return usuarios.save(usuario);
    }

    public Usuario crearUsuario(Identidad solicitante, DatosBootstrap datos) {
        if (datos == null) {
            throw new IllegalArgumentException("datos nulos");
        }
        new ServicioAutorizacion().exigir(solicitante, Permiso.USUARIO_CREAR_ASIGNAR);
        DatosPersonales personales = datos.datosPersonales() == null
                ? new DatosPersonales(null, datos.nombreUsuario(), null)
                : datos.datosPersonales();
        credenciales.validarPolitica(datos.contrasena(), personales);
        String hash = credenciales.hash(datos.contrasena());
        Usuario usuario = new Usuario(datos.nombreUsuario(), hash, datos.rol(), true, datos.profesionalRef());
        return usuarios.save(usuario);
    }
}
