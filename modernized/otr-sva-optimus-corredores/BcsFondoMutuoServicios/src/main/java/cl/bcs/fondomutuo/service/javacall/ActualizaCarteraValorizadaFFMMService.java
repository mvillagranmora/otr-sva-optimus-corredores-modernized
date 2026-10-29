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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActualizaCarteraValorizadaFFMMService {

    private static final Logger log = LoggerFactory.getLogger(ActualizaCarteraValorizadaFFMMService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlUpdate;

    public ActualizaCarteraValorizadaFFMMService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCarterasFondosMutuos_UpdateCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ActualizaCarteraValorizadaFFMM");

        JsonArray carteras = params.has("p_Carteras") ? params.getAsJsonArray("p_Carteras") : new JsonArray();
        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : carteras) {
            JsonObject c = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", c.has("IdEmpresa") ? c.get("IdEmpresa").getAsLong() : null)
                .addValue("IdCliente", c.has("IdCliente") ? c.get("IdCliente").getAsLong() : null)
                .addValue("IdPortafolio", c.has("IdPortafolio") ? c.get("IdPortafolio").getAsLong() : null)
                .addValue("IdInstrumento", c.has("IdInstrumento") ? c.get("IdInstrumento").getAsLong() : null)
                .addValue("ValorCartera", c.has("ValorCartera") && !c.get("ValorCartera").isJsonNull() ? c.get("ValorCartera").getAsBigDecimal() : BigDecimal.ZERO));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlUpdate, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
