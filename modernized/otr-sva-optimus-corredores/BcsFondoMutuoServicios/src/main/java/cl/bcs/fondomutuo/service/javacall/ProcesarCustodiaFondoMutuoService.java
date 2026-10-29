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
public class ProcesarCustodiaFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCustodiaFondoMutuoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCarteras;
    private final String sqlSelectCarterasRetiradas;

    public ProcesarCustodiaFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectCarteras = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarCustodiaFondoMutuo_SelectCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarterasRetiradas = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarCustodiaFondoMutuo_SelectCarterasRetiradas.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ProcesarCustodiaFondoMutuo");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaCuadratura = params.has("p_FechaCuadratura") ? params.get("p_FechaCuadratura").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaCuadratura", fechaCuadratura);

        List<Map<String, Object>> carteras = jdbc.queryForList(sqlSelectCarteras, ps);
        List<Map<String, Object>> carterasRetiradas = jdbc.queryForList(sqlSelectCarterasRetiradas, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrC = new JsonArray();
        for (Map<String, Object> row : carteras) arrC.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERAS", arrC);
        JsonArray arrCR = new JsonArray();
        for (Map<String, Object> row : carterasRetiradas) arrCR.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERAS_RETIRADAS", arrCR);
        return out;
    }
}
