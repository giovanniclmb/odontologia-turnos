package ar.edu.utn.odontologia.acceso;

import ar.edu.utn.odontologia.acceso.persistencia.TokenAcceso;
import ar.edu.utn.odontologia.acceso.persistencia.TokensAcceso;
import ar.edu.utn.odontologia.acceso.persistencia.Usuario;
import ar.edu.utn.odontologia.acceso.persistencia.Usuarios;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServicioAutenticacion {
    private final Usuarios usuarios;
    private final ServicioCredenciales credenciales;
    private final TokensAcceso tokens;
    private final Clock reloj;
    private final int expiracionHoras;

    public ServicioAutenticacion(Usuarios usuarios, ServicioCredenciales credenciales) {
        this(usuarios, credenciales, null, Clock.systemUTC(), 24);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ServicioAutenticacion(Usuarios usuarios, ServicioCredenciales credenciales, TokensAcceso tokens, Clock reloj,
                                @Value("${app.token.expiracion-horas:24}") int expiracionHoras) {
        this.usuarios = usuarios;
        this.credenciales = credenciales;
        this.tokens = tokens;
        this.reloj = reloj == null ? Clock.systemUTC() : reloj;
        this.expiracionHoras = expiracionHoras <= 0 ? 24 : expiracionHoras;
    }

    public Identidad autenticar(String nombreUsuario, String contrasena) {
        Usuario usuario = (nombreUsuario == null) ? null : usuarios.findByNombreUsuario(nombreUsuario).orElse(null);
        if (usuario == null || !usuario.isActivo()) {
            throw new AutenticacionFallidaException();
        }
        if (!credenciales.verificar(contrasena, usuario.getHashCredencial())) {
            throw new AutenticacionFallidaException();
        }
        return new Identidad(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol(), usuario.getProfesionalRef());
    }

    public String emitirToken(Identidad identidad) {
        if (identidad == null || identidad.usuarioId() == null || tokens == null) {
            throw new AutenticacionFallidaException();
        }
        String claro = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        Instant expira = reloj.instant().plus(expiracionHoras, ChronoUnit.HOURS);
        tokens.save(new TokenAcceso(identidad.usuarioId(), hashToken(claro), expira));
        return claro;
    }

    public Identidad resolverToken(String tokenClaro) {
        if (tokenClaro == null || tokenClaro.isBlank() || tokens == null) {
            throw new AutenticacionFallidaException();
        }
        TokenAcceso token = tokens.findByHashToken(hashToken(tokenClaro))
                .orElseThrow(AutenticacionFallidaException::new);
        if (token.isRevocado() || !token.getExpiraEn().isAfter(reloj.instant())) {
            throw new AutenticacionFallidaException();
        }
        Usuario usuario = usuarios.findById(token.getUsuarioId()).orElseThrow(AutenticacionFallidaException::new);
        if (!usuario.isActivo()) {
            throw new AutenticacionFallidaException();
        }
        return new Identidad(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol(), usuario.getProfesionalRef());
    }

    public void revocar(String tokenClaro) {
        if (tokenClaro == null || tokenClaro.isBlank() || tokens == null) {
            return;
        }
        tokens.findByHashToken(hashToken(tokenClaro)).ifPresent(token -> {
            token.setRevocado(true);
            tokens.save(token);
        });
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}