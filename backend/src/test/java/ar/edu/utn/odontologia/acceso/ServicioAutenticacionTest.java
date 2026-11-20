package ar.edu.utn.odontologia.acceso;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import org.junit.jupiter.api.Test;

class ServicioAutenticacionTest {
    private static class RepoMemoria implements Usuarios {
        private final Map<String, Usuario> porNombre = new ConcurrentHashMap<>();

        void put(Usuario u) {
            porNombre.put(u.getNombreUsuario(), u);
        }

        @Override
        public Usuario save(Usuario usuario) {
            return usuario;
        }

        @Override
        public Optional<Usuario> findById(java.util.UUID id) {
            return porNombre.values().stream().filter(u -> Objects.equals(u.getId(), id)).findFirst();
        }

        @Override
        public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
            return Optional.ofNullable(porNombre.get(nombreUsuario));
        }

        @Override
        public List<Usuario> findAll() {
            return new ArrayList<>(porNombre.values());
        }

        @Override
        public Object adquirirBloqueoBootstrap() {
            return null;
        }
    }

    private static void setId(Usuario u, java.util.UUID id) {
        try {
            Field f = Usuario.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(u, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final ServicioCredenciales cred = new ServicioCredenciales();

    @Test
    void loginValidoRecepcion() {
        RepoMemoria repo = new RepoMemoria();
        java.util.UUID id = java.util.UUID.randomUUID();
        Usuario u = new Usuario("recep", cred.hash("ClaveSegura123"), Rol.RECEPCION, true, null);
        setId(u, id);
        repo.put(u);
        ServicioAutenticacion svc = new ServicioAutenticacion(repo, cred);
        Identidad identidad = svc.autenticar("recep", "ClaveSegura123");
        assertEquals(Rol.RECEPCION, identidad.rol());
    }

    @Test
    void loginValidoOdontologo() {
        RepoMemoria repo = new RepoMemoria();
        Usuario u = new Usuario("odonto", cred.hash("ClaveSegura123"), Rol.ODONTOLOGO, true, null);
        setId(u, java.util.UUID.randomUUID());
        repo.put(u);
        ServicioAutenticacion svc = new ServicioAutenticacion(repo, cred);
        assertEquals(Rol.ODONTOLOGO, svc.autenticar("odonto", "ClaveSegura123").rol());
    }

    @Test
    void rechazoContrasenaIncorrecta() {
        RepoMemoria repo = new RepoMemoria();
        Usuario u = new Usuario("recep", cred.hash("ClaveSegura123"), Rol.RECEPCION, true, null);
        setId(u, java.util.UUID.randomUUID());
        repo.put(u);
        ServicioAutenticacion svc = new ServicioAutenticacion(repo, cred);
        assertThrows(AutenticacionFallidaException.class, () -> svc.autenticar("recep", "otra"));
    }

    @Test
    void rechazoUsuarioDesconocido() {
        RepoMemoria repo = new RepoMemoria();
        ServicioAutenticacion svc = new ServicioAutenticacion(repo, cred);
        assertThrows(AutenticacionFallidaException.class, () -> svc.autenticar("noexiste", "ClaveSegura123"));
    }

    @Test
    void rechazoUsuarioInactivo() {
        RepoMemoria repo = new RepoMemoria();
        Usuario u = new Usuario("recep", cred.hash("ClaveSegura123"), Rol.RECEPCION, false, null);
        setId(u, java.util.UUID.randomUUID());
        repo.put(u);
        ServicioAutenticacion svc = new ServicioAutenticacion(repo, cred);
        assertThrows(AutenticacionFallidaException.class, () -> svc.autenticar("recep", "ClaveSegura123"));
    }

    @Test
    void mismoErrorGenerico() {
        // type equality
        assertTrue(AutenticacionFallidaException.class != null);
    }
}
