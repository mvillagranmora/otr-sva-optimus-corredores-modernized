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
 * Migrated from legacy cl.bcs.plataforma.com.CrearValorCobro (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, ConfiguracionSistema, AgregarLog
 * Complete execute() after pla subpackage migration.
 */
@Service
public class CrearValorCobroService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearValorCobroService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_COUNT_EXISTE = "cl/bcs/plataforma/com/sql/CrearValorCobro_Count_Existe.sql";
    private static final String PATH_SQL_INSERT_VALOR = "cl/bcs/plataforma/com/sql/CrearValorCobro_Insert_Valor.sql";

    public CrearValorCobroService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.CrearValorCobro"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, ConfiguracionSistema, AgregarLog) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long p_IdUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String p_IdModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String p_UsaReglasNegocio = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : null;
            String p_IdValorCobro = params.has("p_IdValorCobro") ? params.get("p_IdValorCobro").getAsString() : null;
            String p_DescripcionValorCobro = params.has("p_DescripcionValorCobro") ? params.get("p_DescripcionValorCobro").getAsString() : null;
            String p_IdTipoProducto = params.has("p_IdTipoProducto") ? params.get("p_IdTipoProducto").getAsString() : null;
            String p_IdCobro = params.has("p_IdCobro") ? params.get("p_IdCobro").getAsString() : null;
            java.math.BigDecimal p_ValorCobro = params.has("p_ValorCobro") ? params.get("p_ValorCobro").getAsBigDecimal() : null;
            java.math.BigDecimal p_ValorTopeMinimoOrden = params.has("p_ValorTopeMinimoOrden") ? params.get("p_ValorTopeMinimoOrden").getAsBigDecimal() : null;

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
