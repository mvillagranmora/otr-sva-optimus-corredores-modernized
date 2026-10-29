package cl.bcs.plataforma.login.sesion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import cl.bcs.plataforma.login.PrefijoPlaFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Session filter of the Plataforma API (T-11), equivalent to legacy {@code SesionFilter.java:43-118} over {@code ACC.Sesion}. Closed by default (decision
 * of 2026-10-26, not in the legacy, which only covered {@code /PLA/}): it applies to every path except {@code /internal/**} (own token), the actuator and
 * {@code /error}; a path without the {@code /PLA} prefix is judged as if it had it:
 * <ul>
 *   <li>exempt (exact path, as the legacy list): login, {@code bootstrap/login}, password validation / update / recovery, SSO, ping, configurations,
 *       {@code CallBatch}, {@code AuditoriaWS}, {@code WebStatic}...;</li>
 *   <li>everything else, {@code bootstrap/home} included, needs the header {@code X-SESSION-TOKEN}: 400 when it is missing, 401 when no session
 *       exists with that token or it is no longer valid ({@code TokenValido <> 'S'}), 401 also when the check itself fails (as the legacy), 500 when
 *       there is no connection.</li>
 * </ul>
 * The control by service ({@code PLA.Servicio.ValidaAccesoServicio}, {@code ServiceFilter}) belongs to {@code /call} and comes with it.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FiltroSesion extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroSesion.class);

    static final String CABECERA_TOKEN = "X-SESSION-TOKEN";

    /** Legacy {@code SesionFilter.noAuthURLs} plus the public half of the bootstrap (the legacy exempted the whole {@code /PLA/bootstrap}). */
    static final List<String> SIN_SESION = List.of(
            "/PLA/ValidaClaveProvisoria", "/PLA/RecuperacionClave", "/PLA/CrearSesion", "/PLA/bootstrap/login",
            "/PLA/LoginCliente", "/PLA/SolicitudRecuperacionClave", "/PLA/ConfirmarCambioClave", "/PLA/CallBatch", "/PLA/WebStatic", "/PLA/RepoStatic",
            "/PLA/CallWebsocket", "/PLA/AuditoriaWS", "/PLA/Configuraciones", "/PLA/Ping", "/PLA/SSOInit", "/PLA/SSOLogon", "/PLA/SSOLogonBofa",
            "/PLA/SSOLogonGoogle", "/PLA/SSOLogonLocalGoogle", "/PLA/SSOLogout");

    static final String SQL_VALIDA_TOKEN = "SELECT COUNT(1) AS OK FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = ? and \"TokenValido\" = 'S'";

    private final DataSource dataSource;

    public FiltroSesion(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Paths that never carry a user session: the service-to-service endpoint (its own token filter), the health endpoints and the error page. */
    static boolean fueraDelAlcance(String ruta) {
        return ruta.equals("/internal") || ruta.startsWith("/internal/") || ruta.equals("/actuator") || ruta.startsWith("/actuator/") || ruta.equals("/error");
    }

    /** Whether a path as the client sent it (with the {@code /PLA} prefix) needs no session. */
    static boolean exenta(String ruta) {
        return SIN_SESION.contains(ruta) || ruta.startsWith("/PLA/WebStatic");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Object atributo = request.getAttribute(PrefijoPlaFilter.ATRIBUTO_RUTA_PLA);
        // closed by default: a request that came without the /PLA prefix is judged as if it had it (so /CrearSesion stays open and /call, /bootstrap/home,
        // /FileBlobManager... need the token); only the paths with their own protection or no session meaning are left out
        String ruta = atributo != null ? (String) atributo : "/PLA" + request.getRequestURI().substring(request.getContextPath().length());
        if (atributo == null && fueraDelAlcance(request.getRequestURI().substring(request.getContextPath().length()))) {
            chain.doFilter(request, response);
            return;
        }
        if (exenta(ruta)) {
            chain.doFilter(request, response);
            return;
        }
        String token = request.getHeader(CABECERA_TOKEN);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            return;
        }
        boolean valido;
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement(SQL_VALIDA_TOKEN)) {
                st.setString(1, token);
                try (ResultSet rs = st.executeQuery()) {
                    valido = rs.next() && rs.getInt("OK") != 0;
                }
            } catch (SQLException e) {
                log.warn("Validacion de sesion fallida: {}", e.getClass().getSimpleName());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Session");
                return;
            }
        } catch (SQLException e) {
            log.error("Sin conexion para validar la sesion: {}", e.getClass().getSimpleName());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error");
            return;
        }
        if (!valido) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Session");
            return;
        }
        chain.doFilter(request, response);
    }
}
