package ar.edu.utn.odontologia.comunicaciones;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class ClaveIdempotencia {
    private ClaveIdempotencia() {}

    public static String generar(String tipo, UUID turnoId, String destinatario) {
        String base = (tipo == null ? "" : tipo.trim())
                + "|" + (turnoId == null ? "" : turnoId)
                + "|" + (destinatario == null ? "" : destinatario.trim().toLowerCase());
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}