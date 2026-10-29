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
 * Migrated from legacy cl.bcs.plataforma.pau.AgregarLogValorizadorPauta (JavaCall).
 * Dependencies on pla.* utilities: AgregarLog
 * Complete execute() after pla subpackage migration.
 */
@Service
public class AgregarLogValorizadorPautaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AgregarLogValorizadorPautaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_SelectLogValorizadorPauta = "cl/bcs/plataforma/pau/sql/AgregarLogValorizadorPauta_SEL_LogValorizadorPauta.sql";
    private static final String PATH_SQL_SelectDetalleLogValorizadorPauta = "cl/bcs/plataforma/pau/sql/AgregarLogValorizadorPauta_SEL_DetalleLogValorizadorPauta.sql";
    private static final String PATH_SQL_UpdateRutaLog = "cl/bcs/plataforma/pau/sql/AgregarLogValorizadorPauta_UPD_RutaLog.sql";

    public AgregarLogValorizadorPautaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pau.AgregarLogValorizadorPauta"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (AgregarLog) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdLogValorizadorPauta = params.has("p_IdLogValorizadorPauta") ? params.get("p_IdLogValorizadorPauta").getAsLong() : null;
            String p_RutaSalidaArchivo = params.has("p_RutaSalidaArchivo") ? params.get("p_RutaSalidaArchivo").getAsString() : null;

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
