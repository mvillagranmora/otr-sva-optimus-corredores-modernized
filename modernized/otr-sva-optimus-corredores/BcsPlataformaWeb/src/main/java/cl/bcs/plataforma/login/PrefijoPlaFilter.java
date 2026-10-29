package cl.bcs.plataforma.login;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@code /PLA} prefix of the Plataforma API (T-10). The gateway rewrites {@code /BSCL/Plataforma/GWJAVA/PLA/**} to {@code /PLA/**} (T-01) and this
 * module answers there: {@code /PLA/CrearSesion}, {@code /PLA/bootstrap/login}, {@code /PLA/call}... A request under {@code /PLA/} is dispatched as the
 * same path without the prefix, so every controller keeps its own mapping and the paths that must stay where jobs and health checks expect them
 * ({@code /internal/**}, {@code /actuator/**}, the root paths already in use) do not move. Runs before every other filter.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrefijoPlaFilter extends OncePerRequestFilter {

    static final String PREFIJO = "/PLA";

    /** Request attribute with the path as the client sent it (with the prefix): the session filter applies to what came through the gateway only. */
    public static final String ATRIBUTO_RUTA_PLA = "plataforma.rutaPla";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contexto = request.getContextPath();
        String ruta = uri.substring(contexto.length());
        if (ruta.equals(PREFIJO) || ruta.startsWith(PREFIJO + "/")) {
            String sinPrefijo = ruta.length() == PREFIJO.length() ? "/" : ruta.substring(PREFIJO.length());
            request.setAttribute(ATRIBUTO_RUTA_PLA, ruta);
            chain.doFilter(new SinPrefijo(request, contexto + sinPrefijo, sinPrefijo), response);
            return;
        }
        chain.doFilter(request, response);
    }

    private static final class SinPrefijo extends HttpServletRequestWrapper {
        private final String uri;
        private final String servletPath;

        SinPrefijo(HttpServletRequest request, String uri, String servletPath) {
            super(request);
            this.uri = uri;
            this.servletPath = servletPath;
        }

        @Override
        public String getRequestURI() {
            return uri;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer(super.getRequestURL());
            int i = url.indexOf(super.getRequestURI());
            return i < 0 ? url : new StringBuffer(url.substring(0, i)).append(uri);
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }
    }
}
