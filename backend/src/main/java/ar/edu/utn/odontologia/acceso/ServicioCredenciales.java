package ar.edu.utn.odontologia.acceso;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ServicioCredenciales {
    public static final int COSTO_BCRYPT = 12;
    public static final int N_REUTILIZACION_DEFAULT = 5;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(COSTO_BCRYPT);

    public String hash(String contrasena) {
        if (contrasena == null) {
            throw new IllegalArgumentException("contrasena nula");
        }
        return encoder.encode(contrasena);
    }

    public boolean verificar(String contrasena, String hash) {
        if (contrasena == null || hash == null) {
            return false;
        }
        return encoder.matches(contrasena, hash);
    }

    public void validarPolitica(String contrasena, DatosPersonales datos) {
        validarPolitica(contrasena, datos, List.of());
    }

    public void validarPolitica(String contrasena, DatosPersonales datos, List<String> hashesAnteriores) {
        validarPolitica(contrasena, datos, hashesAnteriores, N_REUTILIZACION_DEFAULT);
    }

    public void validarPolitica(String contrasena, DatosPersonales datos, List<String> hashesAnteriores, int n) {
        if (contrasena == null || contrasena.length() < 12) {
            throw new PoliticaContrasenaException("La contrasena debe tener al menos 12 caracteres");
        }
        boolean mayuscula = contrasena.chars().anyMatch(Character::isUpperCase);
        boolean minuscula = contrasena.chars().anyMatch(Character::isLowerCase);
        boolean numero = contrasena.chars().anyMatch(Character::isDigit);
        if (!mayuscula || !minuscula || !numero) {
            throw new PoliticaContrasenaException("La contrasena debe incluir mayuscula, minuscula y numero");
        }
        if (datos != null) {
            String baja = contrasena.toLowerCase();
            for (String dato : new String[]{datos.nombre(), datos.nombreUsuario(), datos.email()}) {
                if (dato != null && !dato.isBlank()) {
                    String personal = dato.trim().toLowerCase();
                    if (personal.length() >= 3 && baja.contains(personal)) {
                        throw new PoliticaContrasenaException("La contrasena no debe contener datos personales");
                    }
                }
            }
        }
        if (hashesAnteriores != null && n > 0) {
            int limite = Math.min(n, hashesAnteriores.size());
            for (int i = 0; i < limite; i++) {
                String hashAnterior = hashesAnteriores.get(hashesAnteriores.size() - 1 - i);
                if (hashAnterior != null && encoder.matches(contrasena, hashAnterior)) {
                    throw new PoliticaContrasenaException("La contrasena no puede reutilizar las ultimas " + n);
                }
            }
        }
    }

    public int costo() {
        return COSTO_BCRYPT;
    }
}
