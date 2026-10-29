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
public class ProcesarIngresoEgresoTesoreriaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarIngresoEgresoTesoreriaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectModalidadPago;

    public ProcesarIngresoEgresoTesoreriaService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectModalidadPago = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectModalidadPago.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ProcesarIngresoEgresoTesoreria");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idModalidadPago = params.has("p_IdModalidadPago") ? params.get("p_IdModalidadPago").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdModalidadPago", idModalidadPago);

        List<Map<String, Object>> modalidades = jdbc.queryForList(sqlSelectModalidadPago, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : modalidades) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_MODALIDADES_PAGO", arr);
        return out;
    }
}
