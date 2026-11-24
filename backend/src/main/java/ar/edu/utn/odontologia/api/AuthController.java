package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Rol;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    public record LoginRequest(String nombreUsuario, String contrasena) {}
    public record LoginResponse(String token, UUID usuarioId, String nombreUsuario, Rol rol, UUID profesionalRef) {}

    private final ServicioAutenticacion autenticacion;

    public AuthController(ServicioAutenticacion autenticacion) {
        this.autenticacion = autenticacion;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Identidad identidad = autenticacion.autenticar(
                request == null ? null : request.nombreUsuario(),
                request == null ? null : request.contrasena());
        String token = autenticacion.emitirToken(identidad);
        return ResponseEntity.ok(new LoginResponse(token, identidad.usuarioId(), identidad.nombreUsuario(),
                identidad.rol(), identidad.profesionalRef()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        Object attr = request.getAttribute(TokenAutenticacionFilter.ATRIBUTO_IDENTIDAD);
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            autenticacion.revocar(header.substring("Bearer ".length()).trim());
        }
        return ResponseEntity.noContent().build();
    }
}