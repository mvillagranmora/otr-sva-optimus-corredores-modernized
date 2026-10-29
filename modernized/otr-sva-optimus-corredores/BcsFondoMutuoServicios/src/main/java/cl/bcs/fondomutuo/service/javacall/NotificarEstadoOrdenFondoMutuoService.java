package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificarEstadoOrdenFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(NotificarEstadoOrdenFondoMutuoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public NotificarEstadoOrdenFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/NotificarEstadoOrdenFondoMutuo_SelectSolicitudes.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("NotificarEstadoOrdenFondoMutuo");

        List<Long> solicitudes = new ArrayList<>();
        if (params.has("p_Solicitudes") && params.get("p_Solicitudes").isJsonArray()) {
            for (JsonElement e : params.getAsJsonArray("p_Solicitudes")) {
                solicitudes.add(e.getAsLong());
            }
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdSolicitudAporteRescate", solicitudes)
            .addValue("SegundosZonaHoraria", 0L);

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        return out;
    }
}
