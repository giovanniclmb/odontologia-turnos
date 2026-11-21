package ar.edu.utn.odontologia.acceso;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ServicioAutorizacion {
    private static final Map<Rol, Set<Permiso>> PERMISOS = Map.of(
            Rol.RECEPCION, EnumSet.of(
                    Permiso.TURNO_CREAR,
                    Permiso.TURNO_REPROGRAMAR,
                    Permiso.TURNO_CANCELAR,
                    Permiso.AGENDA_SILLON_CONSULTAR,
                    Permiso.BLOQUEO_CREAR,
                    Permiso.FICHA_LEER,
                    Permiso.CONFIRMACION_REGISTRAR,
                    Permiso.PENDIENTES_CONSULTAR,
                    Permiso.CONFIG_INTERVALO,
                    Permiso.CONFIG_RECURSOS,
                    Permiso.USUARIO_CREAR_ASIGNAR),
            Rol.ODONTOLOGO, EnumSet.of(
                    Permiso.TURNO_CREAR,
                    Permiso.TURNO_REPROGRAMAR,
                    Permiso.TURNO_CANCELAR,
                    Permiso.AGENDA_SILLON_CONSULTAR,
                    Permiso.FICHA_LEER,
                    Permiso.FICHA_EDITAR,
                    Permiso.CONFIRMACION_REGISTRAR,
                    Permiso.PENDIENTES_CONSULTAR));

    public boolean tienePermiso(Identidad identidad, Permiso permiso) {
        if (identidad == null || permiso == null) {
            return false;
        }
        Set<Permiso> permisos = PERMISOS.get(identidad.rol());
        return permisos != null && permisos.contains(permiso);
    }

    public void exigir(Identidad identidad, Permiso permiso) {
        if (!tienePermiso(identidad, permiso)) {
            throw new AutorizacionDenegadaException();
        }
    }

    public boolean puedeConsultarPaciente(Identidad identidad, UUID profesionalRefPaciente) {
        if (identidad == null) {
            return false;
        }
        if (identidad.rol() == Rol.RECEPCION) {
            return true;
        }
        if (identidad.rol() == Rol.ODONTOLOGO) {
            return identidad.profesionalRef() != null
                    && identidad.profesionalRef().equals(profesionalRefPaciente);
        }
        return false;
    }

    public boolean puedeConsultarTurno(Identidad identidad, UUID profesionalRefTurno) {
        return puedeConsultarPaciente(identidad, profesionalRefTurno);
    }

    public Set<Permiso> permisosDe(Rol rol) {
        Set<Permiso> permisos = PERMISOS.get(rol);
        return permisos == null ? Set.of() : Set.copyOf(permisos);
    }
}
