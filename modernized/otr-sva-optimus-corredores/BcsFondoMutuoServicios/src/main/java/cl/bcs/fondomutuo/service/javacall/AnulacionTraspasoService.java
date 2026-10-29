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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnulacionTraspasoService {

    private static final Logger log = LoggerFactory.getLogger(AnulacionTraspasoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlUpdate;

    public AnulacionTraspasoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Anulacion_SelectTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Anulacion_UpdateTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("AnulacionTraspaso");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray traspasos = params.has("p_Traspasos") ? params.getAsJsonArray("p_Traspasos") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            if (t.has("IdTraspaso")) ids.add(t.get("IdTraspaso").getAsLong());
        }

        List<Map<String, Object>> existing = List.of();
        if (!ids.isEmpty()) {
            existing = jdbc.queryForList(sqlSelect,
                new MapSqlParameterSource().addValue("IdTraspaso", ids).addValue("IdEmpresa", idEmpresa));
        }

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdTraspaso", t.has("IdTraspaso") ? t.get("IdTraspaso").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlUpdate, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : existing) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_TRASPASOS", arr);
        return out;
    }
}
