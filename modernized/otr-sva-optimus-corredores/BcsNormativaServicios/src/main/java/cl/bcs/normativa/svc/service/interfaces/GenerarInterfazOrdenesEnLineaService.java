package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GenerarInterfazOrdenesEnLineaService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInterfazOrdenesEnLineaService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_LOG;

    public GenerarInterfazOrdenesEnLineaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazOrdenesEnLinea_SelectInformacionOrdenesEnLinea.sql");
        this.SQL_LOG = loadSql("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInterfazOrdenesEnLineaService.execute called");
        MapSqlParameterSource p = buildParams(params);
        List<Map<String, Object>> rows = jdbc.queryForList(SQL_QUERY, p);
        if (!SQL_LOG.isBlank()) {
            MapSqlParameterSource logP = new MapSqlParameterSource();
            logP.addValue("IdInterfaz", 0L);
            logP.addValue("NumeroRegistros", (long) rows.size());
            logP.addValue("NumeroRegistrosCorrectos", (long) rows.size());
            logP.addValue("NumeroRegistrosIncorrectos", 0L);
            try { jdbc.update(SQL_LOG, logP); } catch (Exception e) { log.warn("Log insert failed", e); }
        }
        // TODO: legacy wrote fixed-width file to disk — file writing skipped in modernized service
        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_NombreArchivo", "");
        out.addProperty("p_out_Comentario", "Total Registros: " + rows.size());
        return out;
    }

    private MapSqlParameterSource buildParams(JsonObject params) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        params.entrySet().forEach(e -> {
            try { p.addValue(e.getKey(), e.getValue().getAsString()); } catch (Exception ex) { p.addValue(e.getKey(), null); }
        });
        return p;
    }

    private static String loadSql(String path) {
        try (var is = GenerarInterfazOrdenesEnLineaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
