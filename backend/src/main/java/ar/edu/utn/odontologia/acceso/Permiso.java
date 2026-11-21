package ar.edu.utn.odontologia.acceso;

public enum Permiso {
    TURNO_CREAR("turno:crear"),
    TURNO_REPROGRAMAR("turno:reprogramar"),
    TURNO_CANCELAR("turno:cancelar"),
    AGENDA_SILLON_CONSULTAR("agenda-sillon:consultar"),
    BLOQUEO_CREAR("bloqueo:crear"),
    FICHA_LEER("ficha:leer"),
    FICHA_EDITAR("ficha:editar"),
    CONFIRMACION_REGISTRAR("confirmacion:registrar"),
    PENDIENTES_CONSULTAR("pendientes:consultar"),
    PACIENTE_DATOS_ADMIN("paciente:datos-admin"),
    CONFIG_INTERVALO("config:intervalo"),
    CONFIG_RECURSOS("config:recursos"),
    USUARIO_CREAR_ASIGNAR("usuario:crear/asignar"),
    PACIENTE_ELIMINAR("paciente:eliminar");

    private final String codigo;

    Permiso(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public static Permiso desdeCodigo(String codigo) {
        for (Permiso permiso : values()) {
            if (permiso.codigo.equals(codigo)) {
                return permiso;
            }
        }
        throw new IllegalArgumentException("Permiso desconocido: " + codigo);
    }
}
