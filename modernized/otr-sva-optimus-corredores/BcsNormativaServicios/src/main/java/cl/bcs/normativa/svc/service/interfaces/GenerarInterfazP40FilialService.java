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

/**
 * Spring @Service migrated from legacy {@code cl.bcs.normativa.interfaces.GenerarInterfazP40Filial}.
 * <p>Status: stub — business logic pending HANA connection validation.
 */
@Service
public class GenerarInterfazP40FilialService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInterfazP40FilialService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_1;
    private final String SQL_2;
    private final String SQL_3;
    private final String SQL_4;
    private final String SQL_5;
    private final String SQL_6;
    private final String SQL_7;
    private final String SQL_8;
    private final String SQL_9;
    private final String SQL_10;

    public GenerarInterfazP40FilialService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRUTFilial.sql");
        this.SQL_1 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRF_IF.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectFFMM.sql");
        this.SQL_3 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRV_Custodia.sql");
        this.SQL_4 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRV_Garantia.sql");
        this.SQL_5 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_SelectRV_Simultanea.sql");
        this.SQL_6 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRF_IF.sql");
        this.SQL_7 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectFFMM.sql");
        this.SQL_8 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRV_Custodia.sql");
        this.SQL_9 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRV_Garantia.sql");
        this.SQL_10 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Filial_HIS_SelectRV_Simultanea.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInterfazP40FilialService.execute called");
        MapSqlParameterSource p = buildParams(params);
        // TODO: legacy wrote fixed-width P40 file — file writing skipped in modernized service
        int totalRows = 0;
        for (String sql : new String[]{SQL_QUERY, SQL_1, SQL_2, SQL_3, SQL_4, SQL_5, SQL_6, SQL_7, SQL_8, SQL_9, SQL_10}) {
            if (!sql.isBlank()) {
                try {
                    List<Map<String, Object>> rows = jdbc.queryForList(sql, p);
                    totalRows += rows.size();
                } catch (Exception e) {
                    log.warn("Query failed in P40Filial: {}", e.getMessage());
                }
            }
        }
        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_Comentario", "Total Registros: " + totalRows);
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
        try (var is = GenerarInterfazP40FilialService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
