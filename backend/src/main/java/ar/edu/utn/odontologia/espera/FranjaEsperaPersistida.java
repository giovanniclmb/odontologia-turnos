package ar.edu.utn.odontologia.espera;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
record FranjaEsperaPersistida(
        @Column(name = "inicio_nanos", nullable = false) long inicioNanos,
        @Column(name = "fin_nanos", nullable = false) long finNanos) {
    FranjaEsperaPersistida(FranjaEspera franja) {
        this(franja.inicio().toNanoOfDay(), franja.fin().toNanoOfDay());
    }

    FranjaEspera valor() {
        return new FranjaEspera(LocalTime.ofNanoOfDay(inicioNanos), LocalTime.ofNanoOfDay(finNanos));
    }
}
