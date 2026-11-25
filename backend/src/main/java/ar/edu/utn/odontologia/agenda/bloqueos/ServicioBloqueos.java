package ar.edu.utn.odontologia.agenda.bloqueos;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.agenda.bloqueos.persistencia.BloqueoPersistido;
import ar.edu.utn.odontologia.agenda.bloqueos.persistencia.BloqueosPersistidos;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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

    @Transactional(readOnly = true)
    public List<BloqueoVisible> consultarVisibles(Identidad solicitante, UUID sillonId, Instant inicio, Instant fin) {
        autorizacion.exigir(solicitante, Permiso.BLOQUEO_CONSULTAR);
        if (sillonId == null) {
            throw new IllegalArgumentException("La referencia del sillón es obligatoria");
        }
        var periodo = new PeriodoBloqueo(inicio, fin);
        if (solicitante.rol() == Rol.RECEPCION) {
            return bloqueos.buscarPeriodo(periodo.inicio(), periodo.fin()).stream()
                    .map(BloqueoPersistido::aBloqueo)
                    .map(bloqueo -> visible(bloqueo, bloqueo.recursos().sillonId(), bloqueo.recursos().profesionalId()))
                    .toList();
        }
        var propio = solicitante.profesionalRef();
        return bloqueos.buscar(propio, sillonId, periodo.inicio(), periodo.fin()).stream()
                .map(BloqueoPersistido::aBloqueo)
                // Odontología no recibe referencias de otros profesionales ni de otros sillones.
                .map(bloqueo -> visible(bloqueo,
                        sillonId.equals(bloqueo.recursos().sillonId()) ? sillonId : null,
                        propio != null && propio.equals(bloqueo.recursos().profesionalId()) ? propio : null))
                .toList();
    }

    private static BloqueoVisible visible(Bloqueo bloqueo, UUID sillonId, UUID profesionalId) {
        return new BloqueoVisible(bloqueo.id(), bloqueo.periodo().inicio(), bloqueo.periodo().fin(),
                sillonId, profesionalId);
    }

    public record BloqueoVisible(UUID id, Instant inicio, Instant fin, UUID sillonId, UUID profesionalId) {}
}
