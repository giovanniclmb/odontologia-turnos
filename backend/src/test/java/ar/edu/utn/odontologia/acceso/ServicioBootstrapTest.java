package ar.edu.utn.odontologia.acceso;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import org.junit.jupiter.api.Test;

class ServicioBootstrapTest {
    private static class RepoMemoria implements Usuarios {
        private final List<Usuario> usuarios = new ArrayList<>();

        @Override
        public Usuario save(Usuario usuario) {
            usuarios.add(usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> findById(java.util.UUID id) {
            return usuarios.stream().filter(u -> Objects.equals(u.getId(), id)).findFirst();
        }

        @Override
        public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
            return usuarios.stream().filter(u -> Objects.equals(u.getNombreUsuario(), nombreUsuario)).findFirst();
        }

        @Override
        public List<Usuario> findAll() {
            return new ArrayList<>(usuarios);
        }

        @Override
        public Object adquirirBloqueoBootstrap() {
            return null;
        }
    }

    private final ServicioCredenciales cred = new ServicioCredenciales();

    @Test
    void bootstrapInicialExitoso() {
        RepoMemoria repo = new RepoMemoria();
        ServicioBootstrap svc = new ServicioBootstrap(repo, cred);
        DatosBootstrap datos = new DatosBootstrap("admin", "ClaveSegura123", Rol.RECEPCION, null, null);
        Usuario u = svc.bootstrapInicial(datos);
        assertEquals("admin", u.getNombreUsuario());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void segundoBootstrapDenegado() {
        RepoMemoria repo = new RepoMemoria();
        ServicioBootstrap svc = new ServicioBootstrap(repo, cred);
        svc.bootstrapInicial(new DatosBootstrap("admin", "ClaveSegura123", Rol.RECEPCION, null, null));
        assertThrows(IllegalStateException.class, () -> svc.bootstrapInicial(new DatosBootstrap("admin2", "ClaveSegura123", Rol.RECEPCION, null, null)));
    }

    @Test
    void creacionSinPermisoDenegada() {
        RepoMemoria repo = new RepoMemoria();
        ServicioBootstrap svc = new ServicioBootstrap(repo, cred);
        svc.bootstrapInicial(new DatosBootstrap("admin", "ClaveSegura123", Rol.ODONTOLOGO, null, null));
        // solicitante sin permiso
        Identidad sinPermiso = new Identidad(java.util.UUID.randomUUID(), "x", Rol.ODONTOLOGO, null);
        assertThrows(AutorizacionDenegadaException.class, () -> svc.crearUsuario(sinPermiso, new DatosBootstrap("nuevo", "ClaveSegura123", Rol.RECEPCION, null, null)));
    }


}
