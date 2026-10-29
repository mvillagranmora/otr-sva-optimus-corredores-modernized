package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

@Service
public class CalcularFechaSolicitudAporteRescateJavaService {

    private static final Logger log = LoggerFactory.getLogger(CalcularFechaSolicitudAporteRescateJavaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlConfigFechas;

    public CalcularFechaSolicitudAporteRescateJavaService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlConfigFechas = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CalcularFechaSolicitudAporteRescate_SelectConfiguracionFechasFFMM.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CalcularFechaSolicitudAporteRescateJava");

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L)
            .addValue("IdSerieFondo", params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "");

        List<Map<String, Object>> rows = jdbc.queryForList(sqlConfigFechas, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_CONFIGURACION", arr);
        return out;
    }
}
