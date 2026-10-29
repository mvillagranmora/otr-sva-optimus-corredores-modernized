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
public class CargaReinversionesFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(CargaReinversionesFondoMutuoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlInsert;

    public CargaReinversionesFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargaReinversionesFondoMutuo_SelectReinversiones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsert = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargaReinversionesFondoMutuo_InsertReinversiones.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CargaReinversionesFondoMutuo");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaProceso = params.has("p_FechaProceso") ? params.get("p_FechaProceso").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaProceso", fechaProceso);

        List<Map<String, Object>> reinversiones = jdbc.queryForList(sqlSelect, ps);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (Map<String, Object> r : reinversiones) {
            batch.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", r.get("IdEmpresa"))
                .addValue("IdCliente", r.get("IdCliente"))
                .addValue("IdPortafolio", r.get("IdPortafolio"))
                .addValue("IdSerieFondo", r.get("IdSerieFondo"))
                .addValue("FechaProceso", fechaProceso));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlInsert, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : reinversiones) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_REINVERSIONES", arr);
        return out;
    }
}
