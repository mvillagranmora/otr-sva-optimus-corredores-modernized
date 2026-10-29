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

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.
 */
@Service
public class ConsultarFlujoCustodiaFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarFlujoCustodiaFondoMutuoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlRescateTransito;
    private final String sqlSerieFondo;
    private final String sqlValorCuota;

    public ConsultarFlujoCustodiaFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlRescateTransito = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarFlujoCustodiaFondoMutuo_SelectRescateTotalTransito.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarFlujoCustodiaFondoMutuo_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlValorCuota = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarFlujoCustodiaFondoMutuo_SelectValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarFlujoCustodiaFondoMutuo");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaSaldo = params.has("p_FechaSaldo") ? params.get("p_FechaSaldo").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaSaldo", fechaSaldo);

        List<Map<String, Object>> seriesFondo = jdbc.queryForList(sqlSerieFondo, ps);
        List<Map<String, Object>> rescatesTransito = jdbc.queryForList(sqlRescateTransito, ps);
        List<Map<String, Object>> valoresCuota = jdbc.queryForList(sqlValorCuota, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : seriesFondo) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arr);
        JsonArray arrRescates = new JsonArray();
        for (Map<String, Object> row : rescatesTransito) arrRescates.add(GSON.toJsonTree(row));
        out.add("P_OUT_RESCATES_TRANSITO", arrRescates);
        return out;
    }
}
