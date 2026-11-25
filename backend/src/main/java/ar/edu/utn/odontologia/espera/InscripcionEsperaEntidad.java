package ar.edu.utn.odontologia.espera;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inscripcion_espera", schema = "odontologia")
class InscripcionEsperaEntidad {
    @Id
    private UUID id;
    @Column(name = "paciente_id", nullable = false, updatable = false)
    private UUID pacienteId;
    @Column(name = "prestacion_id", nullable = false)
    private UUID prestacionId;
    @Column(name = "profesional_id")
    private UUID profesionalId;
    @Column(nullable = false)
    private LocalDate desde;
    @Column(nullable = false)
    private LocalDate hasta;
    @Column(name = "baja_en")
    private Instant bajaEn;
    @ElementCollection
    @CollectionTable(name = "franja_espera", schema = "odontologia", joinColumns = @JoinColumn(name = "inscripcion_id"))
    @OrderColumn(name = "orden", nullable = false)
    private List<FranjaEsperaPersistida> franjas = new ArrayList<>();

    protected InscripcionEsperaEntidad() {}

    InscripcionEsperaEntidad(UUID id, UUID pacienteId, PreferenciasEspera preferencias) {
        this.id = id;
        this.pacienteId = pacienteId;
        actualizar(preferencias);
    }

    void actualizar(PreferenciasEspera preferencias) {
        prestacionId = preferencias.prestacionId();
        profesionalId = preferencias.profesionalId();
        desde = preferencias.desde();
        hasta = preferencias.hasta();
        franjas.clear();
        preferencias.franjas().stream().map(FranjaEsperaPersistida::new).forEach(franjas::add);
    }

    UUID id() {
        return id;
    }

    Instant bajaEn() {
        return bajaEn;
    }

    void marcarBaja(Instant instante) {
        bajaEn = instante;
    }

    VigenciaEspera vigencia(LocalDate hoy) {
        return VigenciaEspera.calcular(hasta, bajaEn, hoy);
    }

    PreferenciasEspera preferencias() {
        return new PreferenciasEspera(prestacionId, desde, hasta,
                franjas.stream().map(FranjaEsperaPersistida::valor).toList(), profesionalId);
    }
}
