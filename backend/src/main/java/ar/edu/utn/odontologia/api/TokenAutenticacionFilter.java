package ar.edu.utn.odontologia.api;

import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.ServicioAutenticacion;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenAutenticacionFilter extends OncePerRequestFilter {
    public static final String ATRIBUTO_IDENTIDAD = "identidad";
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
            } catch (RuntimeException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"codigo\":\"no_autenticado\",\"mensaje\":\"Token invalido o ausente\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}