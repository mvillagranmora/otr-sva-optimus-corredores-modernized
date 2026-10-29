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
import java.util.Map;

@Service
public class ActualizarFondoIndustriaService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarFondoIndustriaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInsert;
    private final String sqlUpdate;
    private final String sqlSelect;

    public ActualizarFondoIndustriaService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlInsert = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarFondoIndustria_FondoIndustria_Insert.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarFondoIndustria_FondoIndustria_Update.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarFondoIndustria_FondoIndustria_Select.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ActualizarFondoIndustria");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fecha = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : "";
        JsonArray fondos = params.has("p_FondosIndustria") ? params.getAsJsonArray("p_FondosIndustria") : new JsonArray();

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa).addValue("Fecha", fecha);
        List<Map<String, Object>> existing = jdbc.queryForList(sqlSelect, ps);

        List<SqlParameterSource> batchInsert = new ArrayList<>();
        List<SqlParameterSource> batchUpdate = new ArrayList<>();

        for (JsonElement e : fondos) {
            JsonObject f = e.getAsJsonObject();
            MapSqlParameterSource fPs = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("Fecha", fecha)
                .addValue("IdSerieFondo", f.has("IdSerieFondo") ? f.get("IdSerieFondo").getAsString() : "")
                .addValue("ValorPatrimonio", f.has("ValorPatrimonio") && !f.get("ValorPatrimonio").isJsonNull() ? f.get("ValorPatrimonio").getAsBigDecimal() : BigDecimal.ZERO)
                .addValue("NumeroCuotas", f.has("NumeroCuotas") && !f.get("NumeroCuotas").isJsonNull() ? f.get("NumeroCuotas").getAsBigDecimal() : BigDecimal.ZERO);
            batchInsert.add(fPs);
        }

        if (!batchInsert.isEmpty()) {
            try { jdbc.batchUpdate(sqlInsert, batchInsert.toArray(new SqlParameterSource[0])); }
            catch (Exception ex) {
                // If insert fails (duplicate), try update
                jdbc.batchUpdate(sqlUpdate, batchInsert.toArray(new SqlParameterSource[0]));
            }
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : existing) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_FONDOS", arr);
        return out;
    }
}
