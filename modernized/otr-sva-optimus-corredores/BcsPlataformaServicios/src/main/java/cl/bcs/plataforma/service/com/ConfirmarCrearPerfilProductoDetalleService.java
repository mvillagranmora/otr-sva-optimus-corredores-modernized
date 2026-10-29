package cl.bcs.plataforma.service.com;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migrated from legacy cl.bcs.plataforma.com.ConfirmarCrearPerfilProductoDetalle (JavaCall).
 */
@Service
public class ConfirmarCrearPerfilProductoDetalleService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarCrearPerfilProductoDetalleService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;

    public ConfirmarCrearPerfilProductoDetalleService(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.ConfirmarCrearPerfilProductoDetalle"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long p_IdUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String p_IdModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String p_UsaReglasNegocio = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : null;
            Long p_IdDetallePerfilProductos = params.has("p_IdDetallePerfilProductos") ? params.get("p_IdDetallePerfilProductos").getAsLong() : null;
            String p_IdEstadoAutorizacion = params.has("p_IdEstadoAutorizacion") ? params.get("p_IdEstadoAutorizacion").getAsString() : null;

            // No SQL resources detected — logic may delegate to other services
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            log.error("Error en {}: {}", fqn(), e.getMessage(), e);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("P_OUT_MENSAJE", e.getMessage());
        }
        log.info("FIN::{}", fqn());
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("SQL not found: " + path);
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
