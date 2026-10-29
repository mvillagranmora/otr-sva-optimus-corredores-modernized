package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ReniciarFolioOperacionOmnibusEmisor.
 */
@Service
public class ReniciarFolioOperacionOmnibusEmisorService {

    private static final Logger log = LoggerFactory.getLogger(ReniciarFolioOperacionOmnibusEmisorService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlUpdate;

    public ReniciarFolioOperacionOmnibusEmisorService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ReniciarFolioOperacionOmnibusEmisor_SelectConfiguracionEmisorOmnibusReinicioDiario.sql").getInputStream(),
            StandardCharsets.UTF_8);
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ReniciarFolioOperacionOmnibusEmisor_UpdateReinicioFolioOperacion.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ReniciarFolioOperacionOmnibusEmisor");
        List<Map<String, Object>> configs = jdbc.queryForList(sqlSelect, Collections.emptyMap());

        List<SqlParameterSource> batchParams = new ArrayList<>();
        for (Map<String, Object> row : configs) {
            batchParams.add(new MapSqlParameterSource()
                .addValue("FolioOperacion", 0L)
                .addValue("IdEmisorOmnibus", row.get("IdEmisorOmnibus")));
        }

        if (!batchParams.isEmpty()) {
            jdbc.batchUpdate(sqlUpdate, batchParams.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
