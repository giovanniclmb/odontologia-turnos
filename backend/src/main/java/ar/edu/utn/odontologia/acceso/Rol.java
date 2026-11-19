package ar.edu.utn.odontologia.acceso;

public enum Rol {
    RECEPCION("recepcion"),
    ODONTOLOGO("odontologo");

    private final String codigo;

    Rol(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public static Rol desdeCodigo(String codigo) {
        for (Rol rol : values()) {
            if (rol.codigo.equals(codigo)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + codigo);
    }
}
