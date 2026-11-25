package ar.edu.utn.odontologia.espera;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

interface InscripcionesEspera extends Repository<InscripcionEsperaEntidad, UUID> {
    InscripcionEsperaEntidad save(InscripcionEsperaEntidad inscripcion);

    @EntityGraph(attributePaths = "franjas")
    Optional<InscripcionEsperaEntidad> findByIdAndPacienteId(UUID id, UUID pacienteId);

    @EntityGraph(attributePaths = "franjas")
    List<InscripcionEsperaEntidad> findAllByPacienteIdOrderById(UUID pacienteId);

    boolean existsByPacienteIdAndPrestacionIdAndBajaEnIsNullAndHastaGreaterThanEqualAndIdNot(
            UUID pacienteId, UUID prestacionId, LocalDate hoy, UUID id);
}
