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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurarFondoAdministracionLiquidezService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurarFondoAdministracionLiquidezService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSerieFondo;
    private final String sqlSelectConf;
    private final String sqlInsertConf;
    private final String sqlUpdateConf;

    public ConfigurarFondoAdministracionLiquidezService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConf = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConfigurarFondoAdministracionLiquidez_SelectConfiguracion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertConf = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConfigurarFondoAdministracionLiquidez_InsertConfiguracion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateConf = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConfigurarFondoAdministracionLiquidez_UpdateConfiguracion.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ConfigurarFondoAdministracionLiquidez");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", List.of(idSerieFondo));

        List<Map<String, Object>> series = jdbc.queryForList(sqlSerieFondo, ps);

        MapSqlParameterSource psConf = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("PorcentajeMinimo", params.has("p_PorcentajeMinimo") && !params.get("p_PorcentajeMinimo").isJsonNull() ? params.get("p_PorcentajeMinimo").getAsBigDecimal() : BigDecimal.ZERO)
            .addValue("PorcentajeMaximo", params.has("p_PorcentajeMaximo") && !params.get("p_PorcentajeMaximo").isJsonNull() ? params.get("p_PorcentajeMaximo").getAsBigDecimal() : BigDecimal.ZERO);

        List<Map<String, Object>> existing = jdbc.queryForList(sqlSelectConf, psConf);

        if (existing.isEmpty()) {
            jdbc.update(sqlInsertConf, psConf);
        } else {
            jdbc.update(sqlUpdateConf, psConf);
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : series) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arrSeries);
        return out;
    }
}
