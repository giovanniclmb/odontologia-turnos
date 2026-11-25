package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.AutenticacionFallidaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenAutenticacionFilter extends OncePerRequestFilter {
    public static final String ATRIBUTO_IDENTIDAD = "identidad";
    private static final Logger LOG = LoggerFactory.getLogger(TokenAutenticacionFilter.class);
    private final ServicioAutenticacion autenticacion;

    public TokenAutenticacionFilter(ServicioAutenticacion autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring("Bearer ".length()).trim();
            try {
                Identidad identidad = autenticacion.resolverToken(token);
                request.setAttribute(ATRIBUTO_IDENTIDAD, identidad);
            } catch (AutenticacionFallidaException ex) {
                responder(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "{\"codigo\":\"no_autenticado\",\"mensaje\":\"Token invalido o ausente\"}");
                return;
            } catch (RuntimeException ex) {
                responderFallaInterna(response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /** El detalle queda solo en el registro; la respuesta lleva el id que permite encontrarlo. */
    private static void responderFallaInterna(HttpServletResponse response, RuntimeException falla) throws IOException {
        String correlacion = UUID.randomUUID().toString();
        LOG.error("Falla interna al resolver el token, correlacion {}", correlacion, falla);
        responder(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "{\"codigo\":\"error_interno\",\"mensaje\":\"No se pudo procesar la solicitud\",\"correlacion\":\""
                        + correlacion + "\"}");
    }

    private static void responder(HttpServletResponse response, int estado, String cuerpo) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.getWriter().write(cuerpo);
    }
}
