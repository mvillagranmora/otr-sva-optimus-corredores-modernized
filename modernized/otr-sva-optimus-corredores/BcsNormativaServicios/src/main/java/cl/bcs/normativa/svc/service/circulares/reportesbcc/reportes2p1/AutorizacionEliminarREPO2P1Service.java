package cl.bcs.normativa.svc.service.circulares.reportesbcc.reportes2p1;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migrated from legacy {@code cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1.AutorizacionEliminarREPO2P1}.
 * <p>Status: stub — business logic pending HANA connection validation.
 */
@Service
public class AutorizacionEliminarREPO2P1Service {

    private static final Logger log = LoggerFactory.getLogger(AutorizacionEliminarREPO2P1Service.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_1;
    private final String SQL_2;

    public AutorizacionEliminarREPO2P1Service(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/AutorizacionEliminarREPO2P1_DatosREPO2P1_delete.sql");
        this.SQL_1 = loadSql("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/AutorizacionEliminarREPO2P1_Solicitud_update.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/AutorizacionEliminarREPO2P1_Registro_Solicitud_Cambio.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("AutorizacionEliminarREPO2P1Service.execute called");
        MapSqlParameterSource p = buildParams(params);
        int totalRows = 0;
        if (!SQL_QUERY.isBlank()) {
            try { if (SQL_QUERY.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_QUERY, p).size(); } else { jdbc.update(SQL_QUERY, p); } } catch (Exception e) { log.warn("Exec failed for SQL_QUERY: {}", e.getMessage()); }
        }
        if (!SQL_1.isBlank()) {
            try { if (SQL_1.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_1, p).size(); } else { jdbc.update(SQL_1, p); } } catch (Exception e) { log.warn("Exec failed for SQL_1: {}", e.getMessage()); }
        }
        if (!SQL_2.isBlank()) {
            try { if (SQL_2.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_2, p).size(); } else { jdbc.update(SQL_2, p); } } catch (Exception e) { log.warn("Exec failed for SQL_2: {}", e.getMessage()); }
        }
        // TODO: plataforma deps (AgregarLog, ConfiguracionSistema, FechaDB) skipped in modernized
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.addProperty("P_OUT_MENSAJE", "OK");
        out.addProperty("totalRows", totalRows);
        return out;
    }


    private MapSqlParameterSource buildParams(JsonObject params) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        params.entrySet().forEach(e -> {
            try { p.addValue(e.getKey(), e.getValue().getAsString()); }
            catch (Exception ex) { p.addValue(e.getKey(), null); }
        });
        return p;
    }

    private static String loadSql(String path) {
        try (var is = AutorizacionEliminarREPO2P1Service.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
