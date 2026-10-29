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
public class CrearTraspasoService {

    private static final Logger log = LoggerFactory.getLogger(CrearTraspasoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSerieFondo;
    private final String sqlInsert;
    private final String sqlInsertDetalleOrigen;
    private final String sqlInsertDetalleDestino;

    public CrearTraspasoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSerieFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsert = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Insert.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertDetalleOrigen = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleOrigen.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertDetalleDestino = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleDestino.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearTraspaso");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        JsonArray traspasos = params.has("p_Traspasos") ? params.getAsJsonArray("p_Traspasos") : new JsonArray();

        MapSqlParameterSource psSelect = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo);
        List<Map<String, Object>> series = jdbc.queryForList(sqlSelectSerieFondo, psSelect);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdSerieFondo", t.has("IdSerieFondo") ? t.get("IdSerieFondo").getAsString() : ""));
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(sqlInsert, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertDetalleOrigen, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertDetalleDestino, batch.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : series) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arrSeries);
        return out;
    }
}
