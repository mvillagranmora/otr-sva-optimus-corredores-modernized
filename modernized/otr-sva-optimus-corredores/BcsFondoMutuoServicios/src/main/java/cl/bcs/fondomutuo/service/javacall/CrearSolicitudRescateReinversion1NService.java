package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.List;
import java.util.Map;

@Service
public class CrearSolicitudRescateReinversion1NService {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudRescateReinversion1NService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSerieFondo;
    private final String sqlSelectValorCuotaTipoCambio;

    public CrearSolicitudRescateReinversion1NService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectValorCuotaTipoCambio = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CrearSolicitudRescateReinversion1N");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        String fechaValor = params.has("p_FechaValor") ? params.get("p_FechaValor").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("FechaValor", fechaValor);

        List<Map<String, Object>> series = jdbc.queryForList(sqlSelectSerieFondo, ps);
        List<Map<String, Object>> valorCuota = jdbc.queryForList(sqlSelectValorCuotaTipoCambio, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : series) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIE_FONDO", arrSeries);
        JsonArray arrVC = new JsonArray();
        for (Map<String, Object> row : valorCuota) arrVC.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALOR_CUOTA", arrVC);
        return out;
    }
}
