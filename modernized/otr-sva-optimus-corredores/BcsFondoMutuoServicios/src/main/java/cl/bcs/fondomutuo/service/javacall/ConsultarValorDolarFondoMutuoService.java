package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo.
 * Uses SQL path for non-dolar-observado mode. For dolar-observado mode delegates to
 * cl.bcs.plataforma.mer.CalcularParidad via /internal/java-call.
 */
@Service
public class ConsultarValorDolarFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarValorDolarFondoMutuoService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ConsultarValorDolarFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarValorDolarFondoMutuo");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray series = params.has("p_Series") ? params.getAsJsonArray("p_Series") : new JsonArray();

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray resultado = new JsonArray();

        if (series.size() > 0) {
            List<String> seriesIds = new ArrayList<>();
            for (JsonElement elem : series) {
                seriesIds.add(elem.getAsJsonObject().get("IdSerieFondo").getAsString());
            }

            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("IdSerieFondo", seriesIds)
                .addValue("IdEmpresa", idEmpresa);
            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, ps);

            for (Map<String, Object> row : rows) {
                JsonObject item = new JsonObject();
                item.addProperty("IdSerieFondo", String.valueOf(row.getOrDefault("IdSerieFondo", "")));
                item.addProperty("ValorParidad", row.get("ValorDolarAporte") != null
                    ? row.get("ValorDolarAporte").toString() : "0");
                resultado.add(item);
            }
        }

        out.add("P_OUT_RESULTADO", resultado);
        return out;
    }
}
