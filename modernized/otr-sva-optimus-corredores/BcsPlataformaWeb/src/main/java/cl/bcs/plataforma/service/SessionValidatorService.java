package cl.bcs.plataforma.service;

import cl.bcs.plataforma.domain.SessionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Ports ProcesaLlamadaServletCall.logica() — validates session token and resolves
 * PLA.Servicio metadata in a single HANA round-trip sequence.
 * Source: BcsUtils/ProcesaLlamadaServletCall.java
 */
@Service
public class SessionValidatorService {

    // Source: ProcesaLlamadaServletCall.queryServicio
    private static final String SQL_SERVICIO =
        "select srv.\"RutaServicio\",srv.\"TipoServicio\",srv.\"TablasTempIN\"," +
        "srv.\"TablasTempOUT\",srv.\"RegistraUso\"," +
        "coalesce(string_agg(srvextdet.\"RutaServicio\",','),'') as \"Extras\"" +
        " from \"BSCL.Plataforma.BD::PLA.Servicio\" srv" +
        " left join \"BSCL.Plataforma.BD::PLA.ServicioExtra\" srvext" +
        "   on srv.\"IdServicio\" = srvext.\"IdServicio\" and srvext.\"IdCanal\" = :p_IdCanal" +
        " left join \"BSCL.Plataforma.BD::PLA.Servicio\" srvextdet" +
        "   on srvext.\"IdServicioExtra\" = srvextdet.\"IdServicio\"" +
        " where srv.\"IdServicio\" = :p_IdServicio" +
        " group by srv.\"RutaServicio\",srv.\"TipoServicio\",srv.\"TablasTempIN\"," +
        "srv.\"TablasTempOUT\",srv.\"RegistraUso\"";

    // Source: ProcesaLlamadaServletCall.querySesion
    private static final String SQL_SESION =
        "select \"IdUsuario\", \"FechaUltimoUso\" from \"BSCL.Plataforma.BD::ACC.Sesion\"" +
        " where \"IdTokenSesion\" = :p_Token and \"TokenValido\" = 'S'" +
        " and \"IdCanal\" = :p_IdCanal";

    // Source: ProcesaLlamadaServletCall.queryUsuario
    private static final String SQL_USUARIO =
        "select \"IdEmpresa\",\"FechaVigencia\",\"IdEstado\",\"CodigoUsuario\",\"TipoEntidad\"" +
        " from \"BSCL.Plataforma.BD::ACC.Usuario\" where \"IdUsuario\" = :p_IdUsuario";

    private final NamedParameterJdbcTemplate jdbc;
    private final int sessionTimeoutMinutes;

    public SessionValidatorService(
            NamedParameterJdbcTemplate jdbc,
            @Value("${plataforma.session-timeout-minutes:30}") int sessionTimeoutMinutes) {
        this.jdbc = jdbc;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    /**
     * Validates token + resolves service metadata. Returns SessionContext on success.
     * Throws ResponseStatusException (401 / 404) on failure — handled by GlobalExceptionHandler.
     */
    public SessionContext validate(String token, long idServicio, String canal) {
        // 1. Resolve PLA.Servicio
        var svcParams = new MapSqlParameterSource()
            .addValue("p_IdServicio", idServicio)
            .addValue("p_IdCanal", canal);
        List<Map<String, Object>> svcRows = jdbc.queryForList(SQL_SERVICIO, svcParams);
        if (svcRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Servicio " + idServicio + " no encontrado");
        }
        Map<String, Object> svc = svcRows.get(0);
        String rutaServicio  = str(svc, "RutaServicio");
        String tipoServicio  = str(svc, "TipoServicio");
        String tablasTempIn  = str(svc, "TablasTempIN");
        String tablasTempOut = str(svc, "TablasTempOUT");
        String registraUso   = str(svc, "RegistraUso");
        String extras        = str(svc, "Extras");

        // Services with RegistraUso='N' bypass session check
        if ("N".equalsIgnoreCase(registraUso)) {
            return new SessionContext(0L, 0L, "", "", rutaServicio, tipoServicio,
                tablasTempIn, tablasTempOut, extras);
        }

        // 2. Validate session token
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de sesión requerido");
        }

        var sesionParams = new MapSqlParameterSource()
            .addValue("p_Token",   token)
            .addValue("p_IdCanal", canal);
        List<Map<String, Object>> sesionRows = jdbc.queryForList(SQL_SESION, sesionParams);

        if (sesionRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada");
        }
        Map<String, Object> sesion = sesionRows.get(0);
        long idUsuario = ((Number) sesion.get("IdUsuario")).longValue();

        // Check idle timeout
        Timestamp ultimoUso = (Timestamp) sesion.get("FechaUltimoUso");
        if (ultimoUso != null) {
            long minutesIdle = ChronoUnit.MINUTES.between(
                ultimoUso.toInstant(), Instant.now());
            if (minutesIdle > sessionTimeoutMinutes) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada por inactividad");
            }
        }

        // 3. Load user details
        var usuarioParams = new MapSqlParameterSource()
            .addValue("p_IdUsuario", idUsuario);
        List<Map<String, Object>> usuarioRows = jdbc.queryForList(SQL_USUARIO, usuarioParams);
        if (usuarioRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }
        Map<String, Object> usr = usuarioRows.get(0);

        String idEstado = str(usr, "IdEstado");
        if (!"A".equalsIgnoreCase(idEstado)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
        }

        long   idEmpresa     = ((Number) usr.get("IdEmpresa")).longValue();
        String codigoUsuario = str(usr, "CodigoUsuario");
        String tipoUsuario   = str(usr, "TipoEntidad");

        return new SessionContext(idEmpresa, idUsuario, codigoUsuario, tipoUsuario,
            rutaServicio, tipoServicio, tablasTempIn, tablasTempOut, extras);
    }

    private static String str(Map<String, Object> row, String col) {
        Object v = row.get(col);
        return v == null ? "" : v.toString();
    }
}
