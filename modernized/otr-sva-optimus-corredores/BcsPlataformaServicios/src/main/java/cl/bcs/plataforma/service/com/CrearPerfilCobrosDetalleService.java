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
 * Migrated from legacy cl.bcs.plataforma.com.CrearPerfilCobrosDetalle (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, ConfiguracionSistema
 * Complete execute() after pla subpackage migration.
 */
@Service
public class CrearPerfilCobrosDetalleService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearPerfilCobrosDetalleService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_INSERT_DETALLE = "cl/bcs/plataforma/com/sql/CrearPerfilCobrosDetalle_Insert_Detalle.sql";
    private static final String PATH_SQL_INSERT_VALOR_COBRO = "cl/bcs/plataforma/com/sql/CrearPerfilCobrosDetalle_Insert_ValorCobro.sql";
    private static final String PATH_SQL_SELECT_GLOSA = "cl/bcs/plataforma/com/sql/CrearPerfilCobrosDetalle_Select_Glosa.sql";
    private static final String PATH_SQL_SELECT_REQUIERE_AUTORIZACION = "cl/bcs/plataforma/com/sql/CrearPerfilCobrosDetalle_Select_RequiereAutorizacion.sql";

    public CrearPerfilCobrosDetalleService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.CrearPerfilCobrosDetalle"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, ConfiguracionSistema) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long p_IdUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String p_IdModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String p_UsaReglasNegocio = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : null;
            String p_IdPerfilCobros = params.has("p_IdPerfilCobros") ? params.get("p_IdPerfilCobros").getAsString() : null;
            String p_IdValorCobro = params.has("p_IdValorCobro") ? params.get("p_IdValorCobro").getAsString() : null;
            String p_IdBolsa = params.has("p_IdBolsa") ? params.get("p_IdBolsa").getAsString() : null;
            String p_IdTipoProducto = params.has("p_IdTipoProducto") ? params.get("p_IdTipoProducto").getAsString() : null;
            String p_IdTipoOperacion = params.has("p_IdTipoOperacion") ? params.get("p_IdTipoOperacion").getAsString() : null;
            String p_IdCanalIngreso = params.has("p_IdCanalIngreso") ? params.get("p_IdCanalIngreso").getAsString() : null;

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
