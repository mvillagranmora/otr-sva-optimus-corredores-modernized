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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ValidaPatrimonioEmisorService {

    private static final Logger log = LoggerFactory.getLogger(ValidaPatrimonioEmisorService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSerieFondo;
    private final String sqlDeleteTablaPaso;
    private final String sqlInsertTablaPaso;
    private final String sqlSelectCustodia;

    public ValidaPatrimonioEmisorService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSerieFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDeleteTablaPaso = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_DeleteTablaPaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertTablaPaso = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_InsertTablaPaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCustodia = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_SelectCustodia.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ValidaPatrimonioEmisor");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idEmisor = params.has("p_IdEmisor") ? params.get("p_IdEmisor").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdEmisor", idEmisor);

        List<Map<String, Object>> series = jdbc.queryForList(sqlSelectSerieFondo, ps);

        jdbc.update(sqlDeleteTablaPaso, ps);
        jdbc.update(sqlInsertTablaPaso, ps);

        List<Map<String, Object>> custodia = jdbc.queryForList(sqlSelectCustodia, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : series) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arrSeries);
        JsonArray arrCust = new JsonArray();
        for (Map<String, Object> row : custodia) arrCust.add(GSON.toJsonTree(row));
        out.add("P_OUT_CUSTODIA", arrCust);
        return out;
    }
}
