package ar.edu.utn.odontologia.acceso;

import ar.edu.utn.odontologia.acceso.persistencia.*;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioCambioContrasena {
    private final Usuarios usuarios;
    private final HistorialContrasenas historial;
    private final ServicioCredenciales credenciales;

    public ServicioCambioContrasena(Usuarios usuarios, HistorialContrasenas historial, ServicioCredenciales credenciales) {
        this.usuarios=usuarios;this.historial=historial;this.credenciales=credenciales;
    }

    /** Cambio propio confirmado por la contraseña vigente; no recibe un usuario destino distinto. */
    @Transactional
    public void cambiarContrasena(Identidad identidad, String actual, String nueva) {
        if(identidad==null || identidad.usuarioId()==null) throw new AutorizacionDenegadaException();
        var usuario=usuarios.buscarParaCambio(identidad.usuarioId()).orElseThrow(AutenticacionFallidaException::new);
        if(!usuario.isActivo() || !credenciales.verificar(actual,usuario.getHashCredencial())) {
            throw new AutenticacionFallidaException();
        }
        var previas=historial.findTop4ByUsuarioIdOrderBySecuenciaDesc(usuario.getId());
        var ultimas=new ArrayList<String>();
        ultimas.add(usuario.getHashCredencial());
        previas.forEach(h->ultimas.add(h.getHashCredencial()));
        credenciales.validarPolitica(nueva,new DatosPersonales(null,usuario.getNombreUsuario(),null),ultimas,5);
        String hashNuevo=credenciales.hash(nueva);
        long secuencia=previas.isEmpty()?1:Math.addExact(previas.getFirst().getSecuencia(),1);
        historial.save(new HistorialContrasena(usuario.getId(),secuencia,usuario.getHashCredencial()));
        usuario.setHashCredencial(hashNuevo);
        usuarios.save(usuario);
    }
}
