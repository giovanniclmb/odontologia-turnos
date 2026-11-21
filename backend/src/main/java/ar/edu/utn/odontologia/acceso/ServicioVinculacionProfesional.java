package ar.edu.utn.odontologia.acceso;

import ar.edu.utn.odontologia.acceso.ContratoVinculacionUsuarioProfesional.Vinculacion;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencias;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioVinculacionProfesional {
    private final Usuarios usuarios;
    private final ProfesionalReferencias profesionales;
    private final ServicioAutorizacion autorizacion;

    public ServicioVinculacionProfesional(Usuarios usuarios, ProfesionalReferencias profesionales,
                                          ServicioAutorizacion autorizacion) {
        this.usuarios = usuarios;
        this.profesionales = profesionales;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public void vincular(Identidad solicitante, Vinculacion datos) {
        ContratoVinculacionUsuarioProfesional.validarAsignacion(solicitante, autorizacion);
        if (datos == null || datos.usuarioId() == null || datos.profesionalRef() == null) {
            throw new IllegalArgumentException("El usuario y el profesional son obligatorios");
        }
        if (profesionales.findById(datos.profesionalRef()).isEmpty()) {
            throw new IllegalArgumentException("El profesional no existe");
        }
        // Usar el mismo bloqueo que el cambio de contraseña para preservar cambios concurrentes del usuario.
        Usuario usuario = usuarios.buscarParaCambio(datos.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
        usuario.setProfesionalRef(datos.profesionalRef());
    }
}
