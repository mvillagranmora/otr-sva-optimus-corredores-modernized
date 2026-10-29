package cl.bcs.fondomutuo.service.javacall;

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
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.QuitarTramoComision.
 */
@Service
public class QuitarTramoComisionService {

    private static final Logger log = LoggerFactory.getLogger(QuitarTramoComisionService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlUpdate;

    public QuitarTramoComisionService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/QuitarTramoComision_UpdateTramo.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("QuitarTramoComision");
        JsonArray tramos = params.has("p_TramoComision") ? params.getAsJsonArray("p_TramoComision") : new JsonArray();

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement elem : tramos) {
            JsonObject t = elem.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdTramoComision", getL(t, "IdTramoComision"))
                .addValue("IdEmpresa", getL(t, "IdEmpresa")));
        }

        if (!batch.isEmpty()) jdbc.batchUpdate(sqlUpdate, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    private Long getL(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsLong() : null; }
}
