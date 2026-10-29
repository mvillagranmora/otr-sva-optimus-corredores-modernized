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
public class CalcularMontoNumeroCuotaService {

    private static final Logger log = LoggerFactory.getLogger(CalcularMontoNumeroCuotaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlValorCuota;
    private final String sqlSerieFondo;

    public CalcularMontoNumeroCuotaService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlValorCuota = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CalcularMontoNumeroCuota");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;

        List<String> series = new ArrayList<>();
        if (params.has("p_Series") && params.get("p_Series").isJsonArray()) {
            for (JsonElement e : params.getAsJsonArray("p_Series")) {
                JsonObject s = e.getAsJsonObject();
                if (s.has("IdSerieFondo")) series.add(s.get("IdSerieFondo").getAsString());
            }
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", series.isEmpty() ? List.of("") : series);

        List<Map<String, Object>> valorCuota = jdbc.queryForList(sqlValorCuota, ps);
        List<Map<String, Object>> seriesFondo = series.isEmpty() ? List.of() :
            jdbc.queryForList(sqlSerieFondo, new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa).addValue("IdSerieFondo", series));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrValor = new JsonArray();
        for (Map<String, Object> row : valorCuota) arrValor.add(GSON.toJsonTree(row));
        out.add("P_OUT_RESULTADO", arrValor);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : seriesFondo) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES_FONDO", arrSeries);
        return out;
    }
}
