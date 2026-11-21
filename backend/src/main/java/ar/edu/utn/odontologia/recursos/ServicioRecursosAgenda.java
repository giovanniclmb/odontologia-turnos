package ar.edu.utn.odontologia.recursos;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import ar.edu.utn.odontologia.referencias.persistencia.PrestacionReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.PrestacionReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.ProfesionalReferencias;
import ar.edu.utn.odontologia.referencias.persistencia.SillonReferencia;
import ar.edu.utn.odontologia.referencias.persistencia.SillonReferencias;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioRecursosAgenda {
    private final ProfesionalReferencias profesionales;
    private final SillonReferencias sillones;
    private final ServicioConfiguracionSede sede;
    private final PrestacionReferencias prestaciones;
    private final ServicioAutorizacion autorizacion;

    public ServicioRecursosAgenda(ProfesionalReferencias profesionales, SillonReferencias sillones,
                                 PrestacionReferencias prestaciones, ServicioConfiguracionSede sede,
                                 ServicioAutorizacion autorizacion) {
        this.profesionales = profesionales;
        this.sillones = sillones;
        this.sede = sede;
        this.prestaciones = prestaciones;
        this.autorizacion = autorizacion;
    }

    public Recurso altaProfesional(Identidad solicitante, String codigo, boolean activo) {
        autorizacion.exigir(solicitante, Permiso.CONFIG_RECURSOS);
        validarCodigo(codigo);
        return profesional(profesionales.save(new ProfesionalReferencia(codigo, activo)));
    }

    public List<Recurso> consultarProfesionales(Identidad solicitante) {
        exigirIdentidad(solicitante);
        return profesionales.findAll().stream().map(ServicioRecursosAgenda::profesional).toList();
    }

    public Recurso altaSillon(Identidad solicitante, String codigo, boolean activo) {
        autorizacion.exigir(solicitante, Permiso.CONFIG_RECURSOS);
        validarCodigo(codigo);
        return sillon(sillones.save(new SillonReferencia(codigo, activo)));
    }

    public List<Recurso> consultarSillones(Identidad solicitante) {
        exigirIdentidad(solicitante);
        return sillones.findAll().stream().map(ServicioRecursosAgenda::sillon).toList();
    }

    public int validarDisponibilidadMinima(Identidad solicitante, UUID profesionalId, UUID sillonId) {
        exigirIdentidad(solicitante);
        if (profesionalId == null || profesionales.findById(profesionalId)
                .filter(ProfesionalReferencia::isActivo).isEmpty()) {
            throw new IllegalArgumentException("El profesional debe existir y estar activo");
        }
        if (sillonId == null || sillones.findById(sillonId)
                .filter(SillonReferencia::isActivo).isEmpty()) {
            throw new IllegalArgumentException("El sillón debe existir y estar activo");
        }
        return sede.consultarIntervalo(solicitante)
                .orElseThrow(() -> new IllegalStateException("El intervalo de sede está sin configurar"));
    }

    public Prestacion altaPrestacion(Identidad solicitante, String codigo, Integer minutos) {
        autorizacion.exigir(solicitante, Permiso.CONFIG_RECURSOS);
        validarCodigo(codigo);
        validarDuracion(minutos);
        return prestacion(prestaciones.save(new PrestacionReferencia(codigo, minutos)));
    }

    public List<Prestacion> consultarPrestaciones(Identidad solicitante) {
        exigirIdentidad(solicitante);
        return prestaciones.findAll().stream().map(ServicioRecursosAgenda::prestacion).toList();
    }

    @Transactional
    public Prestacion ajustarDuracion(Identidad solicitante, UUID prestacionId, Integer minutos) {
        autorizacion.exigir(solicitante, Permiso.CONFIG_RECURSOS);
        validarDuracion(minutos);
        if (prestacionId == null) {
            throw new IllegalArgumentException("La referencia de prestación es obligatoria");
        }
        PrestacionReferencia referencia = prestaciones.findById(prestacionId)
                .orElseThrow(() -> new IllegalArgumentException("La prestación no existe"));
        referencia.ajustarDuracion(minutos);
        return prestacion(referencia);
    }

    private static Recurso profesional(ProfesionalReferencia referencia) {
        return new Recurso(referencia.getId(), referencia.getCodigo(), referencia.isActivo());
    }

    private static Recurso sillon(SillonReferencia referencia) {
        return new Recurso(referencia.getId(), referencia.getCodigo(), referencia.isActivo());
    }

    private static Prestacion prestacion(PrestacionReferencia referencia) {
        return new Prestacion(referencia.getId(), referencia.getCodigo(), referencia.getDuracionReferenciaMinutos());
    }

    private static void validarDuracion(Integer minutos) {
        if (minutos == null || minutos <= 0) {
            throw new IllegalArgumentException("La duración de referencia debe ser un entero positivo");
        }
    }

    private static void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
    }

    private static void exigirIdentidad(Identidad solicitante) {
        if (solicitante == null || solicitante.rol() == null) {
            throw new AutorizacionDenegadaException();
        }
    }

    public record Recurso(UUID id, String codigo, boolean activo) {}

    public record Prestacion(UUID id, String codigo, Integer duracionReferenciaMinutos) {}
}
