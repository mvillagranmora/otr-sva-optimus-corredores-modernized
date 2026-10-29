package cl.bcs.plataforma.service.com;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migrated from legacy cl.bcs.plataforma.com.CrearPerfilProductos (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils
 * Complete execute() after pla subpackage migration.
 */
@Service
public class CrearPerfilProductosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearPerfilProductosService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_INSERT_PERFIL = "cl/bcs/plataforma/com/sql/CrearPerfilProductos_Insert_Perfil.sql";
    private static final String PATH_SQL_INSERT_DETALLE = "cl/bcs/plataforma/com/sql/CrearPerfilProductos_Insert_Detalle.sql";

    public CrearPerfilProductosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.CrearPerfilProductos"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            String p_IdTipoProducto = params.has("p_IdTipoProducto") ? params.get("p_IdTipoProducto").getAsString() : null;
            String p_IdTipoOperacion = params.has("p_IdTipoOperacion") ? params.get("p_IdTipoOperacion").getAsString() : null;
            String p_IdCanalIngreso = params.has("p_IdCanalIngreso") ? params.get("p_IdCanalIngreso").getAsString() : null;
            String p_IdMoneda = params.has("p_IdMoneda") ? params.get("p_IdMoneda").getAsString() : null;
            String p_IdEstado = params.has("p_IdEstado") ? params.get("p_IdEstado").getAsString() : null;
            String p_IdGrupoActivos = params.has("p_IdGrupoActivos") ? params.get("p_IdGrupoActivos").getAsString() : null;

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
