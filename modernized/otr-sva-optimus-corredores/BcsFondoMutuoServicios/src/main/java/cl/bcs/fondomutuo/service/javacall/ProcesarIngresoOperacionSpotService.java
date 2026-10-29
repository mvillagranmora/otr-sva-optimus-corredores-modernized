package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProcesarIngresoOperacionSpotService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarIngresoOperacionSpotService.class);
    private static final Gson GSON = new Gson();

    // Inline SQL from legacy — selects the default mesa for MEME product type
    private static final String SQL_SELECT_MESA =
        "SELECT TOP 1 \"IdMesa\" FROM BSCL.\"BSCL.Mesa.BD::MES.Mesa\" " +
        "WHERE \"IdTipoProducto\" = 'MEME' AND \"IdEstadoRegistro\" = 'ACT' AND \"PorDefecto\" = 'S'";

    private final NamedParameterJdbcTemplate jdbc;

    public ProcesarIngresoOperacionSpotService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JsonObject execute(JsonObject params) {
        log.info("ProcesarIngresoOperacionSpot");

        List<Map<String, Object>> mesa = jdbc.queryForList(SQL_SELECT_MESA, new MapSqlParameterSource());

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : mesa) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_MESA", arr);
        return out;
    }
}
