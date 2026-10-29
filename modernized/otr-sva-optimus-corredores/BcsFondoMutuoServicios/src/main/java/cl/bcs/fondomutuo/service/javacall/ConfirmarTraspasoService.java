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
public class ConfirmarTraspasoService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarTraspasoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectTraspaso;
    private final String sqlUpdateTraspaso;
    private final String sqlSelectOrigen;
    private final String sqlUpdateOrigen;
    private final String sqlSelectDestino;

    public ConfirmarTraspasoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectTraspaso = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_SelectTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateTraspaso = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_UpdateTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectOrigen = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_SelectTraspasoOrigen.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateOrigen = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_UpdateTraspasoOrigen.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectDestino = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_SelectTraspasoDestino.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ConfirmarTraspaso");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray traspasos = params.has("p_Traspasos") ? params.getAsJsonArray("p_Traspasos") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            if (t.has("IdTraspaso")) ids.add(t.get("IdTraspaso").getAsLong());
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdTraspaso", ids.isEmpty() ? List.of(0L) : ids);

        List<Map<String, Object>> existing = jdbc.queryForList(sqlSelectTraspaso, ps);
        List<Map<String, Object>> origen = jdbc.queryForList(sqlSelectOrigen, ps);
        List<Map<String, Object>> destino = jdbc.queryForList(sqlSelectDestino, ps);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdTraspaso", t.has("IdTraspaso") ? t.get("IdTraspaso").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa));
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(sqlUpdateTraspaso, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateOrigen, batch.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrT = new JsonArray();
        for (Map<String, Object> row : existing) arrT.add(GSON.toJsonTree(row));
        out.add("P_OUT_TRASPASOS", arrT);
        JsonArray arrO = new JsonArray();
        for (Map<String, Object> row : origen) arrO.add(GSON.toJsonTree(row));
        out.add("P_OUT_ORIGEN", arrO);
        JsonArray arrD = new JsonArray();
        for (Map<String, Object> row : destino) arrD.add(GSON.toJsonTree(row));
        out.add("P_OUT_DESTINO", arrD);
        return out;
    }
}
