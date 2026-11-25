package ar.edu.utn.odontologia.agenda;

import java.util.Set;

public enum EstadoTurno {
    PENDIENTE,
    CONFIRMADO,
    CANCELADO;

    public static final Set<EstadoTurno> OCUPANTES = Set.of(PENDIENTE, CONFIRMADO);
}
