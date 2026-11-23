package ar.edu.utn.odontologia.agenda.bloqueos;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.agenda.bloqueos.persistencia.BloqueoPersistido;
import ar.edu.utn.odontologia.agenda.bloqueos.persistencia.BloqueosPersistidos;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioBloqueos {
    private final ServicioAutorizacion autorizacion;
    private final LocksRecursosAgenda locks;
    private final ConsultaTurnosOcupantes turnos;
    private final BloqueosPersistidos bloqueos;

    public ServicioBloqueos(ServicioAutorizacion autorizacion, LocksRecursosAgenda locks,
                           ConsultaTurnosOcupantes turnos, BloqueosPersistidos bloqueos) {
        this.autorizacion = autorizacion;
        this.locks = locks;
        this.turnos = turnos;
        this.bloqueos = bloqueos;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Bloqueo crear(Identidad solicitante, RecursosBloqueo recursos, PeriodoBloqueo periodo) {
        autorizacion.exigir(solicitante, Permiso.BLOQUEO_CREAR);
        if (recursos == null || periodo == null) {
            throw new IllegalArgumentException("Recursos y período son obligatorios");
        }
        locks.adquirir(List.of(recursos));
        if (turnos.existeConflicto(recursos, periodo)) {
            throw new TurnoOcupanteException();
        }
        return bloqueos.save(new BloqueoPersistido(recursos, periodo)).aBloqueo();
    }
}
