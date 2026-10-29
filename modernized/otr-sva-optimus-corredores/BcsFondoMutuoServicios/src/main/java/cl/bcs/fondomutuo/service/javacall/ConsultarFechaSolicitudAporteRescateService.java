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
public class ConsultarFechaSolicitudAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarFechaSolicitudAporteRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSerieFondo;
    private final String sqlDiaFeriado;

    public ConsultarFechaSolicitudAporteRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarFechaSolicitudAporteRescate_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDiaFeriado = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectDiaEsFeriado.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarFechaSolicitudAporteRescate");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;

        List<String> serieFondos = new ArrayList<>();
        if (params.has("p_SolicitudAporteRescate") && params.get("p_SolicitudAporteRescate").isJsonArray()) {
            for (JsonElement e : params.getAsJsonArray("p_SolicitudAporteRescate")) {
                JsonObject sol = e.getAsJsonObject();
                if (sol.has("IdSerieFondo")) serieFondos.add(sol.get("IdSerieFondo").getAsString());
            }
        }

        List<Map<String, Object>> seriesFondo = List.of();
        if (!serieFondos.isEmpty()) {
            seriesFondo = jdbc.queryForList(sqlSerieFondo,
                new MapSqlParameterSource().addValue("IdSerieFondo", serieFondos).addValue("IdEmpresa", idEmpresa));
        }

        List<Map<String, Object>> diasFeriado = jdbc.queryForList(sqlDiaFeriado,
            new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : seriesFondo) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES_FONDO", arrSeries);
        JsonArray arrFeriados = new JsonArray();
        for (Map<String, Object> row : diasFeriado) arrFeriados.add(GSON.toJsonTree(row));
        out.add("P_OUT_DIAS_FERIADO", arrFeriados);
        return out;
    }
}
