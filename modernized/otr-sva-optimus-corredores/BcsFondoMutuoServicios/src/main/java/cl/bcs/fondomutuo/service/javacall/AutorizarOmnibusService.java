package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.AutorizarOmnibus.
 */
@Service
public class AutorizarOmnibusService {

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlUpdateOmnibusDetalle;

    public AutorizarOmnibusService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdateOmnibusDetalle = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AutorizarOmnibus_UpdateOmnibusDetalle.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        String idEstado = params.has("p_IdEstado") ? params.get("p_IdEstado").getAsString() : "";
        JsonArray omnibusArray = params.has("p_Omnibus") ? params.getAsJsonArray("p_Omnibus") : new JsonArray();

        List<SqlParameterSource> batchParams = new ArrayList<>();
        for (int i = 0; i < omnibusArray.size(); i++) {
            long idOmnibusDetalle = omnibusArray.get(i).getAsLong();
            batchParams.add(new MapSqlParameterSource()
                .addValue("IdOmnibusDetalle", idOmnibusDetalle)
                .addValue("IdEstado", idEstado));
        }

        if (!batchParams.isEmpty()) {
            jdbc.batchUpdate(sqlUpdateOmnibusDetalle, batchParams.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
