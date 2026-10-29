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
public class ActualizarFechaGestionOperacionesService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarFechaGestionOperacionesService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ActualizarFechaGestionOperacionesService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarFechaGestionOperaciones_SelectFechas.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ActualizarFechaGestionOperaciones");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        List<Long> ids = new ArrayList<>();
        if (params.has("p_IdSolicitudAporteRescate") && params.get("p_IdSolicitudAporteRescate").isJsonArray()) {
            for (JsonElement e : params.getAsJsonArray("p_IdSolicitudAporteRescate")) {
                if (e.isJsonObject() && e.getAsJsonObject().has("IdSolicitudAporteRescate")) {
                    ids.add(e.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong());
                } else if (e.isJsonPrimitive()) {
                    ids.add(e.getAsLong());
                }
            }
        }

        List<Map<String, Object>> rows = ids.isEmpty() ? List.of() :
            jdbc.queryForList(sqlSelect, new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", ids).addValue("IdEmpresa", idEmpresa));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        return out;
    }
}
