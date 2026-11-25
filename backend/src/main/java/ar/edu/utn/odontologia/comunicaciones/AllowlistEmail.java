package ar.edu.utn.odontologia.comunicaciones;

import java.util.List;
import java.util.Locale;

public final class AllowlistEmail {
    private final List<String> permitidos;

    public AllowlistEmail(List<String> permitidos) {
        this.permitidos = permitidos == null ? List.of() : permitidos;
    }

    public boolean permite(String destinatario) {
        if (destinatario == null || destinatario.isBlank()) return false;
        String normalizado = destinatario.trim().toLowerCase(Locale.ROOT);
        for (String p : permitidos) {
            if (p != null && p.trim().toLowerCase(Locale.ROOT).equals(normalizado)) return true;
        }
        return false;
    }

    public List<String> permitidos() { return permitidos; }
}