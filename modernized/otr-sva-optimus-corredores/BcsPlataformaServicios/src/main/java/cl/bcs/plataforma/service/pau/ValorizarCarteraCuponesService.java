package cl.bcs.plataforma.service.pau;

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
 * Migrated from legacy cl.bcs.plataforma.pau.ValorizarCarteraCupones (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, ConfiguracionSistema, AgregarLog
 * Complete execute() after pla subpackage migration.
 */
@Service
public class ValorizarCarteraCuponesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValorizarCarteraCuponesService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_SelectCartera = "cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraCupones.sql";

    public ValorizarCarteraCuponesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pau.ValorizarCarteraCupones"; }

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
            String p_FechaSistema = params.has("p_FechaSistema") ? params.get("p_FechaSistema").getAsString() : null;
            Long p_IdPais = params.has("p_IdPais") ? params.get("p_IdPais").getAsLong() : null;
            String p_SimularOperacion = params.has("p_SimularOperacion") ? params.get("p_SimularOperacion").getAsString() : null;
            Long p_IdPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : null;
            Long p_IdCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : null;

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
