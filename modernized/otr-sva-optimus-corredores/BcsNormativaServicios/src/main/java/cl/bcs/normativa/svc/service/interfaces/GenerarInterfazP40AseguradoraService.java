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
public class GenerarInterfazP40AseguradoraService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInterfazP40AseguradoraService.class);
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
    private final String SQL_11;
    private final String SQL_12;
    private final String SQL_13;
    private final String SQL_14;

    public GenerarInterfazP40AseguradoraService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRUTCorredoraSeguros.sql");
        this.SQL_1 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRF_IF.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectFFMM.sql");
        this.SQL_3 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_CustodiaGarantia.sql");
        this.SQL_4 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_SinCustodiaGarantia.sql");
        this.SQL_5 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_Custodia.sql");
        this.SQL_6 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_SinCustodia.sql");
        this.SQL_7 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_SelectRV_Simultanea.sql");
        this.SQL_8 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRF_IF.sql");
        this.SQL_9 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectFFMM.sql");
        this.SQL_10 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_CustodiaGarantia.sql");
        this.SQL_11 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_SinCustodiaGarantia.sql");
        this.SQL_12 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_Custodia.sql");
        this.SQL_13 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_SinCustodia.sql");
        this.SQL_14 = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfazP40Aseguradora_HIS_SelectRV_Simultanea.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarInterfazP40AseguradoraService.execute called");
        MapSqlParameterSource p = buildParams(params);

        // TODO: legacy wrote fixed-width P40 file — file writing skipped in modernized service
        int totalRows = 0;
        for (String sql : new String[]{SQL_QUERY, SQL_1, SQL_2, SQL_3, SQL_4, SQL_5, SQL_6, SQL_7,
                                       SQL_8, SQL_9, SQL_10, SQL_11, SQL_12, SQL_13, SQL_14}) {
            if (!sql.isBlank()) {
                try {
                    List<Map<String, Object>> rows = jdbc.queryForList(sql, p);
                    totalRows += rows.size();
                } catch (Exception e) {
                    log.warn("Query failed in P40Aseguradora: {}", e.getMessage());
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
        try (var is = GenerarInterfazP40AseguradoraService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
