package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarDemandaAsignacion (JavaCall).
 */
@Service
public class ConsultarDemandaAsignacionService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDemandaAsignacionService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT;

    public ConsultarDemandaAsignacionService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.SQL_SELECT = loadSql("cl/bcs/ordenes/sql/ConsultarDemandaAsignacion_SelectOrdenes.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarDemandaAsignacionService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            String idMonedaSistema = configUtil.getParametroString(idEmpresa, "PLA.MonedaSistema");

            MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdInstrumento", params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsLong() : null)
                .addValue("EsVentaCorta", params.has("p_EsVentaCorta") ? params.get("p_EsVentaCorta").getAsString() : "N")
                .addValue("IdTipoInstrumento", params.has("p_IdTipoInstrumento") ? params.get("p_IdTipoInstrumento").getAsString() : null)
                .addValue("FechaConsulta", toDate(params.has("p_FechaConsulta") ? params.get("p_FechaConsulta").getAsString() : null));

            List<Map<String, Object>> demanda = jdbc.queryForList(SQL_SELECT, sqlParams);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> row : demanda) {
                row.put("IdMoneda", idMonedaSistema);
                result.add(row);
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_DEMANDA", JsonParser.parseString(GSON.toJson(result)));
            return out;
        } catch (Exception e) {
            log.error("ConsultarDemandaAsignacionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static Date toDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Date.valueOf(s.substring(0, 10)); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarDemandaAsignacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
