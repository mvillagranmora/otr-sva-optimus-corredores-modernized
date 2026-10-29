package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ConsultarValidacionDJ1932PorAgnoComercial.
 */
@Service
public class ConsultarValidacionDJ1932PorAgnoComercialService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarValidacionDJ1932PorAgnoComercialService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ConsultarValidacionDJ1932PorAgnoComercialService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarValidacionDJ1932PorAgnoComercial_SelectValidacionDJ1932.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        String agnoComercial = params.has("p_AgnoComercial") ? params.get("p_AgnoComercial").getAsString() : "";

        long advertencias = 0L;
        if (!agnoComercial.isEmpty()) {
            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect,
                new MapSqlParameterSource("AgnoComercial", agnoComercial));
            String codigo = rows.isEmpty() ? "N" : String.valueOf(rows.get(0).getOrDefault("CodigoValidacion", "N"));
            if ("N".equals(codigo)) {
                advertencias = 1L;
            }
        } else {
            advertencias = 1L;
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ADVERTENCIASCONTADOR", advertencias);
        return out;
    }
}
