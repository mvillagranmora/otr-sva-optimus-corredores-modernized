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
 * Migrated from legacy cl.bcs.plataforma.com.ConsultarMatrizOperacionalExcel (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils
 * Complete execute() after pla subpackage migration.
 */
@Service
public class ConsultarMatrizOperacionalExcelService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMatrizOperacionalExcelService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_SELECT_PLANTILLA_COBROS_BASE = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaCobrosBase.sql";
    private static final String PATH_SQL_SELECT_PLANTILLA_COBROS_EXTRA = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaCobrosExtra.sql";
    private static final String PATH_SQL_SELECT_PLANTILLA_PRODUCTOS_BASE = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaProductosBase.sql";
    private static final String PATH_SQL_SELECT_PLANTILLA_PRODUCTOS_EXTRA = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaProductosExtra.sql";

    public ConsultarMatrizOperacionalExcelService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.ConsultarMatrizOperacionalExcel"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;

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
